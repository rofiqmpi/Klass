package com.mpi.klass.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PaletteIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Palette",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(12f, 3f)
            curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
            curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
            curveTo(12.83f, 21f, 13.5f, 20.33f, 13.5f, 19.5f)
            curveTo(13.5f, 19.11f, 13.35f, 18.76f, 13.11f, 18.5f)
            curveTo(12.88f, 18.23f, 12.73f, 17.88f, 12.73f, 17.5f)
            curveTo(12.73f, 16.67f, 13.4f, 16f, 14.23f, 16f)
            horizontalLineTo(16f)
            curveTo(18.76f, 16f, 21f, 13.76f, 21f, 11f)
            curveTo(21f, 6.58f, 16.97f, 3f, 12f, 3f)
            close()
            moveTo(6.5f, 12f)
            curveTo(5.67f, 12f, 5f, 11.33f, 5f, 10.5f)
            curveTo(5f, 9.67f, 5.67f, 9f, 6.5f, 9f)
            curveTo(7.33f, 9f, 8f, 9.67f, 8f, 10.5f)
            curveTo(8f, 11.33f, 7.33f, 12f, 6.5f, 12f)
            close()
            moveTo(9.5f, 8f)
            curveTo(8.67f, 8f, 8f, 7.33f, 8f, 6.5f)
            curveTo(8f, 5.67f, 8.67f, 5f, 9.5f, 5f)
            curveTo(10.33f, 5f, 11f, 5.67f, 11f, 6.5f)
            curveTo(11f, 7.33f, 10.33f, 8f, 9.5f, 8f)
            close()
            moveTo(14.5f, 8f)
            curveTo(13.67f, 8f, 13f, 7.33f, 13f, 6.5f)
            curveTo(13f, 5.67f, 13.67f, 5f, 14.5f, 5f)
            curveTo(15.33f, 5f, 16f, 5.67f, 16f, 6.5f)
            curveTo(16f, 7.33f, 15.33f, 8f, 14.5f, 8f)
            close()
            moveTo(17.5f, 12f)
            curveTo(16.67f, 12f, 16f, 11.33f, 16f, 10.5f)
            curveTo(16f, 9.67f, 16.67f, 9f, 17.5f, 9f)
            curveTo(18.33f, 9f, 19f, 9.67f, 19f, 10.5f)
            curveTo(19f, 11.33f, 18.33f, 12f, 17.5f, 12f)
            close()
        }
    }.build()

val CloseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }
    }.build()

val LightModeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "LightMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(12f, 7f)
            curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
            curveTo(7f, 14.76f, 9.24f, 17f, 12f, 17f)
            curveTo(14.76f, 17f, 17f, 14.76f, 17f, 12f)
            curveTo(17f, 9.24f, 14.76f, 7f, 12f, 7f)
            close()
            moveTo(12f, 2f)
            verticalLineTo(4f)
            moveTo(12f, 20f)
            verticalLineTo(22f)
            moveTo(4.22f, 4.22f)
            lineTo(5.64f, 5.64f)
            moveTo(18.36f, 18.36f)
            lineTo(19.78f, 19.78f)
            moveTo(2f, 12f)
            horizontalLineTo(4f)
            moveTo(20f, 12f)
            horizontalLineTo(22f)
            moveTo(4.22f, 19.78f)
            lineTo(5.64f, 18.36f)
            moveTo(18.36f, 5.64f)
            lineTo(19.78f, 4.22f)
        }
    }.build()

val DarkModeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "DarkMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.3f, 2f)
            curveTo(6.98f, 2f, 2.63f, 6.32f, 2.63f, 11.64f)
            curveTo(2.63f, 16.96f, 6.98f, 21.28f, 12.3f, 21.28f)
            curveTo(16.05f, 21.28f, 19.31f, 19.12f, 20.89f, 15.96f)
            curveTo(14.28f, 15.96f, 8.94f, 10.56f, 8.94f, 3.95f)
            curveTo(8.94f, 3.26f, 9.07f, 2.61f, 9.31f, 2f)
            curveTo(10.28f, 2f, 11.29f, 2f, 12.3f, 2f)
            close()
        }
    }.build()

val AutoModeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "AutoMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 20f)
            verticalLineTo(4f)
            curveTo(16.41f, 4f, 20f, 7.59f, 20f, 12f)
            curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f)
            close()
        }
    }.build()



val RightArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "RightArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(5f, 12f)
            lineTo(19f, 12f)
            moveTo(12f, 5f)
            lineTo(19f, 12f)
            lineTo(12f, 19f)
        }
    }.build()

val HomeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Home",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // Roof
            moveTo(3f, 10f)
            lineTo(12f, 3f)
            lineTo(21f, 10f)
            // Body
            moveTo(5f, 8.5f)
            verticalLineTo(19f)
            curveTo(5f, 20.1f, 5.9f, 21f, 7f, 21f)
            horizontalLineTo(17f)
            curveTo(18.1f, 21f, 19f, 20.1f, 19f, 19f)
            verticalLineTo(8.5f)
            // Door
            moveTo(10f, 21f)
            verticalLineTo(14f)
            horizontalLineTo(14f)
            verticalLineTo(21f)
        }
    }.build()

val MenuIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Menu",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // 3 Clean Hamburger Menu Lines
            moveTo(3f, 6f)
            horizontalLineTo(21f)
            moveTo(3f, 12f)
            horizontalLineTo(21f)
            moveTo(3f, 18f)
            horizontalLineTo(21f)
        }
    }.build()
