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
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.ThemeMode
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

    val autoLocationSyncLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank() && email.isNotBlank() && currentRole == UserRole.STUDENT) {
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

    fun navigateTo(tab: String) {
        if (selectedTab != tab) {
            tabBackStack.add(tab)
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
            studentProfile = RoleConfig.fetchStudentProfile(cleanEmail)
            if (currentRole == UserRole.TEACHER) {
                val loadedPref = fetchTeacherSchedulingPrefFromFirestore(cleanEmail)
                teacherPref = loadedPref
                if ((loadedPref == null || !loadedPref.schedulingSetupComplete) && !teacherOnboardingDismissed) {
                    navigateTo("teacher_prefs")
                }
            }
            if (currentRole == UserRole.STUDENT) {
                autoLocationSyncLauncher()
            }
            CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                RoleConfig.seedBtebLibraryBooks(forceReseed = false)
            }
        }
        publishedPlan = fetchRoutinePlanFromFirestore("master_6m_published")
        classrooms = fetchClassroomsFromFirestore()
    }

    val coroutineScope = rememberCoroutineScope()
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
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
                    if (pageTab == "home" && tabBackStack.size >= 2 && tabBackStack[tabBackStack.lastIndex - 1] == "home") {
                        tabBackStack.removeAt(tabBackStack.lastIndex)
                    } else {
                        tabBackStack.add(pageTab)
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
            .background(MaterialTheme.colorScheme.background),
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

                            val activeShiftForTime = when (currentRole) {
                                UserRole.STUDENT -> RoutineRules.normalizeShift(studentProfile?.shift ?: RoutineRules.SHIFT_1)
                                UserRole.TEACHER -> RoutineRules.normalizeShift(teacherPref?.shift ?: RoutineRules.SHIFT_1)
                                UserRole.ADMIN -> RoutineRules.SHIFT_1
                            }

                            val headerBadgeText = when (currentRole) {
                                UserRole.STUDENT -> {
                                    val sp = studentProfile
                                    if (sp != null && sp.department.isNotBlank()) {
                                        val semNum = RoutineRules.semesterNumber(sp.semester) ?: 1
                                        val shiftNum = if (RoutineRules.normalizeShift(sp.shift) == RoutineRules.SHIFT_2) "2" else "1"
                                        val dept = RoutineRules.normalizeDepartmentShort(sp.department)
                                        val grpLetter = RoutineRules.normalizeGroup(dept, sp.section)
                                            .removePrefix("Group ")
                                            .trim()
                                        "$semNum/$shiftNum/$dept-$grpLetter"
                                    } else {
                                        currentUserName.ifBlank { "Student" }
                                    }
                                }
                                UserRole.TEACHER -> {
                                    val tp = teacherPref
                                    if (tp != null && tp.department.isNotBlank()) {
                                        val dept = RoutineRules.normalizeDepartmentShort(tp.department)
                                        val shiftNum = if (RoutineRules.normalizeShift(tp.shift) == RoutineRules.SHIFT_2) "2" else "1"
                                        "$shiftNum/$dept • ${homeFiltered.size} Cls"
                                    } else {
                                        currentUserName.ifBlank { "Teacher" }
                                    }
                                }
                                UserRole.ADMIN -> "1/1/CST-A"
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
                                        entries = homeFiltered,
                                        periodsPerDay = activeConfig.periodsPerDay,
                                        shift = activeShiftForTime,
                                        config = activeConfig,
                                        showGroupOrRoomHint = currentRole == UserRole.TEACHER,
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
                                    .background(MaterialTheme.colorScheme.surface),
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
                                .background(MaterialTheme.colorScheme.surface),
                        ) {
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
                                .background(MaterialTheme.colorScheme.surface),
                        ) {
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
                                )
                            }
                        }
                    }
                    "teachers" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
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
                                .background(MaterialTheme.colorScheme.surface),
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
                                .background(MaterialTheme.colorScheme.surface),
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
                                .background(MaterialTheme.colorScheme.surface),
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
                                .background(MaterialTheme.colorScheme.surface),
                        ) {
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
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                TeacherPreferencesScreen(
                                    email = email,
                                    userName = currentUserName,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    topContentPadding = 0.dp,
                                    isOnboardingPrompt = !(teacherPref?.schedulingSetupComplete ?: true),
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
                                .background(MaterialTheme.colorScheme.surface),
                        ) {
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
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                BloodFinderScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    topContentPadding = 0.dp,
                                )
                            }
                        }
                    }
                    "ai" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            AiAssistantScreen(
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onBack = { goBack() },
                                onNotificationClick = { navigateTo("notification") },
                                userDisplayName = currentUserName,
                            )
                        }
                    }
                }
            }
    }

    // Slot Inspection / Teacher Edit Request Dialog
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

