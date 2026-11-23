package com.mpi.klass

import kotlin.math.abs

object RoutineGeneratorEngine {

    private data class GroupSubjectKey(
        val department: String,
        val shift: String,
        val semester: String,
        val group: String,
        val subjectCode: String,
    )

    private data class SlotOccupancy(
        val teacherBusy: MutableSet<String> = mutableSetOf(), // key: shift|day|period|teacherEmail
        val roomBusy: MutableSet<String> = mutableSetOf(),    // key: shift|day|period|roomKey
        val groupBusy: MutableSet<String> = mutableSetOf(),   // key: shift|dept|sem|group|day|period
    ) {
        fun teacherKey(shift: String, day: String, period: Int, email: String) =
            "${shift.lowercase()}|$day|$period|${email.lowercase()}"

        fun roomKey(shift: String, day: String, period: Int, roomNumber: String) =
            "${shift.lowercase()}|$day|$period|${roomNumber.trim().uppercase()}"

        fun groupKey(shift: String, dept: String, sem: String, group: String, day: String, period: Int) =
            "${shift.lowercase()}|$dept|$sem|$group|$day|$period"

        fun isFree(
            shift: String,
            day: String,
            period: Int,
            teacherEmail: String,
            roomNumber: String,
            dept: String,
            sem: String,
            group: String,
        ): Boolean {
            if (groupKey(shift, dept, sem, group, day, period) in groupBusy) return false
            if (roomKey(shift, day, period, roomNumber) in roomBusy) return false
            if (teacherEmail.isNotBlank() && teacherKey(shift, day, period, teacherEmail) in teacherBusy) return false
            return true
        }

        fun reserve(
            shift: String,
            day: String,
            period: Int,
            teacherEmail: String,
            roomNumber: String,
            dept: String,
            sem: String,
            group: String,
        ) {
            groupBusy.add(groupKey(shift, dept, sem, group, day, period))
            roomBusy.add(roomKey(shift, day, period, roomNumber))
            if (teacherEmail.isNotBlank()) {
                teacherBusy.add(teacherKey(shift, day, period, teacherEmail))
            }
        }
    }

    /**
     * Generates a conflict-free 6-month (weekly repeating Sun-Thu) master routine.
     *
     * Rules enforced:
     * 1. Zero teacher overlap per (shift, day, period).
     * 2. Zero room/lab overlap per (shift, day, period).
     * 3. Single-Teacher-per-Group-Subject Lock: the same teacher owns ALL weekly theory & practical classes
     *    for a given (department, shift, semester, group, subjectCode).
     * 4. CT & ET have 3 groups (A, B, C); CST, MT, PT, ENT, EMT have 2 groups (A, B).
     * 5. Labs placed in LAB rooms (preferring 2-3 contiguous periods), Theory in THEORY rooms across 5 days.
     */
    fun generateRoutine(
        config: RoutineConfig,
        books: List<LibraryBook>,
        teachers: List<TeacherSchedulingPref>,
        classrooms: List<ClassroomItem>,
        generatedBy: String,
        nowMillis: Long,
        campusMap: CampusMapConfig = CampusMapConfig(),
    ): RoutinePlan {
        val periodsPerDay = config.periodsPerDay.coerceIn(4, 10)
        val activeSemesters = config.activeSemesters
            .mapNotNull { RoutineRules.canonicalSemesterKey(it).takeIf { k -> k.isNotBlank() } }
            .distinct()
            .ifEmpty { RoutineRules.ALL_SEMESTER_KEYS }

        val entries = mutableListOf<RoutineEntry>()
        val conflicts = mutableListOf<RoutineConflict>()
        val occupancy = SlotOccupancy()

        // Track assigned load per teacher for balanced distribution
        val teacherLoad = mutableMapOf<String, Int>()
        teachers.forEach { teacherLoad[it.email.lowercase()] = 0 }

        // Lock single teacher per (dept, shift, sem, group, subjectCode)
        val lockedTeacherMap = mutableMapOf<GroupSubjectKey, TeacherSchedulingPref?>()

        val shifts = listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)
        val days = RoutineRules.WORKING_DAYS

