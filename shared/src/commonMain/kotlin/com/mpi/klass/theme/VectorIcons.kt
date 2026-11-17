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

val SparkleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Sparkle",
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
            moveTo(12f, 2f)
            curveTo(12.3f, 7.5f, 16.5f, 11.7f, 22f, 12f)
            curveTo(16.5f, 12.3f, 12.3f, 16.5f, 12f, 22f)
            curveTo(11.7f, 16.5f, 7.5f, 12.3f, 2f, 12f)
            curveTo(7.5f, 11.7f, 11.7f, 7.5f, 12f, 2f)
            close()
        }
    }.build()

val SendIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Send",
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
            moveTo(2.01f, 21f)
            lineTo(23f, 12f)
            lineTo(2.01f, 3f)
            lineTo(2f, 10f)
            lineTo(17f, 12f)
            lineTo(2f, 14f)
            close()
        }
    }.build()

val ChevronDownIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ChevronDown",
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
            moveTo(6f, 9f)
            lineTo(12f, 15f)
            lineTo(18f, 9f)
        }
    }.build()

// ─── FontAwesome-Matching Profile & Edit Profile Icons ───────────────────────

val SolidUserIcon: ImageVector
    get() = ImageVector.Builder(
        name = "SolidUser",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 12f)
            curveTo(14.76f, 12f, 17f, 9.76f, 17f, 7f)
            curveTo(17f, 4.24f, 14.76f, 2f, 12f, 2f)
            curveTo(9.24f, 2f, 7f, 4.24f, 7f, 7f)
            curveTo(7f, 9.76f, 9.24f, 12f, 12f, 12f)
            close()
            moveTo(12f, 14f)
            curveTo(7.58f, 14f, 4f, 16.69f, 4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            horizontalLineTo(18f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            curveTo(20f, 16.69f, 16.42f, 14f, 12f, 14f)
            close()
        }
    }.build()

val UserTieIcon: ImageVector
    get() = ImageVector.Builder(
        name = "UserTie",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 12f)
            curveTo(14.76f, 12f, 17f, 9.76f, 17f, 7f)
            curveTo(17f, 4.24f, 14.76f, 2f, 12f, 2f)
            curveTo(9.24f, 2f, 7f, 4.24f, 7f, 7f)
            curveTo(7f, 9.76f, 9.24f, 12f, 12f, 12f)
            close()
            moveTo(14.5f, 14.2f)
            lineTo(13.2f, 19.8f)
            lineTo(12f, 21f)
            lineTo(10.8f, 19.8f)
            lineTo(9.5f, 14.2f)
            curveTo(6.3f, 15.1f, 4f, 17.3f, 4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            horizontalLineTo(18f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            curveTo(20f, 17.3f, 17.7f, 15.1f, 14.5f, 14.2f)
            close()
            moveTo(11f, 14f)
            horizontalLineTo(13f)
            lineTo(12.6f, 16.2f)
            lineTo(13.2f, 18.8f)
            lineTo(12f, 20f)
            lineTo(10.8f, 18.8f)
            lineTo(11.4f, 16.2f)
            close()
        }
    }.build()

val UserNurseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "UserNurse",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(7.5f, 6.2f)
            lineTo(8.5f, 2.5f)
            horizontalLineTo(15.5f)
            lineTo(16.5f, 6.2f)
            close()
            moveTo(12f, 12.5f)
            curveTo(14.5f, 12.5f, 16.5f, 10.5f, 16.5f, 8f)
            horizontalLineTo(7.5f)
            curveTo(7.5f, 10.5f, 9.5f, 12.5f, 12f, 12.5f)
            close()
            moveTo(12f, 14.2f)
            curveTo(7.6f, 14.2f, 4f, 16.8f, 4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            horizontalLineTo(18f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            curveTo(20f, 16.8f, 16.4f, 14.2f, 12f, 14.2f)
            close()
        }
    }.build()

val CalendarDaysIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CalendarDays",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 4f)
            horizontalLineTo(18f)
            verticalLineTo(2f)
            horizontalLineTo(16f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
            verticalLineTo(2f)
            horizontalLineTo(6f)
            verticalLineTo(4f)
            horizontalLineTo(5f)
            curveTo(3.89f, 4f, 3f, 4.9f, 3f, 6f)
            verticalLineTo(20f)
            curveTo(3f, 21.1f, 3.89f, 22f, 5f, 22f)
            horizontalLineTo(19f)
            curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
            verticalLineTo(6f)
            curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
            close()
            moveTo(19f, 20f)
            horizontalLineTo(5f)
            verticalLineTo(10f)
            horizontalLineTo(19f)
            verticalLineTo(20f)
            close()
            moveTo(7f, 12f)
            horizontalLineTo(9f)
            verticalLineTo(14f)
            horizontalLineTo(7f)
            close()
            moveTo(11f, 12f)
            horizontalLineTo(13f)
            verticalLineTo(14f)
            horizontalLineTo(11f)
            close()
            moveTo(15f, 12f)
            horizontalLineTo(17f)
            verticalLineTo(14f)
            horizontalLineTo(15f)
            close()
            moveTo(7f, 16f)
            horizontalLineTo(9f)
            verticalLineTo(18f)
            horizontalLineTo(7f)
            close()
            moveTo(11f, 16f)
            horizontalLineTo(13f)
            verticalLineTo(18f)
            horizontalLineTo(11f)
            close()
        }
    }.build()

val DropletIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Droplet",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2.69f)
            lineTo(17.66f, 8.35f)
            curveTo(20.78f, 11.47f, 20.78f, 16.53f, 17.66f, 19.65f)
            curveTo(14.54f, 22.77f, 9.46f, 22.77f, 6.34f, 19.65f)
            curveTo(3.22f, 16.53f, 3.22f, 11.47f, 6.34f, 8.35f)
            lineTo(12f, 2.69f)
            close()
        }
    }.build()

val PrayingHandsIcon: ImageVector
    get() = ImageVector.Builder(
        name = "PrayingHands",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(11.5f, 3f)
            lineTo(8.2f, 12.2f)
            lineTo(4.5f, 15.8f)
            lineTo(6.2f, 18.2f)
            lineTo(11.5f, 14.5f)
            verticalLineTo(3f)
            close()
            moveTo(12.5f, 3f)
            verticalLineTo(14.5f)
            lineTo(17.8f, 18.2f)
            lineTo(19.5f, 15.8f)
            lineTo(15.8f, 12.2f)
            lineTo(12.5f, 3f)
            close()
            moveTo(4f, 19.5f)
            lineTo(11.5f, 16f)
            verticalLineTo(21f)
            horizontalLineTo(5.5f)
            close()
            moveTo(20f, 19.5f)
            lineTo(18.5f, 21f)
            horizontalLineTo(12.5f)
            verticalLineTo(16f)
            close()
        }
    }.build()

val VenusMarsIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VenusMars",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.1f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(9.5f, 13.5f)
            curveTo(11.71f, 13.5f, 13.5f, 11.71f, 13.5f, 9.5f)
            curveTo(13.5f, 7.29f, 11.71f, 5.5f, 9.5f, 5.5f)
            curveTo(7.29f, 5.5f, 5.5f, 7.29f, 5.5f, 9.5f)
            curveTo(5.5f, 11.71f, 7.29f, 13.5f, 9.5f, 13.5f)
            close()
            moveTo(9.5f, 13.5f)
            verticalLineTo(20.5f)
            moveTo(7f, 17.5f)
            horizontalLineTo(12f)
            moveTo(13f, 6.5f)
            lineTo(19f, 3f)
            moveTo(15f, 3f)
            horizontalLineTo(19f)
            verticalLineTo(7f)
        }
    }.build()

val RingIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Ring",
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
            moveTo(12f, 20.5f)
            curveTo(15.87f, 20.5f, 19f, 17.37f, 19f, 13.5f)
            curveTo(19f, 9.63f, 15.87f, 6.5f, 12f, 6.5f)
            curveTo(8.13f, 6.5f, 5f, 9.63f, 5f, 13.5f)
            curveTo(5f, 17.37f, 8.13f, 20.5f, 12f, 20.5f)
            close()
            moveTo(9.5f, 3.5f)
            horizontalLineTo(14.5f)
            lineTo(13f, 6.5f)
            horizontalLineTo(11f)
            close()
        }
    }.build()

val PhoneSolidIcon: ImageVector
    get() = ImageVector.Builder(
        name = "PhoneSolid",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6.62f, 10.79f)
            curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
            lineTo(15.41f, 15.18f)
            curveTo(15.69f, 14.9f, 16.08f, 14.82f, 16.43f, 14.93f)
            curveTo(17.55f, 15.3f, 18.75f, 15.5f, 20f, 15.5f)
            curveTo(20.55f, 15.5f, 21f, 15.95f, 21f, 16.5f)
            verticalLineTo(20f)
            curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
            curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
            curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
            horizontalLineTo(7.5f)
            curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
            curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
            curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
            lineTo(6.62f, 10.79f)
            close()
        }
    }.build()

val EnvelopeSolidIcon: ImageVector
    get() = ImageVector.Builder(
        name = "EnvelopeSolid",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 4f)
            horizontalLineTo(4f)
            curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
            lineTo(2f, 18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            horizontalLineTo(20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            verticalLineTo(6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            close()
            moveTo(20f, 8f)
            lineTo(12f, 13f)
            lineTo(4f, 8f)
            verticalLineTo(6f)
            lineTo(12f, 11f)
            lineTo(20f, 6f)
            verticalLineTo(8f)
            close()
        }
    }.build()

val LocationDotIcon: ImageVector
    get() = ImageVector.Builder(
        name = "LocationDot",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
            curveTo(5f, 14.25f, 12f, 22f, 12f, 22f)
            curveTo(12f, 22f, 19f, 14.25f, 19f, 9f)
            curveTo(19f, 5.13f, 15.87f, 2f, 12f, 2f)
            close()
            moveTo(12f, 11.5f)
            curveTo(10.62f, 11.5f, 9.5f, 10.38f, 9.5f, 9f)
            curveTo(9.5f, 7.62f, 10.62f, 6.5f, 12f, 6.5f)
            curveTo(13.38f, 6.5f, 14.5f, 7.62f, 14.5f, 9f)
            curveTo(14.5f, 10.38f, 13.38f, 11.5f, 12f, 11.5f)
            close()
        }
    }.build()

val LayerGroupIcon: ImageVector
    get() = ImageVector.Builder(
        name = "LayerGroup",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(11.99f, 18.54f)
            lineTo(4.62f, 12.81f)
            lineTo(3f, 14.07f)
            lineTo(12f, 21.07f)
            lineTo(21f, 14.07f)
            lineTo(19.37f, 12.8f)
            lineTo(11.99f, 18.54f)
            close()
            moveTo(12f, 16f)
            lineTo(19.36f, 10.27f)
            lineTo(21f, 9f)
            lineTo(12f, 2f)
            lineTo(3f, 9f)
            lineTo(4.63f, 10.27f)
            lineTo(12f, 16f)
            close()
        }
    }.build()

val ClockIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Clock",
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
            moveTo(15.5f, 15.5f)
            lineTo(11f, 12.8f)
            verticalLineTo(7f)
            horizontalLineTo(12.8f)
            verticalLineTo(11.8f)
            lineTo(16.5f, 14f)
            close()
        }
    }.build()

val UsersRectangleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "UsersRectangle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 4f)
            horizontalLineTo(4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
            verticalLineTo(18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            horizontalLineTo(20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            verticalLineTo(6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            close()
            moveTo(9.5f, 11.5f)
            curveTo(10.6f, 11.5f, 11.5f, 10.6f, 11.5f, 9.5f)
            curveTo(11.5f, 8.4f, 10.6f, 7.5f, 9.5f, 7.5f)
            curveTo(8.4f, 7.5f, 7.5f, 8.4f, 7.5f, 9.5f)
            curveTo(7.5f, 10.6f, 8.4f, 11.5f, 9.5f, 11.5f)
            close()
            moveTo(13.5f, 16.5f)
            horizontalLineTo(5.5f)
            verticalLineTo(15.5f)
            curveTo(5.5f, 14.1f, 7.3f, 13f, 9.5f, 13f)
            curveTo(11.7f, 13f, 13.5f, 14.1f, 13.5f, 15.5f)
            verticalLineTo(16.5f)
            close()
            moveTo(16f, 11.5f)
            curveTo(16.8f, 11.5f, 17.5f, 10.8f, 17.5f, 10f)
            curveTo(17.5f, 9.2f, 16.8f, 8.5f, 16f, 8.5f)
            curveTo(15.2f, 8.5f, 14.5f, 9.2f, 14.5f, 10f)
            curveTo(14.5f, 10.8f, 15.2f, 11.5f, 16f, 11.5f)
            close()
            moveTo(19f, 16.5f)
            horizontalLineTo(15f)
            verticalLineTo(15.2f)
            curveTo(15f, 14.3f, 14.6f, 13.6f, 14.1f, 13.1f)
            curveTo(16.6f, 13.1f, 19f, 14.1f, 19f, 15.5f)
            verticalLineTo(16.5f)
            close()
        }
    }.build()

val IdCardIcon: ImageVector
    get() = ImageVector.Builder(
        name = "IdCard",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 4f)
            horizontalLineTo(4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
            verticalLineTo(18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            horizontalLineTo(20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            verticalLineTo(6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            close()
            moveTo(8.5f, 8.5f)
            curveTo(9.6f, 8.5f, 10.5f, 9.4f, 10.5f, 10.5f)
            curveTo(10.5f, 11.6f, 9.6f, 12.5f, 8.5f, 12.5f)
            curveTo(7.4f, 12.5f, 6.5f, 11.6f, 6.5f, 10.5f)
            curveTo(6.5f, 9.4f, 7.4f, 8.5f, 8.5f, 8.5f)
            close()
            moveTo(12f, 16.5f)
            horizontalLineTo(5f)
            verticalLineTo(15.5f)
            curveTo(5f, 14.3f, 6.6f, 13.5f, 8.5f, 13.5f)
            curveTo(10.4f, 13.5f, 12f, 14.3f, 12f, 15.5f)
            verticalLineTo(16.5f)
            close()
            moveTo(19f, 15f)
            horizontalLineTo(14f)
            verticalLineTo(13.2f)
            horizontalLineTo(19f)
            close()
            moveTo(19f, 11.8f)
            horizontalLineTo(14f)
            verticalLineTo(10f)
            horizontalLineTo(19f)
            close()
        }
    }.build()

val FileLinesIcon: ImageVector
    get() = ImageVector.Builder(
        name = "FileLines",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(14f, 2f)
            horizontalLineTo(6f)
            curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
            verticalLineTo(20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            horizontalLineTo(18f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            verticalLineTo(8f)
            lineTo(14f, 2f)
            close()
            moveTo(16f, 18f)
            horizontalLineTo(8f)
            verticalLineTo(16f)
            horizontalLineTo(16f)
            verticalLineTo(18f)
            close()
            moveTo(16f, 14f)
            horizontalLineTo(8f)
            verticalLineTo(12f)
            horizontalLineTo(16f)
            verticalLineTo(14f)
            close()
            moveTo(13f, 9f)
            verticalLineTo(3.5f)
            lineTo(18.5f, 9f)
            horizontalLineTo(13f)
            close()
        }
    }.build()

val DatabaseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Database",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 3f)
            curveTo(7.58f, 3f, 4f, 4.34f, 4f, 6f)
            curveTo(4f, 7.66f, 7.58f, 9f, 12f, 9f)
            curveTo(16.42f, 9f, 20f, 7.66f, 20f, 6f)
            curveTo(20f, 4.34f, 16.42f, 3f, 12f, 3f)
            close()
            moveTo(4f, 9f)
            verticalLineTo(12f)
            curveTo(4f, 13.66f, 7.58f, 15f, 12f, 15f)
            curveTo(16.42f, 15f, 20f, 13.66f, 20f, 12f)
            verticalLineTo(9f)
            curveTo(18.21f, 10.24f, 15.31f, 11f, 12f, 11f)
            curveTo(8.69f, 11f, 5.79f, 10.24f, 4f, 9f)
            close()
            moveTo(4f, 15f)
            verticalLineTo(18f)
            curveTo(4f, 19.66f, 7.58f, 21f, 12f, 21f)
            curveTo(16.42f, 21f, 20f, 19.66f, 20f, 18f)
            verticalLineTo(15f)
            curveTo(18.21f, 16.24f, 15.31f, 17f, 12f, 17f)
            curveTo(8.69f, 17f, 5.79f, 16.24f, 4f, 15f)
            close()
        }
    }.build()

val TrashCanIcon: ImageVector
    get() = ImageVector.Builder(
        name = "TrashCan",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 3f)
            verticalLineTo(4f)
            horizontalLineTo(4f)
            verticalLineTo(6f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            curveTo(5f, 20.1f, 5.9f, 21f, 7f, 21f)
            horizontalLineTo(17f)
            curveTo(18.1f, 21f, 19f, 20.1f, 19f, 19f)
            verticalLineTo(6f)
            horizontalLineTo(20f)
            verticalLineTo(4f)
            horizontalLineTo(15f)
            verticalLineTo(3f)
            horizontalLineTo(9f)
            close()
            moveTo(9f, 8f)
            horizontalLineTo(11f)
            verticalLineTo(17f)
            horizontalLineTo(9f)
            verticalLineTo(8f)
            close()
            moveTo(13f, 8f)
            horizontalLineTo(15f)
            verticalLineTo(17f)
            horizontalLineTo(13f)
            verticalLineTo(8f)
            close()
        }
    }.build()

val SearchIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Search",
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
            moveTo(11f, 19f)
            curveTo(15.42f, 19f, 19f, 15.42f, 19f, 11f)
            curveTo(19f, 6.58f, 15.42f, 3f, 11f, 3f)
            curveTo(6.58f, 3f, 3f, 6.58f, 3f, 11f)
            curveTo(3f, 15.42f, 6.58f, 19f, 11f, 19f)
            close()
            moveTo(21f, 21f)
            lineTo(16.65f, 16.65f)
        }
    }.build()

val RefreshIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Refresh",
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
            moveTo(21f, 12f)
            curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
            curveTo(8.56f, 21f, 5.57f, 19.07f, 4.06f, 16.24f)
            moveTo(3f, 12f)
            curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
            curveTo(15.44f, 3f, 18.43f, 4.93f, 19.94f, 7.76f)
            moveTo(21f, 3f)
            verticalLineTo(8f)
            horizontalLineTo(16f)
            moveTo(3f, 21f)
            verticalLineTo(16f)
            horizontalLineTo(8f)
        }
    }.build()

val PlusIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Plus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12f, 5f)
            verticalLineTo(19f)
            moveTo(5f, 12f)
            horizontalLineTo(19f)
        }
    }.build()

val WarningIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Warning",
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
            moveTo(10.29f, 3.86f)
            lineTo(1.82f, 18f)
            curveTo(1.64f, 18.3f, 1.55f, 18.65f, 1.55f, 19f)
            curveTo(1.55f, 20.1f, 2.45f, 21f, 3.55f, 21f)
            horizontalLineTo(20.45f)
            curveTo(21.55f, 21f, 22.45f, 20.1f, 22.45f, 19f)
            curveTo(22.45f, 18.65f, 22.36f, 18.3f, 22.18f, 18f)
            lineTo(13.71f, 3.86f)
            curveTo(13.33f, 3.22f, 12.66f, 2.86f, 12f, 2.86f)
            curveTo(11.34f, 2.86f, 10.67f, 3.22f, 10.29f, 3.86f)
            close()
            moveTo(12f, 9f)
            verticalLineTo(13f)
            moveTo(12f, 17f)
            horizontalLineTo(12.01f)
        }
    }.build()

