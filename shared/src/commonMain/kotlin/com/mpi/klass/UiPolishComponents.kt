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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
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
import com.mpi.klass.theme.SearchIcon
import com.mpi.klass.theme.SparkleIcon
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.notification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Clean minimal background canvas using the Slate & Cobalt theme background.
 */
@Composable
internal fun MinimalBackdrop(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content,
    )
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
    val offsetYAnim = remember { Animatable(8f) }

    LaunchedEffect(index) {
        val staggerMs = (index.coerceAtMost(6) * 20L)
        if (staggerMs > 0L) delay(staggerMs)
        launch {
            alphaAnim.animateTo(1f, tween(durationMillis = 180, easing = FastOutSlowInEasing))
        }
        launch {
            offsetYAnim.animateTo(0f, tween(durationMillis = 200, easing = FastOutSlowInEasing))
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
 * Clean crisp Card Surface (14.dp rounded corners, 0.75.dp subtle outlineVariant border).
 */
@Composable
internal fun MinimalCardSurface(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    borderColor: Color? = MaterialTheme.colorScheme.outlineVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
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
 * Clean TopAppBar (56.dp height, 19.sp title & subtle bottom divider).
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
    containerColor: Color = MaterialTheme.colorScheme.surface,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        color = containerColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
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
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 19.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
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
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

/**
 * Consistent AI & Notification circular icon buttons matching HomeRoutineHeader icons and colors.
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
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val primaryAccent = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    val notiAccent = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)

    Row(
        modifier = modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showAi) {
            Surface(
                onClick = onAiClick,
                shape = CircleShape,
                color = cardBg,
                border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.45f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.dp)
                    .testTag("top_bar_ai_button"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SparkleIcon,
                        contentDescription = "Study Assistant",
                        tint = primaryAccent,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }

        if (showNotification) {
            Surface(
                onClick = onNotificationClick,
                shape = CircleShape,
                color = cardBg,
                border = BorderStroke(1.dp, notiAccent.copy(alpha = 0.45f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.dp)
                    .testTag("top_bar_notification_button"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.notification),
                        contentDescription = "Notifications",
                        tint = notiAccent,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

/**
 * Clean minimal section label.
 */
@Composable
internal fun GoogleSectionHeader(
    title: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.5.sp,
        fontFamily = hindSiliguri,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

/**
 * Tactile 3D Extruded Button restoring the user's signature 3D lip + Cobalt/Slate/Red palette.
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
    height: Dp = 44.dp,
    cornerRadius: Dp = 12.dp,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val canClick = enabled && !isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (fgColor, baseColor, contentColor) = when {
        isDestructive -> Triple(
            Color(0xFFEF4444),
            Color(0xFFB91C1C),
            Color.White,
        )
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
        // 3D Bottom Shadow Lip
        Box(
            modifier = Modifier
                .offset(y = lipDepth)
                .fillMaxWidth()
                .height(safeHeight)
                .clip(shape)
                .background(baseColor),
        )

        // Tactile Pressable Surface
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
            modifier = Modifier.size(18.dp),
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
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Clean Outlined Button with crisp 12.dp corners and subtle outlineVariant border.
 */
@Composable
internal fun GoogleOutlinedButton(
    text: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 44.dp,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
    visualSize: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    cornerRadius: Dp = 11.dp,
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
 * Clean minimal SearchBar (44.dp height, 12.dp corners, 0.75.dp subtle border).
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
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) cs.primary else cs.outlineVariant,
        animationSpec = tween(160),
        label = "SearchBorderColor",
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cs.surfaceContainerLow,
        border = BorderStroke(if (isFocused) 1.2.dp else 0.75.dp, borderColor),
        shadowElevation = 0.dp,
        modifier = modifier.height(44.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = SearchIcon,
                contentDescription = null,
                tint = if (isFocused) cs.primary else cs.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
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
                        fontSize = 13.5.sp,
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
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Normal,
                    ),
                    cursorBrush = SolidColor(cs.primary),
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
                        .size(36.dp)
                        .then(if (clearTestTag != null) Modifier.testTag(clearTestTag) else Modifier),
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Clear search",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

/**
 * Clean FilterChip (34.dp height, crisp 10.dp rounded corners & 0.75.dp border).
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
    val containerColor = if (selected) cs.primaryContainer else cs.surfaceContainerLow
    val contentColor = if (selected) cs.onPrimaryContainer else cs.onSurfaceVariant
    val borderStroke = BorderStroke(
        0.75.dp,
        if (selected) cs.primary.copy(alpha = 0.55f) else cs.outlineVariant,
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = borderStroke,
        modifier = modifier
            .height(34.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val activeIcon = if (selected && icon == null) CheckIcon else icon
            if (activeIcon != null) {
                Icon(
                    imageVector = activeIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = hindSiliguri,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Compact Stat Badge.
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
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cs.surfaceContainerLow,
        border = BorderStroke(0.75.dp, cs.outlineVariant),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                color = cs.onSurface,
                fontSize = 12.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
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
 * Clean Full-Screen Dialog Sheet (compact 56.dp header).
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
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppBackHandler(enabled = true) {
        if (dismissEnabled) onDismiss()
    }

    val cs = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cs.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Surface(
                color = cs.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
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
                                imageVector = CloseIcon,
                                contentDescription = "Close",
                                tint = cs.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = cs.onSurface,
                                fontSize = 18.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    color = cs.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = cs.outlineVariant.copy(alpha = 0.5f),
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
                    color = cs.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = cs.outlineVariant.copy(alpha = 0.5f),
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
