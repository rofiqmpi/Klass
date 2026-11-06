package com.mpi.klass

object AppStrings {
    fun quote(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ফোকাস মানে হলো হাজারটা ভালো আইডিয়াকে 'না' বলা। আর সেই একটা 'হ্যাঁ'-এর জন্য শেষ পর্যন্ত লড়ে যাওয়া।"
        AppLanguage.ENGLISH -> "Focus is saying 'no' to a hundred good ideas, and fighting till the end for that one 'yes'."
    }

    fun languageBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বাংলা"
        AppLanguage.ENGLISH -> "English"
    }

    fun registrationBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "রেজিস্ট্রেশন"
        AppLanguage.ENGLISH -> "Registration"
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
        AppLanguage.BENGALI -> "Google, Facebook বা Apple অ্যাকাউন্ট\nব্যবহার করে সাইন ইন করুন"
        AppLanguage.ENGLISH -> "Sign in using your Google, Facebook or Apple account"
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

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বাতিল"
        AppLanguage.ENGLISH -> "Cancel"
    }
}