        // Build all scheduling work units sorted so harder-to-place labs and single-choice subjects go first
        data class SchedulingDemand(
            val department: String,
            val shift: String,
            val semester: String,
            val group: String,
            val book: LibraryBook,
            val candidateCount: Int,
        )

        val demands = mutableListOf<SchedulingDemand>()
        for (dept in RoutineRules.TECH_DEPARTMENTS) {
            val groups = RoutineRules.groupsForDepartment(dept)
            for (sem in activeSemesters) {
                val semBooks = books.filter { book ->
                    book.departmentSemesters.any { (bookDept, bookSems) ->
                        RoutineRules.normalizeDepartmentShort(bookDept) == dept &&
                            bookSems.any { RoutineRules.canonicalSemesterKey(it) == sem }
                    }
                }
                for (shift in shifts) {
                    for (group in groups) {
                        for (book in semBooks) {
                            if (book.theoryClassesPerWeek <= 0 && book.practicalClassesPerWeek <= 0) continue
                            val candidates = findMatchingTeachers(
                                teachers = teachers,
                                dept = dept,
                                shift = shift,
                                semester = sem,
                                subjectCode = book.subjectCode,
                            )
                            demands.add(
                                SchedulingDemand(
                                    department = dept,
                                    shift = shift,
                                    semester = sem,
                                    group = group,
                                    book = book,
                                    candidateCount = candidates.size,
                                )
                            )
                        }
                    }
                }
            }
        }

        // Prioritize fewest teacher candidates, then practical classes, then higher credit/hours
        val sortedDemands = demands.sortedWith(
            compareBy<SchedulingDemand> { if (it.candidateCount == 0) Int.MAX_VALUE else it.candidateCount }
                .thenByDescending { it.book.practicalClassesPerWeek }
                .thenByDescending { it.book.theoryClassesPerWeek }
                .thenBy { it.department }
                .thenBy { RoutineRules.semesterNumber(it.semester) ?: 9 }
        )

        var entrySeq = 1
        var conflictSeq = 1

        // 1. Lock one teacher per Group+Subject and schedule Practical (Lab) blocks first
        for (demand in sortedDemands) {
            val key = GroupSubjectKey(
                department = demand.department,
                shift = demand.shift,
                semester = demand.semester,
                group = demand.group,
                subjectCode = demand.book.subjectCode,
            )
            val chosenTeacher = lockedTeacherMap.getOrPut(key) {
                pickBestTeacher(
                    teachers = teachers,
                    teacherLoad = teacherLoad,
                    dept = demand.department,
                    shift = demand.shift,
                    semester = demand.semester,
                    subjectCode = demand.book.subjectCode,
                )
            }

            if (chosenTeacher == null) {
                conflicts.add(
                    RoutineConflict(
                        id = "conf_${conflictSeq++}",
                        severity = "ERROR",
                        department = demand.department,
                        semester = demand.semester,
                        shift = demand.shift,
                        group = demand.group,
                        subjectCode = demand.book.subjectCode,
                        subjectTitle = demand.book.title,
                        reason = "No teacher has selected ${demand.book.subjectCode} (${demand.book.title}) for ${RoutineRules.semesterDisplayLabel(demand.semester)} in ${demand.shift}.",
                    )
                )
                continue
            }

            // Reserve expected load lightly to spread subjects evenly before placement
            val teacherEmail = chosenTeacher.email.lowercase()
            teacherLoad[teacherEmail] = (teacherLoad[teacherEmail] ?: 0) +
                demand.book.practicalClassesPerWeek + demand.book.theoryClassesPerWeek
        }

