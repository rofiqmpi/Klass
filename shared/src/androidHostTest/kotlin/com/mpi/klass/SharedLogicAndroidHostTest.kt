package com.mpi.klass

import com.mpi.klass.theme.LottieIconCatalog
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class SharedLogicAndroidHostTest {

    @Test
    fun allLottieIconCompositionsParseCleanly() = runBlocking {
        val iconNames = listOf(
            "Notification", "MessageBubble", "Refresh", "TrashCan", "PhoneSolid",
            "Sparkle", "UsersGroup", "UsersRectangle", "Download", "Search", "LightMode",
            "DarkMode", "AutoMode", "Filter", "Plus", "Send", "Droplet", "Check", "Book",
            "CalendarDays", "GraduationCap", "DoorOpen", "Crown", "Shield",
            "Palette", "Clock", "Edit", "Person", "SolidUser", "UserTie",
            "UserNurse", "Lock", "Login", "Logout", "InfoCircle", "HelpCircle",
            "Home", "Award", "Close", "BackArrow", "RightArrow",
            "Camera", "LanguageGlobe", "IdCard", "Registration", "FileLines",
            "Database", "LocationDot", "EnvelopeSolid", "Eye", "EyeOff",
            "Copy", "Pin", "HandWave", "UserPlus", "Swap", "Warning",
            "ChevronRight", "ChevronDown", "MoreVert", "MoreHoriz",
            "LayerGroup", "VenusMars", "Ring", "PrayingHands",
        )

        iconNames.forEach { name ->
            assertTrue(
                LottieIconCatalog.hasBespokeAnimation(name),
                "Expected bespoke Lottie animation for $name",
            )
            val json = LottieIconCatalog.jsonFor(name)
            val composition = LottieCompositionSpec.JsonString(json).load()
            assertEquals(60.0, composition.durationFrames.toDouble(), 0.01, "Expected 60 frames for $name")
        }

        val genericComposition = LottieCompositionSpec.JsonString(LottieIconCatalog.jsonFor("UnknownFallback")).load()
        assertEquals(60.0, genericComposition.durationFrames.toDouble(), 0.01)
    }

    @Test
    fun multiNearestDepartmentRoomRanksMatchingDepartmentsFirst() {
        val parsed = ClassroomItem(nearestDepartment = "CST, CT")
        assertEquals(listOf("CST", "CT"), parsed.effectiveNearestDepartments())

        val books = listOf(
            LibraryBook(
                id = "b1",
                subjectCode = "66611",
                title = "Computer Application",
                credit = "2",
                theoryClassesPerWeek = 1,
                practicalClassesPerWeek = 0,
                departmentSemesters = mapOf("CST" to listOf("১ম সেমিস্টার")),
            )
        )
        val teachers = listOf(
            TeacherSchedulingPref(
                email = "t@mpi.edu",
                name = "Teacher A",
                department = "CST",
                shift = RoutineRules.SHIFT_1,
                semesterSubjects = mapOf("১ম সেমিস্টার" to listOf("66611")),
                schedulingSetupComplete = true,
            )
        )
        val classrooms = listOf(
            ClassroomItem(
                id = "r_other",
                roomNumber = "501",
                roomType = ClassroomType.THEORY,
                department = "ALL",
                nearestDepartment = "MT",
                nearestDepartments = listOf("MT"),
                shifts = listOf(RoutineRules.SHIFT_1),
            ),
            ClassroomItem(
                id = "r_near_multi",
                roomNumber = "201",
                roomType = ClassroomType.THEORY,
                department = "ALL",
                nearestDepartment = "CST, CT",
                nearestDepartments = listOf("CST", "CT"),
                shifts = listOf(RoutineRules.SHIFT_1),
            ),
        )
        val plan = RoutineGeneratorEngine.generateRoutine(
            config = RoutineConfig(activeSemesters = listOf("১ম সেমিস্টার")),
            books = books,
            teachers = teachers,
            classrooms = classrooms,
            generatedBy = "admin@mpi.edu",
            nowMillis = 1_000L,
        )
        assertTrue(plan.entries.isNotEmpty(), "Expected generated routine entries")
        assertTrue(plan.entries.all { it.roomNumber == "201" }, "Expected multi-select CST/CT room 201 to be chosen over MT room 501")
    }

    @Test
    fun zedEngineOutputsA2UiBlocksWithoutPresetQuestions() {
        val snapshot = ZedContextSnapshot(
            donors = listOf(
                BloodDonorItem(
                    id = "d1",
                    name = "Rashed",
                    bloodGroup = "O+",
                    phone = "01711000000",
                    roleLabel = "Student",
                    department = "CST",
                )
            ),
            userDisplayName = "Nabil",
        )
        val reply = ZedAiEngine.evaluate("O+ blood donor", snapshot)
        assertTrue(reply.isSupportedDomain)
        assertTrue(reply.blocks.any { it is ZedA2UiBlock.HeaderBanner })
        assertTrue(reply.blocks.any { it is ZedA2UiBlock.EntityListCard })
        val entityCard = reply.blocks.filterIsInstance<ZedA2UiBlock.EntityListCard>().first()
        assertEquals("01711000000", entityCard.entities.first().phone)
    }

    @Test
    fun legacyConnectionsPrivacyMapsToPrivate() {
        assertEquals(ProfilePrivacy.PRIVATE, ProfilePrivacy.fromKey("CONNECTIONS"))
    }

    @Test
    fun primaryAdminEmailAndFullBtebCatalogConfigured() {
        assertEquals("mdrofiqmpi@gmail.com", PRIMARY_ADMIN_EMAIL)
        assertEquals(179, BtebBooksCatalog.books.size)
        assertTrue(BtebBooksCatalog.books.all { it.id.startsWith("bteb_") })
    }

    @Test
    fun zedSchemaAndBackendHelpersOperateCleanly() {
        assertEquals("OBJECT", zedResponseSchema.type)
        assertTrue(zedResponseSchema.properties?.containsKey("answerText") == true)
        assertTrue(zedResponseSchema.properties?.containsKey("blocks") == true)

        val history = buildSanitizedChatHistory(
            listOf(
                AiChatMessage(id = "m0", text = "Welcome", isFromUser = false),
                AiChatMessage(id = "u1", text = "Hi", isFromUser = true),
                AiChatMessage(id = "m1", text = "Hello!", isFromUser = false),
                AiChatMessage(id = "u2", text = "Trailing user", isFromUser = true),
            )
        )
        assertEquals(2, history.size)
        assertEquals("user", history[0].role)
        assertEquals("model", history[1].role)

        val fenced = """
            ```json
            {"answerText":"Computer Application is 66611.","sourceBadge":"Books Library","isSupportedDomain":true,"blocks":[{"blockType":"CALLOUT","domain":"BOOKS","message":"Available in 1st Sem","isWarning":false}]}
            ```
        """.trimIndent()
        val unwrapped = extractJsonObjectIfWrapped(fenced)!!
        val parsed = parseZedA2UiResponse(unwrapped)
        assertEquals("Computer Application is 66611.", parsed.answerText)
        assertEquals("Books Library", parsed.sourceBadge)
        assertEquals(1, parsed.blocks.size)

        val campusJson = buildFullAppContextJson(
            query = "CST 1st semester books",
            snapshot = ZedContextSnapshot(books = BtebBooksCatalog.books, userDisplayName = "Rofiq"),
            todayEnglishDay = "Sunday",
        )
        assertTrue(campusJson.contains("Mymensingh Polytechnic Institute"))
        assertTrue(campusJson.contains("books"))
        assertTrue(campusJson.length < 8_000, "Expected focused campus context payload, was ${campusJson.length}")

        val generalJson = buildFullAppContextJson(
            query = "Explain Newton second law of motion",
            snapshot = ZedContextSnapshot(books = BtebBooksCatalog.books, userDisplayName = "Rofiq"),
            todayEnglishDay = "Sunday",
        )
        assertTrue(generalJson.contains("Mymensingh Polytechnic Institute"))
        assertTrue(!generalJson.contains("\"books\":["), "General query should not include full books dump")
        assertTrue(generalJson.length < 1_000, "Expected tiny general context payload, was ${generalJson.length}")
    }

    @Test
    fun zedQueryClassifierAndDiagnosticsWorkCleanly() = runBlocking {
        assertTrue(!isCampusOrAppQuery("hi"))
        assertTrue(!isCampusOrAppQuery("Explain recursion in Kotlin"))
        assertTrue(isCampusOrAppQuery("CST 1st semester books"))
        assertTrue(isCampusOrAppQuery("আজকের ক্লাস রুটিন দেখাও"))
        assertTrue(isCampusOrAppQuery("O+ blood donor"))
        assertTrue(isCampusOrAppQuery("66611"))

        ZedAppCheckDiagnostics.register { "Simulated App Check diagnostic error" }
        assertEquals("Simulated App Check diagnostic error", ZedAppCheckDiagnostics.checkError())

        ZedAppCheckDiagnostics.register { throw IllegalStateException("unexpected") }
        assertEquals(null, ZedAppCheckDiagnostics.checkError())
    }
}