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
    fun campusMapRanksNearestRoomByEuclideanDistance() {
        val map = CampusMapConfig(
            originSet = true,
            departmentPoints = mapOf(
                "CST" to DepartmentPoint("CST", xMeters = 0f, yMeters = 0f),
                "CT" to DepartmentPoint("CT", xMeters = 10f, yMeters = 0f),
                "MT" to DepartmentPoint("MT", xMeters = 50f, yMeters = 0f),
            ),
        )
        assertEquals(0.0, map.distanceBetween("CST", "CST"), 0.01)
        assertEquals(10.0, map.distanceBetween("CST", "CT"), 0.01)
        assertEquals(50.0, map.distanceBetween("CST", "MT"), 0.01)

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
                id = "r_far",
                roomNumber = "501",
                roomType = ClassroomType.THEORY,
                department = "ALL",
                nearestDepartment = "MT",
                shifts = listOf(RoutineRules.SHIFT_1),
            ),
            ClassroomItem(
                id = "r_near",
                roomNumber = "201",
                roomType = ClassroomType.THEORY,
                department = "ALL",
                nearestDepartment = "CT",
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
            campusMap = map,
        )
        assertTrue(plan.entries.isNotEmpty(), "Expected generated routine entries")
        assertTrue(plan.entries.all { it.roomNumber == "201" }, "Expected nearer CT-zone room 201 to be chosen over MT-zone 501")
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
}