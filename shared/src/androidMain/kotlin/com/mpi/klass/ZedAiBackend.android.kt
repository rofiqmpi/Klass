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
private const val GEMINI_GA_FLASH_MODEL = "gemini-2.5-flash"
private const val GEMINI_GA_LITE_MODEL = "gemini-2.5-flash-lite"
private const val PRIMARY_STEP_TIMEOUT_MS = 22_000L
private const val FALLBACK_STEP_TIMEOUT_MS = 16_000L

private val ZED_STRUCTURED_SYSTEM_PROMPT = """
    You are Zed, the intelligent, privacy-first AI assistant inside the MPI Klass Android app (Mymensingh Polytechnic Institute).

    STRICT SECURITY & PRIVACY RULES:
    - NEVER reveal, ask for, or invent personal phone numbers, personal emails, passwords, OTPs, tokens, or private identity numbers.
    - If a user asks for anyone's password, email, or phone number, politely explain that Zed AI protects user security/privacy and guide them with an ACTION_ROW button to open Teachers or Blood Finder inside the app.

    STRICT MODERATION & VERIFIED LEARNING:
    - NEVER respond playfully to profanity, slurs, vulgarity, or abusive messages. Firmly decline and NEVER include or learn any inappropriate word.
    - NEVER accept or store false claims, rumors, or fabricated campus rules as facts.
    - If (and ONLY if) the user shares a genuinely true, helpful, non-sensitive campus or study fact (with zero PII and zero profanity), set `learnedFact` with `isVerifiedFactual=true` so Zed can remember it accurately for future chats. Otherwise omit `learnedFact`.

    CAMPUS DATABASE QUERYING & GOOGLE A2UI OUTPUT:
    - You receive CAMPUS_DATA JSON containing filtered Books Library results, Blood Finder group summaries & safe donor metadata, Teachers directory (no contacts), Classrooms (with `labSpecialty` for practical labs), Published Class Routine, and Learned Knowledge.
    - STRICT LAB ROUTING KNOWLEDGE: Physics practicals (25912, 25922) MUST be routed to Physics Lab; Chemistry practicals (25913) to Chemistry Lab; Engineering Drawing/CAD (21011, CAD subjects) to Drawing/CAD Lab; Programming/Networking to Computer Lab; Electrical to Electrical Lab; Electronics to Electronics Lab; Civil/Survey/Hydraulics to Civil Lab; Workshop/Welding/Foundry to Mechanical Workshop; Power/Auto/IC Engine to Power & Automobile Lab; Electro-Medical to Electro-Medical Lab.
    - Output valid JSON matching the schema:
      * answerText: Clear, concise answer grounded strictly in CAMPUS_DATA (reply in Bengali if user writes in Bengali/Banglish, else English).
      * sourceBadge: Short topic label e.g. "Books Library", "Blood Finder", "Teachers", "Classrooms", "Class Routine", "Zed AI".
      * isSupportedDomain: true.
      * blocks: Ordered Google A2UI cards:
        - HEADER_BANNER (title, subtitle, domain)
        - STAT_ROW (2-3 metrics in items)
        - KEY_VALUE_CARD (title + key-value pairs in items)
        - ENTITY_LIST (books, safe donor summary, teachers, classrooms — with optional `actionType` + filter args like `COPY_TEXT`, `OPEN_BOOKS`, `OPEN_BLOOD`, `OPEN_TEACHERS`, `OPEN_CLASSROOMS`, `OPEN_ROUTINE`)
        - SCHEDULE_LIST (timetable slots — `badge` = day/period, `primary` = subject, `secondary` = time/teacher/room)
        - CALLOUT (important note in `message`)
        - ACTION_ROW (actionable pill buttons in `items`: `primary` = button label, `actionType` = one of OPEN_BOOKS/OPEN_BLOOD/OPEN_TEACHERS/OPEN_CLASSROOMS/OPEN_ROUTINE/COPY_TEXT, plus optional `dept`, `semester`, `bloodGroup`, `query`). Include ACTION_ROW whenever Books, Blood Finder, Teachers, Classrooms, or Routine are discussed!
    - Never include preset follow-up question lists.
""".trimIndent()

