package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.FilterIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.SwapIcon
import com.mpi.klass.theme.WarningIcon
import com.mpi.klass.theme.dayPaletteAt
import com.mpi.klass.theme.departmentAccent
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
    val cs = MaterialTheme.colorScheme
    val genAccent = KlassFeaturePalette.routine(isDark)
    val publishAccent = KlassFeaturePalette.success(isDark)
    val teacherAccent = KlassFeaturePalette.teaching(isDark)
    val bookAccent = KlassFeaturePalette.books(isDark)
    val adminAccent = KlassFeaturePalette.admin(isDark)
    val conflictAccent = KlassFeaturePalette.error(isDark)

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

    var showExplorerFilterSheet by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<RoutineEntry?>(null) }
    var resolvingConflict by remember { mutableStateOf<RoutineConflict?>(null) }
    var showScheduleSettings by remember { mutableStateOf(false) }
    var showConflictsScreen by remember { mutableStateOf(false) }
    var reviewingRequest by remember { mutableStateOf<RoutineEditRequest?>(null) }

    val hasAnySubScreen = editingSlot != null ||
        resolvingConflict != null ||
        showScheduleSettings ||
        showConflictsScreen ||
        showExplorerFilterSheet ||
        reviewingRequest != null

    LaunchedEffect(hasAnySubScreen) {
        onFullScreenOverlayChange(hasAnySubScreen)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    fun refreshAll() {
        scope.launch {
            isLoading = true
            try {
                books = RoleConfig.fetchLibraryBooks().ifEmpty { BtebBooksCatalog.books }
                teachers = fetchAllTeacherSchedulingPrefsFromFirestore()
                classrooms = fetchClassroomsFromFirestore()
                val draft = fetchRoutinePlanFromFirestore("master_6m")
                val pub = fetchRoutinePlanFromFirestore("master_6m_published")
                val loadedPlan = listOfNotNull(draft, pub).maxByOrNull {
                    maxOf(it.generatedAt, it.publishedAt)
                }
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
        MinimalBackdrop(
            isDark = isDark,
            focusAccent = when (activeTab) {
                0 -> genAccent.primary
                1 -> teacherAccent.primary
                else -> adminAccent.primary
            },
        ) {
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
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 980.dp),
                ) {
                    val tabs: List<Triple<Int, String, Pair<ImageVector, Color>>> = listOf(
                        Triple(0, "Generate", SparkleIcon to genAccent.primary),
                        Triple(1, "Explorer", CalendarDaysIcon to teacherAccent.primary),
                        Triple(
                            2,
                            if (pendingRequestsCount > 0) "Requests ($pendingRequestsCount)" else "Requests",
                            SwapIcon to adminAccent.primary,
                        ),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) cs.surfaceContainerLow.copy(alpha = 0.70f)
                                else Color.White.copy(alpha = 0.74f)
                            ),
                    ) {
                        tabs.forEach { (tabIdx, title, iconAndColor) ->
                            val (icon, tabAccent) = iconAndColor
                            val selected = activeTab == tabIdx
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeTab = tabIdx }
                                    .testTag("routine_tab_$tabIdx"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 13.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selected) tabAccent else cs.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 15.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontFamily = hindSiliguri,
                                        color = if (selected) tabAccent else cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(if (selected) tabAccent else Color.Transparent),
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = cs.outlineVariant.copy(alpha = 0.5f),
                    )

                    if (isLoading && currentPlan == null && books.isEmpty()) {
                        KlassRoutineEngineSkeleton(isDark = isDark)
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
                                contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
                            ) {
                                // Colored Readiness Badges + Refresh
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        LazyRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            item {
                                                MinimalStatBadge(
                                                    icon = PersonIcon,
                                                    text = "$readyTeachers Faculty",
                                                    accent = teacherAccent.primary,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                            item {
                                                MinimalStatBadge(
                                                    icon = DoorOpenIcon,
                                                    text = "${theoryRooms}T / ${labRooms}L Rooms",
                                                    accent = publishAccent.primary,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                            item {
                                                MinimalStatBadge(
                                                    icon = BookIcon,
                                                    text = "${books.size} Books",
                                                    accent = bookAccent.primary,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                )
                                            }
                                        }

                                        WelcomeMinimal3DIconButton(
                                            icon = RefreshIcon,
                                            contentDescription = "Refresh data",
                                            isDark = isDark,
                                            containerColor = genAccent.softTint,
                                            iconTint = genAccent.primary,
                                            baseColor = genAccent.borderTint,
                                            isLoading = isLoading,
                                            enabled = !isLoading,
                                            onClick = { refreshAll() },
                                        )
                                    }
                                    SocialSectionDividerBand(isDark = isDark, height = 8.dp)
                                }

                                // Schedule Settings Navigation Row -> Opens Level 1
                                item {
                                    SocialSurfaceBlock(
                                        isDark = isDark,
                                        showTopHairline = true,
                                        showBottomHairline = true,
                                    ) {
                                        KlassNavigationRow(
                                            title = "Schedule Settings",
                                            subtitle = "$shift1Start • $periodsPerDay periods • ${activeSemesters.size} semesters",
                                            icon = ClockIcon,
                                            accent = genAccent.primary,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            badgeText = "${periodDuration}m",
                                            testTag = "routine_schedule_settings_button",
                                            onClick = { showScheduleSettings = true },
                                        )
                                    }
                                    SocialSectionDividerBand(isDark = isDark)
                                }

                                // Calibrated 48.dp Generate & Publish 3D Buttons
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        WelcomeMinimal3DButton(
                                            text = "Generate Draft",
                                            icon = SparkleIcon,
                                            hindSiliguri = hindSiliguri,
                                            isDark = isDark,
                                            isLoading = isProcessing,
                                            customPrimaryColor = genAccent.primary,
                                            customBaseColor = genAccent.deep,
                                            height = 48.dp,
                                            cornerRadius = 16.dp,
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
                                            isDark = isDark,
                                            enabled = planToPub != null && !isProcessing,
                                            customPrimaryColor = publishAccent.primary,
                                            customBaseColor = publishAccent.deep,
                                            height = 48.dp,
                                            cornerRadius = 16.dp,
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
                                    SocialSectionDividerBand(isDark = isDark)
                                }

                                // Plan Status & Conflicts Sub-Screen Entry
                                val plan = currentPlan
                                if (plan != null) {
                                    val isLive = plan.status.equals("PUBLISHED", ignoreCase = true)
                                    val statusAccent = if (isLive) publishAccent else bookAccent
                                    item {
                                        SocialSurfaceBlock(
                                            isDark = isDark,
                                            showTopHairline = true,
                                            showBottomHairline = true,
                                            accentTint = statusAccent.primary,
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    ) {
                                                        Text(
                                                            text = "Current Plan",
                                                            fontSize = 17.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = hindSiliguri,
                                                            color = cs.onSurface,
                                                        )
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = statusAccent.softTint,
                                                            border = BorderStroke(0.75.dp, statusAccent.borderTint),
                                                        ) {
                                                            Text(
                                                                text = plan.status,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                fontFamily = hindSiliguri,
                                                                color = statusAccent.primary,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${plan.entries.size} scheduled slots across active shifts",
                                                        fontSize = 14.sp,
                                                        fontFamily = hindSiliguri,
                                                        color = cs.onSurfaceVariant,
                                                    )
                                                }
                                                KlassTonalButton(
                                                    text = "Explore",
                                                    icon = CalendarDaysIcon,
                                                    accent = teacherAccent.primary,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    height = 38.dp,
                                                    onClick = { activeTab = 1 },
                                                )
                                            }

                                            if (plan.conflicts.isNotEmpty()) {
                                                SocialInsetDivider(startIndent = 16.dp)
                                                KlassNavigationRow(
                                                    title = "Detected Conflicts",
                                                    subtitle = "Tap to resolve unscheduled subjects manually",
                                                    icon = WarningIcon,
                                                    accent = conflictAccent.primary,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    badgeText = "${plan.conflicts.size} remaining",
                                                    badgeColor = conflictAccent.primary,
                                                    testTag = "routine_conflicts_button",
                                                    onClick = { showConflictsScreen = true },
                                                )
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    KlassEmptyState(
                                        icon = CalendarDaysIcon,
                                        title = "No Routine Generated Yet",
                                        subtitle = "Create your 6-month draft in the Generate tab first.",
                                        accent = teacherAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        actionLabel = "Go to Generate",
                                        onActionClick = { activeTab = 0 },
                                    )
                                }
                                return@Column
                            }

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

                            val activeTeacherName = remember(teachers, filterTeacherEmail) {
                                teachers.firstOrNull { it.email.equals(filterTeacherEmail, true) }
                                    ?.name?.ifBlank { filterTeacherEmail.substringBefore("@") }
                                    ?: "Select Teacher"
                            }
                            val explorerSummaryText = when (explorerMode) {
                                0 -> "$filterDept • ${RoutineRules.semesterShortLabel(filterSem)} • $filterShift • $filterGroup"
                                1 -> "$activeTeacherName • $filterShift"
                                else -> "Room ${filterRoomNumber.ifBlank { "-" }} • $filterShift"
                            }
                            val explorerAccent = if (explorerMode == 0) departmentAccent(filterDept, isDark) else teacherAccent

                            Column(modifier = Modifier.fillMaxSize()) {
                                Surface(
                                    onClick = { showExplorerFilterSheet = true },
                                    color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.78f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("routine_explorer_filter_button"),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = explorerAccent.softTint,
                                            modifier = Modifier.size(34.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(
                                                    imageVector = FilterIcon,
                                                    contentDescription = "Filter routine",
                                                    tint = explorerAccent.primary,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                        Text(
                                            text = explorerSummaryText,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = explorerAccent.softTint,
                                            border = BorderStroke(0.75.dp, explorerAccent.borderTint),
                                        ) {
                                            Text(
                                                text = "${filteredEntries.size} slots",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = explorerAccent.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                            )
                                        }
                                        Icon(
                                            imageVector = ChevronDownIcon,
                                            contentDescription = null,
                                            tint = cs.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp),
                                        )
                                    }
                                }

                                SocialSectionDividerBand(isDark = isDark, height = 6.dp)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    ClassRoutineTable(
                                        hindSiliguri = hindSiliguri,
                                        isDark = isDark,
                                        entries = filteredEntries,
                                        periodsPerDay = plan.config.periodsPerDay,
                                        shift = filterShift,
                                        config = plan.config,
                                        showGroupOrRoomHint = explorerMode != 0,
                                        onSlotClick = { entry -> editingSlot = entry },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        // ─── TAB 2: TEACHER EDIT / SWAP REQUESTS ──────────────────
                        else -> {
                            if (editRequests.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    KlassEmptyState(
                                        icon = SwapIcon,
                                        title = "No Teacher Edit Requests",
                                        subtitle = "Teacher slot swap requests appear here with live clash checking.",
                                        accent = adminAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 10.dp),
                                ) {
                                    item {
                                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                            editRequests.forEachIndexed { idx, req ->
                                                val reqAccent = when (req.status) {
                                                    EditRequestStatus.PENDING -> bookAccent
                                                    EditRequestStatus.APPROVED -> publishAccent
                                                    EditRequestStatus.DECLINED -> conflictAccent
                                                }
                                                KlassNavigationRow(
                                                    title = "${req.requestedByName.ifBlank { req.requestedByEmail }} • ${req.subjectTitle}",
                                                    subtitle = "${req.originalDay} P${req.originalPeriod + 1} → ${req.requestedDay} P${req.requestedPeriod + 1} • ${req.department}",
                                                    icon = SwapIcon,
                                                    accent = reqAccent.primary,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    badgeText = req.status.label,
                                                    badgeColor = reqAccent.primary,
                                                    onClick = { reviewingRequest = req },
                                                )
                                                if (idx < editRequests.lastIndex) {
                                                    SocialInsetDivider(startIndent = 72.dp)
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

        // Level 1 Sub-Screen: Explorer Filter & Target Selector
        KlassSubScreenHost(visible = showExplorerFilterSheet, zIndex = 74f) {
            ExplorerFilterSubScreen(
                explorerMode = explorerMode,
                filterDept = filterDept,
                filterShift = filterShift,
                filterSem = filterSem,
                filterGroup = filterGroup,
                filterTeacherEmail = filterTeacherEmail,
                filterRoomNumber = filterRoomNumber,
                teachers = teachers,
                classrooms = classrooms,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onModeChange = { explorerMode = it },
                onShiftChange = { filterShift = it },
                onDeptChange = { d ->
                    filterDept = d
                    filterGroup = RoutineRules.normalizeGroup(d, filterGroup)
                },
                onSemChange = { filterSem = it },
                onGroupChange = { filterGroup = it },
                onTeacherChange = { filterTeacherEmail = it },
                onRoomChange = { filterRoomNumber = it },
                onDismiss = { showExplorerFilterSheet = false },
            )
        }

        // Level 1 Sub-Screen: Schedule Settings (+ Level 2 Period Timing Preview)
        KlassSubScreenHost(visible = showScheduleSettings, zIndex = 75f) {
            ScheduleSettingsSubScreen(
                shift1Start = shift1Start,
                shift2Start = shift2Start,
                periodDuration = periodDuration,
                periodsPerDay = periodsPerDay,
                activeSemesters = activeSemesters,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onShift1Change = { shift1Start = it },
                onShift2Change = { shift2Start = it },
                onDurationChange = { periodDuration = it },
                onPeriodsPerDayChange = { periodsPerDay = it },
                onToggleSemester = { sem ->
                    activeSemesters = if (sem in activeSemesters && activeSemesters.size > 1) {
                        activeSemesters - sem
                    } else {
                        activeSemesters + sem
                    }
                },
                onBack = { showScheduleSettings = false },
            )
        }

        // Level 1 Sub-Screen: Detected Conflicts
        KlassSubScreenHost(visible = showConflictsScreen && currentPlan != null, zIndex = 76f) {
            currentPlan?.let { planWithConflicts ->
                ConflictsSubScreen(
                    conflicts = planWithConflicts.conflicts,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onResolveClick = { conf -> resolvingConflict = conf },
                    onBack = { showConflictsScreen = false },
                )
            }
        }

        // Level 1 Sub-Screen: Request Review + Live Clash Check + Approve/Decline
        KlassSubScreenHost(visible = reviewingRequest != null, zIndex = 78f) {
            reviewingRequest?.let { req ->
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
                RequestReviewSubScreen(
                    req = req,
                    clashes = clashes,
                    isProcessing = isProcessing,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onApprove = {
                        scope.launch {
                            isProcessing = true
                            try {
                                val plan = currentPlan
                                if (plan == null) {
                                    showToast("No active routine loaded.")
                                    return@launch
                                }
                                val targetExists = plan.entries.any { it.id == req.targetEntryId }
                                if (!targetExists) {
                                    showToast("Target slot no longer exists in current routine.")
                                    return@launch
                                }
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
                                }.sortedWith(
                                    compareBy<RoutineEntry> { RoutineRules.dayOrderIndex(it.day) }
                                        .thenBy { it.periodIndex }
                                )
                                val nowTs = getCurrentTimeMillis()
                                val updatedPlan = plan.copy(
                                    entries = updatedEntries,
                                    publishedAt = nowTs,
                                )
                                saveRoutinePlanInFirestore(updatedPlan.copy(id = "master_6m"))
                                if (updatedPlan.status == "PUBLISHED") {
                                    saveRoutinePlanInFirestore(
                                        updatedPlan.copy(id = "master_6m_published")
                                    )
                                    onPlanPublished(updatedPlan)
                                }
                                currentPlan = updatedPlan
                                resolveRoutineEditRequestInFirestore(
                                    requestId = req.id,
                                    newStatus = EditRequestStatus.APPROVED,
                                    adminNote = "Approved by Admin",
                                )
                                reviewingRequest = null
                                refreshAll()
                                showToast("Edit request approved & routine updated.")
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    onDecline = {
                        scope.launch {
                            isProcessing = true
                            try {
                                resolveRoutineEditRequestInFirestore(
                                    requestId = req.id,
                                    newStatus = EditRequestStatus.DECLINED,
                                    adminNote = "Declined by Admin",
                                )
                                reviewingRequest = null
                                refreshAll()
                                showToast("Edit request declined.")
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    onDismiss = { if (!isProcessing) reviewingRequest = null },
                )
            }
        }

        // Level 2 Sub-Sub-Screen: Manual Slot Override / Conflict Resolver (+ Level 3 Room & Teacher Pickers)
        val activeEdit = editingSlot
        val activeConf = resolvingConflict
        val planForSlot = currentPlan
        KlassSubScreenHost(
            visible = planForSlot != null && (activeEdit != null || activeConf != null),
            zIndex = 82f,
        ) {
            if (planForSlot != null && (activeEdit != null || activeConf != null)) {
                ManualSlotEditorOverlay(
                    plan = planForSlot,
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
                                val existingIdx = planForSlot.entries.indexOfFirst { it.id == updatedEntry.id }
                                val updatedEntries = if (existingIdx >= 0) {
                                    planForSlot.entries.toMutableList().apply { set(existingIdx, updatedEntry) }
                                } else {
                                    planForSlot.entries + updatedEntry
                                }.sortedWith(
                                    compareBy<RoutineEntry> { RoutineRules.dayOrderIndex(it.day) }
                                        .thenBy { it.periodIndex }
                                )
                                val updatedConflicts = if (removeConflictId != null) {
                                    planForSlot.conflicts.filterNot { it.id == removeConflictId }
                                } else {
                                    planForSlot.conflicts
                                }
                                val nowTs = getCurrentTimeMillis()
                                val updatedPlan = planForSlot.copy(
                                    entries = updatedEntries,
                                    conflicts = updatedConflicts,
                                    publishedAt = nowTs,
                                )
                                saveRoutinePlanInFirestore(updatedPlan.copy(id = "master_6m"))
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
private fun ExplorerFilterSubScreen(
    explorerMode: Int,
    filterDept: String,
    filterShift: String,
    filterSem: String,
    filterGroup: String,
    filterTeacherEmail: String,
    filterRoomNumber: String,
    teachers: List<TeacherSchedulingPref>,
    classrooms: List<ClassroomItem>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onModeChange: (Int) -> Unit,
    onShiftChange: (String) -> Unit,
    onDeptChange: (String) -> Unit,
    onSemChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onTeacherChange: (String) -> Unit,
    onRoomChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val teacherAccent = KlassFeaturePalette.teaching(isDark)
    val roomAccent = KlassFeaturePalette.classrooms(isDark)
    val deptAccent = departmentAccent(filterDept, isDark)

    var showTeacherPicker by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = "Explore Routine",
            subtitle = "Switch between Group, Teacher or Room view",
            levelLabel = "Routine Explorer • Level 1",
            icon = FilterIcon,
            accent = teacherAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            headerActions = {
                HeaderSavePillButton(
                    text = "Done",
                    accent = teacherAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onClick = onDismiss,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        0 to ("By Group" to deptAccent.primary),
                        1 to ("By Teacher" to teacherAccent.primary),
                        2 to ("By Room" to roomAccent.primary),
                    ).forEach { (m, labelAndColor) ->
                        val (label, col) = labelAndColor
                        MinimalIconChip(
                            label = label,
                            selected = explorerMode == m,
                            accent = col,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                            onClick = { onModeChange(m) },
                        )
                    }
                }

                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Shift",
                        hindSiliguri = hindSiliguri,
                        accent = teacherAccent.primary,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2).forEach { sh ->
                            MinimalIconChip(
                                label = sh,
                                selected = filterShift == sh,
                                accent = teacherAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
                                onClick = { onShiftChange(sh) },
                            )
                        }
                    }
                }

                when (explorerMode) {
                    0 -> {
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            GoogleSectionHeader(
                                title = "Department",
                                hindSiliguri = hindSiliguri,
                                accent = deptAccent.primary,
                            )
                            FlowRow(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RoutineRules.TECH_DEPARTMENTS.forEach { d ->
                                    MinimalIconChip(
                                        label = d,
                                        selected = filterDept == d,
                                        accent = departmentAccent(d, isDark).primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = { onDeptChange(d) },
                                    )
                                }
                            }

                            SocialInsetDivider(startIndent = 16.dp)

                            GoogleSectionHeader(
                                title = "Semester",
                                hindSiliguri = hindSiliguri,
                                accent = deptAccent.primary,
                            )
                            FlowRow(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RoutineRules.ALL_SEMESTER_KEYS.forEachIndexed { sIdx, s ->
                                    MinimalIconChip(
                                        label = RoutineRules.semesterShortLabel(s),
                                        selected = filterSem == s,
                                        accent = dayPaletteAt(sIdx, isDark).primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = { onSemChange(s) },
                                    )
                                }
                            }

                            SocialInsetDivider(startIndent = 16.dp)

                            GoogleSectionHeader(
                                title = "Group",
                                hindSiliguri = hindSiliguri,
                                accent = deptAccent.primary,
                            )
                            FlowRow(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RoutineRules.groupsForDepartment(filterDept).forEach { g ->
                                    MinimalIconChip(
                                        label = g,
                                        selected = filterGroup == g,
                                        accent = deptAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = { onGroupChange(g) },
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        val currentTeacher = teachers.firstOrNull { it.email.equals(filterTeacherEmail, true) }
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            KlassNavigationRow(
                                title = "Select Teacher",
                                subtitle = currentTeacher?.name?.ifBlank { currentTeacher.email } ?: "Tap to choose faculty",
                                icon = PersonIcon,
                                accent = teacherAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                badgeText = currentTeacher?.department?.ifBlank { null },
                                onClick = { showTeacherPicker = true },
                            )
                        }
                    }
                    else -> {
                        val currentRoom = classrooms.firstOrNull { it.roomNumber.equals(filterRoomNumber, true) }
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            KlassNavigationRow(
                                title = "Select Classroom / Lab",
                                subtitle = if (filterRoomNumber.isBlank()) "Tap to pick room" else "Room $filterRoomNumber",
                                icon = DoorOpenIcon,
                                accent = roomAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                badgeText = currentRoom?.roomType?.label,
                                onClick = { showRoomPicker = true },
                            )
                        }
                    }
                }
            }
        }

        // Level 2: Teacher Picker
        KlassSubScreenHost(visible = showTeacherPicker, zIndex = 88f) {
            KlassSelectionPickerSubScreen(
                title = "Choose Teacher",
                subtitle = "${teachers.size} faculty configured",
                levelLabel = "Explorer Filter • Level 2",
                icon = PersonIcon,
                accent = teacherAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = teachers,
                selectedOption = teachers.firstOrNull { it.email.equals(filterTeacherEmail, true) },
                optionLabel = { it.name.ifBlank { it.email.substringBefore("@") } },
                optionSubtitle = { "${it.department} • ${it.shift}" },
                onSelect = { onTeacherChange(it.email) },
                onDismiss = { showTeacherPicker = false },
            )
        }

        // Level 2: Classroom Picker
        KlassSubScreenHost(visible = showRoomPicker, zIndex = 88f) {
            KlassSelectionPickerSubScreen(
                title = "Choose Classroom / Lab",
                subtitle = "${classrooms.size} rooms available",
                levelLabel = "Explorer Filter • Level 2",
                icon = DoorOpenIcon,
                accent = roomAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = classrooms,
                selectedOption = classrooms.firstOrNull { it.roomNumber.equals(filterRoomNumber, true) },
                optionLabel = { "Room ${it.roomNumber}" },
                optionSubtitle = { "${it.roomType.label} • ${it.department} • Cap ${it.capacity}" },
                onSelect = { onRoomChange(it.roomNumber) },
                onDismiss = { showRoomPicker = false },
            )
        }
    }
}

@Composable
private fun RequestReviewSubScreen(
    req: RoutineEditRequest,
    clashes: List<String>,
    isProcessing: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit,
) {
    val approveAccent = KlassFeaturePalette.success(isDark)
    val declineAccent = KlassFeaturePalette.error(isDark)
    val adminAccent = KlassFeaturePalette.admin(isDark)
    val deptAccent = departmentAccent(req.department, isDark)
    val cs = MaterialTheme.colorScheme

    MinimalFullScreenSheet(
        title = req.subjectTitle,
        subtitle = "Requested by ${req.requestedByName.ifBlank { req.requestedByEmail }}",
        levelLabel = "Edit Request • Level 1",
        icon = SwapIcon,
        accent = adminAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        dismissEnabled = !isProcessing,
        headerActions = {
            if (req.status == EditRequestStatus.PENDING) {
                KlassTonalButton(
                    text = "Decline",
                    accent = declineAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    enabled = !isProcessing,
                    height = 36.dp,
                    onClick = onDecline,
                )
                HeaderSavePillButton(
                    text = if (isProcessing) "..." else "Approve",
                    accent = approveAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    isLoading = isProcessing,
                    enabled = !isProcessing,
                    onClick = onApprove,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GoogleSectionHeader(
                title = "${req.department} • ${RoutineRules.semesterDisplayLabel(req.semester)} • ${req.group} • ${req.status.label}",
                accent = adminAccent.primary,
                hindSiliguri = hindSiliguri,
            )

            SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                KlassDetailFieldRow(
                    label = "Current Slot",
                    value = "${req.originalDay} • Period ${req.originalPeriod + 1} • Room ${req.originalRoomNumber}",
                    icon = ClockIcon,
                    accent = KlassFeaturePalette.teachers(isDark).primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
                SocialInsetDivider(startIndent = 68.dp)
                KlassDetailFieldRow(
                    label = "Requested New Slot",
                    value = "${req.requestedDay} • Period ${req.requestedPeriod + 1} • Room ${req.requestedRoomNumber}",
                    icon = CalendarDaysIcon,
                    accent = deptAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
                if (req.reason.isNotBlank()) {
                    SocialInsetDivider(startIndent = 68.dp)
                    KlassDetailFieldRow(
                        label = "Teacher's Reason",
                        value = req.reason,
                        icon = InfoCircleIcon,
                        accent = adminAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }
            }

            if (req.status == EditRequestStatus.PENDING) {
                val clashAccent = if (clashes.isEmpty()) approveAccent else declineAccent
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = clashAccent.softTint,
                        border = BorderStroke(1.dp, clashAccent.borderTint),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (clashes.isEmpty()) "Clash Check: Clear (Safe to Approve)" else "Detected Clashes (${clashes.size}):",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = clashAccent.primary,
                            )
                            clashes.forEach { msg ->
                                Text(
                                    text = "• $msg",
                                    fontSize = 14.sp,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurface,
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
    val cs = MaterialTheme.colorScheme
    val genAccent = KlassFeaturePalette.routine(isDark)
    val publishAccent = KlassFeaturePalette.success(isDark)
    val conflictAccent = KlassFeaturePalette.error(isDark)
    val teacherAccent = KlassFeaturePalette.teaching(isDark)
    val roomAccent = KlassFeaturePalette.classrooms(isDark)

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

    var showRoomPicker by remember { mutableStateOf(false) }
    var showTeacherPicker by remember { mutableStateOf(false) }

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

    val statusAccent = if (liveClashes.isEmpty()) publishAccent else conflictAccent

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = if (conflict != null) "Resolve Conflict Slot" else "Manual Slot Override",
            subtitle = "$subjectTitle ($subjectCode) • $dept ${RoutineRules.semesterShortLabel(sem)} $group",
            levelLabel = "Slot Editor • Level 2",
            icon = CalendarDaysIcon,
            accent = if (liveClashes.isEmpty()) genAccent.primary else conflictAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            dismissEnabled = !isSaving,
            maxContentWidth = 820.dp,
            headerActions = {
                HeaderSavePillButton(
                    text = if (liveClashes.isEmpty()) "Save" else "Force Save",
                    accent = statusAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    isLoading = isSaving,
                    enabled = !isSaving,
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
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = statusAccent.softTint,
                        border = BorderStroke(1.dp, statusAccent.borderTint),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (liveClashes.isEmpty()) "Clash Check: Clear (No overlaps detected)" else "Overlap Warning (${liveClashes.size}):",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = statusAccent.primary,
                            )
                            liveClashes.forEach { msg ->
                                Text(
                                    text = "• $msg",
                                    fontSize = 13.5.sp,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurface,
                                )
                            }
                        }
                    }
                }

                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Working Day (Sun–Thu)",
                        hindSiliguri = hindSiliguri,
                        accent = genAccent.primary,
                    )
                    LazyRow(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(RoutineRules.WORKING_DAYS) { dIdx, day ->
                            MinimalIconChip(
                                label = day,
                                selected = selectedDay.equals(day, true),
                                accent = dayPaletteAt(dIdx, isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { selectedDay = day },
                            )
                        }
                    }

                    SocialInsetDivider(startIndent = 16.dp)

                    GoogleSectionHeader(
                        title = "Period",
                        hindSiliguri = hindSiliguri,
                        accent = genAccent.primary,
                    )
                    LazyRow(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val periodCount = plan.config.periodsPerDay.coerceIn(4, 10)
                        items(periodCount) { p ->
                            val (st, _) = RoutineRules.periodTimeRange(shift, p, plan.config)
                            MinimalIconChip(
                                label = "P${p + 1} ($st)",
                                selected = selectedPeriod == p,
                                accent = genAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { selectedPeriod = p },
                            )
                        }
                    }
                }

                // Clean Sub-Screen Pickers for Classroom & Teacher (no chip overload)
                val currentRoom = classrooms.firstOrNull { it.roomNumber.equals(selectedRoomNumber, true) }
                val currentTeacher = teachers.firstOrNull { it.email.equals(selectedTeacherEmail, true) }

                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Room & Instructor Assignment",
                        hindSiliguri = hindSiliguri,
                        accent = teacherAccent.primary,
                    )
                    KlassNavigationRow(
                        title = "Classroom / Lab",
                        subtitle = if (selectedRoomNumber.isBlank()) "Tap to pick a room" else "Room $selectedRoomNumber",
                        icon = DoorOpenIcon,
                        accent = roomAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badgeText = currentRoom?.roomType?.label,
                        onClick = { showRoomPicker = true },
                    )
                    SocialInsetDivider(startIndent = 72.dp)
                    KlassNavigationRow(
                        title = "Assigned Teacher",
                        subtitle = currentTeacher?.name?.ifBlank { currentTeacher.email } ?: selectedTeacherEmail.ifBlank { "Tap to pick faculty" },
                        icon = PersonIcon,
                        accent = teacherAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badgeText = currentTeacher?.department?.ifBlank { null },
                        onClick = { showTeacherPicker = true },
                    )
                }
            }
        }

        // Level 3: Room Picker with Busy/Free Hint
        KlassSubScreenHost(visible = showRoomPicker, zIndex = 92f) {
            KlassSelectionPickerSubScreen(
                title = "Pick Classroom / Lab",
                subtitle = "$selectedDay • Period ${selectedPeriod + 1}",
                levelLabel = "Slot Editor • Level 3",
                icon = DoorOpenIcon,
                accent = roomAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = classrooms,
                selectedOption = classrooms.firstOrNull { it.roomNumber.equals(selectedRoomNumber, true) },
                optionLabel = { "Room ${it.roomNumber} (${it.roomType.label})" },
                optionSubtitle = { room ->
                    val busy = plan.entries.any {
                        it.id != entryId &&
                            RoutineRules.normalizeShift(it.shift) == RoutineRules.normalizeShift(shift) &&
                            it.day.equals(selectedDay, true) &&
                            it.periodIndex == selectedPeriod &&
                            it.roomNumber.equals(room.roomNumber, true)
                    }
                    if (busy) "Occupied at P${selectedPeriod + 1} (${room.department})" else "Available • ${room.department}"
                },
                onSelect = { selectedRoomNumber = it.roomNumber },
                onDismiss = { showRoomPicker = false },
            )
        }

        // Level 3: Teacher Picker with Busy/Free Hint
        KlassSubScreenHost(visible = showTeacherPicker, zIndex = 92f) {
            KlassSelectionPickerSubScreen(
                title = "Pick Assigned Teacher",
                subtitle = "$selectedDay • Period ${selectedPeriod + 1}",
                levelLabel = "Slot Editor • Level 3",
                icon = PersonIcon,
                accent = teacherAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = teachers,
                selectedOption = teachers.firstOrNull { it.email.equals(selectedTeacherEmail, true) },
                optionLabel = { it.name.ifBlank { it.email.substringBefore("@") } },
                optionSubtitle = { t ->
                    val busy = plan.entries.any {
                        it.id != entryId &&
                            RoutineRules.normalizeShift(it.shift) == RoutineRules.normalizeShift(shift) &&
                            it.day.equals(selectedDay, true) &&
                            it.periodIndex == selectedPeriod &&
                            it.teacherEmail.equals(t.email, true)
                    }
                    if (busy) "Teaching another class at P${selectedPeriod + 1}" else "Available • ${t.department}"
                },
                onSelect = { selectedTeacherEmail = it.email },
                onDismiss = { showTeacherPicker = false },
            )
        }
    }
}

@Composable
private fun RoutineConfigTimeField(
    label: String,
    value: String,
    preview: String,
    accent: Color,
    onValueChange: (String) -> Unit,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = {
            Text(
                text = label,
                fontFamily = hindSiliguri,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingIcon = {
            Text(
                text = preview,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = accent,
                modifier = Modifier.padding(end = 10.dp),
            )
        },
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = hindSiliguri,
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = cs.outlineVariant,
            focusedLabelColor = accent,
            unfocusedLabelColor = cs.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun StepperChipCard(
    label: String,
    valueText: String,
    accent: Color,
    isDark: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = if (isDark) 0.14f else 0.08f),
        border = BorderStroke(0.75.dp, accent.copy(alpha = 0.28f)),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = cs.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    onClick = onMinus,
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.26f else 0.16f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "−",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                    }
                }
                Text(
                    text = valueText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = cs.onSurface,
                )
                Surface(
                    onClick = onPlus,
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.26f else 0.16f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleSettingsSubScreen(
    shift1Start: String,
    shift2Start: String,
    periodDuration: Int,
    periodsPerDay: Int,
    activeSemesters: Set<String>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onShift1Change: (String) -> Unit,
    onShift2Change: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onPeriodsPerDayChange: (Int) -> Unit,
    onToggleSemester: (String) -> Unit,
    onBack: () -> Unit,
) {
    val genAccent = KlassFeaturePalette.routine(isDark)
    val bookAccent = KlassFeaturePalette.books(isDark)
    var showTimingPreview by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = "Schedule Settings",
            subtitle = "Shift timings & active semesters",
            levelLabel = "Routine Engine • Level 1",
            icon = ClockIcon,
            accent = genAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onBack,
            closeTestTag = "schedule_settings_back_button",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Shift Timings & Periods",
                        hindSiliguri = hindSiliguri,
                        accent = genAccent.primary,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
                                accent = genAccent.primary,
                                onValueChange = onShift1Change,
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
                            )
                            RoutineConfigTimeField(
                                label = "2nd Shift (24h)",
                                value = shift2Start,
                                preview = RoutineRules.formatMinutesToTime(
                                    RoutineRules.parseMinutesFromHhMm(shift2Start, 13 * 60 + 30)
                                ),
                                accent = genAccent.primary,
                                onValueChange = onShift2Change,
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
                                accent = genAccent.primary,
                                isDark = isDark,
                                onMinus = { if (periodDuration > 30) onDurationChange(periodDuration - 5) },
                                onPlus = { if (periodDuration < 75) onDurationChange(periodDuration + 5) },
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
                            )
                            StepperChipCard(
                                label = "Periods / Day",
                                valueText = "$periodsPerDay",
                                accent = bookAccent.primary,
                                isDark = isDark,
                                onMinus = { if (periodsPerDay > 4) onPeriodsPerDayChange(periodsPerDay - 1) },
                                onPlus = { if (periodsPerDay < 10) onPeriodsPerDayChange(periodsPerDay + 1) },
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    SocialInsetDivider(startIndent = 16.dp)
                    KlassNavigationRow(
                        title = "Preview Period Timings",
                        subtitle = "View calculated start and end times for all $periodsPerDay periods",
                        icon = LayerGroupIcon,
                        accent = genAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badgeText = "P1–P$periodsPerDay",
                        onClick = { showTimingPreview = true },
                    )
                }

                SocialSectionDividerBand(isDark = isDark)

                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Active Semesters (Sun–Thu)",
                        hindSiliguri = hindSiliguri,
                        accent = bookAccent.primary,
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RoutineRules.ALL_SEMESTER_KEYS.forEachIndexed { sIdx, sem ->
                            val selected = sem in activeSemesters
                            MinimalIconChip(
                                label = RoutineRules.semesterShortLabel(sem),
                                selected = selected,
                                accent = dayPaletteAt(sIdx, isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { onToggleSemester(sem) },
                            )
                        }
                    }
                }
            }
        }

        // Level 2 Sub-Sub-Screen: Period Timings Preview
        KlassSubScreenHost(visible = showTimingPreview, zIndex = 88f) {
            val previewConfig = RoutineConfig(
                shift1StartTime = shift1Start.trim().ifBlank { "08:00" },
                shift2StartTime = shift2Start.trim().ifBlank { "13:30" },
                periodDurationMinutes = periodDuration,
                periodsPerDay = periodsPerDay,
            )
            MinimalFullScreenSheet(
                title = "Period Timings Preview",
                subtitle = "$periodDuration min per period • $periodsPerDay periods/day",
                levelLabel = "Schedule Settings • Level 2",
                icon = ClockIcon,
                accent = genAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = { showTimingPreview = false },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    listOf(
                        RoutineRules.SHIFT_1 to genAccent.primary,
                        RoutineRules.SHIFT_2 to KlassFeaturePalette.teaching(isDark).primary,
                    ).forEach { (sh, accent) ->
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            GoogleSectionHeader(title = sh, hindSiliguri = hindSiliguri, accent = accent)
                            for (p in 0 until periodsPerDay.coerceIn(4, 10)) {
                                val (st, et) = RoutineRules.periodTimeRange(sh, p, previewConfig)
                                KlassDetailFieldRow(
                                    label = "Period ${p + 1}",
                                    value = "$st – $et",
                                    icon = ClockIcon,
                                    accent = accent,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                )
                                if (p < periodsPerDay - 1) {
                                    SocialInsetDivider(startIndent = 68.dp)
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
private fun ConflictsSubScreen(
    conflicts: List<RoutineConflict>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onResolveClick: (RoutineConflict) -> Unit,
    onBack: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val conflictAccent = KlassFeaturePalette.error(isDark)
    val successAccent = KlassFeaturePalette.success(isDark)

    MinimalFullScreenSheet(
        title = "Detected Conflicts",
        subtitle = "${conflicts.size} remaining",
        levelLabel = "Routine Conflicts • Level 1",
        icon = WarningIcon,
        accent = conflictAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onBack,
        closeTestTag = "conflicts_back_button",
    ) {
        if (conflicts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                KlassEmptyState(
                    icon = CheckIcon,
                    title = "All Conflicts Resolved!",
                    subtitle = "Your routine has zero scheduling overlaps.",
                    accent = successAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                item {
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        conflicts.forEachIndexed { idx, conf ->
                            val deptAccent = departmentAccent(conf.department, isDark)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onResolveClick(conf) }
                                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = conflictAccent.softTint,
                                    border = BorderStroke(0.75.dp, conflictAccent.borderTint),
                                    modifier = Modifier.size(42.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = WarningIcon,
                                            contentDescription = null,
                                            tint = conflictAccent.primary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${conf.department} • ${RoutineRules.semesterShortLabel(conf.semester)} • ${conf.group}",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = deptAccent.primary,
                                    )
                                    Text(
                                        text = "${conf.subjectTitle} (${conf.subjectCode})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                    )
                                    Text(
                                        text = conf.reason,
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                    )
                                }
                                WelcomeMinimal3DIconButton(
                                    icon = PlusIcon,
                                    contentDescription = "Assign slot manually",
                                    isDark = isDark,
                                    containerColor = conflictAccent.softTint,
                                    iconTint = conflictAccent.primary,
                                    baseColor = conflictAccent.borderTint,
                                    visualSize = 38.dp,
                                    iconSize = 17.dp,
                                    onClick = { onResolveClick(conf) },
                                )
                            }
                            if (idx < conflicts.lastIndex) {
                                SocialInsetDivider(startIndent = 68.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
