package com.mpi.klass

/**
 * Bridge between the Compose UI and the Swift/GoogleSignIn-iOS side.
 *
 * Kotlin requests the native sign-in sheet via [launchSignIn]; Swift presents
 * the Google account picker and hands the outcome back through [completeSignIn].
 * A null [completeSignIn] errorMsg with a null email means "user canceled".
 */
object IosSignInBridge {

    var onSignInRequested: (() -> Unit)? = null
    var onSignOutRequested: (() -> Unit)? = null

    private var pendingCallback: ((email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit)? = null

    fun launchSignIn(callback: (email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit) {
        pendingCallback = callback
        onSignInRequested?.invoke()
    }

    fun completeSignIn(email: String?, name: String?, photoUrl: String?, errorMsg: String?) {
        val callback = pendingCallback
        pendingCallback = null
        callback?.invoke(email, name, photoUrl, errorMsg)
    }

    fun signOut() {
        onSignOutRequested?.invoke()
    }
}
