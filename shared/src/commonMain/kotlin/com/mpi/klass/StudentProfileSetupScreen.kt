package com.mpi.klass

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
fun StudentProfileSetupScreen(
    email: String,
    displayName: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSetupComplete: (StudentProfile) -> Unit,
    onSetupAbandoned: () -> Unit,
) {}
