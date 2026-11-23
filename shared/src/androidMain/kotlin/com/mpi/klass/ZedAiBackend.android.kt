package com.mpi.klass

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val ZED_AI_TAG = "ZedAiBackend"
private const val GEMINI_TIMEOUT_MS = 45_000L

private val zedGeminiModel by lazy {
    val blockSchema = Schema.obj(
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
                description = "Material 3 A2UI component type to render",
            ),
            "title" to Schema.string(description = "Banner or section title", nullable = true),
            "subtitle" to Schema.string(description = "Optional banner subtitle", nullable = true),
            "domain" to Schema.enumeration(
                values = listOf("BOOKS", "BLOOD", "TEACHERS", "ROUTINE", "GENERAL"),
                description = "Color domain theme",
            ),
            "message" to Schema.string(description = "Callout notice text", nullable = true),
            "isWarning" to Schema.boolean(description = "True when callout highlights a warning", nullable = true),
            "items" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "primary" to Schema.string(description = "Primary title, person name, subject, or stat label"),
                        "secondary" to Schema.string(description = "Details, stat value, department/shift, or time range"),
                        "badge" to Schema.string(description = "Short pill badge like A+, 66651, CST, or P1", nullable = true),
                        "phone" to Schema.string(description = "Phone number for one-tap dialer when present in context", nullable = true),
                        "isPractical" to Schema.boolean(description = "True if slot is a lab/practical session", nullable = true),
                    ),
                    optionalProperties = listOf("badge", "phone", "isPractical"),
                ),
                description = "Structured items for STAT_ROW, KEY_VALUE_CARD, ENTITY_LIST, or SCHEDULE_LIST",
                nullable = true,
            ),
        ),
        optionalProperties = listOf("title", "subtitle", "domain", "message", "isWarning", "items"),
    )

    val replySchema = Schema.obj(
        properties = mapOf(
            "answerText" to Schema.string(
                description = "Concise 1-2 sentence answer in the user's language (Bengali for Bengali/Banglish queries, else English)",
            ),
            "sourceBadge" to Schema.string(
                description = "Domain source badge such as Books Library, Blood Donor, Teachers, Class Routine, or Zed AI",
            ),
            "isSupportedDomain" to Schema.boolean(
                description = "True for Books Library, Blood Donor, Teachers, Class Routine, or greetings; false for unrelated topics",
            ),
            "blocks" to Schema.array(
                items = blockSchema,
                description = "Ordered list of declarative A2UI cards/components representing the response data",
            ),
        ),
    )

    Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-3.8-flash",
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = replySchema
            temperature = 0.25f
        },
        systemInstruction = content {
            text(
                """
                You are Zed, the campus academic assistant inside MPI Klass (Mymensingh Polytechnic Institute).
                Allowed domains ONLY:
                1) Books Library (BTEB subjects, subject codes, credits, theory/practical periods, department semesters)
                2) Blood Donor directory (donors, blood groups, departments, phone numbers)
                3) Teachers Directory (faculty names, departments, shifts, phone numbers, weekly load)
                4) Class Routine (published Sun-Thu timetable slots, rooms, teachers, times)

                Strict rules:
                - Ground EVERY book, teacher, donor, phone number, and class slot strictly in the provided CAMPUS_DATA JSON. Never invent or guess missing people, rooms, or numbers.
                - Reply in Bengali when the user writes in Bengali or Banglish; otherwise reply in crisp English.
                - Keep answerText brief (1-2 sentences) and put structured details into declarative A2UI blocks:
                  * HEADER_BANNER for topic header
                  * STAT_ROW for quick counts/metrics (2-3 stats max)
                  * ENTITY_LIST for books, blood donors, or teachers (populate `phone` verbatim if present in CAMPUS_DATA)
                  * SCHEDULE_LIST for class routine slots (`badge` = e.g. "Sun P1", `secondary` = timeRange, `primary` = subject, `subtitle`/`detail` via secondary/message; use `secondary` for teacher & room if concise, or format clearly)
                  * KEY_VALUE_CARD for key attribute pairs
                  * CALLOUT for empty matches, scope boundaries, or helpful tips.
                - Never include follow-up question suggestions.
                """.trimIndent()
            )
        },
    )
}

