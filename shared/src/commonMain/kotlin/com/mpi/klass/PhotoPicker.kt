package com.mpi.klass

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit
