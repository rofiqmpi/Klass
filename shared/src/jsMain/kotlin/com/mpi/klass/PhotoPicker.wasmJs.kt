package com.mpi.klass

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (String?) -> Unit): () -> Unit {
    return {
        onPhotoPicked(null)
    }
}