actual suspend fun askZedWithGemini(
    prompt: String,
    snapshot: ZedContextSnapshot,
    todayEnglishDay: String,
): ZedReply {
    val fallback by lazy { ZedAiEngine.evaluate(prompt, snapshot, todayEnglishDay) }
    val trimmed = prompt.trim()
    if (trimmed.isBlank()) return fallback

    return try {
        val contextJson = buildFocusedContextJson(trimmed, snapshot, todayEnglishDay)
        val fullPrompt = "USER_QUERY: $trimmed\n\nCAMPUS_DATA:\n$contextJson"
        val response = withTimeout(GEMINI_TIMEOUT_MS) {
            zedGeminiModel.generateContent(fullPrompt)
        }
        val rawJson = response.text
        if (rawJson.isNullOrBlank()) {
            throw IllegalStateException("Empty response from Gemini.")
        }
        parseZedA2UiResponse(rawJson)
    } catch (e: TimeoutCancellationException) {
        Log.e(ZED_AI_TAG, "Gemini AI request timed out after ${GEMINI_TIMEOUT_MS / 1000}s", e)
        withCloudWarning(
            fallback,
            "Gemini cloud request timed out after ${GEMINI_TIMEOUT_MS / 1000}s. Showing verified local campus records.",
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(ZED_AI_TAG, "Firebase AI Logic generation failed", e)
        val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: "Cloud AI unavailable"
        withCloudWarning(
            fallback,
            "Cloud AI error ($detail). Showing verified local campus records.",
        )
    }
}

private fun withCloudWarning(base: ZedReply, notice: String): ZedReply {
    return base.copy(
        blocks = base.blocks + ZedA2UiBlock.CalloutCard(message = notice, isWarning = true),
    )
}

private fun parseZedA2UiResponse(jsonString: String): ZedReply {
    val root = JSONObject(jsonString)
    val answerText = root.optString("answerText").trim()
    val sourceBadge = root.optString("sourceBadge", "Zed AI").ifBlank { "Zed AI" }
    val isSupported = root.optBoolean("isSupportedDomain", true)
    val blocksArr = root.optJSONArray("blocks") ?: JSONArray()

    val blocks = mutableListOf<ZedA2UiBlock>()
    for (i in 0 until blocksArr.length()) {
        val obj = blocksArr.optJSONObject(i) ?: continue
        val type = obj.optString("blockType").trim().uppercase()
        val title = obj.optString("title").trim()
        val subtitle = obj.optString("subtitle").trim()
        val domain = obj.optString("domain", "GENERAL").trim().uppercase()
        val message = obj.optString("message").trim()
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
                    val lbl = it.optString("primary").trim()
                    val valStr = it.optString("secondary").trim()
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
                    val k = it.optString("primary").trim()
                    val v = it.optString("secondary").trim()
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
                    val prim = it.optString("primary").trim()
                    val sec = it.optString("secondary").trim()
                    val badge = it.optString("badge").trim()
                    val phone = it.optString("phone").trim().takeIf { p -> p.isNotBlank() && !p.equals("null", true) }
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
                    val subj = it.optString("primary").trim()
                    val detail = it.optString("secondary").trim()
                    val dayPeriod = it.optString("badge").trim().ifBlank { "Slot" }
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
        answerText = answerText.ifBlank { "Here is the summary from MPI Klass records." },
        sourceBadge = sourceBadge,
        isSupportedDomain = isSupported,
        blocks = blocks,
    )
}

private fun buildFocusedContextJson(
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
        .take(35)

    val teachers = snapshot.teachers
        .sortedByDescending { t ->
            tokens.count { tok ->
                t.name.lowercase().contains(tok) || t.department.lowercase() == tok
            }
        }
        .take(35)

    val donors = snapshot.donors
        .sortedByDescending { d ->
            tokens.count { tok ->
                d.bloodGroup.lowercase() == tok ||
                    d.department.lowercase() == tok ||
                    d.name.lowercase().contains(tok)
            }
        }
        .take(35)

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
                    e.subjectCode.lowercase().contains(tok)
            }
            score
        }
        .take(45)

    return JSONObject().apply {
        put("today", todayEnglishDay)
        put("user", JSONObject().apply {
            put("name", snapshot.userDisplayName)
            put("role", snapshot.userRole?.name ?: "GUEST")
            put("department", snapshot.userProfile?.department ?: "")
            put("semester", snapshot.userProfile?.semester ?: "")
            put("shift", snapshot.userProfile?.shift ?: "")
        })
        put("totals", JSONObject().apply {
            put("books", snapshot.books.size)
            put("teachers", snapshot.teachers.size)
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
                put(JSONObject().apply {
                    put("name", t.name)
                    put("department", t.department)
                    put("shift", t.shift)
                    put("phone", t.phone)
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
