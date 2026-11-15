package com.mpi.klass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutineGeneratorEngineTest {

    @Test
    fun groupCountMatchesDepartmentRules() {
        assertEquals(listOf("Group A", "Group B", "Group C"), RoutineRules.groupsForDepartment("CT"))
        assertEquals(listOf("Group A", "Group B", "Group C"), RoutineRules.groupsForDepartment("ET (Electrical Technology)"))
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("CST"))
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("MT"))
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("PT"))
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("ENT"))
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("EMT"))
        assertEquals("Group A", RoutineRules.normalizeGroup("CST", "Section C"))
        assertEquals("Group C", RoutineRules.normalizeGroup("CT", "Section C"))
    }

    @Test
    fun generateRoutineEnforcesZeroOverlapAndSingleTeacherLock() {
        val books = listOf(
            LibraryBook(
                id = "b_prog",
                title = "Programming Essentials",
                subjectCode = "28511",
                departmentSemesters = mapOf(
                    "CST" to listOf("১ম সেমিস্টার"),
                    "CT" to listOf("১ম সেমিস্টার"),
                ),
                theoryClassesPerWeek = 2,
                practicalClassesPerWeek = 3,
                credit = "3",
            ),
            LibraryBook(
                id = "b_eng",
                title = "English-I",
                subjectCode = "25712",
                departmentSemesters = mapOf(
                    "CST" to listOf("১ম সেমিস্টার"),
                    "CT" to listOf("১ম সেমিস্টার"),
                ),
                theoryClassesPerWeek = 2,
                practicalClassesPerWeek = 0,
                credit = "2",
            ),
        )

        val teachers = listOf(
            TeacherSchedulingPref(
                email = "t1@mpi.edu.bd",
                name = "Rahman Sir",
                department = "CST",
                shift = RoutineRules.SHIFT_1,
                timePreference = TeacherTimePreference.EARLY,
                semesterSubjects = mapOf("১ম সেমিস্টার" to listOf("28511")),
                schedulingSetupComplete = true,
            ),
            TeacherSchedulingPref(
                email = "t2@mpi.edu.bd",
                name = "Karim Sir",
                department = "CST",
                shift = RoutineRules.SHIFT_1,
                timePreference = TeacherTimePreference.LATE,
                semesterSubjects = mapOf("১ম সেমিস্টার" to listOf("28511")),
                schedulingSetupComplete = true,
            ),
            TeacherSchedulingPref(
                email = "nontech@mpi.edu.bd",
                name = "Nadia Madam",
                department = "Non-Tech",
                shift = RoutineRules.SHIFT_1,
                timePreference = TeacherTimePreference.ANY,
                semesterSubjects = mapOf("১ম সেমিস্টার" to listOf("25712")),
                schedulingSetupComplete = true,
            ),
        )

        val classrooms = listOf(
            ClassroomItem(id = "r1", roomNumber = "101", roomType = ClassroomType.THEORY, department = "ALL"),
            ClassroomItem(id = "r2", roomNumber = "102", roomType = ClassroomType.THEORY, department = "ALL"),
            ClassroomItem(id = "r3", roomNumber = "103", roomType = ClassroomType.THEORY, department = "ALL"),
            ClassroomItem(id = "l1", roomNumber = "Lab-1", roomType = ClassroomType.LAB, department = "ALL"),
            ClassroomItem(id = "l2", roomNumber = "Lab-2", roomType = ClassroomType.LAB, department = "ALL"),
        )

        val config = RoutineConfig(
            shift1StartTime = "08:00",
            shift2StartTime = "13:30",
            periodDurationMinutes = 45,
            periodsPerDay = 7,
            activeSemesters = listOf("১ম সেমিস্টার"),
        )

        val plan = RoutineGeneratorEngine.generateRoutine(
            config = config,
            books = books,
            teachers = teachers,
            classrooms = classrooms,
            generatedBy = "admin@mpi.edu.bd",
            nowMillis = 1000L,
        )

        // All Shift 1 slots should be placed cleanly
        val shift1Entries = plan.entries.filter { it.shift == RoutineRules.SHIFT_1 }
        assertTrue(shift1Entries.isNotEmpty(), "Expected Shift 1 routine entries")

        // Days must only be Sunday..Thursday
        assertTrue(plan.entries.all { it.day in RoutineRules.WORKING_DAYS })

        // Verify CT got 3 groups and CST got 2 groups in Shift 1
        val cstGroups = shift1Entries.filter { it.department == "CST" }.map { it.group }.toSet()
        val ctGroups = shift1Entries.filter { it.department == "CT" }.map { it.group }.toSet()
        assertEquals(setOf("Group A", "Group B"), cstGroups)
        assertEquals(setOf("Group A", "Group B", "Group C"), ctGroups)

        // Verify zero teacher overlap per (shift, day, period)
        val teacherSlotKeys = shift1Entries.map { "${it.shift}|${it.day}|${it.periodIndex}|${it.teacherEmail}" }
        assertEquals(teacherSlotKeys.size, teacherSlotKeys.distinct().size, "Teacher overlap detected!")

        // Verify zero room overlap per (shift, day, period)
        val roomSlotKeys = shift1Entries.map { "${it.shift}|${it.day}|${it.periodIndex}|${it.roomNumber}" }
        assertEquals(roomSlotKeys.size, roomSlotKeys.distinct().size, "Room overlap detected!")

        // Verify Single-Teacher-per-Group-Subject lock (never split one group's subject across teachers)
        val groupedTeachers = shift1Entries
            .groupBy { "${it.department}|${it.shift}|${it.semester}|${it.group}|${it.subjectCode}" }
            .mapValues { (_, list) -> list.map { it.teacherEmail }.distinct() }
        assertTrue(groupedTeachers.values.all { it.size == 1 }, "A group subject was split between multiple teachers!")

        // Verify Lab entries use LAB rooms and Theory entries use THEORY rooms
        val roomTypes = classrooms.associate { it.roomNumber to it.roomType }
        assertTrue(shift1Entries.all { e ->
            val expectedType = if (e.isPractical) ClassroomType.LAB else ClassroomType.THEORY
            roomTypes[e.roomNumber] == expectedType
        })
    }

    @Test
    fun validateSlotEditDetectsOverlaps() {
        val e1 = RoutineEntry(
            id = "e1",
            day = "Sunday",
            periodIndex = 0,
            shift = RoutineRules.SHIFT_1,
            department = "CST",
            semester = "১ম সেমিস্টার",
            group = "Group A",
            subjectCode = "28511",
            subjectTitle = "Programming",
            teacherEmail = "t1@mpi.edu.bd",
            teacherName = "Rahman Sir",
            roomNumber = "101",
        )
        val e2 = RoutineEntry(
            id = "e2",
            day = "Monday",
            periodIndex = 1,
            shift = RoutineRules.SHIFT_1,
            department = "CST",
            semester = "১ম সেমিস্টার",
            group = "Group B",
            subjectCode = "25712",
            subjectTitle = "English-I",
            teacherEmail = "t2@mpi.edu.bd",
            teacherName = "Nadia Madam",
            roomNumber = "102",
        )

        val clashes = RoutineGeneratorEngine.validateSlotEdit(
            allEntries = listOf(e1, e2),
            editingEntryId = "e2",
            targetShift = RoutineRules.SHIFT_1,
            targetDay = "Sunday",
            targetPeriod = 0,
            targetDepartment = "CST",
            targetSemester = "১ম সেমিস্টার",
            targetGroup = "Group A",
            targetTeacherEmail = "t1@mpi.edu.bd",
            targetRoomNumber = "101",
        )

        assertEquals(3, clashes.size, "Expected teacher, room, and group overlap warnings")
    }
}
