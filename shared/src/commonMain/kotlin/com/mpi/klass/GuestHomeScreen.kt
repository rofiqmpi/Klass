package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.UsersGroupIcon
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
    var isRoutineLoading by remember { mutableStateOf(true) }
    var tempRoutineFilter by remember { mutableStateOf<RoutineQuickFilter?>(null) }
    var isRoutineFilterExpanded by remember { mutableStateOf(false) }
    var isDownloadingRoutine by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isClassroomSubOverlayOpen by remember { mutableStateOf(false) }
    var isNotificationsSubOverlayOpen by remember { mutableStateOf(false) }
    var isBloodFinderSubOverlayOpen by remember { mutableStateOf(false) }
    var isProfileSubOverlayOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isRoutineLoading = true
        try {
            publishedPlan = fetchRoutinePlanFromFirestore("master_6m_published")
        } finally {
            isRoutineLoading = false
        }
    }

    val hindSiliguri = getHindSiliguriFontFamily()
    val tabBackStack = remember { mutableStateListOf("home") }
    val selectedTab = tabBackStack.lastOrNull() ?: "home"

    LaunchedEffect(publishedPlan, selectedTab) {
        val permissionGranted = hasNotificationPermission()
        val actionIds = PushScheduleMath.activeActionNotificationIds(
            role = null,
            studentProfile = null,
            teacherPref = null,
            publishedPlan = publishedPlan,
            permissionGranted = permissionGranted,
        )
        NotificationSeenStore.setActiveIds(actionIds)
    }

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
                    beyondViewportPageCount = 1,
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

                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Spacer(
                                        modifier = Modifier
                                            .statusBarsPadding()
                                            .height(56.dp),
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

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isRoutineFilterExpanded,
                                    enter = fadeIn(tween(80)),
                                    exit = fadeOut(tween(110)),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .zIndex(15f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = if (isDark) 0.34f else 0.14f))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { isRoutineFilterExpanded = false },
                                            ),
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.TopCenter)
                                        .zIndex(25f),
                                ) {
                                    HomeRoutineHeader(
                                        isDark = isDark,
                                        cardBg = cardBg,
                                        photoUrl = currentPhotoUrl,
                                        avatarSeed = guestSeed,
                                        onProfileClick = {
                                            isRoutineFilterExpanded = false
                                            navigateTo("profile")
                                        },
                                        onAiClick = {
                                            isRoutineFilterExpanded = false
                                            navigateTo("messenger")
                                        },
                                        onNotificationClick = {
                                            isRoutineFilterExpanded = false
                                            navigateTo("notification")
                                        },
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
                                            onAiClick = { navigateTo("messenger") },
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
                                        onMessagesClick = { navigateTo("messenger") },
                                        onSignInClick = onSwitchToLogin,
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
                                            onAiClick = { navigateTo("messenger") },
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
                                            onAiClick = { navigateTo("messenger") },
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
                                            onAiClick = { navigateTo("messenger") },
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
                                            onAiClick = { navigateTo("messenger") },
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
                    "messenger" -> {
                        val cs = MaterialTheme.colorScheme
                        val aiAccent = KlassFeaturePalette.ai(isDark)
                        val fbBlue = Color(0xFF0866FF)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .rainbowScreenBackground(isDark)
                                .navigationBarsPadding(),
                        ) {
                            GoogleTopAppBar(
                                title = "মেসেজ".localized(),
                                hindSiliguri = hindSiliguri,
                                onNavigationClick = { goBack() },
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                GoogleSectionHeader(
                                    title = "AI Assistant",
                                    accent = aiAccent.primary,
                                    hindSiliguri = hindSiliguri,
                                )
                                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { navigateTo("ai") }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                            .testTag("guest_messenger_zed_ai_row"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = aiAccent.softTint,
                                            border = BorderStroke(1.dp, aiAccent.borderTint),
                                            modifier = Modifier.size(48.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = SparkleIcon,
                                                    contentDescription = "Zed AI",
                                                    tint = aiAccent.primary,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Text(
                                                    text = "Zed AI",
                                                    color = cs.onSurface,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = hindSiliguri,
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = aiAccent.softTint,
                                                    border = BorderStroke(0.5.dp, aiAccent.borderTint),
                                                ) {
                                                    Text(
                                                        text = "AI",
                                                        color = aiAccent.primary,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = hindSiliguri,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Ask Zed AI 1-to-1 about routines, books & MPI campus",
                                                color = cs.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                fontFamily = hindSiliguri,
                                                maxLines = 1,
                                            )
                                        }
                                        Icon(
                                            imageVector = ChevronRightIcon,
                                            contentDescription = null,
                                            tint = cs.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    KlassEmptyState(
                                        icon = UsersGroupIcon,
                                        title = "সবাইকে খুঁজুন".localized(),
                                        subtitle = "Sign in to chat 1-to-1 with MPI students & teachers.",
                                        accent = fbBlue,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        actionLabel = "সাইন ইন করুন".localized(),
                                        onActionClick = onSwitchToLogin,
                                    )
                                }
                            }
                        }
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
