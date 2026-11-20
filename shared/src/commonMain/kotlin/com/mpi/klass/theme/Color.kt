package com.mpi.klass.theme

import androidx.compose.ui.graphics.Color

// Clean, Modern Cobalt & Slate Material 3 Light Palette
val LightPrimary = Color(0xFF2563EB)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDBEAFE)
val LightOnPrimaryContainer = Color(0xFF1E3A8A)

val LightSecondary = Color(0xFF475569)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFF1F5F9)
val LightOnSecondaryContainer = Color(0xFF0F172A)

val LightTertiary = Color(0xFF0F766E)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFCCFBF1)
val LightOnTertiaryContainer = Color(0xFF115E59)

val LightError = Color(0xFFDC2626)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF991B1B)

val LightBackground = Color(0xFFF8FAFC)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFF8FAFC)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFFFFFFF)
val LightSurfaceContainer = Color(0xFFF1F5F9)
val LightSurfaceContainerHigh = Color(0xFFE9EFF6)
val LightSurfaceContainerHighest = Color(0xFFDCE5F0)
val LightOutline = Color(0xFF94A3B8)
val LightOutlineVariant = Color(0xFFE2E8F0)

// Clean, Crisp Midnight Slate Dark Palette
val DarkPrimary = Color(0xFF60A5FA)
val DarkOnPrimary = Color(0xFF0B1120)
val DarkPrimaryContainer = Color(0xFF1E3A8A)
val DarkOnPrimaryContainer = Color(0xFFDBEAFE)

val DarkSecondary = Color(0xFF94A3B8)
val DarkOnSecondary = Color(0xFF0F172A)
val DarkSecondaryContainer = Color(0xFF1E293B)
val DarkOnSecondaryContainer = Color(0xFFF1F5F9)

val DarkTertiary = Color(0xFF2DD4BF)
val DarkOnTertiary = Color(0xFF042F2E)
val DarkTertiaryContainer = Color(0xFF134E4A)
val DarkOnTertiaryContainer = Color(0xFFCCFBF1)

val DarkError = Color(0xFFF87171)
val DarkOnError = Color(0xFF450A0A)
val DarkErrorContainer = Color(0xFF7F1D1D)
val DarkOnErrorContainer = Color(0xFFFEE2E2)

val DarkBackground = Color(0xFF0B1120)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkSurface = Color(0xFF0B1120)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkSurfaceContainerLowest = Color(0xFF070B14)
val DarkSurfaceContainerLow = Color(0xFF151F32)
val DarkSurfaceContainer = Color(0xFF1E293B)
val DarkSurfaceContainerHigh = Color(0xFF263348)
val DarkSurfaceContainerHighest = Color(0xFF334155)
val DarkOutline = Color(0xFF475569)
val DarkOutlineVariant = Color(0xFF253247)

// Clean, Luminous Rainbow Screen Background Canvas (Light pastel & Dark aurora)
val RainbowScreenLightStops: List<Color> = listOf(
    Color(0xFFFFE4EC), // Soft Rose Blush
    Color(0xFFFFEFE0), // Soft Peach Apricot
    Color(0xFFFEF9DB), // Soft Sunlit Cream
    Color(0xFFE2FCEE), // Soft Mint Breeze
    Color(0xFFE0F2FE), // Soft Sky Mist
    Color(0xFFE8ECFF), // Soft Periwinkle
    Color(0xFFF3E8FF), // Soft Lavender Glow
)

val RainbowScreenDarkStops: List<Color> = listOf(
    Color(0xFF2B1123), // Aurora Rose
    Color(0xFF281715), // Aurora Coral
    Color(0xFF242010), // Aurora Amber
    Color(0xFF0C2620), // Aurora Emerald
    Color(0xFF0C1E35), // Aurora Sky
    Color(0xFF151836), // Aurora Indigo
    Color(0xFF241335), // Aurora Violet
)

fun rainbowScreenStops(isDark: Boolean): List<Color> =
    if (isDark) RainbowScreenDarkStops else RainbowScreenLightStops

// Clean accent helpers preserved for any semantic chart/tag uses
val RainbowLightRed = Color(0xFFE11D48)
val RainbowLightOrange = Color(0xFFEA580C)
val RainbowLightAmber = Color(0xFFD97706)
val RainbowLightEmerald = Color(0xFF059669)
val RainbowLightSky = Color(0xFF0284C7)
val RainbowLightIndigo = Color(0xFF4F46E5)
val RainbowLightViolet = Color(0xFF7C3AED)

val RainbowDarkRed = Color(0xFFFB7185)
val RainbowDarkOrange = Color(0xFFFB923C)
val RainbowDarkAmber = Color(0xFFFBBF24)
val RainbowDarkEmerald = Color(0xFF34D399)
val RainbowDarkSky = Color(0xFF38BDF8)
val RainbowDarkIndigo = Color(0xFF818CF8)
val RainbowDarkViolet = Color(0xFFA78BFA)

val RainbowSpectrumLight: List<Color> = listOf(
    RainbowLightRed,
    RainbowLightOrange,
    RainbowLightAmber,
    RainbowLightEmerald,
    RainbowLightSky,
    RainbowLightIndigo,
    RainbowLightViolet,
)

val RainbowSpectrumDark: List<Color> = listOf(
    RainbowDarkRed,
    RainbowDarkOrange,
    RainbowDarkAmber,
    RainbowDarkEmerald,
    RainbowDarkSky,
    RainbowDarkIndigo,
    RainbowDarkViolet,
)

