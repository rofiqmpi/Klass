@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.mpi.klass

import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@JsFun("(key) => { try { return window.localStorage.getItem(key); } catch (e) { return null; } }")
internal external fun jsStorageGet(key: String): String?

@JsFun("(key, value) => { try { window.localStorage.setItem(key, value); } catch (e) {} }")
internal external fun jsStorageSet(key: String, value: String)

@JsFun("(key) => { try { window.localStorage.removeItem(key); } catch (e) {} }")
internal external fun jsStorageRemove(key: String)

@JsFun("() => Date.now()")
internal external fun jsNowMillis(): Double

@JsFun("() => new Date().getDay()")
internal external fun jsDayOfWeek(): Int

@JsFun("() => new Date().getFullYear()")
internal external fun jsYear(): Int

@JsFun("() => new Date().getMonth() + 1")
internal external fun jsMonth(): Int

@JsFun("() => new Date().getDate()")
internal external fun jsDayOfMonth(): Int

@JsFun("(message) => console.log(message)")
internal external fun jsLog(message: String)

@JsFun("() => (typeof navigator !== 'undefined' && typeof navigator.onLine === 'boolean') ? navigator.onLine : true")
internal external fun jsIsOnline(): Boolean

@JsFun("() => Boolean(window.__klassFirebase && window.__klassFirebase.isConfigured())")
internal external fun jsHasFirebaseBridge(): Boolean

@JsFun("(msg) => { if (window.__klassFirebase && window.__klassFirebase.showToast) window.__klassFirebase.showToast(msg); else console.log(msg); }")
internal external fun jsShowToast(msg: String)

@JsFun("() => (window.__klassFirebase && window.__klassFirebase.randomSaltHex) ? window.__klassFirebase.randomSaltHex() : '0123456789abcdef0123456789abcdef'")
internal external fun jsRandomSaltHex(): String

@JsFun("(saltHex, password, cb) => window.__klassFirebase.pbkdf2Hash(saltHex, password, cb)")
internal external fun jsPbkdf2Hash(saltHex: String, password: String, cb: (String) -> Unit)

@JsFun("(cb) => window.__klassFirebase.authStatus(cb)")
internal external fun jsAuthStatus(cb: (String) -> Unit)

@JsFun("(cb) => window.__klassFirebase.signInWithGoogle(cb)")
internal external fun jsSignInWithGoogle(cb: (String) -> Unit)

@JsFun("(cb) => window.__klassFirebase.signOutGoogle(cb)")
internal external fun jsSignOutGoogle(cb: (String) -> Unit)

@JsFun("(email, password, cb) => window.__klassFirebase.signInWithEmail(email, password, cb)")
internal external fun jsSignInWithEmail(email: String, password: String, cb: (String) -> Unit)

@JsFun("(email, password, cb) => window.__klassFirebase.createUserWithEmail(email, password, cb)")
internal external fun jsCreateUserWithEmail(email: String, password: String, cb: (String) -> Unit)

@JsFun("(email, newPassword, currentPassword, hasCurrent, cb) => window.__klassFirebase.setOrUpdateAuthPassword(email, newPassword, currentPassword, hasCurrent, cb)")
internal external fun jsSetOrUpdateAuthPassword(
    email: String,
    newPassword: String,
    currentPassword: String,
    hasCurrent: Boolean,
    cb: (String) -> Unit,
)

@JsFun("(cb) => window.__klassFirebase.deleteCurrentAuthUser(cb)")
internal external fun jsDeleteCurrentAuthUser(cb: (String) -> Unit)

@JsFun("(col, docId, tryAllDbs, cb) => window.__klassFirebase.getDoc(col, docId, tryAllDbs, cb)")
internal external fun jsGetDoc(col: String, docId: String, tryAllDbs: Boolean, cb: (String) -> Unit)

@JsFun("(col, mergeAllDbs, cb) => window.__klassFirebase.getCollection(col, mergeAllDbs, cb)")
internal external fun jsGetCollection(col: String, mergeAllDbs: Boolean, cb: (String) -> Unit)

@JsFun("(col, field, value, mergeAllDbs, cb) => window.__klassFirebase.queryEquals(col, field, value, mergeAllDbs, cb)")
internal external fun jsQueryEquals(col: String, field: String, value: String, mergeAllDbs: Boolean, cb: (String) -> Unit)

@JsFun("(col, docId, dataJson, merge, writeAllDbs, cb) => window.__klassFirebase.setDoc(col, docId, dataJson, merge, writeAllDbs, cb)")
internal external fun jsSetDoc(
    col: String,
    docId: String,
    dataJson: String,
    merge: Boolean,
    writeAllDbs: Boolean,
    cb: (String) -> Unit,
)

