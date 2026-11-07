package com.mpi.klass.theme

import kotlinx.browser.localStorage

actual fun saveThemeMode(mode: ThemeMode) {
    try {
        localStorage.setItem("theme_mode", mode.name)
    } catch (_: Throwable) {}
}

actual fun loadThemeMode(): ThemeMode {
    return try {
        val savedName = localStorage.getItem("theme_mode")
        if (savedName != null) ThemeMode.valueOf(savedName) else ThemeMode.DEFAULT
    } catch (_: Throwable) {
        ThemeMode.DEFAULT
    }
}
