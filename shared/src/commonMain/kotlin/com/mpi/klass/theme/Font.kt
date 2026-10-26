package com.mpi.klass.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
expect fun getHindSiliguriFontFamily(): FontFamily

@Composable
fun getAppTypography(): Typography {
    val hindSiliguri = getHindSiliguriFontFamily()
    val defaultTypography = Typography()

    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = hindSiliguri),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = hindSiliguri),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = hindSiliguri),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = hindSiliguri),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = hindSiliguri),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = hindSiliguri),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = hindSiliguri),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = hindSiliguri),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = hindSiliguri),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = hindSiliguri),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = hindSiliguri),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = hindSiliguri),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = hindSiliguri),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = hindSiliguri),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = hindSiliguri),
    )
}
