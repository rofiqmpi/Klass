package com.mpi.klass

import androidx.compose.runtime.Composable

@Composable
expect fun rememberLocationPermissionLauncher(onLocationFetched: (String) -> Unit): () -> Unit
