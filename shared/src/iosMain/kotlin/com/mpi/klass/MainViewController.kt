package com.mpi.klass

import androidx.compose.ui.window.ComposeUIViewController
import platform.posix.exit

@Suppress("FunctionName")
fun MainViewController() = ComposeUIViewController {
    App(
        onLaunchGoogleSignIn = { callback -> IosSignInBridge.launchSignIn(callback) },
        onSignOutGoogle = { IosSignInBridge.signOut() },
        // Apple discourages programmatic exit, but this single-view Compose app
        // has no other way to honor the Welcome screen's close button.
        onCloseApp = { exit(0) },
    )
}
