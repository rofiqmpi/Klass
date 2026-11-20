package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.DownloadIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.departmentAccent
import com.mpi.klass.theme.getHindSiliguriFontFamily
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.home
import klass.shared.generated.resources.menu
import klass.shared.generated.resources.notification
import org.jetbrains.compose.resources.painterResource

data class RoutineQuickFilter(
    val semesterKey: String = RoutineRules.ALL_SEMESTER_KEYS.first(),
    val shift: String = RoutineRules.SHIFT_1,
    val department: String = "CST",
    val group: String = "Group A",
) {
    fun toBadgeText(): String {
        val semNum = RoutineRules.semesterNumber(semesterKey) ?: 1
        val shiftNum = if (RoutineRules.normalizeShift(shift) == RoutineRules.SHIFT_2) "2" else "1"
        val dept = RoutineRules.normalizeDepartmentShort(department).ifBlank { "CST" }
        val grpLetter = RoutineRules.normalizeGroup(dept, group)
            .removePrefix("Group ")
            .trim()
        return "$semNum/$shiftNum/$dept-$grpLetter"
    }
}

/**
 * Floating Home Routine header with color-coded Profile ring, wide fully-rounded 1-line
 * batch info pill (expandable in-place to browse any semester/shift/dept/group temporarily)
 * with inline PDF Download button, plus AI and Notification buttons.
 */
