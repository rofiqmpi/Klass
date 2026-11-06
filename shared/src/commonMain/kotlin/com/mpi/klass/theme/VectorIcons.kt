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
            moveTo(3f, 10f)
            lineTo(12f, 3f)
            lineTo(21f, 10f)
            moveTo(5f, 8.5f)
            verticalLineTo(19f)
            curveTo(5f, 20.1f, 5.9f, 21f, 7f, 21f)
            horizontalLineTo(17f)
            curveTo(18.1f, 21f, 19f, 20.1f, 19f, 19f)
            verticalLineTo(8.5f)
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
            moveTo(3f, 6f)
            horizontalLineTo(21f)
            moveTo(3f, 12f)
            horizontalLineTo(21f)
            moveTo(3f, 18f)
            horizontalLineTo(21f)
        }
    }.build()

val LogoutIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Logout",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 7f)
            lineTo(15.59f, 8.41f)
            lineTo(18.17f, 11f)
            lineTo(8f, 11f)
            verticalLineTo(13f)
            lineTo(18.17f, 13f)
            lineTo(15.59f, 15.59f)
            lineTo(17f, 17f)
            lineTo(22f, 12f)
            close()
            moveTo(4f, 5f)
            horizontalLineTo(12f)
            verticalLineTo(3f)
            horizontalLineTo(4f)
            curveTo(2.9f, 3f, 2f, 3.9f, 2f, 5f)
            verticalLineTo(19f)
            curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f)
            horizontalLineTo(12f)
            verticalLineTo(19f)
            horizontalLineTo(4f)
            close()
        }
    }.build()

val NotificationIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Notification",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 22f)
            curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
            horizontalLineTo(10f)
            curveTo(10f, 21.1f, 10.89f, 22f, 12f, 22f)
            close()
            moveTo(18f, 16f)
            verticalLineTo(11f)
            curveTo(18f, 7.93f, 16.36f, 5.36f, 13.5f, 4.68f)
            verticalLineTo(4f)
            curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
            curveTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f)
            verticalLineTo(4.68f)
            curveTo(7.63f, 5.36f, 6f, 7.92f, 6f, 11f)
            verticalLineTo(16f)
            lineTo(4f, 18f)
            verticalLineTo(19f)
            horizontalLineTo(20f)
            verticalLineTo(18f)
            lineTo(18f, 16f)
            close()
        }
    }.build()

val EditIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Edit",
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
            moveTo(11f, 4f)
            lineTo(4f, 4f)
            curveTo(3.45f, 4f, 3f, 4.45f, 3f, 5f)
            verticalLineTo(20f)
            curveTo(3f, 20.55f, 3.45f, 21f, 4f, 21f)
            horizontalLineTo(19f)
            curveTo(19.55f, 21f, 20f, 20.55f, 20f, 20f)
            verticalLineTo(13f)
            moveTo(18.5f, 2.5f)
            curveTo(19.33f, 1.67f, 20.67f, 1.67f, 21.5f, 2.5f)
            curveTo(22.33f, 3.33f, 22.33f, 4.67f, 21.5f, 5.5f)
            lineTo(12f, 15f)
            lineTo(8f, 16f)
            lineTo(9f, 12f)
            lineTo(18.5f, 2.5f)
        }
    }.build()

val CameraIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Camera",
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
            moveTo(23f, 19f)
            curveTo(23f, 19.55f, 22.55f, 20f, 22f, 20f)
            horizontalLineTo(2f)
            curveTo(1.45f, 20f, 1f, 19.55f, 1f, 19f)
            verticalLineTo(8f)
            curveTo(1f, 7.45f, 1.45f, 7f, 2f, 7f)
            horizontalLineTo(7f)
            lineTo(9f, 4f)
            horizontalLineTo(15f)
            lineTo(17f, 7f)
            horizontalLineTo(22f)
            curveTo(22.55f, 7f, 23f, 7.45f, 23f, 8f)
            verticalLineTo(19f)
            close()
            moveTo(12f, 17f)
            curveTo(14.21f, 17f, 16f, 15.21f, 16f, 13f)
            curveTo(16f, 10.79f, 14.21f, 9f, 12f, 9f)
            curveTo(9.79f, 9f, 8f, 10.79f, 8f, 13f)
            curveTo(8f, 15.21f, 9.79f, 17f, 12f, 17f)
            close()
        }
    }.build()

val GalleryIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Gallery",
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
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(8.5f, 10f)
            curveTo(9.33f, 10f, 10f, 9.33f, 10f, 8.5f)
            curveTo(10f, 7.67f, 9.33f, 7f, 8.5f, 7f)
            curveTo(7.67f, 7f, 7f, 7.67f, 7f, 8.5f)
            curveTo(7f, 9.33f, 7.67f, 10f, 8.5f, 10f)
            close()
            moveTo(21f, 15f)
            lineTo(16f, 10f)
            lineTo(5f, 21f)
        }
    }.build()