val DoorOpenIcon: ImageVector
    get() = ImageVector.Builder(
        name = "DoorOpen",
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
            moveTo(3f, 21f)
            horizontalLineTo(21f)
            moveTo(5f, 21f)
            verticalLineTo(5f)
            curveTo(5f, 3.9f, 5.9f, 3f, 7f, 3f)
            horizontalLineTo(15f)
            curveTo(16.1f, 3f, 17f, 3.9f, 17f, 5f)
            verticalLineTo(21f)
            moveTo(17f, 8f)
            horizontalLineTo(19f)
            curveTo(20.1f, 8f, 21f, 8.9f, 21f, 10f)
            verticalLineTo(21f)
            moveTo(13f, 12f)
            horizontalLineTo(13.01f)
        }
    }.build()

val SwapIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Swap",
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
            moveTo(7f, 16f)
            lineTo(3f, 12f)
            lineTo(7f, 8f)
            moveTo(3f, 12f)
            horizontalLineTo(17f)
            moveTo(17f, 8f)
            lineTo(21f, 12f)
            lineTo(17f, 16f)
            moveTo(21f, 12f)
            horizontalLineTo(7f)
        }
    }.build()

val BackArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BackArrow",
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
            moveTo(19f, 12f)
            horizontalLineTo(5f)
            moveTo(12f, 19f)
            lineTo(5f, 12f)
            lineTo(12f, 5f)
        }
    }.build()

val FilterIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Filter",
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
            moveTo(4f, 6f)
            horizontalLineTo(20f)
            moveTo(7f, 12f)
            horizontalLineTo(17f)
            moveTo(10f, 18f)
            horizontalLineTo(14f)
        }
    }.build()

val MoreVertIcon: ImageVector
    get() = ImageVector.Builder(
        name = "MoreVert",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
        ) {
            // Top dot (12, 5)
            moveTo(12f, 3.5f)
            curveTo(12.83f, 3.5f, 13.5f, 4.17f, 13.5f, 5f)
            curveTo(13.5f, 5.83f, 12.83f, 6.5f, 12f, 6.5f)
            curveTo(11.17f, 6.5f, 10.5f, 5.83f, 10.5f, 5f)
            curveTo(10.5f, 4.17f, 11.17f, 3.5f, 12f, 3.5f)
            close()
            // Middle dot (12, 12)
            moveTo(12f, 10.5f)
            curveTo(12.83f, 10.5f, 13.5f, 11.17f, 13.5f, 12f)
            curveTo(13.5f, 12.83f, 12.83f, 13.5f, 12f, 13.5f)
            curveTo(11.17f, 13.5f, 10.5f, 12.83f, 10.5f, 12f)
            curveTo(10.5f, 11.17f, 11.17f, 10.5f, 12f, 10.5f)
            close()
            // Bottom dot (12, 19)
            moveTo(12f, 17.5f)
            curveTo(12.83f, 17.5f, 13.5f, 18.17f, 13.5f, 19f)
            curveTo(13.5f, 19.83f, 12.83f, 20.5f, 12f, 20.5f)
            curveTo(11.17f, 20.5f, 10.5f, 19.83f, 10.5f, 19f)
            curveTo(10.5f, 18.17f, 11.17f, 17.5f, 12f, 17.5f)
            close()
        }
    }.build()

