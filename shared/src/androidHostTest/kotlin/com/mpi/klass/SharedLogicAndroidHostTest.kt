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
}