val GraduationCapIcon: ImageVector
    get() = ImageVector.Builder(
        name = "GraduationCap",
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
            moveTo(22f, 10f)
            lineTo(12f, 5f)
            lineTo(2f, 10f)
            lineTo(12f, 15f)
            lineTo(22f, 10f)
            close()
            moveTo(6f, 12f)
            verticalLineTo(17f)
            curveTo(6f, 18.5f, 8.7f, 20f, 12f, 20f)
            curveTo(15.3f, 20f, 18f, 18.5f, 18f, 17f)
            verticalLineTo(12f)
            moveTo(22f, 10f)
            verticalLineTo(16f)
        }
    }.build()

val BookIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Book",
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
            moveTo(4f, 19.5f)
            curveTo(4f, 18.12f, 5.12f, 17f, 6.5f, 17f)
            horizontalLineTo(20f)
            moveTo(4f, 19.5f)
            curveTo(4f, 20.88f, 5.12f, 22f, 6.5f, 22f)
            horizontalLineTo(20f)
            verticalLineTo(4f)
            horizontalLineTo(6.5f)
            curveTo(5.12f, 4f, 4f, 5.12f, 4f, 6.5f)
            verticalLineTo(19.5f)
            close()
        }
    }.build()

val ShieldIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Shield",
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
            moveTo(12f, 22f)
            curveTo(12f, 22f, 20f, 18f, 20f, 12f)
            verticalLineTo(5f)
            lineTo(12f, 2f)
            lineTo(4f, 5f)
            verticalLineTo(12f)
            curveTo(4f, 18f, 12f, 22f, 12f, 22f)
            close()
        }
    }.build()

val CrownIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Crown",
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
            moveTo(2f, 20f)
            horizontalLineTo(22f)
            moveTo(4f, 17f)
            lineTo(2f, 7f)
            lineTo(8f, 11f)
            lineTo(12f, 3f)
            lineTo(16f, 11f)
            lineTo(22f, 7f)
            lineTo(20f, 17f)
            horizontalLineTo(4f)
            close()
        }
    }.build()

val AwardIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Award",
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
            moveTo(12f, 15f)
            curveTo(15.31f, 15f, 18f, 12.31f, 18f, 9f)
            curveTo(18f, 5.69f, 15.31f, 3f, 12f, 3f)
            curveTo(8.69f, 3f, 6f, 5.69f, 6f, 9f)
            curveTo(6f, 12.31f, 8.69f, 15f, 12f, 15f)
            close()
            moveTo(8.21f, 13.89f)
            lineTo(7f, 21f)
            lineTo(12f, 18f)
            lineTo(17f, 21f)
            lineTo(15.79f, 13.88f)
        }
    }.build()

val StarIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Star",
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
            moveTo(12f, 2f)
            lineTo(15.09f, 8.26f)
            lineTo(22f, 9.27f)
            lineTo(17f, 14.14f)
            lineTo(18.18f, 21.02f)
            lineTo(12f, 17.77f)
            lineTo(5.82f, 21.02f)
            lineTo(7f, 14.14f)
            lineTo(2f, 9.27f)
            lineTo(8.91f, 8.26f)
            lineTo(12f, 2f)
            close()
        }
    }.build()

val GearIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Gear",
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
            moveTo(12f, 15f)
            curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
            curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
            curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            close()
            moveTo(19.4f, 15f)
            curveTo(19.27f, 15.46f, 19.18f, 15.93f, 19.06f, 16.39f)
            lineTo(20.3f, 17.36f)
            lineTo(18.3f, 20.82f)
            lineTo(16.78f, 20.21f)
            curveTo(16.39f, 20.52f, 15.98f, 20.79f, 15.54f, 21.02f)
            lineTo(15.31f, 22.64f)
            horizontalLineTo(11.31f)
            lineTo(11.08f, 21.02f)
            curveTo(10.64f, 20.79f, 10.23f, 20.52f, 9.84f, 20.21f)
            lineTo(8.32f, 20.82f)
            lineTo(6.32f, 17.36f)
            lineTo(7.56f, 16.39f)
            curveTo(7.44f, 15.93f, 7.35f, 15.46f, 7.22f, 15f)
            lineTo(5.6f, 14.77f)
            verticalLineTo(10.77f)
            lineTo(7.22f, 10.54f)
            curveTo(7.35f, 10.08f, 7.44f, 9.61f, 7.56f, 9.15f)
            lineTo(6.32f, 8.18f)
            lineTo(8.32f, 4.72f)
            lineTo(9.84f, 5.33f)
            curveTo(10.23f, 5.02f, 10.64f, 4.75f, 11.08f, 4.52f)
            lineTo(11.31f, 2.9f)
            horizontalLineTo(15.31f)
            lineTo(15.54f, 4.52f)
            curveTo(15.98f, 4.75f, 16.39f, 5.02f, 16.78f, 5.33f)
            lineTo(18.32f, 4.72f)
            lineTo(20.32f, 8.18f)
            lineTo(19.08f, 9.15f)
            curveTo(19.2f, 9.61f, 19.29f, 10.08f, 19.42f, 10.54f)
            lineTo(21.04f, 10.77f)
            verticalLineTo(14.77f)
            lineTo(19.4f, 15f)
            close()
        }
    }.build()

val PenIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Pen",
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
            moveTo(12f, 19f)
            lineTo(19f, 12f)
            lineTo(22f, 15f)
            lineTo(15f, 22f)
            lineTo(12f, 19f)
            close()
            moveTo(18f, 13f)
            lineTo(11f, 6f)
            lineTo(14f, 3f)
            lineTo(21f, 10f)
            lineTo(18f, 13f)
            close()
            moveTo(2f, 22f)
            lineTo(7f, 21f)
            lineTo(17f, 11f)
            lineTo(12f, 6f)
            lineTo(2f, 16f)
            lineTo(2f, 22f)
            close()
        }
    }.build()

// ─── New Icons ───────────────────────────────────────────────────────────────

val CheckIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Check",
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
            moveTo(20f, 6f)
            lineTo(9f, 17f)
            lineTo(4f, 12f)
        }
    }.build()

val PersonIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Person",
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
            moveTo(20f, 21f)
            verticalLineTo(19f)
            curveTo(20f, 16.79f, 18.21f, 15f, 16f, 15f)
            horizontalLineTo(8f)
            curveTo(5.79f, 15f, 4f, 16.79f, 4f, 19f)
            verticalLineTo(21f)
            moveTo(12f, 11f)
            curveTo(14.21f, 11f, 16f, 9.21f, 16f, 7f)
            curveTo(16f, 4.79f, 14.21f, 3f, 12f, 3f)
            curveTo(9.79f, 3f, 8f, 4.79f, 8f, 7f)
            curveTo(8f, 9.21f, 9.79f, 11f, 12f, 11f)
            close()
        }
    }.build()

val ToggleOnIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ToggleOn",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 7f)
            horizontalLineTo(7f)
            curveTo(4.24f, 7f, 2f, 9.24f, 2f, 12f)
            curveTo(2f, 14.76f, 4.24f, 17f, 7f, 17f)
            horizontalLineTo(17f)
            curveTo(19.76f, 17f, 22f, 14.76f, 22f, 12f)
            curveTo(22f, 9.24f, 19.76f, 7f, 17f, 7f)
            close()
            moveTo(17f, 15f)
            curveTo(15.34f, 15f, 14f, 13.66f, 14f, 12f)
            curveTo(14f, 10.34f, 15.34f, 9f, 17f, 9f)
            curveTo(18.66f, 9f, 20f, 10.34f, 20f, 12f)
            curveTo(20f, 13.66f, 18.66f, 15f, 17f, 15f)
            close()
        }
    }.build()

val ToggleOffIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ToggleOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 7f)
            horizontalLineTo(7f)
            curveTo(4.24f, 7f, 2f, 9.24f, 2f, 12f)
            curveTo(2f, 14.76f, 4.24f, 17f, 7f, 17f)
            horizontalLineTo(17f)
            curveTo(19.76f, 17f, 22f, 14.76f, 22f, 12f)
            curveTo(22f, 9.24f, 19.76f, 7f, 17f, 7f)
            close()
            moveTo(7f, 15f)
            curveTo(5.34f, 15f, 4f, 13.66f, 4f, 12f)
            curveTo(4f, 10.34f, 5.34f, 9f, 7f, 9f)
            curveTo(8.66f, 9f, 10f, 10.34f, 10f, 12f)
            curveTo(10f, 13.66f, 8.66f, 15f, 7f, 15f)
            close()
        }
    }.build()

val GoogleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Google",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
        ) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 11.3f, 21.93f, 10.63f, 21.8f, 10f)
            horizontalLineTo(12f)
            verticalLineTo(14f)
            horizontalLineTo(17.65f)
            curveTo(17.39f, 15.35f, 16.35f, 18f, 12f, 18f)
            curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
            curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
            curveTo(13.66f, 6f, 15.14f, 6.62f, 16.24f, 7.64f)
            lineTo(19.07f, 4.81f)
            curveTo(17.38f, 3.25f, 15.11f, 2f, 12f, 2f)
            close()
        }
    }.build()

val FacebookIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Facebook",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(24f, 12.073f)
            curveTo(24f, 5.405f, 18.627f, 0f, 12f, 0f)
            curveTo(5.373f, 0f, 0f, 5.405f, 0f, 12.073f)
            curveTo(0f, 18.1f, 4.388f, 23.094f, 10.125f, 24f)
            verticalLineTo(15.563f)
            horizontalLineTo(7.078f)
            verticalLineTo(12.073f)
            horizontalLineTo(10.125f)
            verticalLineTo(9.413f)
            curveTo(10.125f, 6.405f, 11.916f, 4.723f, 14.656f, 4.723f)
            curveTo(15.969f, 4.723f, 17.344f, 4.955f, 17.344f, 4.955f)
            verticalLineTo(7.93f)
            horizontalLineTo(15.83f)
            curveTo(14.34f, 7.93f, 13.875f, 8.853f, 13.875f, 9.795f)
            verticalLineTo(12.073f)
            horizontalLineTo(17.203f)
            lineTo(16.671f, 15.563f)
            horizontalLineTo(13.875f)
            verticalLineTo(24f)
            curveTo(19.612f, 23.094f, 24f, 18.1f, 24f, 12.073f)
            close()
        }
    }.build()

val AppleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Apple",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(18.71f, 19.5f)
            curveTo(17.88f, 20.74f, 17f, 21.95f, 15.66f, 21.97f)
            curveTo(14.32f, 22f, 13.89f, 21.18f, 12.37f, 21.18f)
            curveTo(10.85f, 21.18f, 10.37f, 21.95f, 9.1f, 22.03f)
            curveTo(7.77f, 22.1f, 6.8f, 20.73f, 5.96f, 19.5f)
            curveTo(4.25f, 17f, 2.94f, 12.45f, 4.7f, 9.39f)
            curveTo(5.57f, 7.87f, 7.13f, 6.91f, 8.82f, 6.88f)
            curveTo(10.1f, 6.86f, 11.32f, 7.75f, 12.11f, 7.75f)
            curveTo(12.9f, 7.75f, 14.35f, 6.64f, 15.88f, 6.81f)
            curveTo(16.52f, 6.84f, 18.3f, 7.08f, 19.45f, 8.76f)
            curveTo(19.36f, 8.82f, 17.3f, 10.05f, 17.33f, 12.5f)
            curveTo(17.36f, 15.35f, 19.82f, 16.32f, 19.85f, 16.34f)
            curveTo(19.83f, 16.4f, 19.46f, 17.7f, 18.71f, 19.5f)
            close()
            moveTo(15.13f, 4.88f)
            curveTo(15.82f, 4.04f, 16.28f, 2.87f, 16.15f, 1.7f)
            curveTo(15.12f, 1.74f, 13.84f, 2.39f, 13.14f, 3.23f)
            curveTo(12.51f, 3.99f, 11.97f, 5.18f, 12.12f, 6.34f)
            curveTo(13.27f, 6.43f, 14.44f, 5.72f, 15.13f, 4.88f)
            close()
        }
    }.build()

val LanguageGlobeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "LanguageGlobe",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()

            moveTo(2f, 12f)
            horizontalLineTo(22f)

            moveTo(12f, 2f)
            curveTo(8.5f, 5.5f, 6.5f, 8.5f, 6.5f, 12f)
            curveTo(6.5f, 15.5f, 8.5f, 18.5f, 12f, 22f)

            moveTo(12f, 2f)
            curveTo(15.5f, 5.5f, 17.5f, 8.5f, 17.5f, 12f)
            curveTo(17.5f, 15.5f, 15.5f, 18.5f, 12f, 22f)
        }
    }.build()

val RegistrationIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Registration",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(9f, 12f)
            curveTo(11.21f, 12f, 13f, 10.21f, 13f, 8f)
            curveTo(13f, 5.79f, 11.21f, 4f, 9f, 4f)
            curveTo(6.79f, 4f, 5f, 5.79f, 5f, 8f)
            curveTo(5f, 10.21f, 6.79f, 12f, 9f, 12f)
            close()

            moveTo(17f, 20f)
            verticalLineTo(18.5f)
            curveTo(17f, 16.5f, 14.5f, 15f, 9f, 15f)
            curveTo(3.5f, 15f, 1f, 16.5f, 1f, 18.5f)
            verticalLineTo(20f)

            moveTo(20f, 8f)
            verticalLineTo(14f)

            moveTo(17f, 11f)
            horizontalLineTo(23f)
        }
    }.build()
