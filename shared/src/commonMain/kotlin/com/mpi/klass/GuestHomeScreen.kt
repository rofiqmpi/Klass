package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.SparkleIcon
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.getHindSiliguriFontFamily
import androidx.compose.runtime.mutableStateListOf
import coil3.compose.AsyncImage
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.home
import klass.shared.generated.resources.menu
import klass.shared.generated.resources.notification
import klass.shared.generated.resources.welcome
import org.jetbrains.compose.resources.painterResource

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
    val guestSeed = remember { initialSession?.email?.takeIf { it.isNotBlank() && it.startsWith("guest_") } ?: ("guest_" + (100000..999999).random()) }
    var currentUserName by remember { mutableStateOf(initialSession?.name?.ifBlank { "গেস্ট ইউজার" } ?: "গেস্ট ইউজার") }
    var currentPhotoUrl by remember { mutableStateOf(initialSession?.photoUrl) }

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
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

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
        // Base Layer: Only Home <-> Menu swipe left/right together with their headers & bottom bar
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = overlayTab == null,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> {
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
                                currentThemeMode = currentThemeMode,
                                onThemeModeChange = onThemeModeChange,
                                onProfileClick = { navigateTo("profile") },
                                onAiClick = { navigateTo("ai") },
                                onNoticeClick = { navigateTo("notification") },
                                topContentPadding = 72.dp,
                            )
                            FloatingFadeTopHeader(
                                title = "মেনু".localized(),
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

        // Secondary Overlay Layer (Profile, Notification, AI) with fade in / fade out (no horizontal swipe)
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
                            title = "প্রোফাইল".localized(),
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
                        NotificationScreen(isDark = isDark, hindSiliguri = hindSiliguri)
                        FloatingFadeTopHeader(
                            title = "নোটিফিকেশন".localized(),
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

        // Top-most floating screen-window morph overlay above ALL UI layers
        SwipeDragWindowOverlay(
            controller = dragController,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
        )
    }
    }
}



