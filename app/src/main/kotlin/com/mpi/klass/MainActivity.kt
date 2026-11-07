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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
                                    callback?.invoke(null, null, null, authTask.exception?.localizedMessage ?: "Firebase sign-in failed")
                                }
                            }
                    } catch (exception: Exception) {
                        callback?.invoke(null, null, null, exception.localizedMessage ?: "Firebase sign-in failed")
                    }
                } else {
                    callback?.invoke(null, null, null, "Google did not return an ID token")
                }
            } catch (e: ApiException) {
                callback?.invoke(null, null, null, e.localizedMessage ?: "গুগল সাইন-ইন ব্যর্থ হয়েছে")
            }
        } else {
            callback?.invoke(null, null, null, null) // Cancelled by user
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
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            App(
                onLaunchGoogleSignIn = { callback ->
                    launchGoogleSignIn(callback)
                },
                onLaunchEmailSignIn = { email, password, callback ->
                    launchEmailSignIn(email, password, callback)
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

    private fun launchEmailSignIn(
        email: String,
        password: String,
        callback: (email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit,
    ) {
        try {
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    callback(
                        user?.email ?: email,
                        user?.displayName ?: email.substringBefore("@"),
                        user?.photoUrl?.toString(),
                        null,
                    )
                }
                .addOnFailureListener {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            callback(
                                user?.email ?: email,
                                user?.displayName ?: email.substringBefore("@"),
                                user?.photoUrl?.toString(),
                                null,
                            )
                        }
                        .addOnFailureListener { error ->
                            callback(null, null, null, error.localizedMessage ?: "ইমেইল সাইন-ইন ব্যর্থ হয়েছে")
                        }
                }
        } catch (error: Exception) {
            callback(null, null, null, error.localizedMessage ?: "Firebase কনফিগারেশন অনুপস্থিত")
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
