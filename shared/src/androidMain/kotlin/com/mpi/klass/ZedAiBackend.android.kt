package com.mpi.klass

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val ZED_AI_TAG = "ZedAiBackend"
private const val GEMINI_TIMEOUT_MS = 60_000L
private val GEMINI_CANDIDATE_MODELS = listOf(
    "gemini-2.5-flash",
    "gemini-3-flash-preview",
    "gemini-3.8-flash",
    "gemini-2.5-flash-lite",
)

private data class RestApiResult(
    val statusCode: Int,
    val body: String,
)

actual suspend fun askZedWithGemini(
    prompt: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
    history: List<AiChatMessage>,
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

    val apiKey = ZedAiKeyProvider.getApiKey()
    if (apiKey.isBlank()) {
        val msg = if (isBn) {
            "GEMINI_API_KEY সংযুক্ত করা হয়নি। AI Studio-এর Secrets প্যানেলে `GEMINI_API_KEY` যোগ করে অ্যাপটি আবার বিল্ড করুন।"
        } else {
            "GEMINI_API_KEY is not configured. Add `GEMINI_API_KEY` in the AI Studio Secrets panel and rebuild the app."
        }
        return ZedReply(
            answerText = msg,
            sourceBadge = "Setup Required",
            isSupportedDomain = true,
            blocks = listOf(
                ZedA2UiBlock.CalloutCard(message = msg, isWarning = true),
            ),
        )
    }

    return try {
        val contextJson = buildFullAppContextJson(trimmed, snapshot, todayEnglishDay)
        val requestPayload = buildGeminiRestPayload(
            prompt = trimmed,
            contextJson = contextJson,
            history = history,
        )

        val responseBody = withTimeout(GEMINI_TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                executeWithModelFallback(
                    apiKey = apiKey,
                    schemaPayloadJson = requestPayload,
                    plainPayloadJson = buildGeminiRestPayload(
                        prompt = trimmed,
                        contextJson = contextJson,
                        history = history,
                        useStructuredSchema = false,
                    ),
                )
            }
        }

        extractReplyFromGeminiRestJson(responseBody, isBn)
    } catch (e: TimeoutCancellationException) {
        Log.e(ZED_AI_TAG, "Gemini REST API request timed out after ${GEMINI_TIMEOUT_MS / 1000}s")
        val msg = if (isBn) {
            "Gemini API রিকোয়েস্ট টাইমআউট হয়েছে (${GEMINI_TIMEOUT_MS / 1000} সেকেন্ড)। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।"
        } else {
            "Gemini API request timed out after ${GEMINI_TIMEOUT_MS / 1000} seconds. Please check your connection and try again."
        }
        ZedReply(
            answerText = msg,
            sourceBadge = "Timeout",
            isSupportedDomain = true,
            blocks = listOf(ZedA2UiBlock.CalloutCard(message = msg, isWarning = true)),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val rawDetail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e::class.simpleName ?: "Unknown error"
        val detail = sanitizeSecret(rawDetail, apiKey)
        Log.e(ZED_AI_TAG, "Gemini REST API call failed: $detail")
        val msg = if (isBn) {
            "Gemini API কল ব্যর্থ হয়েছে: $detail"
        } else {
            "Gemini API call failed: $detail"
        }
        ZedReply(
            answerText = msg,
            sourceBadge = "API Error",
            isSupportedDomain = true,
            blocks = listOf(ZedA2UiBlock.CalloutCard(message = msg, isWarning = true)),
        )
    }
}

private fun sanitizeSecret(text: String, apiKey: String): String {
    var clean = text
    if (apiKey.isNotBlank()) {
        clean = clean.replace(apiKey, "[REDACTED]")
    }
    clean = clean.replace(Regex("AQ\\.[A-Za-z0-9_\\-]{8,}"), "[REDACTED]")
    clean = clean.replace(Regex("AIza[A-Za-z0-9_\\-]{10,}"), "[REDACTED]")
    return clean
}

