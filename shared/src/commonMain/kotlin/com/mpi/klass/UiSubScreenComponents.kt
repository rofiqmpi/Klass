package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.CloseIcon

/**
 * Animated Sub-Screen Host supporting crisp slide-and-fade drill-down across Levels 1, 2, and 3.
 */
@Composable
internal fun KlassSubScreenHost(
    visible: Boolean,
    zIndex: Float = 40f,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { (it * 0.14f).toInt() }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { (it * 0.14f).toInt() }),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(zIndex),
    ) {
        content()
    }
}

/**
 * Rich, clean Hero Banner with tinted translucent backing, colored icon halo, bold title,
 * concise subtitle, and FlowRow status pills.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun KlassHeroBanner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    badges: List<Pair<String, Color>> = emptyList(),
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent
    val cardBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.84f)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, resolvedAccent.copy(alpha = if (isDark) 0.42f else 0.28f)),
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            resolvedAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
                            Color.Transparent,
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = resolvedAccent.copy(alpha = if (isDark) 0.24f else 0.15f),
                        border = BorderStroke(1.25.dp, resolvedAccent.copy(alpha = 0.48f)),
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = resolvedAccent,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = title,
                            color = cs.onSurface,
                            fontSize = 19.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                color = cs.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontFamily = hindSiliguri,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (trailingContent != null) {
                        trailingContent()
                    }
                }

                if (badges.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        badges.forEach { (badgeText, badgeColor) ->
                            val bc = if (badgeColor == Color.Unspecified) resolvedAccent else badgeColor
                            Surface(
                                shape = CircleShape,
                                color = bc.copy(alpha = if (isDark) 0.20f else 0.12f),
                                border = BorderStroke(0.75.dp, bc.copy(alpha = 0.35f)),
                            ) {
                                Text(
                                    text = badgeText,
                                    color = bc,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = hindSiliguri,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean, Spacious Drill-Down List Row (min 60.dp touch target) with colorful circular icon badge,
 * bold title, concise 1-line subtitle, optional colored pill badge, and chevron.
 */
