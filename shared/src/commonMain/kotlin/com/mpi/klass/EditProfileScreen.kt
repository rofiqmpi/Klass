package com.mpi.klass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DatabaseIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.IdCardIcon
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.PrayingHandsIcon
import com.mpi.klass.theme.RingIcon
import com.mpi.klass.theme.SendIcon
import com.mpi.klass.theme.SolidUserIcon
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserTieIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import kotlin.math.roundToInt

// Exact HTML design palette & tokens (Light + Dark Mode optimized)
internal object HtmlProfileColors {
    val BsPrimary = Color(0xFF0D6EFD)
    val BsSuccess = Color(0xFF198754)
    val BsDanger = Color(0xFFDC3545)
    val BsWarning = Color(0xFFFFC107)
    val BsInfo = Color(0xFF0DCAF0)
    val BsSecondary = Color(0xFF6C757D)

    val PurpleAccent = Color(0xFF7928CA)
    val PinkAccent = Color(0xFFFF758C)
    val HeadingDark = Color(0xFF1A202C)
    val SubheadingSlate = Color(0xFF4A5568)
    val FormLabelColor = Color(0xFF34495E)
    val InfoLabelGray = Color(0xFF718096)
    val InputBorderGray = Color(0xFFE0E0E0)
    val CardBorderGray = Color(0xFFEDF2F7)
    val SubmitPink = Color(0xFFF857A6)
    val SubmitRed = Color(0xFFFF5858)
}

internal data class HtmlProfilePalette(
    val isDark: Boolean,
    val bsPrimary: Color,
    val bsSuccess: Color,
    val bsDanger: Color,
    val bsWarning: Color,
    val bsInfo: Color,
    val bsSecondary: Color,
    val purpleAccent: Color,
    val purpleHoverTint: Color,
    val pinkAccent: Color,
    val headingColor: Color,
    val subheadingColor: Color,
    val formLabelColor: Color,
    val infoLabelColor: Color,
    val emptyValueColor: Color,
    val placeholderColor: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val inputBg: Color,
    val inputHoverBg: Color,
    val inputBorder: Color,
    val overlayWash: Color,
    val brandBarBg: Color,
    val brandBarBorder: Color,
    val avatarBg: Color,
    val sectionGradientStart: Color,
    val dropdownBg: Color,
    val dropdownBorder: Color,
    val haloAlphaTarget: Float,
) {
    companion object {
        fun of(isDark: Boolean): HtmlProfilePalette = if (isDark) {
            HtmlProfilePalette(
                isDark = true,
                bsPrimary = Color(0xFF60A5FA),
                bsSuccess = Color(0xFF34D399),
                bsDanger = Color(0xFFF87171),
                bsWarning = Color(0xFFFBBF24),
                bsInfo = Color(0xFF38BDF8),
                bsSecondary = Color(0xFF94A3B8),
                purpleAccent = Color(0xFFC084FC),
                purpleHoverTint = Color(0xFFD8B4FE),
                pinkAccent = Color(0xFFFF758C),
                headingColor = Color(0xFFF8FAFC),
                subheadingColor = Color(0xFFCBD5E1),
                formLabelColor = Color(0xFFE2E8F0),
                infoLabelColor = Color(0xFF94A3B8),
                emptyValueColor = Color(0xFF64748B),
                placeholderColor = Color(0xFF64748B),
                cardBg = Color(0xFF1E293B),
                cardBorder = Color(0xFF334155),
                inputBg = Color(0xFF1E293B),
                inputHoverBg = Color(0xFF2A2444),
                inputBorder = Color(0xFF334155),
                overlayWash = Color(0xFF0B1120).copy(alpha = 0.90f),
                brandBarBg = Color(0xFF1E293B),
                brandBarBorder = Color(0xFFC084FC).copy(alpha = 0.28f),
                avatarBg = Color(0xFF0F172A),
                sectionGradientStart = Color(0xFFA855F7).copy(alpha = 0.18f),
                dropdownBg = Color(0xFF1E293B),
                dropdownBorder = Color(0xFFC084FC).copy(alpha = 0.35f),
                haloAlphaTarget = 0.26f,
            )
        } else {
            HtmlProfilePalette(
                isDark = false,
                bsPrimary = HtmlProfileColors.BsPrimary,
                bsSuccess = HtmlProfileColors.BsSuccess,
                bsDanger = HtmlProfileColors.BsDanger,
                bsWarning = HtmlProfileColors.BsWarning,
                bsInfo = HtmlProfileColors.BsInfo,
                bsSecondary = HtmlProfileColors.BsSecondary,
                purpleAccent = HtmlProfileColors.PurpleAccent,
                purpleHoverTint = HtmlProfileColors.PurpleAccent,
                pinkAccent = HtmlProfileColors.PinkAccent,
                headingColor = HtmlProfileColors.HeadingDark,
                subheadingColor = HtmlProfileColors.SubheadingSlate,
                formLabelColor = HtmlProfileColors.FormLabelColor,
                infoLabelColor = HtmlProfileColors.InfoLabelGray,
                emptyValueColor = Color(0xFF94A3B8),
                placeholderColor = Color(0xFF94A3B8),
                cardBg = Color.White,
                cardBorder = HtmlProfileColors.CardBorderGray,
                inputBg = Color.White,
                inputHoverBg = Color(0xFFFAF6FF),
                inputBorder = HtmlProfileColors.InputBorderGray,
                overlayWash = Color.White.copy(alpha = 0.92f),
                brandBarBg = Color.White,
                brandBarBorder = HtmlProfileColors.PurpleAccent.copy(alpha = 0.10f),
                avatarBg = Color.White,
                sectionGradientStart = HtmlProfileColors.PurpleAccent.copy(alpha = 0.08f),
                dropdownBg = Color.White,
                dropdownBorder = HtmlProfileColors.PurpleAccent.copy(alpha = 0.25f),
                haloAlphaTarget = 0.18f,
            )
        }
    }
}

internal val LocalHtmlProfilePalette = compositionLocalOf { HtmlProfilePalette.of(false) }

