package com.mpi.klass

private fun routineCacheKey(key: String): String = "klass_routine_cache_v1_$key"

private fun cacheRoutineString(key: String, value: String) {
    jsStorageSet(routineCacheKey(key), value)
}

private fun readRoutineCacheString(key: String): String? =
    jsStorageGet(routineCacheKey(key))

// ─── JSON Serialization Helpers ──────────────────────────────────────────────

private fun teacherPrefToJObj(pref: TeacherSchedulingPref): JValue.JObj = jObj {
    put("email", pref.email.trim().lowercase())
    put("name", pref.name)
    put("department", pref.department)
    put("shift", pref.shift)
    put("time_preference", pref.timePreference.key)
    put("scheduling_setup_complete", pref.schedulingSetupComplete)
    put("updated_at", pref.updatedAt)
    put(
        "semester_subjects",
        jObj {
            for ((sem, codes) in pref.semesterSubjects) {
                put(sem, jStrArr(codes))
            }
        }
    )
}

private fun parseSemesterSubjectsObj(semObj: JValue.JObj?): Map<String, List<String>> {
    if (semObj == null) return emptyMap()
    val semMap = linkedMapOf<String, List<String>>()
    for ((semKey, rawArr) in semObj.entries) {
        val codes = rawArr.asArr()?.items
            ?.mapNotNull { it.asStr().trim().takeIf { c -> c.isNotBlank() } }
            ?: emptyList()
        if (codes.isNotEmpty()) semMap[semKey.trim()] = codes
    }
    return semMap
}

private fun teacherPrefFromJObj(idFallback: String, obj: JValue.JObj): TeacherSchedulingPref {
    val email = obj.optStr("email").ifBlank { idFallback }.trim().lowercase()
    val semMap = parseSemesterSubjectsObj(obj.optObj("semester_subjects"))
    val setupDone = obj.optBool("scheduling_setup_complete", semMap.isNotEmpty())
    return TeacherSchedulingPref(
        email = email,
        name = obj.optStr("name").trim(),
        department = obj.optStr("department").trim(),
        shift = obj.optStr("shift").trim(),
        timePreference = TeacherTimePreference.fromRaw(obj.optStr("time_preference")),
        semesterSubjects = semMap,
        schedulingSetupComplete = setupDone,
        updatedAt = obj.optLong("updated_at", 0L),
    )
}

private fun classroomToJObj(room: ClassroomItem): JValue.JObj {
    val effectiveDepts = room.effectiveNearestDepartments()
    return jObj {
        put("id", room.id)
        put("room_number", room.roomNumber)
        put("building_or_floor", room.buildingOrFloor)
        put("room_type", room.roomType.key)
        put("department", room.department)
        put("nearest_department", effectiveDepts.joinToString(", "))
        put("nearest_departments", jStrArr(effectiveDepts))
        put("shifts", jStrArr(room.shifts))
        put("capacity", room.capacity)
        put("updated_at", room.updatedAt)
        if (room.labSpecialty.isNotBlank()) put("lab_specialty", room.labSpecialty)
    }
}

private fun classroomFromJObj(idFallback: String, obj: JValue.JObj): ClassroomItem {
    val id = obj.optStr("id").ifBlank { idFallback }.trim()
    val shifts = obj.optArr("shifts")?.items
        ?.mapNotNull { it.asStr().trim().takeIf { s -> s.isNotBlank() } }
        ?.ifEmpty { listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2) }
        ?: listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)

    val nearDepts = mutableListOf<String>()
    obj.optArr("nearest_departments")?.items?.forEach { item ->
        val d = RoutineRules.normalizeDepartmentShort(item.asStr())
        if (d.isNotBlank() && d != "ALL" && d !in nearDepts) nearDepts.add(d)
    }
    val legacyNear = obj.optStr("nearest_department")
    if (nearDepts.isEmpty() && legacyNear.isNotBlank()) {
        legacyNear.split(",", "/").forEach { part ->
            val d = RoutineRules.normalizeDepartmentShort(part)
            if (d.isNotBlank() && d != "ALL" && d !in nearDepts) nearDepts.add(d)
        }
    }

    return ClassroomItem(
        id = id,
        roomNumber = obj.optStr("room_number").trim(),
        buildingOrFloor = obj.optStr("building_or_floor").trim(),
        roomType = ClassroomType.fromRaw(obj.optStr("room_type")),
        department = obj.optStr("department", "ALL").trim().ifBlank { "ALL" },
        nearestDepartment = nearDepts.joinToString(", "),
        nearestDepartments = nearDepts,
        shifts = shifts,
        capacity = obj.optInt("capacity", 40),
        updatedAt = obj.optLong("updated_at", 0L),
        labSpecialty = obj.optStr("lab_specialty").trim(),
    )
}

