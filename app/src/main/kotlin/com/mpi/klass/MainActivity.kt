package com.mpi.klass

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.mpi.klass.theme.initThemeContext
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val AUTH_TAG = "KlassAuth"
private const val UPDATE_TAG = "KlassUpdate"
private const val LATEST_RELEASE_URL = "https://api.github.com/repos/rofiqmpi/Klass/releases/latest"

class MainActivity : ComponentActivity() {

    private lateinit var credentialManager: CredentialManager
    private var pendingUpdateUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        KlassRuntimeConfig.geminiApiKey = BuildConfig.GEMINI_API_KEY.trim().trim('"').trim()
        KlassRuntimeConfig.recaptchaSiteKey = BuildConfig.RECAPTCHA_SITE_KEY.trim().trim('"').trim()
        KlassRuntimeConfig.firestoreDatabaseId = BuildConfig.FIRESTORE_DATABASE_ID.trim().trim('"').trim()
        KlassRuntimeConfig.appUpdateUrl = BuildConfig.APP_UPDATE_URL.trim().trim('"').trim()

        initThemeContext(this)
        AppStrings.preloadCacheSync()
        applyAppLocale(this, loadAppLanguage())
        installSplashScreen()
        enableEdgeToEdge()

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        registerAppCheckDiagnostics()
        ensurePushNotificationChannels(this)

        credentialManager = CredentialManager.create(this)

