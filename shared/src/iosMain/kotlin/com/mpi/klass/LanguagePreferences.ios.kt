package com.mpi.klass

import platform.Foundation.NSUserDefaults

actual fun saveAppLanguage(language: AppLanguage) {
    NSUserDefaults.standardUserDefaults.setObject(language.name, forKey = "app_language")
}

actual fun loadAppLanguage(): AppLanguage {
    val savedName = NSUserDefaults.standardUserDefaults.stringForKey("app_language")
    return try {
        if (savedName != null) AppLanguage.valueOf(savedName) else AppLanguage.BENGALI
    } catch (_: Exception) {
        AppLanguage.BENGALI
    }
}
