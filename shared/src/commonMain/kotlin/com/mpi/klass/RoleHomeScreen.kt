package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag
import com.mpi.klass.theme.SparkleIcon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

    LaunchedEffect(email) {
        if (email.isNotBlank()) {
            val prof = RoleConfig.fetchUserProfile(email)
            if (prof != null) {
                UserRole.fromRaw(prof.category)?.let {
                    currentRole = it
                }
            }
        }
    }

    val systemInDark = isSystemInDarkTheme()
    val isDark = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    val hindSiliguri = getHindSiliguriFontFamily()
    val tabBackStack = remember { mutableStateListOf(initialTab) }
    val selectedTab = tabBackStack.lastOrNull() ?: "home"
    var lastBackPressTime by remember { mutableStateOf(0L) }

    fun navigateTo(tab: String) {
        if (selectedTab != tab) {
            tabBackStack.add(tab)
        }
    }

    fun goBack() {
        if (tabBackStack.size > 1) {
            tabBackStack.removeAt(tabBackStack.lastIndex)
        } else {
            // At root "home" tab - double back to exit
            val now = getCurrentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                onCloseApp()
            } else {
                lastBackPressTime = now
                showToast("অ্যাপ থেকে বের হতে আবার ব্যাক প্রেস করুন")
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
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

    AppBackHandler(enabled = true) {
        goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val darkerSkyBlue = Color(0xFF5B9BD5)
            val headerGradient = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    darkerSkyBlue,
                ),
            )
            val transparentBrush = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

            // Header
            if (selectedTab != "ai") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Transparent,
                    shadowElevation = if (selectedTab == "home") 6.dp else 0.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selectedTab == "home") headerGradient else transparentBrush),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            when (selectedTab) {
                                "home" -> {
                                    // Left Side: Profile Button & Separated Bar with a subtle gap
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            onClick = { navigateTo("profile") },
                                            shape = CircleShape,
                                            color = cardBg,
                                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.size(50.dp).clip(CircleShape),
                                            shadowElevation = 5.dp,
                                        ) {
                                            if (!currentPhotoUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = currentPhotoUrl,
                                                    contentDescription = "Profile Photo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop,
                                                )
                                            } else {
                                                DynamicAvatar(
                                                    seed = email.ifBlank { currentUserName },
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            onClick = { },
                                            shape = CircleShape,
                                            color = cardBg,
                                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            modifier = Modifier
                                                .height(40.dp)
                                                .width(160.dp),
                                            shadowElevation = 2.dp,
                                        ) {}
                                    }
                                }
                                "menu" -> {
                                    // Menu Screen Top Bar: Close button + "মেনু" text on left
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            onClick = { goBack() },
                                            shape = CircleShape,
                                            color = cardBg,
                                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.size(50.dp),
                                            shadowElevation = 4.dp,
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = CloseIcon,
                                                    contentDescription = "বন্ধ করুন",
                                                    tint = if (isDark) Color.White else Color.Black,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "মেনু",
                                            fontSize = 20.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                        )
                                    }
                                }
                                "profile" -> {
                                    // Profile Screen Top Bar: Close button + "প্রোফাইল" text on left
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            onClick = { goBack() },
                                            shape = CircleShape,
                                            color = cardBg,
                                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.size(50.dp),
                                            shadowElevation = 4.dp,
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = CloseIcon,
                                                    contentDescription = "বন্ধ করুন",
                                                    tint = if (isDark) Color.White else Color.Black,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "প্রোফাইল",
                                            fontSize = 20.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                        )
                                    }
                                }
                                "notification" -> {
                                    Text(
                                        text = "নোটিফিকেশন",
                                        fontSize = 20.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                    )
                                }
                            }

                            // Right Side: AI Assistant Button + Notification/Close Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (selectedTab != "ai") {
                                    Surface(
                                        onClick = { navigateTo("ai") },
                                        shape = CircleShape,
                                        color = cardBg,
                                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.size(50.dp),
                                        shadowElevation = 4.dp,
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = SparkleIcon,
                                                contentDescription = "AI সহকারী",
                                                tint = Color(0xFF8B5CF6),
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                }

                                if (selectedTab == "notification") {
                                    Surface(
                                        onClick = { goBack() },
                                        shape = CircleShape,
                                        color = cardBg,
                                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.size(50.dp),
                                        shadowElevation = 4.dp,
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = CloseIcon,
                                                contentDescription = "বন্ধ করুন",
                                                tint = if (isDark) Color.White else Color.Black,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        onClick = { navigateTo("notification") },
                                        shape = CircleShape,
                                        color = cardBg,
                                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.size(50.dp),
                                        shadowElevation = 4.dp,
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.notification),
                                                contentDescription = "নোটিফিকেশন",
                                                tint = if (isDark) Color.White else Color.Black,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom borderline (Visible ONLY on Home screen!)
                if (selectedTab == "home") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(if (isDark) Color.White else Color.Black),
                    )
                }
            }

            // Center Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (selectedTab) {
                    "home" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ClassRoutineTable(
                                hindSiliguri = hindSiliguri,
                                isDark = isDark,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    "menu" -> {
                        MenuScreen(
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            role = currentRole,
                            currentThemeMode = currentThemeMode,
                            onThemeModeChange = onThemeModeChange,
                            onProfileClick = { navigateTo("profile") },
                            onAiClick = { navigateTo("ai") },
                            onNoticeClick = { navigateTo("notification") },
                        )
                    }
                    "ai" -> {
                        AiAssistantScreen(
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onBack = { goBack() },
                            userDisplayName = currentUserName,
                        )
                    }
                    "notification" -> {
                        NotificationScreen(isDark = isDark, hindSiliguri = hindSiliguri)
                    }
                    "profile" -> {
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
                            onUpdateProfile = { newName, newPhotoUrl ->
                                currentUserName = newName
                                currentPhotoUrl = newPhotoUrl
                                val sessionEmail = email.ifBlank { loadUserSession()?.email ?: "" }
                                if (sessionEmail.isNotBlank()) {
                                    saveUserSession(
                                        UserSession(
                                            role = currentRole.name,
                                            email = sessionEmail,
                                            name = newName,
                                            photoUrl = newPhotoUrl,
                                        )
                                    )
                                    coroutineScope.launch {
                                        if (currentRole == UserRole.TEACHER) {
                                            RoleConfig.saveTeacherProfile(sessionEmail, newName, newPhotoUrl)
                                        } else if (currentRole == UserRole.ADMIN) {
                                            RoleConfig.saveAdminEmail(sessionEmail, newName, newPhotoUrl)
                                        } else if (currentRole == UserRole.STUDENT) {
                                            val existing = RoleConfig.fetchStudentProfile(sessionEmail)
                                            val studentProfileToSave = if (existing != null) {
                                                existing.copy(name = newName, photoUrl = newPhotoUrl)
                                            } else {
                                                StudentProfile(email = sessionEmail, name = newName, photoUrl = newPhotoUrl)
                                            }
                                            RoleConfig.saveStudentProfile(studentProfileToSave)
                                        }
                                        RoleConfig.saveUserProfile(sessionEmail, newName, newPhotoUrl, "google", currentRole.name)
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

            // Bottom Navigation Bar (Visible on "home" and "menu" screens)
            if ((selectedTab == "home") || (selectedTab == "menu")) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBg,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(if (isDark) Color.White else Color.Black),
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                        ) {
                            val homeInteractionSource = remember { MutableInteractionSource() }
                            val isHomePressed by homeInteractionSource.collectIsPressedAsState()

                            val menuInteractionSource = remember { MutableInteractionSource() }
                            val isMenuPressed by menuInteractionSource.collectIsPressedAsState()

                            val isHomeSelected = selectedTab == "home"
                            val isMenuSelected = selectedTab == "menu"

                            Row(
                                modifier = Modifier
                                    .offset(y = 3.dp)
                                    .fillMaxWidth()
                                    .height(63.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(63.dp)
                                        .clip(homeShape)
                                        .background(if (isHomeSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(63.dp)
                                        .clip(menuShape)
                                        .background(if (isMenuSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(63.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Button(
                                    onClick = { navigateTo("home") },
                                    interactionSource = homeInteractionSource,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(63.dp)
                                        .offset(y = if (isHomePressed) 3.dp else 0.dp),
                                    shape = homeShape,
                                    border = BorderStroke(0.5.dp, if (isHomeSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                                    contentPadding = PaddingValues(2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHomeSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                        contentColor = if (isDark) Color.White else Color.Black,
                                    ),
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.home),
                                            contentDescription = "রুটিন",
                                            tint = if (isDark) Color.White else Color.Black,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Text(
                                            text = "রুটিন",
                                            fontSize = 11.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color.Black,
                                        )
                                    }
                                }

                                Button(
                                    onClick = { navigateTo("menu") },
                                    interactionSource = menuInteractionSource,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(63.dp)
                                        .offset(y = if (isMenuPressed) 3.dp else 0.dp),
                                    shape = menuShape,
                                    border = BorderStroke(0.5.dp, if (isMenuSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isMenuSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                        contentColor = if (isDark) Color.White else Color.Black,
                                    ),
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.menu),
                                            contentDescription = "মেনু",
                                            tint = if (isDark) Color.White else Color.Black,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        Text(
                                            text = "মেনু",
                                            fontSize = 12.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color.Black,
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



