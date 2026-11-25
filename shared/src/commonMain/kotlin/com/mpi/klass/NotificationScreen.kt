package com.mpi.klass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.dayPaletteAt
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.notiscreen
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private data class ActionNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val metaLabel: String,
    val icon: ImageVector,
    val badgeIcon: ImageVector,
    val accent: Color,
    val testTag: String,
    val onClick: () -> Unit,
)

@Composable
fun NotificationScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    role: UserRole? = null,
    email: String = "",
    studentProfile: StudentProfile? = null,
    teacherPref: TeacherSchedulingPref? = null,
    publishedPlan: RoutinePlan? = null,
    onNavigateToRoute: (String) -> Unit = {},
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    val notiAccent = KlassFeaturePalette.notifications(isDark)
    val routineAccent = KlassFeaturePalette.routine(isDark)
    val profileAccent = KlassFeaturePalette.profile(isDark)
    val aiAccent = KlassFeaturePalette.ai(isDark)
    val adminAccent = KlassFeaturePalette.admin(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)

    var activePlan by remember(publishedPlan) { mutableStateOf(publishedPlan) }
    var isLoading by remember { mutableStateOf(publishedPlan == null) }
    val prefs = remember { getNotificationPrefs() }
    var permissionGranted by remember { mutableStateOf(hasNotificationPermission()) }
    var scheduledAlerts by remember { mutableStateOf<List<ScheduledClassAlert>>(emptyList()) }
    var selectedAlert by remember { mutableStateOf<ScheduledClassAlert?>(null) }

    val seenIds = NotificationSeenStore.seenIds()

    LaunchedEffect(selectedAlert) {
        onFullScreenOverlayChange(selectedAlert != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    val permissionLauncher = rememberNotificationPermissionLauncher { granted ->
        permissionGranted = granted
        if (granted) {
            sendInstantTestPushNotification(
                title = "Push Notifications Enabled",
                body = "You will automatically receive alerts before every scheduled class.",
            )
            showToast("Class push notifications enabled")
        } else {
            showToast("Please enable notification permission in Settings for class alerts.")
        }
    }

    LaunchedEffect(email, role, publishedPlan) {
        isLoading = true
        try {
            if (activePlan == null) {
                activePlan = fetchRoutinePlanFromFirestore("master_6m_published")
            }
            permissionGranted = hasNotificationPermission()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(activePlan, role, email, studentProfile, prefs, permissionGranted) {
        val plan = activePlan ?: run {
            scheduledAlerts = emptyList()
            return@LaunchedEffect
        }
        val userEntries = PushScheduleMath.filterUserEntriesForAlerts(
            plan = plan,
            role = role,
            email = email,
            studentProfile = studentProfile,
        )
        if (userEntries.isEmpty()) {
            cancelAllClassPushNotifications()
            scheduledAlerts = emptyList()
        } else {
            scheduledAlerts = syncAutomaticClassPushNotifications(
                entries = userEntries,
                config = plan.config,
                role = role,
                prefs = prefs,
            )
        }
    }

    var nowMillis by remember { mutableLongStateOf(getCurrentTimeMillis()) }
    LaunchedEffect(scheduledAlerts.isNotEmpty(), selectedAlert) {
        nowMillis = getCurrentTimeMillis()
        while (scheduledAlerts.isNotEmpty()) {
            delay(30_000L)
            nowMillis = getCurrentTimeMillis()
        }
    }

    // Action notifications moved from Home header banners
    val actionNotifications = remember(
        role,
        studentProfile,
        teacherPref,
        activePlan,
        permissionGranted,
        isDark,
        email,
    ) {
        buildList {

            if (role == UserRole.STUDENT &&
                (studentProfile == null || studentProfile.department.isBlank() || studentProfile.semester.isBlank())
            ) {
                add(
                    ActionNotificationItem(
                        id = "action_complete_profile",
                        title = "Complete your academic profile",
                        body = "Add Department, Shift & Semester in Profile to unlock your personal class routine.",
                        metaLabel = "Action needed • Tap to open Profile",
                        icon = PersonIcon,
                        badgeIcon = CalendarDaysIcon,
                        accent = profileAccent.primary,
                        testTag = "notification_action_complete_profile",
                        onClick = { onNavigateToRoute("profile") },
                    )
                )
            }
            if (role == UserRole.TEACHER && (teacherPref == null || !teacherPref.schedulingSetupComplete)) {
                add(
                    ActionNotificationItem(
                        id = "action_teacher_prefs",
                        title = "Select your teaching subjects",
                        body = "Choose your subjects and preferred periods to match your assigned classes.",
                        metaLabel = "Setup required • Tap to open Teaching Subjects",
                        icon = BookIcon,
                        badgeIcon = ClockIcon,
                        accent = routineAccent.primary,
                        testTag = "notification_action_teacher_prefs",
                        onClick = { onNavigateToRoute("teacher_prefs") },
                    )
                )
            }
            if (role == UserRole.ADMIN && activePlan == null) {
                add(
                    ActionNotificationItem(
                        id = "action_admin_publish",
                        title = "No live routine published yet",
                        body = "Open Zed engine to generate, review conflicts, and publish the timetable.",
                        metaLabel = "Admin task • Tap to open Zed engine",
                        icon = CalendarDaysIcon,
                        badgeIcon = SparkleIcon,
                        accent = adminAccent.primary,
                        testTag = "notification_action_routine_manager",
                        onClick = { onNavigateToRoute("routine_manager") },
                    )
                )
            }
            if (!permissionGranted) {
                add(
                    ActionNotificationItem(
                        id = "action_enable_push",
                        title = "Allow class push notifications",
                        body = "Turn on notifications so you automatically receive reminders before every class.",
                        metaLabel = "Permission • Tap to allow",
                        icon = NotificationIcon,
                        badgeIcon = ClockIcon,
                        accent = errorAccent.primary,
                        testTag = "enable_push_permission_button",
                        onClick = { permissionLauncher() },
                    )
                )
            }
            if (role == null) {
                add(
                    ActionNotificationItem(
                        id = "action_guest_signin",
                        title = "Sign in with your MPI account",
                        body = "Log in as a Student or Teacher to get personalized automatic class alerts.",
                        metaLabel = "Guest mode • Tap to sign in",
                        icon = PersonIcon,
                        badgeIcon = NotificationIcon,
                        accent = aiAccent.primary,
                        testTag = "notification_action_signin",
                        onClick = { onNavigateToRoute("sign_in") },
                    )
                )
            }
        }
    }

    LaunchedEffect(actionNotifications, scheduledAlerts) {
        NotificationSeenStore.setActiveIds(
            actionNotifications.map { it.id } + scheduledAlerts.map { it.id }
        )
    }

    // Sort unread notifications to top so seen items naturally settle beneath unread ones
    val sortedActions = remember(actionNotifications, seenIds) {
        actionNotifications.sortedBy { it.id in seenIds }
    }
    val sortedAlerts = remember(scheduledAlerts, seenIds) {
        scheduledAlerts.sortedBy { it.id in seenIds }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        MinimalBackdrop(isDark = isDark, focusAccent = notiAccent.primary) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 880.dp),
                ) {
                    if (topContentPadding > 0.dp) {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        Spacer(modifier = Modifier.height(topContentPadding))
                    }

                    when {
                        isLoading -> {
                            FacebookNotificationSkeletonScreen(
                                isDark = isDark,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("notifications_skeleton_loader"),
                            )
                        }

                        sortedActions.isEmpty() && sortedAlerts.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.notiscreen),
                                    contentDescription = "Notifications Empty State",
                                    modifier = Modifier.size(128.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Notifications Yet",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Automatic class alerts and important updates will show up here.",
                                    fontSize = 14.5.sp,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("notifications_feed_list"),
                                contentPadding = PaddingValues(top = 2.dp, bottom = navBarBottom + 28.dp),
                            ) {
                                itemsIndexed(
                                    items = sortedActions,
                                    key = { _, item -> item.id },
                                ) { index, item ->
                                    val isUnread = item.id !in seenIds
                                    FacebookNotificationRow(
                                        titlePrefix = item.title,
                                        bodyText = item.body,
                                        metaText = if (isUnread) item.metaLabel else "${item.metaLabel} • Seen",
                                        isUnread = isUnread,
                                        icon = item.icon,
                                        badgeIcon = if (isUnread) item.badgeIcon else CheckIcon,
                                        accent = item.accent,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        testTag = item.testTag,
                                        onClick = {
                                            NotificationSeenStore.markSeen(item.id)
                                            item.onClick()
                                        },
                                    )
                                    if (index < sortedActions.lastIndex || sortedAlerts.isNotEmpty()) {
                                        SocialInsetDivider(startIndent = 84.dp)
                                    }
                                }

                                itemsIndexed(
                                    items = sortedAlerts,
                                    key = { _, alert -> alert.id },
                                ) { index, alert ->
                                    val countdown = remember(alert.nextTriggerMillis, nowMillis) {
                                        PushScheduleMath.formatCountdownUntil(alert.nextTriggerMillis, nowMillis)
                                    }
                                    val dayIdx = RoutineRules.dayOrderIndex(alert.day).coerceAtLeast(0)
                                    val rowAccent = if (alert.isPreClassReminder) notiAccent else dayPaletteAt(dayIdx, isDark)
                                    val isUnread = alert.id !in seenIds

                                    FacebookNotificationRow(
                                        titlePrefix = alert.subjectTitle,
                                        bodyText = "Room ${alert.roomNumber} • ${alert.classTimeRange.ifBlank { alert.triggerTimeLabel }} (${alert.day})",
                                        metaText = if (isUnread) {
                                            "$countdown • ${alert.triggerTimeLabel}"
                                        } else {
                                            "$countdown • Seen"
                                        },
                                        isUnread = isUnread,
                                        icon = if (alert.isPreClassReminder) ClockIcon else NotificationIcon,
                                        badgeIcon = if (isUnread) CalendarDaysIcon else CheckIcon,
                                        accent = rowAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        testTag = "notification_alert_row_${alert.id}",
                                        onClick = {
                                            NotificationSeenStore.markSeen(alert.id)
                                            selectedAlert = alert
                                        },
                                    )
                                    if (index < sortedAlerts.lastIndex) {
                                        SocialInsetDivider(startIndent = 84.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Level 1 Full-Screen Notification Detail View
        KlassSubScreenHost(visible = selectedAlert != null, zIndex = 80f) {
            selectedAlert?.let { alert ->
                val dayIdx = RoutineRules.dayOrderIndex(alert.day).coerceAtLeast(0)
                val alertAccent = dayPaletteAt(dayIdx, isDark)
                val countdown = PushScheduleMath.formatCountdownUntil(alert.nextTriggerMillis, nowMillis)
                val isCurrentlySeen = alert.id in seenIds

                MinimalFullScreenSheet(
                    title = alert.subjectTitle,
                    subtitle = "${alert.day} • Period ${alert.periodIndex + 1}",
                    levelLabel = if (isCurrentlySeen) "Seen Notification" else "Unread Notification",
                    icon = if (alert.isPreClassReminder) ClockIcon else NotificationIcon,
                    accent = alertAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { selectedAlert = null },
                    closeTestTag = "notification_detail_back_button",
                    headerActions = {
                        Surface(
                            onClick = {
                                if (isCurrentlySeen) {
                                    NotificationSeenStore.markUnseen(alert.id)
                                    showToast("Marked as unread")
                                } else {
                                    NotificationSeenStore.markSeen(alert.id)
                                    showToast("Marked as seen")
                                }
                            },
                            shape = CircleShape,
                            color = alertAccent.softTint,
                            border = BorderStroke(0.75.dp, alertAccent.borderTint),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(32.dp)
                                .testTag("notification_toggle_seen_button"),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (isCurrentlySeen) "Mark Unread" else "Mark Seen",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = hindSiliguri,
                                    color = alertAccent.primary,
                                )
                            }
                        }
                        HeaderSavePillButton(
                            text = "Preview",
                            accent = alertAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = {
                                sendInstantTestPushNotification(
                                    title = alert.triggerTitle,
                                    body = alert.triggerBody,
                                )
                                showToast("Preview notification sent!")
                            },
                        )
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            KlassDetailFieldRow(
                                label = "Subject",
                                value = "${alert.subjectTitle} (${alert.subjectCode})",
                                icon = BookIcon,
                                accent = alertAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Class Time & Day",
                                value = "${alert.day} • ${alert.classTimeRange.ifBlank { alert.triggerTimeLabel }}",
                                icon = ClockIcon,
                                accent = notiAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Classroom",
                                value = "Room ${alert.roomNumber}",
                                icon = DoorOpenIcon,
                                accent = KlassFeaturePalette.classrooms(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            if (alert.metaSubtitle.isNotBlank()) {
                                SocialInsetDivider(startIndent = 68.dp)
                                KlassDetailFieldRow(
                                    label = "Details",
                                    value = alert.metaSubtitle,
                                    icon = SparkleIcon,
                                    accent = KlassFeaturePalette.ai(isDark).primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
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
private fun FacebookNotificationRow(
    titlePrefix: String,
    bodyText: String,
    metaText: String,
    isUnread: Boolean,
    icon: ImageVector,
    badgeIcon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    testTag: String,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val unreadBg = accent.copy(alpha = if (isDark) 0.14f else 0.08f)
    val rowBg = if (isUnread) {
        unreadBg
    } else if (isDark) {
        cs.surfaceContainerLow.copy(alpha = 0.42f)
    } else {
        Color.White.copy(alpha = 0.52f)
    }
    val ringBg = if (isDark) Color(0xFF0F172A) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = if (isUnread) {
                    accent.copy(alpha = if (isDark) 0.24f else 0.15f)
                } else {
                    cs.surfaceContainerHighest.copy(alpha = 0.55f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isUnread) accent.copy(alpha = 0.36f) else cs.outlineVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    com.mpi.klass.theme.TelegramAnimatedIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = if (isUnread) accent else cs.onSurfaceVariant,
                        size = 24.dp,
                        hasActivity = isUnread,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = if (isUnread) accent else cs.outline,
                border = BorderStroke(2.dp, ringBg),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isUnread) cs.onSurface else cs.onSurface.copy(alpha = 0.78f),
                        )
                    ) {
                        append(titlePrefix)
                    }
                    if (bodyText.isNotBlank()) {
                        append(" — ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Normal,
                                color = cs.onSurface.copy(alpha = if (isUnread) 0.86f else 0.65f),
                            )
                        ) {
                            append(bodyText)
                        }
                    }
                },
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                fontFamily = hindSiliguri,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = metaText,
                fontSize = 12.5.sp,
                fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = hindSiliguri,
                color = if (isUnread) accent else cs.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isUnread) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)),
            )
        } else {
            Icon(
                imageVector = ChevronRightIcon,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Full-screen Facebook-style card shimmer loading skeleton (replaces circular spinner).
 */
@Composable
private fun FacebookNotificationSkeletonScreen(
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "FbNotificationShimmer")
    val shimmerTranslate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ShimmerOffset",
    )

    val baseTone = if (isDark) Color(0xFF263349) else Color(0xFFE2E8F0)
    val highlightTone = if (isDark) Color(0xFF3A4B69) else Color(0xFFF8FAFC)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseTone, highlightTone, baseTone),
        start = Offset(shimmerTranslate - 340f, 0f),
        end = Offset(shimmerTranslate, 280f),
    )

    val cardOverlay = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.52f)
    }

    val patterns = remember {
        listOf(
            Triple(0.76f, 0.92f, 0.36f),
            Triple(0.84f, 0.86f, 0.42f),
            Triple(0.68f, 0.94f, 0.34f),
            Triple(0.88f, 0.78f, 0.44f),
            Triple(0.74f, 0.90f, 0.38f),
            Triple(0.82f, 0.84f, 0.32f),
            Triple(0.70f, 0.93f, 0.40f),
            Triple(0.80f, 0.85f, 0.35f),
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rowCount = ((maxHeight / 80.dp).toInt() + 2).coerceIn(12, 24)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            contentPadding = PaddingValues(0.dp),
        ) {
            items(count = rowCount) { index ->
                val (w1, w2, w3) = patterns[index % patterns.size]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardOverlay)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(shimmerBrush),
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(highlightTone),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(w1)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(shimmerBrush),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(w2)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(shimmerBrush),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(w3)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(shimmerBrush),
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(shimmerBrush),
                    )
                }

                if (index < rowCount - 1) {
                    SocialInsetDivider(startIndent = 84.dp)
                }
            }
        }
    }
}