        // Helper to order periods by teacher time preference
        fun orderedPeriodsFor(pref: TeacherSchedulingPref): List<Int> {
            val all = (0 until periodsPerDay).toList()
            return when (pref.timePreference) {
                TeacherTimePreference.EARLY -> all
                TeacherTimePreference.LATE -> all.reversed()
                TeacherTimePreference.ANY -> {
                    val mid = periodsPerDay / 2
                    all.sortedBy { abs(it - mid) }
                }
            }
        }

        // Helper to match all free compatible classrooms ranked by shortest walking distance from CampusMapConfig
        fun matchingRooms(dept: String, shift: String, isLab: Boolean): List<ClassroomItem> {
            val targetType = if (isLab) ClassroomType.LAB else ClassroomType.THEORY
            val matching = classrooms.filter { room ->
                room.roomType == targetType &&
                    (room.shifts.isEmpty() || room.shifts.any { RoutineRules.normalizeShift(it) == shift })
            }
            return matching.sortedWith(
                compareBy<ClassroomItem> { room ->
                    val roomZone = room.nearestDepartment.ifBlank { room.department }
                    campusMap.distanceBetween(dept, roomZone)
                }.thenBy { room ->
                    val roomZone = RoutineRules.normalizeDepartmentShort(
                        room.nearestDepartment.ifBlank { room.department }
                    )
                    if (roomZone == RoutineRules.normalizeDepartmentShort(dept)) 0 else 1
                }.thenBy { it.roomNumber.lowercase() }
            )
        }

        // Phase A: Place Practical Lab blocks (contiguous 2 or 3 period blocks)
        val groupDayHasPractical = mutableSetOf<String>() // shift|dept|sem|group|day
        for (demand in sortedDemands) {
            val practicalHours = demand.book.practicalClassesPerWeek
            if (practicalHours <= 0) continue

            val key = GroupSubjectKey(demand.department, demand.shift, demand.semester, demand.group, demand.book.subjectCode)
            val teacher = lockedTeacherMap[key] ?: continue
            val labRooms = matchingRooms(demand.department, demand.shift, isLab = true)

            if (labRooms.isEmpty()) {
                conflicts.add(
                    RoutineConflict(
                        id = "conf_${conflictSeq++}",
                        severity = "ERROR",
                        department = demand.department,
                        semester = demand.semester,
                        shift = demand.shift,
                        group = demand.group,
                        subjectCode = demand.book.subjectCode,
                        subjectTitle = demand.book.title,
                        reason = "No LAB room configured for ${demand.department} (${demand.shift}). Cannot place $practicalHours practical periods.",
                    )
                )
                continue
            }

            // Split practicalHours into contiguous blocks of 2 or 3 periods (e.g., 6 -> 3 + 3, 4 -> 2 + 2, 3 -> 3, 2 -> 2)
            val blockSizes = splitPracticalIntoBlocks(practicalHours)
            var unplacedHours = 0

            val dayOrder = days.indices.sortedBy {
                // Rotate start day across depts & groups so labs balance across the 5 days
                (it + demand.department.hashCode() + demand.group.hashCode()).let { v -> ((v % 5) + 5) % 5 }
            }

            for (blockSize in blockSizes) {
                var placed = false
                val effectiveBlock = blockSize.coerceAtMost(periodsPerDay)
                val startCandidates = when (teacher.timePreference) {
                    TeacherTimePreference.LATE -> ((periodsPerDay - effectiveBlock) downTo 0).toList()
                    else -> (0..(periodsPerDay - effectiveBlock)).toList()
                }

                // First pass: prefer days without another practical for the same group
                for (avoidSameDayPractical in listOf(true, false)) {
                    if (placed) break
                    for (dayIdx in dayOrder) {
                        if (placed) break
                        val day = days[dayIdx]
                        val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$day"
                        if (avoidSameDayPractical && gdKey in groupDayHasPractical) continue

                        for (startP in startCandidates) {
                            if (placed) break
                            for (room in labRooms) {
                                val allFree = (0 until effectiveBlock).all { offset ->
                                    occupancy.isFree(
                                        shift = demand.shift,
                                        day = day,
                                        period = startP + offset,
                                        teacherEmail = teacher.email,
                                        roomNumber = room.roomNumber,
                                        dept = demand.department,
                                        sem = demand.semester,
                                        group = demand.group,
                                    )
                                }
                                if (allFree) {
                                    for (offset in 0 until effectiveBlock) {
                                        val p = startP + offset
                                        occupancy.reserve(
                                            shift = demand.shift,
                                            day = day,
                                            period = p,
                                            teacherEmail = teacher.email,
                                            roomNumber = room.roomNumber,
                                            dept = demand.department,
                                            sem = demand.semester,
                                            group = demand.group,
                                        )
                                        val (st, et) = RoutineRules.periodTimeRange(demand.shift, p, config)
                                        entries.add(
                                            RoutineEntry(
                                                id = "ent_${entrySeq++}",
                                                day = day,
                                                periodIndex = p,
                                                startTime = st,
                                                endTime = et,
                                                shift = demand.shift,
                                                department = demand.department,
                                                semester = demand.semester,
                                                group = demand.group,
                                                subjectCode = demand.book.subjectCode,
                                                subjectTitle = demand.book.title,
                                                isPractical = true,
                                                teacherEmail = teacher.email,
                                                teacherName = teacher.name,
                                                roomId = room.id,
                                                roomNumber = room.roomNumber,
                                            )
                                        )
                                    }
                                    groupDayHasPractical.add(gdKey)
                                    placed = true
                                    break
                                }
                            }
                        }
                    }
                }
                if (!placed) {
                    unplacedHours += effectiveBlock
                }
            }

            if (unplacedHours > 0) {
                conflicts.add(
                    RoutineConflict(
                        id = "conf_${conflictSeq++}",
                        severity = "WARNING",
                        department = demand.department,
                        semester = demand.semester,
                        shift = demand.shift,
                        group = demand.group,
                        subjectCode = demand.book.subjectCode,
                        subjectTitle = demand.book.title,
                        reason = "Could not place $unplacedHours practical period(s) due to Lab room or Teacher (${teacher.name}) overlap.",
                    )
                )
            }
        }

