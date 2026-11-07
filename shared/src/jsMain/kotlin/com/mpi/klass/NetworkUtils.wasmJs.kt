package com.mpi.klass

import kotlinx.browser.window

actual suspend fun isNetworkAvailable(): Boolean {
    return try {
        window.navigator.onLine
    } catch (_: Throwable) {
        true
    }
}
