package com.mpi.klass

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val AUTH_TAG = "KlassAuth"

class MainActivity : ComponentActivity() {

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        initThemeContext(this)
        AppStrings.preloadCacheSync()
        applyAppLocale(this, loadAppLanguage())
        installSplashScreen()
        enableEdgeToEdge()

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        ZedAiKeyProvider.setApiKey(BuildConfig.GEMINI_API_KEY)
        configureAppCheck(this, intent)
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
}

private const val APPCHECK_TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

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
            val intentToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            val tokenToUse = if (!intentToken.isNullOrBlank()) intentToken else APP_CHECK_DEBUG_TOKEN
            prefs.edit()
                .putString(DEBUG_SECRET_KEY, tokenToUse)
                .commit()
            Log.d(APPCHECK_TAG, "App Check debug secret configured: $tokenToUse")

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance(),
            )
            Log.d(APPCHECK_TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey),
                    )
                    Log.d(APPCHECK_TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(APPCHECK_TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(APPCHECK_TAG, "recaptcha_site_key string resource not found in package.")
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
