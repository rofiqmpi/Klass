package com.mpi.klass

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZedAiEngineTest {

    private fun buildSampleSnapshot(): ZedContextSnapshot {
        val books = listOf(
            LibraryBook(
                id = "b1",
                title = "Data Structure & Algorithm",
                subjectCode = "66651",
                departmentSemesters = mapOf("CST" to listOf("5th")),
                theoryClassesPerWeek = 3,
                practicalClassesPerWeek = 3,
                credit = "4",
            ),
            LibraryBook(
                id = "b2",
                title = "Structural Mechanics",
                subjectCode = "66441",
                departmentSemesters = mapOf("CT" to listOf("4th")),
                theoryClassesPerWeek = 3,
                practicalClassesPerWeek = 2,
                credit = "3",
            ),
        )
        val teachers = listOf(
            TeacherDirectoryItem(
                email = "rahim@mpi.edu",
                name = "Md. Rahim Uddin",
                department = "CST",
                shift = "1st Shift",
                phone = "01711000001",
            ),
        )
        val routine = RoutinePlan(
            id = "master_6m_published",
            status = "PUBLISHED",
            entries = listOf(
                RoutineEntry(
                    id = "e1",
                    day = "Sunday",
                    periodIndex = 1,
                    startTime = "08:00",
                    endTime = "08:45",
                    shift = "1st Shift",
                    department = "CST",
                    semester = "৫ম সেমিস্টার",
                    group = "A",
                    subjectCode = "66651",
                    subjectTitle = "Data Structure & Algorithm",
                    isPractical = false,
                    teacherEmail = "rahim@mpi.edu",
                    teacherName = "Md. Rahim Uddin",
                    roomId = "r1",
                    roomNumber = "204",
                )
            ),
        )
        val donors = listOf(
            BloodDonorItem(
                id = "d1",
                name = "Tanvir Ahmed",
                bloodGroup = "O+",
                phone = "01800000000",
                roleLabel = "Student",
                department = "CST",
            )
        )
        return ZedContextSnapshot(
            books = books,
            teachers = teachers,
            routinePlan = routine,
            donors = donors,
            userProfile = StudentProfile(
                name = "Nabil Hasan",
                department = "CST",
                semester = "৫ম সেমিস্টার",
                shift = "1st Shift",
                section = "A",
            ),
            userRole = UserRole.STUDENT,
            userDisplayName = "Nabil Hasan",
        )
    }

    @Test
    fun answersOnlyFourAllowedDomainsConcisely() {
        val snap = buildSampleSnapshot()

        val bookRes = ZedAiEngine.evaluate("Tell me about book 66651", snap)
        assertTrue(bookRes.isSupportedDomain)
        assertTrue(bookRes.answerText.contains("Data Structure & Algorithm"))
        assertTrue(bookRes.answerText.lines().size <= 5)

        val bloodRes = ZedAiEngine.evaluate("Need O+ blood donor", snap)
        assertTrue(bloodRes.isSupportedDomain)
        assertTrue(bloodRes.answerText.contains("Tanvir Ahmed"))
        assertTrue(bloodRes.answerText.lines().size <= 5)

        val teacherRes = ZedAiEngine.evaluate("CST teacher Rahim sir koyta class nen?", snap)
        assertTrue(teacherRes.isSupportedDomain)
        assertTrue(teacherRes.answerText.contains("Md. Rahim Uddin"))
        assertTrue(teacherRes.answerText.contains("1 টি ক্লাস"))

        val routineRes = ZedAiEngine.evaluate("Ajker Sunday class routine dekhao", snap, todayEnglishDay = "Sunday")
        assertTrue(routineRes.isSupportedDomain)
        assertTrue(routineRes.answerText.contains("Data Structure & Algorithm"))
        assertTrue(routineRes.answerText.contains("Room 204"))
    }

    @Test
    fun strictlyRejectsQuestionsOutsideTheFourDomains() {
        val snap = buildSampleSnapshot()

        val generalRes = ZedAiEngine.evaluate("What is the distance from Earth to Mars?", snap)
        assertFalse(generalRes.isSupportedDomain)
        assertTrue(generalRes.answerText.contains("Books Library"))
        assertTrue(generalRes.answerText.contains("Blood Donor"))
        assertTrue(generalRes.answerText.contains("Teachers"))
        assertTrue(generalRes.answerText.contains("Class Routine"))

        val mpiPortalRes = ZedAiEngine.evaluate("mpi.politech.gov.bd er principal ke?", snap)
        assertFalse(mpiPortalRes.isSupportedDomain)

        val roomRes = ZedAiEngine.evaluate("Room 204 kothay?", snap)
        assertFalse(roomRes.isSupportedDomain)
    }

    @Test
    fun greetingIsShortAndWelcoming() {
        val snap = buildSampleSnapshot()
        val greet = ZedAiEngine.evaluate("Assalamu alaikum", snap)
        assertTrue(greet.isSupportedDomain)
        assertTrue(greet.answerText.contains("Nabil"))
        assertTrue(greet.answerText.lines().size <= 2)
    }

    @Test
    fun keyProviderRejectsPlaceholderAndStoresConfiguredKey() {
        ZedAiKeyProvider.setApiKey("GEMINI_API_KEY_DEFAULT_VALUE")
        assertTrue(ZedAiKeyProvider.getApiKey().isEmpty())

        ZedAiKeyProvider.setApiKey("   ")
        assertTrue(ZedAiKeyProvider.getApiKey().isEmpty())

        ZedAiKeyProvider.setApiKey("sample-test-key-123")
        assertTrue(ZedAiKeyProvider.getApiKey() == "sample-test-key-123")
        ZedAiKeyProvider.setApiKey("")
    }
}
