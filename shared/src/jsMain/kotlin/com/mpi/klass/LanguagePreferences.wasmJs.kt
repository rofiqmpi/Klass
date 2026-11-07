package com.mpi.klass

import kotlinx.browser.localStorage

actual fun saveAppLanguage(language: AppLanguage) {
    try {
        localStorage.setItem("app_language", language.name)
    } catch (_: Throwable) {}
}

actual fun loadAppLanguage(): AppLanguage {
    return try {
        val savedName = localStorage.getItem("app_language")
        if (savedName != null) AppLanguage.valueOf(savedName) else AppLanguage.BENGALI
    } catch (_: Throwable) {
        AppLanguage.BENGALI
    }
}
