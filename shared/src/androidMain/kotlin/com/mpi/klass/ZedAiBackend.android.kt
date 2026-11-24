package com.mpi.klass

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.mpi.klass.theme.getAppContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

private const val ZED_AI_TAG = "ZedAiBackend"
private const val GEMINI_PRIMARY_MODEL = "gemini-3.8-flash"
private const val GEMINI_LITE_MODEL = "gemini-3.5-flash-lite"
private const val PRIMARY_STEP_TIMEOUT_MS = 30_000L
private const val FALLBACK_STEP_TIMEOUT_MS = 20_000L

private val ZED_STRUCTURED_SYSTEM_PROMPT = """
    You are Zed, the intelligent AI assistant inside the MPI Klass Android app (Mymensingh Polytechnic Institute).

    Capabilities:
    1) Complete App & Campus Intelligence:
       You receive CAMPUS_DATA JSON with the user's profile, MPI rules, BTEB Books Library, Teachers Directory, Classrooms, Published Class Routine, and Blood Donors.
       - Ground EVERY specific person name, phone number, email, room number, subject code, and routine slot strictly in CAMPUS_DATA. Never invent missing contacts, rooms, or timetable slots.
    2) General Intelligence:
       Answer any general question clearly and helpfully using your full knowledge.

    Response & A2UI Formatting Rules:
    - Reply in Bengali when the user writes in Bengali or Banglish; otherwise reply in crisp English.
    - Output valid JSON matching the schema:
      * answerText: Clear, direct answer.
      * sourceBadge: Short topic label e.g. "Zed AI", "Books Library", "Teachers", "Class Routine", "Blood Donor", "Classrooms".
      * isSupportedDomain: true.
      * blocks: Optional ordered Material 3 A2UI cards:
        - HEADER_BANNER (title, subtitle, domain)
        - STAT_ROW (2-3 metrics in items)
        - KEY_VALUE_CARD (title + key-value pairs in items)
        - ENTITY_LIST (books, donors, teachers, classrooms in items — include `phone` verbatim when present in CAMPUS_DATA)
        - SCHEDULE_LIST (timetable slots in items — `badge` = day/period e.g. "Sun P1", `primary` = subject, `secondary` = time/teacher/room)
        - CALLOUT (important note in `message`)
    - Never include preset follow-up question lists.
""".trimIndent()

private val ZED_PLAIN_SYSTEM_PROMPT = """
    You are Zed, the friendly, intelligent AI assistant inside the MPI Klass Android app (Mymensingh Polytechnic Institute).

    You can help with BOTH:
    1) MPI Klass & Campus Queries using the CAMPUS_DATA JSON provided with the user's message (user profile, books/subjects, teachers & phone numbers, classrooms, published class routine, and blood donors). Always state names, phone numbers, rooms, subject codes, and schedule slots strictly from CAMPUS_DATA when answering campus questions—never invent missing contacts or routine slots.
    2) Any General Question (science, math, programming, polytechnic studies, translations, writing, history, everyday chat). Answer thoroughly, clearly, and directly.

    Formatting Rules:
    - Reply in Bengali when the user writes in Bengali or Banglish; otherwise reply in crisp English.
    - Respond directly in clean, readable natural prose (or concise bullet points). Do NOT wrap your reply in raw JSON or markdown code fences unless the user explicitly asked for code.
    - Never include preset follow-up question lists.
""".trimIndent()

private val zedItemSchema = Schema.obj(
    properties = mapOf(
        "primary" to Schema.string(description = "Primary title, label, or subject"),
        "secondary" to Schema.string(description = "Secondary value, details, or time/room"),
        "badge" to Schema.string(description = "Optional short badge like subject code, day/period, or blood group"),
        "phone" to Schema.string(description = "Optional phone number verbatim from CAMPUS_DATA"),
        "isPractical" to Schema.boolean(description = "True if the schedule slot is a practical lab"),
    ),
    optionalProperties = listOf("badge", "phone", "isPractical"),
)