private fun executeWithModelFallback(
    apiKey: String,
    schemaPayloadJson: String,
    plainPayloadJson: String,
): String {
    var lastError: RestApiResult? = null
    for (model in GEMINI_CANDIDATE_MODELS) {
        var result = postGeminiGenerateContent(model = model, apiKey = apiKey, payloadJson = schemaPayloadJson)
        if (result.statusCode in 200..299) {
            return result.body
        }
        // If structured responseSchema triggers 400 on this model, retry with unconstrained payload
        if (result.statusCode == 400 && (result.body.contains("schema", true) || result.body.contains("responseSchema", true) || result.body.contains("response_schema", true))) {
            result = postGeminiGenerateContent(model = model, apiKey = apiKey, payloadJson = plainPayloadJson)
            if (result.statusCode in 200..299) {
                return result.body
            }
        }
        lastError = result
        if (result.statusCode != 404 && result.statusCode != 400) {
            break
        }
        Log.w(ZED_AI_TAG, "Model $model returned HTTP ${result.statusCode}; trying next candidate model")
    }

    val err = lastError ?: throw IllegalStateException("No response received from Gemini API.")
    val apiMessage = runCatching {
        JSONObject(err.body).optJSONObject("error")?.optString("message").orEmpty()
    }.getOrDefault("").ifBlank { err.body.take(240) }
    throw IllegalStateException("HTTP ${err.statusCode}: ${sanitizeSecret(apiMessage, apiKey)}")
}

private fun postGeminiGenerateContent(
    model: String,
    apiKey: String,
    payloadJson: String,
): RestApiResult {
    val endpoint = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
    val conn = (endpoint.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 15_000
        readTimeout = 45_000
        doInput = true
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        setRequestProperty("x-goog-api-key", apiKey)
    }

    return try {
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(payloadJson)
            writer.flush()
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = if (stream != null) {
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        } else {
            ""
        }
        RestApiResult(statusCode = code, body = body)
    } finally {
        conn.disconnect()
    }
}