private fun routinePlanToJObj(plan: RoutinePlan): JValue.JObj = jObj {
    put("id", plan.id)
    put("status", plan.status)
    put("generated_at", plan.generatedAt)
    put("published_at", plan.publishedAt)
    put("generated_by", plan.generatedBy)
    put(
        "config",
        jObj {
            put("shift1_start", plan.config.shift1StartTime)
            put("shift2_start", plan.config.shift2StartTime)
            put("period_duration_min", plan.config.periodDurationMinutes)
            put("periods_per_day", plan.config.periodsPerDay)
            put("valid_months", plan.config.validMonths)
            put("practical_block_mode", plan.config.practicalBlockMode)
            put("active_semesters", jStrArr(plan.config.activeSemesters))
        }
    )
    put(
        "entries",
        jObjArr(
            plan.entries.map { e ->
                jObj {
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
                }
            }
        )
    )
    put(
        "conflicts",
        jObjArr(
            plan.conflicts.map { c ->
                jObj {
                    put("id", c.id)
                    put("severity", c.severity)
                    put("department", c.department)
                    put("semester", c.semester)
                    put("shift", c.shift)
                    put("group", c.group)
                    put("subject_code", c.subjectCode)
                    put("subject_title", c.subjectTitle)
                    put("reason", c.reason)
                }
            }
        )
    )
}

private fun routinePlanFromJObj(obj: JValue.JObj): RoutinePlan {
    val cfgObj = obj.optObj("config")
    val sems = cfgObj.optArr("active_semesters")?.items
        ?.mapNotNull { it.asStr().trim().takeIf { s -> s.isNotBlank() } }
        ?.ifEmpty { RoutineRules.ALL_SEMESTER_KEYS }
        ?: RoutineRules.ALL_SEMESTER_KEYS

    val config = RoutineConfig(
        shift1StartTime = cfgObj.optStr("shift1_start", "08:00").ifBlank { "08:00" },
        shift2StartTime = cfgObj.optStr("shift2_start", "13:30").ifBlank { "13:30" },
        periodDurationMinutes = cfgObj.optInt("period_duration_min", 45),
        periodsPerDay = cfgObj.optInt("periods_per_day", 7),
        activeSemesters = sems,
        validMonths = cfgObj.optInt("valid_months", 6),
        practicalBlockMode = cfgObj.optStr("practical_block_mode", "AUTO_2_3").ifBlank { "AUTO_2_3" },
    )

    val entries = obj.optArr("entries")?.items?.mapNotNull { item ->
        val eo = item.asObj() ?: return@mapNotNull null
        RoutineEntry(
            id = eo.optStr("id"),
            day = eo.optStr("day"),
            periodIndex = eo.optInt("period_index", 0),
            startTime = eo.optStr("start_time"),
            endTime = eo.optStr("end_time"),
            shift = eo.optStr("shift"),
            department = eo.optStr("department"),
            semester = eo.optStr("semester"),
            group = eo.optStr("group"),
            subjectCode = eo.optStr("subject_code"),
            subjectTitle = eo.optStr("subject_title"),
            isPractical = eo.optBool("is_practical", false),
            teacherEmail = eo.optStr("teacher_email"),
            teacherName = eo.optStr("teacher_name"),
            roomId = eo.optStr("room_id"),
            roomNumber = eo.optStr("room_number"),
            isManualOverride = eo.optBool("is_manual_override", false),
        )
    } ?: emptyList()

    val conflicts = obj.optArr("conflicts")?.items?.mapNotNull { item ->
        val co = item.asObj() ?: return@mapNotNull null
        RoutineConflict(
            id = co.optStr("id"),
            severity = co.optStr("severity", "WARNING"),
            department = co.optStr("department"),
            semester = co.optStr("semester"),
            shift = co.optStr("shift"),
            group = co.optStr("group"),
            subjectCode = co.optStr("subject_code"),
            subjectTitle = co.optStr("subject_title"),
            reason = co.optStr("reason"),
        )
    } ?: emptyList()

    return RoutinePlan(
        id = obj.optStr("id", "master_6m").ifBlank { "master_6m" },
        status = obj.optStr("status", "DRAFT").ifBlank { "DRAFT" },
        config = config,
        entries = entries,
        conflicts = conflicts,
        generatedAt = obj.optLong("generated_at", 0L),
        publishedAt = obj.optLong("published_at", 0L),
        generatedBy = obj.optStr("generated_by"),
    )
}

