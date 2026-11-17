package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    var isClassroomSubOverlayOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        publishedPlan = fetchRoutinePlanFromFirestore("master_6m_published")
    }

    val hindSiliguri = getHindSiliguriFontFamily()
    val tabBackStack = remember { mutableStateListOf("home") }
    val selectedTab = tabBackStack.lastOrNull() ?: "home"

    fun navigateTo(tab: String) {
        if (selectedTab != tab) {
            tabBackStack.add(tab)
        }
    }

    fun goBack() {
        if (tabBackStack.size > 1) {
            tabBackStack.removeAt(tabBackStack.lastIndex)
        } else {
            onBackToWelcome()
        }
    }

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
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
                            val entries = publishedPlan?.entries ?: emptyList()
                            val filtered = remember(entries) {
                                entries.filter {
                                    it.department == "CST" &&
                                        RoutineRules.normalizeShift(it.shift) == RoutineRules.SHIFT_1 &&
                                        RoutineRules.canonicalSemesterKey(it.semester) == RoutineRules.ALL_SEMESTER_KEYS.first() &&
                                        RoutineRules.normalizeGroup("CST", it.group) == "Group A"
                                }
                            }
                            val cfg = publishedPlan?.config ?: RoutineConfig()

                            Column(modifier = Modifier.fillMaxSize()) {
                                HomeRoutineHeader(
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    photoUrl = currentPhotoUrl,
                                    avatarSeed = guestSeed,
                                    onProfileClick = { navigateTo("profile") },
                                    onAiClick = { navigateTo("ai") },
                                    onNotificationClick = { navigateTo("notification") },
                                    titleText = "1/1/CST-A",
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
                                        entries = filtered,
                                        periodsPerDay = cfg.periodsPerDay,
                                        shift = RoutineRules.SHIFT_1,
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
                                        role = null,
                                        currentThemeMode = currentThemeMode,
                                        onThemeModeChange = onThemeModeChange,
                                        onProfileClick = { navigateTo("profile") },
                                        onAiClick = { navigateTo("ai") },
                                        onNoticeClick = { navigateTo("notification") },
                                        onClassroomClick = { navigateTo("classrooms") },
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

            AnimatedContent(
                targetState = overlayTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(220))
                },
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
                                    role = null,
                                    isDark = isDark,
                                    currentThemeMode = currentThemeMode,
                                    onThemeModeChange = onThemeModeChange,
                                    onSignOut = onSwitchToLogin,
                                    hindSiliguri = hindSiliguri,
                                    email = guestSeed,
                                    photoUrl = currentPhotoUrl,
                                    topContentPadding = 0.dp,
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
                                    role = null,
                                    publishedPlan = publishedPlan,
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
                                    isGuest = true,
                                    onSignInClick = onSwitchToLogin,
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
}
