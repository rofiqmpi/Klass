package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KlassDarkPrimary,
    onPrimary = KlassDarkOnPrimary,
    primaryContainer = KlassDarkPrimaryContainer,
    onPrimaryContainer = KlassDarkOnPrimaryContainer,
    secondary = KlassDarkSecondary,
    onSecondary = KlassDarkOnSecondary,
    secondaryContainer = KlassDarkSecondaryContainer,
    onSecondaryContainer = KlassDarkOnSecondaryContainer,
    tertiary = KlassDarkTertiary,
    onTertiary = KlassDarkOnTertiary,
    tertiaryContainer = KlassDarkTertiaryContainer,
    onTertiaryContainer = KlassDarkOnTertiaryContainer,
    background = KlassDarkBackground,
    onBackground = KlassDarkOnBackground,
    surface = KlassDarkSurface,
    onSurface = KlassDarkOnSurface,
    surfaceVariant = KlassDarkSurfaceVariant,
    onSurfaceVariant = KlassDarkOnSurfaceVariant,
    outline = KlassDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = KlassPrimary,
    onPrimary = KlassOnPrimary,
    primaryContainer = KlassPrimaryContainer,
    onPrimaryContainer = KlassOnPrimaryContainer,
    secondary = KlassSecondary,
    onSecondary = KlassOnSecondary,
    secondaryContainer = KlassSecondaryContainer,
    onSecondaryContainer = KlassOnSecondaryContainer,
    tertiary = KlassTertiary,
    onTertiary = KlassOnTertiary,
    tertiaryContainer = KlassTertiaryContainer,
    onTertiaryContainer = KlassOnTertiaryContainer,
    background = KlassBackground,
    onBackground = KlassOnBackground,
    surface = KlassSurface,
    onSurface = KlassOnSurface,
    surfaceVariant = KlassSurfaceVariant,
    onSurfaceVariant = KlassOnSurfaceVariant,
    outline = KlassOutline
)

@Composable
fun KlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted brand colors for strong educational identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    KlassTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