private data class SlotMetaField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val accent: Color,
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
    val cardBg = cs.surfaceContainerLow
    val textColor = cs.onSurface
    val subTextColor = cs.onSurfaceVariant
    val borderCol = cs.outlineVariant
    val accent = cs.primary

    var showEditForm by remember { mutableStateOf(false) }
    var reqDay by remember(entry) { mutableStateOf(entry.day) }
    var reqPeriod by remember(entry) { mutableIntStateOf(entry.periodIndex) }
    var reqRoom by remember(entry) { mutableStateOf(entry.roomNumber) }
    var reqReason by remember(entry) { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val metaFields = remember(entry, cs) {
        listOf(
            SlotMetaField(
                label = "Schedule",
                value = "${entry.day}, Period ${entry.periodIndex + 1} (${entry.startTime} – ${entry.endTime})",
                icon = ClockIcon,
                accent = cs.primary,
            ),
            SlotMetaField(
                label = "Department & Batch",
                value = "${entry.department} • ${RoutineRules.semesterDisplayLabel(entry.semester)} • ${entry.group}",
                icon = GraduationCapIcon,
                accent = cs.primary,
            ),
            SlotMetaField(
                label = "Classroom / Lab",
                value = "Room ${entry.roomNumber} (${entry.shift})",
                icon = DoorOpenIcon,
                accent = cs.tertiary,
            ),
            SlotMetaField(
                label = "Instructor",
                value = entry.teacherName.ifBlank { entry.teacherEmail.ifBlank { "Assigned Faculty" } },
                icon = PersonIcon,
                accent = cs.secondary,
            ),
            SlotMetaField(
                label = "Subject Code & Format",
                value = "${entry.subjectCode} • ${if (entry.isPractical) "Practical / Lab" else "Theory"}",
                icon = BookIcon,
                accent = cs.primary,
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
                    publishNotificationInFirestore(
                        AppNotificationItem(
                            id = "noti_req_new_$now",
                            title = "New Teacher Edit Request",
                            body = "$teacherName requested moving ${entry.subjectTitle} to $reqDay P${reqPeriod + 1} (Room ${req.requestedRoomNumber}).",
                            category = "REQUEST",
                            targetRole = "ADMIN",
                            createdAt = now,
                        )
                    )
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
        MinimalFullScreenSheet(
            title = if (showEditForm) "Request Slot Change" else entry.subjectTitle,
            subtitle = "${entry.subjectCode} • ${if (entry.isPractical) "Practical / Lab" else "Theory"}",
            icon = CalendarDaysIcon,
            accent = accent,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = {
                if (showEditForm && !isSubmitting) {
                    showEditForm = false
                } else if (!isSubmitting) {
                    onDismiss()
                }
            },
            dismissEnabled = !isSubmitting,
            maxContentWidth = 760.dp,
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GoogleOutlinedButton(
                        text = if (showEditForm) "Back" else "Close",
                        hindSiliguri = hindSiliguri,
                        enabled = !isSubmitting,
                        height = 46.dp,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (showEditForm) showEditForm = false else onDismiss()
                        },
                    )
                    if (showEditForm) {
                        WelcomeMinimal3DButton(
                            text = if (isSubmitting) "Submitting..." else "Submit Request",
                            icon = CheckIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            isLoading = isSubmitting,
                            enabled = !isSubmitting,
                            height = 46.dp,
                            cornerRadius = 23.dp,
                            modifier = Modifier.weight(1.3f),
                            onClick = { submitEditRequest() },
                        )
                    } else if (role == UserRole.TEACHER) {
                        WelcomeMinimal3DButton(
                            text = "Request Edit",
                            icon = EditIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            height = 46.dp,
                            cornerRadius = 23.dp,
                            modifier = Modifier.weight(1.3f),
                            onClick = { showEditForm = true },
                        )
                    } else if (role == UserRole.ADMIN) {
                        WelcomeMinimal3DButton(
                            text = "Edit in Manager",
                            icon = EditIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            height = 46.dp,
                            cornerRadius = 23.dp,
                            modifier = Modifier.weight(1.3f),
                            onClick = onOpenRoutineManager,
                        )
                    }
                }
            },
        ) {
            val cs = MaterialTheme.colorScheme
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!showEditForm) {
                    MinimalResponsiveGrid(
                        items = metaFields,
                        twoColMinWidth = 560.dp,
                        spacing = 10.dp,
                    ) { _, field ->
                        MinimalCardSurface(isDark = isDark, shape = RoundedCornerShape(18.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = cs.primaryContainer,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = field.icon,
                                            contentDescription = null,
                                            tint = cs.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = field.label,
                                        fontSize = 12.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Normal,
                                        color = cs.onSurfaceVariant,
                                    )
                                    Text(
                                        text = field.value,
                                        fontSize = 14.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Medium,
                                        color = cs.onSurface,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    MinimalStatBadge(
                        icon = ClockIcon,
                        text = "Current: ${entry.day} P${entry.periodIndex + 1} • Room ${entry.roomNumber}",
                        accent = cs.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GoogleSectionHeader(title = "Requested Working Day", hindSiliguri = hindSiliguri)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RoutineRules.WORKING_DAYS.forEach { d ->
                                MinimalIconChip(
                                    label = d.take(3),
                                    selected = reqDay.equals(d, true),
                                    accent = cs.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    onClick = { reqDay = d },
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GoogleSectionHeader(title = "Requested Period", hindSiliguri = hindSiliguri)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (p in 0 until periodsPerDay.coerceIn(4, 10)) {
                                MinimalIconChip(
                                    label = "P${p + 1}",
                                    selected = reqPeriod == p,
                                    accent = cs.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    onClick = { reqPeriod = p },
                                )
                            }
                        }
                    }

                    if (classrooms.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            GoogleSectionHeader(title = "Requested Classroom / Lab", hindSiliguri = hindSiliguri)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(classrooms) { r ->
                                    MinimalIconChip(
                                        label = r.roomNumber,
                                        selected = reqRoom.equals(r.roomNumber, true),
                                        accent = cs.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = { reqRoom = r.roomNumber },
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reqReason,
                        onValueChange = { reqReason = it },
                        minLines = 3,
                        maxLines = 6,
                        label = { Text("Reason", fontFamily = hindSiliguri) },
                        placeholder = { Text("Reason for change request...", fontFamily = hindSiliguri) },
                        textStyle = TextStyle(
                            fontSize = 14.5.sp,
                            fontFamily = hindSiliguri,
                            lineHeight = 20.sp,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cs.primary,
                            unfocusedBorderColor = cs.outlineVariant,
                            focusedLabelColor = cs.primary,
                            unfocusedLabelColor = cs.onSurfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
