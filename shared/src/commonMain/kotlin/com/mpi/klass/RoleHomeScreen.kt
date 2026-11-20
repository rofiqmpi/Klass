package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.RightArrowIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.departmentAccent
import com.mpi.klass.theme.getHindSiliguriFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun RoleHomeScreen(
    role: UserRole = UserRole.STUDENT,
    userName: String,
    photoUrl: String? = null,
    email: String = "",
    currentThemeMode: ThemeMode = ThemeMode.DEFAULT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onSignOut: () -> Unit = {},
    onCloseApp: () -> Unit = {},
    onBackToWelcome: () -> Unit = {},
    onProfileUpdated: ((newName: String, newPhotoUrl: String?) -> Unit)? = null,
    initialTab: String = "home",
) {
    var currentUserName by remember(userName) { mutableStateOf(userName) }
    var currentPhotoUrl by remember(photoUrl) { mutableStateOf(photoUrl) }
    var currentRole by remember(role) { mutableStateOf(role) }

    var studentProfile by remember(email) {
        mutableStateOf(if (email.isNotBlank()) fetchLocalStudentProfile(email.trim().lowercase()) else null)
    }
    var teacherPref by remember(email) { mutableStateOf<TeacherSchedulingPref?>(null) }
    var teacherOnboardingDismissed by remember(email) { mutableStateOf(false) }
    var publishedPlan by remember { mutableStateOf<RoutinePlan?>(null) }
    var classrooms by remember { mutableStateOf<List<ClassroomItem>>(emptyList()) }

    // Selected slot modal (for Teacher Edit Request or Details)
    var inspectedEntry by remember { mutableStateOf<RoutineEntry?>(null) }
    var tempRoutineFilter by remember { mutableStateOf<RoutineQuickFilter?>(null) }
    var isRoutineFilterExpanded by remember { mutableStateOf(false) }
    var isDownloadingRoutine by remember { mutableStateOf(false) }

    val autoLocationSyncLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank() && email.isNotBlank() && currentRole == UserRole.STUDENT) {
            studentProfile = studentProfile?.copy(address = realAddress)
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                RoleConfig.saveStudentAddress(email.trim().lowercase(), realAddress)
            }
        }
    }

    val systemInDark = isSystemInDarkTheme()
    val isDark = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    val currentLang = LocalAppLanguage.current
    val hindSiliguri = getHindSiliguriFontFamily()
    val tabBackStack = remember { mutableStateListOf(initialTab) }
    val selectedTab = tabBackStack.lastOrNull() ?: "home"
    var lastBackPressTime by remember { mutableStateOf(0L) }
    var isTeachersSubOverlayOpen by remember { mutableStateOf(false) }
    var isBooksSubOverlayOpen by remember { mutableStateOf(false) }
    var isClassroomSubOverlayOpen by remember { mutableStateOf(false) }
    var isRoutineSubOverlayOpen by remember { mutableStateOf(false) }
    var isProfileSubOverlayOpen by remember { mutableStateOf(false) }
    var isNotificationsSubOverlayOpen by remember { mutableStateOf(false) }
    var isBloodFinderSubOverlayOpen by remember { mutableStateOf(false) }
    var isTeacherPrefsSubOverlayOpen by remember { mutableStateOf(false) }

    fun navigateTo(tab: String) {
        when (tab) {
            "home" -> {
                tabBackStack.clear()
                tabBackStack.add("home")
            }
            "menu" -> {
                tabBackStack.clear()
                tabBackStack.add("home")
                tabBackStack.add("menu")
            }
            else -> {
                if (selectedTab != tab) {
                    tabBackStack.add(tab)
                }
            }
        }
    }

    fun goBack() {
        if (tabBackStack.size > 1) {
            tabBackStack.removeAt(tabBackStack.lastIndex)
        } else {
            val now = getCurrentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                onCloseApp()
            } else {
                lastBackPressTime = now
                showToast("Press back again to exit".localized(currentLang))
            }
        }
    }

    LaunchedEffect(email) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isNotBlank()) {
            val prof = RoleConfig.fetchUserProfile(cleanEmail)
            if (prof != null) {
                UserRole.fromRaw(prof.category)?.let { currentRole = it }
                val fetchedPhoto = prof.photoUrl
                if (!fetchedPhoto.isNullOrBlank() && (currentPhotoUrl.isNullOrBlank() || fetchedPhoto.startsWith("data:image"))) {
                    currentPhotoUrl = fetchedPhoto
                }
                if (currentUserName.isBlank() && prof.name.isNotBlank()) {
                    currentUserName = prof.name
                }
            }
            val loadedStudentProfile = RoleConfig.fetchStudentProfile(cleanEmail)
            studentProfile = loadedStudentProfile
            if (currentRole == UserRole.TEACHER) {
                val loadedPref = fetchTeacherSchedulingPrefFromFirestore(cleanEmail)
                teacherPref = loadedPref
                if ((loadedPref == null || !loadedPref.schedulingSetupComplete) && !teacherOnboardingDismissed) {
                    navigateTo("teacher_prefs")
                }
            }
            if (currentRole == UserRole.STUDENT && loadedStudentProfile?.address.isNullOrBlank()) {
                autoLocationSyncLauncher()
            }
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                RoleConfig.seedBtebLibraryBooks(forceReseed = false)
            }
        }
        publishedPlan = fetchRoutinePlanFromFirestore("master_6m_published")
        classrooms = fetchClassroomsFromFirestore()
    }

    LaunchedEffect(publishedPlan, currentRole, studentProfile, email) {
        val plan = publishedPlan ?: return@LaunchedEffect
        val userEntries = PushScheduleMath.filterUserEntriesForAlerts(
            plan = plan,
            role = currentRole,
            email = email,
            studentProfile = studentProfile,
        )
        val prefs = getNotificationPrefs()
        if (prefs.enabled && userEntries.isNotEmpty()) {
            syncAutomaticClassPushNotifications(
                entries = userEntries,
                config = plan.config,
                role = currentRole,
                prefs = prefs,
            )
        } else {
            cancelAllClassPushNotifications()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.86f) else Color.White.copy(alpha = 0.86f)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val isGesture = remember { isGestureNavigation() }
    val exactCornerRadiusDp = remember { getDisplayBottomCornerRadiusDp() }
    val bottomRadius = if (isGesture) exactCornerRadiusDp.coerceIn(30f, 43f).dp else 0.dp

    val homeShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = bottomRadius,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
    )

    val menuShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = bottomRadius,
    )

    val activeBaseTab = tabBackStack.lastOrNull { it == "home" || it == "menu" } ?: "home"
    val overlayTab = if (selectedTab == "home" || selectedTab == "menu") null else selectedTab

    val pagerState = rememberPagerState(
        initialPage = if (initialTab == "menu") 1 else 0,
        pageCount = { 2 },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val pageTab = if (page == 0) "home" else "menu"
                val current = tabBackStack.lastOrNull() ?: "home"
                if ((current == "home" || current == "menu") && current != pageTab) {
                    if (pageTab == "home") {
                        tabBackStack.clear()
                        tabBackStack.add("home")
                    } else {
                        tabBackStack.clear()
                        tabBackStack.add("home")
                        tabBackStack.add("menu")
                    }
                }
            }
    }

    LaunchedEffect(activeBaseTab) {
        val targetPage = if (activeBaseTab == "menu") 1 else 0
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    AppBackHandler(enabled = true) {
        goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .rainbowScreenBackground(isDark),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = overlayTab == null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { page ->
                    when (page) {
                        0 -> {
                            // Compute role-filtered routine entries
                            val plan = publishedPlan
                            val allEntries = plan?.entries ?: emptyList()
                            val activeConfig = plan?.config ?: RoutineConfig()
                            val cleanEmail = email.trim().lowercase()

                            val homeFiltered = remember(
                                allEntries,
                                currentRole,
                                studentProfile,
                                cleanEmail,
                            ) {
                                when (currentRole) {
                                    UserRole.STUDENT -> {
                                        val sp = studentProfile
                                        if (sp == null || sp.department.isBlank() || sp.semester.isBlank()) {
                                            emptyList()
                                        } else {
                                            val dept = RoutineRules.normalizeDepartmentShort(sp.department)
                                            val shift = RoutineRules.normalizeShift(sp.shift)
                                            val sem = RoutineRules.canonicalSemesterKey(sp.semester)
                                            val grp = RoutineRules.normalizeGroup(dept, sp.section)
                                            allEntries.filter {
                                                it.department == dept &&
                                                    RoutineRules.normalizeShift(it.shift) == shift &&
                                                    RoutineRules.canonicalSemesterKey(it.semester) == sem &&
                                                    RoutineRules.normalizeGroup(dept, it.group) == grp
                                            }
                                        }
                                    }
                                    UserRole.TEACHER -> {
                                        allEntries.filter {
                                            it.teacherEmail.trim().lowercase() == cleanEmail
                                        }
                                    }
                                    UserRole.ADMIN -> {
                                        allEntries.filter {
                                            it.department == "CST" &&
                                                RoutineRules.normalizeShift(it.shift) == RoutineRules.SHIFT_1 &&
                                                RoutineRules.canonicalSemesterKey(it.semester) == RoutineRules.ALL_SEMESTER_KEYS.first() &&
                                                RoutineRules.normalizeGroup("CST", it.group) == "Group A"
                                        }
                                    }
                                }
                            }

                            val defaultQuickFilter = remember(currentRole, studentProfile, teacherPref) {
                                val sp = studentProfile
                                if (currentRole == UserRole.STUDENT && sp != null && sp.department.isNotBlank()) {
                                    val dept = RoutineRules.normalizeDepartmentShort(sp.department)
                                    RoutineQuickFilter(
                                        semesterKey = RoutineRules.canonicalSemesterKey(sp.semester).ifBlank { RoutineRules.ALL_SEMESTER_KEYS.first() },
                                        shift = RoutineRules.normalizeShift(sp.shift),
                                        department = dept,
                                        group = RoutineRules.normalizeGroup(dept, sp.section),
                                    )
                                } else {
                                    val dept = teacherPref?.department?.let { RoutineRules.normalizeDepartmentShort(it) }
                                        ?.takeIf { it in RoutineRules.TECH_DEPARTMENTS } ?: "CST"
                                    val shift = teacherPref?.shift?.takeIf { it.isNotBlank() }?.let { RoutineRules.normalizeShift(it) }
                                        ?: RoutineRules.SHIFT_1
                                    RoutineQuickFilter(
                                        semesterKey = RoutineRules.ALL_SEMESTER_KEYS.first(),
                                        shift = shift,
                                        department = dept,
                                        group = "Group A",
                                    )
                                }
                            }

                            val displayedEntries = remember(homeFiltered, allEntries, tempRoutineFilter) {
                                val tf = tempRoutineFilter
                                if (tf == null) {
                                    homeFiltered
                                } else {
                                    val dept = RoutineRules.normalizeDepartmentShort(tf.department)
                                    val shift = RoutineRules.normalizeShift(tf.shift)
                                    val sem = RoutineRules.canonicalSemesterKey(tf.semesterKey)
                                    val grp = RoutineRules.normalizeGroup(dept, tf.group)
                                    allEntries.filter {
                                        it.department == dept &&
                                            RoutineRules.normalizeShift(it.shift) == shift &&
                                            RoutineRules.canonicalSemesterKey(it.semester) == sem &&
                                            RoutineRules.normalizeGroup(dept, it.group) == grp
                                    }
                                }
                            }

                            val activeShiftForTime = when {
                                tempRoutineFilter != null -> RoutineRules.normalizeShift(tempRoutineFilter!!.shift)
                                currentRole == UserRole.STUDENT -> RoutineRules.normalizeShift(studentProfile?.shift ?: RoutineRules.SHIFT_1)
                                currentRole == UserRole.TEACHER -> RoutineRules.normalizeShift(teacherPref?.shift ?: RoutineRules.SHIFT_1)
                                else -> RoutineRules.SHIFT_1
                            }

                            val headerBadgeText = when {
                                tempRoutineFilter != null -> tempRoutineFilter!!.toBadgeText()
                                currentRole == UserRole.STUDENT -> {
                                    val sp = studentProfile
                                    if (sp != null && sp.department.isNotBlank() && sp.semester.isNotBlank()) {
                                        val semNum = RoutineRules.semesterNumber(sp.semester) ?: 1
                                        val shiftNum = if (RoutineRules.normalizeShift(sp.shift) == RoutineRules.SHIFT_2) "2" else "1"
                                        val dept = RoutineRules.normalizeDepartmentShort(sp.department)
                                        val grpLetter = RoutineRules.normalizeGroup(dept, sp.section)
                                            .removePrefix("Group ")
                                            .trim()
                                        "$semNum/$shiftNum/$dept-$grpLetter"
                                    } else {
                                        ""
                                    }
                                }
                                currentRole == UserRole.TEACHER -> {
                                    val tp = teacherPref
                                    if (tp != null && tp.department.isNotBlank()) {
                                        val dept = RoutineRules.normalizeDepartmentShort(tp.department)
                                        val shiftNum = if (RoutineRules.normalizeShift(tp.shift) == RoutineRules.SHIFT_2) "2" else "1"
                                        "$shiftNum/$dept • ${homeFiltered.size} Cls"
                                    } else {
                                        ""
                                    }
                                }
                                else -> ""
                            }

                            Column(modifier = Modifier.fillMaxSize()) {
                                HomeRoutineHeader(
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    photoUrl = currentPhotoUrl,
                                    avatarSeed = email.ifBlank { currentUserName },
                                    onProfileClick = { navigateTo("profile") },
                                    onAiClick = { navigateTo("ai") },
                                    onNotificationClick = { navigateTo("notification") },
                                    titleText = headerBadgeText,
                                    subtitleText = "",
                                    isExpanded = isRoutineFilterExpanded,
                                    onToggleExpand = { isRoutineFilterExpanded = !isRoutineFilterExpanded },
                                    isDownloading = isDownloadingRoutine,
                                    onDownloadClick = {
                                        if (displayedEntries.isEmpty()) {
                                            showToast("No classes to download")
                                        } else if (!isDownloadingRoutine) {
                                            isDownloadingRoutine = true
                                            val label = headerBadgeText.ifBlank {
                                                if (currentRole == UserRole.TEACHER) "Teacher_Routine" else "Class_Routine"
                                            }
                                            coroutineScope.launch {
                                                try {
                                                    val res = downloadRoutinePdf(
                                                        label = label,
                                                        shift = activeShiftForTime,
                                                        config = activeConfig,
                                                        entries = displayedEntries,
                                                    )
                                                    res.onSuccess { msg -> showToast(msg) }
                                                        .onFailure { err -> showToast(err.message ?: "Could not save routine PDF") }
                                                } finally {
                                                    isDownloadingRoutine = false
                                                }
                                            }
                                        }
                                    },
                                    activeTempFilter = tempRoutineFilter,
                                    defaultFilter = defaultQuickFilter,
                                    onTempFilterChange = { updated -> tempRoutineFilter = updated },
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ClassRoutineTable(
                                        hindSiliguri = hindSiliguri,
                                        isDark = isDark,
                                        entries = displayedEntries,
                                        periodsPerDay = activeConfig.periodsPerDay,
                                        shift = activeShiftForTime,
                                        config = activeConfig,
                                        showGroupOrRoomHint = currentRole == UserRole.TEACHER && tempRoutineFilter == null,
                                        onSlotClick = { entry -> inspectedEntry = entry },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rainbowScreenBackground(isDark),
                            ) {
                                GoogleTopAppBar(
                                    title = "Menu",
                                    hindSiliguri = hindSiliguri,
                                    navigationIcon = null,
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                ) {
                                    MenuScreen(
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        role = currentRole,
                                        currentThemeMode = currentThemeMode,
                                        onThemeModeChange = onThemeModeChange,
                                        onProfileClick = { navigateTo("profile") },
                                        onAiClick = { navigateTo("ai") },
                                        onNoticeClick = { navigateTo("notification") },
                                        onClassroomClick = { navigateTo("classrooms") },
                                        onBooksClick = { navigateTo("books") },
                                        onTeachersClick = { navigateTo("teachers") },
                                        onRoutineManagerClick = { navigateTo("routine_manager") },
                                        onTeacherPreferencesClick = { navigateTo("teacher_prefs") },
                                        onBloodFinderClick = { navigateTo("blood_finder") },
                                        onAppearanceClick = { navigateTo("appearance") },
                                        onSignInControlClick = { navigateTo("admin_signin_control") },
                                        onAdminApprovalsClick = { navigateTo("admin_approvals") },
                                        topContentPadding = 0.dp,
                                    )
                                }
                            }
                        }
                    }
                }

                HomeMenuBottomBar(
                    isHomeSelected = if (pagerState.isScrollInProgress) pagerState.targetPage == 0 else activeBaseTab == "home",
                    isDark = isDark,
                    cardBg = cardBg,
                    hindSiliguri = hindSiliguri,
                    homeShape = homeShape,
                    menuShape = menuShape,
                    onHomeClick = { navigateTo("home") },
                    onMenuClick = { navigateTo("menu") },
                )
            }

            // Secondary Overlay Layer
            AnimatedContent(
                targetState = overlayTab,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(20f),
                label = "RoleOverlayTabTransition",
            ) { tab ->
                when (tab) {
                    "profile" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isProfileSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Profile",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                ProfileScreen(
                                    userName = currentUserName,
                                    role = currentRole,
                                    isDark = isDark,
                                    currentThemeMode = currentThemeMode,
                                    onThemeModeChange = onThemeModeChange,
                                    onSignOut = onSignOut,
                                    hindSiliguri = hindSiliguri,
                                    email = email,
                                    photoUrl = currentPhotoUrl,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isProfileSubOverlayOpen = it },
                                    onOpenTeachingPrefs = if (currentRole == UserRole.TEACHER) {
                                        { navigateTo("teacher_prefs") }
                                    } else {
                                        null
                                    },
                                    onUpdateProfile = { newName, newPhotoUrl ->
                                        currentUserName = newName
                                        currentPhotoUrl = newPhotoUrl
                                        val sessionEmail = email.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()
                                        if (sessionEmail.isNotBlank()) {
                                            saveLocalProfile(sessionEmail, newName, newPhotoUrl, "google", currentRole.name)
                                            saveUserSession(
                                                UserSession(
                                                    role = currentRole.name,
                                                    email = sessionEmail,
                                                    name = newName,
                                                    photoUrl = newPhotoUrl,
                                                )
                                            )
                                            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                                                if (currentRole == UserRole.ADMIN) {
                                                    RoleConfig.saveAdminEmail(sessionEmail, newName, newPhotoUrl)
                                                }
                                                RoleConfig.saveUserProfile(sessionEmail, newName, newPhotoUrl, "google", currentRole.name)
                                                studentProfile = RoleConfig.fetchStudentProfile(sessionEmail)
                                            }
                                        }
                                        onProfileUpdated?.invoke(newName, newPhotoUrl)
                                    },
                                    onCategoryChanged = { newRole ->
                                        currentRole = newRole
                                        val sessionEmail = email.ifBlank { loadUserSession()?.email ?: "" }
                                        if (sessionEmail.isNotBlank()) {
                                            saveUserSession(
                                                UserSession(
                                                    role = newRole.name,
                                                    email = sessionEmail,
                                                    name = currentUserName,
                                                    photoUrl = currentPhotoUrl,
                                                )
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                    "notification" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isNotificationsSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Notifications",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            showAi = true,
                                            showNotification = false,
                                            onAiClick = { navigateTo("ai") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                NotificationScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    topContentPadding = 0.dp,
                                    role = currentRole,
                                    email = email,
                                    studentProfile = studentProfile,
                                    teacherPref = teacherPref,
                                    publishedPlan = publishedPlan,
                                    onNavigateToRoute = { dest -> navigateTo(dest) },
                                    onFullScreenOverlayChange = { isNotificationsSubOverlayOpen = it },
                                )
                            }
                        }
                    }
                    "teachers" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isTeachersSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Teachers Directory",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                TeachersDirectoryScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    canEdit = currentRole == UserRole.ADMIN,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isTeachersSubOverlayOpen = it },
                                )
                            }
                        }
                    }
                    "books" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isBooksSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Books Library",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                BooksLibraryScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    canEdit = currentRole == UserRole.ADMIN,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isBooksSubOverlayOpen = it },
                                )
                            }
                        }
                    }
                    "classrooms" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isClassroomSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Classrooms & Labs",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                ClassroomManagementScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    canEdit = currentRole == UserRole.ADMIN,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isClassroomSubOverlayOpen = it },
                                )
                            }
                        }
                    }
                    "routine_manager" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isRoutineSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Routine Engine",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                RoutineGeneratorScreen(
                                    adminEmail = email,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isRoutineSubOverlayOpen = it },
                                    onPlanPublished = { updated -> publishedPlan = updated },
                                )
                            }
                        }
                    }
                    "teacher_prefs" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isTeacherPrefsSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Teaching Subjects",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = {
                                        teacherOnboardingDismissed = true
                                        goBack()
                                    },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                TeacherPreferencesScreen(
                                    email = email,
                                    userName = currentUserName,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    topContentPadding = 0.dp,
                                    isOnboardingPrompt = !(teacherPref?.schedulingSetupComplete ?: true),
                                    onFullScreenOverlayChange = { isTeacherPrefsSubOverlayOpen = it },
                                    onDismissOnboarding = {
                                        teacherOnboardingDismissed = true
                                        goBack()
                                    },
                                    onSaved = { saved ->
                                        teacherPref = saved
                                        teacherOnboardingDismissed = true
                                        goBack()
                                    },
                                )
                            }
                        }
                    }
                    "blood_finder" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AnimatedVisibility(visible = !isBloodFinderSubOverlayOpen) {
                                GoogleTopAppBar(
                                    title = "Blood Finder",
                                    hindSiliguri = hindSiliguri,
                                    onNavigationClick = { goBack() },
                                    actions = {
                                        HeaderAiNotificationActions(
                                            isDark = isDark,
                                            onAiClick = { navigateTo("ai") },
                                            onNotificationClick = { navigateTo("notification") },
                                        )
                                    },
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                BloodFinderScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isBloodFinderSubOverlayOpen = it },
                                )
                            }
                        }
                    }
                    "appearance" -> {
                        AppearanceSubScreen(
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            currentThemeMode = currentThemeMode,
                            onThemeModeChange = onThemeModeChange,
                            onBack = { goBack() },
                        )
                    }
                    "admin_signin_control" -> {
                        SignInControlSubScreen(
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onBack = { goBack() },
                        )
                    }
                    "admin_approvals" -> {
                        AdminApprovalsSubScreen(
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onBack = { goBack() },
                        )
                    }
                    "ai" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark),
                        ) {
                            AiAssistantScreen(
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onBack = { goBack() },
                                onNotificationClick = { navigateTo("notification") },
                                userDisplayName = currentUserName,
                                userRole = currentRole,
                                userEmail = email,
                            )
                        }
                    }
                }
            }
            // Slot Inspection / Teacher Edit Request Dialog (inside root Box)
            val slot = inspectedEntry
            if (slot != null) {
                SlotInspectionDialog(
                    entry = slot,
                    role = currentRole,
                    teacherName = currentUserName,
                    teacherEmail = email,
                    classrooms = classrooms,
                    periodsPerDay = publishedPlan?.config?.periodsPerDay ?: 7,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { inspectedEntry = null },
                    onOpenRoutineManager = {
                        inspectedEntry = null
                        navigateTo("routine_manager")
                    },
                )
            }
    }
}