// ─── 1. Teacher Scheduling Preferences ───────────────────────────────────────

actual suspend fun saveTeacherSchedulingPrefInFirestore(pref: TeacherSchedulingPref): Result<Unit> {
    val cleanEmail = pref.email.trim().lowercase()
    if (cleanEmail.isBlank()) return Result.failure(Exception("Invalid email"))
    val now = jsNowMillis().toLong()
    val updated = pref.copy(email = cleanEmail, updatedAt = now, schedulingSetupComplete = true)
    val payload = teacherPrefToJObj(updated)
    cacheRoutineString("teacher_pref_$cleanEmail", WebJson.stringify(payload))
    return WebFirebaseBridge.setDoc("teachers", cleanEmail, payload, merge = true, writeAllDbs = true)
}

actual suspend fun fetchTeacherSchedulingPrefFromFirestore(email: String): TeacherSchedulingPref? {
    val cleanEmail = email.trim().lowercase()
    if (cleanEmail.isBlank()) return null

    val localCached = readRoutineCacheString("teacher_pref_$cleanEmail")?.let { raw ->
        WebJson.parseObj(raw)?.let { teacherPrefFromJObj(cleanEmail, it) }
    }

    val remoteDoc = WebFirebaseBridge.getDoc("teachers", cleanEmail, tryAllDbs = true)
    val remote = remoteDoc?.let { d ->
        teacherPrefFromJObj(cleanEmail, d).also { pref ->
            cacheRoutineString("teacher_pref_$cleanEmail", WebJson.stringify(teacherPrefToJObj(pref)))
        }
    }

    return if (remote != null && (localCached == null || remote.updatedAt >= localCached.updatedAt)) {
        remote
    } else {
        localCached ?: remote
    }
}

actual suspend fun fetchAllTeacherSchedulingPrefsFromFirestore(): List<TeacherSchedulingPref> {
    val (_, docs) = WebFirebaseBridge.getCollection("teachers", mergeAllDbs = true)
    val map = linkedMapOf<String, TeacherSchedulingPref>()
    for (doc in docs) {
        val incoming = teacherPrefFromJObj(doc.id, doc.data)
        if (incoming.email.isBlank()) continue
        val fixed = if (incoming.name.isBlank()) incoming.copy(name = incoming.email.substringBefore("@")) else incoming
        val prev = map[fixed.email]
        if (prev == null || fixed.updatedAt >= prev.updatedAt) {
            map[fixed.email] = fixed
        }
    }
    return map.values.sortedBy { it.name.lowercase() }
}

// ─── 2. Classrooms & Labs CRUD ───────────────────────────────────────────────

private fun readLocalClassroomsCache(): List<ClassroomItem> {
    val raw = readRoutineCacheString("classrooms_json") ?: return emptyList()
    val arr = WebJson.parseArr(raw) ?: return emptyList()
    return arr.items.mapNotNull { item ->
        item.asObj()?.let { classroomFromJObj("", it) }
    }
}

private fun writeLocalClassroomsCache(items: List<ClassroomItem>) {
    cacheRoutineString("classrooms_json", WebJson.stringify(jObjArr(items.map(::classroomToJObj))))
}

