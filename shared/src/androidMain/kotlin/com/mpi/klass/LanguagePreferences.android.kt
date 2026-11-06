package com.mpi.klass

import android.content.Context
import com.mpi.klass.theme.getAppContext

actual fun saveAppLanguage(language: AppLanguage) {
    getAppContext()?.getSharedPreferences("klass_prefs", Context.MODE_PRIVATE)
        ?.edit()
        ?.putString("app_language", language.name)
        ?.apply()
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
