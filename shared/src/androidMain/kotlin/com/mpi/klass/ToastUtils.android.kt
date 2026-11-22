package com.mpi.klass

import android.os.Handler
import android.os.Looper

private val mainToastHandler = Handler(Looper.getMainLooper())

actual fun showToast(message: String) {
    val clean = message.trim()
    if (clean.isBlank()) return
    if (Looper.myLooper() == Looper.getMainLooper()) {
        AppToastState.show(clean)
    } else {
        mainToastHandler.post {
            AppToastState.show(clean)
        }
    }
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
