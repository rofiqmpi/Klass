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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.mpi.klass.theme.DownloadIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.FeatureAccent
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.LocationDotIcon
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
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    topContentPadding: Dp = 0.dp,
) {
    val currentLang = LocalAppLanguage.current
    val academicTiles = remember(role, isDark, currentLang) {
        buildList {
            if (role == UserRole.ADMIN) {
                add(
                    MenuTileSpec(
                        id = "routine_manager",
                        title = "Zed engine".localized(currentLang),
                        subtitle = "Generate, edit & publish schedules".localized(currentLang),
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
                        title = "Teaching Subjects".localized(currentLang),
                        subtitle = "Department, shift & subject choices".localized(currentLang),
                        icon = GraduationCapIcon,
                        accent = KlassFeaturePalette.teaching(isDark),
                        badgeText = "Setup",
                        onClick = onTeacherPreferencesClick,
                    )
                )
            }
            add(
                MenuTileSpec(
                    id = "books",
                    title = "Books library".localized(currentLang),
                    subtitle = "Subject syllabus, codes & credits".localized(currentLang),
                    icon = BookIcon,
                    accent = KlassFeaturePalette.books(isDark),
                    onClick = onBooksClick,
                )
            )
            if (role != null) {
                add(
                    MenuTileSpec(
                        id = "teachers",
                        title = "Teachers".localized(currentLang),
                        subtitle = "Faculty members & teaching subjects".localized(currentLang),
                        icon = GraduationCapIcon,
                        accent = KlassFeaturePalette.teachers(isDark),
                        onClick = onTeachersClick,
                    )
                )
            }
            add(
                MenuTileSpec(
                    id = "classrooms",
                    title = "Classrooms".localized(currentLang),
                    subtitle = "Room schedule & live availability".localized(currentLang),
                    icon = DoorOpenIcon,
                    accent = KlassFeaturePalette.classrooms(isDark),
                    onClick = onClassroomClick,
                )
            )
        }
    }

    val campusServicesTiles = remember(isDark, role, currentLang) {
        buildList {
            add(
                MenuTileSpec(
                    id = "notices",
                    title = "Notifications".localized(currentLang),
                    subtitle = "Class reminders & campus updates".localized(currentLang),
                    icon = NotificationIcon,
                    accent = KlassFeaturePalette.notifications(isDark),
                    onClick = onNoticeClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "blood_finder",
                    title = "Blood finder".localized(currentLang),
                    subtitle = "Verified campus donor directory".localized(currentLang),
                    icon = DropletIcon,
                    accent = KlassFeaturePalette.blood(isDark),
                    badgeText = "SOS",
                    onClick = onBloodFinderClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "ai_assistant",
                    title = "Zed ai".localized(currentLang),
                    subtitle = "Smart campus knowledge & A2UI actions".localized(currentLang),
                    icon = SparkleIcon,
                    accent = KlassFeaturePalette.ai(isDark),
                    badgeText = "ZED",
                    onClick = onAiClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "profile",
                    title = "Profile".localized(currentLang),
                    subtitle = "Academic info & profile overview".localized(currentLang),
                    icon = PersonIcon,
                    accent = KlassFeaturePalette.profile(isDark),
                    onClick = onProfileClick,
                )
            )
        }
    }

    val adminTiles = remember(role, isDark, currentLang) {
        if (role != UserRole.ADMIN) {
            emptyList()
        } else {
            listOf(
                MenuTileSpec(
                    id = "admin_signin_control",
                    title = "Sign-in Controls".localized(currentLang),
                    subtitle = "Role sign-in switches & access rules".localized(currentLang),
                    icon = ShieldIcon,
                    accent = KlassFeaturePalette.admin(isDark),
                    onClick = onSignInControlClick,
                ),
                MenuTileSpec(
                    id = "admin_approvals",
                    title = "Admin Approvals".localized(currentLang),
                    subtitle = "Review pending admin requests".localized(currentLang),
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
                    title = "Academic & Schedule".localized(),
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
                    title = "Campus & Tools".localized(),
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
                        title = "Administration".localized(),
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
                    title = "Preferences".localized(),
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
                    title = "Account",
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
                            title = "Sign Out",
                            subtitle = "Sign out of your current account",
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
                            title = "Delete Account",
                            subtitle = "Permanently remove all account data",
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
                            title = "Sign In",
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
    val currentLang = LocalAppLanguage.current
    val appearanceAccent = KlassFeaturePalette.appearance(isDark)

    val options = remember(isDark) {
        listOf(
            Triple(ThemeMode.LIGHT, "Light Mode" to "Clean and bright color theme", LightModeIcon to KlassFeaturePalette.books(isDark)),
            Triple(ThemeMode.DARK, "Dark Mode" to "Comfortable dark theme for low light", DarkModeIcon to KlassFeaturePalette.profile(isDark)),
            Triple(ThemeMode.DEFAULT, "System Default" to "Follow your device theme setting", AutoModeIcon to KlassFeaturePalette.teaching(isDark)),
        )
    }
    val activeThemeName = when (currentThemeMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.DEFAULT -> "System"
    }

    MinimalFullScreenSheet(
        title = "Appearance",
        subtitle = "Active: $activeThemeName",
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
        title = "Sign-in Controls".localized(),
        subtitle = "Role access switches".localized(),
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
                    title = "Role Switches".localized(),
                    accent = adminAccent.primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    SignInToggleRow(
                        title = "Admin Sign-in".localized(),
                        subtitle = "Requires approved admin account".localized(),
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
                        title = "Teacher Sign-in".localized(),
                        subtitle = "Faculty routine & preference setup".localized(),
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
                        title = "Student Sign-in".localized(),
                        subtitle = "Personal class routine & push alerts".localized(),
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
            title = "Admin Approvals".localized(),
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
                    contentDescription = "Refresh".localized(),
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
                            title = "No Pending Requests".localized(),
                            subtitle = "All admin verification requests have been reviewed.".localized(),
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
                            title = "Tap applicant to inspect & decide".localized(),
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
                                label = "Applicant Email".localized(),
                                value = activeItem.email,
                                icon = ShieldIcon,
                                accent = booksAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Permissions Granted on Approval".localized(),
                                value = "Zed engine, Books library, Teachers & Sign-in Controls",
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

@Composable
fun AppUpdateManagerSubScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val currentLang = LocalAppLanguage.current
    val updateAccent = KlassFeaturePalette.success(isDark)
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(true) }
    var latestVersion by remember { mutableStateOf("") }
    var apkUrl by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val loaded = RoleConfig.fetchAppUpdateConfig()
            enabled = loaded.enabled
            latestVersion = loaded.latestVersion
            apkUrl = loaded.apkUrl
            notes = loaded.notes
        } finally {
            isLoading = false
        }
    }

    fun saveConfig() {
        val cleanVersion = latestVersion.trim().removePrefix("v").removePrefix("V").trim()
        val normalizedUrl = KlassUpdateUtils.normalizeApkDownloadUrl(apkUrl)
        if (enabled && (cleanVersion.isBlank() || normalizedUrl.isBlank())) {
            showToast("ভার্সন এবং APK ডাউনলোড লিংক উভয়ই দিন।".localized(currentLang))
            return
        }
        if (normalizedUrl.isNotBlank() && !normalizedUrl.startsWith("https://", ignoreCase = true)) {
            showToast("সঠিক https:// APK ডাউনলোড লিংক দিন।".localized(currentLang))
            return
        }
        isSaving = true
        coroutineScope.launch {
            try {
                val res = RoleConfig.saveAppUpdateConfig(
                    AppUpdateConfig(
                        enabled = enabled,
                        latestVersion = cleanVersion,
                        apkUrl = normalizedUrl,
                        notes = notes.trim(),
                        force = true,
                    ),
                )
                if (res.isSuccess) {
                    apkUrl = normalizedUrl
                    latestVersion = cleanVersion
                    showToast("অ্যাপ আপডেট কনফিগ সফলভাবে সেভ হয়েছে!".localized(currentLang))
                } else {
                    showToast(
                        res.exceptionOrNull()?.localizedMessage
                            ?: "Firestore-এ সেভ করা যায়নি।".localized(currentLang),
                    )
                }
            } finally {
                isSaving = false
            }
        }
    }

    MinimalFullScreenSheet(
        title = "App Updates".localized(),
        subtitle = KlassRuntimeConfig.installedVersionName.ifBlank { null }?.let { "Installed: v$it" }
            ?: "Firebase remote update feed",
        levelLabel = "Menu › Admin",
        icon = DownloadIcon,
        accent = updateAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onBack,
        closeTestTag = "app_update_manager_back_button",
        headerActions = {
            HeaderSavePillButton(
                text = "Publish".localized(),
                accent = updateAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                isLoading = isSaving,
                enabled = !isLoading && !isSaving,
                onClick = { saveConfig() },
            )
        },
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
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                GoogleSectionHeader(
                    title = "Private Repo Safe Update Channel".localized(),
                    accent = updateAccent.primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    SignInToggleRow(
                        title = "Enable In-App Update".localized(),
                        subtitle = "Prompt users automatically when a newer version is set".localized(),
                        icon = DownloadIcon,
                        accent = updateAccent.primary,
                        isDark = isDark,
                        checked = enabled,
                        enabled = !isSaving,
                        hindSiliguri = hindSiliguri,
                        onToggle = { enabled = it },
                    )
                    SocialInsetDivider(startIndent = 72.dp)
                    SignInToggleRow(
                        title = "বাধ্যতামূলক আপডেট".localized(),
                        subtitle = "ব্যবহার চালিয়ে যেতে আপডেট ইনস্টল করতে হবে। এড়িয়ে যাওয়ার সুযোগ নেই।".localized(),
                        icon = ShieldIcon,
                        accent = KlassFeaturePalette.admin(isDark).primary,
                        isDark = isDark,
                        checked = true,
                        enabled = false,
                        hindSiliguri = hindSiliguri,
                        onToggle = {},
                    )
                }

                SocialSectionDividerBand(isDark = isDark, height = 6.dp)
                GoogleSectionHeader(
                    title = "Release Details (Firestore)".localized(),
                    accent = updateAccent.primary,
                    hindSiliguri = hindSiliguri,
                )
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = latestVersion,
                            onValueChange = { latestVersion = it },
                            label = { Text("Latest Version (e.g. 1.0.12)".localized(), fontFamily = hindSiliguri) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = updateAccent.primary,
                                focusedLabelColor = updateAccent.primary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_update_version_input"),
                        )

                        OutlinedTextField(
                            value = apkUrl,
                            onValueChange = { apkUrl = it },
                            label = { Text("Public APK URL (Public Releases Repo / Drive)".localized(), fontFamily = hindSiliguri) },
                            placeholder = { Text("https://github.com/user/Klass-Releases/releases/download/v1.0.12/Klass-release.apk".localized(), fontSize = 12.sp) },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = updateAccent.primary,
                                focusedLabelColor = updateAccent.primary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_update_url_input"),
                        )

                        Text(
                            text = "নোট: প্রাইভেট GitHub রিপোর লিংক ইউজারদের ফোনে কাজ করে না। কোড প্রাইভেট রেখেই একটি পাবলিক শুধু-রিলিজ রিপো (যেমন Klass-Releases) অথবা Google Drive ('Anyone with link')-এ APK দিয়ে লিংকটি এখানে দিন।".localized(),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontFamily = hindSiliguri,
                            color = cs.onSurfaceVariant,
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("What's New (Optional)".localized(), fontFamily = hindSiliguri) },
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = updateAccent.primary,
                                focusedLabelColor = updateAccent.primary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_update_notes_input"),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            KlassTonalButton(
                                text = "Test Update Check".localized(),
                                accent = KlassFeaturePalette.routine(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
                                onClick = { KlassRuntimeConfig.onManualCheckForUpdate?.invoke() },
                            )
                        }
                    }
                }
            }
        }
    }
}
