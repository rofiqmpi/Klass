package com.mpi.klass.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure Compose interactive animated icon system inspired by Telegram's expressive animated icons.
 * Reacts smoothly to press interactions, click triggers, loading state, and active/unread states.
 */
@Composable
fun TelegramAnimatedIcon(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    interactionSource: InteractionSource? = null,
    triggerKey: Int = 0,
    isLoading: Boolean = false,
    hasActivity: Boolean = false,
) {
    val isPressed = if (interactionSource != null) {
        val state by interactionSource.collectIsPressedAsState()
        state
    } else {
        false
    }

    val pulseAnim = remember { Animatable(0f) }
    LaunchedEffect(triggerKey, isPressed) {
        if (isPressed) {
            pulseAnim.snapTo(0.35f)
            pulseAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.52f, stiffness = 520f),
            )
        } else if (triggerKey > 0) {
            pulseAnim.snapTo(0f)
            pulseAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.48f, stiffness = 460f),
            )
            pulseAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            )
        } else {
            pulseAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
            )
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "tg_icon_scale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "tg_icon_ambient")
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tg_icon_spin",
    )
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tg_icon_ambient_phase",
    )

    val p = pulseAnim.value

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        contentAlignment = Alignment.Center,
    ) {
        when (icon.name) {
            "Notification" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramBell(
                    tint = tint,
                    pulse = p,
                    ambient = if (hasActivity) ambientPhase else 0f,
                    hasActivity = hasActivity,
                )
            }

            "MessageBubble" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramMessageBubble(
                    tint = tint,
                    pulse = p,
                    ambient = if (hasActivity) ambientPhase else 0f,
                    hasActivity = hasActivity,
                )
            }

            "Refresh" -> Canvas(modifier = Modifier.fillMaxSize()) {
                val angle = if (isLoading) continuousRotation else p * 360f
                drawTelegramRefresh(tint = tint, angle = angle)
            }

            "TrashCan" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramTrashCan(tint = tint, lidLift = p)
            }

            "PhoneSolid" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramPhone(tint = tint, ringPulse = p)
            }

            "Sparkle" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramSparkle(tint = tint, pulse = p, ambient = ambientPhase)
            }

            "UsersGroup" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramUsersGroup(tint = tint, pulse = p)
            }

            "Download" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramDownload(tint = tint, pulse = p, isLoading = isLoading, ambient = ambientPhase)
            }

            "Search" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramSearch(tint = tint, pulse = p)
            }

            "LightMode" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramSun(tint = tint, pulse = p)
            }

            "DarkMode" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramMoon(tint = tint, pulse = p)
            }

            "AutoMode" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramAutoTheme(tint = tint, pulse = p)
            }

            "Plus" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramPlus(tint = tint, pulse = p)
            }

            "Filter" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramFilter(tint = tint, pulse = p)
            }

            "Send" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramSend(tint = tint, pulse = p)
            }

            "Droplet" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramDroplet(tint = tint, pulse = p)
            }

            "Check" -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawTelegramCheck(tint = tint, pulse = p)
            }

            else -> {
                val reactiveTilt = when (icon.name) {
                    "CalendarDays", "Book", "GraduationCap", "Crown", "Palette", "Edit" ->
                        sin(p * PI).toFloat() * -10f
                    "DoorOpen", "Login", "Logout" ->
                        sin(p * PI).toFloat() * 8f
                    else -> 0f
                }
                val reactiveNudgeX = when (icon.name) {
                    "Login", "Logout" -> sin(p * PI).toFloat() * 3f
                    else -> 0f
                }
                val reactivePop = 1f + sin(p * PI).toFloat() * 0.14f

                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = reactiveTilt
                            translationX = reactiveNudgeX
                            scaleX = reactivePop
                            scaleY = reactivePop
                        },
                )
            }
        }
    }
}

