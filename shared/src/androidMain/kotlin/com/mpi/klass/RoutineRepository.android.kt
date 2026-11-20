package com.mpi.klass

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mpi.klass.theme.getAppContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

private const val REPO_TAG = "RoutineRepo"
private const val ROUTINE_CACHE_PREFS = "klass_routine_cache_v1"

private fun getRoutineCustomDbId(): String? {
    val ctx = getAppContext() ?: return null
    val resId = ctx.resources.getIdentifier("firestore_database_id", "string", ctx.packageName)
    if (resId == 0) return null
    val dbName = ctx.getString(resId).trim()
    return if (dbName.isEmpty() || dbName == "(default)") null else dbName
}

private fun getRepoFirestoreInstances(): List<Pair<FirebaseFirestore, String>> {
    val list = mutableListOf<Pair<FirebaseFirestore, String>>()
    val customDbId = getRoutineCustomDbId()
    if (customDbId != null) {
        try {
            list.add(FirebaseFirestore.getInstance(customDbId) to customDbId)
            return list
        } catch (e: Exception) {
            Log.w(REPO_TAG, "Custom DB unavailable ($customDbId): ${e.message}")
        }
    }
    try {
        list.add(FirebaseFirestore.getInstance() to "(default)")
    } catch (e: Exception) {
        Log.w(REPO_TAG, "Default DB unavailable: ${e.message}")
    }
    return list
}

private fun isFirebaseSignedIn(): Boolean = try {
    FirebaseAuth.getInstance().currentUser != null
} catch (_: Exception) {
    false
}

private fun cacheString(key: String, value: String) {
    getAppContext()?.getSharedPreferences(ROUTINE_CACHE_PREFS, Context.MODE_PRIVATE)
        ?.edit()?.putString(key, value)?.apply()
}

private fun readCacheString(key: String): String? =
    getAppContext()?.getSharedPreferences(ROUTINE_CACHE_PREFS, Context.MODE_PRIVATE)
        ?.getString(key, null)

// ─── JSON Serialization Helpers ──────────────────────────────────────────────

private fun teacherPrefToJson(pref: TeacherSchedulingPref): JSONObject = JSONObject().apply {
    put("email", pref.email.trim().lowercase())
    put("name", pref.name)
    put("department", pref.department)
    put("shift", pref.shift)
    put("time_preference", pref.timePreference.key)
    put("scheduling_setup_complete", pref.schedulingSetupComplete)
    put("updated_at", pref.updatedAt)
    val semObj = JSONObject()
    for ((sem, codes) in pref.semesterSubjects) {
        val arr = JSONArray()
        codes.forEach { arr.put(it) }
        semObj.put(sem, arr)
    }
    put("semester_subjects", semObj)
}

private fun teacherPrefFromJson(obj: JSONObject): TeacherSchedulingPref {
    val semMap = linkedMapOf<String, List<String>>()
    val semObj = obj.optJSONObject("semester_subjects")
    if (semObj != null) {
        val keys = semObj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val arr = semObj.optJSONArray(k) ?: continue
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val c = arr.optString(i).trim()
                if (c.isNotBlank()) list.add(c)
            }
            if (list.isNotEmpty()) semMap[k] = list
        }
    }
    return TeacherSchedulingPref(
        email = obj.optString("email").trim().lowercase(),
        name = obj.optString("name"),
        department = obj.optString("department"),
        shift = obj.optString("shift"),
        timePreference = TeacherTimePreference.fromRaw(obj.optString("time_preference")),
        semesterSubjects = semMap,
        schedulingSetupComplete = obj.optBoolean("scheduling_setup_complete", false),
        updatedAt = obj.optLong("updated_at", 0L),
    )
}

private fun classroomToJson(room: ClassroomItem): JSONObject = JSONObject().apply {
    put("id", room.id)
    put("room_number", room.roomNumber)
    put("building_or_floor", room.buildingOrFloor)
    put("room_type", room.roomType.key)
    put("department", room.department)
    put("capacity", room.capacity)
    put("updated_at", room.updatedAt)
    val sArr = JSONArray()
    room.shifts.forEach { sArr.put(it) }
    put("shifts", sArr)
}

