package com.mpi.klass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun quickFilterFormatsBadgeAndNormalizesGroups() {
        val cstFilter = RoutineQuickFilter(
            semesterKey = "৬ষ্ঠ সেমিস্টার",
            shift = RoutineRules.SHIFT_2,
            department = "CST",
            group = "Group C",
        )
        assertEquals("6/2/CST-A", cstFilter.toBadgeText())
        assertEquals(listOf("Group A", "Group B"), RoutineRules.groupsForDepartment("CST"))
        assertEquals(listOf("Group A", "Group B", "Group C"), RoutineRules.groupsForDepartment("CT"))
        assertEquals(listOf("Group A", "Group B", "Group C"), RoutineRules.groupsForDepartment("ET"))
    }

    @Test
    fun activeActionNotificationsMatchRoleAndPermissionState() {
        val studentActions = PushScheduleMath.activeActionNotificationIds(
            role = UserRole.STUDENT,
            studentProfile = null,
            teacherPref = null,
            publishedPlan = null,
            permissionGranted = false,
        )
        assertTrue("action_complete_profile" in studentActions)
        assertTrue("action_enable_push" in studentActions)

        val guestActions = PushScheduleMath.activeActionNotificationIds(
            role = null,
            studentProfile = null,
            teacherPref = null,
            publishedPlan = null,
            permissionGranted = true,
        )
        assertEquals(listOf("action_guest_signin"), guestActions)
    }

    @Test
    fun validateLoginPasswordChecksStrengthAndSecurityRules() {
        assertEquals(
            "লগইন পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে",
            validateLoginPassword("Ab1@xy", "Ab1@xy"),
        )
        assertEquals(
            "পাসওয়ার্ডে কোনো স্পেস ব্যবহার করা যাবে না",
            validateLoginPassword("Abc 123@!", "Abc 123@!"),
        )
        assertEquals(
            "ইমেইল বা সহজ পাসওয়ার্ডের পরিবর্তে শক্তিশালী পাসওয়ার্ড দিন",
            validateLoginPassword("Rofiq@123", "Rofiq@123", email = "rofiq@gmail.com"),
        )
        assertEquals(
            "পাসওয়ার্ডে অন্তত একটি বড় (A-Z) ও একটি ছোট (a-z) অক্ষর থাকতে হবে",
            validateLoginPassword("abcdef1@!", "abcdef1@!"),
        )
        assertEquals(
            "পাসওয়ার্ডে অন্তত একটি সংখ্যা (0-9) থাকতে হবে",
            validateLoginPassword("Abcdefg@!", "Abcdefg@!"),
        )
        assertEquals(
            "পাসওয়ার্ডে অন্তত একটি বিশেষ চিহ্ন (@, #, $, ! ইত্যাদি) থাকতে হবে",
            validateLoginPassword("Abcdef12", "Abcdef12"),
        )
        assertEquals(
            "উভয় পাসওয়ার্ড একরকম হতে হবে",
            validateLoginPassword("Strong#2026", "Strong#2025"),
        )
        assertEquals(null, validateLoginPassword("Strong#2026", "Strong#2026", email = "rofiq@gmail.com"))

        val strongEval = evaluatePasswordStrength("Strong#2026", email = "rofiq@gmail.com")
        assertTrue(strongEval.isAcceptable)
        assertEquals(PasswordStrengthLevel.STRONG, strongEval.level)

        val weakEval = evaluatePasswordStrength("12345678")
        assertTrue(!weakEval.isAcceptable)
        assertEquals(PasswordStrengthLevel.WEAK, weakEval.level)
    }

    @Test
    fun welcomeButtonLabelsTranslateCleanly() {
        assertEquals("শুরু করুন", AppStrings.registrationBtn(AppLanguage.BENGALI))
        assertEquals("Get Started", AppStrings.registrationBtn(AppLanguage.ENGLISH))
        assertEquals("সাপোর্ট", AppStrings.supportBtn(AppLanguage.BENGALI))
        assertEquals("Support", AppStrings.supportBtn(AppLanguage.ENGLISH))
        assertEquals("লগইন", AppStrings.loginBtn(AppLanguage.BENGALI))
        assertEquals("Login", AppStrings.loginBtn(AppLanguage.ENGLISH))
    }

    @Test
    fun localizedEnglishLabelsTranslateToBengali() {
        assertEquals("সব", AppStrings.translate("All", AppLanguage.BENGALI))
        assertEquals("সম্পর্কে", AppStrings.translate("About", AppLanguage.BENGALI))
        assertEquals("প্রোফাইল এডিট", AppStrings.translate("প্রোফাইল এডিট", AppLanguage.BENGALI))
    }

    @Test
    fun customKeyboardIncludesRareSymbolsEmojisAndDeletesWholeEmojiTokens() {
        val allSpecial = KlassKeyboardLayouts.specialRow1 +
            KlassKeyboardLayouts.specialRow2 +
            KlassKeyboardLayouts.specialRow3 +
            KlassKeyboardLayouts.specialRow4
        assertTrue("§" in allSpecial)
        assertTrue("◈" in allSpecial)
        assertTrue("❖" in allSpecial)
        assertTrue("⌘" in allSpecial)
        assertTrue("∞" in allSpecial)

        val allEmojis = KlassKeyboardLayouts.emojiRow1 +
            KlassKeyboardLayouts.emojiRow2 +
            KlassKeyboardLayouts.emojiRow3
        assertTrue("🔐" in allEmojis)
        assertTrue("🛡️" in allEmojis)
        assertTrue("⚡" in allEmojis)
        assertTrue("🗝️" in allEmojis)

        // Ensure multi-byte surrogate emojis & variation-selector emojis delete cleanly in one tap
        assertEquals("ab", popLastKeyboardToken("ab🔐"))
        assertEquals("ab", popLastKeyboardToken("ab🛡️"))
        assertEquals("a", popLastKeyboardToken("ab"))
        assertEquals("", popLastKeyboardToken("🗝️"))
        assertEquals("", popLastKeyboardToken(""))
    }
}