private val zedBlockSchema = Schema.obj(
    properties = mapOf(
        "blockType" to Schema.enumeration(
            values = listOf(
                "HEADER_BANNER",
                "STAT_ROW",
                "KEY_VALUE_CARD",
                "ENTITY_LIST",
                "SCHEDULE_LIST",
                "CALLOUT",
            ),
            description = "A2UI card block type",
        ),
        "title" to Schema.string(description = "Card or banner title"),
        "subtitle" to Schema.string(description = "Banner subtitle or list context"),
        "domain" to Schema.enumeration(
            values = listOf("BOOKS", "BLOOD", "TEACHERS", "ROUTINE", "GENERAL"),
            description = "Campus or general domain",
        ),
        "message" to Schema.string(description = "Callout card message"),
        "isWarning" to Schema.boolean(description = "Whether the callout is a warning"),
        "items" to Schema.array(
            items = zedItemSchema,
            description = "Ordered list of items for STAT_ROW, KEY_VALUE_CARD, ENTITY_LIST, or SCHEDULE_LIST",
        ),
    ),
    optionalProperties = listOf("title", "subtitle", "message", "isWarning", "items"),
)

internal val zedResponseSchema: Schema = Schema.obj(
    properties = mapOf(
        "answerText" to Schema.string(description = "Primary helpful answer text"),
        "sourceBadge" to Schema.string(description = "Short topic badge label"),
        "isSupportedDomain" to Schema.boolean(description = "True for supported campus or general queries"),
        "blocks" to Schema.array(
            items = zedBlockSchema,
            description = "A2UI display blocks",
        ),
    ),
    optionalProperties = listOf("sourceBadge", "isSupportedDomain", "blocks"),
)

private object ZedFirebaseAiProvider {
    val structuredModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_PRIMARY_MODEL,
            generationConfig = generationConfig {
                temperature = 0.45f
                responseMimeType = "application/json"
                responseSchema = zedResponseSchema
            },
            systemInstruction = content { text(ZED_STRUCTURED_SYSTEM_PROMPT) },
        )
    }

    val plainModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_PRIMARY_MODEL,
            generationConfig = generationConfig {
                temperature = 0.55f
            },
            systemInstruction = content { text(ZED_PLAIN_SYSTEM_PROMPT) },
        )
    }

    val litePlainModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_LITE_MODEL,
            generationConfig = generationConfig {
                temperature = 0.55f
            },
            systemInstruction = content { text(ZED_PLAIN_SYSTEM_PROMPT) },
        )
    }
}