private fun classroomFromJson(obj: JSONObject): ClassroomItem {
    val shifts = mutableListOf<String>()
    val sArr = obj.optJSONArray("shifts")
    if (sArr != null) {
        for (i in 0 until sArr.length()) {
            val s = sArr.optString(i).trim()
            if (s.isNotBlank()) shifts.add(s)
        }
    }
    return ClassroomItem(
        id = obj.optString("id"),
        roomNumber = obj.optString("room_number"),
        buildingOrFloor = obj.optString("building_or_floor"),
        roomType = ClassroomType.fromRaw(obj.optString("room_type")),
        department = obj.optString("department", "ALL").ifBlank { "ALL" },
        shifts = shifts.ifEmpty { listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2) },
        capacity = obj.optInt("capacity", 40),
        updatedAt = obj.optLong("updated_at", 0L),
    )
}

private fun routinePlanToJson(plan: RoutinePlan): JSONObject = JSONObject().apply {
    put("id", plan.id)
    put("status", plan.status)
    put("generated_at", plan.generatedAt)
    put("published_at", plan.publishedAt)
    put("generated_by", plan.generatedBy)
    put("config", JSONObject().apply {
        put("shift1_start", plan.config.shift1StartTime)
        put("shift2_start", plan.config.shift2StartTime)
        put("period_duration_min", plan.config.periodDurationMinutes)
        put("periods_per_day", plan.config.periodsPerDay)
        put("valid_months", plan.config.validMonths)
        val semArr = JSONArray()
        plan.config.activeSemesters.forEach { semArr.put(it) }
        put("active_semesters", semArr)
    })
    val eArr = JSONArray()
    plan.entries.forEach { e ->
        eArr.put(JSONObject().apply {
            put("id", e.id)
            put("day", e.day)
            put("period_index", e.periodIndex)
            put("start_time", e.startTime)
            put("end_time", e.endTime)
            put("shift", e.shift)
            put("department", e.department)
            put("semester", e.semester)
            put("group", e.group)
            put("subject_code", e.subjectCode)
            put("subject_title", e.subjectTitle)
            put("is_practical", e.isPractical)
            put("teacher_email", e.teacherEmail)
            put("teacher_name", e.teacherName)
            put("room_id", e.roomId)
            put("room_number", e.roomNumber)
            put("is_manual_override", e.isManualOverride)
        })
    }
    put("entries", eArr)
    val cArr = JSONArray()
    plan.conflicts.forEach { c ->
        cArr.put(JSONObject().apply {
            put("id", c.id)
            put("severity", c.severity)
            put("department", c.department)
            put("semester", c.semester)
            put("shift", c.shift)
            put("group", c.group)
            put("subject_code", c.subjectCode)
            put("subject_title", c.subjectTitle)
            put("reason", c.reason)
        })
    }
    put("conflicts", cArr)
}

