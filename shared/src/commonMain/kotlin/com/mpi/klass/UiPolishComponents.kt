package com.mpi.klass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.SearchIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.rainbowScreenStops
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.notification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Smooth, luminous full-bleed rainbow aurora canvas for all screens.
 */
internal fun Modifier.rainbowScreenBackground(
    isDark: Boolean,
    focusAccent: Color? = null,
): Modifier = this.drawBehind {
    val stops = rainbowScreenStops(isDark)
    drawRect(
        brush = Brush.linearGradient(
            colors = stops,
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        )
    )
    // Soft ambient radial highlights to enrich screen depth without adding clutter
    val topHighlight = focusAccent ?: stops.last()
    val bottomHighlight = stops.first()
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                topHighlight.copy(alpha = if (isDark) 0.16f else 0.14f),
                Color.Transparent,
            ),
            center = Offset(size.width * 0.85f, size.height * 0.10f),
            radius = (size.maxDimension * 0.65f).coerceAtLeast(1f),
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                bottomHighlight.copy(alpha = if (isDark) 0.14f else 0.18f),
                Color.Transparent,
            ),
            center = Offset(size.width * 0.15f, size.height * 0.88f),
            radius = (size.maxDimension * 0.60f).coerceAtLeast(1f),
        )
    )
}

@Composable
internal fun MinimalBackdrop(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    focusAccent: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .rainbowScreenBackground(isDark, focusAccent),
        content = content,
    )
}

/**
 * Subtle spacious separator band over the rainbow background.
 */
@Composable
internal fun SocialSectionDividerBand(
    isDark: Boolean,
    height: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    )
}

/**
 * Clean inset hairline divider between list rows.
 */
@Composable
internal fun SocialInsetDivider(
    startIndent: Dp = 76.dp,
    endIndent: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = modifier.padding(start = startIndent, end = endIndent),
    )
}

/**
 * Clean translucent glass block allowing the rainbow screen background to shine gently through.
 */
@Composable
internal fun SocialSurfaceBlock(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    showTopHairline: Boolean = false,
    showBottomHairline: Boolean = false,
    accentTint: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val baseBg = if (isDark) {
        cs.surfaceContainerLow.copy(alpha = 0.62f)
    } else {
        Color.White.copy(alpha = 0.68f)
    }
    val hairlineColor = accentTint?.copy(alpha = if (isDark) 0.30f else 0.20f)
        ?: cs.outlineVariant.copy(alpha = 0.45f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(baseBg)
            .then(
                if (accentTint != null) {
                    Modifier.background(accentTint.copy(alpha = if (isDark) 0.08f else 0.045f))
                } else {
                    Modifier
                }
            ),
    ) {
        if (showTopHairline) {
            HorizontalDivider(thickness = 0.5.dp, color = hairlineColor)
        }
        content()
        if (showBottomHairline) {
            HorizontalDivider(thickness = 0.5.dp, color = hairlineColor)
        }
    }
}

/**
 * Smooth entrance animation for list items.
 */