actual suspend fun askZedWithGemini(
    prompt: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
    history: List<AiChatMessage>,
    onPartialText: (String) -> Unit,
): ZedReply {
    val trimmed = prompt.trim()
    val isBn = ZedAiEngine.prefersBengali(trimmed)

    if (trimmed.isBlank()) {
        return ZedReply(
            answerText = if (isBn) "দয়া করে একটি প্রশ্ন লিখুন।" else "Please enter a question.",
            sourceBadge = "Zed AI",
            isSupportedDomain = true,
        )
    }

    if (!isNetworkAvailable()) {
        val offlineMsg = if (isBn) {
            "ইন্টারনেট সংযোগ পাওয়া যায়নি। দয়া করে Wi-Fi বা মোবাইল ডাটা চালু করে আবার চেষ্টা করুন।"
        } else {
            "No active internet connection detected. Please check your Wi-Fi or mobile data and try again."
        }
        return ZedReply(
            answerText = offlineMsg,
            sourceBadge = "Offline",
            isSupportedDomain = true,
            blocks = listOf(ZedA2UiBlock.CalloutCard(message = offlineMsg, isWarning = true)),
        )
    }

    return try {
        getAppContext()?.let { ctx ->
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
        }

        val campusQuery = isCampusOrAppQuery(trimmed)
        val (currentTurnText, chatHistory) = withContext(Dispatchers.Default) {
            val enrichedSnapshot = if (snapshot.books.size < BtebBooksCatalog.books.size) {
                val merged = (snapshot.books + BtebBooksCatalog.books)
                    .distinctBy { it.id.ifBlank { it.subjectCode } }
                snapshot.copy(books = merged)
            } else {
                snapshot
            }
            val contextJson = buildFullAppContextJson(trimmed, enrichedSnapshot, todayEnglishDay)
            val builtHistory = buildSanitizedChatHistory(history)
            "USER_QUERY: $trimmed\n\nCAMPUS_DATA:\n$contextJson" to builtHistory
        }

        Log.d(
            ZED_AI_TAG,
            "Sending query to Firebase AI (campus=$campusQuery, payload=${currentTurnText.length} chars, history=${chatHistory.size} turns)",
        )

        val responseText = if (campusQuery) {
            executeCampusInferenceWithFallbacks(
                message = currentTurnText,
                history = chatHistory,
                onPartialText = onPartialText,
            )
        } else {
            executeGeneralInferenceWithFallbacks(
                message = currentTurnText,
                history = chatHistory,
                onPartialText = onPartialText,
            )
        }

        if (responseText.isBlank()) {
            throw IllegalStateException("Gemini returned an empty response.")
        }

        val jsonCandidate = if (campusQuery) extractJsonObjectIfWrapped(responseText) else null
        if (jsonCandidate != null) {
            runCatching {
                parseZedA2UiResponse(jsonCandidate)
            }.getOrElse {
                ZedReply(
                    answerText = responseText,
                    sourceBadge = "Zed AI",
                    isSupportedDomain = true,
                    blocks = emptyList(),
                )
            }
        } else {
            ZedReply(
                answerText = responseText,
                sourceBadge = "Zed AI",
                isSupportedDomain = true,
                blocks = emptyList(),
            )
        }
    } catch (e: TimeoutCancellationException) {
        currentCoroutineContext().ensureActive()
        Log.e(ZED_AI_TAG, "Firebase AI request timed out", e)
        val msg = if (isBn) {
            "Zed AI রিকোয়েস্ট টাইমআউট হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।"
        } else {
            "Zed AI request timed out. Please check your internet connection and try again."
        }
        ZedReply(
            answerText = msg,
            sourceBadge = "Timeout",
            isSupportedDomain = true,
            blocks = listOf(ZedA2UiBlock.CalloutCard(message = msg, isWarning = true)),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        val appCheckDiag = if (isAppCheckOrAuthFailure(t)) {
            withTimeoutOrNull(4_000L) { ZedAppCheckDiagnostics.checkError() }
        } else {
            null
        }
        val detail = formatThrowableDetail(t, appCheckDiag)
        Log.e(ZED_AI_TAG, "Firebase AI call failed: $detail", t)
        val msg = if (isBn) {
            "Zed AI সংযোগ ব্যর্থ হয়েছে: $detail"
        } else {
            "Zed AI request failed: $detail"
        }
        ZedReply(
            answerText = msg,
            sourceBadge = "AI Error",
            isSupportedDomain = true,
            blocks = listOf(ZedA2UiBlock.CalloutCard(message = msg, isWarning = true)),
        )
    }
}

private suspend fun executeCampusInferenceWithFallbacks(
    message: String,
    history: List<Content>,
    onPartialText: (String) -> Unit,
): String {
    val firstFailure: Throwable? = try {
        val unary = withTimeout(PRIMARY_STEP_TIMEOUT_MS) {
            generateUnary(
                model = ZedFirebaseAiProvider.structuredModel,
                history = history,
                message = message,
            )
        }
        if (unary.isNotBlank()) return unary
        null
    } catch (t: Throwable) {
        if (t is TimeoutCancellationException) {
            currentCoroutineContext().ensureActive()
        } else if (t is CancellationException) {
            throw t
        }
        if (t is PromptBlockedException || isAppCheckOrAuthFailure(t)) throw t
        Log.w(ZED_AI_TAG, "Structured campus call failed (${t::class.simpleName}: ${t.localizedMessage}); falling back to plain stream", t)
        t
    }

    return executePlainAndLiteChain(
        message = message,
        history = history,
        onPartialText = onPartialText,
        priorFailure = firstFailure,
    )
}

private suspend fun executeGeneralInferenceWithFallbacks(
    message: String,
    history: List<Content>,
    onPartialText: (String) -> Unit,
): String = executePlainAndLiteChain(
    message = message,
    history = history,
    onPartialText = onPartialText,
    priorFailure = null,
)

private suspend fun executePlainAndLiteChain(
    message: String,
    history: List<Content>,
    onPartialText: (String) -> Unit,
    priorFailure: Throwable?,
): String {
    var lastError: Throwable? = priorFailure

    // 1. Live streaming on primary model (fastest time-to-first-token)
    try {
        val streamed = withTimeout(PRIMARY_STEP_TIMEOUT_MS) {
            streamChat(
                model = ZedFirebaseAiProvider.plainModel,
                history = history,
                message = message,
                onPartialText = onPartialText,
            )
        }
        if (streamed.isNotBlank()) return streamed
    } catch (t: Throwable) {
        if (t is TimeoutCancellationException) {
            currentCoroutineContext().ensureActive()
        } else if (t is CancellationException) {
            throw t
        }
        if (t is PromptBlockedException || isAppCheckOrAuthFailure(t)) throw t
        Log.w(ZED_AI_TAG, "Primary stream failed (${t::class.simpleName}: ${t.localizedMessage}); trying unary primary", t)
        lastError = t
    }

    // 2. Unary fallback on primary model (bypasses SSE channel/buffering issues)
    try {
        val unary = withTimeout(FALLBACK_STEP_TIMEOUT_MS) {
            generateUnary(
                model = ZedFirebaseAiProvider.plainModel,
                history = history,
                message = message,
            )
        }
        if (unary.isNotBlank()) return unary
    } catch (t: Throwable) {
        if (t is TimeoutCancellationException) {
            currentCoroutineContext().ensureActive()
        } else if (t is CancellationException) {
            throw t
        }
        if (t is PromptBlockedException || isAppCheckOrAuthFailure(t)) throw t
        Log.w(ZED_AI_TAG, "Primary unary failed (${t::class.simpleName}: ${t.localizedMessage}); trying $GEMINI_LITE_MODEL", t)
        lastError = t
    }

    // 3. Final fallback on lite model (stream -> unary)
    return try {
        withTimeout(FALLBACK_STEP_TIMEOUT_MS) {
            val liteStreamed = runCatching {
                streamChat(
                    model = ZedFirebaseAiProvider.litePlainModel,
                    history = history,
                    message = message,
                    onPartialText = onPartialText,
                )
            }.getOrElse { ex ->
                if (ex is CancellationException) throw ex
                if (ex is PromptBlockedException || isAppCheckOrAuthFailure(ex)) throw ex
                ""
            }
            if (liteStreamed.isNotBlank()) {
                liteStreamed
            } else {
                generateUnary(
                    model = ZedFirebaseAiProvider.litePlainModel,
                    history = history,
                    message = message,
                )
            }
        }
    } catch (t: Throwable) {
        if (t is TimeoutCancellationException) {
            currentCoroutineContext().ensureActive()
        } else if (t is CancellationException) {
            throw t
        }
        throw (lastError ?: t)
    }
}

private suspend fun streamChat(
    model: GenerativeModel,
    history: List<Content>,
    message: String,
    onPartialText: (String) -> Unit,
): String {
    val chat = model.startChat(history = history)
    val sb = StringBuilder()
    chat.sendMessageStream(message).collect { chunk ->
        val piece = chunk.text
        if (!piece.isNullOrEmpty()) {
            sb.append(piece)
            onPartialText(sb.toString())
        }
    }
    return sb.toString().trim()
}

private suspend fun generateUnary(
    model: GenerativeModel,
    history: List<Content>,
    message: String,
): String {
    val chat = model.startChat(history = history)
    return chat.sendMessage(message).text?.trim().orEmpty()
}

private fun isAppCheckOrAuthFailure(t: Throwable): Boolean {
    val combined = buildString {
        append(t::class.simpleName.orEmpty())
        append(' ')
        append(t.localizedMessage.orEmpty())
        append(' ')
        append(t.cause?.localizedMessage.orEmpty())
    }.lowercase()
    return combined.contains("appcheck") ||
        combined.contains("app check") ||
        combined.contains("attestation") ||
        combined.contains("403") ||
        combined.contains("permission_denied") ||
        combined.contains("unauthorized")
}

private fun formatThrowableDetail(t: Throwable, appCheckDiag: String?): String {
    val primary = t.localizedMessage?.trim()?.takeIf { it.isNotBlank() }
        ?: t::class.simpleName
        ?: "Unknown error"
    val causeMsg = t.cause?.localizedMessage?.trim()?.takeIf {
        it.isNotBlank() && !primary.contains(it, ignoreCase = true)
    }
    val base = if (causeMsg != null) "$primary ($causeMsg)" else primary
    return if (!appCheckDiag.isNullOrBlank() && !base.contains(appCheckDiag, ignoreCase = true)) {
        "$base [App Check: $appCheckDiag]"
    } else {
        base
    }
}

internal fun extractJsonObjectIfWrapped(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        return trimmed
    }
    val start = trimmed.indexOf('{')
    val end = trimmed.lastIndexOf('}')
    if (start >= 0 && end > start) {
        return trimmed.substring(start, end + 1)
    }
    return null
}