@Composable
internal fun HtmlProfilePageWrapper(
    topContentPadding: Dp,
    isDark: Boolean = false,
    horizontalPadding: Dp = 44.dp,
    onBackgroundTap: () -> Unit = {},
    brandBar: @Composable (collapseProgress: Float) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = remember(isDark) { HtmlProfilePalette.of(isDark) }
    val bgBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF1E1B4B),
                    0.25f to Color(0xFF311042),
                    0.50f to Color(0xFF0F172A),
                    0.75f to Color(0xFF0C4A6E),
                    1.0f to Color(0xFF134E4A),
                ),
                start = Offset.Zero,
                end = Offset(1200f, 1800f),
            )
        } else {
            Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFFFF9A9E),
                    0.20f to Color(0xFFFAD0C4),
                    0.40f to Color(0xFFFFD1FF),
                    0.60f to Color(0xFFA1C4FD),
                    0.80f to Color(0xFFC2E9FB),
                    1.0f to Color(0xFFD4FC79),
                ),
                start = Offset.Zero,
                end = Offset(1200f, 1800f),
            )
        }
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val collapseThresholdPx = with(density) { 36.dp.toPx() }
    val isCollapsed by remember(collapseThresholdPx) {
        derivedStateOf {
            scrollState.value > collapseThresholdPx
        }
    }
    val collapseProgress by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "BrandBarCollapseProgress",
    )

    val startTopOffset = if (topContentPadding > 0.dp) topContentPadding else 20.dp
    val endTopOffset = (startTopOffset - 22.dp).coerceAtLeast(8.dp)
    val animatedTopOffset = lerp(startTopOffset, endTopOffset, collapseProgress)

    CompositionLocalProvider(LocalHtmlProfilePalette provides palette) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.overlayWash)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackgroundTap,
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(scrollState)
                        .padding(horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (topContentPadding > 0.dp) {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        Spacer(modifier = Modifier.height(topContentPadding + 88.dp))
                    } else {
                        Spacer(modifier = Modifier.height(104.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        content()
                    }
                }

                // Floating Brand Card Bar that glides toward the transparent header & smoothly morphs to 1:1 avatar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .offset(y = animatedTopOffset)
                        .zIndex(5f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    brandBar(collapseProgress)
                }
            }
        }
    }
}

@Composable
internal fun HtmlBrandCardBar(
    photoUrl: String?,
    avatarSeed: String,
    buttonText: String,
    buttonIcon: ImageVector,
    hindSiliguri: FontFamily,
    testTag: String,
    collapseProgress: Float = 0f,
    isSyncing: Boolean = false,
    onAvatarClick: () -> Unit,
    onButtonClick: () -> Unit,
) {
    val palette = LocalHtmlProfilePalette.current
    val clampedProgress = collapseProgress.coerceIn(0f, 1f)
    val avatarSize = lerp(55.dp, 46.dp, clampedProgress)
    val barPadding = 8.dp // Identical 8dp padding on top, bottom, start, and end

    Surface(
        shape = CircleShape,
        color = palette.brandBarBg,
        shadowElevation = lerp(10.dp, 14.dp, clampedProgress),
        border = BorderStroke(if (palette.isDark) 1.5.dp else 2.dp, palette.brandBarBorder),
    ) {
        Row(
            modifier = Modifier.padding(all = barPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(avatarSize),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    onClick = onAvatarClick,
                    shape = CircleShape,
                    color = palette.avatarBg,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("profile_avatar_button"),
                ) {
                    val resolvedModel = resolveProfileImageModel(photoUrl)
                    if (resolvedModel != null) {
                        AsyncImage(
                            model = resolvedModel,
                            contentDescription = "গ্যালারি থেকে ছবি আপলোড".localized(),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        DynamicAvatar(
                            seed = avatarSeed.ifBlank { "MPI" },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(CircleShape),
                        )
                    }
                }
                if (isSyncing) {
                    CircularProgressIndicator(
                        color = palette.purpleAccent.copy(alpha = 0.88f),
                        trackColor = palette.purpleAccent.copy(alpha = 0.15f),
                        strokeWidth = 2.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Fade the button completely out BEFORE shrinking width so it never looks sliced from the right
            val fadeFraction = (1f - (clampedProgress / 0.35f)).coerceIn(0f, 1f)
            val widthProgress = ((clampedProgress - 0.28f) / 0.72f).coerceIn(0f, 1f)
            val revealFraction = 1f - widthProgress
            val buttonScale = 1f - 0.14f * (1f - fadeFraction)
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minWidth = 0))
                        val visibleWidth = (placeable.width * revealFraction).roundToInt()
                        layout(visibleWidth, placeable.height) {
                            if (visibleWidth > 0) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                    }
                    .graphicsLayer(
                        alpha = fadeFraction,
                        scaleX = buttonScale,
                        scaleY = buttonScale,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp, end = 6.dp),
                ) {
                    Surface(
                        onClick = onButtonClick,
                        enabled = clampedProgress < 0.25f,
                        shape = RoundedCornerShape(26.dp),
                        color = Color.Transparent,
                        shadowElevation = 5.dp,
                        modifier = Modifier
                            .height(42.dp) // Balanced compact height, still smaller than the 55.dp profile image
                            .testTag(testTag),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(HtmlProfileColors.PurpleAccent, HtmlProfileColors.PinkAccent),
                                        start = Offset.Zero,
                                        end = Offset(260f, 100f),
                                    )
                                )
                                .padding(horizontal = 15.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = buttonIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = buttonText,
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HtmlSectionTitle(
    title: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val palette = LocalHtmlProfilePalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 18.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(palette.sectionGradientStart, Color.Transparent),
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(palette.pinkAccent),
        )
        Text(
            text = title,
            color = palette.purpleAccent,
            fontSize = 19.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        )
    }
}

@Composable
internal fun HtmlGradientSubmitButton(
    text: String,
    icon: ImageVector,
    hindSiliguri: FontFamily,
    testTag: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = { if (enabled) onClick() },
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .heightIn(min = 54.dp)
                .testTag(testTag),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                HtmlProfileColors.SubmitPink,
                                HtmlProfileColors.SubmitRed,
                                HtmlProfileColors.PurpleAccent,
                            ),
                            start = Offset.Zero,
                            end = Offset(800f, 300f),
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

@Composable
internal fun HtmlProfileFooter(hindSiliguri: FontFamily) {
    val palette = LocalHtmlProfilePalette.current
    Text(
        text = "© 2026 Mymensingh Polytechnic Institute | Designed by Klass",
        color = palette.subheadingColor,
        fontSize = 14.sp,
        fontFamily = hindSiliguri,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp, bottom = 28.dp),
    )
}

internal fun formatProfileDob(rawDob: String, isBengali: Boolean): String {
    val clean = rawDob.trim()
    if (clean.isBlank()) return ""
    val parts = clean.split("-")
    if (parts.size != 3) return clean
    val year = parts[0].toIntOrNull() ?: return clean
    val month = parts[1].toIntOrNull() ?: return clean
    val day = parts[2].toIntOrNull() ?: return clean

    val bnMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর",
    )
    val enMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
    val monthIdx = (month - 1).coerceIn(0, 11)
    return if (isBengali) {
        "${toBengaliDigits(day.toString())} ${bnMonths[monthIdx]}, ${toBengaliDigits(year.toString())}"
    } else {
        "$day ${enMonths[monthIdx]}, $year"
    }
}

