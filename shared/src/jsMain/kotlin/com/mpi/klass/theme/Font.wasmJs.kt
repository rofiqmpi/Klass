package com.mpi.klass.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.hind_siliguri_bold
import klass.shared.generated.resources.hind_siliguri_light
import klass.shared.generated.resources.hind_siliguri_medium
import klass.shared.generated.resources.hind_siliguri_regular
import klass.shared.generated.resources.hind_siliguri_semibold
import org.jetbrains.compose.resources.Font

@Composable
actual fun getHindSiliguriFontFamily(): FontFamily = FontFamily(
    Font(Res.font.hind_siliguri_light, FontWeight.Light),
    Font(Res.font.hind_siliguri_regular, FontWeight.Normal),
    Font(Res.font.hind_siliguri_medium, FontWeight.Medium),
    Font(Res.font.hind_siliguri_semibold, FontWeight.SemiBold),
    Font(Res.font.hind_siliguri_bold, FontWeight.Bold),
)
