package com.mpi.klass.theme

import android.content.Context

private var appContext: Context? = null

fun initThemeContext(context: Context) {
    appContext = context.applicationContext
}

fun getAppContext(): Context? = appContext

actual fun saveThemeMode(mode: ThemeMode) {
    appContext?.getSharedPreferences("klass_prefs", Context.MODE_PRIVATE)
        ?.edit()
        ?.putString("theme_mode", mode.name)
        ?.apply()
}

actual fun loadThemeMode(): ThemeMode {
    val savedName = appContext?.getSharedPreferences("klass_prefs", Context.MODE_PRIVATE)
        ?.getString("theme_mode", ThemeMode.DEFAULT.name)
    return try {
        if (savedName != null) ThemeMode.valueOf(savedName) else ThemeMode.DEFAULT
    } catch (_: Exception) {
        ThemeMode.DEFAULT
    }
}