        // Phase B: Place Theory classes spread evenly across Sunday-Thursday
        val subjectOnDaySet = mutableSetOf<String>() // shift|dept|sem|group|subjectCode|day
        for (demand in sortedDemands) {
            val theoryHours = demand.book.theoryClassesPerWeek
            if (theoryHours <= 0) continue

            val key = GroupSubjectKey(demand.department, demand.shift, demand.semester, demand.group, demand.book.subjectCode)
            val teacher = lockedTeacherMap[key] ?: continue
            val theoryRooms = matchingRooms(demand.department, demand.shift, isLab = false)

            if (theoryRooms.isEmpty()) {
                conflicts.add(
                    RoutineConflict(
                        id = "conf_${conflictSeq++}",
                        severity = "ERROR",
                        department = demand.department,
                        semester = demand.semester,
                        shift = demand.shift,
                        group = demand.group,
                        subjectCode = demand.book.subjectCode,
                        subjectTitle = demand.book.title,
                        reason = "No THEORY room configured for ${demand.department} (${demand.shift}). Cannot place $theoryHours theory classes.",
                    )
                )
                continue
            }

            val periodOrder = orderedPeriodsFor(teacher)
            var unplacedTheory = 0

            for (lectureNum in 0 until theoryHours) {
                var placed = false
                // Distribute evenly across the 5 days
                val startDayIdx = ((lectureNum * 2) + abs(demand.book.subjectCode.hashCode())) % days.size
                val rotatedDays = (days.indices).map { days[(startDayIdx + it) % days.size] }

                // First pass avoids placing the same theory subject twice on the same day
                for (avoidDuplicateDay in listOf(true, false)) {
                    if (placed) break
                    for (day in rotatedDays) {
                        if (placed) break
                        val sdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|${demand.book.subjectCode}|$day"
                        if (avoidDuplicateDay && sdKey in subjectOnDaySet) continue

                        for (p in periodOrder) {
                            if (placed) break
                            for (room in theoryRooms) {
                                if (occupancy.isFree(
                                        shift = demand.shift,
                                        day = day,
                                        period = p,
                                        teacherEmail = teacher.email,
                                        roomNumber = room.roomNumber,
                                        dept = demand.department,
                                        sem = demand.semester,
                                        group = demand.group,
                                    )
                                ) {
                                    occupancy.reserve(
                                        shift = demand.shift,
                                        day = day,
                                        period = p,
                                        teacherEmail = teacher.email,
                                        roomNumber = room.roomNumber,
                                        dept = demand.department,
                                        sem = demand.semester,
                                        group = demand.group,
                                    )
                                    val (st, et) = RoutineRules.periodTimeRange(demand.shift, p, config)
                                    entries.add(
                                        RoutineEntry(
                                            id = "ent_${entrySeq++}",
                                            day = day,
                                            periodIndex = p,
                                            startTime = st,
                                            endTime = et,
                                            shift = demand.shift,
                                            department = demand.department,
                                            semester = demand.semester,
                                            group = demand.group,
                                            subjectCode = demand.book.subjectCode,
                                            subjectTitle = demand.book.title,
                                            isPractical = false,
                                            teacherEmail = teacher.email,
                                            teacherName = teacher.name,
                                            roomId = room.id,
                                            roomNumber = room.roomNumber,
                                        )
                                    )
                                    subjectOnDaySet.add(sdKey)
                                    placed = true
                                    break
                                }
                            }
                        }
                    }
                }
                if (!placed) unplacedTheory++
            }

            if (unplacedTheory > 0) {
                conflicts.add(
                    RoutineConflict(
                        id = "conf_${conflictSeq++}",
                        severity = "WARNING",
                        department = demand.department,
                        semester = demand.semester,
                        shift = demand.shift,
                        group = demand.group,
                        subjectCode = demand.book.subjectCode,
                        subjectTitle = demand.book.title,
                        reason = "Could not fit $unplacedTheory theory period(s) due to Theory room or Teacher (${teacher.name}) clash.",
                    )
                )
            }
        }

