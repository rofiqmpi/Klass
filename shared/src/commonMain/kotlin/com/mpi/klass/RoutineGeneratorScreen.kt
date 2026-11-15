package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.SwapIcon
import com.mpi.klass.theme.WarningIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineGeneratorScreen(
    adminEmail: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
    onPlanPublished: (RoutinePlan) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    var books by remember { mutableStateOf<List<LibraryBook>>(emptyList()) }
    var teachers by remember { mutableStateOf<List<TeacherSchedulingPref>>(emptyList()) }
    var classrooms by remember { mutableStateOf<List<ClassroomItem>>(emptyList()) }
    var currentPlan by remember { mutableStateOf<RoutinePlan?>(null) }
    var editRequests by remember { mutableStateOf<List<RoutineEditRequest>>(emptyList()) }

    var shift1Start by remember { mutableStateOf("08:00") }
    var shift2Start by remember { mutableStateOf("13:30") }
    var periodDuration by remember { mutableIntStateOf(45) }
    var periodsPerDay by remember { mutableIntStateOf(7) }
    var activeSemesters by remember { mutableStateOf(RoutineRules.ALL_SEMESTER_KEYS.toSet()) }

    var explorerMode by remember { mutableIntStateOf(0) }
    var filterDept by remember { mutableStateOf("CST") }
    var filterShift by remember { mutableStateOf(RoutineRules.SHIFT_1) }
    var filterSem by remember { mutableStateOf(RoutineRules.ALL_SEMESTER_KEYS.first()) }
    var filterGroup by remember { mutableStateOf("Group A") }
    var filterTeacherEmail by remember { mutableStateOf("") }
    var filterRoomNumber by remember { mutableStateOf("") }

    var editingSlot by remember { mutableStateOf<RoutineEntry?>(null) }
    var resolvingConflict by remember { mutableStateOf<RoutineConflict?>(null) }

    LaunchedEffect(editingSlot, resolvingConflict) {
        onFullScreenOverlayChange(editingSlot != null || resolvingConflict != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val accent = Color(0xFF0284C7)

    fun refreshAll() {
        scope.launch {
            isLoading = true
            try {
                books = RoleConfig.fetchLibraryBooks().ifEmpty { BtebBooksCatalog.books }
                teachers = fetchAllTeacherSchedulingPrefsFromFirestore()
                classrooms = fetchClassroomsFromFirestore()
                val draft = fetchRoutinePlanFromFirestore("master_6m")
                val pub = fetchRoutinePlanFromFirestore("master_6m_published")
                val loadedPlan = draft ?: pub
                currentPlan = loadedPlan
                if (loadedPlan != null) {
                    shift1Start = loadedPlan.config.shift1StartTime
                    shift2Start = loadedPlan.config.shift2StartTime
                    periodDuration = loadedPlan.config.periodDurationMinutes
                    periodsPerDay = loadedPlan.config.periodsPerDay
                    activeSemesters = loadedPlan.config.activeSemesters.toSet().ifEmpty {
                        RoutineRules.ALL_SEMESTER_KEYS.toSet()
                    }
                }
                if (filterTeacherEmail.isBlank()) {
                    filterTeacherEmail = teachers.firstOrNull()?.email ?: ""
                }
                if (filterRoomNumber.isBlank()) {
                    filterRoomNumber = classrooms.firstOrNull()?.roomNumber ?: ""
                }
                editRequests = fetchRoutineEditRequestsFromFirestore()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    val pendingRequestsCount = editRequests.count { it.status == EditRequestStatus.PENDING }

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
                        .widthIn(max = 980.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    // Minimal Iconic 3-Segment Switcher
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val tabs: List<Triple<Int, String, ImageVector>> = listOf(
                                Triple(0, "Generate", SparkleIcon),
                                Triple(1, "Explorer", CalendarDaysIcon),
                                Triple(
                                    2,
                                    if (pendingRequestsCount > 0) "Requests ($pendingRequestsCount)" else "Requests",
                                    SwapIcon,
                                ),
                            )
                            tabs.forEach { (idx, title, icon) ->
                                val selected = activeTab == idx
                                Surface(
                                    onClick = { activeTab = idx },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) accent else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("routine_tab_$idx"),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selected) Color.White else accent,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = if (selected) Color.White else textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading && currentPlan == null && books.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = accent, strokeWidth = 2.5.dp)
                        }
                        return@Column
                    }

                    when (activeTab) {
                        // ─── TAB 0: CONFIGURE, GENERATE & PUBLISH ─────────────────
                        0 -> {
                            val readyTeachers = teachers.count { it.schedulingSetupComplete }
                            val theoryRooms = classrooms.count { it.roomType == ClassroomType.THEORY }
                            val labRooms = classrooms.count { it.roomType == ClassroomType.LAB }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                            ) {
                                // Clean Iconic Readiness Badges Strip + Refresh
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            item {
                                                MinimalStatBadge(
                                                    icon = GraduationCapIcon,
                                                    text = "$readyTeachers Faculty",
                                                    accent = Color(0xFF059669),
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                            item {
                                                MinimalStatBadge(
                                                    icon = DoorOpenIcon,
                                                    text = "${theoryRooms}T / ${labRooms}L",
                                                    accent = Color(0xFF7C3AED),
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                            item {
                                                MinimalStatBadge(
                                                    icon = BookIcon,
                                                    text = "${books.size} Books",
                                                    accent = accent,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        WelcomeMinimal3DIconButton(
                                            icon = RefreshIcon,
                                            contentDescription = "Refresh data",
                                            isDark = isDark,
                                            iconTint = accent,
                                            isLoading = isLoading,
                                            enabled = !isLoading,
                                            onClick = { refreshAll() },
                                        )
                                    }
                                }

                                // Responsive Config Grid (2-col on >= 640dp, 1-col on phone)
                                item {
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val twoCol = maxWidth >= 640.dp

                                        val timingsCard: @Composable (Modifier) -> Unit = { mod ->
                                            MinimalCardSurface(isDark = isDark, modifier = mod) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    ) {
                                                        Icon(
                                                            imageVector = ClockIcon,
                                                            contentDescription = null,
                                                            tint = accent,
                                                            modifier = Modifier.size(16.dp),
                                                        )
                                                        Text(
                                                            text = "Shift Timings & Periods",
                                                            fontSize = 14.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = hindSiliguri,
                                                            color = textColor,
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    ) {
                                                        RoutineConfigTimeField(
                                                            label = "1st Shift (24h)",
                                                            value = shift1Start,
                                                            preview = RoutineRules.formatMinutesToTime(
                                                                RoutineRules.parseMinutesFromHhMm(shift1Start, 8 * 60)
                                                            ),
                                                            onValueChange = { shift1Start = it },
                                                            isDark = isDark,
                                                            hindSiliguri = hindSiliguri,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        RoutineConfigTimeField(
                                                            label = "2nd Shift (24h)",
                                                            value = shift2Start,
                                                            preview = RoutineRules.formatMinutesToTime(
                                                                RoutineRules.parseMinutesFromHhMm(shift2Start, 13 * 60 + 30)
                                                            ),
                                                            onValueChange = { shift2Start = it },
                                                            isDark = isDark,
                                                            hindSiliguri = hindSiliguri,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    ) {
                                                        StepperChipCard(
                                                            label = "Duration",
                                                            valueText = "$periodDuration min",
                                                            onMinus = { if (periodDuration > 30) periodDuration -= 5 },
                                                            onPlus = { if (periodDuration < 75) periodDuration += 5 },
                                                            isDark = isDark,
                                                            hindSiliguri = hindSiliguri,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        StepperChipCard(
                                                            label = "Periods / Day",
                                                            valueText = "$periodsPerDay",
                                                            onMinus = { if (periodsPerDay > 4) periodsPerDay -= 1 },
                                                            onPlus = { if (periodsPerDay < 10) periodsPerDay += 1 },
                                                            isDark = isDark,
                                                            hindSiliguri = hindSiliguri,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        val semestersAndActionsCard: @Composable (Modifier) -> Unit = { mod ->
                                            MinimalCardSurface(isDark = isDark, modifier = mod) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Text(
                                                            text = "Active Semesters (1–7)",
                                                            fontSize = 14.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = hindSiliguri,
                                                            color = textColor,
                                                        )
                                                        Text(
                                                            text = "Sun–Thu • 5 Days",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontFamily = hindSiliguri,
                                                            color = subTextColor,
                                                        )
                                                    }

                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    ) {
                                                        RoutineRules.ALL_SEMESTER_KEYS.forEach { sem ->
                                                            val selected = sem in activeSemesters
                                                            MinimalIconChip(
                                                                label = RoutineRules.semesterShortLabel(sem),
                                                                selected = selected,
                                                                accent = accent,
                                                                isDark = isDark,
                                                                hindSiliguri = hindSiliguri,
                                                                onClick = {
                                                                    activeSemesters = if (selected && activeSemesters.size > 1) {
                                                                        activeSemesters - sem
                                                                    } else {
                                                                        activeSemesters + sem
                                                                    }
                                                                },
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    ) {
                                                        WelcomeMinimal3DButton(
                                                            text = "Generate Draft",
                                                            icon = SparkleIcon,
                                                            hindSiliguri = hindSiliguri,
                                                            isDark = isDark,
                                                            isLoading = isProcessing,
                                                            modifier = Modifier.weight(1f),
                                                            testTag = "generate_routine_button",
                                                            onClick = {
                                                                if (classrooms.isEmpty()) {
                                                                    showToast("Please add Classrooms & Labs first.")
                                                                    return@WelcomeMinimal3DButton
                                                                }
                                                                if (teachers.isEmpty()) {
                                                                    showToast("No teachers have configured their subjects yet.")
                                                                    return@WelcomeMinimal3DButton
                                                                }
                                                                scope.launch {
                                                                    isProcessing = true
                                                                    try {
                                                                        val cfg = RoutineConfig(
                                                                            shift1StartTime = shift1Start.trim().ifBlank { "08:00" },
                                                                            shift2StartTime = shift2Start.trim().ifBlank { "13:30" },
                                                                            periodDurationMinutes = periodDuration,
                                                                            periodsPerDay = periodsPerDay,
                                                                            activeSemesters = activeSemesters.toList(),
                                                                            validMonths = 6,
                                                                        )
                                                                        val draft = RoutineGeneratorEngine.generateRoutine(
                                                                            config = cfg,
                                                                            books = books,
                                                                            teachers = teachers,
                                                                            classrooms = classrooms,
                                                                            generatedBy = adminEmail,
                                                                            nowMillis = getCurrentTimeMillis(),
                                                                        )
                                                                        currentPlan = draft
                                                                        saveRoutinePlanInFirestore(draft)
                                                                        showToast(
                                                                            "Draft ready: ${draft.entries.size} slots, ${draft.conflicts.size} conflict(s)."
                                                                        )
                                                                    } finally {
                                                                        isProcessing = false
                                                                    }
                                                                }
                                                            },
                                                        )

                                                        val planToPub = currentPlan
                                                        WelcomeMinimal3DButton(
                                                            text = "Publish Live",
                                                            icon = CheckIcon,
                                                            hindSiliguri = hindSiliguri,
                                                            isPrimaryAccent = false,
                                                            isDark = isDark,
                                                            enabled = planToPub != null && !isProcessing,
                                                            modifier = Modifier.weight(1f),
                                                            testTag = "publish_routine_button",
                                                            onClick = {
                                                                if (planToPub == null) return@WelcomeMinimal3DButton
                                                                scope.launch {
                                                                    isProcessing = true
                                                                    try {
                                                                        val now = getCurrentTimeMillis()
                                                                        val published = planToPub.copy(
                                                                            id = "master_6m_published",
                                                                            status = "PUBLISHED",
                                                                            publishedAt = now,
                                                                        )
                                                                        saveRoutinePlanInFirestore(published)
                                                                        saveRoutinePlanInFirestore(
                                                                            published.copy(id = "master_6m")
                                                                        )
                                                                        publishNotificationInFirestore(
                                                                            AppNotificationItem(
                                                                                id = "noti_pub_$now",
                                                                                title = "6-Month Class Routine Published",
                                                                                body = "A new institute class routine (${published.entries.size} scheduled slots) is now live.",
                                                                                category = "ROUTINE",
                                                                                targetRole = "ALL",
                                                                                createdAt = now,
                                                                            )
                                                                        )
                                                                        currentPlan = published
                                                                        onPlanPublished(published)
                                                                        showToast("6-Month routine published live!")
                                                                    } finally {
                                                                        isProcessing = false
                                                                    }
                                                                }
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (twoCol) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                timingsCard(Modifier.weight(1f))
                                                semestersAndActionsCard(Modifier.weight(1f))
                                            }
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                timingsCard(Modifier.fillMaxWidth())
                                                semestersAndActionsCard(Modifier.fillMaxWidth())
                                            }
                                        }
                                    }
                                }

                                // Plan Status + Responsive Conflicts Grid
                                val plan = currentPlan
                                if (plan != null) {
                                    item {
                                        val isPub = plan.status == "PUBLISHED"
                                        val statusColor = if (isPub) Color(0xFF059669) else Color(0xFFD97706)
                                        MinimalCardSurface(
                                            isDark = isDark,
                                            borderColor = statusColor.copy(alpha = 0.4f),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Plan Status: ${plan.status}",
                                                        fontSize = 14.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = hindSiliguri,
                                                        color = statusColor,
                                                    )
                                                    Text(
                                                        text = "${plan.entries.size} scheduled slots • ${plan.conflicts.size} conflict(s)",
                                                        fontSize = 12.sp,
                                                        fontFamily = hindSiliguri,
                                                        color = subTextColor,
                                                    )
                                                }
                                                MinimalIconChip(
                                                    label = "Explore",
                                                    selected = true,
                                                    accent = accent,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    onClick = { activeTab = 1 },
                                                )
                                            }
                                        }
                                    }

                                    if (plan.conflicts.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Detected Conflicts (${plan.conflicts.size})",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = Color(0xFFEF4444),
                                            )
                                        }
                                        item {
                                            MinimalResponsiveGrid(
                                                items = plan.conflicts,
                                                twoColMinWidth = 620.dp,
                                                spacing = 10.dp,
                                            ) { _, conf ->
                                                MinimalCardSurface(
                                                    isDark = isDark,
                                                    borderColor = Color(0xFFEF4444).copy(alpha = 0.45f),
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Icon(
                                                            imageVector = WarningIcon,
                                                            contentDescription = null,
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(19.dp),
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "${conf.department} • ${RoutineRules.semesterShortLabel(conf.semester)} • ${conf.shift} • ${conf.group}",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                fontFamily = hindSiliguri,
                                                                color = textColor,
                                                            )
                                                            Text(
                                                                text = "${conf.subjectTitle} (${conf.subjectCode})",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontFamily = hindSiliguri,
                                                                color = accent,
                                                            )
                                                            Text(
                                                                text = conf.reason,
                                                                fontSize = 11.5.sp,
                                                                fontFamily = hindSiliguri,
                                                                color = subTextColor,
                                                            )
                                                        }
                                                        WelcomeMinimal3DIconButton(
                                                            icon = PlusIcon,
                                                            contentDescription = "Assign slot manually",
                                                            isDark = isDark,
                                                            iconTint = accent,
                                                            visualSize = 34.dp,
                                                            iconSize = 15.dp,
                                                            onClick = { resolvingConflict = conf },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ─── TAB 1: EXPLORER & MANUAL OVERRIDE ────────────────────
                        1 -> {
                            val plan = currentPlan
                            if (plan == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    MinimalCardSurface(
                                        isDark = isDark,
                                        modifier = Modifier.widthIn(max = 380.dp),
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Icon(
                                                imageVector = CalendarDaysIcon,
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(38.dp),
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "No Routine Generated Yet",
                                                fontSize = 15.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = textColor,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Create your 6-month draft in the Generate tab first.",
                                                fontSize = 12.5.sp,
                                                fontFamily = hindSiliguri,
                                                color = subTextColor,
                                            )
                                        }
                                    }
                                }
                                return@Column
                            }

                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    listOf(0 to "By Group", 1 to "By Teacher", 2 to "By Room").forEach { (m, label) ->
                                        MinimalIconChip(
                                            label = label,
                                            selected = explorerMode == m,
                                            accent = Color(0xFF7C3AED),
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            modifier = Modifier.weight(1f),
                                            onClick = { explorerMode = m },
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                when (explorerMode) {
                                    0 -> {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(RoutineRules.TECH_DEPARTMENTS) { d ->
                                                FilterPill(d, filterDept == d, cardBg, borderCol, accent, textColor, hindSiliguri) {
                                                    filterDept = d
                                                    filterGroup = RoutineRules.normalizeGroup(d, filterGroup)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)) { sh ->
                                                FilterPill(sh, filterShift == sh, cardBg, borderCol, Color(0xFF059669), textColor, hindSiliguri) {
                                                    filterShift = sh
                                                }
                                            }
                                            items(RoutineRules.groupsForDepartment(filterDept)) { g ->
                                                FilterPill(g, filterGroup == g, cardBg, borderCol, Color(0xFFD97706), textColor, hindSiliguri) {
                                                    filterGroup = g
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(RoutineRules.ALL_SEMESTER_KEYS) { s ->
                                                FilterPill(
                                                    RoutineRules.semesterShortLabel(s),
                                                    filterSem == s,
                                                    cardBg,
                                                    borderCol,
                                                    Color(0xFF7C3AED),
                                                    textColor,
                                                    hindSiliguri,
                                                ) {
                                                    filterSem = s
                                                }
                                            }
                                        }
                                    }
                                    1 -> {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(teachers) { t ->
                                                FilterPill(
                                                    t.name.ifBlank { t.email.substringBefore("@") },
                                                    filterTeacherEmail.equals(t.email, true),
                                                    cardBg,
                                                    borderCol,
                                                    accent,
                                                    textColor,
                                                    hindSiliguri,
                                                ) {
                                                    filterTeacherEmail = t.email
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2).forEach { sh ->
                                                FilterPill(sh, filterShift == sh, cardBg, borderCol, Color(0xFF059669), textColor, hindSiliguri) {
                                                    filterShift = sh
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(classrooms) { r ->
                                                FilterPill(
                                                    "Room ${r.roomNumber}",
                                                    filterRoomNumber.equals(r.roomNumber, true),
                                                    cardBg,
                                                    borderCol,
                                                    Color(0xFF7C3AED),
                                                    textColor,
                                                    hindSiliguri,
                                                ) {
                                                    filterRoomNumber = r.roomNumber
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2).forEach { sh ->
                                                FilterPill(sh, filterShift == sh, cardBg, borderCol, Color(0xFF059669), textColor, hindSiliguri) {
                                                    filterShift = sh
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val filteredEntries = remember(
                                    plan.entries,
                                    explorerMode,
                                    filterDept,
                                    filterShift,
                                    filterSem,
                                    filterGroup,
                                    filterTeacherEmail,
                                    filterRoomNumber,
                                ) {
                                    when (explorerMode) {
                                        0 -> plan.entries.filter {
                                            it.department == filterDept &&
                                                RoutineRules.normalizeShift(it.shift) == filterShift &&
                                                RoutineRules.canonicalSemesterKey(it.semester) == filterSem &&
                                                RoutineRules.normalizeGroup(filterDept, it.group) == filterGroup
                                        }
                                        1 -> plan.entries.filter {
                                            it.teacherEmail.equals(filterTeacherEmail, ignoreCase = true) &&
                                                RoutineRules.normalizeShift(it.shift) == filterShift
                                        }
                                        else -> plan.entries.filter {
                                            it.roomNumber.trim().equals(filterRoomNumber.trim(), ignoreCase = true) &&
                                                RoutineRules.normalizeShift(it.shift) == filterShift
                                        }
                                    }
                                }

                                Text(
                                    text = "Tap any slot to override • ${filteredEntries.size} classes shown",
                                    fontSize = 11.5.sp,
                                    fontFamily = hindSiliguri,
                                    color = subTextColor,
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                ) {
                                    ClassRoutineTable(
                                        hindSiliguri = hindSiliguri,
                                        isDark = isDark,
                                        entries = filteredEntries,
                                        periodsPerDay = plan.config.periodsPerDay,
                                        shift = filterShift,
                                        config = plan.config,
                                        showGroupOrRoomHint = explorerMode != 0,
                                        emptyMessage = "No classes scheduled for this filter.",
                                        onSlotClick = { entry -> editingSlot = entry },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        // ─── TAB 2: TEACHER EDIT / SWAP REQUESTS ──────────────────
                        else -> {
                            if (editRequests.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    MinimalCardSurface(
                                        isDark = isDark,
                                        modifier = Modifier.widthIn(max = 380.dp),
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Icon(
                                                imageVector = SwapIcon,
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(38.dp),
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "No Teacher Edit Requests",
                                                fontSize = 15.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = textColor,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Teacher slot swap requests appear here with live clash checking.",
                                                fontSize = 12.5.sp,
                                                fontFamily = hindSiliguri,
                                                color = subTextColor,
                                            )
                                        }
                                    }
                                }
                            } else {
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val cols = if (maxWidth >= 640.dp) 2 else 1
                                    val reqRows = remember(editRequests, cols) {
                                        editRequests.chunked(cols)
                                    }

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(bottom = 24.dp),
                                    ) {
                                        itemsIndexed(
                                            items = reqRows,
                                            key = { idx, row -> row.firstOrNull()?.id ?: "req_row_$idx" },
                                        ) { _, rowItems ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                rowItems.forEach { req ->
                                                    val statusColor = when (req.status) {
                                                        EditRequestStatus.PENDING -> Color(0xFFD97706)
                                                        EditRequestStatus.APPROVED -> Color(0xFF059669)
                                                        EditRequestStatus.DECLINED -> Color(0xFFEF4444)
                                                    }
                                                    val targetEntry = currentPlan?.entries?.firstOrNull { it.id == req.targetEntryId }
                                                    val clashes = remember(req, currentPlan, targetEntry) {
                                                        val planEntries = currentPlan?.entries ?: emptyList()
                                                        if (req.status != EditRequestStatus.PENDING || planEntries.isEmpty()) {
                                                            emptyList()
                                                        } else {
                                                            RoutineGeneratorEngine.validateSlotEdit(
                                                                allEntries = planEntries,
                                                                editingEntryId = req.targetEntryId,
                                                                targetShift = req.shift,
                                                                targetDay = req.requestedDay,
                                                                targetPeriod = req.requestedPeriod,
                                                                targetDepartment = req.department,
                                                                targetSemester = req.semester,
                                                                targetGroup = req.group,
                                                                targetTeacherEmail = targetEntry?.teacherEmail ?: req.requestedByEmail,
                                                                targetRoomNumber = req.requestedRoomNumber,
                                                            )
                                                        }
                                                    }

                                                    Box(modifier = Modifier.weight(1f)) {
                                                        MinimalCardSurface(
                                                            isDark = isDark,
                                                            borderColor = statusColor.copy(alpha = 0.4f),
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(14.dp),
                                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Text(
                                                                        text = "${req.requestedByName.ifBlank { req.requestedByEmail }} • ${req.subjectTitle}",
                                                                        fontSize = 14.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontFamily = hindSiliguri,
                                                                        color = textColor,
                                                                        modifier = Modifier.weight(1f),
                                                                    )
                                                                    Surface(
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        color = statusColor.copy(alpha = 0.14f),
                                                                    ) {
                                                                        Text(
                                                                            text = req.status.label,
                                                                            fontSize = 10.5.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = hindSiliguri,
                                                                            color = statusColor,
                                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                        )
                                                                    }
                                                                }

                                                                Text(
                                                                    text = "${req.department} • ${RoutineRules.semesterShortLabel(req.semester)} • ${req.shift} • ${req.group}",
                                                                    fontSize = 12.sp,
                                                                    fontFamily = hindSiliguri,
                                                                    color = accent,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                )

                                                                Text(
                                                                    text = "${req.originalDay} P${req.originalPeriod + 1} (${req.originalRoomNumber}) → ${req.requestedDay} P${req.requestedPeriod + 1} (${req.requestedRoomNumber})",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    fontFamily = hindSiliguri,
                                                                    color = textColor,
                                                                )

                                                                if (req.reason.isNotBlank()) {
                                                                    Text(
                                                                        text = req.reason,
                                                                        fontSize = 11.5.sp,
                                                                        fontFamily = hindSiliguri,
                                                                        color = subTextColor,
                                                                    )
                                                                }

                                                                if (req.status == EditRequestStatus.PENDING) {
                                                                    Surface(
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        color = if (clashes.isEmpty()) {
                                                                            Color(0xFF10B981).copy(alpha = 0.12f)
                                                                        } else {
                                                                            Color(0xFFEF4444).copy(alpha = 0.12f)
                                                                        },
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                    ) {
                                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                                            if (clashes.isEmpty()) {
                                                                                Text(
                                                                                    text = "Clash Check: Clear",
                                                                                    fontSize = 11.5.sp,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    fontFamily = hindSiliguri,
                                                                                    color = Color(0xFF059669),
                                                                                )
                                                                            } else {
                                                                                clashes.forEach { msg ->
                                                                                    Text(
                                                                                        text = "• $msg",
                                                                                        fontSize = 11.5.sp,
                                                                                        fontFamily = hindSiliguri,
                                                                                        color = Color(0xFFEF4444),
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    ) {
                                                                        WelcomeMinimal3DButton(
                                                                            text = "Approve",
                                                                            icon = CheckIcon,
                                                                            hindSiliguri = hindSiliguri,
                                                                            isDark = isDark,
                                                                            enabled = !isProcessing,
                                                                            height = 42.dp,
                                                                            modifier = Modifier.weight(1f),
                                                                            onClick = {
                                                                                scope.launch {
                                                                                    isProcessing = true
                                                                                    try {
                                                                                        val plan = currentPlan
                                                                                        if (plan != null) {
                                                                                            val (st, et) = RoutineRules.periodTimeRange(
                                                                                                req.shift,
                                                                                                req.requestedPeriod,
                                                                                                plan.config,
                                                                                            )
                                                                                            val updatedEntries = plan.entries.map { entry ->
                                                                                                if (entry.id == req.targetEntryId) {
                                                                                                    entry.copy(
                                                                                                        day = req.requestedDay,
                                                                                                        periodIndex = req.requestedPeriod,
                                                                                                        startTime = st,
                                                                                                        endTime = et,
                                                                                                        roomId = req.requestedRoomId.ifBlank { entry.roomId },
                                                                                                        roomNumber = req.requestedRoomNumber.ifBlank { entry.roomNumber },
                                                                                                        isManualOverride = true,
                                                                                                    )
                                                                                                } else {
                                                                                                    entry
                                                                                                }
                                                                                            }
                                                                                            val updatedPlan = plan.copy(
                                                                                                entries = updatedEntries,
                                                                                                publishedAt = getCurrentTimeMillis(),
                                                                                            )
                                                                                            saveRoutinePlanInFirestore(updatedPlan)
                                                                                            if (updatedPlan.status == "PUBLISHED") {
                                                                                                saveRoutinePlanInFirestore(
                                                                                                    updatedPlan.copy(id = "master_6m_published")
                                                                                                )
                                                                                                onPlanPublished(updatedPlan)
                                                                                            }
                                                                                            currentPlan = updatedPlan
                                                                                        }
                                                                                        resolveRoutineEditRequestInFirestore(
                                                                                            requestId = req.id,
                                                                                            newStatus = EditRequestStatus.APPROVED,
                                                                                            adminNote = "Approved by Admin",
                                                                                        )
                                                                                        val now = getCurrentTimeMillis()
                                                                                        publishNotificationInFirestore(
                                                                                            AppNotificationItem(
                                                                                                id = "noti_req_app_$now",
                                                                                                title = "Routine Edit Approved",
                                                                                                body = "${req.subjectTitle} (${req.department} ${req.group}) moved to ${req.requestedDay} P${req.requestedPeriod + 1}, Room ${req.requestedRoomNumber}.",
                                                                                                category = "REQUEST",
                                                                                                targetRole = "TEACHER",
                                                                                                targetEmail = req.requestedByEmail,
                                                                                                createdAt = now,
                                                                                            )
                                                                                        )
                                                                                        refreshAll()
                                                                                        showToast("Edit request approved & routine updated.")
                                                                                    } finally {
                                                                                        isProcessing = false
                                                                                    }
                                                                                }
                                                                            },
                                                                        )

                                                                        WelcomeMinimal3DButton(
                                                                            text = "Decline",
                                                                            icon = CloseIcon,
                                                                            hindSiliguri = hindSiliguri,
                                                                            isPrimaryAccent = false,
                                                                            isDestructive = true,
                                                                            isDark = isDark,
                                                                            enabled = !isProcessing,
                                                                            height = 42.dp,
                                                                            modifier = Modifier.weight(1f),
                                                                            onClick = {
                                                                                scope.launch {
                                                                                    isProcessing = true
                                                                                    try {
                                                                                        resolveRoutineEditRequestInFirestore(
                                                                                            requestId = req.id,
                                                                                            newStatus = EditRequestStatus.DECLINED,
                                                                                            adminNote = "Declined by Admin",
                                                                                        )
                                                                                        val now = getCurrentTimeMillis()
                                                                                        publishNotificationInFirestore(
                                                                                            AppNotificationItem(
                                                                                                id = "noti_req_dec_$now",
                                                                                                title = "Routine Edit Declined",
                                                                                                body = "Your edit request for ${req.subjectTitle} (${req.requestedDay} P${req.requestedPeriod + 1}) was declined.",
                                                                                                category = "REQUEST",
                                                                                                targetRole = "TEACHER",
                                                                                                targetEmail = req.requestedByEmail,
                                                                                                createdAt = now,
                                                                                            )
                                                                                        )
                                                                                        refreshAll()
                                                                                        showToast("Edit request declined.")
                                                                                    } finally {
                                                                                        isProcessing = false
                                                                                    }
                                                                                }
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
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
                        }
                    }
                }
            }
        }

        val activeEdit = editingSlot
        val activeConf = resolvingConflict
        val plan = currentPlan
        if (plan != null && (activeEdit != null || activeConf != null)) {
            Box(modifier = Modifier.fillMaxSize().zIndex(80f)) {
                ManualSlotEditorOverlay(
                    plan = plan,
                    initialEntry = activeEdit,
                    conflict = activeConf,
                    teachers = teachers,
                    classrooms = classrooms,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    isSaving = isProcessing,
                    onDismiss = {
                        if (!isProcessing) {
                            editingSlot = null
                            resolvingConflict = null
                        }
                    },
                    onSaveSlot = { updatedEntry, removeConflictId ->
                        scope.launch {
                            isProcessing = true
                            try {
                                val existingIdx = plan.entries.indexOfFirst { it.id == updatedEntry.id }
                                val updatedEntries = if (existingIdx >= 0) {
                                    plan.entries.toMutableList().apply { set(existingIdx, updatedEntry) }
                                } else {
                                    plan.entries + updatedEntry
                                }.sortedWith(
                                    compareBy<RoutineEntry> { RoutineRules.dayOrderIndex(it.day) }
                                        .thenBy { it.periodIndex }
                                )
                                val updatedConflicts = if (removeConflictId != null) {
                                    plan.conflicts.filterNot { it.id == removeConflictId }
                                } else {
                                    plan.conflicts
                                }
                                val updatedPlan = plan.copy(
                                    entries = updatedEntries,
                                    conflicts = updatedConflicts,
                                )
                                saveRoutinePlanInFirestore(updatedPlan)
                                if (updatedPlan.status == "PUBLISHED") {
                                    saveRoutinePlanInFirestore(updatedPlan.copy(id = "master_6m_published"))
                                    onPlanPublished(updatedPlan)
                                }
                                currentPlan = updatedPlan
                                editingSlot = null
                                resolvingConflict = null
                                showToast("Routine slot updated!")
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManualSlotEditorOverlay(
    plan: RoutinePlan,
    initialEntry: RoutineEntry?,
    conflict: RoutineConflict?,
    teachers: List<TeacherSchedulingPref>,
    classrooms: List<ClassroomItem>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSaveSlot: (RoutineEntry, removeConflictId: String?) -> Unit,
) {
    val shift = initialEntry?.shift ?: conflict?.shift ?: RoutineRules.SHIFT_1
    val dept = initialEntry?.department ?: conflict?.department ?: "CST"
    val sem = initialEntry?.semester ?: conflict?.semester ?: RoutineRules.ALL_SEMESTER_KEYS.first()
    val group = initialEntry?.group ?: conflict?.group ?: "Group A"
    val subjectCode = initialEntry?.subjectCode ?: conflict?.subjectCode ?: ""
    val subjectTitle = initialEntry?.subjectTitle ?: conflict?.subjectTitle ?: ""
    val entryId = initialEntry?.id ?: ("ent_manual_" + getCurrentTimeMillis())

    var selectedDay by remember { mutableStateOf(initialEntry?.day ?: RoutineRules.WORKING_DAYS.first()) }
    var selectedPeriod by remember { mutableIntStateOf(initialEntry?.periodIndex ?: 0) }
    var selectedTeacherEmail by remember {
        mutableStateOf(initialEntry?.teacherEmail ?: teachers.firstOrNull()?.email ?: "")
    }
    var selectedRoomNumber by remember {
        mutableStateOf(initialEntry?.roomNumber ?: classrooms.firstOrNull()?.roomNumber ?: "")
    }

    val liveClashes = remember(
        selectedDay,
        selectedPeriod,
        selectedTeacherEmail,
        selectedRoomNumber,
        plan.entries,
    ) {
        RoutineGeneratorEngine.validateSlotEdit(
            allEntries = plan.entries,
            editingEntryId = entryId,
            targetShift = shift,
            targetDay = selectedDay,
            targetPeriod = selectedPeriod,
            targetDepartment = dept,
            targetSemester = sem,
            targetGroup = group,
            targetTeacherEmail = selectedTeacherEmail,
            targetRoomNumber = selectedRoomNumber,
        )
    }

    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val accent = Color(0xFF0284C7)

    MinimalFullScreenSheet(
        title = if (conflict != null) "Resolve Conflict Slot" else "Manual Slot Override",
        subtitle = "$subjectTitle ($subjectCode) • $dept ${RoutineRules.semesterShortLabel(sem)} $group • $shift",
        icon = CalendarDaysIcon,
        accent = accent,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        dismissEnabled = !isSaving,
        maxContentWidth = 820.dp,
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
                    enabled = !isSaving,
                    height = 46.dp,
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                )
                WelcomeMinimal3DButton(
                    text = if (liveClashes.isEmpty()) "Save Slot" else "Force Save",
                    icon = EditIcon,
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                    isLoading = isSaving,
                    height = 46.dp,
                    modifier = Modifier.weight(1.3f),
                    onClick = {
                        val teacherObj = teachers.firstOrNull { it.email.equals(selectedTeacherEmail, true) }
                        val roomObj = classrooms.firstOrNull { it.roomNumber.equals(selectedRoomNumber, true) }
                        val (st, et) = RoutineRules.periodTimeRange(shift, selectedPeriod, plan.config)
                        onSaveSlot(
                            RoutineEntry(
                                id = entryId,
                                day = selectedDay,
                                periodIndex = selectedPeriod,
                                startTime = st,
                                endTime = et,
                                shift = shift,
                                department = dept,
                                semester = sem,
                                group = group,
                                subjectCode = subjectCode,
                                subjectTitle = subjectTitle,
                                isPractical = initialEntry?.isPractical ?: (roomObj?.roomType == ClassroomType.LAB),
                                teacherEmail = teacherObj?.email ?: selectedTeacherEmail,
                                teacherName = teacherObj?.name ?: (initialEntry?.teacherName ?: selectedTeacherEmail),
                                roomId = roomObj?.id ?: (initialEntry?.roomId ?: ""),
                                roomNumber = roomObj?.roomNumber ?: selectedRoomNumber,
                                isManualOverride = true,
                            ),
                            conflict?.id,
                        )
                    },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (liveClashes.isEmpty()) {
                    Color(0xFF10B981).copy(alpha = 0.14f)
                } else {
                    Color(0xFFEF4444).copy(alpha = 0.14f)
                },
                border = BorderStroke(
                    1.dp,
                    if (liveClashes.isEmpty()) Color(0xFF10B981) else Color(0xFFEF4444),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (liveClashes.isEmpty()) {
                        Text(
                            text = "Clash Check: Clear (No overlaps detected)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = Color(0xFF059669),
                        )
                    } else {
                        Text(
                            text = "Overlap Warning:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = Color(0xFFEF4444),
                        )
                        liveClashes.forEach { msg ->
                            Text(
                                text = "• $msg",
                                fontSize = 12.sp,
                                fontFamily = hindSiliguri,
                                color = Color(0xFFEF4444),
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    text = "Working Day (Sun–Thu)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RoutineRules.WORKING_DAYS.forEach { day ->
                        FilterPill(day, selectedDay.equals(day, true), cardBg, borderCol, accent, textColor, hindSiliguri) {
                            selectedDay = day
                        }
                    }
                }
            }

            Column {
                Text(
                    text = "Period",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (p in 0 until plan.config.periodsPerDay.coerceIn(4, 10)) {
                        val (st, _) = RoutineRules.periodTimeRange(shift, p, plan.config)
                        FilterPill(
                            "P${p + 1} ($st)",
                            selectedPeriod == p,
                            cardBg,
                            borderCol,
                            Color(0xFF7C3AED),
                            textColor,
                            hindSiliguri,
                        ) {
                            selectedPeriod = p
                        }
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val twoCol = maxWidth >= 600.dp

                val roomPicker: @Composable (Modifier) -> Unit = { mod ->
                    Column(modifier = mod) {
                        Text(
                            text = "Classroom / Lab",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = textColor,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            classrooms.forEach { room ->
                                FilterPill(
                                    "${room.roomNumber} (${room.roomType.key.take(1)})",
                                    selectedRoomNumber.equals(room.roomNumber, true),
                                    cardBg,
                                    borderCol,
                                    Color(0xFF059669),
                                    textColor,
                                    hindSiliguri,
                                ) {
                                    selectedRoomNumber = room.roomNumber
                                }
                            }
                        }
                    }
                }

                val teacherPicker: @Composable (Modifier) -> Unit = { mod ->
                    Column(modifier = mod) {
                        Text(
                            text = "Assigned Teacher",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = textColor,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            teachers.forEach { t ->
                                FilterPill(
                                    t.name.ifBlank { t.email.substringBefore("@") },
                                    selectedTeacherEmail.equals(t.email, true),
                                    cardBg,
                                    borderCol,
                                    Color(0xFFD97706),
                                    textColor,
                                    hindSiliguri,
                                ) {
                                    selectedTeacherEmail = t.email
                                }
                            }
                        }
                    }
                }

                if (twoCol) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        roomPicker(Modifier.weight(1f))
                        teacherPicker(Modifier.weight(1f))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        roomPicker(Modifier.fillMaxWidth())
                        teacherPicker(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    active: Boolean,
    cardBg: Color,
    borderCol: Color,
    activeColor: Color,
    textColor: Color,
    hindSiliguri: FontFamily,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (active) activeColor else cardBg,
        border = BorderStroke(1.dp, if (active) activeColor else borderCol),
        modifier = Modifier.height(36.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = if (active) Color.White else textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RoutineConfigTimeField(
    label: String,
    value: String,
    preview: String,
    onValueChange: (String) -> Unit,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val bg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = hindSiliguri,
            color = subTextColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = bg,
            border = BorderStroke(1.dp, borderCol),
            modifier = Modifier.fillMaxWidth().height(42.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        color = textColor,
                    ),
                    cursorBrush = SolidColor(Color(0xFF0284C7)),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = preview,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = hindSiliguri,
                    color = Color(0xFF0284C7),
                )
            }
        }
    }
}

@Composable
private fun StepperChipCard(
    label: String,
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val bg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(1.dp, borderCol),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = subTextColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    onClick = onMinus,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color.White,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                }
                Text(
                    text = valueText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = textColor,
                )
                Surface(
                    onClick = onPlus,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color.White,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                }
            }
        }
    }
}