@Composable
fun HomeRoutineHeader(
    isDark: Boolean,
    cardBg: Color,
    photoUrl: String?,
    avatarSeed: String,
    onProfileClick: () -> Unit,
    onAiClick: () -> Unit,
    onNotificationClick: () -> Unit,
    titleText: String = "",
    subtitleText: String = "",
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = onProfileClick,
    onDownloadClick: (() -> Unit)? = null,
    isDownloading: Boolean = false,
    activeTempFilter: RoutineQuickFilter? = null,
    defaultFilter: RoutineQuickFilter = RoutineQuickFilter(),
    onTempFilterChange: (RoutineQuickFilter?) -> Unit = {},
) {
    val hindSiliguri = getHindSiliguriFontFamily()
    val cs = MaterialTheme.colorScheme
    val profileAccent = KlassFeaturePalette.profile(isDark)
    val routineAccent = KlassFeaturePalette.routine(isDark)
    val aiAccent = KlassFeaturePalette.ai(isDark)
    val notiAccent = KlassFeaturePalette.notifications(isDark)

    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val glassBg = if (isDark) Color(0xFF151F32).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(220),
        label = "HeaderChevronRotation",
    )

    if (isExpanded) {
        AppBackHandler(enabled = true) {
            onToggleExpand()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .animateContentSize(animationSpec = tween(220))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = glassBg,
                border = BorderStroke(1.5.dp, profileAccent.primary.copy(alpha = 0.72f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .clip(CircleShape)
                    .testTag("home_profile_button"),
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = resolveProfileImageModel(photoUrl),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    DynamicAvatar(
                        seed = avatarSeed,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Full-width pill-round 1-line batch badge + Download button; tapping opens selector below
            val pillBorderColor = when {
                isExpanded -> routineAccent.primary
                activeTempFilter != null -> aiAccent.borderTint
                else -> routineAccent.borderTint
            }
            Surface(
                onClick = onToggleExpand,
                shape = CircleShape,
                color = glassBg,
                border = BorderStroke(if (isExpanded) 1.4.dp else 1.dp, pillBorderColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("home_header_profile_badge"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (activeTempFilter != null) aiAccent.softTint else routineAccent.softTint,
                                    Color.Transparent,
                                )
                            )
                        )
                        .padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (titleText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (activeTempFilter != null) aiAccent.primary else routineAccent.primary),
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = titleText,
                            fontSize = 13.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = if (isExpanded) "Collapse routine filter" else "Expand routine filter",
                        tint = subTextColor,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(chevronRotation),
                    )

                    if (onDownloadClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            onClick = onDownloadClick,
                            enabled = !isDownloading,
                            shape = CircleShape,
                            color = routineAccent.softTint,
                            border = BorderStroke(0.75.dp, routineAccent.borderTint),
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("home_routine_download_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        color = routineAccent.primary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = DownloadIcon,
                                        contentDescription = "Download Routine PDF",
                                        tint = routineAccent.primary,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                onClick = onAiClick,
                shape = CircleShape,
                color = glassBg,
                border = BorderStroke(1.dp, aiAccent.borderTint),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(42.dp)
                    .testTag("home_ai_button"),
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

            Surface(
                onClick = onNotificationClick,
                shape = CircleShape,
                color = glassBg,
                border = BorderStroke(1.dp, notiAccent.borderTint),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(42.dp)
                    .testTag("home_notification_button"),
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

        // Expandable Temporary Batch Selector (Semester / Shift / Department / Group)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140)),
        ) {
            val workingFilter = activeTempFilter ?: defaultFilter
            val allowedGroups = RoutineRules.groupsForDepartment(workingFilter.department)
            val deptAccent = departmentAccent(workingFilter.department, isDark)

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = glassBg,
                border = BorderStroke(1.dp, routineAccent.borderTint),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_routine_quick_filter_panel"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeTempFilter != null) {
                                    "Viewing ${workingFilter.toBadgeText()}"
                                } else {
                                    "Browse Any Routine"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = cs.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Temporary preview only — saved profile stays unchanged",
                                fontSize = 11.5.sp,
                                fontFamily = hindSiliguri,
                                color = subTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (activeTempFilter != null) {
                                Surface(
                                    onClick = { onTempFilterChange(null) },
                                    shape = CircleShape,
                                    color = notiAccent.softTint,
                                    border = BorderStroke(0.75.dp, notiAccent.borderTint),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("home_filter_reset_button"),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = RefreshIcon,
                                            contentDescription = "Reset to saved profile",
                                            tint = notiAccent.primary,
                                            modifier = Modifier.size(13.dp),
                                        )
                                        Text(
                                            text = "Reset",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = notiAccent.primary,
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = onToggleExpand,
                                shape = CircleShape,
                                color = routineAccent.primary,
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("home_filter_done_button"),
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Done",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = if (routineAccent.primary.luminance() > 0.52f) Color(0xFF0B1120) else Color.White,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = cs.outlineVariant.copy(alpha = 0.45f),
                    )

                    // Department selector
                    QuickFilterRowLabel("Department", hindSiliguri, subTextColor) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(end = 4.dp),
                        ) {
                            items(RoutineRules.TECH_DEPARTMENTS) { dept ->
                                val isSel = workingFilter.department == dept
                                val accent = departmentAccent(dept, isDark).primary
                                QuickFilterPillChip(
                                    label = dept,
                                    selected = isSel,
                                    accent = accent,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    onClick = {
                                        val normGrp = RoutineRules.normalizeGroup(dept, workingFilter.group)
                                        onTempFilterChange(
                                            workingFilter.copy(
                                                department = dept,
                                                group = normGrp,
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }

                    // Semester selector
                    QuickFilterRowLabel("Semester", hindSiliguri, subTextColor) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(end = 4.dp),
                        ) {
                            items(RoutineRules.ALL_SEMESTER_KEYS) { semKey ->
                                val isSel = RoutineRules.canonicalSemesterKey(workingFilter.semesterKey) == semKey
                                QuickFilterPillChip(
                                    label = RoutineRules.semesterShortLabel(semKey),
                                    selected = isSel,
                                    accent = routineAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    onClick = {
                                        onTempFilterChange(workingFilter.copy(semesterKey = semKey))
                                    },
                                )
                            }
                        }
                    }

                    // Shift & Group row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Shift",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                color = subTextColor,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    RoutineRules.SHIFT_1 to "1st",
                                    RoutineRules.SHIFT_2 to "2nd",
                                ).forEach { (shiftKey, shortLabel) ->
                                    QuickFilterPillChip(
                                        label = shortLabel,
                                        selected = RoutineRules.normalizeShift(workingFilter.shift) == shiftKey,
                                        accent = aiAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = {
                                            onTempFilterChange(workingFilter.copy(shift = shiftKey))
                                        },
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1.25f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Group",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                color = subTextColor,
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(allowedGroups) { grp ->
                                    val shortGrp = grp.removePrefix("Group ").trim()
                                    QuickFilterPillChip(
                                        label = shortGrp,
                                        selected = RoutineRules.normalizeGroup(workingFilter.department, workingFilter.group) == grp,
                                        accent = deptAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = {
                                            onTempFilterChange(workingFilter.copy(group = grp))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickFilterRowLabel(
    label: String,
    hindSiliguri: FontFamily,
    labelColor: Color,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = hindSiliguri,
            color = labelColor,
        )
        content()
    }
}

@Composable
private fun QuickFilterPillChip(
    label: String,
    selected: Boolean,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val bgColor = if (selected) accent else if (isDark) cs.surfaceContainerHigh.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.85f)
    val fgColor = if (selected) {
        if (accent.luminance() > 0.52f) Color(0xFF0B1120) else Color.White
    } else {
        cs.onSurface
    }
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        border = if (!selected) BorderStroke(0.75.dp, accent.copy(alpha = 0.30f)) else null,
        modifier = Modifier.height(32.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = fgColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Tactile 3D split bottom bar keeping Home and Menu drawable icons intact,
 * with vibrant semantic active fill and concentric bottom corner adaptation.
 */
@Composable
fun HomeMenuBottomBar(
    isHomeSelected: Boolean,
    isDark: Boolean,
    cardBg: Color,
    hindSiliguri: FontFamily,
    homeShape: Shape,
    menuShape: Shape,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val isMenuSelected = !isHomeSelected
    val isGesture = remember { isGestureNavigation() }
    val horizontalPad = if (isGesture) 6.dp else 0.dp
    val bottomPad = if (isGesture) 4.dp else 0.dp
    val barHeight = 54.dp
    val lipDepth = 3.dp

    val routineAccent = KlassFeaturePalette.routine(isDark)
    val menuAccent = KlassFeaturePalette.profile(isDark)

    val homeActiveFg = if (isDark) Color(0xFF38BDF8) else routineAccent.primary
    val homeActiveBase = if (isDark) Color(0xFF0284C7) else routineAccent.deep

    val menuActiveFg = if (isDark) Color(0xFF818CF8) else menuAccent.primary
    val menuActiveBase = if (isDark) Color(0xFF4338CA) else menuAccent.deep

    val activeContent = if (isDark) Color(0xFF0B1120) else Color.White
    val inactiveFg = if (isDark) Color(0xFF151F32).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.94f)
    val inactiveBase = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val inactiveContent = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    val homeBgColor by animateColorAsState(
        targetValue = if (isHomeSelected) homeActiveFg else inactiveFg,
        animationSpec = tween(200),
        label = "BottomHomeBg",
    )
    val menuBgColor by animateColorAsState(
        targetValue = if (isMenuSelected) menuActiveFg else inactiveFg,
        animationSpec = tween(200),
        label = "BottomMenuBg",
    )

    val homeInteraction = remember { MutableInteractionSource() }
    val isHomePressed by homeInteraction.collectIsPressedAsState()

    val menuInteraction = remember { MutableInteractionSource() }
    val isMenuPressed by menuInteraction.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPad)
            .padding(bottom = bottomPad)
            .height(barHeight + lipDepth),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Home 3D Split Button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight + lipDepth),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .offset(y = lipDepth)
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(homeShape)
                    .background(if (isHomeSelected) homeActiveBase else inactiveBase),
            )

            Surface(
                onClick = onHomeClick,
                interactionSource = homeInteraction,
                shape = homeShape,
                color = homeBgColor,
                border = BorderStroke(0.75.dp, if (isHomeSelected) homeActiveBase else inactiveBase),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .offset(y = if (isHomePressed) lipDepth else 0.dp)
                    .testTag("bottom_bar_home"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.home),
                        contentDescription = "হোম".localized(),
                        tint = if (isHomeSelected) activeContent else inactiveContent,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হোম".localized(),
                        fontSize = 15.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isHomeSelected) activeContent else inactiveContent,
                        maxLines = 1,
                    )
                }
            }
        }

        // Menu 3D Split Button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight + lipDepth),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .offset(y = lipDepth)
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(menuShape)
                    .background(if (isMenuSelected) menuActiveBase else inactiveBase),
            )

            Surface(
                onClick = onMenuClick,
                interactionSource = menuInteraction,
                shape = menuShape,
                color = menuBgColor,
                border = BorderStroke(0.75.dp, if (isMenuSelected) menuActiveBase else inactiveBase),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .offset(y = if (isMenuPressed) lipDepth else 0.dp)
                    .testTag("bottom_bar_menu"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.menu),
                        contentDescription = "মেনু".localized(),
                        tint = if (isMenuSelected) activeContent else inactiveContent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মেনু".localized(),
                        fontSize = 15.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = if (isMenuSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isMenuSelected) activeContent else inactiveContent,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
