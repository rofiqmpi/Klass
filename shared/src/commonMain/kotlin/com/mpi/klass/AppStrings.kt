package com.mpi.klass

import androidx.compose.runtime.compositionLocalOf

val LocalAppLanguage = compositionLocalOf { AppLanguage.BENGALI }

object AppStrings {
    // Standard UI keys
    fun languageBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ভাষা"
        AppLanguage.ENGLISH -> "Language"
    }

    fun registrationBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "এগিয়ে যান"
        AppLanguage.ENGLISH -> "Proceed"
    }

    fun quote(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ফোকাস মানে হলো হাজারটা ভালো আইডিয়াকে 'না' বলা। আর সেই একটা 'হ্যাঁ'-এর জন্য শেষ পর্যন্ত লড়ে যাওয়া।"
        AppLanguage.ENGLISH -> "Focus is saying 'no' to a hundred good ideas, and fighting till the end for that one 'yes'."
    }

    fun termsAndConditions(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "টার্মস অ্যান্ড কন্ডিশন"
        AppLanguage.ENGLISH -> "Terms & Conditions"
    }

    fun signInTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সাইন ইন করুন"
        AppLanguage.ENGLISH -> "Sign In"
    }

    fun signInSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "Google অথবা ইমেইল অ্যাকাউন্ট\nব্যবহার করে সাইন ইন করুন"
        AppLanguage.ENGLISH -> "Sign in using Google or email"
    }

    fun guestMode(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "গেস্ট মোড"
        AppLanguage.ENGLISH -> "Guest Mode"
    }

    fun languageDialogTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ভাষা নির্বাচন করুন"
        AppLanguage.ENGLISH -> "Select Language"
    }

    fun closeAppDialogTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অ্যাপ বন্ধ করুন"
        AppLanguage.ENGLISH -> "Close App"
    }

    fun closeAppDialogMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "আপনি কি অ্যাপটি বন্ধ করতে চান?"
        AppLanguage.ENGLISH -> "Do you want to close the app?"
    }

    fun yes(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "হ্যাঁ"
        AppLanguage.ENGLISH -> "Yes"
    }

    fun no(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "না"
        AppLanguage.ENGLISH -> "No"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বাতিল"
        AppLanguage.ENGLISH -> "Cancel"
    }

    // Dynamic translation dictionary across the entire app
    private val translations = mapOf(
        // Navigation & Bars
        "হোম" to "Home",
        "মেনু" to "Menu",
        "প্রোফাইল" to "Profile",
        "বিজ্ঞপ্তি" to "Notifications",
        "নোটিশ" to "Notice",
        "মেনু অপশনসমূহ" to "Menu Options",
        "এখানে আপনার প্রয়োজনীয় মেনু ও অপশনগুলো উপলব্ধ রয়েছে।" to "All your required menus and options are available here.",

        // Roles & Dashboards
        "শিক্ষার্থী" to "Student",
        "শিক্ষক" to "Teacher",
        "এডমিন" to "Admin",
        "এডমিনিস্ট্রেটর" to "Administrator",
        "গেস্ট" to "Guest",
        "গেস্ট মোড" to "Guest Mode",
        "শিক্ষক ড্যাশবোর্ড" to "Teacher Dashboard",
        "এডমিন ড্যাশবোর্ড" to "Admin Dashboard",
        "শিক্ষার্থী ড্যাশবোর্ড" to "Student Dashboard",

        // Actions & Buttons
        "লগআউট" to "Log Out",
        "লগইন" to "Log In",
        "সাইন ইন" to "Sign In",
        "সাইন ইন করুন" to "Sign In",
        "এডমিন সাইন ইন" to "Admin Sign In",
        "শিক্ষক সাইন ইন" to "Teacher Sign In",
        "সংরক্ষণ করুন" to "Save",
        "সংরক্ষণ করা হচ্ছে..." to "Saving...",
        "প্রোফাইল সংরক্ষণ করুন" to "Save Profile",
        "প্রোফাইল এডিট" to "Edit Profile",
        "প্রোফাইল এডিট করুন" to "Edit Profile",
        "প্রোফাইল নাম ও ছবি পরিবর্তন করুন" to "Change profile name and photo",
        "প্রোফাইল সেটআপ" to "Profile Setup",
        "শিক্ষার্থী প্রোফাইল সেটআপ" to "Student Profile Setup",
        "শিক্ষার্থী প্রোফাইল বিবরণ" to "Student Profile Details",
        "এগিয়ে যান" to "Proceed",
        "রেজিস্ট্রেশন" to "Registration",
        "ফিরে যান" to "Back",
        "অ্যাপ বন্ধ করুন" to "Close App",
        "টার্মস অ্যান্ড কন্ডিশন" to "Terms & Conditions",
        "থিম নির্বাচন করুন" to "Select Theme",
        "ভাষা নির্বাচন করুন" to "Select Language",
        "ভাষা নির্বাচন" to "Select Language",
        "ভাষা" to "Language",

        // Dialogs & Feedback
        "আপনি কি অ্যাপটি বন্ধ করতে চান?" to "Do you want to close the app?",
        "আপনি কি লগআউট করতে চান?" to "Are you sure you want to log out?",
        "হ্যাঁ" to "Yes",
        "না" to "No",
        "বাতিল" to "Cancel",
        "ইন্টারনেট সংযোগ নেই। আপনার নেটওয়ার্ক চেক করুন।" to "No internet connection. Please check your network.",
        "কিছু ভুল হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।" to "Something went wrong. Please try again.",
        "গুগল সাইন-ইন ব্যর্থ হয়েছে" to "Google sign-in failed",
        "অনুমোদন প্রক্রিয়াধীন" to "Approval Pending",
        "এডমিন অনুমোদন অনুরোধ" to "Admin Approval Request",
    )

    /**
     * Translates any string dynamically based on the current AppLanguage.
     */
    fun translate(text: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return text
        return translations[text.trim()] ?: text
    }
}

/**
 * Convenient extension to translate strings dynamically throughout Composables.
 */
fun String.localized(lang: AppLanguage): String = AppStrings.translate(this, lang)

@androidx.compose.runtime.Composable
fun String.localized(): String {
    val lang = LocalAppLanguage.current
    return AppStrings.translate(this, lang)
}