private fun routinePlanFromJson(obj: JSONObject): RoutinePlan {
    val cfgObj = obj.optJSONObject("config") ?: JSONObject()
    val semArr = cfgObj.optJSONArray("active_semesters")
    val sems = mutableListOf<String>()
    if (semArr != null) {
        for (i in 0 until semArr.length()) {
            val s = semArr.optString(i).trim()
            if (s.isNotBlank()) sems.add(s)
        }
    }
    val config = RoutineConfig(
        shift1StartTime = cfgObj.optString("shift1_start", "08:00").ifBlank { "08:00" },
        shift2StartTime = cfgObj.optString("shift2_start", "13:30").ifBlank { "13:30" },
        periodDurationMinutes = cfgObj.optInt("period_duration_min", 45),
        periodsPerDay = cfgObj.optInt("periods_per_day", 7),
        activeSemesters = sems.ifEmpty { RoutineRules.ALL_SEMESTER_KEYS },
        validMonths = cfgObj.optInt("valid_months", 6),
    )

    val entries = mutableListOf<RoutineEntry>()
    val eArr = obj.optJSONArray("entries")
    if (eArr != null) {
        for (i in 0 until eArr.length()) {
            val eo = eArr.optJSONObject(i) ?: continue
            entries.add(
                RoutineEntry(
                    id = eo.optString("id"),
                    day = eo.optString("day"),
                    periodIndex = eo.optInt("period_index", 0),
                    startTime = eo.optString("start_time"),
                    endTime = eo.optString("end_time"),
                    shift = eo.optString("shift"),
                    department = eo.optString("department"),
                    semester = eo.optString("semester"),
                    group = eo.optString("group"),
                    subjectCode = eo.optString("subject_code"),
                    subjectTitle = eo.optString("subject_title"),
                    isPractical = eo.optBoolean("is_practical", false),
                    teacherEmail = eo.optString("teacher_email"),
                    teacherName = eo.optString("teacher_name"),
                    roomId = eo.optString("room_id"),
                    roomNumber = eo.optString("room_number"),
                    isManualOverride = eo.optBoolean("is_manual_override", false),
                )
            )
        }
    }

    val conflicts = mutableListOf<RoutineConflict>()
    val cArr = obj.optJSONArray("conflicts")
    if (cArr != null) {
        for (i in 0 until cArr.length()) {
            val co = cArr.optJSONObject(i) ?: continue
            conflicts.add(
                RoutineConflict(
                    id = co.optString("id"),
                    severity = co.optString("severity", "WARNING"),
                    department = co.optString("department"),
                    semester = co.optString("semester"),
                    shift = co.optString("shift"),
                    group = co.optString("group"),
                    subjectCode = co.optString("subject_code"),
                    subjectTitle = co.optString("subject_title"),
                    reason = co.optString("reason"),
                )
            )
        }
    }

    return RoutinePlan(
        id = obj.optString("id", "master_6m"),
        status = obj.optString("status", "DRAFT"),
        config = config,
        entries = entries,
        conflicts = conflicts,
        generatedAt = obj.optLong("generated_at", 0L),
        publishedAt = obj.optLong("published_at", 0L),
        generatedBy = obj.optString("generated_by"),
    )
}

// ─── 1. Teacher Scheduling Preferences ───────────────────────────────────────