private val ZED_PLAIN_SYSTEM_PROMPT = """
    You are Zed, the friendly, privacy-safe AI assistant inside the MPI Klass Android app (Mymensingh Polytechnic Institute).

    STRICT SECURITY & MODERATION RULES:
    - NEVER reveal, ask for, or invent user passwords, personal phone numbers, personal emails, or security credentials. Direct contact lookups to the authorized Blood Finder or Teachers screen inside the app.
    - NEVER reply casually to profanity, obscene, or abusive prompts; decline respectfully and never echo or learn vulgar words.
    - NEVER confirm false claims or rumors.

    CAPABILITIES:
    1) MPI Klass Campus Queries grounded strictly in CAMPUS_DATA JSON (Books Library with dept/sem/credit/theory-practical filtering, Blood Finder summaries, Teachers, Classrooms & specialized Labs — Physics Lab for Physics practicals, Chemistry Lab for Chemistry practicals, Drawing/CAD Lab, Computer/Electrical/Electronics/Civil/Mechanical/Power/Electro-Medical Labs — Published Routine, and Verified Learned Knowledge).
    2) General Knowledge & Polytechnic Study Help across coding, science, math, and assignments.
    - Reply in Bengali when the user writes in Bengali or Banglish; otherwise English.
    - Respond directly in clear natural prose without raw JSON fences unless code was requested.
""".trimIndent()

private val zedItemSchema = Schema.obj(
    properties = mapOf(
        "primary" to Schema.string(description = "Primary title, metric label, key, or button label"),
        "secondary" to Schema.string(description = "Secondary value, details, or time/room"),
        "badge" to Schema.string(description = "Optional short badge like subject code, day/period, or blood group"),
        "isPractical" to Schema.boolean(description = "True if the schedule slot is a practical lab"),
        "actionType" to Schema.enumeration(
            values = listOf(
                "OPEN_BOOKS",
                "OPEN_BLOOD",
                "OPEN_TEACHERS",
                "OPEN_CLASSROOMS",
                "OPEN_ROUTINE",
                "OPEN_NOTIFICATIONS",
                "COPY_TEXT",
                "ASK_FOLLOWUP",
            ),
            description = "Optional A2UI actionable command type",
        ),
        "dept" to Schema.string(description = "Optional department filter e.g. CST, CT, ET"),
        "semester" to Schema.string(description = "Optional semester filter e.g. 1st, 4th"),
        "bloodGroup" to Schema.string(description = "Optional blood group filter e.g. O+, A+"),
        "query" to Schema.string(description = "Optional search text or text to copy"),
    ),
    optionalProperties = listOf("secondary", "badge", "isPractical", "actionType", "dept", "semester", "bloodGroup", "query"),
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
                "ACTION_ROW",
            ),
            description = "A2UI card block type",
        ),
        "title" to Schema.string(description = "Card, banner, or action row title"),
        "subtitle" to Schema.string(description = "Banner subtitle or list context"),
        "domain" to Schema.enumeration(
            values = listOf("BOOKS", "BLOOD", "TEACHERS", "CLASSROOMS", "ROUTINE", "GENERAL"),
            description = "Campus or general domain",
        ),
        "message" to Schema.string(description = "Callout card message"),
        "isWarning" to Schema.boolean(description = "Whether the callout is a warning"),
        "items" to Schema.array(
            items = zedItemSchema,
            description = "Ordered list of items for STAT_ROW, KEY_VALUE_CARD, ENTITY_LIST, SCHEDULE_LIST, or ACTION_ROW buttons",
        ),
    ),
    optionalProperties = listOf("title", "subtitle", "message", "isWarning", "items"),
)

private val zedLearnedFactSchema = Schema.obj(
    properties = mapOf(
        "topic" to Schema.string(description = "Short topic heading (2-6 words)"),
        "fact" to Schema.string(description = "Verified non-PII factual statement to remember"),
        "domain" to Schema.enumeration(
            values = listOf("BOOKS", "BLOOD", "TEACHERS", "CLASSROOMS", "ROUTINE", "GENERAL"),
            description = "Domain category",
        ),
        "isVerifiedFactual" to Schema.boolean(
            description = "Set true ONLY if the statement is accurate, truthful, non-profane, and contains zero personal PII",
        ),
    ),
)

