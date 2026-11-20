package com.mpi.klass

import android.content.Context
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

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