actual suspend fun saveTeacherSchedulingPrefInFirestore(pref: TeacherSchedulingPref): Result<Unit> {
    val cleanEmail = pref.email.trim().lowercase()
    if (cleanEmail.isBlank()) return Result.failure(Exception("Invalid email"))
    val now = System.currentTimeMillis()
    val updated = pref.copy(email = cleanEmail, updatedAt = now, schedulingSetupComplete = true)

    cacheString("teacher_pref_$cleanEmail", teacherPrefToJson(updated).toString())

    val data = mapOf(
        "email" to cleanEmail,
        "name" to updated.name,
        "department" to updated.department,
        "shift" to updated.shift,
        "time_preference" to updated.timePreference.key,
        "semester_subjects" to updated.semesterSubjects,
        "scheduling_setup_complete" to true,
        "updated_at" to now,
    )

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(8.seconds) {
        for ((db, label) in getRepoFirestoreInstances()) {
            try {
                db.collection("teachers").document(cleanEmail).set(data, SetOptions.merge()).awaitTask()
                ok = true
            } catch (e: Exception) {
                err = e
                Log.w(REPO_TAG, "[$label] Failed saving teacher pref: ${e.message}")
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed to save teacher preferences"))
}

actual suspend fun fetchTeacherSchedulingPrefFromFirestore(email: String): TeacherSchedulingPref? {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return null

    val localCached = readCacheString("teacher_pref_$cleanEmail")?.let { raw ->
        try { teacherPrefFromJson(JSONObject(raw)) } catch (_: Exception) { null }
    }

    val remote = withTimeoutOrNull(7.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                val doc = db.collection("teachers").document(cleanEmail).get().awaitTask()
                if (doc.exists()) {
                    val data = doc.data ?: continue
                    val semMap = parseSemesterSubjects(data["semester_subjects"])
                    val setupDone = (data["scheduling_setup_complete"] as? Boolean) ?: semMap.isNotEmpty()
                    val pref = TeacherSchedulingPref(
                        email = cleanEmail,
                        name = (data["name"] as? String)?.trim() ?: "",
                        department = (data["department"] as? String)?.trim() ?: "",
                        shift = (data["shift"] as? String)?.trim() ?: "",
                        timePreference = TeacherTimePreference.fromRaw(data["time_preference"] as? String),
                        semesterSubjects = semMap,
                        schedulingSetupComplete = setupDone,
                        updatedAt = (data["updated_at"] as? Number)?.toLong() ?: 0L,
                    )
                    cacheString("teacher_pref_$cleanEmail", teacherPrefToJson(pref).toString())
                    return@withTimeoutOrNull pref
                }
            } catch (_: Exception) {}
        }
        null
    }

    return if (remote != null && (localCached == null || remote.updatedAt >= localCached.updatedAt)) {
        remote
    } else {
        localCached ?: remote
    }
}

actual suspend fun fetchAllTeacherSchedulingPrefsFromFirestore(): List<TeacherSchedulingPref> {
    val map = linkedMapOf<String, TeacherSchedulingPref>()
    withTimeoutOrNull(9.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                val snap = db.collection("teachers").get().awaitTask()
                for (doc in snap.documents) {
                    val data = doc.data ?: continue
                    val email = ((data["email"] as? String)?.ifBlank { null } ?: doc.id).trim().lowercase()
                    if (email.isBlank()) continue
                    val semMap = parseSemesterSubjects(data["semester_subjects"])
                    val setupDone = (data["scheduling_setup_complete"] as? Boolean) ?: semMap.isNotEmpty()
                    val incoming = TeacherSchedulingPref(
                        email = email,
                        name = ((data["name"] as? String)?.trim()?.ifBlank { null } ?: email.substringBefore("@")),
                        department = (data["department"] as? String)?.trim() ?: "",
                        shift = (data["shift"] as? String)?.trim() ?: "",
                        timePreference = TeacherTimePreference.fromRaw(data["time_preference"] as? String),
                        semesterSubjects = semMap,
                        schedulingSetupComplete = setupDone,
                        updatedAt = (data["updated_at"] as? Number)?.toLong() ?: 0L,
                    )
                    val prev = map[email]
                    if (prev == null || incoming.updatedAt >= prev.updatedAt) {
                        map[email] = incoming
                    }
                }
            } catch (_: Exception) {}
        }
    }
    return map.values.sortedBy { it.name.lowercase() }
}

private fun parseSemesterSubjects(raw: Any?): Map<String, List<String>> {
    val map = linkedMapOf<String, List<String>>()
    val rawMap = raw as? Map<*, *> ?: return map
    for ((k, v) in rawMap) {
        val sem = (k as? String)?.trim() ?: continue
        val rawList = v as? List<*> ?: continue
        val codes = rawList.mapNotNull { (it as? String)?.trim()?.takeIf { c -> c.isNotBlank() } }
        if (codes.isNotEmpty()) map[sem] = codes
    }
    return map
}

// ─── 2. Classrooms & Labs CRUD ───────────────────────────────────────────────

