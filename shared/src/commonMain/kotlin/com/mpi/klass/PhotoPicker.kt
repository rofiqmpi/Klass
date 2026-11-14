package com.mpi.klass

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPickerLauncher(onPhotoPicked: (String) -> Unit): () -> Unit

expect fun resolveProfileImageModel(photoUrl: String?): Any?

@Composable
expect fun rememberDatePickerLauncher(initialDate: String, onDatePicked: (String) -> Unit): () -> Unit
