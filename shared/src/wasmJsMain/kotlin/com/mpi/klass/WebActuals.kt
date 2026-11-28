@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.mpi.klass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@JsFun("(tel) => { try { window.open('tel:' + tel, '_self'); } catch (e) {} }")
private external fun jsOpenTel(tel: String)

private const val ONLINE_DICT_CACHE_KEY = "klass_online_dict_bn_to_en_json"
private const val NOTIF_PREFS_KEY = "klass_notif_prefs_json"
private const val NOTIF_SEEN_IDS_KEY = "klass_notif_seen_ids_json"

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

actual fun isGestureNavigation(): Boolean = false

actual fun getDisplayBottomCornerRadiusDp(): Float = 0f

actual fun saveAppLanguage(language: AppLanguage) {
    jsStorageSet("klass_app_language", AppLanguage.ENGLISH.name)
}

actual fun loadAppLanguage(): AppLanguage = AppLanguage.ENGLISH

@Composable
actual fun rememberLocationPermissionLauncher(onLocationFetched: (String) -> Unit): () -> Unit = { }

actual suspend fun isNetworkAvailable(): Boolean = jsIsOnline()

// ─── Online Translation Dictionary ───────────────────────────────────────────

actual fun loadCachedTranslations(): Map<String, String> {
    val raw = jsStorageGet(ONLINE_DICT_CACHE_KEY) ?: return emptyMap()
    val obj = WebJson.parseObj(raw) ?: return emptyMap()
    val result = LinkedHashMap<String, String>(obj.entries.size)
    for ((k, v) in obj.entries) {
        val cleanVal = v.asStr().trim()
        if (k.isNotBlank() && cleanVal.isNotBlank()) {
            result[k] = cleanVal
        }
    }
    return result
}

actual fun saveCachedTranslations(entries: Map<String, String>) {
    if (entries.isEmpty()) return
    val existing = loadCachedTranslations().toMutableMap()
    for ((k, v) in entries) {
        if (k.isNotBlank() && v.isNotBlank()) {
            existing[k] = v
        }
    }
    val json = jObj {
        existing.forEach { (k, v) -> put(k, v) }
    }
    jsStorageSet(ONLINE_DICT_CACHE_KEY, WebJson.stringify(json))
}

actual suspend fun fetchOnlineDictionaryFromCloud(): Map<String, String> {
    val doc = WebFirebaseBridge.getDoc("config", "language_dictionary", tryAllDbs = true)
        ?: return emptyMap()
    val result = LinkedHashMap<String, String>()
    doc.optObj("en")?.entries?.forEach { (k, v) ->
        val cleanK = k.trim()
        val cleanV = v.asStr().trim()
        if (cleanK.isNotBlank() && cleanV.isNotBlank()) {
            result[cleanK] = cleanV
        }
    }
    for ((k, v) in doc.entries) {
        if (k != "en" && v is JValue.JStr && k.isNotBlank() && v.value.isNotBlank()) {
            result[k.trim()] = v.value.trim()
        }
    }
    return result
}

actual suspend fun fetchOnlineTranslations(
    texts: List<String>,
    sourceLanguage: AppLanguage,
): Map<String, String> {
    val distinct = texts.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    if (distinct.isEmpty() || !jsIsOnline()) return emptyMap()

    val sl = if (sourceLanguage == AppLanguage.BENGALI) "bn" else "en"
    val tl = if (sourceLanguage == AppLanguage.BENGALI) "en" else "bn"
    val result = LinkedHashMap<String, String>(distinct.size)
    for (item in distinct.take(24)) {
        val translated = WebFirebaseBridge.translate(item, sl, tl)?.trim()
        if (!translated.isNullOrBlank()) {
            result[item] = translated
        }
    }
    return result
}

// ─── Device / Browser Helpers ────────────────────────────────────────────────

actual fun openPhoneDialer(phone: String) {
    val cleaned = phone.trim()
    if (cleaned.isNotBlank()) jsOpenTel(cleaned)
}

actual fun getTodayWorkingDayName(): String = dayNameFromJs()

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPhotoPicked)
    return {
        jsPickImageDataUri { dataUri ->
            if (dataUri.isNotBlank()) {
                currentOnPicked(dataUri)
            }
        }
    }
}

private val decodedWebAvatarCache = LinkedHashMap<String, ByteArray>()

@OptIn(ExperimentalEncodingApi::class)
actual fun resolveProfileImageModel(photoUrl: String?): Any? {
    if (photoUrl.isNullOrBlank()) return null
    if (photoUrl.startsWith("data:image", ignoreCase = true) && photoUrl.contains("base64,", ignoreCase = true)) {
        decodedWebAvatarCache[photoUrl]?.let { return it }
        return try {
            val base64Part = photoUrl.substringAfter("base64,").filterNot { it.isWhitespace() }
            val decoded = Base64.decode(base64Part)
            if (decodedWebAvatarCache.size > 24) {
                decodedWebAvatarCache.remove(decodedWebAvatarCache.keys.first())
            }
            decodedWebAvatarCache[photoUrl] = decoded
            decoded
        } catch (_: Throwable) {
            null
        }
    }
    return photoUrl
}

@Composable
actual fun rememberDatePickerLauncher(initialDate: String, onDatePicked: (String) -> Unit): () -> Unit = { }

// ─── Notifications & Local Preferences ───────────────────────────────────────

