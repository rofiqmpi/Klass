@file:Suppress("DEPRECATION")

package com.mpi.klass

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.mpi.klass.theme.initThemeContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var pendingCallback: ((email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val callback = pendingCallback
        pendingCallback = null

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    try {
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    val email = user?.email ?: account.email ?: "${user?.uid}@google.user"
                                    val name = user?.displayName ?: account.displayName ?: "Google User"
                                    val photoUrl = user?.photoUrl?.toString() ?: account.photoUrl?.toString()
                                    callback?.invoke(email, name, photoUrl, null)
                                } else {
                                    handleFallbackSignIn(account, callback)
                                }
                            }
                    } catch (_: Exception) {
                        handleFallbackSignIn(account, callback)
                    }
                } else {
                    handleFallbackSignIn(account, callback)
                }
            } catch (e: ApiException) {
                callback?.invoke(null, null, null, e.localizedMessage ?: "গুগল সাইন-ইন ব্যর্থ হয়েছে")
            }
        } else {
            callback?.invoke(null, null, null, null) // Cancelled by user
        }
    }

    private fun handleFallbackSignIn(
        account: GoogleSignInAccount?,
        callback: ((email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit)?,
    ) {
        val email = account?.email ?: "google.user@example.com"
        val name = account?.displayName ?: "Google User"
        val photoUrl = account?.photoUrl?.toString()

        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously().addOnCompleteListener { _ ->
                    callback?.invoke(email, name, photoUrl, null)
                }
            } else {
                callback?.invoke(email, name, photoUrl, null)
            }
        } catch (_: Exception) {
            callback?.invoke(email, name, photoUrl, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initThemeContext(this)
        applyAppLocale(this, loadAppLanguage())
        installSplashScreen()
        enableEdgeToEdge()

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        RoleConfig.configureLocalEmulator()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            App(
                onLaunchGoogleSignIn = { callback ->
                    launchGoogleSignIn(callback)
                },
                onLaunchFacebookSignIn = { callback ->
                    launchOAuthSignIn("facebook.com", listOf("email", "public_profile"), callback)
                },
                onLaunchAppleSignIn = { callback ->
                    launchOAuthSignIn("apple.com", listOf("email", "name"), callback)
                },
                onSignOutGoogle = {
                    try {
                        googleSignInClient.signOut()
                        FirebaseAuth.getInstance().signOut()
                    } catch (_: Exception) {}
                },
                onCloseApp = { finish() },
            )
        }
    }

    private fun launchGoogleSignIn(callback: (email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit) {
        pendingCallback = callback
        try {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            pendingCallback = null
            callback(null, null, null, e.localizedMessage ?: "সাইন-ইন শুরু করা যায়নি")
        }
    }

    private fun launchOAuthSignIn(
        providerId: String,
        scopes: List<String>,
        callback: (email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit
    ) {
        try {
            val provider = OAuthProvider.newBuilder(providerId)
            provider.scopes = scopes
            val auth = FirebaseAuth.getInstance()

            val pendingResultTask = auth.pendingAuthResult
            if (pendingResultTask != null) {
                pendingResultTask
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val email = user?.email ?: "user@$providerId"
                        val name = user?.displayName ?: providerId.substringBefore(".").replaceFirstChar { it.uppercase() }
                        val photoUrl = user?.photoUrl?.toString()
                        callback(email, name, photoUrl, null)
                    }
                    .addOnFailureListener { e ->
                        callback(null, null, null, e.localizedMessage ?: "$providerId সাইন-ইন ব্যর্থ হয়েছে")
                    }
            } else {
                auth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val email = user?.email ?: "user@$providerId"
                        val name = user?.displayName ?: providerId.substringBefore(".").replaceFirstChar { it.uppercase() }
                        val photoUrl = user?.photoUrl?.toString()
                        callback(email, name, photoUrl, null)
                    }
                    .addOnFailureListener { e ->
                        callback(null, null, null, e.localizedMessage ?: "$providerId সাইন-ইন ব্যর্থ হয়েছে")
                    }
            }
        } catch (e: Exception) {
            callback(null, null, null, e.localizedMessage ?: "Firebase কনফিগারেশন অনুপস্থিত")
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
