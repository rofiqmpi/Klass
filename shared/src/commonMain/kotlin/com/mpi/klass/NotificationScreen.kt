package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.notiscreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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
) {
    val scope = rememberCoroutineScope()
    val headingColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val accent = Color(0xFF0284C7)

    var notifications by remember { mutableStateOf<List<AppNotificationItem>>(emptyList()) }
    var activePlan by remember(publishedPlan) { mutableStateOf(publishedPlan) }
    var isLoading by remember { mutableStateOf(true) }
    var showComposeSheet by remember { mutableStateOf(false) }
    var noticeTitle by remember { mutableStateOf("") }
    var noticeBody by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    val todayName = remember { getTodayWorkingDayName() }

    fun refreshNotices() {
        scope.launch {
            isLoading = true
            try {
                notifications = fetchNotificationsFromFirestore()
                if (activePlan == null) {
                    activePlan = fetchRoutinePlanFromFirestore("master_6m_published")
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(email, role) {
        refreshNotices()
    }

    val todaysClasses = remember(activePlan, role, email, studentProfile, teacherPref, todayName) {
        val entries = activePlan?.entries ?: return@remember emptyList()
        val dayEntries = entries.filter { it.day.equals(todayName, ignoreCase = true) }
        when (role) {
            UserRole.TEACHER -> {
                val cleanEmail = email.trim().lowercase()
                dayEntries.filter { it.teacherEmail.trim().lowercase() == cleanEmail }
                    .sortedBy { it.periodIndex }
            }
            UserRole.STUDENT -> {
                val prof = studentProfile
                if (prof == null || prof.department.isBlank() || prof.semester.isBlank()) {
                    emptyList()
                } else {
                    val dept = RoutineRules.normalizeDepartmentShort(prof.department)
                    val shift = RoutineRules.normalizeShift(prof.shift)
                    val sem = RoutineRules.canonicalSemesterKey(prof.semester)
                    val group = RoutineRules.normalizeGroup(dept, prof.section)
                    dayEntries.filter {
                        it.department == dept &&
                            RoutineRules.normalizeShift(it.shift) == shift &&
                            RoutineRules.canonicalSemesterKey(it.semester) == sem &&
                            RoutineRules.normalizeGroup(dept, it.group) == group
                    }.sortedBy { it.periodIndex }
                }
            }
            else -> emptyList()
        }
    }

    val visibleNotices = remember(notifications, role, email) {
        val cleanEmail = email.trim().lowercase()
        val roleKey = role?.name ?: "GUEST"
        notifications.filter { n ->
            val targetMatches = n.targetEmail.isBlank() || n.targetEmail.trim().lowercase() == cleanEmail
            val roleMatches = n.targetRole == "ALL" || n.targetRole.equals(roleKey, ignoreCase = true)
            targetMatches && roleMatches
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NoticeFloatAnim")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "NoticeFloatY",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalBackdrop(isDark = isDark) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 960.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    // Clean Minimal Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MinimalStatBadge(
                                icon = CalendarDaysIcon,
                                text = if (todayName in RoutineRules.WORKING_DAYS) {
                                    "Today • $todayName"
                                } else {
                                    "Weekend • $todayName"
                                },
                                accent = accent,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            if (visibleNotices.isNotEmpty()) {
                                MinimalStatBadge(
                                    icon = NotificationIcon,
                                    text = "${visibleNotices.size}",
                                    accent = Color(0xFFD97706),
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (role == UserRole.ADMIN) {
                                WelcomeMinimal3DButton(
                                    text = "Post Notice",
                                    icon = PlusIcon,
                                    hindSiliguri = hindSiliguri,
                                    isDark = isDark,
                                    height = 40.dp,
                                    cornerRadius = 11.dp,
                                    modifier = Modifier.width(128.dp),
                                    testTag = "admin_post_notice_button",
                                    onClick = { showComposeSheet = true },
                                )
                            }
                            WelcomeMinimal3DIconButton(
                                icon = RefreshIcon,
                                contentDescription = "Refresh notifications",
                                isDark = isDark,
                                iconTint = accent,
                                isLoading = isLoading,
                                enabled = !isLoading,
                                onClick = { refreshNotices() },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading && visibleNotices.isEmpty() && todaysClasses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = accent, strokeWidth = 2.5.dp)
                        }
                    } else if (visibleNotices.isEmpty() && todaysClasses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 28.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedStaggeredItem(index = 0) {
                                MinimalCardSurface(
                                    isDark = isDark,
                                    modifier = Modifier.widthIn(max = 380.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Image(
                                            painter = painterResource(Res.drawable.notiscreen),
                                            contentDescription = "Notifications",
                                            modifier = Modifier
                                                .size(110.dp)
                                                .graphicsLayer(translationY = floatY),
                                            contentScale = ContentScale.Fit,
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "No New Notifications",
                                            fontSize = 16.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = headingColor,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Today's classes and institute notices appear here automatically.",
                                            fontSize = 12.5.sp,
                                            fontFamily = hindSiliguri,
                                            color = subTextColor,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val showSplitPane = maxWidth >= 720.dp &&
                                todaysClasses.isNotEmpty() &&
                                visibleNotices.isNotEmpty()

                            if (showSplitPane) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(bottom = 24.dp),
                                    ) {
                                        item {
                                            Text(
                                                text = "Today's Classes (${todaysClasses.size})",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = headingColor,
                                            )
                                        }
                                        itemsIndexed(todaysClasses, key = { _, e -> "cls_${e.id}" }) { idx, cls ->
                                            AnimatedStaggeredItem(index = idx) {
                                                TodayClassNoticeCard(
                                                    cls = cls,
                                                    role = role,
                                                    isDark = isDark,
                                                    headingColor = headingColor,
                                                    subTextColor = subTextColor,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                        }
                                    }

                                    LazyColumn(
                                        modifier = Modifier.weight(1.15f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(bottom = 24.dp),
                                    ) {
                                        item {
                                            Text(
                                                text = "Notices & Updates (${visibleNotices.size})",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = headingColor,
                                            )
                                        }
                                        itemsIndexed(
                                            visibleNotices,
                                            key = { idx, item -> item.id.ifBlank { "noti_$idx" } },
                                        ) { idx, item ->
                                            AnimatedStaggeredItem(index = idx) {
                                                AnnouncementNoticeCard(
                                                    item = item,
                                                    isDark = isDark,
                                                    headingColor = headingColor,
                                                    subTextColor = subTextColor,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                ) {
                                    if (todaysClasses.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Today's Classes (${todaysClasses.size})",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = headingColor,
                                            )
                                        }
                                        itemsIndexed(todaysClasses, key = { _, e -> "cls_${e.id}" }) { idx, cls ->
                                            AnimatedStaggeredItem(index = idx) {
                                                TodayClassNoticeCard(
                                                    cls = cls,
                                                    role = role,
                                                    isDark = isDark,
                                                    headingColor = headingColor,
                                                    subTextColor = subTextColor,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                        }
                                    }

                                    if (visibleNotices.isNotEmpty()) {
                                        item {
                                            if (todaysClasses.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                            Text(
                                                text = "Notices & Updates (${visibleNotices.size})",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = headingColor,
                                            )
                                        }
                                        itemsIndexed(
                                            visibleNotices,
                                            key = { idx, item -> item.id.ifBlank { "noti_$idx" } },
                                        ) { idx, item ->
                                            AnimatedStaggeredItem(index = idx) {
                                                AnnouncementNoticeCard(
                                                    item = item,
                                                    isDark = isDark,
                                                    headingColor = headingColor,
                                                    subTextColor = subTextColor,
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
            }
        }

        AnimatedVisibility(
            visible = showComposeSheet,
            enter = fadeIn(tween(220)) + slideInVertically(tween(240)) { it / 6 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 6 },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(80f),
        ) {
            MinimalFullScreenSheet(
                title = "Post Notice",
                subtitle = "Broadcast to All Users",
                icon = NotificationIcon,
                accent = accent,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = { if (!isPosting) showComposeSheet = false },
                dismissEnabled = !isPosting,
                maxContentWidth = 680.dp,
                bottomBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        WelcomeMinimal3DButton(
                            text = "Cancel",
                            hindSiliguri = hindSiliguri,
                            isPrimaryAccent = false,
                            isDark = isDark,
                            enabled = !isPosting,
                            height = 46.dp,
                            modifier = Modifier.weight(1f),
                            onClick = { showComposeSheet = false },
                        )
                        WelcomeMinimal3DButton(
                            text = if (isPosting) "Publishing..." else "Publish Notice",
                            icon = CheckIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            isLoading = isPosting,
                            enabled = !isPosting,
                            height = 46.dp,
                            modifier = Modifier.weight(1.3f),
                            onClick = {
                                if (noticeTitle.isBlank() || noticeBody.isBlank()) {
                                    showToast("Please fill in title and message.")
                                    return@WelcomeMinimal3DButton
                                }
                                scope.launch {
                                    isPosting = true
                                    try {
                                        val now = getCurrentTimeMillis()
                                        publishNotificationInFirestore(
                                            AppNotificationItem(
                                                id = "noti_adm_$now",
                                                title = noticeTitle.trim(),
                                                body = noticeBody.trim(),
                                                category = "GENERAL",
                                                targetRole = "ALL",
                                                createdAt = now,
                                            )
                                        )
                                        noticeTitle = ""
                                        noticeBody = ""
                                        showComposeSheet = false
                                        refreshNotices()
                                        showToast("Notice published!")
                                    } finally {
                                        isPosting = false
                                    }
                                }
                            },
                        )
                    }
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column {
                        Text(
                            text = "Notice Title *",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = headingColor,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, borderCol),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (noticeTitle.isEmpty()) {
                                    Text(
                                        text = "e.g. Exam Schedule Update",
                                        color = subTextColor,
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                    )
                                }
                                BasicTextField(
                                    value = noticeTitle,
                                    onValueChange = { noticeTitle = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = headingColor,
                                        fontSize = 14.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    cursorBrush = SolidColor(accent),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Notice Message *",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = headingColor,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, borderCol),
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                contentAlignment = Alignment.TopStart,
                            ) {
                                if (noticeBody.isEmpty()) {
                                    Text(
                                        text = "Write the announcement details...",
                                        color = subTextColor,
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                    )
                                }
                                BasicTextField(
                                    value = noticeBody,
                                    onValueChange = { noticeBody = it },
                                    textStyle = TextStyle(
                                        color = headingColor,
                                        fontSize = 14.sp,
                                        fontFamily = hindSiliguri,
                                        lineHeight = 20.sp,
                                    ),
                                    cursorBrush = SolidColor(accent),
                                    modifier = Modifier.fillMaxSize(),
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
private fun TodayClassNoticeCard(
    cls: RoutineEntry,
    role: UserRole?,
    isDark: Boolean,
    headingColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
) {
    val emerald = Color(0xFF059669)
    MinimalCardSurface(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(emerald.copy(alpha = if (isDark) 0.16f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ClockIcon,
                    contentDescription = null,
                    tint = emerald,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${cls.subjectTitle} (${cls.subjectCode})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = headingColor,
                )
                Text(
                    text = "${cls.startTime} – ${cls.endTime} • Room ${cls.roomNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = hindSiliguri,
                    color = emerald,
                )
                Text(
                    text = if (role == UserRole.TEACHER) {
                        "${cls.department} ${RoutineRules.semesterShortLabel(cls.semester)} ${cls.group} • ${cls.shift}"
                    } else {
                        "Instructor: ${cls.teacherName.ifBlank { "Faculty" }}"
                    },
                    fontSize = 11.5.sp,
                    fontFamily = hindSiliguri,
                    color = subTextColor,
                )
            }
        }
    }
}

@Composable
private fun AnnouncementNoticeCard(
    item: AppNotificationItem,
    isDark: Boolean,
    headingColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
) {
    val badgeColor = when (item.category) {
        "ROUTINE" -> Color(0xFF0284C7)
        "REQUEST" -> Color(0xFF7C3AED)
        else -> Color(0xFFD97706)
    }
    MinimalCardSurface(isDark = isDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = if (isDark) 0.16f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = NotificationIcon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(17.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = headingColor,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.body,
                    fontSize = 12.5.sp,
                    fontFamily = hindSiliguri,
                    color = subTextColor,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