        val sortedEntries = entries.sortedWith(
            compareBy<RoutineEntry> { RoutineRules.dayOrderIndex(it.day) }
                .thenBy { it.periodIndex }
                .thenBy { it.department }
                .thenBy { RoutineRules.semesterNumber(it.semester) ?: 9 }
                .thenBy { it.group }
        )

        return RoutinePlan(
            id = "master_6m",
            status = "DRAFT",
            config = config,
            entries = sortedEntries,
            conflicts = conflicts,
            generatedAt = nowMillis,
            publishedAt = 0L,
            generatedBy = generatedBy,
        )
    }

    /**
     * Checks live whether editing or moving a slot to a new day, period, room, or teacher introduces any clash.
     */
    fun validateSlotEdit(
        allEntries: List<RoutineEntry>,
        editingEntryId: String,
        targetShift: String,
        targetDay: String,
        targetPeriod: Int,
        targetDepartment: String,
        targetSemester: String,
        targetGroup: String,
        targetTeacherEmail: String,
        targetRoomNumber: String,
    ): List<String> {
        val clashes = mutableListOf<String>()
        val cleanTeacher = targetTeacherEmail.trim().lowercase()
        val cleanRoom = targetRoomNumber.trim().uppercase()
        val cleanShift = RoutineRules.normalizeShift(targetShift)
        val cleanSem = RoutineRules.canonicalSemesterKey(targetSemester).ifBlank { targetSemester }
        val cleanGroup = RoutineRules.normalizeGroup(targetDepartment, targetGroup)

        for (other in allEntries) {
            if (other.id == editingEntryId) continue
            if (RoutineRules.normalizeShift(other.shift) != cleanShift) continue
            if (!other.day.equals(targetDay, ignoreCase = true)) continue
            if (other.periodIndex != targetPeriod) continue

            if (cleanTeacher.isNotBlank() && other.teacherEmail.trim().lowercase() == cleanTeacher) {
                clashes.add(
                    "Teacher overlap: ${other.teacherName} is already teaching ${other.subjectCode} in ${other.department} (${RoutineRules.semesterShortLabel(other.semester)} ${other.group})."
                )
            }
            if (cleanRoom.isNotBlank() && other.roomNumber.trim().uppercase() == cleanRoom) {
                clashes.add(
                    "Room overlap: Room $cleanRoom is occupied by ${other.department} (${RoutineRules.semesterShortLabel(other.semester)} ${other.group}, ${other.subjectCode})."
                )
            }
            val otherSem = RoutineRules.canonicalSemesterKey(other.semester).ifBlank { other.semester }
            val otherGroup = RoutineRules.normalizeGroup(other.department, other.group)
            if (other.department == targetDepartment && otherSem == cleanSem && otherGroup == cleanGroup) {
                clashes.add(
                    "Group overlap: $targetDepartment ${RoutineRules.semesterShortLabel(cleanSem)} $cleanGroup already has ${other.subjectTitle} in period ${targetPeriod + 1}."
                )
            }
        }
        return clashes.distinct()
    }

    private fun splitPracticalIntoBlocks(hours: Int): List<Int> {
        val result = mutableListOf<Int>()
        var rem = hours
        while (rem > 0) {
            when {
                rem == 3 -> {
                    result.add(3)
                    rem -= 3
                }
                rem == 4 -> {
                    result.add(2)
                    result.add(2)
                    rem = 0
                }
                rem >= 5 -> {
                    result.add(3)
                    rem -= 3
                }
                rem == 2 -> {
                    result.add(2)
                    rem = 0
                }
                else -> {
                    result.add(1)
                    rem = 0
                }
            }
        }
        return result
    }

    private fun findMatchingTeachers(
        teachers: List<TeacherSchedulingPref>,
        dept: String,
        shift: String,
        semester: String,
        subjectCode: String,
    ): List<TeacherSchedulingPref> {
        val cleanCode = subjectCode.trim()
        val cleanSem = RoutineRules.canonicalSemesterKey(semester)
        return teachers.filter { teacher ->
            val selectedForSem = teacher.semesterSubjects.entries.firstOrNull { (k, _) ->
                RoutineRules.canonicalSemesterKey(k) == cleanSem
            }?.value ?: emptyList()

            val handlesSubject = selectedForSem.any { it.trim().equals(cleanCode, ignoreCase = true) }
            if (!handlesSubject) return@filter false

            val teacherShift = if (teacher.shift.isBlank()) "" else RoutineRules.normalizeShift(teacher.shift)
            val shiftOk = teacherShift.isBlank() || teacherShift == shift
            shiftOk
        }
    }

    private fun pickBestTeacher(
        teachers: List<TeacherSchedulingPref>,
        teacherLoad: Map<String, Int>,
        dept: String,
        shift: String,
        semester: String,
        subjectCode: String,
    ): TeacherSchedulingPref? {
        val candidates = findMatchingTeachers(teachers, dept, shift, semester, subjectCode)
        if (candidates.isEmpty()) return null
        return candidates.minByOrNull { t ->
            val tDept = RoutineRules.normalizeDepartmentShort(t.department)
            val deptPenalty = when {
                tDept == dept -> 0
                tDept == "Non-Tech" -> 2
                else -> 5
            }
            val load = teacherLoad[t.email.lowercase()] ?: 0
            deptPenalty * 100 + load
        }
    }
}
