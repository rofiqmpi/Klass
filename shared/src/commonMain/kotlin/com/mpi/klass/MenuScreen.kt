package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.FeatureAccent
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.LoginIcon
import com.mpi.klass.theme.LogoutIcon
import com.mpi.klass.theme.MessageBubbleIcon
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.PaletteIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.TelegramAnimatedIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.UsersGroupIcon
import kotlinx.coroutines.launch

private data class MenuTileSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: FeatureAccent,
    val badgeText: String? = null,
    val onClick: () -> Unit,
)

@Composable
fun MenuScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    role: UserRole? = null,
    currentThemeMode: ThemeMode = ThemeMode.DEFAULT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onNoticeClick: () -> Unit = {},
    onClassroomClick: () -> Unit = {},
    onBooksClick: () -> Unit = {},
    onTeachersClick: () -> Unit = {},
    onRoutineManagerClick: () -> Unit = {},
    onTeacherPreferencesClick: () -> Unit = {},
    onBloodFinderClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onSignInControlClick: () -> Unit = {},
    onAdminApprovalsClick: () -> Unit = {},
    unreadMessagesCount: Int = 0,
    incomingRequestsCount: Int = 0,
    onMessagesClick: () -> Unit = {},
    onConnectionsClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    topContentPadding: Dp = 0.dp,
) {
    val academicTiles = remember(role, isDark) {
        buildList {
            if (role == UserRole.ADMIN) {
                add(
                    MenuTileSpec(
                        id = "routine_manager",
                        title = "Routine Engine",
                        subtitle = "Generate, edit & publish schedules",
                        icon = CalendarDaysIcon,
                        accent = KlassFeaturePalette.routine(isDark),
                        badgeText = "Engine",
                        onClick = onRoutineManagerClick,
                    )
                )
            } else if (role == UserRole.TEACHER) {
                add(
                    MenuTileSpec(
                        id = "teacher_prefs",
                        title = "Teaching Subjects",
                        subtitle = "Department, shift & subject choices",
                        icon = GraduationCapIcon,
                        accent = KlassFeaturePalette.teaching(isDark),
                        badgeText = "Setup",
                        onClick = onTeacherPreferencesClick,
                    )
                )
            }
            if (role != null) {
                add(
                    MenuTileSpec(
                        id = "books",
                        title = "Books Library",
                        subtitle = "Subject syllabus, codes & credits",
                        icon = BookIcon,
                        accent = KlassFeaturePalette.books(isDark),
                        onClick = onBooksClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "teachers",
                        title = "Teachers Directory",
                        subtitle = "Faculty members & contact info",
                        icon = GraduationCapIcon,
                        accent = KlassFeaturePalette.teachers(isDark),
                        onClick = onTeachersClick,
                    )
                )
            }
            add(
                MenuTileSpec(
                    id = "classrooms",
                    title = "Classrooms & Labs",
                    subtitle = "Room schedule & live availability",
                    icon = DoorOpenIcon,
                    accent = KlassFeaturePalette.classrooms(isDark),
                    onClick = onClassroomClick,
                )
            )
        }
    }

    val currentLang = LocalAppLanguage.current
    val campusServicesTiles = remember(isDark, role, unreadMessagesCount, incomingRequestsCount, currentLang) {
        buildList {
            if (role != null) {
                add(
                    MenuTileSpec(
                        id = "messages",
                        title = "মেসেজ".localized(currentLang),
                        subtitle = "1-on-1 messenger & Zed AI chat",
                        icon = MessageBubbleIcon,
                        accent = KlassFeaturePalette.routine(isDark),
                        badgeText = if (unreadMessagesCount > 0) "$unreadMessagesCount" else null,
                        onClick = onMessagesClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "friends",
                        title = "বন্ধু ও কানেকশন".localized(currentLang),
                        subtitle = "Friend requests & campus directory",
                        icon = UsersGroupIcon,
                        accent = KlassFeaturePalette.profile(isDark),
                        badgeText = if (incomingRequestsCount > 0) "$incomingRequestsCount" else null,
                        onClick = onConnectionsClick,
                    )
                )
            }
            add(
                MenuTileSpec(
                    id = "notices",
                    title = "Class Push Alerts",
                    subtitle = "Automatic pre-class reminders",
                    icon = NotificationIcon,
                    accent = KlassFeaturePalette.notifications(isDark),
                    onClick = onNoticeClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "blood_finder",
                    title = "Blood Finder",
                    subtitle = "Verified campus donor directory",
                    icon = DropletIcon,
                    accent = KlassFeaturePalette.blood(isDark),
                    badgeText = "SOS",
                    onClick = onBloodFinderClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "ai_assistant",
                    title = "Zed AI",
                    subtitle = "Books, teachers, mpi.politech & web search",
                    icon = SparkleIcon,
                    accent = KlassFeaturePalette.ai(isDark),
                    badgeText = "ZED",
                    onClick = onAiClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "profile",
                    title = "Profile",
                    subtitle = "Academic info & profile overview",
                    icon = PersonIcon,
                    accent = KlassFeaturePalette.profile(isDark),
                    onClick = onProfileClick,
                )
            )
        }
    }

    val adminTiles = remember(role, isDark) {
        if (role != UserRole.ADMIN) {
            emptyList()
        } else {
            listOf(
                MenuTileSpec(
                    id = "admin_signin_control",
                    title = "Sign-in Controls",
                    subtitle = "Role sign-in switches & access rules",
                    icon = ShieldIcon,
                    accent = KlassFeaturePalette.admin(isDark),
                    onClick = onSignInControlClick,
                ),
                MenuTileSpec(
                    id = "admin_approvals",
                    title = "Admin Approvals",
                    subtitle = "Review pending admin requests",
                    icon = CrownIcon,
                    accent = KlassFeaturePalette.books(isDark),
                    onClick = onAdminApprovalsClick,
                ),
            )
        }
    }

    val themeLabel = when (currentThemeMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.DEFAULT -> "System"
    }

    MinimalBackdrop(isDark = isDark) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                }

                GoogleSectionHeader(
                    title = "Academic & Schedule",
                    accent = KlassFeaturePalette.routine(isDark).primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    academicTiles.forEachIndexed { index, tile ->
                        SocialMenuRow(tile = tile, isDark = isDark, hindSiliguri = hindSiliguri)
                        if (index < academicTiles.lastIndex) {
                            SocialInsetDivider(startIndent = 72.dp)
                        }
                    }
                }

                SocialSectionDividerBand(isDark = isDark, height = 6.dp)
                GoogleSectionHeader(
                    title = "Campus & Tools",
                    accent = KlassFeaturePalette.ai(isDark).primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    campusServicesTiles.forEachIndexed { index, tile ->
                        SocialMenuRow(tile = tile, isDark = isDark, hindSiliguri = hindSiliguri)
                        if (index < campusServicesTiles.lastIndex) {
                            SocialInsetDivider(startIndent = 72.dp)
                        }
                    }
                }

                if (adminTiles.isNotEmpty()) {
                    SocialSectionDividerBand(isDark = isDark, height = 6.dp)
                    GoogleSectionHeader(
                        title = "Administration",
                        accent = KlassFeaturePalette.admin(isDark).primary,
                        hindSiliguri = hindSiliguri,
                    )
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        adminTiles.forEachIndexed { index, tile ->
                            SocialMenuRow(tile = tile, isDark = isDark, hindSiliguri = hindSiliguri)
                            if (index < adminTiles.lastIndex) {
                                SocialInsetDivider(startIndent = 72.dp)
                            }
                        }
                    }
                }

                SocialSectionDividerBand(isDark = isDark, height = 6.dp)
                GoogleSectionHeader(
                    title = "Preferences",
                    accent = KlassFeaturePalette.appearance(isDark).primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    SocialMenuRow(
                        tile = MenuTileSpec(
                            id = "appearance",
                            title = "Appearance",
                            subtitle = "Theme mode & color preview",
                            icon = PaletteIcon,
                            accent = KlassFeaturePalette.appearance(isDark),
                            badgeText = themeLabel,
                            onClick = onAppearanceClick,
                        ),
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }

                SocialSectionDividerBand(isDark = isDark, height = 6.dp)
                GoogleSectionHeader(
                    title = "অ্যাকাউন্ট".localized(),
                    accent = if (role != null) {
                        KlassFeaturePalette.notifications(isDark).primary
                    } else {
                        KlassFeaturePalette.profile(isDark).primary
                    },
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    if (role != null) {
                        KlassNavigationRow(
                            title = "লগআউট করুন".localized(),
                            subtitle = "বর্তমান অ্যাকাউন্ট থেকে বের হন".localized(),
                            icon = LogoutIcon,
                            accent = KlassFeaturePalette.notifications(isDark).primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            showChevron = false,
                            testTag = "menu_logout_button",
                            onClick = onSignOutClick,
                        )
                        SocialInsetDivider(startIndent = 72.dp)
                        KlassNavigationRow(
                            title = "ডিলিট একাউন্ট".localized(),
                            subtitle = "সকল তথ্য স্থায়ীভাবে মুছে ফেলুন".localized(),
                            icon = TrashCanIcon,
                            accent = KlassFeaturePalette.error(isDark).primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            showChevron = false,
                            testTag = "menu_delete_account_button",
                            onClick = onDeleteAccountClick,
                        )
                    } else {
                        KlassNavigationRow(
                            title = "সাইন ইন করুন".localized(),
                            subtitle = "Sign in to access your routine & campus connections",
                            icon = LoginIcon,
                            accent = KlassFeaturePalette.profile(isDark).primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            showChevron = true,
                            testTag = "menu_sign_in_button",
                            onClick = onSignInClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialMenuRow(
    tile: MenuTileSpec,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    KlassNavigationRow(
        title = tile.title,
        subtitle = tile.subtitle,
        icon = tile.icon,
        accent = tile.accent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        badgeText = tile.badgeText,
        badgeColor = tile.accent.primary,
        testTag = "menu_tile_${tile.id}",
        onClick = tile.onClick,
    )
}

@Composable
fun AppearanceSubScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appearanceAccent = KlassFeaturePalette.appearance(isDark)

    val options = remember(isDark) {
        listOf(
            Triple(ThemeMode.LIGHT, "Light Mode" to "Bright rainbow pastel canvas", LightModeIcon to KlassFeaturePalette.books(isDark)),
            Triple(ThemeMode.DARK, "Dark Mode" to "Deep aurora slate contrast", DarkModeIcon to KlassFeaturePalette.profile(isDark)),
            Triple(ThemeMode.DEFAULT, "System Default" to "Match device brightness setting", AutoModeIcon to KlassFeaturePalette.teaching(isDark)),
        )
    }

    MinimalFullScreenSheet(
        title = "Appearance",
        subtitle = "Active: ${currentThemeMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
        levelLabel = "Menu › Preferences",
        icon = PaletteIcon,
        accent = appearanceAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onBack,
        closeTestTag = "appearance_back_button",
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            GoogleSectionHeader(
                title = "Select Theme Mode",
                accent = appearanceAccent.primary,
                hindSiliguri = hindSiliguri,
            )
            SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                options.forEachIndexed { idx, (mode, texts, iconAndAccent) ->
                    val (label, desc) = texts
                    val (icon, optionAccent) = iconAndAccent
                    val isSelected = currentThemeMode == mode
                    Surface(
                        onClick = { onThemeModeChange(mode) },
                        color = if (isSelected) optionAccent.softTint else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appearance_mode_${mode.name.lowercase()}"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = optionAccent.softTint,
                                border = BorderStroke(0.75.dp, optionAccent.borderTint),
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    TelegramAnimatedIcon(
                                        icon = icon,
                                        contentDescription = label,
                                        tint = optionAccent.primary,
                                        size = 20.dp,
                                        triggerKey = if (isSelected) 1 else 0,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    fontSize = 16.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontFamily = hindSiliguri,
                                    color = if (isSelected) optionAccent.primary else cs.onSurface,
                                )
                                Text(
                                    text = desc,
                                    fontSize = 13.sp,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = optionAccent.primary),
                            )
                        }
                    }
                    if (idx < options.lastIndex) {
                        SocialInsetDivider(startIndent = 72.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SignInControlSubScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
) {
    val adminAccent = KlassFeaturePalette.admin(isDark)
    val coroutineScope = rememberCoroutineScope()
    var signInControl by remember { mutableStateOf(SignInControlConfig()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            signInControl = RoleConfig.fetchSignInControl()
        } finally {
            isLoading = false
        }
    }

    fun updateControl(updated: SignInControlConfig) {
        signInControl = updated
        isUpdating = true
        coroutineScope.launch {
            try {
                RoleConfig.saveSignInControl(updated)
            } finally {
                isUpdating = false
            }
        }
    }

    MinimalFullScreenSheet(
        title = "Sign-in Controls",
        subtitle = "Role access switches",
        levelLabel = "Menu › Admin",
        icon = ShieldIcon,
        accent = adminAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onBack,
        closeTestTag = "signin_control_back_button",
    ) {
        if (isLoading) {
            KlassHeroAndToggleListSkeleton(
                isDark = isDark,
                rowCount = 3,
                showSwitchPills = true,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                GoogleSectionHeader(
                    title = "Role Switches",
                    accent = adminAccent.primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    SignInToggleRow(
                        title = "Admin Sign-in",
                        subtitle = "Requires approved admin account",
                        icon = CrownIcon,
                        accent = adminAccent.primary,
                        isDark = isDark,
                        checked = signInControl.adminSignInEnabled,
                        enabled = !isUpdating,
                        hindSiliguri = hindSiliguri,
                        onToggle = { updateControl(signInControl.copy(adminSignInEnabled = it)) },
                    )
                    SocialInsetDivider(startIndent = 72.dp)
                    SignInToggleRow(
                        title = "Teacher Sign-in",
                        subtitle = "Faculty routine & preference setup",
                        icon = GraduationCapIcon,
                        accent = KlassFeaturePalette.teaching(isDark).primary,
                        isDark = isDark,
                        checked = signInControl.teacherSignInEnabled,
                        enabled = !isUpdating,
                        hindSiliguri = hindSiliguri,
                        onToggle = { updateControl(signInControl.copy(teacherSignInEnabled = it)) },
                    )
                    SocialInsetDivider(startIndent = 72.dp)
                    SignInToggleRow(
                        title = "Student Sign-in",
                        subtitle = "Personal class routine & push alerts",
                        icon = PersonIcon,
                        accent = KlassFeaturePalette.routine(isDark).primary,
                        isDark = isDark,
                        checked = signInControl.studentSignInEnabled,
                        enabled = !isUpdating,
                        hindSiliguri = hindSiliguri,
                        onToggle = { updateControl(signInControl.copy(studentSignInEnabled = it)) },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SignInToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    checked: Boolean,
    enabled: Boolean,
    hindSiliguri: FontFamily,
    onToggle: (Boolean) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = accent.copy(alpha = if (isDark) 0.22f else 0.12f),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                TelegramAnimatedIcon(
                    icon = icon,
                    contentDescription = null,
                    tint = accent,
                    size = 19.dp,
                    triggerKey = if (checked) 1 else 0,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = cs.onSurface,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontFamily = hindSiliguri,
                color = cs.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
            ),
        )
    }
}

@Composable
fun AdminApprovalsSubScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
) {
    val booksAccent = KlassFeaturePalette.books(isDark)
    val successAccent = KlassFeaturePalette.success(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)
    val coroutineScope = rememberCoroutineScope()
    var pendingRequests by remember { mutableStateOf<List<AdminRequestItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<AdminRequestItem?>(null) }
    var processingEmail by remember { mutableStateOf<String?>(null) }
    var processingAction by remember { mutableStateOf<String?>(null) }

    fun refreshRequests() {
        coroutineScope.launch {
            isLoading = true
            try {
                pendingRequests = RoleConfig.fetchPendingAdminRequestsFromFirestore()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshRequests()
    }

    fun handleDecision(item: AdminRequestItem, approve: Boolean) {
        processingEmail = item.email
        processingAction = if (approve) "APPROVE" else "DECLINE"
        coroutineScope.launch {
            try {
                RoleConfig.updateAdminRequestStatusInFirestore(
                    item.email,
                    if (approve) "APPROVED" else "DECLINED",
                )
                selectedItem = null
            } finally {
                processingEmail = null
                processingAction = null
                refreshRequests()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = "Admin Approvals",
            subtitle = "${pendingRequests.size} pending request(s)",
            levelLabel = "Menu › Admin",
            icon = CrownIcon,
            accent = booksAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onBack,
            closeTestTag = "admin_approvals_back_button",
            headerActions = {
                KlassHeaderIconButton(
                    icon = RefreshIcon,
                    contentDescription = "Refresh",
                    isDark = isDark,
                    visualSize = 38.dp,
                    isLoading = isLoading,
                    onClick = { refreshRequests() },
                )
            },
        ) {
            when {
                isLoading && pendingRequests.isEmpty() -> {
                    KlassRowListSkeleton(
                        isDark = isDark,
                        showHeaderBar = true,
                        showActionPill = true,
                    )
                }
                pendingRequests.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        KlassEmptyState(
                            icon = CrownIcon,
                            title = "No Pending Requests",
                            subtitle = "All admin verification requests have been reviewed.",
                            accent = booksAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            actionLabel = "Refresh",
                            onActionClick = { refreshRequests() },
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        GoogleSectionHeader(
                            title = "Tap applicant to inspect & decide",
                            accent = booksAccent.primary,
                            hindSiliguri = hindSiliguri,
                        )
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            pendingRequests.forEachIndexed { idx, item ->
                                KlassNavigationRow(
                                    title = item.name.ifBlank { "Admin Applicant" },
                                    subtitle = item.email,
                                    icon = CrownIcon,
                                    accent = booksAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    badgeText = "Pending",
                                    badgeColor = booksAccent.primary,
                                    onClick = { selectedItem = item },
                                )
                                if (idx < pendingRequests.lastIndex) {
                                    SocialInsetDivider(startIndent = 72.dp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Level 2: Applicant Detail & Decision Sub-Sub-Screen
        val activeItem = selectedItem
        KlassSubScreenHost(visible = activeItem != null, zIndex = 50f) {
            if (activeItem != null) {
                val isItemProcessing = processingEmail == activeItem.email
                MinimalFullScreenSheet(
                    title = activeItem.name.ifBlank { "Admin Applicant" },
                    subtitle = activeItem.email,
                    levelLabel = "Menu › Approvals › Review",
                    icon = CrownIcon,
                    accent = booksAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { if (!isItemProcessing) selectedItem = null },
                    dismissEnabled = !isItemProcessing,
                    headerActions = {
                        KlassTonalButton(
                            text = if (isItemProcessing && processingAction == "DECLINE") "..." else "Decline",
                            accent = errorAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            height = 36.dp,
                            enabled = processingEmail == null,
                            onClick = { handleDecision(activeItem, approve = false) },
                        )
                        HeaderSavePillButton(
                            text = if (isItemProcessing && processingAction == "APPROVE") "..." else "Approve",
                            accent = successAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            isLoading = isItemProcessing && processingAction == "APPROVE",
                            enabled = processingEmail == null,
                            onClick = { handleDecision(activeItem, approve = true) },
                        )
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            KlassDetailFieldRow(
                                label = "Applicant Email",
                                value = activeItem.email,
                                icon = ShieldIcon,
                                accent = booksAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Permissions Granted on Approval",
                                value = "Routine Engine, Books, Teachers Directory & Sign-in Controls",
                                icon = InfoCircleIcon,
                                accent = successAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                        }
                    }
                }
            }
        }
    }
}