actual suspend fun fetchClassroomsFromFirestore(): List<ClassroomItem> {
    val (fetchedAny, docs) = WebFirebaseBridge.getCollection("classrooms", mergeAllDbs = true)
    if (!fetchedAny) return readLocalClassroomsCache()

    val map = linkedMapOf<String, ClassroomItem>()
    for (doc in docs) {
        val incoming = classroomFromJObj(doc.id, doc.data)
        if (incoming.id.isBlank()) continue
        val prev = map[incoming.id]
        if (prev == null || incoming.updatedAt >= prev.updatedAt) {
            map[incoming.id] = incoming
        }
    }
    val sorted = map.values.sortedWith(
        compareBy<ClassroomItem> { it.roomType.key }.thenBy { it.roomNumber.lowercase() }
    )
    writeLocalClassroomsCache(sorted)
    return sorted
}

actual suspend fun saveClassroomInFirestore(room: ClassroomItem): Result<Unit> {
    val now = jsNowMillis().toLong()
    val safeRoom = room.roomNumber.trim().replace(Regex("[^A-Za-z0-9_-]"), "")
    val roomId = room.id.trim().ifBlank { "room_${now}_${safeRoom.ifBlank { (100..999).random().toString() }}" }
    val effectiveNear = room.effectiveNearestDepartments()
    val cleanRoom = room.copy(
        id = roomId,
        roomNumber = room.roomNumber.trim(),
        buildingOrFloor = room.buildingOrFloor.trim(),
        department = room.department.ifBlank { "ALL" },
        nearestDepartment = effectiveNear.joinToString(", "),
        nearestDepartments = effectiveNear,
        updatedAt = now,
    )

    val list = readLocalClassroomsCache().toMutableList()
    val idx = list.indexOfFirst { it.id == roomId }
    if (idx >= 0) list[idx] = cleanRoom else list.add(0, cleanRoom)
    writeLocalClassroomsCache(list)

    return WebFirebaseBridge.setDoc("classrooms", roomId, classroomToJObj(cleanRoom), merge = true, writeAllDbs = true)
}

actual suspend fun deleteClassroomFromFirestore(roomId: String): Result<Unit> {
    val cleanId = roomId.trim()
    if (cleanId.isBlank()) return Result.failure(Exception("Invalid room id"))
    writeLocalClassroomsCache(readLocalClassroomsCache().filterNot { it.id == cleanId })
    return WebFirebaseBridge.deleteDoc("classrooms", cleanId, writeAllDbs = true)
}

actual suspend fun deleteAllClassroomsFromFirestore(): Result<Unit> {
    writeLocalClassroomsCache(emptyList())
    return WebFirebaseBridge.deleteCollection("classrooms", writeAllDbs = true)
}

// ─── 3. Routine Plan Save / Fetch ────────────────────────────────────────────

actual suspend fun saveRoutinePlanInFirestore(plan: RoutinePlan): Result<Unit> {
    val jsonStr = WebJson.stringify(routinePlanToJObj(plan))
    cacheRoutineString("routine_plan_${plan.id}", jsonStr)
    if (plan.status == "PUBLISHED") {
        cacheRoutineString("routine_plan_master_6m_published", jsonStr)
    }

    val payload = jObj {
        put("id", plan.id)
        put("status", plan.status)
        put("generated_at", plan.generatedAt)
        put("published_at", plan.publishedAt)
        put("generated_by", plan.generatedBy)
        put("payload_json", jsonStr)
        put("entries_count", plan.entries.size)
        put("conflicts_count", plan.conflicts.size)
    }

    val res = WebFirebaseBridge.setDoc("routines", plan.id, payload, merge = true, writeAllDbs = true)
    if (plan.status == "PUBLISHED") {
        val pubPayload = jObj {
            payload.entries.forEach { (k, v) -> put(k, v) }
            put("id", "master_6m_published")
        }
        WebFirebaseBridge.setDoc("routines", "master_6m_published", pubPayload, merge = true, writeAllDbs = true)
    }
    return res
}