private fun readLocalClassroomsCache(): List<ClassroomItem> {
    val raw = readCacheString("classrooms_json") ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { idx ->
            arr.optJSONObject(idx)?.let { classroomFromJson(it) }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun writeLocalClassroomsCache(items: List<ClassroomItem>) {
    val arr = JSONArray()
    items.forEach { arr.put(classroomToJson(it)) }
    cacheString("classrooms_json", arr.toString())
}

actual suspend fun fetchClassroomsFromFirestore(): List<ClassroomItem> {
    if (!isFirebaseSignedIn()) return readLocalClassroomsCache()
    val map = linkedMapOf<String, ClassroomItem>()
    var anyFetched = false

    withTimeoutOrNull(8.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                val snap = db.collection("classrooms").get().awaitTask()
                anyFetched = true
                for (doc in snap.documents) {
                    val data = doc.data ?: continue
                    val id = ((data["id"] as? String)?.ifBlank { null } ?: doc.id).trim()
                    if (id.isBlank()) continue
                    val shifts = (data["shifts"] as? List<*>)
                        ?.mapNotNull { (it as? String)?.trim()?.takeIf { s -> s.isNotBlank() } }
                        ?.ifEmpty { listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2) }
                        ?: listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)
                    val incoming = ClassroomItem(
                        id = id,
                        roomNumber = (data["room_number"] as? String)?.trim() ?: "",
                        buildingOrFloor = (data["building_or_floor"] as? String)?.trim() ?: "",
                        roomType = ClassroomType.fromRaw(data["room_type"] as? String),
                        department = ((data["department"] as? String)?.trim()?.ifBlank { "ALL" } ?: "ALL"),
                        shifts = shifts,
                        capacity = (data["capacity"] as? Number)?.toInt() ?: 40,
                        updatedAt = (data["updated_at"] as? Number)?.toLong() ?: 0L,
                    )
                    val prev = map[id]
                    if (prev == null || incoming.updatedAt >= prev.updatedAt) {
                        map[id] = incoming
                    }
                }
            } catch (e: Exception) {
                Log.w(REPO_TAG, "Failed fetching classrooms: ${e.message}")
            }
        }
    }

    return if (anyFetched) {
        val sorted = map.values.sortedWith(compareBy<ClassroomItem> { it.roomType.key }.thenBy { it.roomNumber.lowercase() })
        writeLocalClassroomsCache(sorted)
        sorted
    } else {
        readLocalClassroomsCache()
    }
}

