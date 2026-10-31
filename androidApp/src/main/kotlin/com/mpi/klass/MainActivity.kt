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
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                val email = user?.email ?: account.email ?: "${user?.uid}@firebase.user"
                                val name = user?.displayName ?: account.displayName
                                val photoUrl = user?.photoUrl?.toString() ?: account.photoUrl?.toString()
                                callback?.invoke(email, name, photoUrl, null)
                            } else {
                                handleFallbackSignIn(account, callback)
                            }
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
        val email = account?.email
        val name = account?.displayName
        val photoUrl = account?.photoUrl?.toString()

        if (email == null) {
            callback?.invoke(null, null, null, "গুগল অ্যাকাউন্ট ইমেইল পাওয়া যায়নি")
            return
        }

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { _ ->
                callback?.invoke(email, name, photoUrl, null)
            }
        } else {
            callback?.invoke(email, name, photoUrl, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setBengaliLocale(this)
        initThemeContext(this)
        installSplashScreen()
        enableEdgeToEdge()

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        val webClientResId = resources.getIdentifier("default_web_client_id", "string", packageName)
        val webClientId = if (webClientResId != 0) {
            getString(webClientResId)
        } else {
            "931767497129"
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            App(
                onLaunchGoogleSignIn = { callback ->
                    launchGoogleSignIn(callback)
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

    private fun setBengaliLocale(context: Context) {
        val locale = Locale.forLanguageTag("bn-BD")
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