actual suspend fun fetchRoutinePlanFromFirestore(docId: String): RoutinePlan? {
    val cached = readRoutineCacheString("routine_plan_$docId")?.let { raw ->
        WebJson.parseObj(raw)?.let(::routinePlanFromJObj)
    }
    if (!WebFirebaseBridge.isSignedIn() && docId != "master_6m_published") {
        return cached
    }

    val remoteDoc = WebFirebaseBridge.getDoc("routines", docId, tryAllDbs = true)
    val jsonStr = remoteDoc?.optStr("payload_json")
    val parsed = jsonStr?.let { WebJson.parseObj(it)?.let(::routinePlanFromJObj) }
    if (parsed != null) {
        cacheRoutineString("routine_plan_$docId", WebJson.stringify(routinePlanToJObj(parsed)))
        return parsed
    }
    return cached
}

// ─── 4. Teacher Manual Edit Requests ─────────────────────────────────────────

actual suspend fun submitRoutineEditRequestInFirestore(request: RoutineEditRequest): Result<Unit> {
    val now = jsNowMillis().toLong()
    val reqId = request.id.trim().ifBlank { "req_${now}_${(100..999).random()}" }
    val clean = request.copy(id = reqId, createdAt = if (request.createdAt > 0L) request.createdAt else now)
    val data = jObj {
        put("id", clean.id)
        put("requested_by_email", clean.requestedByEmail.trim().lowercase())
        put("requested_by_name", clean.requestedByName)
        put("target_entry_id", clean.targetEntryId)
        put("department", clean.department)
        put("semester", clean.semester)
        put("shift", clean.shift)
        put("group", clean.group)
        put("subject_code", clean.subjectCode)
        put("subject_title", clean.subjectTitle)
        put("original_day", clean.originalDay)
        put("original_period", clean.originalPeriod)
        put("original_room_number", clean.originalRoomNumber)
        put("requested_day", clean.requestedDay)
        put("requested_period", clean.requestedPeriod)
        put("requested_room_id", clean.requestedRoomId)
        put("requested_room_number", clean.requestedRoomNumber)
        put("reason", clean.reason)
        put("status", clean.status.key)
        put("admin_note", clean.adminNote)
        put("created_at", clean.createdAt)
        put("resolved_at", clean.resolvedAt)
    }
    return WebFirebaseBridge.setDoc("routine_edit_requests", reqId, data, merge = true, writeAllDbs = true)
}