fun rainbowSpectrumColors(isDark: Boolean): List<Color> =
    if (isDark) RainbowSpectrumDark else RainbowSpectrumLight

fun rainbowAccentAt(index: Int, isDark: Boolean): Color {
    val palette = rainbowSpectrumColors(isDark)
    val safeIndex = ((index % palette.size) + palette.size) % palette.size
    return palette[safeIndex]
}

/**
 * Unified semantic color token per feature, category, or status.
 */
data class FeatureAccent(
    val primary: Color,
    val deep: Color,
    val softTint: Color,
    val borderTint: Color,
)

object KlassFeaturePalette {
    fun routine(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFF60A5FA), Color(0xFF1D4ED8), Color(0x2E3B82F6), Color(0x6660A5FA))
    } else {
        FeatureAccent(Color(0xFF2563EB), Color(0xFF1E40AF), Color(0x182563EB), Color(0x452563EB))
    }

    fun teaching(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFF2DD4BF), Color(0xFF0F766E), Color(0x2B14B8A6), Color(0x662DD4BF))
    } else {
        FeatureAccent(Color(0xFF0D9488), Color(0xFF115E59), Color(0x170D9488), Color(0x420D9488))
    }

    fun books(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFFFBBF24), Color(0xFFB45309), Color(0x2BF59E0B), Color(0x66FBBF24))
    } else {
        FeatureAccent(Color(0xFFD97706), Color(0xFF92400E), Color(0x18D97706), Color(0x42D97706))
    }

    fun teachers(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFF38BDF8), Color(0xFF0369A1), Color(0x2B0EA5E9), Color(0x6638BDF8))
    } else {
        FeatureAccent(Color(0xFF0284C7), Color(0xFF075985), Color(0x170284C7), Color(0x420284C7))
    }

    fun classrooms(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFF34D399), Color(0xFF047857), Color(0x2B10B981), Color(0x6634D399))
    } else {
        FeatureAccent(Color(0xFF059669), Color(0xFF065F46), Color(0x17059669), Color(0x42059669))
    }

    fun notifications(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFFF59E0B), Color(0xFFB45309), Color(0x2BF59E0B), Color(0x66FBBF24))
    } else {
        FeatureAccent(Color(0xFFEA580C), Color(0xFF9A3412), Color(0x17EA580C), Color(0x42EA580C))
    }

    fun blood(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFFFB7185), Color(0xFFBE123C), Color(0x2EF43F5E), Color(0x66FB7185))
    } else {
        FeatureAccent(Color(0xFFE11D48), Color(0xFF9F1239), Color(0x18E11D48), Color(0x45E11D48))
    }

    fun ai(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFFA78BFA), Color(0xFF6D28D9), Color(0x2E8B5CF6), Color(0x66A78BFA))
    } else {
        FeatureAccent(Color(0xFF7C3AED), Color(0xFF5B21B6), Color(0x187C3AED), Color(0x457C3AED))
    }

    fun profile(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFF818CF8), Color(0xFF4338CA), Color(0x2E6366F1), Color(0x66818CF8))
    } else {
        FeatureAccent(Color(0xFF4F46E5), Color(0xFF3730A3), Color(0x184F46E5), Color(0x454F46E5))
    }

    fun admin(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFFFB923C), Color(0xFFC2410C), Color(0x2EF97316), Color(0x66FB923C))
    } else {
        FeatureAccent(Color(0xFFEA580C), Color(0xFF9A3412), Color(0x18EA580C), Color(0x45EA580C))
    }

    fun appearance(isDark: Boolean): FeatureAccent = if (isDark) {
        FeatureAccent(Color(0xFFF472B6), Color(0xFFBE185D), Color(0x2EEC4899), Color(0x66F472B6))
    } else {
        FeatureAccent(Color(0xFFDB2777), Color(0xFF9D174D), Color(0x17DB2777), Color(0x42DB2777))
    }

    fun success(isDark: Boolean): FeatureAccent = classrooms(isDark)
    fun warning(isDark: Boolean): FeatureAccent = books(isDark)
    fun error(isDark: Boolean): FeatureAccent = blood(isDark)
}

fun dayPaletteAt(dayIndex: Int, isDark: Boolean): FeatureAccent {
    return when (((dayIndex % 5) + 5) % 5) {
        0 -> KlassFeaturePalette.routine(isDark)      // Sun - Cobalt
        1 -> KlassFeaturePalette.teaching(isDark)     // Mon - Teal
        2 -> KlassFeaturePalette.ai(isDark)           // Tue - Violet
        3 -> KlassFeaturePalette.books(isDark)        // Wed - Amber
        else -> KlassFeaturePalette.blood(isDark)     // Thu - Rose
    }
}

fun departmentAccent(deptCode: String, isDark: Boolean): FeatureAccent {
    val norm = deptCode.trim().uppercase()
    return when {
        norm.contains("CST") || norm.contains("CMT") -> KlassFeaturePalette.routine(isDark)
        norm.contains("CT") || norm.contains("CIV") -> KlassFeaturePalette.books(isDark)
        norm.contains("ET") && !norm.contains("ENT") -> KlassFeaturePalette.teachers(isDark)
        norm.contains("ENT") -> KlassFeaturePalette.ai(isDark)
        norm.contains("MT") -> KlassFeaturePalette.teaching(isDark)
        norm.contains("RAC") -> KlassFeaturePalette.classrooms(isDark)
        norm.contains("FT") -> KlassFeaturePalette.appearance(isDark)
        else -> KlassFeaturePalette.profile(isDark)
    }
}



