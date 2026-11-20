package com.mpi.klass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PushScheduleMathTest {

    @Test
    fun parseHourMinute24_handles12hAnd24h() {
        assertEquals(8 to 0, PushScheduleMath.parseHourMinute24("08:00 AM"))
        assertEquals(13 to 30, PushScheduleMath.parseHourMinute24("1:30 PM"))
        assertEquals(0 to 15, PushScheduleMath.parseHourMinute24("12:15 AM"))
        assertEquals(12 to 45, PushScheduleMath.parseHourMinute24("12:45 PM"))
        assertEquals(14 to 5, PushScheduleMath.parseHourMinute24("14:05"))
        assertNull(PushScheduleMath.parseHourMinute24("invalid"))
    }

    @Test
    fun subtractMinutes_wrapsCleanly() {
        assertEquals(7 to 50, PushScheduleMath.subtractMinutes(8, 0, 10))
        assertEquals(13 to 15, PushScheduleMath.subtractMinutes(13, 30, 15))
        assertEquals(23 to 55, PushScheduleMath.subtractMinutes(0, 5, 10))
    }

    @Test
    fun format12Hour_producesExpectedLabels() {
        assertEquals("8:00 AM", PushScheduleMath.format12Hour(8, 0))
        assertEquals("1:30 PM", PushScheduleMath.format12Hour(13, 30))
        assertEquals("12:00 PM", PushScheduleMath.format12Hour(12, 0))
        assertEquals("12:05 AM", PushScheduleMath.format12Hour(0, 5))
    }

    @Test
    fun filterUserEntriesForAlerts_filtersStudentAndTeacher() {
        val entries = listOf(
            RoutineEntry(
                id = "e1",
                day = "Sunday",
                periodIndex = 1,
                startTime = "8:45 AM",
                endTime = "9:30 AM",
                shift = RoutineRules.SHIFT_1,
                department = "CST",
                semester = "৬ষ্ঠ সেমিস্টার",
                group = "Group A",
                subjectCode = "66661",
                subjectTitle = "Android Dev",
                teacherEmail = "rahman@mpi.edu.bd",
                roomNumber = "301",
            ),
            RoutineEntry(
                id = "e2",
                day = "Sunday",
                periodIndex = 0,
                startTime = "8:00 AM",
                endTime = "8:45 AM",
                shift = RoutineRules.SHIFT_1,
                department = "CST",
                semester = "৬ষ্ঠ সেমিস্টার",
                group = "Group B",
                subjectCode = "66662",
                subjectTitle = "Microprocessor",
                teacherEmail = "karim@mpi.edu.bd",
                roomNumber = "302",
            ),
        )
        val plan = RoutinePlan(id = "master_6m_published", entries = entries)

        val studentProfile = StudentProfile(
            department = "CST (Computer Science & Technology)",
            semester = "6th Semester",
            shift = "1st Shift",
            section = "Group A",
        )
        val studentAlerts = PushScheduleMath.filterUserEntriesForAlerts(
            plan = plan,
            role = UserRole.STUDENT,
            email = "stu@mpi.edu.bd",
            studentProfile = studentProfile,
        )
        assertEquals(listOf("e1"), studentAlerts.map { it.id })

        val teacherAlerts = PushScheduleMath.filterUserEntriesForAlerts(
            plan = plan,
            role = UserRole.TEACHER,
            email = "KARIM@MPI.EDU.BD",
            studentProfile = null,
        )
        assertEquals(listOf("e2"), teacherAlerts.map { it.id })
    }
}