private fun buildGeminiRestPayload(
    prompt: String,
    contextJson: String,
    history: List<AiChatMessage>,
    useStructuredSchema: Boolean = true,
): String {
    val systemPrompt = """
        You are Zed, the intelligent, unrestricted AI assistant inside the MPI Klass Android app (Mymensingh Polytechnic Institute).

        You have two core capabilities:
        1) Complete App & Campus Intelligence:
           You receive the live, authoritative CAMPUS_DATA JSON below containing the user's profile, MPI institute rules, app features/screens, BTEB Books Library, Teachers Directory, Teacher Subject Preferences, Classrooms & Nearest Departments, Published Class Routine, and Blood Donors.
           - Answer any question about the app or MPI campus accurately using CAMPUS_DATA.
           - Ground EVERY specific person name, phone number, email, room number, subject code, and routine slot strictly in CAMPUS_DATA. Never invent missing contacts, rooms, or timetable slots.

        2) Unrestricted General Intelligence:
           You are also a full general-purpose Gemini AI assistant.
           - Answer ANY OTHER question the user asks—science, math, programming, polytechnic studies, history, general knowledge, coding help, translations, writing, career advice, or everyday conversation—thoroughly, clearly, and helpfully using your full knowledge!
           - Never refuse a valid general question or claim you are limited only to campus topics.

        Response & A2UI Formatting Rules:
        - Reply in Bengali when the user writes in Bengali or Banglish; otherwise reply in crisp English.
        - Output valid JSON matching the schema:
          * answerText: Clear, direct, helpful answer (concise when cards already list details; thorough and clear for general/educational explanations).
          * sourceBadge: e.g. "Zed AI", "Books Library", "Teachers", "Class Routine", "Blood Donor", "Classrooms", or topic label like "General AI".
          * isSupportedDomain: Always true for normal queries.
          * blocks: Optional ordered list of Material 3 A2UI cards to visually organize structured facts:
            - HEADER_BANNER (title, subtitle, domain)
            - STAT_ROW (2-3 primary/secondary metrics)
            - KEY_VALUE_CARD (title + key-value pairs)
            - ENTITY_LIST (books, donors, teachers, classrooms — include `phone` verbatim when present in CAMPUS_DATA)
            - SCHEDULE_LIST (timetable slots — `badge` = day/period e.g. "Sun P1", `primary` = subject, `secondary` = time/teacher/room)
            - CALLOUT (important tip or status note)
            For straightforward general chat/questions where prose alone is best, keep `blocks` empty or use a single concise KEY_VALUE_CARD/CALLOUT if genuinely helpful.
        - Never include preset follow-up question lists.
    """.trimIndent()

    val itemSchema = JSONObject().apply {
        put("type", "OBJECT")
        put("properties", JSONObject().apply {
            put("primary", JSONObject().put("type", "STRING"))
            put("secondary", JSONObject().put("type", "STRING"))
            put("badge", JSONObject().put("type", "STRING").put("nullable", true))
            put("phone", JSONObject().put("type", "STRING").put("nullable", true))
            put("isPractical", JSONObject().put("type", "BOOLEAN").put("nullable", true))
        })
        put("required", JSONArray().put("primary").put("secondary"))
    }

    val blockSchema = JSONObject().apply {
        put("type", "OBJECT")
        put("properties", JSONObject().apply {
            put("blockType", JSONObject().put("type", "STRING").put(
                "enum",
                JSONArray()
                    .put("HEADER_BANNER")
                    .put("STAT_ROW")
                    .put("KEY_VALUE_CARD")
                    .put("ENTITY_LIST")
                    .put("SCHEDULE_LIST")
                    .put("CALLOUT"),
            ))
            put("title", JSONObject().put("type", "STRING").put("nullable", true))
            put("subtitle", JSONObject().put("type", "STRING").put("nullable", true))
            put("domain", JSONObject().put("type", "STRING").put(
                "enum",
                JSONArray().put("BOOKS").put("BLOOD").put("TEACHERS").put("ROUTINE").put("GENERAL"),
            ))
            put("message", JSONObject().put("type", "STRING").put("nullable", true))
            put("isWarning", JSONObject().put("type", "BOOLEAN").put("nullable", true))
            put("items", JSONObject().put("type", "ARRAY").put("items", itemSchema).put("nullable", true))
        })
        put("required", JSONArray().put("blockType").put("domain"))
    }

    val responseSchema = JSONObject().apply {
        put("type", "OBJECT")
        put("properties", JSONObject().apply {
            put("answerText", JSONObject().put("type", "STRING"))
            put("sourceBadge", JSONObject().put("type", "STRING"))
            put("isSupportedDomain", JSONObject().put("type", "BOOLEAN"))
            put("blocks", JSONObject().put("type", "ARRAY").put("items", blockSchema))
        })
        put("required", JSONArray().put("answerText").put("sourceBadge").put("isSupportedDomain").put("blocks"))
    }

    val contentsArray = JSONArray()
    history.takeLast(10).forEach { msg ->
        val txt = msg.text.trim()
        if (txt.isNotBlank()) {
            contentsArray.put(
                JSONObject().apply {
                    put("role", if (msg.isFromUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", txt)))
                }
            )
        }
    }

    val currentTurnText = "USER_QUERY: $prompt\n\nCAMPUS_DATA:\n$contextJson"
    contentsArray.put(
        JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", currentTurnText)))
        }
    )

    return JSONObject().apply {
        put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
        put("contents", contentsArray)
        put("generationConfig", JSONObject().apply {
            put("temperature", 0.45)
            if (useStructuredSchema) {
                put("responseMimeType", "application/json")
                put("responseSchema", responseSchema)
            }
        })
    }.toString()
}

private fun extractReplyFromGeminiRestJson(restResponseJson: String, isBn: Boolean): ZedReply {
    val root = JSONObject(restResponseJson)
    val candidates = root.optJSONArray("candidates")
    if (candidates == null || candidates.length() == 0) {
        val blockReason = root.optJSONObject("promptFeedback")?.optString("blockReason").orEmpty()
        throw IllegalStateException(
            if (blockReason.isNotBlank()) "Prompt blocked: $blockReason" else "Gemini returned no candidates."
        )
    }

    val firstCandidate = candidates.getJSONObject(0)
    val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
    val rawText = buildString {
        if (parts != null) {
            for (i in 0 until parts.length()) {
                append(parts.optJSONObject(i)?.optString("text").orEmpty())
            }
        }
    }.trim()

    if (rawText.isBlank()) {
        val finishReason = firstCandidate.optString("finishReason")
        throw IllegalStateException("Empty Gemini response${if (finishReason.isNotBlank()) " ($finishReason)" else ""}.")
    }

    return runCatching {
        parseZedA2UiResponse(rawText)
    }.getOrElse {
        // Fallback if model returned plain conversational text instead of JSON
        ZedReply(
            answerText = rawText,
            sourceBadge = "Zed AI",
            isSupportedDomain = true,
            blocks = emptyList(),
        )
    }
}

private fun JSONObject.safeString(key: String, default: String = ""): String {
    if (!has(key) || isNull(key)) return default
    val v = optString(key, default).trim()
    return if (v.equals("null", ignoreCase = true)) default else v
}

private fun parseZedA2UiResponse(jsonString: String): ZedReply {
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

private fun buildFullAppContextJson(
    query: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
): String {
    val q = query.lowercase()
    val tokens = q.split(Regex("\\s+")).filter { it.length >= 2 }

    val books = snapshot.books
        .sortedByDescending { b ->
            tokens.count { tok ->
                b.title.lowercase().contains(tok) ||
                    b.subjectCode.lowercase().contains(tok) ||
                    b.departmentSemesters.keys.any { it.lowercase() == tok }
            }
        }
        .take(65)

    val prefByEmail = snapshot.teacherPrefs.associateBy { it.email.trim().lowercase() }
    val teachers = snapshot.teachers
        .sortedByDescending { t ->
            tokens.count { tok ->
                t.name.lowercase().contains(tok) || t.department.lowercase() == tok
            }
        }
        .take(50)

    val donors = snapshot.donors
        .sortedByDescending { d ->
            tokens.count { tok ->
                d.bloodGroup.lowercase() == tok ||
                    d.department.lowercase() == tok ||
                    d.name.lowercase().contains(tok)
            }
        }
        .take(45)

    val routineEntries = (snapshot.routinePlan?.entries ?: emptyList())
        .sortedByDescending { e ->
            var score = 0
            if (e.day.equals(todayEnglishDay, true)) score += 2
            if (snapshot.userProfile?.department?.equals(e.department, true) == true) score += 2
            score += tokens.count { tok ->
                e.department.lowercase() == tok ||
                    e.day.lowercase().startsWith(tok) ||
                    e.subjectTitle.lowercase().contains(tok) ||
                    e.teacherName.lowercase().contains(tok) ||
                    e.subjectCode.lowercase().contains(tok) ||
                    e.roomNumber.lowercase().contains(tok)
            }
            score
        }
        .take(65)

    return JSONObject().apply {
        put("today", todayEnglishDay)
        put("instituteOverview", JSONObject().apply {
            put("name", "Mymensingh Polytechnic Institute (MPI)")
            put("appName", "MPI Klass")
            put("curriculum", "BTEB Probidhan-2022")
            put("departments", JSONArray(RoutineRules.ALL_TEACHER_DEPARTMENTS))
            put("workingDays", JSONArray(RoutineRules.WORKING_DAYS))
            put("shifts", JSONObject().apply {
                put("1st Shift", "08:00 AM - 01:15 PM (7 periods x 45 mins)")
                put("2nd Shift", "01:30 PM - 06:45 PM (7 periods x 45 mins)")
            })
            put("appFeatures", JSONArray().apply {
                put("Home Dashboard & Role-based access (Student, Teacher, CR, Admin, Guest)")
                put("Books Library (full BTEB syllabus, subject codes, theory/practical periods, credits)")
                put("Class Routine Generator & Management (automated conflict-free routine prioritizing classrooms near each department & edit requests)")
                put("Teachers Directory & Multi-department Subject Preferences")
                put("Classroom Management (theory & lab rooms with multi-select nearest departments and shift availability)")
                put("Blood Donor Directory with one-tap calling")
                put("Notifications & Firebase Push Alerts")
                put("Profile & Edit Profile (Privacy controls, Bengali/English language toggle, Dark/Light/Auto theme)")
            })
        })
        put("currentUser", JSONObject().apply {
            put("name", snapshot.userDisplayName)
            put("email", snapshot.userFullProfile?.email ?: snapshot.userProfile?.email ?: "")
            put("role", snapshot.userRole?.name ?: "GUEST")
            put("department", snapshot.userProfile?.department ?: "")
            put("semester", snapshot.userProfile?.semester ?: "")
            put("shift", snapshot.userProfile?.shift ?: "")
            put("section", snapshot.userProfile?.section ?: "")
            put("rollNumber", snapshot.userProfile?.rollNumber ?: "")
            put("registrationNumber", snapshot.userProfile?.registrationNumber ?: "")
            put("bloodGroup", snapshot.userProfile?.bloodGroup ?: "")
            put("phone", snapshot.userProfile?.phone ?: "")
            put("address", snapshot.userProfile?.address ?: "")
        })
        put("totals", JSONObject().apply {
            put("books", snapshot.books.size)
            put("teachers", snapshot.teachers.size)
            put("teacherPrefs", snapshot.teacherPrefs.size)
            put("classrooms", snapshot.classrooms.size)
            put("donors", snapshot.donors.size)
            put("routineSlots", snapshot.routinePlan?.entries?.size ?: 0)
        })
        put("books", JSONArray().apply {
            books.forEach { b ->
                put(JSONObject().apply {
                    put("code", b.subjectCode)
                    put("title", b.title)
                    put("credit", b.credit)
                    put("theoryPeriods", b.theoryClassesPerWeek)
                    put("practicalPeriods", b.practicalClassesPerWeek)
                    put("departments", b.departmentSemesters.entries.joinToString("; ") { (d, sems) ->
                        "$d:${sems.joinToString(",") { RoutineRules.semesterShortLabel(it) }}"
                    })
                })
            }
        })
        put("teachers", JSONArray().apply {
            teachers.forEach { t ->
                val pref = prefByEmail[t.email.trim().lowercase()]
                put(JSONObject().apply {
                    put("name", t.name)
                    put("email", t.email)
                    put("department", t.department)
                    put("shift", pref?.shift?.ifBlank { t.shift } ?: t.shift)
                    put("phone", t.phone)
                    if (pref != null && pref.semesterSubjects.isNotEmpty()) {
                        put("teachingSubjects", pref.semesterSubjects.entries.joinToString("; ") { (sem, codes) ->
                            "${RoutineRules.semesterShortLabel(sem)}:[${codes.joinToString(",")}]"
                        })
                    }
                })
            }
        })
        put("classrooms", JSONArray().apply {
            snapshot.classrooms.take(50).forEach { r ->
                put(JSONObject().apply {
                    put("roomNumber", r.roomNumber)
                    put("type", r.roomType.name)
                    put("floorOrWing", r.buildingOrFloor)
                    put("nearestDepartments", r.effectiveNearestDepartments().joinToString(", ").ifBlank { "All Departments" })
                    put("shifts", r.shifts.joinToString(", "))
                })
            }
        })
        put("donors", JSONArray().apply {
            donors.forEach { d ->
                put(JSONObject().apply {
                    put("name", d.name)
                    put("bloodGroup", d.bloodGroup)
                    put("department", d.department)
                    put("role", d.roleLabel)
                    put("phone", d.phone)
                    put("address", d.address)
                })
            }
        })
        put("routineSlots", JSONArray().apply {
            routineEntries.forEach { e ->
                put(JSONObject().apply {
                    put("day", e.day)
                    put("period", e.periodIndex + 1)
                    put("time", "${e.startTime}-${e.endTime}")
                    put("dept", e.department)
                    put("sem", RoutineRules.semesterShortLabel(e.semester))
                    put("group", e.group)
                    put("shift", e.shift)
                    put("subject", "${e.subjectTitle} (${e.subjectCode})")
                    put("isLab", e.isPractical)
                    put("teacher", e.teacherName)
                    put("room", e.roomNumber)
                })
            }
        })
    }.toString()
}
