package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.DownloadIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.MessageBubbleIcon
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.TelegramAnimatedIcon
import com.mpi.klass.theme.departmentAccent
import com.mpi.klass.theme.getHindSiliguriFontFamily
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.home
import klass.shared.generated.resources.menu
import klass.shared.generated.resources.notification
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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
 * Floating Home Routine header with color-coded Profile ring, wide shadowless capsule
 * 1-line batch badge (when expanded, hides AI button smoothly to reclaim its width and
 * expands downward into a 4-column snap-wheel picker popup over the routine),
 * plus AI button and Notification button with unread red dot.
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
    hasUnreadNotifications: Boolean = NotificationSeenStore.hasUnread(),
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
    val glassBg = if (isDark) Color(0xFF151F32).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.92f)
    // 100% solid opaque popup background so underlying table lines never bleed through during pop expansion
    val expandedSolidBg = if (isDark) Color(0xFF10192C) else Color(0xFFFFFFFF)
    val pillBgColor by animateColorAsState(
        targetValue = if (isExpanded) expandedSolidBg else glassBg,
        animationSpec = tween(durationMillis = 60),
        label = "HeaderPillBgColor",
    )
    val dotHaloColor = if (isDark) Color(0xFF0F172A) else Color.White
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 850f),
        label = "HeaderChevronRotation",
    )

    if (isExpanded) {
        AppBackHandler(enabled = true) {
            onToggleExpand()
        }
    }

    // All 4 header items sit in uniform 48.dp touch slots with 42.dp visual height (exact 24.dp vertical center!)
    val fadeTopColor = if (isDark) cs.surface.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.76f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        fadeTopColor,
                        fadeTopColor.copy(alpha = 0.35f),
                        Color.Transparent,
                    )
                )
            ),
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // 1. Profile Avatar Button (48.dp touch box, 42.dp visual circle centered at y = 24.dp)
        HomeProfileAvatarSlot(
            photoUrl = photoUrl,
            avatarSeed = avatarSeed,
            glassBg = glassBg,
            borderColor = profileAccent.primary.copy(alpha = 0.78f),
            onClick = onProfileClick,
        )

        // 2. Dynamic Pill: 42.dp top strip aligned at top = 3.dp (center y = 24.dp), pops out crisply with hairline expanded border!
        val workingFilter = activeTempFilter ?: defaultFilter
        val pillBorderColor = when {
            isExpanded -> routineAccent.primary.copy(alpha = if (isDark) 0.30f else 0.22f)
            activeTempFilter != null -> aiAccent.borderTint
            else -> routineAccent.borderTint
        }
        val pillDisplayTitle = when {
            titleText.isNotBlank() -> titleText
            isExpanded -> workingFilter.toBadgeText()
            else -> ""
        }

        Surface(
            shape = RoundedCornerShape(21.dp),
            color = pillBgColor,
            border = BorderStroke(if (isExpanded) 0.5.dp else 1.dp, pillBorderColor),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp, end = 4.dp, top = 3.dp)
                .testTag("home_header_profile_badge"),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top 1-line pill strip (always 42.dp height to match Profile/AI/Notification circles)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clickable(onClick = onToggleExpand)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (activeTempFilter != null || isExpanded) aiAccent.softTint else routineAccent.softTint,
                                    Color.Transparent,
                                )
                            )
                        )
                        .padding(start = 13.dp, end = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (pillDisplayTitle.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activeTempFilter != null) aiAccent.primary else routineAccent.primary
                                ),
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = pillDisplayTitle,
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
                        Spacer(modifier = Modifier.width(5.dp))
                        val dlInteraction = remember { MutableInteractionSource() }
                        var dlTrigger by remember { mutableIntStateOf(0) }
                        Surface(
                            onClick = {
                                dlTrigger++
                                onDownloadClick()
                            },
                            interactionSource = dlInteraction,
                            enabled = !isDownloading,
                            shape = CircleShape,
                            color = routineAccent.softTint,
                            border = BorderStroke(0.75.dp, routineAccent.borderTint),
                            shadowElevation = 0.dp,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("home_routine_download_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                TelegramAnimatedIcon(
                                    icon = DownloadIcon,
                                    contentDescription = "Download Routine PDF".localized(),
                                    tint = routineAccent.primary,
                                    size = 15.dp,
                                    interactionSource = dlInteraction,
                                    triggerKey = dlTrigger,
                                    isLoading = isDownloading,
                                )
                            }
                        }
                    }
                }

                // Connected 4-column snap-wheel picker inside the widened solid popup pill
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = spring(dampingRatio = 0.76f, stiffness = 880f),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(animationSpec = tween(durationMillis = 70)),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Top,
                    ) + fadeOut(animationSpec = tween(durationMillis = 70)),
                ) {
                    val allowedGroups = RoutineRules.groupsForDepartment(workingFilter.department)
                    val deptAccent = departmentAccent(workingFilter.department, isDark).primary
                    val colDividerColor = cs.outlineVariant.copy(alpha = 0.28f)
                    val lensGradient = Brush.horizontalGradient(
                        listOf(
                            routineAccent.softTint.copy(alpha = if (isDark) 0.55f else 0.72f),
                            aiAccent.softTint.copy(alpha = if (isDark) 0.50f else 0.65f),
                            deptAccent.copy(alpha = if (isDark) 0.22f else 0.14f),
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_routine_quick_filter_panel"),
                    ) {
                        HorizontalDivider(thickness = 0.5.dp, color = colDividerColor)

                        // Column headers with subtle rainbow accents
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QuickColumnHeader("Sem", hindSiliguri, routineAccent.primary, Modifier.weight(0.95f))
                            QuickColumnHeader("Shift", hindSiliguri, aiAccent.primary, Modifier.weight(0.9f))
                            QuickColumnHeader("Dept", hindSiliguri, deptAccent, Modifier.weight(1.25f))
                            QuickColumnHeader("Grp", hindSiliguri, profileAccent.primary, Modifier.weight(0.85f))
                        }

                        // 3D Snap-Wheel 4-Column Area with Center Selection Lens & Top/Bottom Fade
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(126.dp),
                        ) {
                            // Centered Selection Lens Pill spanning across all 4 columns
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(lensGradient)
                                    .then(
                                        Modifier.background(Color.Transparent)
                                    ),
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(0.5.dp, routineAccent.primary.copy(alpha = if (isDark) 0.24f else 0.18f)),
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                                    .height(38.dp),
                            ) {}

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                            ) {
                                // Col 1: Semester wheel
                                val semIndex = RoutineRules.ALL_SEMESTER_KEYS.indexOf(
                                    RoutineRules.canonicalSemesterKey(workingFilter.semesterKey)
                                ).coerceAtLeast(0)
                                QuickWheelColumn(
                                    items = RoutineRules.ALL_SEMESTER_KEYS,
                                    selectedIndex = semIndex,
                                    itemLabel = { RoutineRules.semesterShortLabel(it) },
                                    accent = routineAccent.primary,
                                    hindSiliguri = hindSiliguri,
                                    onSelect = { semKey ->
                                        onTempFilterChange(workingFilter.copy(semesterKey = semKey))
                                    },
                                    testTag = "quick_filter_col_semester",
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .fillMaxHeight(),
                                )

                                VerticalDivider(
                                    thickness = 0.5.dp,
                                    color = colDividerColor,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                )

                                // Col 2: Shift wheel
                                val shiftOptions = remember {
                                    listOf(
                                        RoutineRules.SHIFT_1 to "1st",
                                        RoutineRules.SHIFT_2 to "2nd",
                                    )
                                }
                                val shiftIndex = shiftOptions.indexOfFirst {
                                    it.first == RoutineRules.normalizeShift(workingFilter.shift)
                                }.coerceAtLeast(0)
                                QuickWheelColumn(
                                    items = shiftOptions,
                                    selectedIndex = shiftIndex,
                                    itemLabel = { it.second },
                                    accent = aiAccent.primary,
                                    hindSiliguri = hindSiliguri,
                                    onSelect = { (shiftKey, _) ->
                                        onTempFilterChange(workingFilter.copy(shift = shiftKey))
                                    },
                                    testTag = "quick_filter_col_shift",
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .fillMaxHeight(),
                                )

                                VerticalDivider(
                                    thickness = 0.5.dp,
                                    color = colDividerColor,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                )

                                // Col 3: Department wheel
                                val deptIndex = RoutineRules.TECH_DEPARTMENTS.indexOf(
                                    RoutineRules.normalizeDepartmentShort(workingFilter.department)
                                ).coerceAtLeast(0)
                                QuickWheelColumn(
                                    items = RoutineRules.TECH_DEPARTMENTS,
                                    selectedIndex = deptIndex,
                                    itemLabel = { it },
                                    accent = deptAccent,
                                    hindSiliguri = hindSiliguri,
                                    onSelect = { dept ->
                                        val normGrp = RoutineRules.normalizeGroup(dept, workingFilter.group)
                                        onTempFilterChange(
                                            workingFilter.copy(
                                                department = dept,
                                                group = normGrp,
                                            )
                                        )
                                    },
                                    testTag = "quick_filter_col_department",
                                    modifier = Modifier
                                        .weight(1.25f)
                                        .fillMaxHeight(),
                                )

                                VerticalDivider(
                                    thickness = 0.5.dp,
                                    color = colDividerColor,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                )

                                // Col 4: Group wheel
                                val grpIndex = allowedGroups.indexOf(
                                    RoutineRules.normalizeGroup(workingFilter.department, workingFilter.group)
                                ).coerceAtLeast(0)
                                QuickWheelColumn(
                                    items = allowedGroups,
                                    selectedIndex = grpIndex,
                                    itemLabel = { it.removePrefix("Group ").trim() },
                                    accent = profileAccent.primary,
                                    hindSiliguri = hindSiliguri,
                                    onSelect = { grp ->
                                        onTempFilterChange(workingFilter.copy(group = grp))
                                    },
                                    testTag = "quick_filter_col_group",
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .fillMaxHeight(),
                                )
                            }

                            // Top smooth fade gradient overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(expandedSolidBg, Color.Transparent)
                                        )
                                    ),
                            )

                            // Bottom smooth fade gradient overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, expandedSolidBg)
                                        )
                                    ),
                            )
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = colDividerColor)

                        // Bottom action bar inside the pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (activeTempFilter != null) {
                                Surface(
                                    onClick = { onTempFilterChange(null) },
                                    shape = CircleShape,
                                    color = notiAccent.softTint,
                                    border = BorderStroke(0.75.dp, notiAccent.borderTint),
                                    shadowElevation = 0.dp,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("home_filter_reset_button"),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = RefreshIcon,
                                            contentDescription = "Reset to saved profile".localized(),
                                            tint = notiAccent.primary,
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Text(
                                            text = "Reset".localized(),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = notiAccent.primary,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Temporary preview".localized(),
                                    fontSize = 11.sp,
                                    fontFamily = hindSiliguri,
                                    color = subTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 4.dp),
                                )
                            }

                            Surface(
                                onClick = onToggleExpand,
                                shape = CircleShape,
                                color = routineAccent.primary,
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("home_filter_done_button"),
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Done".localized(),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = if (routineAccent.primary.luminance() > 0.52f) Color(0xFF0B1120) else Color.White,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Zed AI Assistant Button
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(tween(120, delayMillis = 20)) +
                scaleIn(
                    initialScale = 0.68f,
                    animationSpec = spring(dampingRatio = 0.74f, stiffness = 880f),
                ) +
                expandHorizontally(
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = 880f),
                    expandFrom = Alignment.Start,
                ),
            exit = fadeOut(tween(75)) +
                scaleOut(
                    targetScale = 0.60f,
                    animationSpec = tween(90, easing = FastOutSlowInEasing),
                ) +
                shrinkHorizontally(
                    animationSpec = tween(120, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.End,
                ),
        ) {
            val aiInteraction = remember { MutableInteractionSource() }
            val aiPressed by aiInteraction.collectIsPressedAsState()
            var aiTrigger by remember { mutableIntStateOf(0) }
            val aiScale by animateFloatAsState(
                targetValue = if (aiPressed) 0.90f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
                label = "home_ai_scale",
            )
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = aiInteraction,
                            indication = androidx.compose.material3.ripple(bounded = false, radius = 24.dp),
                            onClick = {
                                aiTrigger++
                                onAiClick()
                            },
                        )
                        .testTag("home_ai_button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = glassBg,
                        border = BorderStroke(1.dp, aiAccent.borderTint),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .graphicsLayer {
                                scaleX = aiScale
                                scaleY = aiScale
                            },
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(aiAccent.softTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            TelegramAnimatedIcon(
                                icon = SparkleIcon,
                                contentDescription = "Zed AI".localized(),
                                tint = aiAccent.primary,
                                size = 19.dp,
                                interactionSource = aiInteraction,
                                triggerKey = aiTrigger,
                                hasActivity = false,
                            )
                        }
                    }
                }
            }
        }

        // 4. Notification Button (48.dp touch box, 42.dp visual circle centered at y = 24.dp) + crisp Red Dot
        val notiInteraction = remember { MutableInteractionSource() }
        val notiPressed by notiInteraction.collectIsPressedAsState()
        var notiTrigger by remember { mutableIntStateOf(0) }
        val notiScale by animateFloatAsState(
            targetValue = if (notiPressed) 0.90f else 1f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
            label = "home_noti_scale",
        )
        Box(modifier = Modifier.size(48.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = notiInteraction,
                        indication = androidx.compose.material3.ripple(bounded = false, radius = 24.dp),
                        onClick = {
                            notiTrigger++
                            onNotificationClick()
                        },
                    )
                    .testTag("home_notification_button"),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = glassBg,
                    border = BorderStroke(1.dp, notiAccent.borderTint),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .graphicsLayer {
                            scaleX = notiScale
                            scaleY = notiScale
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(notiAccent.softTint),
                        contentAlignment = Alignment.Center,
                    ) {
                        TelegramAnimatedIcon(
                            icon = NotificationIcon,
                            contentDescription = "Notifications".localized(),
                            tint = notiAccent.primary,
                            size = 19.dp,
                            interactionSource = notiInteraction,
                            triggerKey = notiTrigger,
                            hasActivity = hasUnreadNotifications,
                        )
                    }
                }
            }

            if (hasUnreadNotifications) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 3.dp)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(dotHaloColor)
                        .padding(1.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .testTag("home_notification_unread_dot"),
                )
            }
        }
    }
    }
}