actual suspend fun fetchRoutineEditRequestsFromFirestore(): List<RoutineEditRequest> {
    if (!WebFirebaseBridge.isSignedIn()) return emptyList()
    val (_, docs) = WebFirebaseBridge.getCollection("routine_edit_requests", mergeAllDbs = true)
    val map = linkedMapOf<String, RoutineEditRequest>()
    for (doc in docs) {
        val d = doc.data
        val id = d.optStr("id").ifBlank { doc.id }.trim()
        val incoming = RoutineEditRequest(
            id = id,
            requestedByEmail = d.optStr("requested_by_email").trim(),
            requestedByName = d.optStr("requested_by_name").trim(),
            targetEntryId = d.optStr("target_entry_id").trim(),
            department = d.optStr("department").trim(),
            semester = d.optStr("semester").trim(),
            shift = d.optStr("shift").trim(),
            group = d.optStr("group").trim(),
            subjectCode = d.optStr("subject_code").trim(),
            subjectTitle = d.optStr("subject_title").trim(),
            originalDay = d.optStr("original_day").trim(),
            originalPeriod = d.optInt("original_period", 0),
            originalRoomNumber = d.optStr("original_room_number").trim(),
            requestedDay = d.optStr("requested_day").trim(),
            requestedPeriod = d.optInt("requested_period", 0),
            requestedRoomId = d.optStr("requested_room_id").trim(),
            requestedRoomNumber = d.optStr("requested_room_number").trim(),
            reason = d.optStr("reason").trim(),
            status = EditRequestStatus.fromRaw(d.optStr("status")),
            adminNote = d.optStr("admin_note").trim(),
            createdAt = d.optLong("created_at", 0L),
            resolvedAt = d.optLong("resolved_at", 0L),
        )
        val prev = map[id]
        if (prev == null || incoming.resolvedAt >= prev.resolvedAt) {
            map[id] = incoming
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
    val patch = jObj {
        put("status", newStatus.key)
        put("admin_note", adminNote.trim())
        put("resolved_at", jsNowMillis().toLong())
    }
    return WebFirebaseBridge.setDoc("routine_edit_requests", cleanId, patch, merge = true, writeAllDbs = true)
}

// ─── 5. Blood Donors Aggregator ──────────────────────────────────────────────

private fun readLocalDonorsCache(): List<BloodDonorItem> {
    val raw = readRoutineCacheString("blood_donors_json") ?: return emptyList()
    val arr = WebJson.parseArr(raw) ?: return emptyList()
    return arr.items.mapNotNull { item ->
        val o = item.asObj() ?: return@mapNotNull null
        BloodDonorItem(
            id = o.optStr("id"),
            name = o.optStr("name"),
            bloodGroup = o.optStr("blood_group"),
            phone = o.optStr("phone"),
            roleLabel = o.optStr("role_label"),
            department = o.optStr("department"),
            address = o.optStr("address"),
            email = o.optStr("email"),
        )
    }
}

private fun writeLocalDonorsCache(items: List<BloodDonorItem>) {
    val arr = jObjArr(items.map { d ->
        jObj {
            put("id", d.id)
            put("name", d.name)
            put("blood_group", d.bloodGroup)
            put("phone", d.phone)
            put("role_label", d.roleLabel)
            put("department", d.department)
            put("address", d.address)
            put("email", d.email)
        }
    })
    cacheRoutineString("blood_donors_json", WebJson.stringify(arr))
}

actual suspend fun fetchBloodDonorsFromFirestore(): List<BloodDonorItem> {
    if (!WebFirebaseBridge.isSignedIn()) return readLocalDonorsCache()
    val donorMap = linkedMapOf<String, BloodDonorItem>()
    var fetchedAny = false

    for (collectionName in listOf("students", "teachers", "admins")) {
        val (ok, docs) = WebFirebaseBridge.getCollection(collectionName, mergeAllDbs = true)
        if (!ok) continue
        fetchedAny = true
        for (doc in docs) {
            val d = doc.data
            val bg = d.optStr("blood_group").trim().uppercase()
            if (bg.isBlank()) continue
            val email = d.optStr("email").ifBlank { doc.id }.trim().lowercase()
            val name = d.optStr("name").trim().ifBlank { email.substringBefore("@") }
            val phone = d.optStr("phone").trim()
            val rawRole = d.optStr("role").trim().uppercase().ifBlank {
                when (collectionName) {
                    "teachers" -> "TEACHER"
                    "admins" -> "ADMIN"
                    else -> "STUDENT"
                }
            }
            val roleLabel = when (rawRole) {
                "ADMIN" -> "Admin"
                "TEACHER" -> "Teacher"
                else -> "Student"
            }
            val dept = RoutineRules.normalizeDepartmentShort(d.optStr("department"))
            val addr = d.optStr("address").trim()
            val existing = donorMap[email]
            donorMap[email] = BloodDonorItem(
                id = email,
                name = name.ifBlank { existing?.name ?: "" },
                bloodGroup = bg,
                phone = phone.ifBlank { existing?.phone ?: "" },
                roleLabel = if (rawRole != "STUDENT") roleLabel else (existing?.roleLabel ?: roleLabel),
                department = dept.ifBlank { existing?.department ?: "" },
                address = addr.ifBlank { existing?.address ?: "" },
                email = email,
            )
        }
    }

    return if (fetchedAny) {
        val list = donorMap.values.sortedWith(
            compareBy<BloodDonorItem> { it.bloodGroup }.thenBy { it.name.lowercase() }
        )
        writeLocalDonorsCache(list)
        list
    } else {
        readLocalDonorsCache()
    }
}

// ─── 6. Zed AI Verified Knowledge Base ───────────────────────────────────────

private fun readLocalZedKnowledgeCache(): List<ZedKnowledgeEntry> {
    val raw = readRoutineCacheString("zed_knowledge_v1_json") ?: return emptyList()
    val arr = WebJson.parseArr(raw) ?: return emptyList()
    return arr.items.mapNotNull { item ->
        val o = item.asObj() ?: return@mapNotNull null
        ZedKnowledgeEntry(
            id = o.optStr("id"),
            topic = o.optStr("topic"),
            fact = o.optStr("fact"),
            domain = o.optStr("domain", "GENERAL"),
            authorRole = o.optStr("author_role", "USER"),
            verified = o.optBool("verified", true),
            createdAt = o.optLong("created_at", 0L),
        )
    }
}

private fun writeLocalZedKnowledgeCache(items: List<ZedKnowledgeEntry>) {
    val arr = jObjArr(items.take(200).map { k ->
        jObj {
            put("id", k.id)
            put("topic", k.topic)
            put("fact", k.fact)
            put("domain", k.domain)
            put("author_role", k.authorRole)
            put("verified", k.verified)
            put("created_at", k.createdAt)
        }
    })
    cacheRoutineString("zed_knowledge_v1_json", WebJson.stringify(arr))
}

actual suspend fun fetchZedKnowledgeFromFirestore(): List<ZedKnowledgeEntry> {
    val cached = readLocalZedKnowledgeCache()
    val (fetchedAny, docs) = WebFirebaseBridge.getCollection("zed_knowledge", mergeAllDbs = true)
    if (!fetchedAny) return cached

    val remoteMap = linkedMapOf<String, ZedKnowledgeEntry>()
    for (doc in docs) {
        val d = doc.data
        val fact = d.optStr("fact").trim()
        val topic = d.optStr("topic").trim()
        if (fact.isBlank() || topic.isBlank()) continue
        val entry = ZedKnowledgeEntry(
            id = d.optStr("id").ifBlank { doc.id },
            topic = topic,
            fact = fact,
            domain = d.optStr("domain", "GENERAL").trim().uppercase().ifBlank { "GENERAL" },
            authorRole = d.optStr("author_role", "USER").trim().ifBlank { "USER" },
            verified = d.optBool("verified", true),
            createdAt = d.optLong("created_at", 0L),
        )
        if (entry.verified) remoteMap[entry.id] = entry
    }

    val merged = (cached.associateBy { it.id } + remoteMap).values.sortedByDescending { it.createdAt }
    writeLocalZedKnowledgeCache(merged)
    return merged
}

actual suspend fun saveZedKnowledgeInFirestore(entry: ZedKnowledgeEntry): Result<Unit> {
    val now = if (entry.createdAt > 0L) entry.createdAt else jsNowMillis().toLong()
    val cleanId = entry.id.ifBlank { "zk_${now}_${(1000..9999).random()}" }.take(150)
    val allowedDomain = when (entry.domain.trim().uppercase()) {
        "BOOKS", "ROUTINE", "TEACHERS", "CLASSROOMS", "BLOOD", "GENERAL" -> entry.domain.trim().uppercase()
        else -> "GENERAL"
    }
    val normalized = entry.copy(
        id = cleanId,
        topic = entry.topic.trim().take(115),
        fact = entry.fact.trim().take(480),
        domain = allowedDomain,
        verified = true,
        createdAt = now,
    )

    val local = readLocalZedKnowledgeCache().toMutableList()
    if (local.none { it.fact.equals(normalized.fact, ignoreCase = true) }) {
        local.add(0, normalized)
        writeLocalZedKnowledgeCache(local)
    }

    val uid = WebFirebaseBridge.currentUid()
    if (uid.isNullOrBlank()) {
        return Result.success(Unit)
    }

    val payload = jObj {
        put("id", normalized.id)
        put("topic", normalized.topic)
        put("fact", normalized.fact)
        put("domain", normalized.domain)
        put("author_uid", uid)
        put("author_role", normalized.authorRole.take(30))
        put("verified", true)
        put("created_at", now)
    }
    return WebFirebaseBridge.setDoc("zed_knowledge", cleanId, payload, merge = false, writeAllDbs = true)
}
