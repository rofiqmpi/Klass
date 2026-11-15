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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.SearchIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean minimal screen canvas matching WelcomeScreen, MenuScreen & HomeScreen.
 */
@Composable
internal fun MinimalBackdrop(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
        content = content,
    )
}

/**
 * Subtle, crisp entrance fade + minimal slide for list items.
 */
@Composable
internal fun AnimatedStaggeredItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(12f) }

    LaunchedEffect(index) {
        val staggerMs = (index.coerceAtMost(8) * 28L)
        if (staggerMs > 0L) delay(staggerMs)
        launch {
            alphaAnim.animateTo(1f, tween(durationMillis = 200, easing = FastOutSlowInEasing))
        }
        launch {
            offsetYAnim.animateTo(0f, tween(durationMillis = 220, easing = FastOutSlowInEasing))
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
 * Clean minimal card surface matching MenuScreen & WelcomeScreen About cards.
 */
@Composable
internal fun MinimalCardSurface(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subtleBorder = borderColor ?: if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val taggedModifier = if (testTag != null) modifier.testTag(testTag) else modifier
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = cardBg,
            border = BorderStroke(1.dp, subtleBorder),
            shadowElevation = if (isDark) 2.dp else 1.5.dp,
            modifier = taggedModifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth(), content = content)
        }
    } else {
        Surface(
            shape = shape,
            color = cardBg,
            border = BorderStroke(1.dp, subtleBorder),
            shadowElevation = if (isDark) 2.dp else 1.5.dp,
            modifier = taggedModifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

/**
 * Exact WelcomeScreen-style 3D minimal button:
 * - Crisp 3.dp matte shadow base underneath
 * - Hairline 0.5.dp border on foreground Surface/Button
 * - Snaps cleanly down 3.dp onto base when pressed (no gradients, no bulky borders)
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
    cornerRadius: Dp = 12.dp,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val canClick = enabled && !isLoading
    val shape = RoundedCornerShape(cornerRadius)

    val containerColor = when {
        isDestructive -> Color(0xFFEF4444)
        isPrimaryAccent -> Color(0xFF8CB9D9)
        else -> if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isDestructive -> Color.White
        isPrimaryAccent -> Color(0xFF1E293B)
        else -> if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    }
    val baseColor = when {
        isDestructive -> Color(0xFF991B1B)
        isPrimaryAccent -> Color(0xFF8E7864)
        else -> if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
    }

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .height(height + 3.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        // 3D Shadow Base underneath
        Box(
            modifier = Modifier
                .offset(y = 3.dp)
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(if (enabled) baseColor else baseColor.copy(alpha = 0.45f)),
        )

        // Foreground Minimal 3D Button
        Button(
            onClick = onClick,
            enabled = canClick,
            interactionSource = interactionSource,
            shape = shape,
            border = BorderStroke(0.5.dp, baseColor),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.55f),
                disabledContentColor = contentColor.copy(alpha = 0.65f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = if (isPressed && canClick) 3.dp else 0.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp),
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
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * WelcomeScreen-style minimal 3D square/rounded icon button (matches Theme/Language header button)
 * inside a 48.dp touch target.
 */
@Composable
internal fun WelcomeMinimal3DIconButton(
    icon: ImageVector,
    contentDescription: String,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = if (isDark) Color(0xFF1E293B) else Color.White,
    iconTint: Color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B),
    baseColor: Color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
    visualSize: Dp = 38.dp,
    iconSize: Dp = 17.dp,
    cornerRadius: Dp = 10.dp,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val canClick = enabled && !isLoading
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(48.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = canClick,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(visualSize)
                .height(visualSize + 3.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .offset(y = 3.dp)
                    .size(visualSize)
                    .clip(shape)
                    .background(baseColor),
            )
            Surface(
                shape = shape,
                color = containerColor,
                border = BorderStroke(0.5.dp, baseColor),
                modifier = Modifier
                    .size(visualSize)
                    .offset(y = if (isPressed && canClick) 3.dp else 0.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = iconTint,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(iconSize),
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDescription,
                            tint = iconTint,
                            modifier = Modifier.size(iconSize),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean minimal hairline-bordered search input used across Books, Teachers, Classrooms & Blood Finder.
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
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val headingColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) accent else cardBorder,
        animationSpec = tween(180),
        label = "MinimalSearchBorder",
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(if (isFocused) 1.4.dp else 1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = modifier.height(48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = SearchIcon,
                contentDescription = null,
                tint = if (isFocused) accent else subTextColor,
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
                        color = subTextColor,
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
                        color = headingColor,
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
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
                        .size(36.dp)
                        .then(if (clearTestTag != null) Modifier.testTag(clearTestTag) else Modifier),
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Clear search",
                        tint = subTextColor,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

/**
 * Crisp minimal iconic filter chip (1.dp hairline border, clean active fill).
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
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) accent else cardBg,
        border = BorderStroke(1.dp, if (selected) accent else borderCol),
        modifier = modifier
            .height(38.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else accent,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = if (selected) Color.White else textColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Compact iconic stat badge for replacing oversized header banners.
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
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val headingColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderCol),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = text,
                color = headingColor,
                fontSize = 12.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Bold,
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
 * Clean responsive full-screen modal sheet matching ProfileScreen / WelcomeScreen aesthetic.
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
    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val headingColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    AppBackHandler(enabled = true) {
        if (dismissEnabled) onDismiss()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = screenBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Surface(
                color = cardBg,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                                modifier = Modifier.size(40.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(19.dp),
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = title,
                                    color = headingColor,
                                    fontSize = 17.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text = subtitle,
                                        color = subTextColor,
                                        fontSize = 11.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        WelcomeMinimal3DIconButton(
                            icon = CloseIcon,
                            contentDescription = "Close",
                            isDark = isDark,
                            iconTint = headingColor,
                            enabled = dismissEnabled,
                            testTag = closeTestTag,
                            onClick = onDismiss,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(cardBorder),
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
                    color = cardBg,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(cardBorder),
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