@Composable
internal fun KlassNavigationRow(
    title: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = accent,
    showChevron: Boolean = true,
    testTag: String? = null,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent
    val resolvedBadge = if (badgeColor == Color.Unspecified) resolvedAccent else badgeColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = resolvedAccent.copy(alpha = if (isDark) 0.22f else 0.13f),
            border = BorderStroke(0.75.dp, resolvedAccent.copy(alpha = 0.32f)),
            modifier = Modifier.size(42.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = cs.onSurface,
                fontSize = 16.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = cs.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (!badgeText.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = resolvedBadge.copy(alpha = if (isDark) 0.20f else 0.12f),
                border = BorderStroke(0.75.dp, resolvedBadge.copy(alpha = 0.34f)),
            ) {
                Text(
                    text = badgeText,
                    color = resolvedBadge,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        if (showChevron) {
            Icon(
                imageVector = ChevronRightIcon,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.72f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Spacious Detail Field Row with colored circular icon badge, muted label, bold crisp value,
 * optional onClick (to open a Sub-Sub detail/action screen) and optional trailing action slot.
 */
@Composable
internal fun KlassDetailFieldRow(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = resolvedAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
            modifier = Modifier.size(38.dp),
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

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                color = cs.onSurfaceVariant,
                fontSize = 13.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                color = cs.onSurface,
                fontSize = 15.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (trailingContent != null) {
            Box(content = trailingContent)
        } else if (onClick != null) {
            Icon(
                imageVector = ChevronRightIcon,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Clean Empty State with colored halo icon, large readable title, concise subtitle, and optional CTA.
 */
@Composable
internal fun KlassEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionTestTag: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (accent == Color.Unspecified) cs.primary else accent

    MinimalCardSurface(
        isDark = isDark,
        borderColor = resolvedAccent.copy(alpha = if (isDark) 0.36f else 0.24f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = resolvedAccent.copy(alpha = if (isDark) 0.22f else 0.13f),
                border = BorderStroke(1.dp, resolvedAccent.copy(alpha = 0.35f)),
                modifier = Modifier.size(62.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = resolvedAccent,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Text(
                text = title,
                color = cs.onSurface,
                fontSize = 18.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = subtitle,
                color = cs.onSurfaceVariant,
                fontSize = 14.sp,
                fontFamily = hindSiliguri,
                textAlign = TextAlign.Center,
            )

            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                KlassTonalButton(
                    text = actionLabel,
                    accent = resolvedAccent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    testTag = actionTestTag,
                    onClick = onActionClick,
                )
            }
        }
    }
}

/**
 * Polished Confirm Dialog with semantic colored icon ring, bold heading, and calibrated action bar.
 */
@Composable
internal fun KlassConfirmDialog(
    title: String,
    message: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    confirmText: String,
    cancelText: String = "বাতিল",
    isDestructive: Boolean = false,
    confirmTestTag: String? = null,
    cancelTestTag: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val resolvedAccent = if (isDestructive) Color(0xFFE11D48) else accent

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = if (isDark) cs.surfaceContainerHigh else Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = resolvedAccent.copy(alpha = if (isDark) 0.22f else 0.13f),
                border = BorderStroke(1.dp, resolvedAccent.copy(alpha = 0.35f)),
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = resolvedAccent,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = cs.onSurface,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 15.sp,
                fontFamily = hindSiliguri,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            WelcomeMinimal3DButton(
                text = confirmText,
                hindSiliguri = hindSiliguri,
                isDestructive = isDestructive,
                isDark = isDark,
                customPrimaryColor = if (isDestructive) null else resolvedAccent,
                height = 44.dp,
                testTag = confirmTestTag,
                onClick = onConfirm,
                modifier = Modifier.widthIn(min = 108.dp),
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .height(44.dp)
                    .then(if (cancelTestTag != null) Modifier.testTag(cancelTestTag) else Modifier),
            ) {
                Text(
                    text = cancelText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = hindSiliguri,
                    color = cs.onSurfaceVariant,
                )
            }
        },
    )
}

/**
 * Full-Screen Option Picker Sub-Screen — replaces cramped dropdowns with spacious rainbow-backed list items.
 */
@Composable
internal fun <T> KlassSelectionPickerSubScreen(
    title: String,
    subtitle: String,
    levelLabel: String?,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    options: List<T>,
    selectedOption: T?,
    optionLabel: (T) -> String,
    optionSubtitle: ((T) -> String?)? = null,
    searchable: Boolean = options.size > 6,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }
    val filteredOptions = remember(options, query) {
        if (query.isBlank()) options
        else options.filter {
            optionLabel(it).contains(query, ignoreCase = true) ||
                (optionSubtitle?.invoke(it)?.contains(query, ignoreCase = true) == true)
        }
    }

    MinimalFullScreenSheet(
        title = title,
        subtitle = subtitle,
        levelLabel = levelLabel,
        icon = icon,
        accent = accent,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
    ) {
        if (searchable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                MinimalSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "খুঁজুন...",
                    accent = accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }
            itemsIndexed(filteredOptions) { idx, item ->
                val isSelected = item == selectedOption
                val rowBg = if (isSelected) {
                    accent.copy(alpha = if (isDark) 0.18f else 0.11f)
                } else if (isDark) {
                    cs.surfaceContainerLow.copy(alpha = 0.62f)
                } else {
                    Color.White.copy(alpha = 0.68f)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .clickable {
                                onSelect(item)
                                onDismiss()
                            }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = optionLabel(item),
                                color = if (isSelected) accent else cs.onSurface,
                                fontSize = 16.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val sub = optionSubtitle?.invoke(item)
                            if (!sub.isNullOrBlank()) {
                                Text(
                                    text = sub,
                                    color = cs.onSurfaceVariant,
                                    fontSize = 13.5.sp,
                                    fontFamily = hindSiliguri,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) accent else Color.Transparent,
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (isSelected) accent else cs.outlineVariant,
                            ),
                            modifier = Modifier.size(26.dp),
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = CheckIcon,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }
                        }
                    }
                    if (idx < filteredOptions.lastIndex) {
                        SocialInsetDivider(startIndent = 18.dp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/**
 * Slim Filter Summary Bar displayed when active filters exist, keeping primary screens clean.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun KlassFilterBadgeBar(
    badges: List<String>,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onClear: () -> Unit,
    clearTestTag: String? = null,
    modifier: Modifier = Modifier,
) {
    if (badges.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            badges.forEach { label ->
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.20f else 0.12f),
                    border = BorderStroke(0.75.dp, accent.copy(alpha = 0.34f)),
                ) {
                    Text(
                        text = label,
                        color = accent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Surface(
            onClick = onClear,
            shape = CircleShape,
            color = cs.error.copy(alpha = if (isDark) 0.18f else 0.10f),
            border = BorderStroke(0.75.dp, cs.error.copy(alpha = 0.35f)),
            modifier = Modifier
                .height(32.dp)
                .then(if (clearTestTag != null) Modifier.testTag(clearTestTag) else Modifier),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = CloseIcon,
                    contentDescription = "Clear filters",
                    tint = cs.error,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "রিসেট",
                    color = cs.error,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                )
            }
        }
    }
}