actual fun hasNotificationPermission(): Boolean = false

@Composable
actual fun rememberNotificationPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit = { }

actual fun getNotificationPrefs(): NotificationPrefs {
    val obj = WebJson.parseObj(jsStorageGet(NOTIF_PREFS_KEY)) ?: return NotificationPrefs()
    return NotificationPrefs(
        enabled = obj.optBool("enabled", true),
        reminderMinutesBefore = obj.optInt("reminderMinutesBefore", 10),
        notifyAtClassStart = obj.optBool("notifyAtClassStart", true),
    )
}

actual fun saveNotificationPrefs(prefs: NotificationPrefs) {
    jsStorageSet(
        NOTIF_PREFS_KEY,
        WebJson.stringify(
            jObj {
                put("enabled", prefs.enabled)
                put("reminderMinutesBefore", prefs.reminderMinutesBefore)
                put("notifyAtClassStart", prefs.notifyAtClassStart)
            }
        )
    )
}

actual fun getSeenNotificationIds(): Set<String> {
    val arr = WebJson.parseArr(jsStorageGet(NOTIF_SEEN_IDS_KEY)) ?: return emptySet()
    return arr.items.mapNotNull { it.asStr().trim().takeIf { id -> id.isNotBlank() } }.toSet()
}

actual fun saveSeenNotificationIds(ids: Set<String>) {
    jsStorageSet(NOTIF_SEEN_IDS_KEY, WebJson.stringify(jStrArr(ids)))
}

actual fun sendWelcomePushNotification(userName: String, roleLabel: String) {
}

actual fun sendInstantTestPushNotification(title: String, body: String) {
    jsShowToast("$title: $body")
}

actual fun syncAutomaticClassPushNotifications(
    entries: List<RoutineEntry>,
    config: RoutineConfig,
    role: UserRole?,
    prefs: NotificationPrefs,
): List<ScheduledClassAlert> = emptyList()

actual fun cancelAllClassPushNotifications() {
}

// ─── Routine Export on Web ───────────────────────────────────────────────────

actual suspend fun downloadRoutinePdf(
    label: String,
    shift: String,
    config: RoutineConfig,
    entries: List<RoutineEntry>,
): Result<String> {
    if (entries.isEmpty()) return Result.failure(Exception("No classes to export"))
    val safeName = label.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "routine" }
    val title = "MPI Klass Routine - $label ($shift)"
    val rowsHtml = buildString {
        append("<h2>").append(title).append("</h2>")
        append("<table><thead><tr><th>Day</th><th>Period</th><th>Time</th><th>Subject</th><th>Teacher</th><th>Room</th></tr></thead><tbody>")
        entries.sortedWith(compareBy<RoutineEntry> { RoutineRules.dayOrderIndex(it.day) }.thenBy { it.periodIndex })
            .forEach { e ->
                append("<tr>")
                append("<td>").append(e.day).append("</td>")
                append("<td>P").append(e.periodIndex + 1).append("</td>")
                append("<td>").append(e.startTime).append(" - ").append(e.endTime).append("</td>")
                append("<td>").append(e.subjectTitle).append(" (").append(e.subjectCode).append(")</td>")
                append("<td>").append(e.teacherName.ifBlank { e.teacherEmail }).append("</td>")
                append("<td>").append(e.roomNumber).append("</td>")
                append("</tr>")
            }
        append("</tbody></table>")
    }
    val ok = jsDownloadHtmlRoutine("MPI_Klass_${safeName}.html", title, rowsHtml)
    return if (ok) {
        Result.success("Downloaded MPI_Klass_${safeName}.html")
    } else {
        Result.failure(Exception("Could not trigger browser download"))
    }
}

// ─── Session, Toast, Time & Zed AI ───────────────────────────────────────────

actual fun showToast(message: String) {
    jsShowToast(message)
}

actual fun getCurrentTimeMillis(): Long = jsNowMillis().toLong()

actual fun saveUserSession(session: UserSession) {
    jsStorageSet("klass_session_role", session.role)
    jsStorageSet("klass_session_email", session.email)
    jsStorageSet("klass_session_name", session.name)
    val photo = session.photoUrl
    if (photo != null) jsStorageSet("klass_session_photo", photo) else jsStorageRemove("klass_session_photo")
}

actual fun loadUserSession(): UserSession? {
    val role = jsStorageGet("klass_session_role") ?: return null
    val email = jsStorageGet("klass_session_email") ?: return null
    return UserSession(
        role = role,
        email = email,
        name = jsStorageGet("klass_session_name") ?: "",
        photoUrl = jsStorageGet("klass_session_photo"),
    )
}

actual fun clearUserSession() {
    jsStorageRemove("klass_session_role")
    jsStorageRemove("klass_session_email")
    jsStorageRemove("klass_session_name")
    jsStorageRemove("klass_session_photo")
}

actual suspend fun askZedWithGemini(
    prompt: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
    history: List<AiChatMessage>,
    onPartialText: (String) -> Unit,
): ZedReply = ZedAiEngine.evaluate(
    query = prompt,
    snapshot = snapshot,
    todayEnglishDay = todayEnglishDay,
)

actual fun currentDayNameEnglish(): String = dayNameFromJs()

actual fun currentYearMonthDay(): Triple<Int, Int, Int> = Triple(jsYear(), jsMonth(), jsDayOfMonth())