@Composable
private fun HomeProfileAvatarSlot(
    photoUrl: String?,
    avatarSeed: String,
    glassBg: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    val platformContext = LocalPlatformContext.current
    val resolvedModel = remember(photoUrl) { resolveProfileImageModel(photoUrl) }
    val imageRequest = remember(resolvedModel, photoUrl, platformContext) {
        if (resolvedModel != null && !photoUrl.isNullOrBlank()) {
            ImageRequest.Builder(platformContext)
                .data(resolvedModel)
                .memoryCacheKey("home_avatar_${photoUrl.hashCode()}")
                .crossfade(false)
                .build()
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("home_profile_button"),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = glassBg,
            border = BorderStroke(1.5.dp, borderColor),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
        ) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Profile Photo".localized(),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                DynamicAvatar(
                    seed = avatarSeed,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun QuickColumnHeader(
    label: String,
    hindSiliguri: FontFamily,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = hindSiliguri,
        color = accent,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * Infinite bidirectional circular 3D Snap-Wheel column with real-time distance scaling,
 * opacity depth, snap fling physics, live center selection on settle, and tap-to-center.
 * Scrolls infinitely in BOTH directions (up and down) like a mechanical drum.
 */
@Composable
private fun <T> QuickWheelColumn(
    items: List<T>,
    selectedIndex: Int,
    itemLabel: (T) -> String,
    accent: Color,
    hindSiliguri: FontFamily,
    onSelect: (T) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val n = items.size
    if (n == 0) {
        Box(modifier = modifier.testTag(testTag))
        return
    }

    val cs = MaterialTheme.colorScheme
    val safeIndex = selectedIndex.coerceIn(0, n - 1)
    val isCircular = n > 1
    val loopMultiplier = 4_000
    val totalCount = if (isCircular) n * loopMultiplier else 1
    val middleBase = if (isCircular) (loopMultiplier / 2) * n else 0
    val initialVirtual = middleBase + safeIndex

    // Recreate wheel cleanly if option count changes (e.g., Department changing Group count 2 <-> 3)
    key(n) {
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialVirtual)
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        val coroutineScope = rememberCoroutineScope()

        val currentItems by rememberUpdatedState(items)
        val currentSelectedIdx by rememberUpdatedState(safeIndex)
        val currentOnSelect by rememberUpdatedState(onSelect)
        var pendingTapVirtual by remember { mutableStateOf<Int?>(null) }

        val density = LocalDensity.current
        val itemHeight = 38.dp
        val itemHeightPx = with(density) { itemHeight.toPx() }

        val centeredVirtualIndex by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                info.visibleItemsInfo.minByOrNull { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                }?.index ?: listState.firstVisibleItemIndex
            }
        }

        // Sync external changes (e.g., Reset button) to nearest matching circular virtual row
        LaunchedEffect(safeIndex) {
            if (!isCircular) return@LaunchedEffect
            val tapped = pendingTapVirtual
            if (tapped != null) {
                val tappedLogical = ((tapped % n) + n) % n
                if (tappedLogical == safeIndex) {
                    return@LaunchedEffect
                }
            }
            val anchor = centeredVirtualIndex.coerceIn(0, totalCount - 1)
            val currentLogical = ((anchor % n) + n) % n
            if (currentLogical != safeIndex || (!listState.isScrollInProgress && listState.firstVisibleItemScrollOffset != 0)) {
                val targetVirtual = nearestCircularVirtualIndex(
                    anchorVirtual = anchor,
                    targetLogicalIndex = safeIndex,
                    n = n,
                    totalCount = totalCount,
                )
                if (!listState.isScrollInProgress && anchor != targetVirtual) {
                    listState.animateScrollToItem(targetVirtual)
                }
            }
        }

        // Emit selected logical item when scroll settles & silently re-center if far from middle
        LaunchedEffect(listState) {
            snapshotFlow { listState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { scrolling ->
                    if (!scrolling && currentItems.isNotEmpty()) {
                        pendingTapVirtual = null
                        val info = listState.layoutInfo
                        val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                        val centered = info.visibleItemsInfo.minByOrNull { item ->
                            kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                        }
                        if (centered != null && centered.index in 0 until totalCount) {
                            val logicalIndex = ((centered.index % n) + n) % n
                            if (logicalIndex != currentSelectedIdx) {
                                currentOnSelect(currentItems[logicalIndex])
                            }
                            if (isCircular && (centered.index < n * 100 || centered.index > totalCount - n * 100)) {
                                listState.scrollToItem(middleBase + logicalIndex)
                            }
                        }
                    }
                }
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = 44.dp),
            modifier = modifier.testTag(testTag),
        ) {
            items(
                count = totalCount,
                key = { it },
            ) { virtualIndex ->
                val logicalIndex = if (isCircular) ((virtualIndex % n) + n) % n else 0
                val item = items[logicalIndex]
                val isCentered = virtualIndex == centeredVirtualIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            pendingTapVirtual = virtualIndex
                            if (logicalIndex != safeIndex) {
                                onSelect(item)
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(virtualIndex)
                            }
                        }
                        .graphicsLayer {
                            val layoutInfo = listState.layoutInfo
                            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                            val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == virtualIndex }
                            if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val normDist = (kotlin.math.abs(itemCenter - viewportCenter) / itemHeightPx)
                                    .coerceIn(0f, 1.3f)
                                val factor = normDist.coerceAtMost(1f)
                                val scale = 1.07f - (0.23f * factor)
                                val fade = 1f - (0.60f * factor)
                                scaleX = scale
                                scaleY = scale
                                alpha = fade
                            } else {
                                scaleX = 0.84f
                                scaleY = 0.84f
                                alpha = 0.40f
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = itemLabel(item),
                        fontSize = if (isCentered) 13.5.sp else 12.sp,
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = hindSiliguri,
                        color = if (isCentered) accent else cs.onSurface.copy(alpha = 0.80f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

private fun nearestCircularVirtualIndex(
    anchorVirtual: Int,
    targetLogicalIndex: Int,
    n: Int,
    totalCount: Int,
): Int {
    if (n <= 1 || totalCount <= 1) return 0
    val currentMod = ((anchorVirtual % n) + n) % n
    var delta = targetLogicalIndex - currentMod
    if (delta > n / 2) delta -= n
    if (delta < -n / 2) delta += n
    val candidate = anchorVirtual + delta
    return when {
        candidate < 0 -> candidate + n
        candidate >= totalCount -> candidate - n
        else -> candidate
    }.coerceIn(0, totalCount - 1)
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