private fun DrawScope.drawTelegramBell(
    tint: Color,
    pulse: Float,
    ambient: Float,
    hasActivity: Boolean,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    val swing = if (pulse > 0.01f) {
        sin(pulse * PI * 3.0).toFloat() * 14f
    } else if (hasActivity) {
        sin(ambient * PI * 2.0).toFloat() * 6f
    } else {
        0f
    }
    val clapperX = swing * 0.12f * u

    rotate(degrees = swing, pivot = Offset(12f * u, 3.5f * u)) {
        val bellPath = Path().apply {
            moveTo(18f * u, 8.5f * u)
            cubicTo(18f * u, 5.1f * u, 15.3f * u, 2.5f * u, 12f * u, 2.5f * u)
            cubicTo(8.7f * u, 2.5f * u, 6f * u, 5.1f * u, 6f * u, 8.5f * u)
            cubicTo(6f * u, 14.5f * u, 3.4f * u, 16.3f * u, 3.4f * u, 16.8f * u)
            lineTo(20.6f * u, 16.8f * u)
            cubicTo(20.6f * u, 16.3f * u, 18f * u, 14.5f * u, 18f * u, 8.5f * u)
            close()
        }
        drawPath(
            path = bellPath,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }

    drawArc(
        color = tint,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(9.4f * u + clapperX, 17.4f * u),
        size = Size(5.2f * u, 4.2f * u),
        style = Stroke(width = strokeW, cap = StrokeCap.Round),
    )

    if (pulse > 0.1f) {
        val waveAlpha = (1f - pulse).coerceIn(0f, 0.7f)
        drawArc(
            color = tint.copy(alpha = waveAlpha),
            startAngle = -35f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = Offset(16.5f * u, 3.5f * u),
            size = Size(5.5f * u, 6f * u),
            style = Stroke(width = 1.5f * u, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.drawTelegramMessageBubble(
    tint: Color,
    pulse: Float,
    ambient: Float,
    hasActivity: Boolean,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    val pop = 1f + sin(pulse * PI).toFloat() * 0.1f

    scale(scale = pop, pivot = center) {
        val bubble = Path().apply {
            moveTo(20.8f * u, 11.4f * u)
            cubicTo(20.8f * u, 16.1f * u, 16.9f * u, 19.6f * u, 12.2f * u, 19.6f * u)
            cubicTo(10.7f * u, 19.6f * u, 9.2f * u, 19.2f * u, 8.0f * u, 18.6f * u)
            lineTo(3.3f * u, 20.6f * u)
            lineTo(4.9f * u, 15.8f * u)
            cubicTo(4.1f * u, 14.5f * u, 3.6f * u, 13.0f * u, 3.6f * u, 11.4f * u)
            cubicTo(3.6f * u, 6.8f * u, 7.5f * u, 3.4f * u, 12.2f * u, 3.4f * u)
            cubicTo(17.0f * u, 3.4f * u, 20.8f * u, 6.8f * u, 20.8f * u, 11.4f * u)
            close()
        }
        drawPath(
            path = bubble,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val dotXs = listOf(8.5f, 12.2f, 15.9f)
        dotXs.forEachIndexed { idx, dx ->
            val phase = if (pulse > 0.01f) pulse else if (hasActivity) ambient else 0f
            val wave = if (phase > 0f) {
                sin((phase * PI * 2.0) - idx * 0.85).toFloat().coerceAtLeast(0f)
            } else {
                0f
            }
            val dy = (11.5f - wave * 1.8f) * u
            drawCircle(
                color = tint,
                radius = (1.15f + wave * 0.25f) * u,
                center = Offset(dx * u, dy),
            )
        }
    }
}

private fun DrawScope.drawTelegramRefresh(
    tint: Color,
    angle: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    rotate(degrees = angle, pivot = center) {
        drawArc(
            color = tint,
            startAngle = -55f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(4f * u, 4f * u),
            size = Size(16f * u, 16f * u),
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
        )
        val arrowHead = Path().apply {
            moveTo(15.2f * u, 4.3f * u)
            lineTo(19.2f * u, 5.1f * u)
            lineTo(18.0f * u, 9.0f * u)
        }
        drawPath(
            path = arrowHead,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

private fun DrawScope.drawTelegramTrashCan(
    tint: Color,
    lidLift: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.95f * u
    val openFactor = sin(lidLift * PI).toFloat().coerceIn(0f, 1f)

    // Lid lifts & hinges open like Telegram's animated delete icon
    rotate(degrees = -16f * openFactor, pivot = Offset(5f * u, 6.5f * u)) {
        translate(top = -1.6f * openFactor * u) {
            drawLine(
                color = tint,
                start = Offset(4.5f * u, 6.5f * u),
                end = Offset(19.5f * u, 6.5f * u),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )
            val handle = Path().apply {
                moveTo(9f * u, 6.5f * u)
                lineTo(9.5f * u, 3.8f * u)
                lineTo(14.5f * u, 3.8f * u)
                lineTo(15f * u, 6.5f * u)
            }
            drawPath(
                path = handle,
                color = tint,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }

    val body = Path().apply {
        moveTo(6.5f * u, 9f * u)
        lineTo(7.4f * u, 19.2f * u)
        cubicTo(7.5f * u, 20.3f * u, 8.4f * u, 21f * u, 9.5f * u, 21f * u)
        lineTo(14.5f * u, 21f * u)
        cubicTo(15.6f * u, 21f * u, 16.5f * u, 20.3f * u, 16.6f * u, 19.2f * u)
        lineTo(17.5f * u, 9f * u)
    }
    drawPath(
        path = body,
        color = tint,
        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    drawLine(
        color = tint,
        start = Offset(10.3f * u, 11.5f * u),
        end = Offset(10.5f * u, 17.5f * u),
        strokeWidth = strokeW,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = tint,
        start = Offset(13.7f * u, 11.5f * u),
        end = Offset(13.5f * u, 17.5f * u),
        strokeWidth = strokeW,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawTelegramPhone(
    tint: Color,
    ringPulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    val shake = sin(ringPulse * PI * 4.0).toFloat() * 11f
    val waveAlpha = sin(ringPulse * PI).toFloat().coerceIn(0f, 0.85f)

    rotate(degrees = shake, pivot = center) {
        val handset = Path().apply {
            moveTo(6.6f * u, 4.2f * u)
            lineTo(9.4f * u, 4.2f * u)
            lineTo(10.6f * u, 8.3f * u)
            lineTo(8.6f * u, 10.0f * u)
            cubicTo(9.6f * u, 12.2f * u, 11.4f * u, 14.0f * u, 13.6f * u, 15.0f * u)
            lineTo(15.3f * u, 13.0f * u)
            lineTo(19.4f * u, 14.2f * u)
            lineTo(19.4f * u, 17.0f * u)
            cubicTo(19.4f * u, 18.6f * u, 18.0f * u, 19.8f * u, 16.4f * u, 19.6f * u)
            cubicTo(9.6f * u, 18.8f * u, 4.8f * u, 14.0f * u, 4.0f * u, 7.2f * u)
            cubicTo(3.8f * u, 5.6f * u, 5.0f * u, 4.2f * u, 6.6f * u, 4.2f * u)
            close()
        }
        drawPath(
            path = handset,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }

    if (waveAlpha > 0.05f) {
        drawArc(
            color = tint.copy(alpha = waveAlpha),
            startAngle = -80f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = Offset(13.5f * u, 4.5f * u),
            size = Size(6f * u, 6f * u),
            style = Stroke(width = 1.6f * u, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.drawTelegramSparkle(
    tint: Color,
    pulse: Float,
    ambient: Float,
) {
    val u = size.minDimension / 24f
    val rot = pulse * 90f + ambient * 12f
    val pop = 1f + sin(pulse * PI).toFloat() * 0.14f

    rotate(degrees = rot, pivot = Offset(11.5f * u, 12.5f * u)) {
        scale(scale = pop, pivot = Offset(11.5f * u, 12.5f * u)) {
            val star = Path().apply {
                moveTo(11.5f * u, 3.5f * u)
                quadraticTo(11.9f * u, 12.1f * u, 20.5f * u, 12.5f * u)
                quadraticTo(11.9f * u, 12.9f * u, 11.5f * u, 21.5f * u)
                quadraticTo(11.1f * u, 12.9f * u, 2.5f * u, 12.5f * u)
                quadraticTo(11.1f * u, 12.1f * u, 11.5f * u, 3.5f * u)
                close()
            }
            drawPath(
                path = star,
                color = tint,
                style = Stroke(width = 1.9f * u, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }

    val miniScale = 0.85f + ambient * 0.35f + sin(pulse * PI).toFloat() * 0.2f
    scale(scale = miniScale, pivot = Offset(18.5f * u, 5.5f * u)) {
        drawLine(
            color = tint,
            start = Offset(18.5f * u, 3.4f * u),
            end = Offset(18.5f * u, 7.6f * u),
            strokeWidth = 1.6f * u,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(16.4f * u, 5.5f * u),
            end = Offset(20.6f * u, 5.5f * u),
            strokeWidth = 1.6f * u,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawTelegramUsersGroup(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.95f * u
    val hop = sin(pulse * PI).toFloat() * 1.4f * u

    // Primary user
    translate(top = -hop) {
        drawCircle(
            color = tint,
            radius = 3.5f * u,
            center = Offset(9f * u, 7.4f * u),
            style = Stroke(width = strokeW),
        )
        val shoulders = Path().apply {
            moveTo(2.8f * u, 20f * u)
            cubicTo(2.8f * u, 16.4f * u, 5.6f * u, 14.3f * u, 9f * u, 14.3f * u)
            cubicTo(12.4f * u, 14.3f * u, 15.2f * u, 16.4f * u, 15.2f * u, 20f * u)
        }
        drawPath(
            path = shoulders,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
        )
    }

    // Companion user
    translate(top = hop * 0.45f) {
        drawArc(
            color = tint,
            startAngle = -75f,
            sweepAngle = 155f,
            useCenter = false,
            topLeft = Offset(13.2f * u, 4.5f * u),
            size = Size(5.6f * u, 5.6f * u),
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
        )
        val sideShoulder = Path().apply {
            moveTo(17.5f * u, 14.8f * u)
            cubicTo(19.6f * u, 15.4f * u, 21.2f * u, 17.2f * u, 21.2f * u, 20f * u)
        }
        drawPath(
            path = sideShoulder,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.drawTelegramDownload(
    tint: Color,
    pulse: Float,
    isLoading: Boolean,
    ambient: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    val dropY = if (isLoading) {
        sin(ambient * PI * 2.0).toFloat() * 1.8f * u
    } else {
        sin(pulse * PI).toFloat() * 2.4f * u
    }

    translate(top = dropY) {
        drawLine(
            color = tint,
            start = Offset(12f * u, 3.5f * u),
            end = Offset(12f * u, 14.5f * u),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
        val chevron = Path().apply {
            moveTo(7.4f * u, 10.2f * u)
            lineTo(12f * u, 14.8f * u)
            lineTo(16.6f * u, 10.2f * u)
        }
        drawPath(
            path = chevron,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }

    val tray = Path().apply {
        moveTo(4.2f * u, 16.4f * u)
        lineTo(4.2f * u, 18.8f * u)
        cubicTo(4.2f * u, 19.9f * u, 5.1f * u, 20.8f * u, 6.2f * u, 20.8f * u)
        lineTo(17.8f * u, 20.8f * u)
        cubicTo(18.9f * u, 20.8f * u, 19.8f * u, 19.9f * u, 19.8f * u, 18.8f * u)
        lineTo(19.8f * u, 16.4f * u)
    }
    drawPath(
        path = tray,
        color = tint,
        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawTelegramSearch(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    val tilt = sin(pulse * PI).toFloat() * 12f
    rotate(degrees = tilt, pivot = Offset(10.5f * u, 10.5f * u)) {
        drawCircle(
            color = tint,
            radius = 6.8f * u,
            center = Offset(10.5f * u, 10.5f * u),
            style = Stroke(width = strokeW),
        )
        drawLine(
            color = tint,
            start = Offset(15.6f * u, 15.6f * u),
            end = Offset(20.4f * u, 20.4f * u),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawTelegramSun(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.9f * u
    val rot = pulse * 45f
    val radius = (4.4f + sin(pulse * PI).toFloat() * 0.5f) * u
    drawCircle(
        color = tint,
        radius = radius,
        center = center,
        style = Stroke(width = strokeW),
    )
    rotate(degrees = rot, pivot = center) {
        for (i in 0 until 8) {
            val rad = (i * 45.0) * PI / 180.0
            val r1 = 7.4f * u
            val r2 = (9.8f + sin(pulse * PI).toFloat() * 0.8f) * u
            drawLine(
                color = tint,
                start = Offset(
                    center.x + (cos(rad) * r1).toFloat(),
                    center.y + (sin(rad) * r1).toFloat(),
                ),
                end = Offset(
                    center.x + (cos(rad) * r2).toFloat(),
                    center.y + (sin(rad) * r2).toFloat(),
                ),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.drawTelegramMoon(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.95f * u
    val rot = sin(pulse * PI).toFloat() * -18f
    rotate(degrees = rot, pivot = center) {
        val moon = Path().apply {
            moveTo(20.2f * u, 14.3f * u)
            cubicTo(18.9f * u, 14.9f * u, 17.5f * u, 15.2f * u, 16f * u, 15.2f * u)
            cubicTo(11.5f * u, 15.2f * u, 8.8f * u, 12.5f * u, 8.8f * u, 8f * u)
            cubicTo(8.8f * u, 6.5f * u, 9.1f * u, 5.1f * u, 9.7f * u, 3.8f * u)
            cubicTo(5.8f * u, 4.7f * u, 3.2f * u, 8.2f * u, 3.2f * u, 12.4f * u)
            cubicTo(3.2f * u, 17.3f * u, 7.1f * u, 21f * u, 12f * u, 21f * u)
            cubicTo(16.2f * u, 21f * u, 19.5f * u, 18.2f * u, 20.2f * u, 14.3f * u)
            close()
        }
        drawPath(
            path = moon,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

private fun DrawScope.drawTelegramAutoTheme(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.95f * u
    rotate(degrees = pulse * 180f, pivot = center) {
        drawCircle(
            color = tint,
            radius = 8.4f * u,
            center = center,
            style = Stroke(width = strokeW),
        )
        drawArc(
            color = tint,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(3.6f * u, 3.6f * u),
            size = Size(16.8f * u, 16.8f * u),
        )
    }
}

private fun DrawScope.drawTelegramPlus(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2.2f * u
    rotate(degrees = pulse * 90f, pivot = center) {
        drawLine(
            color = tint,
            start = Offset(12f * u, 4.8f * u),
            end = Offset(12f * u, 19.2f * u),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(4.8f * u, 12f * u),
            end = Offset(19.2f * u, 12f * u),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawTelegramFilter(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.95f * u
    val slide = sin(pulse * PI).toFloat() * 2.2f * u

    drawLine(color = tint, start = Offset(4f * u, 6.5f * u), end = Offset(20f * u, 6.5f * u), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawCircle(color = tint, radius = 2.1f * u, center = Offset(9f * u + slide, 6.5f * u))

    drawLine(color = tint, start = Offset(4f * u, 12f * u), end = Offset(20f * u, 12f * u), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawCircle(color = tint, radius = 2.1f * u, center = Offset(15f * u - slide, 12f * u))

    drawLine(color = tint, start = Offset(4f * u, 17.5f * u), end = Offset(20f * u, 17.5f * u), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawCircle(color = tint, radius = 2.1f * u, center = Offset(10f * u + slide * 0.7f, 17.5f * u))
}

private fun DrawScope.drawTelegramSend(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 1.95f * u
    val fly = sin(pulse * PI).toFloat() * 2.2f * u
    translate(left = fly, top = -fly * 0.8f) {
        val plane = Path().apply {
            moveTo(21f * u, 3.2f * u)
            lineTo(14.2f * u, 20.6f * u)
            lineTo(10.8f * u, 13.2f * u)
            lineTo(3.4f * u, 9.8f * u)
            close()
        }
        drawPath(
            path = plane,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawLine(
            color = tint,
            start = Offset(20.6f * u, 3.6f * u),
            end = Offset(10.8f * u, 13.2f * u),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawTelegramDroplet(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2f * u
    val stretch = 1f + sin(pulse * PI).toFloat() * 0.12f
    scale(scaleX = 1f / stretch, scaleY = stretch, pivot = center) {
        val drop = Path().apply {
            moveTo(12f * u, 2.8f * u)
            lineTo(17.6f * u, 8.8f * u)
            cubicTo(20.2f * u, 11.7f * u, 19.7f * u, 16.5f * u, 16.6f * u, 18.9f * u)
            cubicTo(14.0f * u, 21.0f * u, 10.0f * u, 21.0f * u, 7.4f * u, 18.9f * u)
            cubicTo(4.3f * u, 16.5f * u, 3.8f * u, 11.7f * u, 6.4f * u, 8.8f * u)
            close()
        }
        drawPath(
            path = drop,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

private fun DrawScope.drawTelegramCheck(
    tint: Color,
    pulse: Float,
) {
    val u = size.minDimension / 24f
    val strokeW = 2.3f * u
    val pop = 1f + sin(pulse * PI).toFloat() * 0.18f
    scale(scale = pop, pivot = center) {
        val check = Path().apply {
            moveTo(4.5f * u, 12.6f * u)
            lineTo(9.8f * u, 17.8f * u)
            lineTo(19.6f * u, 6.6f * u)
        }
        drawPath(
            path = check,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