internal val zedResponseSchema: Schema = Schema.obj(
    properties = mapOf(
        "answerText" to Schema.string(description = "Primary helpful answer text"),
        "sourceBadge" to Schema.string(description = "Short topic badge label"),
        "isSupportedDomain" to Schema.boolean(description = "True for supported campus or general queries"),
        "blocks" to Schema.array(
            items = zedBlockSchema,
            description = "A2UI display & action blocks",
        ),
        "learnedFact" to zedLearnedFactSchema,
    ),
    optionalProperties = listOf("sourceBadge", "isSupportedDomain", "blocks", "learnedFact"),
)

private object ZedFirebaseAiProvider {
    val structuredModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_PRIMARY_MODEL,
            generationConfig = generationConfig {
                temperature = 0.40f
                responseMimeType = "application/json"
                responseSchema = zedResponseSchema
            },
            systemInstruction = content { text(ZED_STRUCTURED_SYSTEM_PROMPT) },
        )
    }

    val gaStructuredModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_GA_FLASH_MODEL,
            generationConfig = generationConfig {
                temperature = 0.40f
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
                temperature = 0.50f
            },
            systemInstruction = content { text(ZED_PLAIN_SYSTEM_PROMPT) },
        )
    }

    val gaPlainModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_GA_FLASH_MODEL,
            generationConfig = generationConfig {
                temperature = 0.50f
            },
            systemInstruction = content { text(ZED_PLAIN_SYSTEM_PROMPT) },
        )
    }

    val litePlainModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_LITE_MODEL,
            generationConfig = generationConfig {
                temperature = 0.50f
            },
            systemInstruction = content { text(ZED_PLAIN_SYSTEM_PROMPT) },
        )
    }

    val gaLitePlainModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_GA_LITE_MODEL,
            generationConfig = generationConfig {
                temperature = 0.50f
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

    // Hard pre-flight safety gates: profanity/obscenity refusal & restricted PII guard
    if (ZedModeration.containsProfanityOrAbuse(trimmed) || ZedModeration.asksForRestrictedPersonalData(trimmed)) {
        return ZedAiEngine.evaluate(trimmed, snapshot, todayEnglishDay)
    }

    // Immediate offline / deterministic fallback for explicit local self-learning or offline mode
    if (!isNetworkAvailable()) {
        val local = ZedAiEngine.evaluate(trimmed, snapshot, todayEnglishDay)
        return local.copy(sourceBadge = "${local.sourceBadge} • Offline")
    }

    return try {
        getAppContext()?.let { ctx ->
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
        }

        val campusQuery = isCampusOrAppQuery(trimmed) ||
            ZedModeration.extractUserTaughtFactCandidate(trimmed, snapshot) != null
        val enrichedSnapshot = if (snapshot.books.size < BtebBooksCatalog.books.size) {
            val merged = (snapshot.books + BtebBooksCatalog.books)
                .distinctBy { it.id.ifBlank { it.subjectCode } }
            snapshot.copy(books = merged)
        } else {
            snapshot
        }

        val (currentTurnText, chatHistory) = withContext(Dispatchers.Default) {
            val contextJson = buildFullAppContextJson(trimmed, enrichedSnapshot, todayEnglishDay)
            val builtHistory = buildSanitizedChatHistory(history)
            "USER_QUERY: $trimmed\n\nCAMPUS_DATA:\n$contextJson" to builtHistory
        }

        Log.d(
            ZED_AI_TAG,
            "Sending query to Firebase AI (campus=$campusQuery, payload=${currentTurnText.length} chars, history=${chatHistory.size} turns)",
        )

        val responseText = try {
            if (campusQuery) {
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
        } catch (t: Throwable) {
            if (t is CancellationException && t !is TimeoutCancellationException) throw t
            if (t is PromptBlockedException) throw t
            val apiKey = KlassRuntimeConfig.geminiApiKey.trim()
            if (apiKey.isNotBlank()) {
                Log.w(ZED_AI_TAG, "Firebase AI failed (${t.localizedMessage}); retrying via Secrets GEMINI_API_KEY REST endpoint")
                executeGeminiRestInference(
                    message = currentTurnText,
                    history = history,
                    campusQuery = campusQuery,
                    apiKey = apiKey,
                )
            } else {
                throw t
            }
        }

        if (responseText.isBlank()) {
            throw IllegalStateException("Gemini returned an empty response.")
        }

        val jsonCandidate = if (campusQuery) extractJsonObjectIfWrapped(responseText) else null
        val parsedReply = if (jsonCandidate != null) {
            runCatching {
                parseZedA2UiResponse(jsonCandidate, enrichedSnapshot.userRole?.name ?: "USER")
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

        // Safety audit on output & attach actionable A2UI buttons if helpful
        val cleanAnswer = if (ZedModeration.containsSensitivePii(parsedReply.answerText)) {
            if (isBn) {
                "অ্যাপের নিরাপত্তার স্বার্থে ব্যক্তিগত ফোন নম্বর বা ইমেইল প্রদর্শন করা হয়নি। নিচের বাটন থেকে অনুমোদিত স্ক্রিনে দেখুন।"
            } else {
                "Personal emails and phone numbers are protected and omitted for security. Use the action buttons below to browse authorized screens."
            }
        } else {
            parsedReply.answerText
        }

        val finalLearnedFact = parsedReply.learnedFact ?: ZedModeration.extractUserTaughtFactCandidate(trimmed, enrichedSnapshot)
        val updatedBlocks = enrichWithDefaultActionsIfNeeded(
            query = trimmed,
            blocks = parsedReply.blocks,
            snapshot = enrichedSnapshot,
            isBn = isBn,
        )

        parsedReply.copy(
            answerText = cleanAnswer,
            blocks = updatedBlocks,
            learnedFact = finalLearnedFact,
        )
    } catch (e: TimeoutCancellationException) {
        currentCoroutineContext().ensureActive()
        Log.e(ZED_AI_TAG, "Firebase AI request timed out", e)
        ZedAiEngine.evaluate(trimmed, snapshot, todayEnglishDay)
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        val appCheckDiag = if (isAppCheckOrAuthFailure(t)) {
            withTimeoutOrNull(4_000L) { ZedAppCheckDiagnostics.checkError() }
        } else {
            null
        }
        val detail = formatThrowableDetail(t, appCheckDiag)
        Log.e(ZED_AI_TAG, "Firebase AI call failed ($detail); falling back to ZedAiEngine", t)
        val local = ZedAiEngine.evaluate(trimmed, snapshot, todayEnglishDay)
        if (local.isSupportedDomain) {
            local
        } else {
            val msg = if (isBn) "Zed AI সংযোগ ব্যর্থ হয়েছে: $detail" else "Zed AI request failed: $detail"
            ZedReply(
                answerText = msg,
                sourceBadge = "AI Error",
                isSupportedDomain = true,
                blocks = listOf(ZedA2UiBlock.CalloutCard(message = msg, isWarning = true)),
            )
        }
    }
}

private fun enrichWithDefaultActionsIfNeeded(
    query: String,
    blocks: List<ZedA2UiBlock>,
    snapshot: ZedContextSnapshot,
    isBn: Boolean,
): List<ZedA2UiBlock> {
    if (blocks.any { it is ZedA2UiBlock.ActionRow }) return blocks
    val q = query.lowercase()
    val bannerDomain = blocks.filterIsInstance<ZedA2UiBlock.HeaderBanner>().firstOrNull()?.domain?.uppercase()
    val actions = mutableListOf<ZedA2UiAction>()

    val bookFilter = ZedAiEngine.parseBookFilter(
        rawQuery = query,
        userDept = snapshot.userProfile?.department,
        userSem = snapshot.userProfile?.semester,
    )
    if (bannerDomain == "BOOKS" || q.contains("book") || q.contains("library") || q.contains("বই") || Regex("\\b\\d{5}\\b").containsMatchIn(q)) {
        actions.add(
            ZedA2UiAction(
                label = if (isBn) "Books Library খুলুন" else "Open Books Library",
                actionType = ZedActionType.OPEN_BOOKS,
                dept = bookFilter.dept.orEmpty(),
                semester = bookFilter.semesterKey.orEmpty(),
                query = bookFilter.subjectCode ?: bookFilter.searchKeywords.firstOrNull().orEmpty(),
            )
        )
    }

    val bloodGroup = RoutineRules.BLOOD_GROUPS.firstOrNull { bg ->
        Regex("(?i)(?:^|\\s)${Regex.escape(bg)}(?:\\s|\$|[?,!।])").containsMatchIn(query)
    }
    if (bannerDomain == "BLOOD" || bloodGroup != null || q.contains("blood") || q.contains("donor") || q.contains("রক্ত")) {
        actions.add(
            ZedA2UiAction(
                label = if (bloodGroup != null) "Open Blood Finder ($bloodGroup)" else "Open Blood Finder",
                actionType = ZedActionType.OPEN_BLOOD,
                bloodGroup = bloodGroup.orEmpty(),
                query = ZedAiEngine.detectRequestedDept(q).orEmpty(),
            )
        )
    }

    if (bannerDomain == "TEACHERS" || q.contains("teacher") || q.contains("শিক্ষক")) {
        actions.add(
            ZedA2UiAction(
                label = if (isBn) "Teachers খুলুন" else "Open Teachers",
                actionType = ZedActionType.OPEN_TEACHERS,
            )
        )
    }

    if (bannerDomain == "CLASSROOMS" || q.contains("classroom") || q.contains("room") || q.contains("lab") || q.contains("রুম")) {
        actions.add(
            ZedA2UiAction(
                label = if (isBn) "Classrooms খুলুন" else "Open Classrooms",
                actionType = ZedActionType.OPEN_CLASSROOMS,
            )
        )
    }

    if (bannerDomain == "ROUTINE" || q.contains("routine") || q.contains("রুটিন") || q.contains("class")) {
        actions.add(
            ZedA2UiAction(
                label = if (isBn) "রুটিন দেখুন" else "Open Routine",
                actionType = ZedActionType.OPEN_ROUTINE,
            )
        )
    }

    return if (actions.isEmpty()) {
        blocks
    } else {
        blocks + ZedA2UiBlock.ActionRow(
            title = if (isBn) "সরাসরি অ্যাকশন" else "Quick Actions",
            actions = actions.distinctBy { it.actionType },
        )
    }
}

private suspend fun executeCampusInferenceWithFallbacks(
    message: String,
    history: List<Content>,
    onPartialText: (String) -> Unit,
): String {
    var firstFailure: Throwable? = null
    for (structuredCandidate in listOf(ZedFirebaseAiProvider.structuredModel, ZedFirebaseAiProvider.gaStructuredModel)) {
        try {
            val unary = withTimeout(PRIMARY_STEP_TIMEOUT_MS) {
                generateUnary(
                    model = structuredCandidate,
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
            Log.w(ZED_AI_TAG, "Structured campus call failed (${t::class.simpleName}: ${t.localizedMessage})", t)
            firstFailure = t
        }
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

    val plainCandidates = listOf(
        ZedFirebaseAiProvider.plainModel,
        ZedFirebaseAiProvider.gaPlainModel,
        ZedFirebaseAiProvider.litePlainModel,
        ZedFirebaseAiProvider.gaLitePlainModel,
    )
    for ((idx, candidate) in plainCandidates.withIndex()) {
        val timeoutMs = if (idx == 0) PRIMARY_STEP_TIMEOUT_MS else FALLBACK_STEP_TIMEOUT_MS
        try {
            val streamed = withTimeout(timeoutMs) {
                streamChat(
                    model = candidate,
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
            lastError = t
            Log.w(ZED_AI_TAG, "Stream attempt #$idx failed (${t::class.simpleName}: ${t.localizedMessage}); trying unary", t)
        }

        try {
            val unary = withTimeout(FALLBACK_STEP_TIMEOUT_MS) {
                generateUnary(
                    model = candidate,
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
            lastError = t
            Log.w(ZED_AI_TAG, "Unary attempt #$idx failed (${t::class.simpleName}: ${t.localizedMessage})", t)
        }
    }

    throw (lastError ?: IllegalStateException("All Firebase AI model attempts returned blank."))
}

private suspend fun executeGeminiRestInference(
    message: String,
    history: List<AiChatMessage>,
    campusQuery: Boolean,
    apiKey: String,
): String = withContext(Dispatchers.IO) {
    val systemPrompt = if (campusQuery) ZED_STRUCTURED_SYSTEM_PROMPT else ZED_PLAIN_SYSTEM_PROMPT
    val contentsArray = JSONArray()
    val turns = mutableListOf<Pair<String, String>>()
    for (msg in history.takeLast(8)) {
        val txt = msg.text.trim()
        if (txt.isBlank()) continue
        if (ZedModeration.containsProfanityOrAbuse(txt) || ZedModeration.containsSensitivePii(txt)) continue
        val role = if (msg.isFromUser) "user" else "model"
        if (turns.isEmpty() && role == "model") continue
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
    turns.forEach { (role, text) ->
        contentsArray.put(JSONObject().apply {
            put("role", role)
            put("parts", JSONArray().put(JSONObject().put("text", text)))
        })
    }
    contentsArray.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().put(JSONObject().put("text", message)))
    })

    val payload = JSONObject().apply {
        put("systemInstruction", JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
        })
        put("contents", contentsArray)
        put("generationConfig", JSONObject().apply {
            put("temperature", if (campusQuery) 0.4 else 0.5)
            if (campusQuery) {
                put("responseMimeType", "application/json")
            }
        })
    }.toString()

    val restModels = listOf(GEMINI_GA_FLASH_MODEL, GEMINI_GA_LITE_MODEL, "gemini-2.0-flash")
    var lastRestError: Throwable? = null
    for (modelId in restModels) {
        try {
            val endpoint = java.net.URL(
                "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey",
            )
            val conn = (endpoint.openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 25_000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            try {
                conn.outputStream.use { os -> os.write(payload.toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
                if (code !in 200..299) {
                    val errMsg = runCatching {
                        JSONObject(body).optJSONObject("error")?.optString("message").orEmpty()
                    }.getOrDefault("")
                    lastRestError = IllegalStateException("Gemini REST ($modelId) HTTP $code: ${errMsg.ifBlank { body.take(180) }}")
                    Log.w(ZED_AI_TAG, lastRestError.message.orEmpty())
                    continue
                }
                val root = JSONObject(body)
                val parts = root.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                val combined = buildString {
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val t = parts.optJSONObject(i)?.optString("text").orEmpty()
                            if (t.isNotBlank()) {
                                if (isNotEmpty()) append('\n')
                                append(t)
                            }
                        }
                    }
                }.trim()
                if (combined.isNotBlank()) return@withContext combined
            } finally {
                conn.disconnect()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            lastRestError = t
            Log.w(ZED_AI_TAG, "Gemini REST ($modelId) failed: ${t.message}")
        }
    }
    throw (lastRestError ?: IllegalStateException("Gemini REST request returned an empty response."))
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
        if (ZedModeration.containsProfanityOrAbuse(txt) || ZedModeration.containsSensitivePii(txt)) continue
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

private fun parseActionFromJsonItem(obj: JSONObject, fallbackLabel: String = ""): ZedA2UiAction? {
    val type = ZedActionType.fromRaw(obj.safeString("actionType")) ?: return null
    val label = obj.safeString("primary")
        .ifBlank { obj.safeString("label") }
        .ifBlank { fallbackLabel }
        .ifBlank { "Open" }
    return ZedA2UiAction(
        label = label,
        actionType = type,
        dept = obj.safeString("dept"),
        semester = obj.safeString("semester"),
        bloodGroup = obj.safeString("bloodGroup"),
        query = obj.safeString("query"),
    )
}

internal fun parseZedA2UiResponse(jsonString: String, authorRole: String = "USER"): ZedReply {
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
        val itemsArr = obj.optJSONArray("items") ?: obj.optJSONArray("actions") ?: JSONArray()

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
                    if (k.isNotBlank() && v.isNotBlank() && !ZedModeration.containsSensitivePii(v)) {
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
                    val action = parseActionFromJsonItem(it, fallbackLabel = if (badge.isNotBlank()) "View" else "Open")
                    if (prim.isNotBlank() && !ZedModeration.containsSensitivePii(prim) && !ZedModeration.containsSensitivePii(sec)) {
                        entities.add(ZedEntityItem(primary = prim, secondary = sec, badge = badge, action = action))
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
            "ACTION_ROW" -> {
                val actions = mutableListOf<ZedA2UiAction>()
                for (j in 0 until itemsArr.length()) {
                    val it = itemsArr.optJSONObject(j) ?: continue
                    parseActionFromJsonItem(it)?.let { act -> actions.add(act) }
                }
                if (actions.isNotEmpty()) {
                    blocks.add(ZedA2UiBlock.ActionRow(title = title.ifBlank { "Quick Actions" }, actions = actions))
                }
            }
        }
    }

    val learnedFactObj = root.optJSONObject("learnedFact")
    val learnedEntry = if (learnedFactObj != null && learnedFactObj.optBoolean("isVerifiedFactual", false)) {
        ZedModeration.sanitizeLearnedEntry(
            topic = learnedFactObj.safeString("topic"),
            fact = learnedFactObj.safeString("fact"),
            domain = learnedFactObj.safeString("domain", "GENERAL"),
            authorRole = authorRole,
        )
    } else {
        null
    }

    return ZedReply(
        answerText = answerText.ifBlank { "Here is your answer from Zed AI." },
        sourceBadge = sourceBadge,
        isSupportedDomain = isSupported,
        blocks = blocks,
        learnedFact = learnedEntry,
    )
}

private val QUERY_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}+-]+")
private val SUBJECT_CODE_REGEX = Regex("\\b\\d{5}\\b")

private val CAMPUS_EXACT_TOKENS = setOf(
    "book", "books", "syllabus", "subject", "subjects", "code", "codes", "credit", "credits", "bteb", "probidhan",
    "practical", "theory", "lab", "labs",
    "teacher", "teachers", "sir", "madam", "faculty",
    "routine", "schedule", "schedules", "class", "classes", "period", "periods", "today", "tomorrow", "timetable",
    "room", "rooms", "classroom", "classrooms", "building", "floor",
    "blood", "donor", "donors", "a+", "a-", "b+", "b-", "o+", "o-", "ab+", "ab-",
    "mpi", "klass", "polytechnic", "mymensingh",
    "semester", "semesters", "sem", "shift", "shifts", "section", "department", "departments", "dept", "depts",
    "cst", "mt", "et", "ent", "ct", "rac", "ec", "pt", "emt",
    "profile", "account", "admin", "guest", "app", "zed",
)

private val CAMPUS_BN_STEMS = listOf(
    "বই", "সিলেবাস", "বিষয়", "কোড", "ক্রেডিট", "ব্যবহারিক", "থিউরি",
    "শিক্ষক", "স্যার", "ম্যাম",
    "রুটিন", "ক্লাস", "পিরিয়ড", "সময়", "আজ", "আগামীকাল",
    "রুম", "ল্যাব", "কক্ষ", "ভবন", "ফ্লোর",
    "রক্ত", "ডোনার", "দাতা",
    "সেমিস্টার", "শিফট", "ডিপার্টমেন্ট", "প্রোফাইল", "ক্যাম্পাস", "অ্যাপ",
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
        setOf("book", "books", "syllabus", "subject", "subjects", "code", "codes", "credit", "credits", "bteb", "probidhan", "semester", "semesters", "sem", "practical", "theory"),
        listOf("বই", "সিলেবাস", "বিষয়", "কোড", "ক্রেডিট", "সেমিস্টার", "ব্যবহারিক", "থিউরি"),
    ) || SUBJECT_CODE_REGEX.containsMatchIn(q)
    val wantTeachers = wordTokens.matchesCampusKeywords(
        setOf("teacher", "teachers", "sir", "madam", "faculty"),
        listOf("শিক্ষক", "স্যার", "ম্যাম"),
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
        // STRICT PII SCRUBBING: Zero email, phone, password, roll, or registration exposed to AI
        put("currentUser", JSONObject().apply {
            put("name", snapshot.userDisplayName)
            put("role", snapshot.userRole?.name ?: "GUEST")
            if (userDept.isNotBlank()) put("department", userDept)
            snapshot.userProfile?.semester?.takeIf { it.isNotBlank() }?.let { put("semester", it) }
            snapshot.userProfile?.shift?.takeIf { it.isNotBlank() }?.let { put("shift", it) }
            snapshot.userProfile?.section?.takeIf { it.isNotBlank() }?.let { put("section", it) }
            snapshot.userProfile?.bloodGroup?.takeIf { it.isNotBlank() }?.let { put("blood", it) }
        })
        put("totals", JSONObject().apply {
            put("books", snapshot.books.size)
            put("teachers", snapshot.teachers.size)
            put("classrooms", snapshot.classrooms.size)
            put("donors", snapshot.donors.size)
            put("routineSlots", snapshot.routinePlan?.entries?.size ?: 0)
            put("learnedFacts", snapshot.learnedKnowledge.size)
        })
    }

    // Always include relevant verified learned facts (PII-free)
    if (snapshot.learnedKnowledge.isNotEmpty()) {
        val relevantKnowledge = snapshot.learnedKnowledge
            .filter { !ZedModeration.containsSensitivePii(it.fact) && !ZedModeration.containsProfanityOrAbuse(it.fact) }
            .sortedByDescending { k ->
                val text = "${k.topic} ${k.fact}".lowercase()
                tokens.count { tok -> text.contains(tok) }
            }
            .take(8)
        if (relevantKnowledge.isNotEmpty()) {
            json.put("learnedKnowledge", JSONArray().apply {
                relevantKnowledge.forEach { k ->
                    put(JSONObject().apply {
                        put("topic", k.topic)
                        put("fact", k.fact)
                        put("domain", k.domain)
                    })
                }
            })
        }
    }

    if (!campusQuery) {
        return json.toString()
    }

    val includeAllFallback = !specificDomainMatched

    if (wantBooks || includeAllFallback) {
        val filter = ZedAiEngine.parseBookFilter(
            rawQuery = query,
            userDept = snapshot.userProfile?.department,
            userSem = snapshot.userProfile?.semester,
        )
        val matched = ZedAiEngine.filterBooks(snapshot.books, filter)
        val books = (if (matched.isNotEmpty()) matched else snapshot.books)
            .take(if (wantBooks) 24 else 8)

        json.put("booksMatchCount", matched.size)
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
                        // NO phone or email included for privacy protection
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

    val detectedLab = ZedAiEngine.detectRequestedLabSpecialty(query, snapshot.books)
    if (detectedLab != null) {
        val (spec, book) = detectedLab
        json.put("requestedLabRouting", JSONObject().apply {
            put("requiredLabSpecialty", spec.label)
            if (book != null) {
                put("subjectCode", book.subjectCode)
                put("subjectTitle", book.title)
            }
        })
    }

    if ((wantRooms || detectedLab != null || includeAllFallback) && snapshot.classrooms.isNotEmpty()) {
        val requestedSpec = detectedLab?.first
        val rooms = snapshot.classrooms
            .sortedByDescending { r ->
                var score = 0
                if (requestedSpec != null && r.roomType == ClassroomType.LAB && r.effectiveLabSpecialty() == requestedSpec) {
                    score += 100
                }
                score += tokens.count { tok ->
                    r.roomNumber.lowercase().contains(tok) ||
                        r.buildingOrFloor.lowercase().contains(tok) ||
                        r.effectiveLabSpecialty().label.lowercase().contains(tok) ||
                        r.effectiveNearestDepartments().any { it.lowercase() == tok }
                } * 3
                score
            }
            .take(if (wantRooms || detectedLab != null) 18 else 8)

        json.put("classrooms", JSONArray().apply {
            rooms.forEach { r ->
                put(JSONObject().apply {
                    put("room", r.roomNumber)
                    put("type", r.roomType.name)
                    if (r.roomType == ClassroomType.LAB) {
                        put("labSpecialty", r.effectiveLabSpecialty().label)
                    }
                    if (r.buildingOrFloor.isNotBlank()) put("floor", r.buildingOrFloor)
                    val nearest = r.effectiveNearestDepartments().joinToString(", ")
                    if (nearest.isNotBlank()) put("near", nearest)
                })
            }
        })
    }

    if ((wantDonors || includeAllFallback) && snapshot.donors.isNotEmpty()) {
        // Aggregated blood group counts across entire DB (safe stats, zero PII)
        val groupCounts = JSONObject()
        snapshot.donors.groupingBy { it.bloodGroup.uppercase() }.eachCount().forEach { (bg, count) ->
            if (bg.isNotBlank()) groupCounts.put(bg, count)
        }
        json.put("bloodGroupSummary", groupCounts)

        val reqBg = RoutineRules.BLOOD_GROUPS.firstOrNull { bg ->
            Regex("(?i)(?:^|\\s)${Regex.escape(bg)}(?:\\s|\$|[?,!।])").containsMatchIn(query)
        }
        val reqDept = ZedAiEngine.detectRequestedDept(q)

        val donors = snapshot.donors
            .filter { d ->
                (reqBg == null || d.bloodGroup.equals(reqBg, true)) &&
                    (reqDept == null || d.department.equals(reqDept, true))
            }
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
                    if (d.roleLabel.isNotBlank()) put("role", d.roleLabel)
                    // NO phone, email, or home address PII included
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