actual suspend fun saveClassroomInFirestore(room: ClassroomItem): Result<Unit> {
    val now = System.currentTimeMillis()
    val safeRoom = room.roomNumber.trim().replace(Regex("[^A-Za-z0-9_-]"), "")
    val roomId = room.id.trim().ifBlank { "room_${now}_${safeRoom.ifBlank { (100..999).random().toString() }}" }
    val cleanRoom = room.copy(
        id = roomId,
        roomNumber = room.roomNumber.trim(),
        buildingOrFloor = room.buildingOrFloor.trim(),
        department = room.department.ifBlank { "ALL" },
        updatedAt = now,
    )

    val list = readLocalClassroomsCache().toMutableList()
    val idx = list.indexOfFirst { it.id == roomId }
    if (idx >= 0) list[idx] = cleanRoom else list.add(0, cleanRoom)
    writeLocalClassroomsCache(list)

    val data = mapOf(
        "id" to cleanRoom.id,
        "room_number" to cleanRoom.roomNumber,
        "building_or_floor" to cleanRoom.buildingOrFloor,
        "room_type" to cleanRoom.roomType.key,
        "department" to cleanRoom.department,
        "shifts" to cleanRoom.shifts,
        "capacity" to cleanRoom.capacity,
        "updated_at" to cleanRoom.updatedAt,
    )

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(7.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                db.collection("classrooms").document(roomId).set(data, SetOptions.merge()).awaitTask()
                ok = true
            } catch (e: Exception) {
                err = e
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed to save classroom"))
}

actual suspend fun deleteClassroomFromFirestore(roomId: String): Result<Unit> {
    val cleanId = roomId.trim()
    if (cleanId.isBlank()) return Result.failure(Exception("Invalid room id"))
    writeLocalClassroomsCache(readLocalClassroomsCache().filterNot { it.id == cleanId })

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(7.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                db.collection("classrooms").document(cleanId).delete().awaitTask()
                ok = true
            } catch (e: Exception) {
                err = e
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed to delete classroom"))
}

actual suspend fun deleteAllClassroomsFromFirestore(): Result<Unit> {
    writeLocalClassroomsCache(emptyList())
    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(10.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                val snap = db.collection("classrooms").get().awaitTask()
                if (snap.documents.isNotEmpty()) {
                    snap.documents.chunked(450).forEach { chunk ->
                        val batch = db.batch()
                        chunk.forEach { doc -> batch.delete(doc.reference) }
                        batch.commit().awaitTask()
                    }
                }
                ok = true
            } catch (e: Exception) {
                err = e
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed deleting classrooms"))
}

// ─── 3. Routine Plan Save / Fetch ────────────────────────────────────────────

actual suspend fun saveRoutinePlanInFirestore(plan: RoutinePlan): Result<Unit> {
    val jsonStr = routinePlanToJson(plan).toString()
    cacheString("routine_plan_${plan.id}", jsonStr)
    if (plan.status == "PUBLISHED") {
        cacheString("routine_plan_master_6m_published", jsonStr)
    }

    val payload = mapOf(
        "id" to plan.id,
        "status" to plan.status,
        "generated_at" to plan.generatedAt,
        "published_at" to plan.publishedAt,
        "generated_by" to plan.generatedBy,
        "payload_json" to jsonStr,
        "entries_count" to plan.entries.size,
        "conflicts_count" to plan.conflicts.size,
    )

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(10.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                db.collection("routines").document(plan.id).set(payload, SetOptions.merge()).awaitTask()
                if (plan.status == "PUBLISHED") {
                    db.collection("routines").document("master_6m_published").set(
                        payload + ("id" to "master_6m_published"),
                        SetOptions.merge(),
                    ).awaitTask()
                }
                ok = true
            } catch (e: Exception) {
                err = e
                Log.w(REPO_TAG, "Failed saving routine plan: ${e.message}")
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed to save routine"))
}

actual suspend fun fetchRoutinePlanFromFirestore(docId: String): RoutinePlan? {
    val cached = readCacheString("routine_plan_$docId")?.let { raw ->
        try { routinePlanFromJson(JSONObject(raw)) } catch (_: Exception) { null }
    }
    if (!isFirebaseSignedIn()) return cached

    var bestRemote: RoutinePlan? = null
    withTimeoutOrNull(9.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                val doc = db.collection("routines").document(docId).get().awaitTask()
                if (doc.exists()) {
                    val jsonStr = doc.getString("payload_json") ?: continue
                    val parsed = routinePlanFromJson(JSONObject(jsonStr))
                    if (bestRemote == null || parsed.generatedAt >= (bestRemote?.generatedAt ?: 0L)) {
                        bestRemote = parsed
                    }
                }
            } catch (e: Exception) {
                Log.w(REPO_TAG, "Failed fetching routine $docId: ${e.message}")
            }
        }
    }

    if (bestRemote != null) {
        cacheString("routine_plan_$docId", routinePlanToJson(bestRemote!!).toString())
        return bestRemote
    }
    return cached
}

// ─── 4. Teacher Manual Edit Requests ─────────────────────────────────────────

actual suspend fun submitRoutineEditRequestInFirestore(request: RoutineEditRequest): Result<Unit> {
    val now = System.currentTimeMillis()
    val reqId = request.id.trim().ifBlank { "req_${now}_${(100..999).random()}" }
    val clean = request.copy(id = reqId, createdAt = if (request.createdAt > 0L) request.createdAt else now)

    val data = mapOf(
        "id" to clean.id,
        "requested_by_email" to clean.requestedByEmail.trim().lowercase(),
        "requested_by_name" to clean.requestedByName,
        "target_entry_id" to clean.targetEntryId,
        "department" to clean.department,
        "semester" to clean.semester,
        "shift" to clean.shift,
        "group" to clean.group,
        "subject_code" to clean.subjectCode,
        "subject_title" to clean.subjectTitle,
        "original_day" to clean.originalDay,
        "original_period" to clean.originalPeriod,
        "original_room_number" to clean.originalRoomNumber,
        "requested_day" to clean.requestedDay,
        "requested_period" to clean.requestedPeriod,
        "requested_room_id" to clean.requestedRoomId,
        "requested_room_number" to clean.requestedRoomNumber,
        "reason" to clean.reason,
        "status" to clean.status.key,
        "admin_note" to clean.adminNote,
        "created_at" to clean.createdAt,
        "resolved_at" to clean.resolvedAt,
    )

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(8.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                db.collection("routine_edit_requests").document(reqId).set(data, SetOptions.merge()).awaitTask()
                ok = true
            } catch (e: Exception) {
                err = e
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed to submit edit request"))
}

actual suspend fun fetchRoutineEditRequestsFromFirestore(): List<RoutineEditRequest> {
    if (!isFirebaseSignedIn()) return emptyList()
    val map = linkedMapOf<String, RoutineEditRequest>()
    withTimeoutOrNull(8.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                val snap = db.collection("routine_edit_requests").get().awaitTask()
                for (doc in snap.documents) {
                    val d = doc.data ?: continue
                    val id = ((d["id"] as? String)?.ifBlank { null } ?: doc.id).trim()
                    val incoming = RoutineEditRequest(
                        id = id,
                        requestedByEmail = (d["requested_by_email"] as? String)?.trim() ?: "",
                        requestedByName = (d["requested_by_name"] as? String)?.trim() ?: "",
                        targetEntryId = (d["target_entry_id"] as? String)?.trim() ?: "",
                        department = (d["department"] as? String)?.trim() ?: "",
                        semester = (d["semester"] as? String)?.trim() ?: "",
                        shift = (d["shift"] as? String)?.trim() ?: "",
                        group = (d["group"] as? String)?.trim() ?: "",
                        subjectCode = (d["subject_code"] as? String)?.trim() ?: "",
                        subjectTitle = (d["subject_title"] as? String)?.trim() ?: "",
                        originalDay = (d["original_day"] as? String)?.trim() ?: "",
                        originalPeriod = (d["original_period"] as? Number)?.toInt() ?: 0,
                        originalRoomNumber = (d["original_room_number"] as? String)?.trim() ?: "",
                        requestedDay = (d["requested_day"] as? String)?.trim() ?: "",
                        requestedPeriod = (d["requested_period"] as? Number)?.toInt() ?: 0,
                        requestedRoomId = (d["requested_room_id"] as? String)?.trim() ?: "",
                        requestedRoomNumber = (d["requested_room_number"] as? String)?.trim() ?: "",
                        reason = (d["reason"] as? String)?.trim() ?: "",
                        status = EditRequestStatus.fromRaw(d["status"] as? String),
                        adminNote = (d["admin_note"] as? String)?.trim() ?: "",
                        createdAt = (d["created_at"] as? Number)?.toLong() ?: 0L,
                        resolvedAt = (d["resolved_at"] as? Number)?.toLong() ?: 0L,
                    )
                    val prev = map[id]
                    if (prev == null || incoming.resolvedAt >= prev.resolvedAt) {
                        map[id] = incoming
                    }
                }
            } catch (e: Exception) {
                Log.w(REPO_TAG, "Failed fetching edit requests: ${e.message}")
            }
        }
    }
    return map.values.sortedWith(
        compareBy<RoutineEditRequest> { if (it.status == EditRequestStatus.PENDING) 0 else 1 }
            .thenByDescending { it.createdAt }
    )
}

actual suspend fun resolveRoutineEditRequestInFirestore(
    requestId: String,
    newStatus: EditRequestStatus,
    adminNote: String,
): Result<Unit> {
    val cleanId = requestId.trim()
    if (cleanId.isBlank()) return Result.failure(Exception("Invalid request id"))
    val now = System.currentTimeMillis()
    val patch = mapOf(
        "status" to newStatus.key,
        "admin_note" to adminNote.trim(),
        "resolved_at" to now,
    )

    var ok = false
    var err: Exception? = null
    withTimeoutOrNull(8.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            try {
                db.collection("routine_edit_requests").document(cleanId).set(patch, SetOptions.merge()).awaitTask()
                ok = true
            } catch (e: Exception) {
                err = e
            }
        }
    }
    return if (ok) Result.success(Unit) else Result.failure(err ?: Exception("Failed updating request"))
}