internal fun buildSanitizedChatHistory(history: List<AiChatMessage>): List<Content> {
    val turns = mutableListOf<Pair<String, String>>()
    for (msg in history.takeLast(8)) {
        val txt = msg.text.trim()
        if (txt.isBlank()) continue
        val role = if (msg.isFromUser) "user" else "model"
        if (turns.isEmpty() && role == "model") {
            continue
        }
        if (turns.isNotEmpty() && turns.last().first == role) {
            val prev = turns.removeAt(turns.lastIndex)
            turns.add(role to "${prev.second}\n\n$txt")
        } else {
            turns.add(role to txt)
        }
    }
    if (turns.isNotEmpty() && turns.last().first == "user") {
        turns.removeAt(turns.lastIndex)
    }
    return turns.map { (role, text) ->
        content(role = role) { text(text) }
    }
}

private fun JSONObject.safeString(key: String, default: String = ""): String {
    if (!has(key) || isNull(key)) return default
    val v = optString(key, default).trim()
    return if (v.equals("null", ignoreCase = true)) default else v
}

internal fun parseZedA2UiResponse(jsonString: String): ZedReply {
    val root = JSONObject(jsonString)
    val answerText = root.safeString("answerText")
    val sourceBadge = root.safeString("sourceBadge", "Zed AI").ifBlank { "Zed AI" }
    val isSupported = root.optBoolean("isSupportedDomain", true)
    val blocksArr = root.optJSONArray("blocks") ?: JSONArray()

    val blocks = mutableListOf<ZedA2UiBlock>()
    for (i in 0 until blocksArr.length()) {
        val obj = blocksArr.optJSONObject(i) ?: continue
        val type = obj.safeString("blockType").uppercase()
        val title = obj.safeString("title")
        val subtitle = obj.safeString("subtitle")
        val domain = obj.safeString("domain", "GENERAL").uppercase().ifBlank { "GENERAL" }
        val message = obj.safeString("message")
        val isWarning = obj.optBoolean("isWarning", false)
        val itemsArr = obj.optJSONArray("items") ?: JSONArray()

        when (type) {
            "HEADER_BANNER" -> if (title.isNotBlank()) {
                blocks.add(ZedA2UiBlock.HeaderBanner(title = title, subtitle = subtitle, domain = domain))
            }
            "STAT_ROW" -> {
                val stats = mutableListOf<ZedStatMetric>()
                for (j in 0 until itemsArr.length()) {
                    val it = itemsArr.optJSONObject(j) ?: continue
                    val lbl = it.safeString("primary")
                    val valStr = it.safeString("secondary")
                    if (lbl.isNotBlank() && valStr.isNotBlank()) {
                        stats.add(ZedStatMetric(label = lbl, value = valStr))
                    }
                }
                if (stats.isNotEmpty()) blocks.add(ZedA2UiBlock.StatRow(stats))
            }
            "KEY_VALUE_CARD" -> {
                val kvList = mutableListOf<ZedKeyValue>()
                for (j in 0 until itemsArr.length()) {
                    val it = itemsArr.optJSONObject(j) ?: continue
                    val k = it.safeString("primary")
                    val v = it.safeString("secondary")
                    if (k.isNotBlank() && v.isNotBlank()) {
                        kvList.add(ZedKeyValue(label = k, value = v))
                    }
                }
                if (kvList.isNotEmpty()) {
                    blocks.add(ZedA2UiBlock.KeyValueCard(title = title.ifBlank { "Details" }, items = kvList))
                }
            }
            "ENTITY_LIST" -> {
                val entities = mutableListOf<ZedEntityItem>()
                for (j in 0 until itemsArr.length()) {
                    val it = itemsArr.optJSONObject(j) ?: continue
                    val prim = it.safeString("primary")
                    val sec = it.safeString("secondary")
                    val badge = it.safeString("badge")
                    val phone = it.safeString("phone").takeIf { p -> p.isNotBlank() }
                    if (prim.isNotBlank()) {
                        entities.add(ZedEntityItem(primary = prim, secondary = sec, badge = badge, phone = phone))
                    }
                }
                if (entities.isNotEmpty()) {
                    blocks.add(ZedA2UiBlock.EntityListCard(title = title.ifBlank { "Records" }, entities = entities))
                }
            }
            "SCHEDULE_LIST" -> {
                val slots = mutableListOf<ZedScheduleSlot>()
                for (j in 0 until itemsArr.length()) {
                    val it = itemsArr.optJSONObject(j) ?: continue
                    val subj = it.safeString("primary")
                    val detail = it.safeString("secondary")
                    val dayPeriod = it.safeString("badge").ifBlank { "Slot" }
                    val isPrac = it.optBoolean("isPractical", false)
                    if (subj.isNotBlank()) {
                        slots.add(
                            ZedScheduleSlot(
                                dayAndPeriod = dayPeriod,
                                timeRange = detail,
                                subject = subj,
                                detail = subtitle.ifBlank { domain },
                                isPractical = isPrac,
                            )
                        )
                    }
                }
                if (slots.isNotEmpty()) {
                    blocks.add(ZedA2UiBlock.ScheduleListCard(title = title.ifBlank { "Schedule" }, slots = slots))
                }
            }
            "CALLOUT" -> if (message.isNotBlank()) {
                blocks.add(ZedA2UiBlock.CalloutCard(message = message, isWarning = isWarning))
            }
        }
    }

    return ZedReply(
        answerText = answerText.ifBlank { "Here is your answer from Zed AI." },
        sourceBadge = sourceBadge,
        isSupportedDomain = isSupported,
        blocks = blocks,
    )
}

