package com.mpi.klass

import android.widget.Toast
import com.mpi.klass.theme.getAppContext

actual fun showToast(message: String) {
    getAppContext()?.let { ctx ->
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
        }
    }
}

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
