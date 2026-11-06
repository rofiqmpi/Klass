package com.mpi.klass

enum class AppLanguage {
    BENGALI,
    ENGLISH;

    val displayName: String
        get() = when (this) {
            BENGALI -> "বাংলা"
            ENGLISH -> "English"
        }
}

expect fun saveAppLanguage(language: AppLanguage)
expect fun loadAppLanguage(): AppLanguage