private data class SlotMetaField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: (() -> Unit)? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SlotInspectionDialog(
    entry: RoutineEntry,
    role: UserRole,
    teacherName: String,
    teacherEmail: String,
    classrooms: List<ClassroomItem>,
    periodsPerDay: Int,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onOpenRoutineManager: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val slotAccent = if (entry.isPractical) {
        KlassFeaturePalette.teaching(isDark)
    } else {
        departmentAccent(entry.department, isDark)
    }
    val roomAccent = KlassFeaturePalette.classrooms(isDark)
    val teacherAccent = KlassFeaturePalette.teachers(isDark)
    val booksAccent = KlassFeaturePalette.books(isDark)
    val successAccent = KlassFeaturePalette.success(isDark)

    // Sub-screen navigation stack inside SlotInspection:
    // 0 = Slot Detail (Level 1)
    // 1 = Room Quick-Peek (Level 2)
    // 2 = Request Edit Step 1: Select Day/Period/Room (Level 2)
    // 3 = Request Edit Step 2: Review & Reason Confirm (Level 3)
    var subStep by remember(entry) { mutableIntStateOf(0) }
    var activePickerKind by remember { mutableStateOf<String?>(null) } // "DAY", "PERIOD", "ROOM"
    var reqDay by remember(entry) { mutableStateOf(entry.day) }
    var reqPeriod by remember(entry) { mutableIntStateOf(entry.periodIndex) }
    var reqRoom by remember(entry) { mutableStateOf(entry.roomNumber) }
    var reqReason by remember(entry) { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val matchedRoom = remember(entry, classrooms) {
        classrooms.firstOrNull { it.roomNumber.equals(entry.roomNumber, true) }
    }

    val metaFields = remember(entry, matchedRoom, isDark) {
        listOf(
            SlotMetaField(
                label = "Schedule",
                value = "${entry.day}, Period ${entry.periodIndex + 1} (${entry.startTime} – ${entry.endTime})",
                icon = ClockIcon,
                accent = slotAccent.primary,
            ),
            SlotMetaField(
                label = "Department & Batch",
                value = "${entry.department} • ${RoutineRules.semesterDisplayLabel(entry.semester)} • ${entry.group}",
                icon = GraduationCapIcon,
                accent = KlassFeaturePalette.teaching(isDark).primary,
            ),
            SlotMetaField(
                label = "Classroom / Lab",
                value = "Room ${entry.roomNumber} (${entry.shift})",
                icon = DoorOpenIcon,
                accent = roomAccent.primary,
                onClick = if (matchedRoom != null) { { subStep = 1 } } else null,
            ),
            SlotMetaField(
                label = "Instructor",
                value = entry.teacherName.ifBlank { entry.teacherEmail.ifBlank { "Assigned Faculty" } },
                icon = PersonIcon,
                accent = teacherAccent.primary,
            ),
            SlotMetaField(
                label = "Subject Code & Format",
                value = "${entry.subjectCode} • ${if (entry.isPractical) "Practical / Lab" else "Theory"}",
                icon = BookIcon,
                accent = booksAccent.primary,
            ),
        )
    }

    fun submitEditRequest() {
        scope.launch {
            isSubmitting = true
            try {
                val now = getCurrentTimeMillis()
                val roomObj = classrooms.firstOrNull { it.roomNumber.equals(reqRoom, true) }
                val req = RoutineEditRequest(
                    id = "req_$now",
                    requestedByEmail = teacherEmail.trim().lowercase(),
                    requestedByName = teacherName,
                    targetEntryId = entry.id,
                    department = entry.department,
                    semester = entry.semester,
                    shift = entry.shift,
                    group = entry.group,
                    subjectCode = entry.subjectCode,
                    subjectTitle = entry.subjectTitle,
                    originalDay = entry.day,
                    originalPeriod = entry.periodIndex,
                    originalRoomNumber = entry.roomNumber,
                    requestedDay = reqDay,
                    requestedPeriod = reqPeriod,
                    requestedRoomId = roomObj?.id ?: entry.roomId,
                    requestedRoomNumber = reqRoom.ifBlank { entry.roomNumber },
                    reason = reqReason.trim().ifBlank { "Schedule adjustment requested" },
                    status = EditRequestStatus.PENDING,
                    createdAt = now,
                )
                val res = submitRoutineEditRequestInFirestore(req)
                if (res.isSuccess) {
                    showToast("Request submitted for Admin approval!")
                    onDismiss()
                } else {
                    showToast("Could not submit request.")
                }
            } finally {
                isSubmitting = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().zIndex(90f)) {
        // Level 1: Class Slot Detail Sub-Screen
        MinimalFullScreenSheet(
            title = entry.subjectTitle,
            subtitle = "${entry.subjectCode} • ${entry.day} P${entry.periodIndex + 1}",
            levelLabel = "Routine › Slot Details",
            icon = CalendarDaysIcon,
            accent = slotAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = { if (!isSubmitting) onDismiss() },
            dismissEnabled = !isSubmitting,
            maxContentWidth = 760.dp,
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GoogleOutlinedButton(
                        text = "Close",
                        hindSiliguri = hindSiliguri,
                        enabled = !isSubmitting,
                        height = 48.dp,
                        contentColor = cs.onSurface,
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    )
                    if (role == UserRole.TEACHER) {
                        WelcomeMinimal3DButton(
                            text = "Request Change",
                            icon = EditIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            customPrimaryColor = slotAccent.primary,
                            customBaseColor = slotAccent.deep,
                            height = 48.dp,
                            modifier = Modifier.weight(1.35f),
                            onClick = { subStep = 2 },
                        )
                    } else if (role == UserRole.ADMIN) {
                        WelcomeMinimal3DButton(
                            text = "Edit in Engine",
                            icon = EditIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            customPrimaryColor = slotAccent.primary,
                            customBaseColor = slotAccent.deep,
                            height = 48.dp,
                            modifier = Modifier.weight(1.35f),
                            onClick = onOpenRoutineManager,
                        )
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    KlassHeroBanner(
                        title = entry.subjectTitle,
                        subtitle = "${entry.department} • ${RoutineRules.semesterDisplayLabel(entry.semester)}",
                        icon = if (entry.isPractical) GraduationCapIcon else BookIcon,
                        accent = slotAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badges = listOf(
                            (if (entry.isPractical) "Lab Session" else "Theory Class") to slotAccent.primary,
                            "Room ${entry.roomNumber}" to roomAccent.primary,
                            "${entry.startTime}–${entry.endTime}" to booksAccent.primary,
                        ),
                    )
                }

                GoogleSectionHeader(
                    title = "Class Metadata",
                    accent = slotAccent.primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    metaFields.forEachIndexed { idx, field ->
                        KlassDetailFieldRow(
                            label = field.label,
                            value = field.value,
                            icon = field.icon,
                            accent = field.accent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = field.onClick,
                            trailingContent = if (field.onClick != null) {
                                {
                                    KlassTonalButton(
                                        text = "Room Info",
                                        accent = roomAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        height = 36.dp,
                                        onClick = field.onClick,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                        if (idx < metaFields.lastIndex) {
                            SocialInsetDivider(startIndent = 68.dp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Level 2A: Room Quick-Peek Sub-Sub-Screen
        KlassSubScreenHost(visible = subStep == 1 && matchedRoom != null, zIndex = 92f) {
            val room = matchedRoom
            if (room != null) {
                MinimalFullScreenSheet(
                    title = "Room ${room.roomNumber}",
                    subtitle = "${room.roomType.label} • ${room.shifts.joinToString(", ")}",
                    levelLabel = "Routine › Slot › Room Info",
                    icon = DoorOpenIcon,
                    accent = roomAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { subStep = 0 },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            KlassHeroBanner(
                                title = "Room ${room.roomNumber}",
                                subtitle = room.department.ifBlank { "Shared Campus Facility" },
                                icon = DoorOpenIcon,
                                accent = roomAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                badges = listOf(
                                    room.roomType.label to roomAccent.primary,
                                    "Capacity ${room.capacity}" to booksAccent.primary,
                                ),
                            )
                        }
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            KlassDetailFieldRow(
                                label = "Room Type",
                                value = room.roomType.label,
                                icon = InfoCircleIcon,
                                accent = roomAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Seating Capacity",
                                value = "${room.capacity} Students",
                                icon = PersonIcon,
                                accent = booksAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Primary Department",
                                value = room.department.ifBlank { "General / Multi-Department" },
                                icon = GraduationCapIcon,
                                accent = teacherAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                        }
                    }
                }
            }
        }

        // Level 2B: Teacher Edit Request — Step 1: Select Day, Period & Room
        KlassSubScreenHost(visible = subStep == 2, zIndex = 92f) {
            MinimalFullScreenSheet(
                title = "Request Slot Change",
                subtitle = "Step 1: Choose preferred slot & room",
                levelLabel = "Routine › Slot › Request Edit",
                icon = EditIcon,
                accent = slotAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = { subStep = 0 },
                bottomBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GoogleOutlinedButton(
                            text = "Back",
                            hindSiliguri = hindSiliguri,
                            height = 48.dp,
                            contentColor = cs.onSurface,
                            modifier = Modifier.weight(1f),
                            onClick = { subStep = 0 },
                        )
                        WelcomeMinimal3DButton(
                            text = "Continue",
                            icon = RightArrowIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            customPrimaryColor = slotAccent.primary,
                            customBaseColor = slotAccent.deep,
                            height = 48.dp,
                            modifier = Modifier.weight(1.3f),
                            onClick = { subStep = 3 },
                        )
                    }
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        MinimalStatBadge(
                            icon = ClockIcon,
                            text = "Current: ${entry.day} P${entry.periodIndex + 1} • Room ${entry.roomNumber}",
                            accent = slotAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                    }

                    GoogleSectionHeader(
                        title = "New Slot Preferences",
                        accent = slotAccent.primary,
                        hindSiliguri = hindSiliguri,
                    )
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        KlassNavigationRow(
                            title = "Requested Day",
                            subtitle = "Working day (Sunday – Thursday)",
                            icon = CalendarDaysIcon,
                            accent = slotAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            badgeText = reqDay,
                            onClick = { activePickerKind = "DAY" },
                        )
                        SocialInsetDivider(startIndent = 72.dp)
                        KlassNavigationRow(
                            title = "Requested Period",
                            subtitle = "Period slot index",
                            icon = ClockIcon,
                            accent = booksAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            badgeText = "Period ${reqPeriod + 1}",
                            onClick = { activePickerKind = "PERIOD" },
                        )
                        if (classrooms.isNotEmpty()) {
                            SocialInsetDivider(startIndent = 72.dp)
                            KlassNavigationRow(
                                title = "Requested Classroom / Lab",
                                subtitle = "Preferred room for this class",
                                icon = DoorOpenIcon,
                                accent = roomAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                badgeText = reqRoom.ifBlank { entry.roomNumber },
                                onClick = { activePickerKind = "ROOM" },
                            )
                        }
                    }
                }
            }
        }

        // Level 3A: Dedicated Full-Screen Pickers for Day / Period / Room
        KlassSubScreenHost(visible = subStep == 2 && activePickerKind != null, zIndex = 95f) {
            when (activePickerKind) {
                "DAY" -> KlassSelectionPickerSubScreen(
                    title = "Select Day",
                    subtitle = "Choose target working day",
                    levelLabel = "Routine › Request › Day",
                    icon = CalendarDaysIcon,
                    accent = slotAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    options = RoutineRules.WORKING_DAYS,
                    selectedOption = reqDay,
                    optionLabel = { it },
                    onSelect = { reqDay = it },
                    onDismiss = { activePickerKind = null },
                )
                "PERIOD" -> KlassSelectionPickerSubScreen(
                    title = "Select Period",
                    subtitle = "Choose target class period",
                    levelLabel = "Routine › Request › Period",
                    icon = ClockIcon,
                    accent = booksAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    options = (0 until periodsPerDay.coerceIn(4, 10)).toList(),
                    selectedOption = reqPeriod,
                    optionLabel = { "Period ${it + 1}" },
                    onSelect = { reqPeriod = it },
                    onDismiss = { activePickerKind = null },
                )
                "ROOM" -> KlassSelectionPickerSubScreen(
                    title = "Select Room",
                    subtitle = "${classrooms.size} rooms available",
                    levelLabel = "Routine › Request › Room",
                    icon = DoorOpenIcon,
                    accent = roomAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    options = classrooms,
                    selectedOption = classrooms.firstOrNull { it.roomNumber.equals(reqRoom, true) },
                    optionLabel = { "Room ${it.roomNumber}" },
                    optionSubtitle = { "${it.roomType.label} • Capacity ${it.capacity}" },
                    onSelect = { reqRoom = it.roomNumber },
                    onDismiss = { activePickerKind = null },
                )
            }
        }

        // Level 3B: Confirm & Reason Sub-Sub-Sub-Screen
        KlassSubScreenHost(visible = subStep == 3, zIndex = 94f) {
            MinimalFullScreenSheet(
                title = "Confirm Request",
                subtitle = "Step 2: Review change & add reason",
                levelLabel = "Routine › Slot › Edit › Confirm",
                icon = CheckIcon,
                accent = successAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = { if (!isSubmitting) subStep = 2 },
                dismissEnabled = !isSubmitting,
                bottomBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GoogleOutlinedButton(
                            text = "Back",
                            hindSiliguri = hindSiliguri,
                            enabled = !isSubmitting,
                            height = 48.dp,
                            contentColor = cs.onSurface,
                            modifier = Modifier.weight(1f),
                            onClick = { subStep = 2 },
                        )
                        WelcomeMinimal3DButton(
                            text = if (isSubmitting) "Submitting..." else "Submit Request",
                            icon = CheckIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            customPrimaryColor = successAccent.primary,
                            customBaseColor = successAccent.deep,
                            isLoading = isSubmitting,
                            enabled = !isSubmitting,
                            height = 48.dp,
                            modifier = Modifier.weight(1.4f),
                            onClick = { submitEditRequest() },
                        )
                    }
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    KlassHeroBanner(
                        title = "${entry.subjectTitle} (${entry.subjectCode})",
                        subtitle = "${entry.department} • ${entry.group}",
                        icon = CheckIcon,
                        accent = successAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badges = listOf(
                            "From: ${entry.day} P${entry.periodIndex + 1} (${entry.roomNumber})" to booksAccent.primary,
                            "To: $reqDay P${reqPeriod + 1} (${reqRoom.ifBlank { entry.roomNumber }})" to successAccent.primary,
                        ),
                    )

                    OutlinedTextField(
                        value = reqReason,
                        onValueChange = { reqReason = it },
                        minLines = 3,
                        maxLines = 6,
                        label = { Text("Reason for Change", fontFamily = hindSiliguri) },
                        placeholder = { Text("Write a clear note for the Admin...", fontFamily = hindSiliguri) },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = hindSiliguri,
                            lineHeight = 22.sp,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = successAccent.primary,
                            unfocusedBorderColor = cs.outlineVariant,
                            focusedLabelColor = successAccent.primary,
                            unfocusedLabelColor = cs.onSurfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
