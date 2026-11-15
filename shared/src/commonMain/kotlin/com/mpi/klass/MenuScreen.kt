package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.PaletteIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.RightArrowIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.ThemeMode
import kotlinx.coroutines.launch

private data class MenuTileSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
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
    topContentPadding: Dp = 0.dp,
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val tiles = remember(role) {
        buildList {
            add(
                MenuTileSpec(
                    id = "profile",
                    title = "Profile",
                    subtitle = "Account & academic details",
                    icon = PersonIcon,
                    accent = Color(0xFF2563EB),
                    onClick = onProfileClick,
                )
            )
            if (role == UserRole.ADMIN) {
                add(
                    MenuTileSpec(
                        id = "routine_manager",
                        title = "Routine Engine",
                        subtitle = "Generate 6-month master schedule",
                        icon = CalendarDaysIcon,
                        accent = Color(0xFF0284C7),
                        onClick = onRoutineManagerClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "books",
                        title = "Books Library",
                        subtitle = "BTEB catalog & credits (Sem 1–7)",
                        icon = BookIcon,
                        accent = Color(0xFF059669),
                        onClick = onBooksClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "classrooms",
                        title = "Classrooms & Labs",
                        subtitle = "Rooms inventory & free slots",
                        icon = DoorOpenIcon,
                        accent = Color(0xFF7C3AED),
                        onClick = onClassroomClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "teachers",
                        title = "Teachers Directory",
                        subtitle = "Faculty profiles & departments",
                        icon = GraduationCapIcon,
                        accent = Color(0xFFD97706),
                        onClick = onTeachersClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "notices",
                        title = "Notice Board",
                        subtitle = "Broadcast & view announcements",
                        icon = NotificationIcon,
                        accent = Color(0xFFEF4444),
                        onClick = onNoticeClick,
                    )
                )
            } else {
                if (role == UserRole.TEACHER) {
                    add(
                        MenuTileSpec(
                            id = "teacher_prefs",
                            title = "Teaching Subjects",
                            subtitle = "Shift, hours & Sem 1–7 courses",
                            icon = GraduationCapIcon,
                            accent = Color(0xFF0284C7),
                            onClick = onTeacherPreferencesClick,
                        )
                    )
                }
                add(
                    MenuTileSpec(
                        id = "classrooms",
                        title = "Classrooms & Labs",
                        subtitle = "Room schedules & free slots",
                        icon = DoorOpenIcon,
                        accent = Color(0xFF7C3AED),
                        onClick = onClassroomClick,
                    )
                )
                add(
                    MenuTileSpec(
                        id = "notices",
                        title = "Notice Board",
                        subtitle = "Today's classes & announcements",
                        icon = NotificationIcon,
                        accent = Color(0xFFEF4444),
                        onClick = onNoticeClick,
                    )
                )
            }
            add(
                MenuTileSpec(
                    id = "blood_finder",
                    title = "Blood Finder",
                    subtitle = "Find donors by blood group",
                    icon = DropletIcon,
                    accent = Color(0xFFDC2626),
                    onClick = onBloodFinderClick,
                )
            )
            add(
                MenuTileSpec(
                    id = "ai_assistant",
                    title = "AI Study Assistant",
                    subtitle = "Ask questions & summarize notes",
                    icon = SparkleIcon,
                    accent = Color(0xFF8B5CF6),
                    onClick = onAiClick,
                )
            )
        }
    }

    MinimalBackdrop(isDark = isDark) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 900.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Responsive Iconic Navigation Grid (1 col compact phone / 2 cols >= 600dp)
                MinimalResponsiveGrid(
                    items = tiles,
                    twoColMinWidth = 600.dp,
                    spacing = 12.dp,
                ) { index, tile ->
                    AnimatedStaggeredItem(index = index) {
                        MinimalIconicMenuTile(
                            tile = tile,
                            isDark = isDark,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Admin Management Controls
                if (role == UserRole.ADMIN) {
                    Spacer(modifier = Modifier.height(14.dp))
                    AdminControlsSection(
                        isDark = isDark,
                        cardBg = cardBg,
                        borderCol = borderCol,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        hindSiliguri = hindSiliguri,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Minimal Expandable Appearance Card
                MinimalThemeCard(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    cardBg = cardBg,
                    borderCol = borderCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }
        }
    }
}

@Composable
private fun MinimalIconicMenuTile(
    tile: MenuTileSpec,
    isDark: Boolean,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
) {
    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(16.dp),
        onClick = tile.onClick,
        testTag = "menu_tile_${tile.id}",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tile.accent.copy(alpha = if (isDark) 0.16f else 0.10f),
                border = BorderStroke(1.dp, tile.accent.copy(alpha = 0.25f)),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = tile.title,
                        tint = tile.accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = tile.title,
                    fontSize = 15.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = tile.subtitle,
                    fontSize = 12.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    color = subTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(tile.accent.copy(alpha = if (isDark) 0.14f else 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = RightArrowIcon,
                    contentDescription = null,
                    tint = tile.accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun AdminControlsSection(
    isDark: Boolean,
    cardBg: Color,
    borderCol: Color,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
) {
    val coroutineScope = rememberCoroutineScope()
    var signInControl by remember { mutableStateOf(SignInControlConfig()) }
    var isUpdatingControl by remember { mutableStateOf(false) }
    var isSignInExpanded by remember { mutableStateOf(false) }

    var pendingRequests by remember { mutableStateOf<List<AdminRequestItem>>(emptyList()) }
    var isLoadingRequests by remember { mutableStateOf(false) }
    var isRequestsExpanded by remember { mutableStateOf(false) }
    var processingEmail by remember { mutableStateOf<String?>(null) }
    var processingAction by remember { mutableStateOf<String?>(null) }

    fun refreshRequests() {
        coroutineScope.launch {
            isLoadingRequests = true
            try {
                pendingRequests = RoleConfig.fetchPendingAdminRequestsFromFirestore()
            } finally {
                isLoadingRequests = false
            }
        }
    }

    LaunchedEffect(Unit) {
        signInControl = RoleConfig.fetchSignInControl()
        refreshRequests()
    }

    val amberAccent = Color(0xFFD97706)
    val violetAccent = Color(0xFF7C3AED)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 680.dp

        val signInCard: @Composable () -> Unit = {
            MinimalCardSurface(
                isDark = isDark,
                shape = RoundedCornerShape(16.dp),
                borderColor = if (isSignInExpanded) amberAccent.copy(alpha = 0.55f) else borderCol,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { isSignInExpanded = !isSignInExpanded },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = amberAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, amberAccent.copy(alpha = 0.25f)),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = ShieldIcon,
                                        contentDescription = null,
                                        tint = amberAccent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Sign-In Control",
                                        fontSize = 15.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                    )
                                    if (isUpdatingControl) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = amberAccent,
                                        )
                                    }
                                }
                                Text(
                                    text = "Manage Admin, Teacher & Student registration",
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    color = subTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            val arrowRot by animateFloatAsState(
                                targetValue = if (isSignInExpanded) 180f else 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "SignInArrow",
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(amberAccent.copy(alpha = if (isDark) 0.14f else 0.08f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = ChevronDownIcon,
                                    contentDescription = "Toggle sign-in controls",
                                    tint = amberAccent,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .graphicsLayer(rotationZ = arrowRot),
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isSignInExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(borderCol),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            ThreeDSignInControlRow(
                                title = "Admin Sign-In",
                                iconVector = CrownIcon,
                                iconTint = amberAccent,
                                isEnabled = signInControl.adminSignInEnabled,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                textColor = textColor,
                                onToggle = { newValue ->
                                    val updated = signInControl.copy(adminSignInEnabled = newValue)
                                    signInControl = updated
                                    isUpdatingControl = true
                                    coroutineScope.launch {
                                        try {
                                            RoleConfig.saveSignInControl(updated)
                                        } finally {
                                            isUpdatingControl = false
                                        }
                                    }
                                },
                            )
                            ThreeDSignInControlRow(
                                title = "Teacher Sign-In",
                                iconVector = GraduationCapIcon,
                                iconTint = Color(0xFF059669),
                                isEnabled = signInControl.teacherSignInEnabled,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                textColor = textColor,
                                onToggle = { newValue ->
                                    val updated = signInControl.copy(teacherSignInEnabled = newValue)
                                    signInControl = updated
                                    isUpdatingControl = true
                                    coroutineScope.launch {
                                        try {
                                            RoleConfig.saveSignInControl(updated)
                                        } finally {
                                            isUpdatingControl = false
                                        }
                                    }
                                },
                            )
                            ThreeDSignInControlRow(
                                title = "Student Sign-In",
                                iconVector = BookIcon,
                                iconTint = Color(0xFF0284C7),
                                isEnabled = signInControl.studentSignInEnabled,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                textColor = textColor,
                                onToggle = { newValue ->
                                    val updated = signInControl.copy(studentSignInEnabled = newValue)
                                    signInControl = updated
                                    isUpdatingControl = true
                                    coroutineScope.launch {
                                        try {
                                            RoleConfig.saveSignInControl(updated)
                                        } finally {
                                            isUpdatingControl = false
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        val requestsCard: @Composable () -> Unit = {
            MinimalCardSurface(
                isDark = isDark,
                shape = RoundedCornerShape(16.dp),
                borderColor = if (isRequestsExpanded) violetAccent.copy(alpha = 0.55f) else borderCol,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { isRequestsExpanded = !isRequestsExpanded },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = violetAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, violetAccent.copy(alpha = 0.25f)),
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CrownIcon,
                                        contentDescription = null,
                                        tint = violetAccent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Admin Approvals",
                                        fontSize = 15.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                    )
                                    if (pendingRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEF4444),
                                        ) {
                                            Text(
                                                text = "${pendingRequests.size}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    if (isLoadingRequests) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = violetAccent,
                                        )
                                    }
                                }
                                Text(
                                    text = if (pendingRequests.isNotEmpty()) {
                                        "${pendingRequests.size} request(s) awaiting review"
                                    } else {
                                        "No pending access requests"
                                    },
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    color = subTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            val arrowRot by animateFloatAsState(
                                targetValue = if (isRequestsExpanded) 180f else 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "RequestsArrow",
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(violetAccent.copy(alpha = if (isDark) 0.14f else 0.08f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = ChevronDownIcon,
                                    contentDescription = "Toggle admin approvals",
                                    tint = violetAccent,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .graphicsLayer(rotationZ = arrowRot),
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isRequestsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(borderCol),
                            )
                            if (pendingRequests.isEmpty()) {
                                Text(
                                    text = "All admin access requests are up to date.",
                                    fontSize = 12.5.sp,
                                    fontFamily = hindSiliguri,
                                    color = subTextColor,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            } else {
                                pendingRequests.forEach { item ->
                                    val isItemProcessing = processingEmail == item.email
                                    AdminRequestRowItem(
                                        item = item,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        textColor = textColor,
                                        subTextColor = subTextColor,
                                        isApproving = isItemProcessing && processingAction == "APPROVE",
                                        isDeclining = isItemProcessing && processingAction == "DECLINE",
                                        isDisabled = processingEmail != null,
                                        onApprove = {
                                            processingEmail = item.email
                                            processingAction = "APPROVE"
                                            coroutineScope.launch {
                                                try {
                                                    RoleConfig.updateAdminRequestStatusInFirestore(item.email, "APPROVED")
                                                } finally {
                                                    processingEmail = null
                                                    processingAction = null
                                                    refreshRequests()
                                                }
                                            }
                                        },
                                        onDecline = {
                                            processingEmail = item.email
                                            processingAction = "DECLINE"
                                            coroutineScope.launch {
                                                try {
                                                    RoleConfig.updateAdminRequestStatusInFirestore(item.email, "DECLINED")
                                                } finally {
                                                    processingEmail = null
                                                    processingAction = null
                                                    refreshRequests()
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) { signInCard() }
                Box(modifier = Modifier.weight(1f)) { requestsCard() }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                signInCard()
                requestsCard()
            }
        }
    }
}

@Composable
private fun MinimalThemeCard(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    cardBg: Color,
    borderCol: Color,
    textColor: Color,
    subTextColor: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val indigoAccent = Color(0xFF6366F1)

    val modeLabel: (ThemeMode) -> String = { mode ->
        when (mode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.DEFAULT -> "System"
        }
    }

    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(16.dp),
        borderColor = if (isExpanded) indigoAccent.copy(alpha = 0.55f) else borderCol,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { isExpanded = !isExpanded },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = indigoAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                        border = BorderStroke(1.dp, indigoAccent.copy(alpha = 0.25f)),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PaletteIcon,
                                contentDescription = null,
                                tint = indigoAccent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Appearance",
                            fontSize = 15.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Text(
                            text = "Active theme: ${modeLabel(currentThemeMode)}",
                            fontSize = 12.sp,
                            fontFamily = hindSiliguri,
                            color = subTextColor,
                        )
                    }

                    val arrowRotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "ThemeArrowRotation",
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(indigoAccent.copy(alpha = if (isDark) 0.14f else 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = ChevronDownIcon,
                            contentDescription = "Expand appearance options",
                            tint = indigoAccent,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer(rotationZ = arrowRotation),
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(borderCol),
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val isSelected = mode == currentThemeMode
                            val modeIcon = when (mode) {
                                ThemeMode.LIGHT -> LightModeIcon
                                ThemeMode.DARK -> DarkModeIcon
                                ThemeMode.DEFAULT -> AutoModeIcon
                            }
                            ThreeDThemeOptionButton(
                                text = modeLabel(mode),
                                iconVector = modeIcon,
                                isSelected = isSelected,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                onClick = { onThemeModeChange(mode) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreeDSignInControlRow(
    title: String,
    iconVector: ImageVector,
    iconTint: Color,
    isEnabled: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    textColor: Color,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                )
            }

            ThreeDSwitch(
                checked = isEnabled,
                isDark = isDark,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun ThreeDSwitch(
    checked: Boolean,
    isDark: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp

    val thumbOffsetPx by animateDpAsState(
        targetValue = if (checked) (trackWidth - thumbSize - 3.dp) else 3.dp,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.75f),
        label = "SwitchThumbOffset",
    )

    val activeColor = Color(0xFF10B981)
    val activeBaseColor = Color(0xFF047857)
    val inactiveColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
    val inactiveBaseColor = if (isDark) Color(0xFF1E293B) else Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .width(trackWidth)
            .height(trackHeight + 3.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) },
            ),
        contentAlignment = Alignment.TopStart,
    ) {
        Box(
            modifier = Modifier
                .offset(y = 3.dp)
                .width(trackWidth)
                .height(trackHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(if (checked) activeBaseColor else inactiveBaseColor),
        )

        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(trackHeight)
                .offset(y = if (isPressed) 2.dp else 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (checked) activeColor else inactiveColor)
                .border(
                    1.dp,
                    if (checked) activeBaseColor.copy(alpha = 0.4f) else (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
                    RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetPx, y = 0.dp)
                    .size(thumbSize)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFCBD5E1), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (checked) activeColor else Color(0xFF94A3B8)),
                )
            }
        }
    }
}

@Composable
private fun AdminRequestRowItem(
    item: AdminRequestItem,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    textColor: Color,
    subTextColor: Color,
    isApproving: Boolean,
    isDeclining: Boolean,
    isDisabled: Boolean,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF7C3AED).copy(alpha = if (isDark) 0.22f else 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f)),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.name.take(1).ifBlank { "A" }.uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name.ifBlank { "Admin Applicant" },
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.email,
                        fontSize = 11.5.sp,
                        color = subTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                    border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = "Pending",
                        fontSize = 10.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThreeDActionButton(
                    text = "Approve",
                    iconVector = CheckIcon,
                    baseColor = Color(0xFF047857),
                    surfaceColor = Color(0xFF10B981),
                    textColor = Color.White,
                    isLoading = isApproving,
                    enabled = !isDisabled,
                    hindSiliguri = hindSiliguri,
                    modifier = Modifier.weight(1f),
                    onClick = onApprove,
                )

                ThreeDActionButton(
                    text = "Decline",
                    iconVector = CloseIcon,
                    baseColor = Color(0xFFB91C1C),
                    surfaceColor = Color(0xFFEF4444),
                    textColor = Color.White,
                    isLoading = isDeclining,
                    enabled = !isDisabled,
                    hindSiliguri = hindSiliguri,
                    modifier = Modifier.weight(1f),
                    onClick = onDecline,
                )
            }
        }
    }
}

@Composable
private fun ThreeDActionButton(
    text: String,
    iconVector: ImageVector? = null,
    baseColor: Color,
    surfaceColor: Color,
    textColor: Color,
    isLoading: Boolean,
    enabled: Boolean = true,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val trackHeight = 36.dp
    val actualClickable = enabled && !isLoading

    Box(
        modifier = modifier
            .height(trackHeight + 3.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = actualClickable,
                onClick = onClick,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .offset(y = 3.dp)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(if (enabled) baseColor else baseColor.copy(alpha = 0.4f)),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .offset(y = if (isPressed && actualClickable) 2.dp else 0.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (enabled) surfaceColor else surfaceColor.copy(alpha = 0.5f))
                .border(
                    0.5.dp,
                    if (enabled) baseColor.copy(alpha = 0.5f) else Color.Transparent,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = textColor,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (iconVector != null) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = if (enabled) textColor else textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = text,
                        fontSize = 12.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) textColor else textColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreeDThemeOptionButton(
    text: String,
    iconVector: ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(12.dp)

    val activeColor = Color(0xFF6366F1)
    val activeBaseColor = Color(0xFF4338CA)
    val inactiveSurfaceColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val inactiveBaseColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val contentColor = if (isSelected) Color.White else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B))
    val borderColor = if (isSelected) activeColor else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))

    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 3.dp else if (isSelected) 1.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "ThemeButtonPress",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 3.dp)
                .clip(shape)
                .background(if (isSelected) activeBaseColor else inactiveBaseColor),
        )

        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            color = if (isSelected) activeColor else inactiveSurfaceColor,
            border = BorderStroke(0.8.dp, borderColor),
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffset),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else activeColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    fontSize = 12.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }
    }
}
