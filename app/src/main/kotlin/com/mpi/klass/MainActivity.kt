package com.mpi.klass

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.mpi.klass.theme.initThemeContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val AUTH_TAG = "KlassAuth"
private const val UPDATE_TAG = "KlassUpdate"
private const val LATEST_RELEASE_URL = "https://api.github.com/repos/rofiqmpi/Klass/releases/latest"

class MainActivity : ComponentActivity() {

    private lateinit var credentialManager: CredentialManager
    private var pendingUpdateRelease: RemoteReleaseInfo? = null
    private var updateUiState by mutableStateOf<InAppUpdateUiState>(InAppUpdateUiState.Hidden)
    private var updateDownloadJob: Job? = null
    private var lastAutoUpdateCheckAtMillis: Long = 0L
    private var isCheckingUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        KlassRuntimeConfig.geminiApiKey = BuildConfig.GEMINI_API_KEY.trim().trim('"').trim()
        KlassRuntimeConfig.recaptchaSiteKey = BuildConfig.RECAPTCHA_SITE_KEY.trim().trim('"').trim()
        KlassRuntimeConfig.firestoreDatabaseId = BuildConfig.FIRESTORE_DATABASE_ID.trim().trim('"').trim()
        KlassRuntimeConfig.appUpdateUrl = BuildConfig.APP_UPDATE_URL.trim().trim('"').trim()
        KlassRuntimeConfig.installedVersionName = currentInstalledVersionName()
        KlassRuntimeConfig.onManualCheckForUpdate = { checkForReleaseUpdate(manual = true) }

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
            InAppUpdateOverlay(
                state = updateUiState,
                onLater = { release ->
                    dismissReleasePromptForCooldown(release)
                    updateUiState = InAppUpdateUiState.Hidden
                },
                onStartDownload = { release ->
                    clearReleasePromptCooldown()
                    beginUpdateInstall(release)
                },
                onOpenInstallSettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:$packageName"),
                            ),
                        )
                    }
                },
                onCancelDownload = { release ->
                    updateDownloadJob?.cancel()
                    updateDownloadJob = null
                    if (release.force) {
                        updateUiState = InAppUpdateUiState.Available(release, currentInstalledVersionName())
                    } else {
                        updateUiState = InAppUpdateUiState.Hidden
                    }
                },
                onInstallNow = { release ->
                    launchCachedApkInstaller(release)
                },
                onDismiss = {
                    updateUiState = InAppUpdateUiState.Hidden
                },
            )
        }
        checkForReleaseUpdate(manual = false)
    }

    override fun onStart() {
        super.onStart()
        val shouldAutoCheck = !BuildConfig.DEBUG || BuildConfig.VERSION_NAME != "1.0"
        val now = System.currentTimeMillis()
        if (shouldAutoCheck &&
            updateUiState is InAppUpdateUiState.Hidden &&
            now - lastAutoUpdateCheckAtMillis > 30L * 60L * 1000L
        ) {
            checkForReleaseUpdate(manual = false)
        }
    }

    override fun onResume() {
        super.onResume()
        val release = pendingUpdateRelease
        if (release != null &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls())
        ) {
            pendingUpdateRelease = null
            downloadAndInstallUpdate(release)
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
                val detail = e.localizedMessage?.trim()?.takeIf { it.isNotBlank() }
                val msg = if (detail != null) {
                    "Google সাইন-ইন পাওয়া যায়নি ($detail)। ফোনে Google অ্যাকাউন্ট লগইন আছে কিনা এবং Firebase Console-এ APK-এর SHA-1 ও Google Sign-In যুক্ত আছে কিনা যাচাই করুন।"
                } else {
                    "ডিভাইসে সক্রিয় Google অ্যাকাউন্ট পাওয়া যায়নি অথবা Firebase-এ এই APK-এর SHA-1 যুক্ত নেই।"
                }
                callback(null, null, null, msg)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(AUTH_TAG, "Failed to parse Google ID token", e)
                callback(null, null, null, e.localizedMessage ?: "Invalid Google ID token")
            } catch (e: GetCredentialException) {
                Log.e(AUTH_TAG, "Google Sign-In credential error: ${e.type} - ${e.message}", e)
                val detail = e.localizedMessage?.trim()?.takeIf { it.isNotBlank() } ?: e.type
                callback(
                    null,
                    null,
                    null,
                    "Google Credential ত্রুটি ($detail)। Firebase Project Settings-এ package 'com.klass.mpi'-এর SHA-1 এবং Google provider যাচাই করুন।",
                )
            } catch (e: FirebaseAuthException) {
                Log.e(AUTH_TAG, "FirebaseAuth error [${e.errorCode}]: ${e.message}", e)
                val msg = when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" ->
                        "Firebase Console > Authentication > Sign-in method থেকে Google Sign-In চালু (Enable) করা নেই।"
                    "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_CUSTOM_TOKEN" ->
                        "Google ID টোকেন বা Web Client ID মিলছে না। Firebase Console থেকে নতুন google-services.json আপডেট করুন।"
                    else -> e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "Firebase Auth ত্রুটি (${e.errorCode})"
                }
                callback(null, null, null, msg)
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

    private fun parseReleaseJsonPayload(payload: String): RemoteReleaseInfo? {
        val json = org.json.JSONObject(payload)
        val tag = json.optString("tag_name")
            .ifBlank { json.optString("version") }
            .ifBlank { json.optString("versionName") }
            .ifBlank { json.optString("latest_version") }
            .ifBlank { json.optString("tag") }
            .trim()
        val notes = json.optString("notes")
            .ifBlank { json.optString("body") }
            .trim()
        val force = json.optBoolean("force", false)
        val directApkUrl = json.optString("apk_url")
            .ifBlank { json.optString("download_url") }
            .ifBlank { json.optString("apkUrl") }
            .ifBlank { json.optString("downloadUrl") }
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
        val finalUrl = KlassUpdateUtils.normalizeApkDownloadUrl(directApkUrl.ifBlank { assetApkUrl })
        return if (tag.isBlank() || finalUrl.isBlank()) null else RemoteReleaseInfo(tag, finalUrl, notes, force)
    }

    private fun fetchReleaseFromHttp(rawFeedUrl: String): RemoteReleaseInfo? {
        val feedUrl = KlassUpdateUtils.normalizeUpdateFeedUrl(rawFeedUrl)
        if (feedUrl.isBlank()) return null
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

    private data class FirestoreReleaseFetchResult(
        val release: RemoteReleaseInfo?,
        val explicitlyDisabled: Boolean = false,
    )

    private suspend fun fetchReleaseFromFirestoreConfig(): FirestoreReleaseFetchResult {
        for ((db, label) in FirestoreResolver.getFirestoreInstances()) {
            val result = kotlinx.coroutines.withTimeoutOrNull(4_500L) {
                try {
                    val doc = db.collection("config").document("app_update").get().await()
                    if (!doc.exists()) return@withTimeoutOrNull null
                    val enabled = doc.getBoolean("enabled") ?: true
                    if (!enabled) {
                        FirestoreResolver.markWorkingDatabase(label)
                        return@withTimeoutOrNull FirestoreReleaseFetchResult(null, explicitlyDisabled = true)
                    }
                    val version = (doc.getString("latest_version")
                        ?: doc.getString("version")
                        ?: doc.getString("versionName")
                        ?: doc.getString("tag")
                        ?: doc.getString("tag_name")).orEmpty().trim()
                    val rawApkUrl = (doc.getString("apk_url")
                        ?: doc.getString("download_url")
                        ?: doc.getString("apkUrl")
                        ?: doc.getString("downloadUrl")).orEmpty().trim()
                    val apkUrl = KlassUpdateUtils.normalizeApkDownloadUrl(rawApkUrl)
                    val notes = doc.getString("notes").orEmpty().trim()
                    val force = doc.getBoolean("force") ?: false
                    if (version.isNotBlank() && apkUrl.isNotBlank()) {
                        FirestoreResolver.markWorkingDatabase(label)
                        FirestoreReleaseFetchResult(RemoteReleaseInfo(version, apkUrl, notes, force), false)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    null
                }
            }
            if (result != null) return result
        }
        return FirestoreReleaseFetchResult(null, explicitlyDisabled = false)
    }

    private fun currentInstalledVersionName(): String {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        return info.versionName.orEmpty()
    }

    private fun dismissReleasePromptForCooldown(release: RemoteReleaseInfo) {
        getSharedPreferences("klass_update", Context.MODE_PRIVATE)
            .edit()
            .putString("dismissed_tag", release.version)
            .putLong("dismissed_at", System.currentTimeMillis())
            .apply()
    }

    private fun clearReleasePromptCooldown() {
        getSharedPreferences("klass_update", Context.MODE_PRIVATE)
            .edit()
            .remove("dismissed_tag")
            .remove("dismissed_at")
            .apply()
    }

    private fun checkForReleaseUpdate(manual: Boolean = false) {
        if (isCheckingUpdate) return
        isCheckingUpdate = true
        lastAutoUpdateCheckAtMillis = System.currentTimeMillis()
        val isBn = loadAppLanguage() == AppLanguage.BENGALI
        if (manual) {
            showToast(if (isBn) "আপডেট যাচাই করা হচ্ছে..." else "Checking for updates...")
        }
        lifecycleScope.launch {
            try {
                val (firestoreResult, httpRelease) = withContext(Dispatchers.IO) {
                    val fsResult = fetchReleaseFromFirestoreConfig()
                    val customUrl = KlassRuntimeConfig.appUpdateUrl.trim()
                    val feedCandidate = if (customUrl.isNotBlank()) {
                        fetchReleaseFromHttp(customUrl)
                    } else {
                        fetchReleaseFromHttp(LATEST_RELEASE_URL)
                    }
                    fsResult to feedCandidate
                }

                if (!manual && firestoreResult.explicitlyDisabled) return@launch

                val fsRelease = firestoreResult.release
                val release = when {
                    fsRelease != null && httpRelease != null -> {
                        if (KlassUpdateUtils.compareSemanticVersions(fsRelease.version, httpRelease.version) >= 0) {
                            fsRelease
                        } else {
                            httpRelease
                        }
                    }
                    fsRelease != null -> fsRelease
                    else -> httpRelease
                }

                val installedVersion = currentInstalledVersionName()
                if (release == null) {
                    if (manual) {
                        showToast(
                            if (isBn) {
                                "কোন রিলিজ বা আপডেট ফিড পাওয়া যায়নি। গিটহাবে প্রথম রিলিজ পুশ করুন অথবা অ্যাডমিন প্যানেল থেকে লিংক দিন।"
                            } else {
                                "No release or update feed found. Push your first release to GitHub or configure update feed in Admin Panel."
                            },
                        )
                    }
                    return@launch
                }

                if (KlassUpdateUtils.compareSemanticVersions(release.version, installedVersion) <= 0) {
                    if (manual) {
                        showToast(
                            if (isBn) {
                                "আপনার অ্যাপটি সর্বশেষ ভার্সনে (v$installedVersion) আছে।"
                            } else {
                                "You are already on the latest version (v$installedVersion)."
                            },
                        )
                    }
                    return@launch
                }

                if (manual) {
                    clearReleasePromptCooldown()
                } else {
                    val preferences = getSharedPreferences("klass_update", Context.MODE_PRIVATE)
                    val dismissedTag = preferences.getString("dismissed_tag", null)
                    val dismissedAt = preferences.getLong("dismissed_at", 0L)
                    val withinCooldown = dismissedTag == release.version &&
                        (System.currentTimeMillis() - dismissedAt) < 24L * 60L * 60L * 1000L
                    if (!release.force && withinCooldown) return@launch
                }

                if (manual || updateUiState is InAppUpdateUiState.Hidden) {
                    updateUiState = InAppUpdateUiState.Available(
                        release = release,
                        installedVersion = installedVersion,
                    )
                }
            } finally {
                isCheckingUpdate = false
            }
        }
    }

    private fun beginUpdateInstall(release: RemoteReleaseInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            pendingUpdateRelease = release
            updateUiState = InAppUpdateUiState.PermissionRequired(release)
            return
        }
        downloadAndInstallUpdate(release)
    }

    private fun cachedApkFileFor(release: RemoteReleaseInfo): File {
        val directory = File(cacheDir, "updates")
        if (!directory.exists()) directory.mkdirs()
        val safeVersion = release.version.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(directory, "Klass-release-$safeVersion.apk")
    }

    private fun isValidApkFile(file: File): Boolean {
        if (!file.exists() || file.length() < 64_000L) return false
        return try {
            FileInputStream(file).use { fis ->
                val b0 = fis.read()
                val b1 = fis.read()
                b0 == 0x50 && b1 == 0x4B // 'P', 'K' ZIP/APK signature
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun downloadAndInstallUpdate(release: RemoteReleaseInfo) {
        val cachedApk = cachedApkFileFor(release)
        if (isValidApkFile(cachedApk)) {
            updateUiState = InAppUpdateUiState.ReadyToInstall(release)
            launchApkFileInstaller(cachedApk)
            return
        }

        val isBn = loadAppLanguage() == AppLanguage.BENGALI
        updateDownloadJob?.cancel()
        updateUiState = InAppUpdateUiState.Downloading(
            release = release,
            downloadedBytes = 0L,
            totalBytes = -1L,
        )

        updateDownloadJob = lifecycleScope.launch {
            val tmpFile = File(cachedApk.parentFile, "${cachedApk.name}.tmp")
            try {
                val apk = withContext(Dispatchers.IO) {
                    var currentUrl = KlassUpdateUtils.normalizeApkDownloadUrl(release.apkUrl)
                    var redirects = 0
                    var activeConnection: HttpURLConnection? = null
                    while (redirects < 6) {
                        val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                            connectTimeout = 15_000
                            readTimeout = 45_000
                            instanceFollowRedirects = false
                            requestMethod = "GET"
                            setRequestProperty("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
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
                        val responseCode = connection.responseCode
                        if (responseCode !in 200..299) {
                            if (responseCode == 401 || responseCode == 403 || responseCode == 404) {
                                error(
                                    if (isBn) {
                                        "ডাউনলোড লিংক অ্যাক্সেস করা যাচ্ছে না (HTTP $responseCode)। প্রাইভেট GitHub রিপোজিটরির লিংক কাজ করে না—পাবলিক রিলিজ লিংক ব্যবহার করুন।"
                                    } else {
                                        "Update APK unreachable (HTTP $responseCode). Private GitHub repo links require a public release URL."
                                    },
                                )
                            }
                            error("HTTP $responseCode")
                        }
                        val totalBytes = connection.contentLengthLong
                        val directory = cachedApk.parentFile ?: File(cacheDir, "updates")
                        if (!directory.exists() && !directory.mkdirs()) {
                            error("Could not create update cache directory")
                        }
                        // Remove older cached APKs so cache stays compact
                        directory.listFiles()?.forEach { oldFile ->
                            if (oldFile.name != cachedApk.name && oldFile.name != tmpFile.name) {
                                oldFile.delete()
                            }
                        }

                        if (tmpFile.exists()) tmpFile.delete()
                        connection.inputStream.use { input ->
                            FileOutputStream(tmpFile).use { output ->
                                val buffer = ByteArray(16 * 1024)
                                var downloaded = 0L
                                var lastUiEmitAt = 0L
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read == -1) break
                                    output.write(buffer, 0, read)
                                    downloaded += read
                                    val now = System.currentTimeMillis()
                                    if (now - lastUiEmitAt >= 80L) {
                                        lastUiEmitAt = now
                                        withContext(Dispatchers.Main) {
                                            updateUiState = InAppUpdateUiState.Downloading(
                                                release = release,
                                                downloadedBytes = downloaded,
                                                totalBytes = totalBytes,
                                            )
                                        }
                                    }
                                }
                                output.flush()
                            }
                        }
                        if (!isValidApkFile(tmpFile)) {
                            tmpFile.delete()
                            error(
                                if (isBn) {
                                    "সঠিক APK ফাইল পাওয়া যায়নি (লিংকটি সরাসরি APK ডাউনলোড করছে না)।"
                                } else {
                                    "Invalid APK payload (the link did not return a direct APK binary)."
                                },
                            )
                        }
                        if (cachedApk.exists()) cachedApk.delete()
                        if (!tmpFile.renameTo(cachedApk)) {
                            tmpFile.copyTo(cachedApk, overwrite = true)
                            tmpFile.delete()
                        }
                        cachedApk
                    } finally {
                        connection.disconnect()
                    }
                }
                updateUiState = InAppUpdateUiState.ReadyToInstall(release)
                launchApkFileInstaller(apk)
            } catch (e: CancellationException) {
                tmpFile.delete()
                throw e
            } catch (e: Exception) {
                tmpFile.delete()
                Log.e(UPDATE_TAG, "Failed to download or open the update APK", e)
                clearReleasePromptCooldown()
                updateUiState = InAppUpdateUiState.Error(
                    release = release,
                    message = e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: if (isBn) "ইন্টারনেট বা ডাউনলোড লিংক পরীক্ষা করে আবার চেষ্টা করুন।" else "Check your internet or download URL and try again.",
                )
            }
        }
    }

    private fun launchCachedApkInstaller(release: RemoteReleaseInfo) {
        val file = cachedApkFileFor(release)
        if (isValidApkFile(file)) {
            launchApkFileInstaller(file)
        } else {
            downloadAndInstallUpdate(release)
        }
    }

    private fun launchApkFileInstaller(apk: File) {
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