@Composable
internal fun AnimatedStaggeredItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(6f) }

    LaunchedEffect(index) {
        val staggerMs = (index.coerceAtMost(5) * 16L)
        if (staggerMs > 0L) delay(staggerMs)
        launch {
            alphaAnim.animateTo(1f, tween(durationMillis = 160, easing = FastOutSlowInEasing))
        }
        launch {
            offsetYAnim.animateTo(0f, tween(durationMillis = 180, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = alphaAnim.value
            translationY = offsetYAnim.value
        },
    ) {
        content()
    }
}

/**
 * Kept strictly for empty states or special dialogs; main lists use edge-to-edge Social rows.
 */
@Composable
internal fun MinimalCardSurface(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color? = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
    containerColor: Color = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.74f)
    } else {
        Color.White.copy(alpha = 0.80f)
    },
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val border = borderColor?.let { BorderStroke(0.75.dp, it) }
    val taggedModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = containerColor,
            border = border,
            shadowElevation = 0.dp,
            modifier = taggedModifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth(), content = content)
        }
    } else {
        Surface(
            shape = shape,
            color = containerColor,
            border = border,
            shadowElevation = 0.dp,
            modifier = taggedModifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

/**
 * Crisp Telegram / Facebook style top app bar with bold 22.sp title and subtle rainbow glass edge.
 */
@Composable
internal fun GoogleTopAppBar(
    title: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = BackArrowIcon,
    navigationContentDescription: String = "Back",
    onNavigationClick: (() -> Unit)? = null,
    navigationTestTag: String? = null,
    showBottomDivider: Boolean = true,
    containerColor: Color = Color.Transparent,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = containerColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(60.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (navigationIcon != null && onNavigationClick != null) {
                    IconButton(
                        onClick = onNavigationClick,
                        modifier = Modifier
                            .size(48.dp)
                            .then(if (navigationTestTag != null) Modifier.testTag(navigationTestTag) else Modifier),
                    ) {
                        Icon(
                            imageVector = navigationIcon,
                            contentDescription = navigationContentDescription,
                            tint = cs.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Text(
                    text = title,
                    color = cs.onSurface,
                    fontSize = 22.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    content = actions,
                )
            }
            if (showBottomDivider) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = cs.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

/**
 * Distinct, color-coded AI (Violet) & Notification (Amber/Coral) circular icon buttons with 48.dp hit areas.
 */
@Composable
internal fun HeaderAiNotificationActions(
    isDark: Boolean,
    showAi: Boolean = true,
    showNotification: Boolean = true,
    onAiClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val aiAccent = KlassFeaturePalette.ai(isDark)
    val notiAccent = KlassFeaturePalette.notifications(isDark)
    val baseBg = if (isDark) Color(0xFF151F32).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)

    Row(
        modifier = modifier.padding(end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (showAi) {
            Surface(
                onClick = onAiClick,
                shape = CircleShape,
                color = baseBg,
                border = BorderStroke(1.dp, aiAccent.borderTint),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(42.dp)
                    .testTag("top_bar_ai_button"),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(aiAccent.softTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SparkleIcon,
                        contentDescription = "Zed AI",
                        tint = aiAccent.primary,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }

        if (showNotification) {
            Surface(
                onClick = onNotificationClick,
                shape = CircleShape,
                color = baseBg,
                border = BorderStroke(1.dp, notiAccent.borderTint),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(42.dp)
                    .testTag("top_bar_notification_button"),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(notiAccent.softTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.notification),
                        contentDescription = "Notifications",
                        tint = notiAccent.primary,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

/**
 * Clean section heading with subtle pill dot accent.
 */
@Composable
internal fun GoogleSectionHeader(
    title: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 16.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = title,
            color = accent,
            fontSize = 15.5.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Tactile 3D Extruded Button preserving the signature 3D lip, with optional custom semantic color.
 */
@Composable
internal fun WelcomeMinimal3DButton(
    text: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimaryAccent: Boolean = true,
    isDestructive: Boolean = false,
    isDark: Boolean = false,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 46.dp,
    cornerRadius: Dp = 14.dp,
    customPrimaryColor: Color? = null,
    customBaseColor: Color? = null,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val canClick = enabled && !isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (fgColor, baseColor, contentColor) = when {
        isDestructive -> Triple(
            Color(0xFFE11D48),
            Color(0xFF9F1239),
            Color.White,
        )
        customPrimaryColor != null -> {
            val fg = customPrimaryColor
            val base = customBaseColor ?: customPrimaryColor.copy(
                red = (customPrimaryColor.red * 0.72f).coerceIn(0f, 1f),
                green = (customPrimaryColor.green * 0.72f).coerceIn(0f, 1f),
                blue = (customPrimaryColor.blue * 0.72f).coerceIn(0f, 1f),
            )
            val content = if (fg.luminance() > 0.54f) Color(0xFF0B1120) else Color.White
            Triple(fg, base, content)
        }
        isPrimaryAccent -> Triple(
            if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
            if (isDark) Color(0xFF0284C7) else Color(0xFF1D4ED8),
            if (isDark) Color(0xFF0F172A) else Color.White,
        )
        else -> Triple(
            if (isDark) Color(0xFF1E293B) else Color.White,
            if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
            if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
        )
    }

    val safeHeight = height.coerceAtLeast(44.dp)
    val lipDepth = 3.dp
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .height(safeHeight + lipDepth)
            .alpha(if (enabled) 1f else 0.6f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .offset(y = lipDepth)
                .fillMaxWidth()
                .height(safeHeight)
                .clip(shape)
                .background(baseColor),
        )

        Surface(
            onClick = onClick,
            enabled = canClick,
            interactionSource = interactionSource,
            shape = shape,
            color = fgColor,
            border = BorderStroke(0.75.dp, baseColor),
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(safeHeight)
                .offset(y = if (isPressed && canClick) lipDepth else 0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                GoogleButtonContent(
                    text = text,
                    icon = icon,
                    isLoading = isLoading,
                    contentColor = contentColor,
                    hindSiliguri = hindSiliguri,
                )
            }
        }
    }
}

/**
 * Compact Tonal Pill Action Button (40.dp visual height inside a 48.dp minimum touch target)
 * ideal for inline actions (Call, Email, GPS Sync, Filter, Clear, Copy, View).
 */
@Composable
internal fun KlassTonalButton(
    text: String,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 40.dp,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = resolvedAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
        border = BorderStroke(1.dp, resolvedAccent.copy(alpha = if (isDark) 0.45f else 0.30f)),
        shadowElevation = 0.dp,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(height.coerceAtLeast(36.dp))
            .alpha(if (enabled) 1f else 0.5f)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedAccent,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = resolvedAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GoogleButtonContent(
    text: String,
    icon: ImageVector?,
    isLoading: Boolean,
    contentColor: Color,
    hindSiliguri: FontFamily,
) {
    if (isLoading) {
        CircularProgressIndicator(
            color = contentColor,
            strokeWidth = 2.dp,
            modifier = Modifier.size(19.dp),
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Clean outlined action with larger label typography and optional accent tint.
 */
@Composable
internal fun GoogleOutlinedButton(
    text: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 46.dp,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.42f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
            contentColor = contentColor,
        ),
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .height(height),
    ) {
        GoogleButtonContent(
            text = text,
            icon = icon,
            isLoading = false,
            contentColor = contentColor,
            hindSiliguri = hindSiliguri,
        )
    }
}

/**
 * Tactile 3D Extruded IconButton with accessible 48.dp touch target and press-down feedback.
 */
@Composable
internal fun WelcomeMinimal3DIconButton(
    icon: ImageVector,
    contentDescription: String,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    iconTint: Color = Color.Unspecified,
    baseColor: Color = Color.Transparent,
    visualSize: Dp = 40.dp,
    iconSize: Dp = 19.dp,
    cornerRadius: Dp = 12.dp,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    val canClick = enabled && !isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val resolvedContainer = if (containerColor == Color.Unspecified) {
        if (isDark) Color(0xFF1E293B) else Color.White
    } else {
        containerColor
    }
    val resolvedBase = when {
        baseColor != Color.Transparent -> baseColor
        containerColor == Color.Unspecified -> if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val resolvedTint = if (iconTint == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        iconTint
    }
    val shape = RoundedCornerShape(cornerRadius)
    val lipDepth = 2.5.dp

    Box(
        modifier = modifier
            .size(48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp),
                enabled = canClick,
                onClick = onClick,
            )
            .alpha(if (enabled) 1f else 0.6f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(width = visualSize, height = visualSize + lipDepth),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .offset(y = lipDepth)
                    .size(visualSize)
                    .clip(shape)
                    .background(resolvedBase),
            )

            Surface(
                shape = shape,
                color = resolvedContainer,
                border = BorderStroke(0.75.dp, resolvedBase),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(visualSize)
                    .offset(y = if (isPressed && canClick) lipDepth else 0.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = resolvedTint,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(iconSize),
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDescription,
                            tint = resolvedTint,
                            modifier = Modifier.size(iconSize),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Borderless Telegram / Facebook style pill search field with clear 16.sp input text.
 */
@Composable
internal fun MinimalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    clearTestTag: String? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val pillBg by animateColorAsState(
        targetValue = if (isDark) {
            cs.surfaceContainerLow.copy(alpha = if (isFocused) 0.92f else 0.74f)
        } else {
            Color.White.copy(alpha = if (isFocused) 0.98f else 0.84f)
        },
        animationSpec = tween(150),
        label = "SearchPillBg",
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = pillBg,
        border = BorderStroke(
            width = if (isFocused) 1.4.dp else 0.75.dp,
            color = if (isFocused) accent else accent.copy(alpha = 0.30f),
        ),
        shadowElevation = 0.dp,
        modifier = modifier.height(48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = SearchIcon,
                contentDescription = null,
                tint = if (isFocused) accent else accent.copy(alpha = 0.75f),
                modifier = Modifier.size(19.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = cs.onSurfaceVariant,
                        fontSize = 15.5.sp,
                        fontFamily = hindSiliguri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = cs.onSurface,
                        fontSize = 16.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Normal,
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(40.dp)
                        .then(if (clearTestTag != null) Modifier.testTag(clearTestTag) else Modifier),
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Clear search",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Clean glass filter chip with unified primary active state.
 */
@Composable
internal fun MinimalIconChip(
    label: String,
    selected: Boolean,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent
    val selectedFg = if (resolvedAccent.luminance() > 0.52f) Color(0xFF0B1120) else Color.White
    val unselectedBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.74f) else Color.White.copy(alpha = 0.84f)
    val containerColor by animateColorAsState(
        targetValue = if (selected) resolvedAccent else unselectedBg,
        animationSpec = tween(180),
        label = "ChipContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedFg else cs.onSurface,
        animationSpec = tween(180),
        label = "ChipContent",
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = if (!selected) BorderStroke(0.75.dp, resolvedAccent.copy(alpha = 0.28f)) else null,
        modifier = modifier
            .height(40.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val activeIcon = if (selected && icon == null) CheckIcon else icon
            if (activeIcon != null) {
                Icon(
                    imageVector = activeIcon,
                    contentDescription = null,
                    tint = if (selected) contentColor else resolvedAccent,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 14.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Compact pill stat badge with crisp typography honoring rainbow accents.
 */
@Composable
internal fun MinimalStatBadge(
    icon: ImageVector,
    text: String,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent
    Surface(
        shape = CircleShape,
        color = resolvedAccent.copy(alpha = if (isDark) 0.18f else 0.12f),
        border = BorderStroke(0.75.dp, resolvedAccent.copy(alpha = if (isDark) 0.38f else 0.28f)),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolvedAccent,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = text,
                color = cs.onSurface,
                fontSize = 14.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Responsive non-lazy 2-column (tablet/wide >= breakpoint) / 1-column (phone) grid helper.
 */
@Composable
internal fun <T> MinimalResponsiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    twoColMinWidth: Dp = 600.dp,
    spacing: Dp = 12.dp,
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cols = if (maxWidth >= twoColMinWidth) 2 else 1
        val rows = remember(items, cols) {
            items.withIndex().chunked(cols)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.Top,
                ) {
                    rowItems.forEach { indexed ->
                        Box(modifier = Modifier.weight(1f)) {
                            itemContent(indexed.index, indexed.value)
                        }
                    }
                    if (cols == 2 && rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Polished Full-Screen Sub-Screen Container with colored feature icon badge,
 * optional breadcrumb pill (`levelLabel`), header actions slot, and clean rainbow glass backdrop.
 */
@Composable
internal fun MinimalFullScreenSheet(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    dismissEnabled: Boolean = true,
    closeTestTag: String? = null,
    maxContentWidth: Dp = 740.dp,
    navigationIcon: ImageVector = BackArrowIcon,
    levelLabel: String? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppBackHandler(enabled = true) {
        if (dismissEnabled) onDismiss()
    }

    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent
    val headerBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.82f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .rainbowScreenBackground(isDark, resolvedAccent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            Surface(
                color = headerBg,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(resolvedAccent.copy(alpha = if (isDark) 0.10f else 0.055f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(62.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            enabled = dismissEnabled,
                            modifier = Modifier
                                .size(48.dp)
                                .then(if (closeTestTag != null) Modifier.testTag(closeTestTag) else Modifier),
                        ) {
                            Icon(
                                imageVector = navigationIcon,
                                contentDescription = "Back",
                                tint = cs.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = resolvedAccent.copy(alpha = if (isDark) 0.22f else 0.13f),
                            border = BorderStroke(0.75.dp, resolvedAccent.copy(alpha = 0.38f)),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = resolvedAccent,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (!levelLabel.isNullOrBlank()) {
                                Text(
                                    text = levelLabel,
                                    color = resolvedAccent,
                                    fontSize = 11.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = title,
                                color = cs.onSurface,
                                fontSize = 19.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    color = cs.onSurfaceVariant,
                                    fontSize = 13.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(end = 6.dp),
                            content = headerActions,
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = resolvedAccent.copy(alpha = if (isDark) 0.30f else 0.20f),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = maxContentWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
            }

            if (bottomBar != null) {
                Surface(
                    color = headerBg,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = resolvedAccent.copy(alpha = if (isDark) 0.30f else 0.22f),
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = maxContentWidth)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                bottomBar()
                            }
                        }
                    }
                }
            }
        }
    }
}
