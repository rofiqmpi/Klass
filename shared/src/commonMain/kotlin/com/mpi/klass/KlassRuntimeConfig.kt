package com.mpi.klass

/**
 * Runtime secrets/config populated on Android from Secrets-backed BuildConfig fields
 * in MainActivity.onCreate(). Keeps `.env` / GitHub Actions Secrets cleanly isolated in `app`
 * while letting `shared` Android implementations read them safely without hardcoded credentials.
 */
object KlassRuntimeConfig {
    var geminiApiKey: String = ""
    var recaptchaSiteKey: String = ""
    var firestoreDatabaseId: String = ""
    var appUpdateUrl: String = ""
}
