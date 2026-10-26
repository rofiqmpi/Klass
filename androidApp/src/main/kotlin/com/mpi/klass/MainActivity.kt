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
import com.mpi.klass.theme.initThemeContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var onAuthResult: ((email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        // Capture and clear first so a crashed flow can never double-deliver.
        val callback = onAuthResult
        onAuthResult = null

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            callback?.invoke(account.email, account.displayName, account.photoUrl?.toString(), null)
        } catch (e: Exception) {
            val errorMsg = if ((e is ApiException) && (e.statusCode == 12501)) {
                null // User canceled sign in dialog
            } else {
                e.localizedMessage ?: "সাইন-ইন বিফল হয়েছে"
            }
            callback?.invoke(null, null, null, errorMsg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setBengaliLocale(this)
        initThemeContext(this)
        installSplashScreen()
        enableEdgeToEdge()

        // Force transparent status and navigation bar
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            App(
                onLaunchGoogleSignIn = { callback ->
                    onAuthResult = callback
                    try {
                        // Clear any cached account so the account picker is always
                        // shown. revokeAccess is deliberately NOT called here — it
                        // deauthorizes the app on every attempt and its failure
                        // path used to leave the loader hanging.
                        googleSignInClient.signOut().addOnCompleteListener {
                            try {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                onAuthResult = null
                                callback(null, null, null, e.localizedMessage ?: "সাইন-ইন শুরু করা যায়নি")
                            }
                        }
                    } catch (e: Exception) {
                        onAuthResult = null
                        callback(null, null, null, e.localizedMessage ?: "সাইন-ইন শুরু করা যায়নি")
                    }
                },
                onSignOutGoogle = {
                    try {
                        googleSignInClient.signOut()
                        googleSignInClient.revokeAccess()
                    } catch (_: Exception) {}
                },
                onCloseApp = { finish() },
            )
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
