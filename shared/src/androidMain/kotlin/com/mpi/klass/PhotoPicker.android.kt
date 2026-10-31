package com.mpi.klass

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        onPhotoPicked(uri?.toString())
    }
    return remember(launcher) {
        { launcher.launch("image/*") }
    }
}
