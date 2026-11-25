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

        fun isTeacherFree(shift: String, day: String, period: Int, email: String): Boolean =
            email.isBlank() || teacherKey(shift, day, period, email) !in teacherBusy

        fun isRoomFree(shift: String, day: String, period: Int, roomNumber: String): Boolean =
            roomKey(shift, day, period, roomNumber) !in roomBusy

        fun isGroupFree(shift: String, day: String, period: Int, dept: String, sem: String, group: String): Boolean =
            groupKey(shift, dept, sem, group, day, period) !in groupBusy

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

        fun unreserve(
            shift: String,
            day: String,
            period: Int,
            teacherEmail: String,
            roomNumber: String,
            dept: String,
            sem: String,
            group: String,
        ) {
            groupBusy.remove(groupKey(shift, dept, sem, group, day, period))
            roomBusy.remove(roomKey(shift, day, period, roomNumber))
            if (teacherEmail.isNotBlank()) {
                teacherBusy.remove(teacherKey(shift, day, period, teacherEmail))
            }
        }
    }

    private fun countPackableBlocks(freeFlags: BooleanArray, blockLen: Int): Int {
        if (blockLen <= 1) return freeFlags.count { it }
        var count = 0
        var run = 0
        for (free in freeFlags) {
            if (free) {
                run++
                if (run >= blockLen) {
                    count++
                    run = 0
                }
            } else {
                run = 0
            }
        }
        return count
    }

    private fun blockFragmentationPenalty(
        periodsPerDay: Int,
        startP: Int,
        blockLen: Int,
        isSlotFree: (Int) -> Boolean,
    ): Int {
        if (blockLen < 2) return 0
        val before = BooleanArray(periodsPerDay) { p -> isSlotFree(p) }
        val after = BooleanArray(periodsPerDay) { p -> before[p] && (p < startP || p >= startP + blockLen) }
        val beforeMax = countPackableBlocks(before, blockLen)
        val afterMax = countPackableBlocks(after, blockLen)
        val lostBlocks = (beforeMax - afterMax) - 1
        return if (lostBlocks > 0) lostBlocks * 10 else 0
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
        activeBookCodes: Set<String>? = null,
        activeTeacherEmails: Set<String>? = null,
        activeClassroomIds: Set<String>? = null,
    ): RoutinePlan {
        val periodsPerDay = config.periodsPerDay.coerceIn(4, 10)
        val activeSemesters = config.activeSemesters
            .mapNotNull { RoutineRules.canonicalSemesterKey(it).takeIf { k -> k.isNotBlank() } }
            .distinct()
            .ifEmpty { RoutineRules.ALL_SEMESTER_KEYS }

        val normBookKeys = activeBookCodes?.map { it.trim().lowercase() }?.toSet()
        val effectiveBooks = if (normBookKeys == null) {
            books
        } else {
            books.filter { book ->
                book.id.trim().lowercase() in normBookKeys ||
                    book.subjectCode.trim().lowercase() in normBookKeys
            }
        }

        val normTeacherKeys = activeTeacherEmails?.map { it.trim().lowercase() }?.toSet()
        val effectiveTeachers = if (normTeacherKeys == null) {
            teachers
        } else {
            teachers.filter { it.email.trim().lowercase() in normTeacherKeys }
        }

        val normRoomKeys = activeClassroomIds?.map { it.trim().lowercase() }?.toSet()
        val effectiveClassrooms = if (normRoomKeys == null) {
            classrooms
        } else {
            classrooms.filter { room ->
                room.id.trim().lowercase() in normRoomKeys ||
                    room.roomNumber.trim().lowercase() in normRoomKeys
            }
        }

        val entries = mutableListOf<RoutineEntry>()
        val conflicts = mutableListOf<RoutineConflict>()
        val occupancy = SlotOccupancy()

        // Track assigned load per teacher for balanced distribution
        val teacherLoad = mutableMapOf<String, Int>()
        val teacherPracticalLoad = mutableMapOf<String, Int>()
        effectiveTeachers.forEach {
            val email = it.email.lowercase()
            teacherLoad[email] = 0
            teacherPracticalLoad[email] = 0
        }

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
                val semBooks = effectiveBooks.filter { book ->
                    book.departmentSemesters.any { (bookDept, bookSems) ->
                        RoutineRules.normalizeDepartmentShort(bookDept) == dept &&
                            bookSems.any { RoutineRules.canonicalSemesterKey(it) == sem }
                    }
                }.distinctBy { it.subjectCode.trim() }
                for (shift in shifts) {
                    for (group in groups) {
                        for (book in semBooks) {
                            if (book.theoryClassesPerWeek <= 0 && book.practicalClassesPerWeek <= 0) continue
                            val candidates = findMatchingTeachers(
                                teachers = effectiveTeachers,
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

        // Prioritize fewest teacher candidates, then dedicated science/drawing labs, then practical hours
        val sortedDemands = demands.sortedWith(
            compareBy<SchedulingDemand> { if (it.candidateCount == 0) Int.MAX_VALUE else it.candidateCount }
                .thenBy {
                    when (inferSubjectLabSpecialty(it.book)) {
                        LabSpecialty.PHYSICS, LabSpecialty.CHEMISTRY -> 0
                        LabSpecialty.DRAWING -> 1
                        LabSpecialty.GENERAL -> 3
                        else -> 2
                    }
                }
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
                    teachers = effectiveTeachers,
                    teacherLoad = teacherLoad,
                    teacherPracticalLoad = teacherPracticalLoad,
                    dept = demand.department,
                    shift = demand.shift,
                    semester = demand.semester,
                    subjectCode = demand.book.subjectCode,
                    incomingPracticalHours = demand.book.practicalClassesPerWeek,
                    maxWeeklyPracticalPerTeacher = (periodsPerDay / 2) * days.size * 3,
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

            // Reserve expected load evenly across faculty before placement
            val teacherEmail = chosenTeacher.email.lowercase()
            teacherLoad[teacherEmail] = (teacherLoad[teacherEmail] ?: 0) +
                demand.book.practicalClassesPerWeek + demand.book.theoryClassesPerWeek
            teacherPracticalLoad[teacherEmail] = (teacherPracticalLoad[teacherEmail] ?: 0) +
                demand.book.practicalClassesPerWeek
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

        // Helper to match all free compatible classrooms ranked by lab specialty & nearest departments
        fun matchingRooms(book: LibraryBook, dept: String, shift: String, isLab: Boolean): List<ClassroomItem> {
            val targetType = if (isLab) ClassroomType.LAB else ClassroomType.THEORY
            val normDept = RoutineRules.normalizeDepartmentShort(dept)
            val matching = effectiveClassrooms.filter { room ->
                room.roomType == targetType &&
                    (room.shifts.isEmpty() || room.shifts.any { RoutineRules.normalizeShift(it) == shift })
            }
            if (!isLab) {
                return matching.sortedWith(
                    compareBy<ClassroomItem> { room ->
                        val nearList = room.effectiveNearestDepartments()
                        when {
                            normDept in nearList -> 0
                            nearList.isEmpty() -> 1
                            else -> 2
                        }
                    }.thenBy { room ->
                        room.effectiveNearestDepartments().size
                    }.thenBy { it.roomNumber.lowercase() }
                )
            }

            val neededSpec = inferSubjectLabSpecialty(book)
            val isCadSubject = book.title.contains("cad", ignoreCase = true)
            val hasDedicated = matching.any { it.effectiveLabSpecialty() == neededSpec }
            val nonScienceLabs = matching.filter {
                val sp = it.effectiveLabSpecialty()
                sp != LabSpecialty.PHYSICS && sp != LabSpecialty.CHEMISTRY
            }

            val candidatesPool = when {
                (neededSpec == LabSpecialty.PHYSICS || neededSpec == LabSpecialty.CHEMISTRY) && hasDedicated ->
                    matching.filter { it.effectiveLabSpecialty() == neededSpec }
                neededSpec != LabSpecialty.PHYSICS && neededSpec != LabSpecialty.CHEMISTRY && nonScienceLabs.isNotEmpty() ->
                    nonScienceLabs
                else -> matching
            }

            return candidatesPool.sortedWith(
                compareBy<ClassroomItem> { room ->
                    val rSpec = room.effectiveLabSpecialty()
                    val nearList = room.effectiveNearestDepartments()
                    val nearMatch = normDept in nearList
                    when {
                        rSpec == neededSpec && nearMatch -> 0
                        rSpec == neededSpec -> 1
                        isCadSubject && rSpec == LabSpecialty.COMPUTER && nearMatch -> 2
                        neededSpec == LabSpecialty.DRAWING && nearMatch && rSpec != LabSpecialty.GENERAL -> 2
                        neededSpec == LabSpecialty.POWER_AUTO && rSpec == LabSpecialty.MECHANICAL_WORKSHOP -> 2
                        neededSpec == LabSpecialty.MECHANICAL_WORKSHOP && rSpec == LabSpecialty.POWER_AUTO -> 2
                        neededSpec == LabSpecialty.ELECTRICAL && rSpec == LabSpecialty.ELECTRONICS -> 2
                        neededSpec == LabSpecialty.ELECTRONICS && rSpec == LabSpecialty.ELECTRICAL -> 2
                        rSpec == LabSpecialty.GENERAL && nearMatch -> 3
                        rSpec == LabSpecialty.GENERAL -> 4
                        nearMatch -> 5
                        else -> 6
                    }
                }.thenBy { room ->
                    room.effectiveNearestDepartments().size
                }.thenBy { it.roomNumber.lowercase() }
            )
        }

        // Phase A: Place Practical Lab blocks (contiguous 2 or 3 period blocks)
        val groupDayHasPractical = mutableSetOf<String>() // shift|dept|sem|group|day
        val groupDayLoad = mutableMapOf<String, Int>() // shift|dept|sem|group|day -> occupied periods
        val missingDedicatedSpecialtyWarned = mutableSetOf<String>()
        val booksByCode = effectiveBooks.associateBy { it.subjectCode.trim() }

        fun findContiguousPracticalPlacement(
            demand: SchedulingDemand,
            teacher: TeacherSchedulingPref,
            labRooms: List<ClassroomItem>,
            dayOrder: List<Int>,
            effectiveBlock: Int,
            ignoreGroupKey: GroupSubjectKey? = null,
        ): Triple<String, Int, ClassroomItem>? {
            val maxStart = (periodsPerDay - effectiveBlock).coerceAtLeast(0)
            val startCandidates = when (teacher.timePreference) {
                TeacherTimePreference.LATE -> (maxStart downTo 0).toList()
                else -> (0..maxStart).toList()
            }
            var bestScore = Int.MAX_VALUE
            var bestDay = ""
            var bestStartP = 0
            var bestRoom: ClassroomItem? = null

            for (dayOrderPos in dayOrder.indices) {
                val day = days[dayOrder[dayOrderPos]]
                val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$day"
                val sameDayPenalty = if (gdKey in groupDayHasPractical) 1 else 0
                val dayLoad = groupDayLoad[gdKey] ?: 0

                for (roomIdx in labRooms.indices) {
                    val room = labRooms[roomIdx]
                    for (startIdx in startCandidates.indices) {
                        val startP = startCandidates[startIdx]
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
                        if (!allFree) continue

                        val teacherFrag = blockFragmentationPenalty(periodsPerDay, startP, effectiveBlock) { p ->
                            occupancy.isTeacherFree(demand.shift, day, p, teacher.email)
                        }
                        val roomFrag = blockFragmentationPenalty(periodsPerDay, startP, effectiveBlock) { p ->
                            occupancy.isRoomFree(demand.shift, day, p, room.roomNumber)
                        }
                        val groupFrag = blockFragmentationPenalty(periodsPerDay, startP, effectiveBlock) { p ->
                            occupancy.isGroupFree(demand.shift, day, p, demand.department, demand.semester, demand.group)
                        }
                        val score = (roomIdx * 1000) +
                            (sameDayPenalty * 200) +
                            ((teacherFrag + roomFrag + groupFrag) * 5) +
                            (dayLoad * 4) +
                            (dayOrderPos * 2) +
                            startIdx

                        if (score < bestScore) {
                            bestScore = score
                            bestDay = day
                            bestStartP = startP
                            bestRoom = room
                            if (score == 0) break
                        }
                    }
                    if (bestScore == 0) break
                }
                if (bestScore == 0) break
            }
            return bestRoom?.let { Triple(bestDay, bestStartP, it) }
        }

        fun tryMovePracticalBlock(
            anchorIdx: Int,
            visitedIds: MutableSet<String>,
        ): Boolean {
            val anchor = entries[anchorIdx]
            if (!anchor.isPractical) return false

            val blockIndices = entries.indices.filter { idx ->
                val e = entries[idx]
                e.isPractical &&
                    e.shift == anchor.shift &&
                    e.day == anchor.day &&
                    e.department == anchor.department &&
                    e.semester == anchor.semester &&
                    e.group == anchor.group &&
                    e.subjectCode == anchor.subjectCode
            }.sortedBy { entries[it].periodIndex }
            if (blockIndices.isEmpty()) return false
            // Ensure contiguous single block (in case 2 separate blocks exist, split at gaps)
            val anchorPosInList = blockIndices.indexOf(anchorIdx)
            if (anchorPosInList < 0) return false
            var lo = anchorPosInList
            while (lo > 0 && entries[blockIndices[lo - 1]].periodIndex == entries[blockIndices[lo]].periodIndex - 1) lo--
            var hi = anchorPosInList
            while (hi < blockIndices.lastIndex && entries[blockIndices[hi + 1]].periodIndex == entries[blockIndices[hi]].periodIndex + 1) hi++
            val contiguousIndices = blockIndices.subList(lo, hi + 1)

            if (contiguousIndices.any { entries[it].id in visitedIds }) return false
            contiguousIndices.forEach { visitedIds.add(entries[it].id) }

            val blockLen = contiguousIndices.size
            val blockBook = booksByCode[anchor.subjectCode.trim()]
                ?: LibraryBook(subjectCode = anchor.subjectCode, title = anchor.subjectTitle)
            val altRooms = matchingRooms(blockBook, anchor.department, anchor.shift, isLab = true)
            if (altRooms.isEmpty()) return false

            val candidateDays = days.sortedBy { d ->
                val gdKey = "${anchor.shift}|${anchor.department}|${anchor.semester}|${anchor.group}|$d"
                val hasPrac = if (d != anchor.day && gdKey in groupDayHasPractical) 1 else 0
                (hasPrac * 20) + (groupDayLoad[gdKey] ?: 0)
            }

            // Temporarily unreserve current block to evaluate valid destination slots cleanly
            for (idx in contiguousIndices) {
                val e = entries[idx]
                occupancy.unreserve(
                    e.shift, e.day, e.periodIndex,
                    e.teacherEmail, e.roomNumber,
                    e.department, e.semester, e.group,
                )
            }

            val maxStart = (periodsPerDay - blockLen).coerceAtLeast(0)
            for (altDay in candidateDays) {
                for (altRoom in altRooms) {
                    for (startP in 0..maxStart) {
                        if (altDay == anchor.day && startP == entries[contiguousIndices.first()].periodIndex) continue
                        val canPlace = (0 until blockLen).all { off ->
                            occupancy.isFree(
                                shift = anchor.shift,
                                day = altDay,
                                period = startP + off,
                                teacherEmail = anchor.teacherEmail,
                                roomNumber = altRoom.roomNumber,
                                dept = anchor.department,
                                sem = anchor.semester,
                                group = anchor.group,
                            )
                        }
                        if (canPlace) {
                            val oldGdKey = "${anchor.shift}|${anchor.department}|${anchor.semester}|${anchor.group}|${anchor.day}"
                            val newGdKey = "${anchor.shift}|${anchor.department}|${anchor.semester}|${anchor.group}|$altDay"
                            contiguousIndices.forEachIndexed { off, idx ->
                                val oldE = entries[idx]
                                val p = startP + off
                                occupancy.reserve(
                                    anchor.shift, altDay, p,
                                    anchor.teacherEmail, altRoom.roomNumber,
                                    anchor.department, anchor.semester, anchor.group,
                                )
                                val (st, et) = RoutineRules.periodTimeRange(anchor.shift, p, config)
                                entries[idx] = oldE.copy(
                                    day = altDay,
                                    periodIndex = p,
                                    startTime = st,
                                    endTime = et,
                                    roomId = altRoom.id,
                                    roomNumber = altRoom.roomNumber,
                                )
                            }
                            groupDayLoad[oldGdKey] = ((groupDayLoad[oldGdKey] ?: blockLen) - blockLen).coerceAtLeast(0)
                            groupDayLoad[newGdKey] = (groupDayLoad[newGdKey] ?: 0) + blockLen
                            if (entries.none { it.isPractical && it.shift == anchor.shift && it.day == anchor.day && it.department == anchor.department && it.semester == anchor.semester && it.group == anchor.group }) {
                                groupDayHasPractical.remove(oldGdKey)
                            }
                            groupDayHasPractical.add(newGdKey)
                            return true
                        }
                    }
                }
            }

            // Restore original block reservation on failure
            for (idx in contiguousIndices) {
                val e = entries[idx]
                occupancy.reserve(
                    e.shift, e.day, e.periodIndex,
                    e.teacherEmail, e.roomNumber,
                    e.department, e.semester, e.group,
                )
            }
            return false
        }
        for (demand in sortedDemands) {
            val practicalHours = demand.book.practicalClassesPerWeek
            if (practicalHours <= 0) continue

            val key = GroupSubjectKey(demand.department, demand.shift, demand.semester, demand.group, demand.book.subjectCode)
            val teacher = lockedTeacherMap[key] ?: continue
            val labRooms = matchingRooms(demand.book, demand.department, demand.shift, isLab = true)
            val neededSpec = inferSubjectLabSpecialty(demand.book)

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
                        reason = "No ${neededSpec.label} or compatible LAB room configured for ${demand.department} (${demand.shift}). Cannot place $practicalHours practical periods.",
                    )
                )
                continue
            }

            if ((neededSpec == LabSpecialty.PHYSICS || neededSpec == LabSpecialty.CHEMISTRY) &&
                labRooms.none { it.effectiveLabSpecialty() == neededSpec }
            ) {
                val warnKey = "${demand.shift}|${demand.book.subjectCode}|${neededSpec.key}"
                if (missingDedicatedSpecialtyWarned.add(warnKey)) {
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
                            reason = "No dedicated ${neededSpec.label} configured for ${demand.shift}. Add a ${neededSpec.label} in Classrooms for accurate lab routing.",
                        )
                    )
                }
            }

            // Split practicalHours into contiguous blocks of 2 or 3 periods per practicalBlockMode
            val blockSizes = splitPracticalIntoBlocks(practicalHours, config.practicalBlockMode)
            val dayOrder = days.indices.sortedBy {
                // Rotate start day across depts & groups so labs balance across the 5 days
                (it + demand.department.hashCode() + demand.group.hashCode()).let { v -> ((v % 5) + 5) % 5 }
            }

            var activeTeacher = teacher
            fun placeAllBlocksForTeacher(candTeacher: TeacherSchedulingPref): Int {
                val placedBlockSnapshots = mutableListOf<Triple<Int, String, Int>>() // entryIdx, day, blockLen
                var unplaced = 0
                for (blockSize in blockSizes) {
                    val effectiveBlock = blockSize.coerceAtMost(periodsPerDay)
                    var placement = findContiguousPracticalPlacement(
                        demand = demand,
                        teacher = candTeacher,
                        labRooms = labRooms,
                        dayOrder = dayOrder,
                        effectiveBlock = effectiveBlock,
                    )

                    // 1-hop practical block displacement if blocked on all windows
                    if (placement == null) {
                        val maxStart = (periodsPerDay - effectiveBlock).coerceAtLeast(0)
                        outer@ for (dayIdx in dayOrder) {
                            val day = days[dayIdx]
                            for (room in labRooms) {
                                for (startP in 0..maxStart) {
                                    val groupFreeAll = (0 until effectiveBlock).all { off ->
                                        occupancy.isGroupFree(demand.shift, day, startP + off, demand.department, demand.semester, demand.group)
                                    }
                                    if (!groupFreeAll) continue
                                    val blockingPracticalIndices = (0 until effectiveBlock).mapNotNull { off ->
                                        val p = startP + off
                                        entries.indexOfFirst { e ->
                                            e.isPractical &&
                                                e.shift == demand.shift &&
                                                e.day == day &&
                                                e.periodIndex == p &&
                                                (e.teacherEmail.equals(candTeacher.email, ignoreCase = true) ||
                                                    e.roomNumber.equals(room.roomNumber, ignoreCase = true))
                                        }.takeIf { it >= 0 }
                                    }.distinct()
                                    if (blockingPracticalIndices.size == 1) {
                                        if (tryMovePracticalBlock(blockingPracticalIndices.first(), mutableSetOf())) {
                                            val nowFree = (0 until effectiveBlock).all { off ->
                                                occupancy.isFree(
                                                    demand.shift, day, startP + off,
                                                    candTeacher.email, room.roomNumber,
                                                    demand.department, demand.semester, demand.group,
                                                )
                                            }
                                            if (nowFree) {
                                                placement = Triple(day, startP, room)
                                                break@outer
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (placement != null) {
                        val (bestDay, bestStartP, bestRoom) = placement
                        val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$bestDay"
                        val firstIdx = entries.size
                        for (offset in 0 until effectiveBlock) {
                            val p = bestStartP + offset
                            occupancy.reserve(
                                shift = demand.shift,
                                day = bestDay,
                                period = p,
                                teacherEmail = candTeacher.email,
                                roomNumber = bestRoom.roomNumber,
                                dept = demand.department,
                                sem = demand.semester,
                                group = demand.group,
                            )
                            val (st, et) = RoutineRules.periodTimeRange(demand.shift, p, config)
                            entries.add(
                                RoutineEntry(
                                    id = "ent_${entrySeq++}",
                                    day = bestDay,
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
                                    teacherEmail = candTeacher.email,
                                    teacherName = candTeacher.name,
                                    roomId = bestRoom.id,
                                    roomNumber = bestRoom.roomNumber,
                                )
                            )
                        }
                        placedBlockSnapshots.add(Triple(firstIdx, bestDay, effectiveBlock))
                        groupDayHasPractical.add(gdKey)
                        groupDayLoad[gdKey] = (groupDayLoad[gdKey] ?: 0) + effectiveBlock
                    } else {
                        unplaced += effectiveBlock
                    }
                }
                return unplaced
            }

            val firstAttemptUnplaced = placeAllBlocksForTeacher(activeTeacher)
            var unplacedHours = firstAttemptUnplaced

            // If primary teacher failed to place any block, roll back partial placements for this key and try alternate eligible teachers
            if (unplacedHours > 0) {
                val altCandidates = findMatchingTeachers(
                    teachers = effectiveTeachers,
                    dept = demand.department,
                    shift = demand.shift,
                    semester = demand.semester,
                    subjectCode = demand.book.subjectCode,
                ).filter { !it.email.equals(activeTeacher.email, ignoreCase = true) }
                    .sortedBy { teacherLoad[it.email.lowercase()] ?: 0 }

                if (altCandidates.isNotEmpty()) {
                    // Snapshot & remove current demand's partial practical entries
                    val rollbackEntries = entries.filter {
                        it.isPractical &&
                            it.shift == demand.shift &&
                            it.department == demand.department &&
                            it.semester == demand.semester &&
                            it.group == demand.group &&
                            it.subjectCode == demand.book.subjectCode
                    }
                    fun removeRollbackEntries() {
                        entries.removeAll { e ->
                            e.isPractical &&
                                e.shift == demand.shift &&
                                e.department == demand.department &&
                                e.semester == demand.semester &&
                                e.group == demand.group &&
                                e.subjectCode == demand.book.subjectCode
                        }
                        rollbackEntries.forEach { e ->
                            occupancy.unreserve(
                                e.shift, e.day, e.periodIndex,
                                e.teacherEmail, e.roomNumber,
                                e.department, e.semester, e.group,
                            )
                            val gdKey = "${e.shift}|${e.department}|${e.semester}|${e.group}|${e.day}"
                            groupDayLoad[gdKey] = ((groupDayLoad[gdKey] ?: 1) - 1).coerceAtLeast(0)
                            if (entries.none { it.isPractical && it.shift == e.shift && it.day == e.day && it.department == e.department && it.semester == e.semester && it.group == e.group }) {
                                groupDayHasPractical.remove(gdKey)
                            }
                        }
                    }

                    removeRollbackEntries()
                    var succeeded = false
                    for (altTeacher in altCandidates) {
                        val altUnplaced = placeAllBlocksForTeacher(altTeacher)
                        if (altUnplaced == 0) {
                            val totalWeight = demand.book.practicalClassesPerWeek + demand.book.theoryClassesPerWeek
                            val oldEmail = activeTeacher.email.lowercase()
                            val newEmail = altTeacher.email.lowercase()
                            teacherLoad[oldEmail] = ((teacherLoad[oldEmail] ?: totalWeight) - totalWeight).coerceAtLeast(0)
                            teacherPracticalLoad[oldEmail] = ((teacherPracticalLoad[oldEmail] ?: practicalHours) - practicalHours).coerceAtLeast(0)
                            teacherLoad[newEmail] = (teacherLoad[newEmail] ?: 0) + totalWeight
                            teacherPracticalLoad[newEmail] = (teacherPracticalLoad[newEmail] ?: 0) + practicalHours
                            lockedTeacherMap[key] = altTeacher
                            activeTeacher = altTeacher
                            unplacedHours = 0
                            succeeded = true
                            break
                        } else {
                            // Clear failed alt attempt
                            val failedAttempt = entries.filter {
                                it.isPractical &&
                                    it.shift == demand.shift &&
                                    it.department == demand.department &&
                                    it.semester == demand.semester &&
                                    it.group == demand.group &&
                                    it.subjectCode == demand.book.subjectCode
                            }
                            entries.removeAll { e ->
                                e.isPractical &&
                                    e.shift == demand.shift &&
                                    e.department == demand.department &&
                                    e.semester == demand.semester &&
                                    e.group == demand.group &&
                                    e.subjectCode == demand.book.subjectCode
                            }
                            failedAttempt.forEach { e ->
                                occupancy.unreserve(
                                    e.shift, e.day, e.periodIndex,
                                    e.teacherEmail, e.roomNumber,
                                    e.department, e.semester, e.group,
                                )
                                val gdKey = "${e.shift}|${e.department}|${e.semester}|${e.group}|${e.day}"
                                groupDayLoad[gdKey] = ((groupDayLoad[gdKey] ?: 1) - 1).coerceAtLeast(0)
                                if (entries.none { it.isPractical && it.shift == e.shift && it.day == e.day && it.department == e.department && it.semester == e.semester && it.group == e.group }) {
                                    groupDayHasPractical.remove(gdKey)
                                }
                            }
                        }
                    }
                    if (!succeeded) {
                        // Restore best original entries
                        rollbackEntries.forEach { e ->
                            occupancy.reserve(
                                e.shift, e.day, e.periodIndex,
                                e.teacherEmail, e.roomNumber,
                                e.department, e.semester, e.group,
                            )
                            entries.add(e)
                            val gdKey = "${e.shift}|${e.department}|${e.semester}|${e.group}|${e.day}"
                            groupDayLoad[gdKey] = (groupDayLoad[gdKey] ?: 0) + 1
                            groupDayHasPractical.add(gdKey)
                        }
                    }
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
                        reason = "Could not place $unplacedHours practical period(s) due to Lab room or Teacher (${activeTeacher.name}) overlap.",
                    )
                )
            }
        }

        // Phase B: Place Theory classes spread evenly across Sunday-Thursday
        val groupTotalWeeklyDemand = mutableMapOf<String, Int>()
        sortedDemands.forEach { d ->
            val gKey = "${d.shift}|${d.department}|${d.semester}|${d.group}"
            groupTotalWeeklyDemand[gKey] = (groupTotalWeeklyDemand[gKey] ?: 0) +
                d.book.practicalClassesPerWeek + d.book.theoryClassesPerWeek
        }
        val theoryDemands = sortedDemands
            .filter { it.book.theoryClassesPerWeek > 0 }
            .sortedWith(
                compareByDescending<SchedulingDemand> {
                    groupTotalWeeklyDemand["${it.shift}|${it.department}|${it.semester}|${it.group}"] ?: 0
                }.thenByDescending { it.book.theoryClassesPerWeek }
                    .thenBy { if (it.candidateCount == 0) Int.MAX_VALUE else it.candidateCount }
            )
        val subjectOnDaySet = mutableSetOf<String>() // shift|dept|sem|group|subjectCode|day

        fun tryMoveTheoryEntry(
            entryIdx: Int,
            depth: Int,
            visitedIds: MutableSet<String>,
        ): Boolean {
            val target = entries[entryIdx]
            if (target.isPractical) return tryMovePracticalBlock(entryIdx, visitedIds)
            if (!visitedIds.add(target.id)) return false

            val targetBook = booksByCode[target.subjectCode.trim()]
                ?: LibraryBook(subjectCode = target.subjectCode, title = target.subjectTitle)
            val targetRooms = matchingRooms(targetBook, target.department, target.shift, isLab = false)
            if (targetRooms.isEmpty()) return false

            // Pass 1: Direct move to any completely free slot
            val candidateDays = days.sortedBy { d ->
                groupDayLoad["${target.shift}|${target.department}|${target.semester}|${target.group}|$d"] ?: 0
            }
            for (altDay in candidateDays) {
                for (altP in 0 until periodsPerDay) {
                    if (altDay == target.day && altP == target.periodIndex) continue
                    for (altRoom in targetRooms) {
                        if (occupancy.isFree(
                                shift = target.shift,
                                day = altDay,
                                period = altP,
                                teacherEmail = target.teacherEmail,
                                roomNumber = altRoom.roomNumber,
                                dept = target.department,
                                sem = target.semester,
                                group = target.group,
                            )
                        ) {
                            occupancy.unreserve(
                                target.shift, target.day, target.periodIndex,
                                target.teacherEmail, target.roomNumber,
                                target.department, target.semester, target.group,
                            )
                            val (bSt, bEt) = RoutineRules.periodTimeRange(target.shift, altP, config)
                            occupancy.reserve(
                                target.shift, altDay, altP,
                                target.teacherEmail, altRoom.roomNumber,
                                target.department, target.semester, target.group,
                            )
                            entries[entryIdx] = target.copy(
                                day = altDay,
                                periodIndex = altP,
                                startTime = bSt,
                                endTime = bEt,
                                roomId = altRoom.id,
                                roomNumber = altRoom.roomNumber,
                            )
                            val oldBgKey = "${target.shift}|${target.department}|${target.semester}|${target.group}|${target.day}"
                            val newBgKey = "${target.shift}|${target.department}|${target.semester}|${target.group}|$altDay"
                            groupDayLoad[oldBgKey] = ((groupDayLoad[oldBgKey] ?: 1) - 1).coerceAtLeast(0)
                            groupDayLoad[newBgKey] = (groupDayLoad[newBgKey] ?: 0) + 1
                            return true
                        }
                    }
                }
            }

            if (depth <= 1) {
                visitedIds.remove(target.id)
                return false
            }

            // Pass 2: 2-hop move (displace a single blocking theory entry of teacher or group)
            for (altDay in candidateDays) {
                for (altP in 0 until periodsPerDay) {
                    if (altDay == target.day && altP == target.periodIndex) continue
                    val freeRoom = targetRooms.firstOrNull { occupancy.isRoomFree(target.shift, altDay, altP, it.roomNumber) }
                        ?: continue
                    val tFree = occupancy.isTeacherFree(target.shift, altDay, altP, target.teacherEmail)
                    val gFree = occupancy.isGroupFree(target.shift, altDay, altP, target.department, target.semester, target.group)
                    if (tFree == gFree) continue // Either both free (handled above) or both busy (requires moving 2)

                    val b2Idx = if (!tFree) {
                        entries.indexOfFirst { e ->
                            e.shift == target.shift &&
                                e.day == altDay &&
                                e.periodIndex == altP &&
                                e.teacherEmail.equals(target.teacherEmail, ignoreCase = true)
                        }
                    } else {
                        entries.indexOfFirst { e ->
                            e.shift == target.shift &&
                                e.day == altDay &&
                                e.periodIndex == altP &&
                                e.department == target.department &&
                                e.semester == target.semester &&
                                e.group == target.group
                        }
                    }
                    if (b2Idx < 0) continue
                    if (tryMoveTheoryEntry(b2Idx, depth - 1, visitedIds)) {
                        val altRoom = targetRooms.firstOrNull { occupancy.isRoomFree(target.shift, altDay, altP, it.roomNumber) }
                            ?: freeRoom
                        if (occupancy.isFree(
                                shift = target.shift,
                                day = altDay,
                                period = altP,
                                teacherEmail = target.teacherEmail,
                                roomNumber = altRoom.roomNumber,
                                dept = target.department,
                                sem = target.semester,
                                group = target.group,
                            )
                        ) {
                            val latestTarget = entries[entryIdx]
                            occupancy.unreserve(
                                latestTarget.shift, latestTarget.day, latestTarget.periodIndex,
                                latestTarget.teacherEmail, latestTarget.roomNumber,
                                latestTarget.department, latestTarget.semester, latestTarget.group,
                            )
                            val (bSt, bEt) = RoutineRules.periodTimeRange(latestTarget.shift, altP, config)
                            occupancy.reserve(
                                latestTarget.shift, altDay, altP,
                                latestTarget.teacherEmail, altRoom.roomNumber,
                                latestTarget.department, latestTarget.semester, latestTarget.group,
                            )
                            entries[entryIdx] = latestTarget.copy(
                                day = altDay,
                                periodIndex = altP,
                                startTime = bSt,
                                endTime = bEt,
                                roomId = altRoom.id,
                                roomNumber = altRoom.roomNumber,
                            )
                            val oldBgKey = "${latestTarget.shift}|${latestTarget.department}|${latestTarget.semester}|${latestTarget.group}|${latestTarget.day}"
                            val newBgKey = "${latestTarget.shift}|${latestTarget.department}|${latestTarget.semester}|${latestTarget.group}|$altDay"
                            groupDayLoad[oldBgKey] = ((groupDayLoad[oldBgKey] ?: 1) - 1).coerceAtLeast(0)
                            groupDayLoad[newBgKey] = (groupDayLoad[newBgKey] ?: 0) + 1
                            return true
                        }
                    }
                }
            }

            if (depth <= 2) visitedIds.remove(target.id)
            return false
        }

        fun tryRelocateTheoryForSlot(
            demand: SchedulingDemand,
            teacher: TeacherSchedulingPref,
            theoryRooms: List<ClassroomItem>,
            rotatedDays: List<String>,
            periodOrder: List<Int>,
        ): Boolean {
            for (maxDepth in listOf(1, 2, 3)) {
                // 1. Group free + Teacher busy
                for (day in rotatedDays) {
                    for (p in periodOrder) {
                        if (!occupancy.isGroupFree(demand.shift, day, p, demand.department, demand.semester, demand.group)) continue
                        if (occupancy.isTeacherFree(demand.shift, day, p, teacher.email)) continue
                        val hasRoom = theoryRooms.any { occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) }
                        if (!hasRoom) continue

                        val blockerIdx = entries.indexOfFirst { e ->
                            e.shift == demand.shift &&
                                e.day == day &&
                                e.periodIndex == p &&
                                e.teacherEmail.equals(teacher.email, ignoreCase = true)
                        }
                        if (blockerIdx < 0) continue
                        if (tryMoveTheoryEntry(blockerIdx, maxDepth, mutableSetOf())) {
                            val roomForNew = theoryRooms.firstOrNull {
                                occupancy.isRoomFree(demand.shift, day, p, it.roomNumber)
                            }
                            if (roomForNew != null && occupancy.isFree(
                                    shift = demand.shift,
                                    day = day,
                                    period = p,
                                    teacherEmail = teacher.email,
                                    roomNumber = roomForNew.roomNumber,
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
                                    roomNumber = roomForNew.roomNumber,
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
                                        roomId = roomForNew.id,
                                        roomNumber = roomForNew.roomNumber,
                                    )
                                )
                                val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$day"
                                groupDayLoad[gdKey] = (groupDayLoad[gdKey] ?: 0) + 1
                                return true
                            }
                        }
                    }
                }

                // 2. Teacher free + Group busy
                for (day in rotatedDays) {
                    for (p in periodOrder) {
                        if (!occupancy.isTeacherFree(demand.shift, day, p, teacher.email)) continue
                        if (occupancy.isGroupFree(demand.shift, day, p, demand.department, demand.semester, demand.group)) continue

                        val blockerIdx = entries.indexOfFirst { e ->
                            e.shift == demand.shift &&
                                e.day == day &&
                                e.periodIndex == p &&
                                e.department == demand.department &&
                                e.semester == demand.semester &&
                                e.group == demand.group
                        }
                        if (blockerIdx < 0) continue

                        val blocker = entries[blockerIdx]
                        val hasDirectFreeRoom = theoryRooms.any { occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) }
                        val canReuseBlockerRoom = theoryRooms.any { it.roomNumber.equals(blocker.roomNumber, ignoreCase = true) }
                        if (!hasDirectFreeRoom && !canReuseBlockerRoom) continue

                        if (tryMoveTheoryEntry(blockerIdx, maxDepth, mutableSetOf())) {
                            val roomForNew = theoryRooms.firstOrNull {
                                occupancy.isRoomFree(demand.shift, day, p, it.roomNumber)
                            }
                            if (roomForNew != null && occupancy.isFree(
                                    shift = demand.shift,
                                    day = day,
                                    period = p,
                                    teacherEmail = teacher.email,
                                    roomNumber = roomForNew.roomNumber,
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
                                    roomNumber = roomForNew.roomNumber,
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
                                        roomId = roomForNew.id,
                                        roomNumber = roomForNew.roomNumber,
                                    )
                                )
                                val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$day"
                                groupDayLoad[gdKey] = (groupDayLoad[gdKey] ?: 0) + 1
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }

        for (demand in theoryDemands) {
            val theoryHours = demand.book.theoryClassesPerWeek
            if (theoryHours <= 0) continue

            val key = GroupSubjectKey(demand.department, demand.shift, demand.semester, demand.group, demand.book.subjectCode)
            var teacher = lockedTeacherMap[key] ?: continue
            val theoryRooms = matchingRooms(demand.book, demand.department, demand.shift, isLab = false)

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

            var unplacedTheory = 0

            for (lectureNum in 0 until theoryHours) {
                val periodOrder = orderedPeriodsFor(teacher)
                var placed = false
                // Distribute evenly across the 5 days, preferring days with fewer occupied periods (balancing 2-3p labs)
                val startDayIdx = ((lectureNum * 2) + abs(demand.book.subjectCode.hashCode())) % days.size
                val rotatedDays = (days.indices)
                    .map { days[(startDayIdx + it) % days.size] }
                    .sortedBy { dayName ->
                        val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$dayName"
                        groupDayLoad[gdKey] ?: 0
                    }

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
                                    val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$day"
                                    groupDayLoad[gdKey] = (groupDayLoad[gdKey] ?: 0) + 1
                                    subjectOnDaySet.add(sdKey)
                                    placed = true
                                    break
                                }
                            }
                        }
                    }
                }
                if (!placed) {
                    placed = tryRelocateTheoryForSlot(
                        demand = demand,
                        teacher = teacher,
                        theoryRooms = theoryRooms,
                        rotatedDays = rotatedDays,
                        periodOrder = periodOrder,
                    )
                }
                // Fallback: switch the single-teacher lock for ALL entries of this (group, subject) to an eligible free teacher
                if (!placed) {
                    val existingIndices = entries.indices.filter { idx ->
                        val e = entries[idx]
                        e.shift == demand.shift &&
                            e.department == demand.department &&
                            e.semester == demand.semester &&
                            e.group == demand.group &&
                            e.subjectCode == demand.book.subjectCode
                    }
                    val altCandidates = findMatchingTeachers(
                        teachers = effectiveTeachers,
                        dept = demand.department,
                        shift = demand.shift,
                        semester = demand.semester,
                        subjectCode = demand.book.subjectCode,
                    ).filter { !it.email.equals(teacher.email, ignoreCase = true) }
                        .sortedBy { teacherLoad[it.email.lowercase()] ?: 0 }

                    for (altTeacher in altCandidates) {
                        val freeForExisting = existingIndices.all { idx ->
                            val e = entries[idx]
                            occupancy.isTeacherFree(e.shift, e.day, e.periodIndex, altTeacher.email)
                        }
                        if (!freeForExisting) continue

                        // Check if altTeacher can place this new theory period directly or via 1-hop relocation
                        var targetSlot: Triple<String, Int, ClassroomItem>? = null
                        for (day in rotatedDays) {
                            for (p in periodOrder) {
                                if (!occupancy.isGroupFree(demand.shift, day, p, demand.department, demand.semester, demand.group)) continue
                                if (!occupancy.isTeacherFree(demand.shift, day, p, altTeacher.email)) continue
                                val r = theoryRooms.firstOrNull { occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) }
                                if (r != null) {
                                    targetSlot = Triple(day, p, r)
                                    break
                                }
                            }
                            if (targetSlot != null) break
                        }
                        if (targetSlot == null) {
                            val pinnedIds = existingIndices.map { entries[it].id }.toSet()
                            // Case 1: Group free, altTeacher busy -> move altTeacher's blocker
                            for (day in rotatedDays) {
                                for (p in periodOrder) {
                                    if (!occupancy.isGroupFree(demand.shift, day, p, demand.department, demand.semester, demand.group)) continue
                                    val r = theoryRooms.firstOrNull { occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) } ?: continue
                                    val altBlockerIdx = entries.indexOfFirst { e ->
                                        e.shift == demand.shift &&
                                            e.day == day &&
                                            e.periodIndex == p &&
                                            e.teacherEmail.equals(altTeacher.email, ignoreCase = true)
                                    }
                                    if (altBlockerIdx >= 0 && tryMoveTheoryEntry(altBlockerIdx, 2, pinnedIds.toMutableSet())) {
                                        val freedRoom = theoryRooms.firstOrNull { occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) } ?: r
                                        if (occupancy.isFree(demand.shift, day, p, altTeacher.email, freedRoom.roomNumber, demand.department, demand.semester, demand.group)) {
                                            targetSlot = Triple(day, p, freedRoom)
                                            break
                                        }
                                    }
                                }
                                if (targetSlot != null) break
                            }
                            // Case 2: altTeacher free, Group busy -> move Group's blocker
                            if (targetSlot == null) {
                                for (day in rotatedDays) {
                                    for (p in periodOrder) {
                                        if (!occupancy.isTeacherFree(demand.shift, day, p, altTeacher.email)) continue
                                        if (occupancy.isGroupFree(demand.shift, day, p, demand.department, demand.semester, demand.group)) continue
                                        val grpBlockerIdx = entries.indexOfFirst { e ->
                                            e.shift == demand.shift &&
                                                e.day == day &&
                                                e.periodIndex == p &&
                                                e.department == demand.department &&
                                                e.semester == demand.semester &&
                                                e.group == demand.group
                                        }
                                        if (grpBlockerIdx < 0 || grpBlockerIdx in existingIndices) continue
                                        val blocker = entries[grpBlockerIdx]
                                        val hasRoom = theoryRooms.any {
                                            occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) ||
                                                it.roomNumber.equals(blocker.roomNumber, ignoreCase = true)
                                        }
                                        if (!hasRoom) continue
                                        if (tryMoveTheoryEntry(grpBlockerIdx, 2, pinnedIds.toMutableSet())) {
                                            val freedRoom = theoryRooms.firstOrNull { occupancy.isRoomFree(demand.shift, day, p, it.roomNumber) }
                                            if (freedRoom != null && occupancy.isFree(demand.shift, day, p, altTeacher.email, freedRoom.roomNumber, demand.department, demand.semester, demand.group)) {
                                                targetSlot = Triple(day, p, freedRoom)
                                                break
                                            }
                                        }
                                    }
                                    if (targetSlot != null) break
                                }
                            }
                        }

                        if (targetSlot != null) {
                            // Reassign all existing entries for this Group+Subject to altTeacher
                            for (idx in existingIndices) {
                                val oldE = entries[idx]
                                occupancy.unreserve(
                                    oldE.shift, oldE.day, oldE.periodIndex,
                                    oldE.teacherEmail, oldE.roomNumber,
                                    oldE.department, oldE.semester, oldE.group,
                                )
                                occupancy.reserve(
                                    oldE.shift, oldE.day, oldE.periodIndex,
                                    altTeacher.email, oldE.roomNumber,
                                    oldE.department, oldE.semester, oldE.group,
                                )
                                entries[idx] = oldE.copy(
                                    teacherEmail = altTeacher.email,
                                    teacherName = altTeacher.name,
                                )
                            }
                            val (sDay, sP, sRoom) = targetSlot
                            occupancy.reserve(
                                shift = demand.shift,
                                day = sDay,
                                period = sP,
                                teacherEmail = altTeacher.email,
                                roomNumber = sRoom.roomNumber,
                                dept = demand.department,
                                sem = demand.semester,
                                group = demand.group,
                            )
                            val (st, et) = RoutineRules.periodTimeRange(demand.shift, sP, config)
                            entries.add(
                                RoutineEntry(
                                    id = "ent_${entrySeq++}",
                                    day = sDay,
                                    periodIndex = sP,
                                    startTime = st,
                                    endTime = et,
                                    shift = demand.shift,
                                    department = demand.department,
                                    semester = demand.semester,
                                    group = demand.group,
                                    subjectCode = demand.book.subjectCode,
                                    subjectTitle = demand.book.title,
                                    isPractical = false,
                                    teacherEmail = altTeacher.email,
                                    teacherName = altTeacher.name,
                                    roomId = sRoom.id,
                                    roomNumber = sRoom.roomNumber,
                                )
                            )
                            val totalWeight = demand.book.practicalClassesPerWeek + demand.book.theoryClassesPerWeek
                            val oldEmail = teacher.email.lowercase()
                            val newEmail = altTeacher.email.lowercase()
                            teacherLoad[oldEmail] = ((teacherLoad[oldEmail] ?: totalWeight) - totalWeight).coerceAtLeast(0)
                            teacherLoad[newEmail] = (teacherLoad[newEmail] ?: 0) + totalWeight
                            val gdKey = "${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|$sDay"
                            groupDayLoad[gdKey] = (groupDayLoad[gdKey] ?: 0) + 1
                            subjectOnDaySet.add("${demand.shift}|${demand.department}|${demand.semester}|${demand.group}|${demand.book.subjectCode}|$sDay")
                            lockedTeacherMap[key] = altTeacher
                            teacher = altTeacher
                            placed = true
                            break
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

    internal fun splitPracticalIntoBlocks(
        hours: Int,
        mode: String = "AUTO_2_3",
    ): List<Int> {
        if (hours <= 0) return emptyList()
        if (hours == 1) return listOf(2) // Practical lab classes occupy at least 2 periods
        val normMode = mode.trim().uppercase()
        if (normMode == "TWO_PERIODS") {
            return when (hours) {
                2 -> listOf(2)
                3 -> listOf(3)
                4 -> listOf(2, 2)
                5 -> listOf(3, 2)
                6 -> listOf(2, 2, 2)
                else -> buildList {
                    var rem = hours
                    while (rem > 0) {
                        when {
                            rem == 3 -> { add(3); rem = 0 }
                            rem >= 2 -> { add(2); rem -= 2 }
                            else -> { add(2); rem = 0 }
                        }
                    }
                }
            }
        }
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
                    result.add(2)
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
        teacherPracticalLoad: Map<String, Int>,
        dept: String,
        shift: String,
        semester: String,
        subjectCode: String,
        incomingPracticalHours: Int,
        maxWeeklyPracticalPerTeacher: Int,
    ): TeacherSchedulingPref? {
        val candidates = findMatchingTeachers(teachers, dept, shift, semester, subjectCode)
        if (candidates.isEmpty()) return null
        return candidates.minByOrNull { t ->
            val email = t.email.lowercase()
            val tDept = RoutineRules.normalizeDepartmentShort(t.department)
            val deptPenalty = when {
                tDept == dept -> 0
                tDept == "Non-Tech" -> 2
                else -> 5
            }
            val pracLoad = teacherPracticalLoad[email] ?: 0
            val totalLoad = teacherLoad[email] ?: 0
            val pracOverflow = if (incomingPracticalHours > 0 && pracLoad + incomingPracticalHours > maxWeeklyPracticalPerTeacher) 1 else 0
            (pracOverflow * 10_000) + (deptPenalty * 1_000) + (pracLoad * 3) + totalLoad
        }
    }

    /**
     * Infers the required specialized laboratory for a BTEB subject so Physics practicals run in
     * Physics Lab, Chemistry practicals run in Chemistry Lab, Drawing in Drawing/CAD Lab, etc.
     */
    fun inferSubjectLabSpecialty(book: LibraryBook): LabSpecialty {
        val code = book.subjectCode.trim()
        val title = book.title.trim().lowercase()
        return when {
            code == "25912" || code == "25922" ||
                ((title.contains("physics") || title.contains("ফিজিক্স") || title.contains("পদার্থ")) && !title.contains("medical physics")) ->
                LabSpecialty.PHYSICS
            code == "25913" || title.contains("chemistry") || title.contains("কেমিস্ট্রি") || title.contains("রসায়ন") || title.contains("রসায়ন") ->
                LabSpecialty.CHEMISTRY
            code == "21011" || code == "26421" || code == "26722" || code == "27021" ||
                code == "26443" || code == "26452" || code == "27063" || code == "28664" ||
                title.contains("drawing") || title.contains("drafting") || title.contains("cad") || title.contains("ড্রইং") ->
                LabSpecialty.DRAWING
            code.startsWith("286") || title.contains("biomedical") || title.contains("electro-medical") ||
                title.contains("medical") || title.contains("dental") || title.contains("anesthesia") ||
                title.contains("radiology") || title.contains("physiotherapy") || title.contains("icu") ->
                LabSpecialty.BIOMEDICAL
            code.startsWith("285") || code.startsWith("666") || title.contains("computer") ||
                title.contains("programming") || title.contains("python") || title.contains("java") ||
                title.contains("data structure") || title.contains("algorithm") || title.contains("database") ||
                title.contains("web ") || title.contains("software") || title.contains("network") ||
                title.contains("microprocessor") || title.contains("operating system") || title.contains("cyber") ||
                title.contains("iot") || title.contains("graphics") || title.contains("multimedia") || title.contains("it support") ->
                LabSpecialty.COMPUTER
            code.startsWith("268") || title.contains("electronic") || title.contains("digital") ||
                title.contains("communication") || title.contains("pcb") || title.contains("television") || title.contains("microwave") ->
                LabSpecialty.ELECTRONICS
            code.startsWith("267") || title.contains("electricity") || title.contains("electrical") ||
                title.contains("circuit") || title.contains("switchgear") || title.contains("ac machine") ||
                title.contains("dc machine") || title.contains("wiring") ->
                LabSpecialty.ELECTRICAL
            code.startsWith("264") || code.startsWith("288") || title.contains("civil") ||
                title.contains("surveying") || title.contains("hydraulics") || title.contains("geotech") ||
                title.contains("hydrology") || title.contains("concrete") || title.contains("construction") ||
                title.contains("transportation") || title.contains("estimating") || title.contains("sanitary") || title.contains("water supply") ->
                LabSpecialty.CIVIL
            code.startsWith("270") || code == "26521" || code == "29231" || title.contains("workshop") ||
                title.contains("machine shop") || title.contains("welding") || title.contains("foundry") ||
                title.contains("metallurgy") || title.contains("metrology") || title.contains("applied mechanics") ||
                title.contains("fluid mechanics") || title.contains("heat treatment") || title.contains("tool design") ->
                LabSpecialty.MECHANICAL_WORKSHOP
            code.startsWith("271") || code.startsWith("272") || code.startsWith("262") ||
                title.contains("power") || title.contains("automobile") || title.contains("automotive") ||
                title.contains("ic engine") || title.contains("thermodynamics") || title.contains("fuels") ||
                title.contains("rac") || title.contains("hybrid") || title.contains("service station") ->
                LabSpecialty.POWER_AUTO
            else -> LabSpecialty.GENERAL
        }
    }
}
