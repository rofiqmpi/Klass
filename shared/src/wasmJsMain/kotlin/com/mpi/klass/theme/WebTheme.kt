package com.mpi.klass.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mpi.klass.jsStorageGet
import com.mpi.klass.jsStorageSet
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.hind_siliguri_bold
import klass.shared.generated.resources.hind_siliguri_light
import klass.shared.generated.resources.hind_siliguri_medium
import klass.shared.generated.resources.hind_siliguri_regular
import klass.shared.generated.resources.hind_siliguri_semibold
import org.jetbrains.compose.resources.Font

private const val THEME_KEY = "klass_theme_mode"

actual fun saveThemeMode(mode: ThemeMode) {
    jsStorageSet(THEME_KEY, mode.name)
}

actual fun loadThemeMode(): ThemeMode {
    val saved = jsStorageGet(THEME_KEY) ?: return ThemeMode.DEFAULT
    return ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.DEFAULT
}

@Composable
actual fun getHindSiliguriFontFamily(): FontFamily = FontFamily(
    Font(Res.font.hind_siliguri_light, FontWeight.Light),
    Font(Res.font.hind_siliguri_regular, FontWeight.Normal),
    Font(Res.font.hind_siliguri_medium, FontWeight.Medium),
    Font(Res.font.hind_siliguri_semibold, FontWeight.SemiBold),
    Font(Res.font.hind_siliguri_bold, FontWeight.Bold),
)

@Composable
actual fun SystemBarTheme(isDark: Boolean) {
    // Browsers have no system bars to tint.
}
