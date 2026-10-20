package com.mpi.klass.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun SystemBarTheme(isDark: Boolean) {
    val view = LocalView.current
    val navColor = if (isDark) "#0F172A".toColorInt() else "#FFFFFF".toColorInt()

    SideEffect {
        val window = view.context.findActivity()?.window
        if (window != null) {
            @Suppress("DEPRECATION")
            window.navigationBarColor = navColor
            val insetsController = WindowInsetsControllerCompat(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }
}
