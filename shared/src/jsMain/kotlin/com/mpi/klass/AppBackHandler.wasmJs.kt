package com.mpi.klass

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on Web
}
