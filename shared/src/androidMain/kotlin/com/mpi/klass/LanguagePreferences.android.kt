package com.mpi.klass

import android.content.Context
import com.mpi.klass.theme.getAppContext
import java.util.Locale

actual fun saveAppLanguage(language: AppLanguage) {
    val ctx = getAppContext()
    ctx?.getSharedPreferences("klass_prefs", Context.MODE_PRIVATE)
        ?.edit()
        ?.putString("app_language", AppLanguage.ENGLISH.name)
        ?.apply()
}

actual fun loadAppLanguage(): AppLanguage = AppLanguage.ENGLISH