@JsFun("(col, docId, writeAllDbs, cb) => window.__klassFirebase.deleteDoc(col, docId, writeAllDbs, cb)")
internal external fun jsDeleteDoc(col: String, docId: String, writeAllDbs: Boolean, cb: (String) -> Unit)

@JsFun("(col, writeAllDbs, cb) => window.__klassFirebase.deleteCollection(col, writeAllDbs, cb)")
internal external fun jsDeleteCollection(col: String, writeAllDbs: Boolean, cb: (String) -> Unit)

@JsFun("(col, docsArrayJson, merge, writeAllDbs, cb) => window.__klassFirebase.batchSetDocs(col, docsArrayJson, merge, writeAllDbs, cb)")
internal external fun jsBatchSetDocs(
    col: String,
    docsArrayJson: String,
    merge: Boolean,
    writeAllDbs: Boolean,
    cb: (String) -> Unit,
)

@JsFun("(cb) => { if (window.__klassFirebase && window.__klassFirebase.pickImageDataUri) window.__klassFirebase.pickImageDataUri(cb); else cb(''); }")
internal external fun jsPickImageDataUri(cb: (String) -> Unit)

@JsFun("(text, sl, tl, cb) => { if (window.__klassFirebase && window.__klassFirebase.translateText) window.__klassFirebase.translateText(text, sl, tl, cb); else cb(''); }")
internal external fun jsTranslateText(text: String, sl: String, tl: String, cb: (String) -> Unit)

@JsFun("(filename, title, htmlBody) => (window.__klassFirebase && window.__klassFirebase.downloadHtmlRoutine) ? window.__klassFirebase.downloadHtmlRoutine(filename, title, htmlBody) : false")
internal external fun jsDownloadHtmlRoutine(filename: String, title: String, htmlBody: String): Boolean

internal object WebBackendUnavailable :
    UnsupportedOperationException("Firebase Web backend is not configured.")

internal fun dayNameFromJs(): String = when (jsDayOfWeek()) {
    0 -> "Sunday"
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    4 -> "Thursday"
    5 -> "Friday"
    6 -> "Saturday"
    else -> "Sunday"
}

internal data class WebFirestoreDoc(
    val id: String,
    val data: JValue.JObj,
)

internal object WebFirebaseBridge {
    suspend fun ensureReady(): Boolean {
        if (jsHasFirebaseBridge()) return true
        for (i in 0 until 60) {
            delay(50)
            if (jsHasFirebaseBridge()) return true
        }
        return false
    }