// ─── 5. Blood Donors Aggregator ──────────────────────────────────────────────

private fun readLocalDonorsCache(): List<BloodDonorItem> {
    val raw = readCacheString("blood_donors_json") ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            BloodDonorItem(
                id = o.optString("id"),
                name = o.optString("name"),
                bloodGroup = o.optString("blood_group"),
                phone = o.optString("phone"),
                roleLabel = o.optString("role_label"),
                department = o.optString("department"),
                address = o.optString("address"),
                email = o.optString("email"),
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun writeLocalDonorsCache(items: List<BloodDonorItem>) {
    val arr = JSONArray()
    items.forEach { d ->
        arr.put(JSONObject().apply {
            put("id", d.id)
            put("name", d.name)
            put("blood_group", d.bloodGroup)
            put("phone", d.phone)
            put("role_label", d.roleLabel)
            put("department", d.department)
            put("address", d.address)
            put("email", d.email)
        })
    }
    cacheString("blood_donors_json", arr.toString())
}

actual suspend fun fetchBloodDonorsFromFirestore(): List<BloodDonorItem> {
    if (!isFirebaseSignedIn()) return readLocalDonorsCache()
    val donorMap = linkedMapOf<String, BloodDonorItem>()
    var fetchedAny = false

    withTimeoutOrNull(10.seconds) {
        for ((db, _) in getRepoFirestoreInstances()) {
            for (collectionName in listOf("students", "teachers", "admins")) {
                try {
                    val snap = db.collection(collectionName).get().awaitTask()
                    fetchedAny = true
                    for (doc in snap.documents) {
                        val d = doc.data ?: continue
                        val bg = (d["blood_group"] as? String)?.trim()?.uppercase() ?: ""
                        if (bg.isBlank()) continue
                        val email = ((d["email"] as? String)?.ifBlank { null } ?: doc.id).trim().lowercase()
                        val name = (d["name"] as? String)?.trim()?.ifBlank { email.substringBefore("@") } ?: ""
                        val phone = (d["phone"] as? String)?.trim() ?: ""
                        val rawRole = (d["role"] as? String)?.trim()?.uppercase() ?: when (collectionName) {
                            "teachers" -> "TEACHER"
                            "admins" -> "ADMIN"
                            else -> "STUDENT"
                        }
                        val roleLabel = when (rawRole) {
                            "ADMIN" -> "Admin"
                            "TEACHER" -> "Teacher"
                            else -> "Student"
                        }
                        val dept = RoutineRules.normalizeDepartmentShort((d["department"] as? String) ?: "")
                        val addr = (d["address"] as? String)?.trim() ?: ""

                        val existing = donorMap[email]
                        val merged = BloodDonorItem(
                            id = email,
                            name = name.ifBlank { existing?.name ?: "" },
                            bloodGroup = bg,
                            phone = phone.ifBlank { existing?.phone ?: "" },
                            roleLabel = if (rawRole != "STUDENT") roleLabel else (existing?.roleLabel ?: roleLabel),
                            department = dept.ifBlank { existing?.department ?: "" },
                            address = addr.ifBlank { existing?.address ?: "" },
                            email = email,
                        )
                        donorMap[email] = merged
                    }
                } catch (e: Exception) {
                    Log.w(REPO_TAG, "Blood donor query failed on $collectionName: ${e.message}")
                }
            }
        }
    }

    return if (fetchedAny) {
        val list = donorMap.values.sortedWith(compareBy<BloodDonorItem> { it.bloodGroup }.thenBy { it.name.lowercase() })
        writeLocalDonorsCache(list)
        list
    } else {
        readLocalDonorsCache()
    }
}
