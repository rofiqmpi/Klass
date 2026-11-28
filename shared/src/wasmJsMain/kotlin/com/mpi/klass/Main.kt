package com.mpi.klass

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("klassApp") {
        val scope = rememberCoroutineScope()
        App(
            onLaunchGoogleSignIn = { callback ->
                scope.launch {
                    val res = WebFirebaseBridge.signInWithGoogle()
                    when {
                        res == null -> callback(null, null, null, "Firebase Web কনফিগারেশন লোড হয়নি।")
                        res.optBool("ok", false) -> callback(
                            res.optNullableStr("email"),
                            res.optNullableStr("name"),
                            res.optNullableStr("photoUrl"),
                            null,
                        )
                        else -> callback(
                            null,
                            null,
                            null,
                            res.optStr("error", "Google সাইন-ইন ব্যর্থ হয়েছে।"),
                        )
                    }
                }
            },
            onSignOutGoogle = {
                scope.launch {
                    WebFirebaseBridge.signOutGoogle()
                }
            },
        )
    }
}
