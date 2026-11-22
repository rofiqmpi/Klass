package com.mpi.klass.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay

/**
 * Interactive Lottie animated icon renderer powered by Compottie.
 *
 * Uses multi-layer duotone + trim-path Bodymovin Lottie JSON compositions from [LottieIconCatalog]
 * and reacts smoothly to initial appearance, user presses/clicks (without cancelling on finger lift),
 * loading loops, and unread/active states.
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

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.56f, stiffness = 720f),
        label = "lottie_icon_press_scale",
    )

    val hasLottie = remember(icon.name) { LottieIconCatalog.hasBespokeAnimation(icon.name) }

    if (!hasLottie) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                },
        )
        return
    }

    val lottieJson = remember(icon.name) { LottieIconCatalog.jsonFor(icon.name) }
    val composition by rememberLottieComposition(icon.name) {
        LottieCompositionSpec.JsonString(lottieJson)
    }

    val infiniteProgress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isLoading,
        iterations = Compottie.IterateForever,
        speed = if (icon.name == "Refresh") 1.18f else 0.96f,
    )

    // Monotonic playback token so releasing press never cancels an in-flight animation
    var playToken by remember(icon.name) { mutableIntStateOf(0) }
    var playedEntrance by remember(icon.name) { mutableStateOf(false) }
    var lastTriggerKey by remember(icon.name) { mutableIntStateOf(triggerKey) }
    var wasPressed by remember(icon.name) { mutableStateOf(false) }

    LaunchedEffect(composition) {
        if (composition != null && !playedEntrance) {
            playedEntrance = true
            playToken++
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey != lastTriggerKey) {
            lastTriggerKey = triggerKey
            if (triggerKey > 0) {
                playToken++
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed && !wasPressed) {
            playToken++
        }
        wasPressed = isPressed
    }

    val interactiveProgress = remember(icon.name) { Animatable(0f) }

    LaunchedEffect(composition, playToken, hasActivity, isLoading) {
        if (composition == null || isLoading) return@LaunchedEffect
        if (hasActivity) {
            while (true) {
                interactiveProgress.snapTo(0f)
                interactiveProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 900, easing = LinearEasing),
                )
                interactiveProgress.snapTo(0f)
                delay(1550L)
            }
        } else if (playToken > 0) {
            interactiveProgress.snapTo(0f)
            interactiveProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 820, easing = LinearEasing),
            )
            interactiveProgress.snapTo(0f)
        }
    }

    val activeProgress = if (isLoading) infiniteProgress else interactiveProgress.value

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        contentAlignment = Alignment.Center,
    ) {
        val loadedComp = composition
        if (loadedComp != null) {
            val painter = rememberLottiePainter(
                composition = loadedComp,
                progress = { activeProgress },
            )
            Image(
                painter = painter,
                contentDescription = contentDescription,
                colorFilter = ColorFilter.tint(tint),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