    private suspend fun callJson(timeoutMs: Long = 10_000L, invoker: ((String) -> Unit) -> Unit): JValue.JObj? {
        if (!ensureReady()) return null
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                invoker { rawJson ->
                    if (cont.isActive) {
                        cont.resume(WebJson.parseObj(rawJson))
                    }
                }
            }
        }
    }

    suspend fun getAuthStatus(): JValue.JObj? =
        callJson(4_000L) { cb -> jsAuthStatus(cb) }

    suspend fun isSignedIn(): Boolean =
        getAuthStatus()?.optBool("signedIn", false) == true

    suspend fun currentUid(): String? =
        getAuthStatus()?.takeIf { it.optBool("signedIn", false) }?.optNullableStr("uid")

    suspend fun currentEmail(): String? =
        getAuthStatus()?.takeIf { it.optBool("signedIn", false) }?.optNullableStr("email")?.lowercase()

    suspend fun signInWithGoogle(): JValue.JObj? =
        callJson(120_000L) { cb -> jsSignInWithGoogle(cb) }

    suspend fun signOutGoogle() {
        callJson(5_000L) { cb -> jsSignOutGoogle(cb) }
    }

    suspend fun signInWithEmail(email: String, password: String): JValue.JObj? =
        callJson(10_000L) { cb -> jsSignInWithEmail(email, password, cb) }

    suspend fun createUserWithEmail(email: String, password: String): JValue.JObj? =
        callJson(10_000L) { cb -> jsCreateUserWithEmail(email, password, cb) }

    suspend fun setOrUpdateAuthPassword(
        email: String,
        newPassword: String,
        currentPassword: String?,
    ): JValue.JObj? = callJson(10_000L) { cb ->
        jsSetOrUpdateAuthPassword(
            email = email,
            newPassword = newPassword,
            currentPassword = currentPassword.orEmpty(),
            hasCurrent = !currentPassword.isNullOrEmpty(),
            cb = cb,
        )
    }

    suspend fun deleteCurrentAuthUser(): Boolean =
        callJson(8_000L) { cb -> jsDeleteCurrentAuthUser(cb) }?.optBool("ok", false) == true

    suspend fun hashPbkdf2(saltHex: String, password: String): String? =
        callJson(5_000L) { cb -> jsPbkdf2Hash(saltHex, password, cb) }
            ?.takeIf { it.optBool("ok", false) }
            ?.optNullableStr("hash")

    suspend fun getDoc(collection: String, docId: String, tryAllDbs: Boolean = true): JValue.JObj? {
        val res = callJson(8_000L) { cb -> jsGetDoc(collection, docId, tryAllDbs, cb) } ?: return null
        if (!res.optBool("ok", false) || !res.optBool("exists", false)) return null
        return res.optObj("data")
    }

    suspend fun getCollection(collection: String, mergeAllDbs: Boolean = true): Pair<Boolean, List<WebFirestoreDoc>> {
        val res = callJson(12_000L) { cb -> jsGetCollection(collection, mergeAllDbs, cb) }
            ?: return false to emptyList()
        if (!res.optBool("ok", false)) return false to emptyList()
        val docsArr = res.optArr("docs") ?: return true to emptyList()
        val out = docsArr.items.mapNotNull { item ->
            val obj = item.asObj() ?: return@mapNotNull null
            val id = obj.optStr("id").trim()
            val data = obj.optObj("data") ?: return@mapNotNull null
            if (id.isBlank()) null else WebFirestoreDoc(id, data)
        }
        return true to out
    }

    suspend fun queryEquals(
        collection: String,
        field: String,
        value: String,
        mergeAllDbs: Boolean = true,
    ): List<WebFirestoreDoc> {
        val res = callJson(8_000L) { cb -> jsQueryEquals(collection, field, value, mergeAllDbs, cb) }
            ?: return emptyList()
        if (!res.optBool("ok", false)) return emptyList()
        val docsArr = res.optArr("docs") ?: return emptyList()
        return docsArr.items.mapNotNull { item ->
            val obj = item.asObj() ?: return@mapNotNull null
            val id = obj.optStr("id").trim()
            val data = obj.optObj("data") ?: return@mapNotNull null
            if (id.isBlank()) null else WebFirestoreDoc(id, data)
        }
    }

    suspend fun setDoc(
        collection: String,
        docId: String,
        data: JValue.JObj,
        merge: Boolean = true,
        writeAllDbs: Boolean = false,
    ): Result<Unit> {
        val payload = WebJson.stringify(data)
        val res = callJson(10_000L) { cb ->
            jsSetDoc(collection, docId, payload, merge, writeAllDbs, cb)
        } ?: return Result.failure(Exception("Firebase request timed out"))
        return if (res.optBool("ok", false)) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(res.optStr("error", "Firestore write failed")))
        }
    }

    suspend fun deleteDoc(
        collection: String,
        docId: String,
        writeAllDbs: Boolean = true,
    ): Result<Unit> {
        val res = callJson(8_000L) { cb -> jsDeleteDoc(collection, docId, writeAllDbs, cb) }
            ?: return Result.failure(Exception("Firebase request timed out"))
        return if (res.optBool("ok", false)) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(res.optStr("error", "Firestore delete failed")))
        }
    }

    suspend fun deleteCollection(collection: String, writeAllDbs: Boolean = true): Result<Unit> {
        val res = callJson(12_000L) { cb -> jsDeleteCollection(collection, writeAllDbs, cb) }
            ?: return Result.failure(Exception("Firebase request timed out"))
        return if (res.optBool("ok", false)) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(res.optStr("error", "Failed deleting collection")))
        }
    }

    suspend fun batchSetDocs(
        collection: String,
        docs: List<Pair<String, JValue.JObj>>,
        merge: Boolean = false,
        writeAllDbs: Boolean = false,
    ): Result<Unit> {
        if (docs.isEmpty()) return Result.success(Unit)
        val arr = jObjArr(docs.map { (id, data) ->
            jObj {
                put("id", id)
                put("data", data)
            }
        })
        val payload = WebJson.stringify(arr)
        val res = callJson(16_000L) { cb ->
            jsBatchSetDocs(collection, payload, merge, writeAllDbs, cb)
        } ?: return Result.failure(Exception("Firebase batch write timed out"))
        return if (res.optBool("ok", false)) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(res.optStr("error", "Batch write failed")))
        }
    }

    suspend fun translate(text: String, sourceLang: String, targetLang: String): String? {
        return withTimeoutOrNull(8_000L) {
            suspendCancellableCoroutine { cont ->
                jsTranslateText(text, sourceLang, targetLang) { result ->
                    if (cont.isActive) cont.resume(result.ifBlank { null })
                }
            }
        }
    }
}
