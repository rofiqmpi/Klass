package com.mpi.klass.theme

import platform.Foundation.NSUserDefaults

actual fun saveThemeMode(mode: ThemeMode) {
    NSUserDefaults.standardUserDefaults.setObject(mode.name, forKey = "theme_mode")
}

actual fun loadThemeMode(): ThemeMode {
    val savedName = NSUserDefaults.standardUserDefaults.stringForKey("theme_mode")
    return try {
        if (savedName != null) ThemeMode.valueOf(savedName) else ThemeMode.DEFAULT
    } catch (_: Exception) {
        ThemeMode.DEFAULT
    }
}