private val QUERY_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}+-]+")
private val SUBJECT_CODE_REGEX = Regex("\\b\\d{5}\\b")

private val CAMPUS_EXACT_TOKENS = setOf(
    "book", "books", "syllabus", "subject", "subjects", "code", "codes", "credit", "credits", "bteb", "probidhan",
    "teacher", "teachers", "sir", "madam", "faculty", "phone", "number", "numbers", "contact", "contacts",
    "routine", "schedule", "schedules", "class", "classes", "period", "periods", "today", "tomorrow", "timetable",
    "room", "rooms", "classroom", "classrooms", "lab", "labs", "building", "floor",
    "blood", "donor", "donors", "a+", "a-", "b+", "b-", "o+", "o-", "ab+", "ab-",
    "mpi", "klass", "polytechnic", "mymensingh",
    "semester", "semesters", "sem", "shift", "shifts", "section", "department", "departments", "dept", "depts",
    "cst", "mt", "et", "ent", "ct", "rac", "ec",
    "profile", "roll", "account", "admin", "guest", "app",
)

private val CAMPUS_BN_STEMS = listOf(
    "বই", "সিলেবাস", "বিষয়", "কোড", "ক্রেডিট",
    "শিক্ষক", "স্যার", "ম্যাম", "ফোন", "নম্বর", "যোগাযোগ",
    "রুটিন", "ক্লাস", "পিরিয়ড", "সময়", "আজ", "আগামীকাল",
    "রুম", "ল্যাব", "কক্ষ", "ভবন", "ফ্লোর",
    "রক্ত", "ডোনার", "দাতা",
    "সেমিস্টার", "শিফট", "ডিপার্টমেন্ট", "প্রোফাইল", "রোল", "ক্যাম্পাস", "অ্যাপ",
)

