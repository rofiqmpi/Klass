package com.mpi.klass.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
expect fun getHindSiliguriFontFamily(): FontFamily

@Composable
fun getAppTypography(): Typography {
    return Typography()
}