private fun toBengaliDigits(input: String): String {
    val map = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(input.length)
    for (c in input) {
        if (c in '0'..'9') sb.append(map[c - '0']) else sb.append(c)
    }
    return sb.toString()
}

private fun normalizeSemesterOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    val num = clean.filter { it.isDigit() }
    return when (num) {
        "1" -> "১ম সেমিস্টার"
        "2" -> "২য় সেমিস্টার"
        "3" -> "৩য় সেমিস্টার"
        "4" -> "৪র্থ সেমিস্টার"
        "5" -> "৫ম সেমিস্টার"
        "6" -> "৬ষ্ঠ সেমিস্টার"
        "7" -> "৭ম সেমিস্টার"
        "8" -> ""
        else -> when {
            clean.contains("১ম") || clean.startsWith("1", true) -> "১ম সেমিস্টার"
            clean.contains("২য়") || clean.contains("২য়") || clean.startsWith("2", true) -> "২য় সেমিস্টার"
            clean.contains("৩য়") || clean.contains("৩য়") || clean.startsWith("3", true) -> "৩য় সেমিস্টার"
            clean.contains("৪র্থ") || clean.startsWith("4", true) -> "৪র্থ সেমিস্টার"
            clean.contains("৫ম") || clean.startsWith("5", true) -> "৫ম সেমিস্টার"
            clean.contains("৬ষ্ঠ") || clean.startsWith("6", true) -> "৬ষ্ঠ সেমিস্টার"
            clean.contains("৭ম") || clean.startsWith("7", true) -> "৭ম সেমিস্টার"
            clean.contains("৮ম") || clean.startsWith("8", true) -> ""
            else -> clean
        }
    }
}

private fun normalizeDepartmentOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean.equals("CST", true) || clean.startsWith("CST ", true) -> "CST (Computer Science & Technology)"
        clean.equals("CT", true) || clean.startsWith("CT ", true) -> "CT (Civil Technology)"
        clean.equals("ET", true) || clean.startsWith("ET ", true) -> "ET (Electrical Technology)"
        clean.equals("MT", true) || clean.startsWith("MT ", true) -> "MT (Mechanical Technology)"
        clean.equals("PT", true) || clean.startsWith("PT ", true) -> "PT (Power Technology)"
        clean.equals("ENT", true) || clean.startsWith("ENT ", true) -> "ENT (Electronics Technology)"
        clean.equals("EMT", true) || clean.startsWith("EMT ", true) -> "EMT (Electro-Medical Technology)"
        clean.contains("নন টেক", ignoreCase = true) || clean.contains("নন-টেক", ignoreCase = true) || clean.equals("Non-Tech", true) || clean.equals("Non Tech", true) -> "নন টেক"
        else -> clean
    }
}

private fun normalizeShiftOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean == "1" || clean.contains("1st", true) || clean.contains("১ম") || clean.contains("Morning", true) -> "১ম শিফট"
        clean == "2" || clean.contains("2nd", true) || clean.contains("২য়") || clean.contains("২য়") || clean.contains("Evening", true) -> "২য় শিফট"
        else -> clean
    }
}

private fun normalizeSectionOption(raw: String, dept: String = ""): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return RoutineRules.normalizeGroup(dept, clean)
}

