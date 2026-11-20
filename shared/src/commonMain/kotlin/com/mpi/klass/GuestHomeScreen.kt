package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.getHindSiliguriFontFamily
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun GuestHomeScreen(
    currentThemeMode: ThemeMode = ThemeMode.DEFAULT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onSwitchToLogin: () -> Unit = {},
    onCloseApp: () -> Unit = {},
    onBackToWelcome: () -> Unit = {},
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    val initialSession = remember { loadUserSession() }
    val guestSeed = remember {
        initialSession?.email?.takeIf { it.isNotBlank() && it.startsWith("guest_") }
            ?: ("guest_" + (100000..999999).random())
    }
    var currentUserName by remember { mutableStateOf(initialSession?.name?.ifBlank { "Guest User" } ?: "Guest User") }
    var currentPhotoUrl by remember { mutableStateOf(initialSession?.photoUrl) }

    var publishedPlan by remember { mutableStateOf<RoutinePlan?>(null) }
    var tempRoutineFilter by remember { mutableStateOf<RoutineQuickFilter?>(null) }
    var isRoutineFilterExpanded by remember { mutableStateOf(false) }
    var isDownloadingRoutine by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isClassroomSubOverlayOpen by remember { mutableStateOf(false) }
    var isNotificationsSubOverlayOpen by remember { mutableStateOf(false) }
    var isBloodFinderSubOverlayOpen by remember { mutableStateOf(false) }
    var isProfileSubOverlayOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        publishedPlan = fetchRoutinePlanFromFirestore("master_6m_published")
    }

    val hindSiliguri = getHindSiliguriFontFamily()
    val tabBackStack = remember { mutableStateListOf("home") }
    val selectedTab = tabBackStack.lastOrNull() ?: "home"

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
            onBackToWelcome()
        }
    }

    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.86f) else Color.White.copy(alpha = 0.86f)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
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
        initialPage = 0,
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
                            val entries = publishedPlan?.entries ?: emptyList()
                            val displayedEntries = remember(entries, tempRoutineFilter) {
                                val tf = tempRoutineFilter
                                val dept = RoutineRules.normalizeDepartmentShort(tf?.department ?: "CST")
                                val shift = RoutineRules.normalizeShift(tf?.shift ?: RoutineRules.SHIFT_1)
                                val sem = RoutineRules.canonicalSemesterKey(tf?.semesterKey ?: RoutineRules.ALL_SEMESTER_KEYS.first())
                                val grp = RoutineRules.normalizeGroup(dept, tf?.group ?: "Group A")
                                entries.filter {
                                    it.department == dept &&
                                        RoutineRules.normalizeShift(it.shift) == shift &&
                                        RoutineRules.canonicalSemesterKey(it.semester) == sem &&
                                        RoutineRules.normalizeGroup(dept, it.group) == grp
                                }
                            }
                            val cfg = publishedPlan?.config ?: RoutineConfig()
                            val activeShiftForTime = tempRoutineFilter?.let { RoutineRules.normalizeShift(it.shift) }
                                ?: RoutineRules.SHIFT_1
                            val headerBadgeText = tempRoutineFilter?.toBadgeText() ?: ""

                            Column(modifier = Modifier.fillMaxSize()) {
                                HomeRoutineHeader(
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    photoUrl = currentPhotoUrl,
                                    avatarSeed = guestSeed,
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
                                            val label = headerBadgeText.ifBlank { "Class_Routine" }
                                            coroutineScope.launch {
                                                try {
                                                    val res = downloadRoutinePdf(
                                                        label = label,
                                                        shift = activeShiftForTime,
                                                        config = cfg,
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
                                    defaultFilter = RoutineQuickFilter(),
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
                                        periodsPerDay = cfg.periodsPerDay,
                                        shift = activeShiftForTime,
                                        config = cfg,
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
                                        role = null,
                                        currentThemeMode = currentThemeMode,
                                        onThemeModeChange = onThemeModeChange,
                                        onProfileClick = { navigateTo("profile") },
                                        onAiClick = { navigateTo("ai") },
                                        onNoticeClick = { navigateTo("notification") },
                                        onClassroomClick = { navigateTo("classrooms") },
                                        onBloodFinderClick = { navigateTo("blood_finder") },
                                        onAppearanceClick = { navigateTo("appearance") },
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

            AnimatedContent(
                targetState = overlayTab,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(20f),
                label = "GuestOverlayTabTransition",
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
                                    role = null,
                                    isDark = isDark,
                                    currentThemeMode = currentThemeMode,
                                    onThemeModeChange = onThemeModeChange,
                                    onSignOut = onSwitchToLogin,
                                    hindSiliguri = hindSiliguri,
                                    email = guestSeed,
                                    photoUrl = currentPhotoUrl,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isProfileSubOverlayOpen = it },
                                    onUpdateProfile = { newName, newPhotoUrl ->
                                        currentUserName = newName
                                        currentPhotoUrl = newPhotoUrl
                                        saveUserSession(
                                            UserSession(
                                                role = "GUEST",
                                                email = guestSeed,
                                                name = newName,
                                                photoUrl = newPhotoUrl,
                                            )
                                        )
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
                                    role = null,
                                    publishedPlan = publishedPlan,
                                    onNavigateToRoute = { dest ->
                                        if (dest == "sign_in") onSwitchToLogin() else navigateTo(dest)
                                    },
                                    onFullScreenOverlayChange = { isNotificationsSubOverlayOpen = it },
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
                                    canEdit = false,
                                    topContentPadding = 0.dp,
                                    onFullScreenOverlayChange = { isClassroomSubOverlayOpen = it },
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
                                    isGuest = true,
                                    onSignInClick = onSwitchToLogin,
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
                            )
                        }
                    }
                }
            }
    }
}