private fun extractQueryTokens(query: String): List<String> =
    query.lowercase().split(QUERY_SPLIT_REGEX).filter { it.isNotBlank() }

private fun List<String>.matchesCampusKeywords(
    exactAsciiTokens: Set<String>,
    bnStems: List<String>,
): Boolean = any { tok ->
    tok in exactAsciiTokens || bnStems.any { stem -> tok.startsWith(stem) }
}

internal fun isCampusOrAppQuery(query: String): Boolean {
    val q = query.lowercase()
    val tokens = extractQueryTokens(q)
    return tokens.matchesCampusKeywords(CAMPUS_EXACT_TOKENS, CAMPUS_BN_STEMS) ||
        SUBJECT_CODE_REGEX.containsMatchIn(q)
}

internal fun buildFullAppContextJson(
    query: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
): String {
    val q = query.lowercase()
    val wordTokens = extractQueryTokens(q)
    val tokens = wordTokens.filter { it.length >= 2 }
    val userDept = snapshot.userProfile?.department.orEmpty()
    val campusQuery = isCampusOrAppQuery(query)

    val wantBooks = wordTokens.matchesCampusKeywords(
        setOf("book", "books", "syllabus", "subject", "subjects", "code", "codes", "credit", "credits", "bteb", "probidhan", "semester", "semesters", "sem"),
        listOf("বই", "সিলেবাস", "বিষয়", "কোড", "ক্রেডিট", "সেমিস্টার"),
    ) || SUBJECT_CODE_REGEX.containsMatchIn(q)
    val wantTeachers = wordTokens.matchesCampusKeywords(
        setOf("teacher", "teachers", "sir", "madam", "faculty", "phone", "number", "numbers", "contact", "contacts"),
        listOf("শিক্ষক", "স্যার", "ম্যাম", "ফোন", "নম্বর", "যোগাযোগ"),
    )
    val wantRooms = wordTokens.matchesCampusKeywords(
        setOf("room", "rooms", "classroom", "classrooms", "lab", "labs", "building", "floor"),
        listOf("রুম", "ল্যাব", "কক্ষ", "ভবন", "ফ্লোর"),
    )
    val wantDonors = wordTokens.matchesCampusKeywords(
        setOf("blood", "donor", "donors", "a+", "a-", "b+", "b-", "o+", "o-", "ab+", "ab-"),
        listOf("রক্ত", "ডোনার", "দাতা"),
    )
    val wantRoutine = wordTokens.matchesCampusKeywords(
        setOf("routine", "schedule", "schedules", "class", "classes", "period", "periods", "today", "tomorrow", "timetable"),
        listOf("রুটিন", "ক্লাস", "পিরিয়ড", "আজ", "আগামীকাল"),
    )
    val specificDomainMatched = wantBooks || wantTeachers || wantRooms || wantDonors || wantRoutine

    val json = JSONObject().apply {
        put("today", todayEnglishDay)
        put("instituteOverview", JSONObject().apply {
            put("name", "Mymensingh Polytechnic Institute (MPI)")
            put("appName", "MPI Klass")
            put("curriculum", "BTEB Probidhan-2022")
            put("departments", JSONArray(RoutineRules.ALL_TEACHER_DEPARTMENTS))
            put("workingDays", JSONArray(RoutineRules.WORKING_DAYS))
            put("shifts", JSONObject().apply {
                put("1st", "08:00 AM - 01:15 PM (7 periods)")
                put("2nd", "01:30 PM - 06:45 PM (7 periods)")
            })
        })
        put("currentUser", JSONObject().apply {
            put("name", snapshot.userDisplayName)
            put("role", snapshot.userRole?.name ?: "GUEST")
            val userEmail = snapshot.userFullProfile?.email ?: snapshot.userProfile?.email ?: ""
            if (userEmail.isNotBlank()) put("email", userEmail)
            if (userDept.isNotBlank()) put("department", userDept)
            snapshot.userProfile?.semester?.takeIf { it.isNotBlank() }?.let { put("semester", it) }
            snapshot.userProfile?.shift?.takeIf { it.isNotBlank() }?.let { put("shift", it) }
            snapshot.userProfile?.section?.takeIf { it.isNotBlank() }?.let { put("section", it) }
            snapshot.userProfile?.rollNumber?.takeIf { it.isNotBlank() }?.let { put("roll", it) }
            snapshot.userProfile?.bloodGroup?.takeIf { it.isNotBlank() }?.let { put("blood", it) }
        })
        put("totals", JSONObject().apply {
            put("books", snapshot.books.size)
            put("teachers", snapshot.teachers.size)
            put("classrooms", snapshot.classrooms.size)
            put("donors", snapshot.donors.size)
            put("routineSlots", snapshot.routinePlan?.entries?.size ?: 0)
        })
    }

    if (!campusQuery) {
        return json.toString()
    }

    val includeAllFallback = !specificDomainMatched

    if (wantBooks || includeAllFallback) {
        val books = snapshot.books
            .sortedByDescending { b ->
                var score = tokens.count { tok ->
                    b.title.lowercase().contains(tok) ||
                        b.subjectCode.lowercase().contains(tok) ||
                        b.departmentSemesters.keys.any { it.lowercase() == tok }
                } * 3
                if (userDept.isNotBlank() && b.departmentSemesters.containsKey(userDept)) score += 1
                score
            }
            .take(if (wantBooks) 18 else 8)

        if (books.isNotEmpty()) {
            json.put("books", JSONArray().apply {
                books.forEach { b ->
                    put(JSONObject().apply {
                        put("code", b.subjectCode)
                        put("title", b.title)
                        put("credit", b.credit)
                        if (b.theoryClassesPerWeek > 0) put("theory", b.theoryClassesPerWeek)
                        if (b.practicalClassesPerWeek > 0) put("practical", b.practicalClassesPerWeek)
                        if (b.departmentSemesters.isNotEmpty()) {
                            put("depts", b.departmentSemesters.entries.joinToString("; ") { (d, sems) ->
                                "$d:${sems.joinToString(",") { RoutineRules.semesterShortLabel(it) }}"
                            })
                        }
                    })
                }
            })
        }
    }

    if (wantTeachers || includeAllFallback) {
        val prefByEmail = snapshot.teacherPrefs.associateBy { it.email.trim().lowercase() }
        val teachers = snapshot.teachers
            .sortedByDescending { t ->
                var score = tokens.count { tok ->
                    t.name.lowercase().contains(tok) || t.department.lowercase() == tok
                } * 3
                if (userDept.isNotBlank() && t.department.equals(userDept, true)) score += 1
                score
            }
            .take(if (wantTeachers) 18 else 8)

        if (teachers.isNotEmpty()) {
            json.put("teachers", JSONArray().apply {
                teachers.forEach { t ->
                    val pref = prefByEmail[t.email.trim().lowercase()]
                    put(JSONObject().apply {
                        put("name", t.name)
                        if (t.department.isNotBlank()) put("dept", t.department)
                        val shift = pref?.shift?.ifBlank { t.shift } ?: t.shift
                        if (shift.isNotBlank()) put("shift", shift)
                        if (t.phone.isNotBlank()) put("phone", t.phone)
                        if (pref != null && pref.semesterSubjects.isNotEmpty()) {
                            put("subjects", pref.semesterSubjects.entries.joinToString("; ") { (sem, codes) ->
                                "${RoutineRules.semesterShortLabel(sem)}:[${codes.joinToString(",")}]"
                            })
                        }
                    })
                }
            })
        }
    }

    if ((wantRooms || includeAllFallback) && snapshot.classrooms.isNotEmpty()) {
        val rooms = snapshot.classrooms
            .sortedByDescending { r ->
                tokens.count { tok ->
                    r.roomNumber.lowercase().contains(tok) ||
                        r.buildingOrFloor.lowercase().contains(tok) ||
                        r.effectiveNearestDepartments().any { it.lowercase() == tok }
                } * 3
            }
            .take(if (wantRooms) 18 else 8)

        json.put("classrooms", JSONArray().apply {
            rooms.forEach { r ->
                put(JSONObject().apply {
                    put("room", r.roomNumber)
                    put("type", r.roomType.name)
                    if (r.buildingOrFloor.isNotBlank()) put("floor", r.buildingOrFloor)
                    val nearest = r.effectiveNearestDepartments().joinToString(", ")
                    if (nearest.isNotBlank()) put("near", nearest)
                })
            }
        })
    }

    if ((wantDonors || includeAllFallback) && snapshot.donors.isNotEmpty()) {
        val donors = snapshot.donors
            .sortedByDescending { d ->
                tokens.count { tok ->
                    d.bloodGroup.lowercase() == tok ||
                        d.department.lowercase() == tok ||
                        d.name.lowercase().contains(tok)
                }
            }
            .take(if (wantDonors) 18 else 8)

        json.put("donors", JSONArray().apply {
            donors.forEach { d ->
                put(JSONObject().apply {
                    put("name", d.name)
                    put("blood", d.bloodGroup)
                    if (d.department.isNotBlank()) put("dept", d.department)
                    if (d.phone.isNotBlank()) put("phone", d.phone)
                })
            }
        })
    }

    if (wantRoutine || includeAllFallback) {
        val routineEntries = (snapshot.routinePlan?.entries ?: emptyList())
            .sortedByDescending { e ->
                var score = 0
                if (e.day.equals(todayEnglishDay, true)) score += 2
                if (userDept.isNotBlank() && e.department.equals(userDept, true)) score += 2
                score += tokens.count { tok ->
                    e.department.lowercase() == tok ||
                        e.day.lowercase().startsWith(tok) ||
                        e.subjectTitle.lowercase().contains(tok) ||
                        e.teacherName.lowercase().contains(tok) ||
                        e.subjectCode.lowercase().contains(tok) ||
                        e.roomNumber.lowercase().contains(tok)
                } * 3
                score
            }
            .take(if (wantRoutine) 22 else 10)

        if (routineEntries.isNotEmpty()) {
            json.put("routineSlots", JSONArray().apply {
                routineEntries.forEach { e ->
                    put(JSONObject().apply {
                        put("day", e.day)
                        put("p", e.periodIndex + 1)
                        put("time", "${e.startTime}-${e.endTime}")
                        put("dept", e.department)
                        put("sem", RoutineRules.semesterShortLabel(e.semester))
                        if (e.group.isNotBlank()) put("grp", e.group)
                        put("shift", e.shift)
                        put("subj", "${e.subjectTitle} (${e.subjectCode})")
                        if (e.isPractical) put("lab", true)
                        if (e.teacherName.isNotBlank()) put("teacher", e.teacherName)
                        if (e.roomNumber.isNotBlank()) put("room", e.roomNumber)
                    })
                }
            })
        }
    }

    return json.toString()
}