        setContent {
            App(
                onLaunchGoogleSignIn = { callback ->
                    launchGoogleSignIn(callback)
                },
                onSignOutGoogle = {
                    signOutGoogle()
                },
                onCloseApp = { finish() },
            )
        }
        if (!BuildConfig.DEBUG) checkForReleaseUpdate()
    }

    override fun onResume() {
        super.onResume()
        val url = pendingUpdateUrl
        if (url != null) {
            pendingUpdateUrl = null
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()) {
                downloadAndInstallUpdate(url)
            } else {
                getSharedPreferences("klass_update", Context.MODE_PRIVATE)
                    .edit()
                    .remove("dismissed_tag")
                    .remove("dismissed_at")
                    .apply()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
    }

    private fun launchGoogleSignIn(
        callback: (email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit,
    ) {
        val clientId = try {
            getString(R.string.default_web_client_id).trim()
        } catch (e: Exception) {
            Log.e(AUTH_TAG, "Missing R.string.default_web_client_id", e)
            ""
        }

        if (clientId.isBlank()) {
            callback(
                null,
                null,
                null,
                "Google Sign-In configuration missing: default_web_client_id not found",
            )
            return
        }

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request,
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user ?: Firebase.auth.currentUser
                    val email = user?.email?.takeIf { it.isNotBlank() }
                        ?: googleIdTokenCredential.id.takeIf { it.contains("@") }
                    if (email.isNullOrBlank()) {
                        callback(null, null, null, "Google account email not returned")
                        return@launch
                    }
                    val name = user?.displayName?.takeIf { it.isNotBlank() }
                        ?: googleIdTokenCredential.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@")
                    val photoUrl = user?.photoUrl?.toString()?.takeIf { it.isNotBlank() }
                        ?: googleIdTokenCredential.profilePictureUri?.toString()?.takeIf { it.isNotBlank() }
                    callback(email, name, photoUrl, null)
                } else {
                    Log.e(AUTH_TAG, "Unexpected credential type: ${credential.type}")
                    callback(null, null, null, "Unexpected credential type: ${credential.type}")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(AUTH_TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                callback(null, null, null, null)
            } catch (e: NoCredentialException) {
                Log.w(AUTH_TAG, "No Google credentials available: ${e.message}", e)
                callback(
                    null,
                    null,
                    null,
                    "ডিভাইসে সক্রিয় Google অ্যাকাউন্ট পাওয়া যায়নি। সেটিংস থেকে Google অ্যাকাউন্ট যোগ করে আবার চেষ্টা করুন।",
                )
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(AUTH_TAG, "Failed to parse Google ID token", e)
                callback(null, null, null, e.localizedMessage ?: "Invalid Google ID token")
            } catch (e: GetCredentialException) {
                Log.e(AUTH_TAG, "Google Sign-In credential error: ${e.type} - ${e.message}", e)
                callback(null, null, null, e.localizedMessage ?: e.type)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(AUTH_TAG, "Firebase Google Sign-In failed", e)
                callback(
                    null,
                    null,
                    null,
                    e.localizedMessage ?: "Firebase সাইন-ইন ব্যর্থ হয়েছে".localized(loadAppLanguage()),
                )
            }
        }
    }

    private fun signOutGoogle() {
        lifecycleScope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(AUTH_TAG, "Failed to clear credential state: ${e.message}")
                } finally {
                    try {
                        Firebase.auth.signOut()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun applyAppLocale(context: Context, language: AppLanguage) {
        val tag = if (language == AppLanguage.BENGALI) "bn-BD" else "en-US"
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private data class RemoteReleaseInfo(
        val version: String,
        val apkUrl: String,
        val notes: String = "",
        val force: Boolean = false,
    )

    private fun parseReleaseJsonPayload(payload: String): RemoteReleaseInfo? {
        val json = org.json.JSONObject(payload)
        val tag = json.optString("tag_name")
            .ifBlank { json.optString("version") }
            .ifBlank { json.optString("latest_version") }
            .trim()
        val notes = json.optString("notes")
            .ifBlank { json.optString("body") }
            .trim()
        val force = json.optBoolean("force", false)
        val directApkUrl = json.optString("apk_url")
            .ifBlank { json.optString("download_url") }
            .trim()
        val assetApkUrl = json.optJSONArray("assets")?.let { assets ->
            (0 until assets.length())
                .mapNotNull { assets.optJSONObject(it) }
                .firstOrNull { asset ->
                    val name = asset.optString("name")
                    name == "Klass-release.apk" || name.endsWith(".apk", ignoreCase = true)
                }
                ?.optString("browser_download_url")
                ?.trim()
        }.orEmpty()
        val finalUrl = directApkUrl.ifBlank { assetApkUrl }
        return if (tag.isBlank() || finalUrl.isBlank()) null else RemoteReleaseInfo(tag, finalUrl, notes, force)
    }

    private fun fetchReleaseFromHttp(feedUrl: String): RemoteReleaseInfo? {
        return try {
            val connection = (URL(feedUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json, application/json")
                setRequestProperty("User-Agent", "Klass-Android")
            }
            try {
                if (connection.responseCode !in 200..299) {
                    Log.w(UPDATE_TAG, "Release check HTTP ${connection.responseCode} for $feedUrl")
                    return null
                }
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                parseReleaseJsonPayload(payload)
            } finally {
                connection.disconnect()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(UPDATE_TAG, "Could not fetch release feed ($feedUrl): ${e.message}")
            null
        }
    }

    private suspend fun fetchReleaseFromFirestoreConfig(): RemoteReleaseInfo? {
        for ((db, label) in FirestoreResolver.getFirestoreInstances()) {
            val info = kotlinx.coroutines.withTimeoutOrNull(4_500L) {
                try {
                    val doc = db.collection("config").document("app_update").get().await()
                    if (!doc.exists()) return@withTimeoutOrNull null
                    val enabled = doc.getBoolean("enabled") ?: true
                    if (!enabled) return@withTimeoutOrNull null
                    val version = (doc.getString("latest_version")
                        ?: doc.getString("version")
                        ?: doc.getString("versionName")
                        ?: doc.getString("tag")
                        ?: doc.getString("tag_name")).orEmpty().trim()
                    val apkUrl = (doc.getString("apk_url")
                        ?: doc.getString("download_url")
                        ?: doc.getString("apkUrl")
                        ?: doc.getString("downloadUrl")).orEmpty().trim()
                    val notes = doc.getString("notes").orEmpty().trim()
                    val force = doc.getBoolean("force") ?: false
                    if (version.isNotBlank() && apkUrl.isNotBlank()) {
                        FirestoreResolver.markWorkingDatabase(label)
                        RemoteReleaseInfo(version, apkUrl, notes, force)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    null
                }
            }
            if (info != null) return info
        }
        return null
    }

    private fun checkForReleaseUpdate() {
        lifecycleScope.launch {
            val release = withContext(Dispatchers.IO) {
                val customUrl = KlassRuntimeConfig.appUpdateUrl.trim()
                if (customUrl.isNotBlank()) {
                    fetchReleaseFromHttp(customUrl)?.let { return@withContext it }
                }
                fetchReleaseFromFirestoreConfig()?.let { return@withContext it }
                fetchReleaseFromHttp(LATEST_RELEASE_URL)
            } ?: return@launch

            val installedPackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            val installedVersion = installedPackageInfo.versionName.orEmpty()
            if (compareVersions(release.version, installedVersion) <= 0) return@launch

            val preferences = getSharedPreferences("klass_update", Context.MODE_PRIVATE)
            val dismissedTag = preferences.getString("dismissed_tag", null)
            val dismissedAt = preferences.getLong("dismissed_at", 0L)
            val withinCooldown = dismissedTag == release.version &&
                (System.currentTimeMillis() - dismissedAt) < 24L * 60L * 60L * 1000L
            if (!release.force && withinCooldown) return@launch

            val isBn = loadAppLanguage() == AppLanguage.BENGALI
            val title = if (isBn) "নতুন আপডেট পাওয়া গেছে" else "Klass update available"
            val baseMessage = if (isBn) {
                "নতুন ভার্সন ${release.version} প্রস্তুত। এখনই ডাউনলোড ও ইনস্টল করতে চান?"
            } else {
                "Version ${release.version} is ready. Download and install it now?"
            }
            val message = if (release.notes.isNotBlank()) {
                "$baseMessage\n\n${release.notes.take(260)}"
            } else {
                baseMessage
            }

            AlertDialog.Builder(this@MainActivity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(!release.force)
                .apply {
                    if (!release.force) {
                        setNegativeButton(if (isBn) "পরে" else "Later") { _, _ ->
                            preferences.edit()
                                .putString("dismissed_tag", release.version)
                                .putLong("dismissed_at", System.currentTimeMillis())
                                .apply()
                        }
                        setOnCancelListener {
                            preferences.edit()
                                .putString("dismissed_tag", release.version)
                                .putLong("dismissed_at", System.currentTimeMillis())
                                .apply()
                        }
                    }
                }
                .setPositiveButton(if (isBn) "আপডেট করুন" else "Update") { _, _ ->
                    preferences.edit()
                        .remove("dismissed_tag")
                        .remove("dismissed_at")
                        .apply()
                    beginUpdateInstall(release.apkUrl)
                }
                .show()
        }
    }

    private fun beginUpdateInstall(downloadUrl: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            pendingUpdateUrl = downloadUrl
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName"),
                ),
            )
            return
        }
        downloadAndInstallUpdate(downloadUrl)
    }

    private fun downloadAndInstallUpdate(downloadUrl: String) {
        val isBn = loadAppLanguage() == AppLanguage.BENGALI
        Toast.makeText(
            this@MainActivity,
            if (isBn) "আপডেট ডাউনলোড হচ্ছে…" else "Downloading update…",
            Toast.LENGTH_SHORT,
        ).show()

        lifecycleScope.launch {
            try {
                val apk = withContext(Dispatchers.IO) {
                    var currentUrl = downloadUrl
                    var redirects = 0
                    var activeConnection: HttpURLConnection? = null
                    while (redirects < 6) {
                        val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                            connectTimeout = 15_000
                            readTimeout = 45_000
                            instanceFollowRedirects = false
                            requestMethod = "GET"
                            setRequestProperty("User-Agent", "Klass-Android")
                        }
                        val code = conn.responseCode
                        if (code in listOf(301, 302, 303, 307, 308)) {
                            val location = conn.getHeaderField("Location").orEmpty()
                            conn.disconnect()
                            if (location.isBlank()) error("Empty redirect location")
                            currentUrl = URL(URL(currentUrl), location).toString()
                            redirects++
                            continue
                        }
                        activeConnection = conn
                        break
                    }

                    val connection = activeConnection ?: error("Too many redirects while downloading update")
                    try {
                        if (connection.responseCode !in 200..299) {
                            error("APK download returned HTTP ${connection.responseCode}")
                        }
                        val directory = File(cacheDir, "updates")
                        if (!directory.exists() && !directory.mkdirs()) {
                            error("Could not create update cache")
                        }
                        val outputFile = File(directory, "Klass-release.apk")
                        connection.inputStream.use { input ->
                            FileOutputStream(outputFile).use { output -> input.copyTo(output) }
                        }
                        if (!outputFile.exists() || outputFile.length() == 0L) {
                            error("Downloaded APK file is empty")
                        }
                        outputFile
                    } finally {
                        connection.disconnect()
                    }
                }
                val apkUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "$packageName.fileprovider",
                    apk,
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(installIntent)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(UPDATE_TAG, "Failed to download or open the update APK", e)
                getSharedPreferences("klass_update", Context.MODE_PRIVATE)
                    .edit()
                    .remove("dismissed_tag")
                    .remove("dismissed_at")
                    .apply()
                Toast.makeText(
                    this@MainActivity,
                    if (isBn) "আপডেট ডাউনলোড ব্যর্থ হয়েছে। পরে আবার চেষ্টা করুন।" else "Update download failed. Please try again later.",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun compareVersions(remoteTag: String, installedVersion: String): Int {
        fun numbers(value: String) = Regex("\\d+").findAll(value).map { it.value.toLongOrNull() ?: 0L }.toList()
        val remote = numbers(remoteTag)
        val installed = numbers(installedVersion)
        for (index in 0 until maxOf(remote.size, installed.size)) {
            val comparison = (remote.getOrElse(index) { 0L }).compareTo(installed.getOrElse(index) { 0L })
            if (comparison != 0) return comparison
        }
        return 0
    }
}

private const val APPCHECK_TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val TOKEN_STORE_PREFIX = "com.google.firebase.appcheck.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val LEGACY_POISONED_DEBUG_SECRET = "b81a2f64-9d18-4e5c-a352-78d91f42e316"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

private fun registerAppCheckDiagnostics() {
    ZedAppCheckDiagnostics.register {
        try {
            val tokenResult = FirebaseAppCheck.getInstance().getAppCheckToken(false).await()
            if (tokenResult.token.isBlank()) "Empty App Check token returned." else null
        } catch (e: Exception) {
            e.localizedMessage?.takeIf { it.isNotBlank() }
                ?: e::class.simpleName
                ?: "App Check token retrieval failed."
        }
    }
}

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (BuildConfig.DEBUG) {
            val persistenceKey = FirebaseApp.getInstance().persistenceKey
            val prefs = context.getSharedPreferences(
                "$DEBUG_STORE_PREFIX$persistenceKey",
                Context.MODE_PRIVATE,
            )
            val tokenStorePrefs = context.getSharedPreferences(
                "$TOKEN_STORE_PREFIX$persistenceKey",
                Context.MODE_PRIVATE,
            )
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)?.trim()
            val existing = prefs.getString(DEBUG_SECRET_KEY, null)
            if (!debugToken.isNullOrEmpty()) {
                if (existing != debugToken) {
                    prefs.edit()
                        .putString(DEBUG_SECRET_KEY, debugToken)
                        .commit()
                    tokenStorePrefs.edit().clear().commit()
                    Log.d(APPCHECK_TAG, "App Check debug secret updated cleanly.")
                } else {
                    Log.d(APPCHECK_TAG, "App Check debug secret already configured.")
                }
            } else {
                if (existing == LEGACY_POISONED_DEBUG_SECRET) {
                    prefs.edit().remove(DEBUG_SECRET_KEY).commit()
                    tokenStorePrefs.edit().clear().commit()
                    Log.w(APPCHECK_TAG, "Cleared legacy hardcoded App Check debug secret.")
                } else {
                    Log.w(APPCHECK_TAG, "No debug token found in Intent extra.")
                }
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance(),
            )
            Log.d(APPCHECK_TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val projectId = runCatching { FirebaseApp.getInstance().options.projectId?.trim().orEmpty() }.getOrDefault("")
            val secretSiteKey = KlassRuntimeConfig.recaptchaSiteKey.trim()
            val resourceSiteKey = if (projectId.isEmpty() || projectId == AI_STUDIO_FIREBASE_PROJECT_ID) {
                val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
                if (resId != 0) context.getString(resId).trim() else ""
            } else {
                ""
            }
            val siteKey = secretSiteKey.ifBlank { resourceSiteKey }
            if (siteKey.isNotBlank()) {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(siteKey),
                )
                Log.d(APPCHECK_TAG, "RecaptchaAppCheckProviderFactory installed.")
            } else {
                Log.w(APPCHECK_TAG, "No matching RECAPTCHA_SITE_KEY configured for project '$projectId'; skipping Recaptcha provider.")
            }
        }
    } catch (e: Exception) {
        Log.e(APPCHECK_TAG, "Failed to initialize Firebase App Check", e)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
