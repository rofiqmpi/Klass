package com.mpi.klass

import android.content.Context
import com.mpi.klass.theme.getAppContext
import java.util.Locale

actual fun saveAppLanguage(language: AppLanguage) {
    val ctx = getAppContext()
    ctx?.getSharedPreferences("klass_prefs", Context.MODE_PRIVATE)
        ?.edit()
        ?.putString("app_language", language.name)
        ?.apply()

    ctx?.let { context ->
        try {
            val tag = if (language == AppLanguage.BENGALI) "bn-BD" else "en-US"
            val locale = Locale.forLanguageTag(tag)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } catch (_: Exception) {}
    }
}

actual fun loadAppLanguage(): AppLanguage {
    val savedName = getAppContext()?.getSharedPreferences("klass_prefs", Context.MODE_PRIVATE)
        ?.getString("app_language", AppLanguage.BENGALI.name)
    return try {
        if (savedName != null) AppLanguage.valueOf(savedName) else AppLanguage.BENGALI
    } catch (_: Exception) {
        AppLanguage.BENGALI
    }
}