@Composable
fun EditProfileContent(
    initialProfile: StudentProfile,
    photoUrl: String?,
    fallbackName: String,
    fallbackEmail: String,
    role: UserRole = UserRole.STUDENT,
    hindSiliguri: FontFamily,
    topContentPadding: Dp,
    isSaving: Boolean,
    isDark: Boolean = false,
    onAvatarClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    onSaveProfile: (StudentProfile) -> Unit,
) {
    val palette = remember(isDark) { HtmlProfilePalette.of(isDark) }
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val focusManager = LocalFocusManager.current
    var activeFieldTag by remember { mutableStateOf<String?>(null) }
    var hoverResetKey by remember { mutableIntStateOf(0) }

    val clearFieldHighlight = {
        activeFieldTag = null
        hoverResetKey++
        focusManager.clearFocus(force = true)
    }
    val activateField: (String) -> Unit = { tag ->
        if (activeFieldTag != tag) {
            focusManager.clearFocus(force = false)
        }
        activeFieldTag = tag
    }

    var hasUserEdited by remember(fallbackEmail) { mutableStateOf(false) }
    var hasEditedCurrentAddress by remember(fallbackEmail) { mutableStateOf(false) }
    var fullName by remember(fallbackEmail) { mutableStateOf(initialProfile.name.ifBlank { fallbackName }) }
    var fatherName by remember(fallbackEmail) { mutableStateOf(initialProfile.fatherName) }
    var motherName by remember(fallbackEmail) { mutableStateOf(initialProfile.motherName) }
    var dob by remember(fallbackEmail) { mutableStateOf(initialProfile.dob) }
    var bloodGroup by remember(fallbackEmail) { mutableStateOf(initialProfile.bloodGroup) }
    var religion by remember(fallbackEmail) { mutableStateOf(initialProfile.religion) }
    var gender by remember(fallbackEmail) { mutableStateOf(initialProfile.gender) }
    var maritalStatus by remember(fallbackEmail) { mutableStateOf(initialProfile.maritalStatus.ifBlank { "অবিবাহিত (Unmarried)" }) }
    var phone by remember(fallbackEmail) { mutableStateOf(initialProfile.phone) }
    var contactEmail by remember(fallbackEmail) { mutableStateOf(initialProfile.contactEmail.ifBlank { fallbackEmail }) }
    var currentAddress by remember(fallbackEmail) { mutableStateOf(initialProfile.address) }
    var permanentAddress by remember(fallbackEmail) { mutableStateOf(initialProfile.permanentAddress) }

    var department by remember(fallbackEmail) { mutableStateOf(normalizeDepartmentOption(initialProfile.department)) }
    var semester by remember(fallbackEmail) { mutableStateOf(normalizeSemesterOption(initialProfile.semester)) }
    var shift by remember(fallbackEmail) { mutableStateOf(normalizeShiftOption(initialProfile.shift)) }
    var section by remember(fallbackEmail) {
        val normDept = normalizeDepartmentOption(initialProfile.department)
        mutableStateOf(normalizeSectionOption(initialProfile.section, normDept))
    }
    var rollNumber by remember(fallbackEmail) { mutableStateOf(initialProfile.rollNumber) }
    var registrationNumber by remember(fallbackEmail) { mutableStateOf(initialProfile.registrationNumber) }

    LaunchedEffect(initialProfile) {
        if (!hasUserEdited) {
            fullName = initialProfile.name.ifBlank { fallbackName }
            fatherName = initialProfile.fatherName
            motherName = initialProfile.motherName
            dob = initialProfile.dob
            bloodGroup = initialProfile.bloodGroup
            religion = initialProfile.religion
            gender = initialProfile.gender
            maritalStatus = initialProfile.maritalStatus.ifBlank { "অবিবাহিত (Unmarried)" }
            phone = initialProfile.phone
            contactEmail = initialProfile.contactEmail.ifBlank { fallbackEmail }
            currentAddress = initialProfile.address
            permanentAddress = initialProfile.permanentAddress
            val normDept = normalizeDepartmentOption(initialProfile.department)
            department = normDept
            semester = normalizeSemesterOption(initialProfile.semester)
            shift = normalizeShiftOption(initialProfile.shift)
            section = normalizeSectionOption(initialProfile.section, normDept)
            rollNumber = initialProfile.rollNumber
            registrationNumber = initialProfile.registrationNumber
        }
    }

    // Live GPS tracking updates Current Address automatically unless user manually edited it
    LaunchedEffect(initialProfile.address) {
        if (!hasEditedCurrentAddress && initialProfile.address.isNotBlank()) {
            currentAddress = initialProfile.address
        }
    }

    val launchDatePicker = rememberDatePickerLauncher(initialDate = dob) { picked ->
        hasUserEdited = true
        dob = picked
    }

    val bloodOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    val religionOptions = listOf("ইসলাম", "হিন্দুধর্ম", "বৌদ্ধধর্ম", "খ্রিস্টধর্ম", "অন্যান্য")
    val genderOptions = listOf("পুরুষ (Male)", "মহিলা (Female)", "অন্যান্য (Other)")
    val maritalOptions = listOf("অবিবাহিত (Unmarried)", "বিবাহিত (Married)")
    val baseDeptOptions = listOf(
        "CST (Computer Science & Technology)",
        "CT (Civil Technology)",
        "ET (Electrical Technology)",
        "MT (Mechanical Technology)",
        "PT (Power Technology)",
        "ENT (Electronics Technology)",
        "EMT (Electro-Medical Technology)",
    )
    val deptOptions = if (role == UserRole.TEACHER) baseDeptOptions + "নন টেক" else baseDeptOptions
    val semesterOptions = listOf(
        "১ম সেমিস্টার", "২য় সেমিস্টার", "৩য় সেমিস্টার", "৪র্থ সেমিস্টার",
        "৫ম সেমিস্টার", "৬ষ্ঠ সেমিস্টার", "৭ম সেমিস্টার",
    )
    val shiftOptions = listOf("১ম শিফট", "২য় শিফট")
    val sectionOptions = remember(department) { RoutineRules.groupsForDepartment(department) }

    HtmlProfilePageWrapper(
        topContentPadding = topContentPadding,
        isDark = isDark,
        onBackgroundTap = clearFieldHighlight,
        brandBar = { collapseProgress ->
            HtmlBrandCardBar(
                photoUrl = photoUrl,
                avatarSeed = fallbackEmail.ifBlank { fullName },
                buttonText = "প্রোফাইল দেখুন".localized(),
                buttonIcon = DatabaseIcon,
                hindSiliguri = hindSiliguri,
                testTag = "edit_to_view_profile_button",
                collapseProgress = collapseProgress,
                isSyncing = isSaving,
                onAvatarClick = {
                    clearFieldHighlight()
                    onAvatarClick()
                },
                onButtonClick = {
                    clearFieldHighlight()
                    onViewProfileClick()
                },
            )
        },
    ) {
        // Header Section Titles
        Text(
            text = "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট".localized(),
            color = palette.headingColor,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "আপনার প্রোফাইল সম্পাদনা করুন".localized(),
            color = palette.subheadingColor,
            fontSize = 17.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main Responsive Form Content
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth >= 840.dp -> 3
                maxWidth >= 560.dp -> 2
                else -> 1
            }

            val showPersonalExtended = role != UserRole.ADMIN
            val showFamilySection = role == UserRole.STUDENT
            val showAcademicSection = role != UserRole.ADMIN
            val showFullAcademicFields = role == UserRole.STUDENT

            val contactSectionNum = if (showFamilySection) "৩" else "২"
            val addressSectionNum = if (showFamilySection) "৪" else "৩"
            val academicSectionNum = if (showFamilySection) "৫" else "৪"

            val fullNameLabel = when (role) {
                UserRole.STUDENT -> "শিক্ষার্থীর পূর্ণ নাম (Full Name)"
                UserRole.TEACHER -> "শিক্ষকের পূর্ণ নাম (Full Name)"
                UserRole.ADMIN -> "পূর্ণ নাম (Full Name)"
            }.localized()

            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 1: Personal Information
                HtmlSectionTitle(
                    title = "১. ব্যক্তিগত তথ্য (Personal Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val personalItems = buildList<Pair<Int, @Composable () -> Unit>> {
                    add(
                        cols to {
                            HtmlFormTextField(
                                label = fullNameLabel,
                                icon = SolidUserIcon,
                                iconTint = HtmlProfileColors.BsPrimary,
                                value = fullName,
                                placeholder = "আপনার পুরো নাম লিখুন".localized(),
                                onValueChange = { hasUserEdited = true; fullName = it },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_fullname_input",
                                isActive = activeFieldTag == "edit_fullname_input",
                                hoverResetKey = hoverResetKey,
                                onActivate = { activateField("edit_fullname_input") },
                                onDeactivate = { if (activeFieldTag == "edit_fullname_input") activeFieldTag = null },
                            )
                        }
                    )
                    add(
                        1 to {
                            val dobDisplay = if (dob.isNotBlank()) formatProfileDob(dob, isBengali) else ""
                            HtmlFormDatePickerField(
                                label = "জন্ম তারিখ".localized(),
                                icon = CalendarDaysIcon,
                                iconTint = HtmlProfileColors.BsWarning,
                                value = dobDisplay,
                                placeholder = "mm/dd/yyyy",
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_dob_picker",
                                isActive = activeFieldTag == "edit_dob_picker",
                                hoverResetKey = hoverResetKey,
                                onClick = {
                                    activateField("edit_dob_picker")
                                    launchDatePicker()
                                },
                            )
                        }
                    )
                    add(
                        1 to {
                            HtmlFormSelectField(
                                label = "রক্তের গ্রুপ".localized(),
                                icon = DropletIcon,
                                iconTint = HtmlProfileColors.BsDanger,
                                value = bloodGroup,
                                placeholder = "গ্রুপ নির্বাচন করুন".localized(),
                                options = bloodOptions,
                                translateOptions = false,
                                onSelect = { hasUserEdited = true; bloodGroup = it },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_blood_group_select",
                                isActive = activeFieldTag == "edit_blood_group_select",
                                hoverResetKey = hoverResetKey,
                                onActivate = { activateField("edit_blood_group_select") },
                                onDismissActive = { if (activeFieldTag == "edit_blood_group_select") activeFieldTag = null },
                            )
                        }
                    )
                    if (showPersonalExtended) {
                        add(
                            1 to {
                                HtmlFormSelectField(
                                    label = "ধর্ম".localized(),
                                    icon = PrayingHandsIcon,
                                    iconTint = HtmlProfileColors.BsInfo,
                                    value = religion,
                                    placeholder = "ধর্ম নির্বাচন করুন".localized(),
                                    options = religionOptions,
                                    translateOptions = true,
                                    onSelect = { hasUserEdited = true; religion = it },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_religion_select",
                                    isActive = activeFieldTag == "edit_religion_select",
                                    hoverResetKey = hoverResetKey,
                                    onActivate = { activateField("edit_religion_select") },
                                    onDismissActive = { if (activeFieldTag == "edit_religion_select") activeFieldTag = null },
                                )
                            }
                        )
                    }
                    add(
                        1 to {
                            HtmlFormSelectField(
                                label = "লিঙ্গ (Gender)".localized(),
                                icon = VenusMarsIcon,
                                iconTint = HtmlProfileColors.BsDanger,
                                value = gender,
                                placeholder = "লিঙ্গ নির্বাচন করুন".localized(),
                                options = genderOptions,
                                translateOptions = true,
                                onSelect = { hasUserEdited = true; gender = it },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_gender_select",
                                isActive = activeFieldTag == "edit_gender_select",
                                hoverResetKey = hoverResetKey,
                                onActivate = { activateField("edit_gender_select") },
                                onDismissActive = { if (activeFieldTag == "edit_gender_select") activeFieldTag = null },
                            )
                        }
                    )
                    if (showPersonalExtended) {
                        add(
                            1 to {
                                HtmlFormSelectField(
                                    label = "বৈবাহিক অবস্থা".localized(),
                                    icon = RingIcon,
                                    iconTint = HtmlProfileColors.BsSecondary,
                                    value = maritalStatus,
                                    placeholder = "অবিবাহিত (Unmarried)".localized(),
                                    options = maritalOptions,
                                    translateOptions = true,
                                    onSelect = { hasUserEdited = true; maritalStatus = it },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_marital_select",
                                    isActive = activeFieldTag == "edit_marital_select",
                                    hoverResetKey = hoverResetKey,
                                    onActivate = { activateField("edit_marital_select") },
                                    onDismissActive = { if (activeFieldTag == "edit_marital_select") activeFieldTag = null },
                                )
                            }
                        )
                    }
                }

                HtmlResponsiveGrid(columns = cols, items = personalItems, spacing = 16.dp)

                if (showFamilySection) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Section 2: Family Information (Students only)
                    HtmlSectionTitle(
                        title = "২. পারিবারিক তথ্য (Family Information)".localized(),
                        hindSiliguri = hindSiliguri,
                    )

                    val familyItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                        1 to {
                            HtmlFormTextField(
                                label = "পিতার নাম (Father's Name)".localized(),
                                icon = UserTieIcon,
                                iconTint = HtmlProfileColors.BsSuccess,
                                value = fatherName,
                                placeholder = "পিতার নাম লিখুন".localized(),
                                onValueChange = { hasUserEdited = true; fatherName = it },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_father_name_input",
                                isActive = activeFieldTag == "edit_father_name_input",
                                hoverResetKey = hoverResetKey,
                                onActivate = { activateField("edit_father_name_input") },
                                onDeactivate = { if (activeFieldTag == "edit_father_name_input") activeFieldTag = null },
                            )
                        },
                        1 to {
                            HtmlFormTextField(
                                label = "মাতার নাম (Mother's Name)".localized(),
                                icon = UserNurseIcon,
                                iconTint = HtmlProfileColors.BsDanger,
                                value = motherName,
                                placeholder = "মাতার নাম লিখুন".localized(),
                                onValueChange = { hasUserEdited = true; motherName = it },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_mother_name_input",
                                isActive = activeFieldTag == "edit_mother_name_input",
                                hoverResetKey = hoverResetKey,
                                onActivate = { activateField("edit_mother_name_input") },
                                onDeactivate = { if (activeFieldTag == "edit_mother_name_input") activeFieldTag = null },
                            )
                        },
                    )

                    HtmlResponsiveGrid(columns = cols, items = familyItems, spacing = 16.dp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Contact Information
                HtmlSectionTitle(
                    title = "$contactSectionNum. যোগাযোগের তথ্য (Contact Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val contactItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    1 to {
                        HtmlFormTextField(
                            label = "মোবাইল নম্বর".localized(),
                            icon = PhoneSolidIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            value = phone,
                            placeholder = "01XXXXXXXXX",
                            keyboardType = KeyboardType.Phone,
                            onValueChange = { hasUserEdited = true; phone = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_phone_input",
                            isActive = activeFieldTag == "edit_phone_input",
                            hoverResetKey = hoverResetKey,
                            onActivate = { activateField("edit_phone_input") },
                            onDeactivate = { if (activeFieldTag == "edit_phone_input") activeFieldTag = null },
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "ইমেল ঠিকানা (Email)".localized(),
                            icon = EnvelopeSolidIcon,
                            iconTint = HtmlProfileColors.BsPrimary,
                            value = contactEmail,
                            placeholder = "example@gmail.com",
                            keyboardType = KeyboardType.Email,
                            onValueChange = { hasUserEdited = true; contactEmail = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_email_input",
                            isActive = activeFieldTag == "edit_email_input",
                            hoverResetKey = hoverResetKey,
                            onActivate = { activateField("edit_email_input") },
                            onDeactivate = { if (activeFieldTag == "edit_email_input") activeFieldTag = null },
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = contactItems, spacing = 16.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Address Information
                HtmlSectionTitle(
                    title = "$addressSectionNum. ঠিকানার তথ্য (Address Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val addressItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    1 to {
                        HtmlFormTextField(
                            label = "বর্তমান ঠিকানা".localized(),
                            icon = LocationDotIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = currentAddress,
                            placeholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized(),
                            onValueChange = { hasUserEdited = true; hasEditedCurrentAddress = true; currentAddress = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_current_address_input",
                            isActive = activeFieldTag == "edit_current_address_input",
                            hoverResetKey = hoverResetKey,
                            onActivate = { activateField("edit_current_address_input") },
                            onDeactivate = { if (activeFieldTag == "edit_current_address_input") activeFieldTag = null },
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "স্থায়ী ঠিকানা".localized(),
                            icon = LocationDotIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = permanentAddress,
                            placeholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized(),
                            onValueChange = { hasUserEdited = true; permanentAddress = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_permanent_address_input",
                            isActive = activeFieldTag == "edit_permanent_address_input",
                            hoverResetKey = hoverResetKey,
                            onActivate = { activateField("edit_permanent_address_input") },
                            onDeactivate = { if (activeFieldTag == "edit_permanent_address_input") activeFieldTag = null },
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = addressItems, spacing = 16.dp)

                if (showAcademicSection) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Academic Information
                    HtmlSectionTitle(
                        title = "$academicSectionNum. একাডেমিক তথ্য (Academic Information)".localized(),
                        hindSiliguri = hindSiliguri,
                    )

                    val academicItems = buildList<Pair<Int, @Composable () -> Unit>> {
                        add(
                            1 to {
                                HtmlFormSelectField(
                                    label = "ডিপার্টমেন্ট (Department)".localized(),
                                    icon = GraduationCapIcon,
                                    iconTint = HtmlProfileColors.BsPrimary,
                                    value = department,
                                    placeholder = "ডিপার্টমেন্ট নির্বাচন করুন".localized(),
                                    options = deptOptions,
                                    translateOptions = false,
                                    onSelect = {
                                        hasUserEdited = true
                                        department = it
                                        if (section.isNotBlank()) {
                                            section = RoutineRules.normalizeGroup(it, section)
                                        }
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_department_select",
                                    isActive = activeFieldTag == "edit_department_select",
                                    hoverResetKey = hoverResetKey,
                                    onActivate = { activateField("edit_department_select") },
                                    onDismissActive = { if (activeFieldTag == "edit_department_select") activeFieldTag = null },
                                )
                            }
                        )
                        if (showFullAcademicFields) {
                            add(
                                1 to {
                                    HtmlFormSelectField(
                                        label = "সেমিস্টার".localized(),
                                        icon = LayerGroupIcon,
                                        iconTint = HtmlProfileColors.BsSuccess,
                                        value = semester,
                                        placeholder = "সেমিস্টার নির্বাচন করুন".localized(),
                                        options = semesterOptions,
                                        translateOptions = true,
                                        onSelect = { hasUserEdited = true; semester = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_semester_select",
                                        isActive = activeFieldTag == "edit_semester_select",
                                        hoverResetKey = hoverResetKey,
                                        onActivate = { activateField("edit_semester_select") },
                                        onDismissActive = { if (activeFieldTag == "edit_semester_select") activeFieldTag = null },
                                    )
                                }
                            )
                        }
                        add(
                            1 to {
                                HtmlFormSelectField(
                                    label = "শিফট".localized(),
                                    icon = ClockIcon,
                                    iconTint = HtmlProfileColors.BsWarning,
                                    value = shift,
                                    placeholder = "শিফট নির্বাচন করুন".localized(),
                                    options = shiftOptions,
                                    translateOptions = true,
                                    onSelect = { hasUserEdited = true; shift = it },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_shift_select",
                                    isActive = activeFieldTag == "edit_shift_select",
                                    hoverResetKey = hoverResetKey,
                                    onActivate = { activateField("edit_shift_select") },
                                    onDismissActive = { if (activeFieldTag == "edit_shift_select") activeFieldTag = null },
                                )
                            }
                        )
                        if (showFullAcademicFields) {
                            add(
                                1 to {
                                    HtmlFormSelectField(
                                        label = "সেকশন (Section)".localized(),
                                        icon = UsersRectangleIcon,
                                        iconTint = HtmlProfileColors.BsSecondary,
                                        value = section,
                                        placeholder = "সেকশন নির্বাচন করুন".localized(),
                                        options = sectionOptions,
                                        translateOptions = false,
                                        onSelect = { hasUserEdited = true; section = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_section_select",
                                        isActive = activeFieldTag == "edit_section_select",
                                        hoverResetKey = hoverResetKey,
                                        onActivate = { activateField("edit_section_select") },
                                        onDismissActive = { if (activeFieldTag == "edit_section_select") activeFieldTag = null },
                                    )
                                }
                            )
                            add(
                                1 to {
                                    HtmlFormTextField(
                                        label = "বোর্ড রোল নম্বর (Roll)".localized(),
                                        icon = IdCardIcon,
                                        iconTint = HtmlProfileColors.BsDanger,
                                        value = rollNumber,
                                        placeholder = "যেমন: ৬৪৮১২৩".localized(),
                                        onValueChange = { hasUserEdited = true; rollNumber = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_roll_input",
                                        isActive = activeFieldTag == "edit_roll_input",
                                        hoverResetKey = hoverResetKey,
                                        onActivate = { activateField("edit_roll_input") },
                                        onDeactivate = { if (activeFieldTag == "edit_roll_input") activeFieldTag = null },
                                    )
                                }
                            )
                            add(
                                1 to {
                                    HtmlFormTextField(
                                        label = "রেজিস্ট্রেশন নম্বর (Registration)".localized(),
                                        icon = FileLinesIcon,
                                        iconTint = HtmlProfileColors.BsInfo,
                                        value = registrationNumber,
                                        placeholder = "যেমন: ১৭৫৬৪২".localized(),
                                        onValueChange = { hasUserEdited = true; registrationNumber = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_registration_input",
                                        isActive = activeFieldTag == "edit_registration_input",
                                        hoverResetKey = hoverResetKey,
                                        onActivate = { activateField("edit_registration_input") },
                                        onDeactivate = { if (activeFieldTag == "edit_registration_input") activeFieldTag = null },
                                    )
                                }
                            )
                        }
                    }

                    HtmlResponsiveGrid(columns = cols, items = academicItems, spacing = 16.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(34.dp))

        // Submit Button (.btn-submit)
        HtmlGradientSubmitButton(
            text = if (isSaving) "সংরক্ষণ করা হচ্ছে...".localized() else "সংরক্ষণ করুন".localized(),
            icon = SendIcon,
            hindSiliguri = hindSiliguri,
            testTag = "edit_profile_save_button",
            enabled = !isSaving,
            onClick = {
                val finalName = fullName.trim().ifBlank { fallbackName }
                val updated = initialProfile.copy(
                    email = initialProfile.email.ifBlank { fallbackEmail }.trim().lowercase(),
                    name = finalName,
                    fatherName = fatherName.trim(),
                    motherName = motherName.trim(),
                    dob = dob.trim(),
                    bloodGroup = bloodGroup.trim(),
                    religion = religion.trim(),
                    gender = gender.trim(),
                    maritalStatus = maritalStatus.trim(),
                    phone = phone.trim(),
                    contactEmail = contactEmail.trim(),
                    address = currentAddress.trim(),
                    permanentAddress = permanentAddress.trim(),
                    department = department.trim(),
                    semester = semester.trim(),
                    shift = shift.trim(),
                    section = section.trim(),
                    rollNumber = rollNumber.trim(),
                    registrationNumber = registrationNumber.trim(),
                    profileSetupComplete = true,
                )
                onSaveProfile(updated)
            },
        )

        HtmlProfileFooter(hindSiliguri = hindSiliguri)
    }
}

@Composable
internal fun HtmlResponsiveGrid(
    columns: Int,
    items: List<Pair<Int, @Composable () -> Unit>>,
    spacing: Dp,
) {
    if (columns <= 1) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            items.forEach { (_, content) ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
        return
    }

    // Pack items into rows based on column spans
    val rows = mutableListOf<List<Pair<Int, @Composable () -> Unit>>>()
    var currentRow = mutableListOf<Pair<Int, @Composable () -> Unit>>()
    var currentSpan = 0
    for (item in items) {
        val span = item.first.coerceIn(1, columns)
        if (currentSpan + span > columns && currentRow.isNotEmpty()) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            currentSpan = 0
        }
        currentRow.add(span to item.second)
        currentSpan += span
        if (currentSpan >= columns) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            currentSpan = 0
        }
    }
    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        rows.forEach { rowItems ->
            val usedSpan = rowItems.sumOf { it.first }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                rowItems.forEach { (span, content) ->
                    Box(modifier = Modifier.weight(span.toFloat())) {
                        content()
                    }
                }
                if (usedSpan < columns) {
                    Spacer(modifier = Modifier.weight((columns - usedSpan).toFloat()))
                }
            }
        }
    }
}

@Composable
private fun HtmlFieldLabel(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    hindSiliguri: FontFamily,
) {
    val palette = LocalHtmlProfilePalette.current
    val effectiveTint = if (palette.isDark) {
        when (iconTint) {
            HtmlProfileColors.BsPrimary -> palette.bsPrimary
            HtmlProfileColors.BsSuccess -> palette.bsSuccess
            HtmlProfileColors.BsDanger -> palette.bsDanger
            HtmlProfileColors.BsWarning -> palette.bsWarning
            HtmlProfileColors.BsInfo -> palette.bsInfo
            HtmlProfileColors.BsSecondary -> palette.bsSecondary
            else -> iconTint
        }
    } else {
        iconTint
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = effectiveTint,
            modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 15.5.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.SemiBold,
            color = palette.formLabelColor,
        )
    }
}

@Composable
private fun rememberFieldHoverState(
    hoverResetKey: Int,
): Triple<MutableInteractionSource, Boolean, Modifier> {
    val interactionSource = remember { MutableInteractionSource() }
    val isHoveredBySource by interactionSource.collectIsHoveredAsState()
    var isPointerInside by remember { mutableStateOf(false) }

    LaunchedEffect(hoverResetKey) {
        if (hoverResetKey > 0) {
            isPointerInside = false
        }
    }

    val hoverModifier = Modifier
        .hoverable(interactionSource = interactionSource)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when (event.type) {
                        PointerEventType.Enter -> isPointerInside = true
                        PointerEventType.Exit -> isPointerInside = false
                    }
                }
            }
        }

    return Triple(interactionSource, isHoveredBySource || isPointerInside, hoverModifier)
}

private fun Modifier.htmlFieldHalo(
    haloColor: Color,
    haloAlpha: Float,
): Modifier = this.drawBehind {
    if (haloAlpha > 0.005f) {
        val spreadPx = 4.5.dp.toPx()
        val cornerPx = 15.5.dp.toPx()
        drawRoundRect(
            color = haloColor.copy(alpha = haloAlpha),
            topLeft = Offset(-spreadPx, -spreadPx),
            size = Size(size.width + spreadPx * 2f, size.height + spreadPx * 2f),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
        )
    }
}

@Composable
private fun HtmlFormTextField(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    hindSiliguri: FontFamily,
    testTag: String,
    isActive: Boolean,
    hoverResetKey: Int,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val palette = LocalHtmlProfilePalette.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val (interactionSource, isHovered, hoverModifier) = rememberFieldHoverState(hoverResetKey)

    val isHighlighted = isFocused || isActive || isHovered
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.purpleAccent else palette.inputBorder,
        animationSpec = tween(durationMillis = 180),
        label = "TextFieldBorderColor",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.inputHoverBg else palette.inputBg,
        animationSpec = tween(durationMillis = 180),
        label = "TextFieldBgColor",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) palette.haloAlphaTarget else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "TextFieldHaloAlpha",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        HtmlFieldLabel(label = label, icon = icon, iconTint = iconTint, hindSiliguri = hindSiliguri)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .htmlFieldHalo(palette.purpleAccent, haloAlpha)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(12.dp))
                .then(hoverModifier)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    onActivate()
                    focusRequester.requestFocus()
                }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = palette.headingColor,
                    fontSize = 15.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Normal,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(palette.purpleAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag(testTag)
                    .onFocusChanged { state ->
                        isFocused = state.isFocused
                        if (state.isFocused) onActivate() else onDeactivate()
                    },
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = palette.placeholderColor,
                            fontSize = 15.sp,
                            fontFamily = hindSiliguri,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun HtmlFormDatePickerField(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    hindSiliguri: FontFamily,
    testTag: String,
    isActive: Boolean,
    hoverResetKey: Int,
    onClick: () -> Unit,
) {
    val palette = LocalHtmlProfilePalette.current
    val (interactionSource, isHovered, hoverModifier) = rememberFieldHoverState(hoverResetKey)
    val isHighlighted = isActive || isHovered
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.purpleAccent else palette.inputBorder,
        animationSpec = tween(durationMillis = 180),
        label = "DatePickerBorderColor",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.inputHoverBg else palette.inputBg,
        animationSpec = tween(durationMillis = 180),
        label = "DatePickerBgColor",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) palette.haloAlphaTarget else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "DatePickerHaloAlpha",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        HtmlFieldLabel(label = label, icon = icon, iconTint = iconTint, hindSiliguri = hindSiliguri)

        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = BorderStroke(2.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .htmlFieldHalo(palette.purpleAccent, haloAlpha)
                .then(hoverModifier)
                .testTag(testTag),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    color = if (value.isNotBlank()) palette.headingColor else palette.placeholderColor,
                    fontSize = 15.5.sp,
                    fontFamily = hindSiliguri,
                )
                Icon(
                    imageVector = CalendarDaysIcon,
                    contentDescription = label,
                    tint = if (isHighlighted) palette.purpleAccent else palette.subheadingColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun HtmlFormSelectField(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    options: List<String>,
    translateOptions: Boolean,
    onSelect: (String) -> Unit,
    hindSiliguri: FontFamily,
    testTag: String,
    isActive: Boolean,
    hoverResetKey: Int,
    onActivate: () -> Unit,
    onDismissActive: () -> Unit,
) {
    val palette = LocalHtmlProfilePalette.current
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val fieldWidthDp = with(density) { fieldWidthPx.toDp() }
    val (interactionSource, isHovered, hoverModifier) = rememberFieldHoverState(hoverResetKey)

    val isHighlighted = expanded || isActive || isHovered
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.purpleAccent else palette.inputBorder,
        animationSpec = tween(durationMillis = 180),
        label = "SelectBorderColor",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.inputHoverBg else palette.inputBg,
        animationSpec = tween(durationMillis = 180),
        label = "SelectBgColor",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) palette.haloAlphaTarget else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "SelectHaloAlpha",
    )

    val displayedValue = if (value.isNotBlank()) {
        if (translateOptions) value.localized() else value
    } else ""

    Column(modifier = Modifier.fillMaxWidth()) {
        HtmlFieldLabel(label = label, icon = icon, iconTint = iconTint, hindSiliguri = hindSiliguri)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { fieldWidthPx = it.width },
        ) {
            Surface(
                onClick = {
                    onActivate()
                    expanded = true
                },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                border = BorderStroke(2.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .htmlFieldHalo(palette.purpleAccent, haloAlpha)
                    .then(hoverModifier)
                    .testTag(testTag),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayedValue.ifBlank { placeholder },
                        color = if (displayedValue.isNotBlank()) palette.headingColor else palette.infoLabelColor,
                        fontSize = 15.5.sp,
                        fontFamily = hindSiliguri,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = null,
                        tint = if (isHighlighted) palette.purpleAccent else palette.subheadingColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    onDismissActive()
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = palette.dropdownBg,
                border = BorderStroke(1.dp, palette.dropdownBorder),
                modifier = Modifier.then(if (fieldWidthDp > 0.dp) Modifier.width(fieldWidthDp) else Modifier),
            ) {
                options.forEach { option ->
                    val optionLabel = if (translateOptions) option.localized() else option
                    val isSelected = option.equals(value, ignoreCase = true)
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        text = {
                            Text(
                                text = optionLabel,
                                fontFamily = hindSiliguri,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) palette.purpleHoverTint else palette.headingColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                            onDismissActive()
                        },
                    )
                }
            }
        }
    }
}
