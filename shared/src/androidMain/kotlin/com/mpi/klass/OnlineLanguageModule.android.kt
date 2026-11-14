package com.mpi.klass

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mpi.klass.theme.getAppContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds

private const val TAG = "OnlineLanguageModule"
private const val PREFS_NAME = "klass_online_dict"
private const val KEY_DICT_JSON = "bn_to_en_json"

actual fun loadCachedTranslations(): Map<String, String> {
    return try {
        val ctx = getAppContext() ?: return emptyMap()
        val raw = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DICT_JSON, null) ?: return emptyMap()
        val json = JSONObject(raw)
        val result = HashMap<String, String>(json.length())
        val keys = json.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = json.optString(k, "")
            if (k.isNotBlank() && v.isNotBlank()) {
                result[k] = v
            }
        }
        result
    } catch (e: Exception) {
        Log.w(TAG, "Failed to load cached dictionary: ${e.message}")
        emptyMap()
    }
}

actual fun saveCachedTranslations(entries: Map<String, String>) {
    if (entries.isEmpty()) return
    try {
        val ctx = getAppContext() ?: return
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingRaw = prefs.getString(KEY_DICT_JSON, null)
        val json = if (!existingRaw.isNullOrBlank()) {
            try { JSONObject(existingRaw) } catch (_: Exception) { JSONObject() }
        } else {
            JSONObject()
        }
        for ((k, v) in entries) {
            if (k.isNotBlank() && v.isNotBlank()) {
                json.put(k, v)
            }
        }
        prefs.edit().putString(KEY_DICT_JSON, json.toString()).apply()
    } catch (e: Exception) {
        Log.w(TAG, "Failed to save cached dictionary: ${e.message}")
    }
}

actual suspend fun fetchOnlineDictionaryFromCloud(): Map<String, String> = withContext(Dispatchers.IO) {
    try {
        val snapshot = withTimeoutOrNull(4.seconds) {
            FirebaseFirestore.getInstance()
                .collection("config")
                .document("language_dictionary")
                .get()
                .awaitTask()
        } ?: return@withContext emptyMap()

        if (!snapshot.exists()) return@withContext emptyMap()
        val data = snapshot.data ?: return@withContext emptyMap()
        val result = HashMap<String, String>()

        val nestedEn = data["en"] as? Map<*, *>
        if (nestedEn != null) {
            for ((k, v) in nestedEn) {
                val keyStr = k?.toString()?.trim().orEmpty()
                val valStr = v?.toString()?.trim().orEmpty()
                if (keyStr.isNotBlank() && valStr.isNotBlank()) {
                    result[keyStr] = valStr
                }
            }
        }
        for ((k, v) in data) {
            if (k != "en" && v is String && k.isNotBlank() && v.isNotBlank()) {
                result[k.trim()] = v.trim()
            }
        }
        result
    } catch (e: Exception) {
        Log.w(TAG, "Cloud dictionary fetch skipped: ${e.message}")
        emptyMap()
    }
}

actual suspend fun fetchOnlineTranslations(texts: List<String>): Map<String, String> = withContext(Dispatchers.IO) {
    val distinct = texts.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    if (distinct.isEmpty()) return@withContext emptyMap()

    val result = HashMap<String, String>(distinct.size)
    val singleLineItems = distinct.filter { !it.contains('\n') }
    val multiLineItems = distinct.filter { it.contains('\n') }

    // 1. Translate single-line strings in efficient newline-delimited batches
    val batches = mutableListOf<List<String>>()
    var currentBatch = mutableListOf<String>()
    var currentLen = 0
    for (item in singleLineItems) {
        if (currentBatch.size >= 16 || currentLen + item.length > 750) {
            if (currentBatch.isNotEmpty()) {
                batches.add(currentBatch)
                currentBatch = mutableListOf()
                currentLen = 0
            }
        }
        currentBatch.add(item)
        currentLen += item.length + 1
    }
    if (currentBatch.isNotEmpty()) {
        batches.add(currentBatch)
    }

    val fallbackSingles = java.util.Collections.synchronizedList(mutableListOf<String>())
    fallbackSingles.addAll(multiLineItems)

    val batchSemaphore = Semaphore(4)
    coroutineScope {
        batches.map { batch ->
            async {
                batchSemaphore.withPermit {
                    val joined = batch.joinToString("\n")
                    val translated = translateViaHttpApi(joined)
                    if (!translated.isNullOrBlank()) {
                        val lines = translated.split("\n")
                        if (lines.size == batch.size) {
                            synchronized(result) {
                                batch.forEachIndexed { idx, original ->
                                    val clean = lines[idx].trim()
                                    if (clean.isNotBlank()) {
                                        result[original] = clean
                                    } else {
                                        fallbackSingles.add(original)
                                    }
                                }
                            }
                        } else {
                            fallbackSingles.addAll(batch)
                        }
                    } else {
                        fallbackSingles.addAll(batch)
                    }
                }
            }
        }.awaitAll()
    }

    // 2. Fallback individual translation for multi-line or misaligned batch items
    if (fallbackSingles.isNotEmpty()) {
        val singleSemaphore = Semaphore(6)
        coroutineScope {
            fallbackSingles.distinct().map { item ->
                async {
                    singleSemaphore.withPermit {
                        val translated = translateViaHttpApi(item)?.trim()
                        if (!translated.isNullOrBlank()) {
                            synchronized(result) {
                                result[item] = translated
                            }
                        }
                    }
                }
            }.awaitAll()
        }
    }

    result
}

private fun translateViaHttpApi(text: String): String? {
    var conn: HttpURLConnection? = null
    return try {
        val encoded = URLEncoder.encode(text, "UTF-8")
        val urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=bn&tl=en&dt=t&q=$encoded"
        val url = URL(urlStr)
        conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }
        if (conn.responseCode != HttpURLConnection.HTTP_OK) return null

        val responseText = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
        val root = JSONArray(responseText)
        val segments = root.optJSONArray(0) ?: return null
        val sb = StringBuilder()
        for (i in 0 until segments.length()) {
            val seg = segments.optJSONArray(i) ?: continue
            val piece = seg.optString(0, "")
            sb.append(piece)
        }
        sb.toString().ifBlank { null }
    } catch (e: Exception) {
        Log.w(TAG, "Online translate error: ${e.message}")
        null
    } finally {
        try { conn?.disconnect() } catch (_: Exception) {}
    }
}
