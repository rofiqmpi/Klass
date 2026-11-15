package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
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

    val dragController = rememberSwipeIconDragController()

    CompositionLocalProvider(LocalSwipeIconDragController provides dragController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .onGloballyPositioned { coords ->
                    dragController.rootOffsetInWindow = coords.positionInWindow()
                    dragController.rootSize = coords.size
                },
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = overlayTab == null,
                modifier = Modifier.fillMaxSize(),
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
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(if (isDark) Color.White else Color.Black),
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(3.dp),
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
                            HomeMenuBottomBar(
                                isHomeSelected = true,
                                isDark = isDark,
                                cardBg = cardBg,
                                hindSiliguri = hindSiliguri,
                                homeShape = homeShape,
                                menuShape = menuShape,
                                onHomeClick = { navigateTo("home") },
                                onMenuClick = { navigateTo("menu") },
                            )
                        }
                    }

                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
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
                                    topContentPadding = 72.dp,
                                )
                                FloatingFadeTopHeader(
                                    title = "Menu",
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    textColor = textColor,
                                    hindSiliguri = hindSiliguri,
                                    onCloseClick = { goBack() },
                                    onAiClick = { navigateTo("ai") },
                                    onNotificationClick = { navigateTo("notification") },
                                    modifier = Modifier.align(Alignment.TopCenter),
                                )
                            }
                            HomeMenuBottomBar(
                                isHomeSelected = false,
                                isDark = isDark,
                                cardBg = cardBg,
                                hindSiliguri = hindSiliguri,
                                homeShape = homeShape,
                                menuShape = menuShape,
                                onHomeClick = { navigateTo("home") },
                                onMenuClick = { navigateTo("menu") },
                            )
                        }
                    }
                }
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
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
                                topContentPadding = 72.dp,
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
                            FloatingFadeTopHeader(
                                title = "Profile",
                                isDark = isDark,
                                cardBg = cardBg,
                                textColor = textColor,
                                hindSiliguri = hindSiliguri,
                                onCloseClick = { goBack() },
                                onAiClick = { navigateTo("ai") },
                                onNotificationClick = { navigateTo("notification") },
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        }
                    }
                    "notification" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            NotificationScreen(
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                role = null,
                                publishedPlan = publishedPlan,
                            )
                            FloatingFadeTopHeader(
                                title = "Notifications",
                                isDark = isDark,
                                cardBg = cardBg,
                                textColor = textColor,
                                hindSiliguri = hindSiliguri,
                                showLeftClose = false,
                                showRightClose = true,
                                onCloseClick = { goBack() },
                                onAiClick = { navigateTo("ai") },
                                onNotificationClick = { navigateTo("notification") },
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        }
                    }
                    "classrooms" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            ClassroomManagementScreen(
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                canEdit = false,
                                topContentPadding = 72.dp,
                                onFullScreenOverlayChange = { isClassroomSubOverlayOpen = it },
                            )
                            AnimatedVisibility(
                                visible = !isClassroomSubOverlayOpen,
                                enter = fadeIn(tween(180)),
                                exit = fadeOut(tween(150)),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .zIndex(15f),
                            ) {
                                FloatingFadeTopHeader(
                                    title = "Classrooms & Labs",
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    textColor = textColor,
                                    hindSiliguri = hindSiliguri,
                                    onCloseClick = { goBack() },
                                    onAiClick = { navigateTo("ai") },
                                    onNotificationClick = { navigateTo("notification") },
                                )
                            }
                        }
                    }
                    "blood_finder" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            BloodFinderScreen(
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                topContentPadding = 72.dp,
                                isGuest = true,
                                onSignInClick = onSwitchToLogin,
                            )
                            FloatingFadeTopHeader(
                                title = "Blood Donor Finder",
                                isDark = isDark,
                                cardBg = cardBg,
                                textColor = textColor,
                                hindSiliguri = hindSiliguri,
                                onCloseClick = { goBack() },
                                onAiClick = { navigateTo("ai") },
                                onNotificationClick = { navigateTo("notification") },
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
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
                                userDisplayName = currentUserName,
                            )
                        }
                    }
                }
            }

            SwipeDragWindowOverlay(
                controller = dragController,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
            )
        }
    }
}
