package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
        }

    var selectedLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "bn") ?: "bn"
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value).apply()
        }

    fun resetOnboarding() {
        hasCompletedOnboarding = false
    }

    companion object {
        private const val PREFS_NAME = "klass_app_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "has_completed_onboarding"
        private const val KEY_LANGUAGE = "selected_language"
    }
}
