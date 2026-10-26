package com.mpi.klass

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.RoundedCorner
import android.view.WindowManager
import com.mpi.klass.theme.getAppContext

actual fun isGestureNavigation(): Boolean {
    val context = getAppContext() ?: return true
    return try {
        val mode = Settings.Secure.getInt(
            context.contentResolver,
            "navigation_mode",
            0,
        )
        mode == 2
    } catch (_: Exception) {
        true
    }
}

actual fun getDisplayBottomCornerRadiusDp(): Float {
    val context = getAppContext() ?: return 20f
    val density = context.resources.displayMetrics.density

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val insets = wm?.currentWindowMetrics?.windowInsets
            val bottomLeft = insets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
            val bottomRight = insets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)
            val radiusPx = bottomLeft?.radius ?: bottomRight?.radius
            if ((radiusPx != null) && (radiusPx > 0)) {
                val dp = radiusPx / density
                return (dp - 6f).coerceIn(12f, 30f)
            }
        } catch (_: Exception) {}
    }

    return 20f
}
