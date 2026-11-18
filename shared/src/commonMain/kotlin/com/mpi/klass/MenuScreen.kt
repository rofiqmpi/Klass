package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.RightArrowIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.ThemeMode
import kotlinx.coroutines.launch

private data class MenuTileSpec(
    val id: String,
    val title: String,
    val icon: ImageVector,
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
    val cs = MaterialTheme.colorScheme
    val tiles = remember(role) {
        buildList {
            if (role == UserRole.ADMIN) {
                add(MenuTileSpec("routine_manager", "Routine Engine", CalendarDaysIcon, onClick = onRoutineManagerClick))
                add(MenuTileSpec("books", "Books Library", BookIcon, onClick = onBooksClick))
                add(MenuTileSpec("classrooms", "Classrooms & Labs", DoorOpenIcon, onClick = onClassroomClick))
                add(MenuTileSpec("teachers", "Teachers Directory", GraduationCapIcon, onClick = onTeachersClick))
            } else {
                if (role == UserRole.TEACHER) {
                    add(MenuTileSpec("teacher_prefs", "Teaching Subjects", GraduationCapIcon, onClick = onTeacherPreferencesClick))
                }
                add(MenuTileSpec("classrooms", "Classrooms & Labs", DoorOpenIcon, onClick = onClassroomClick))
            }
            add(MenuTileSpec("notices", "Notice Board", NotificationIcon, onClick = onNoticeClick))
            add(MenuTileSpec("blood_finder", "Blood Finder", DropletIcon, onClick = onBloodFinderClick))
            add(MenuTileSpec("ai_assistant", "Study Assistant", SparkleIcon, onClick = onAiClick))
            add(MenuTileSpec("profile", "Profile", PersonIcon, onClick = onProfileClick))
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
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWide = maxWidth >= 600.dp
                    if (isWide) {
                        MinimalResponsiveGrid(
                            items = tiles,
                            twoColMinWidth = 600.dp,
                            spacing = 10.dp,
                        ) { _, tile ->
                            MinimalCardSurface(
                                isDark = isDark,
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                SimpleMenuRow(tile = tile, hindSiliguri = hindSiliguri)
                            }
                        }
                    } else {
                        MinimalCardSurface(
                            isDark = isDark,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                tiles.forEachIndexed { index, tile ->
                                    SimpleMenuRow(tile = tile, hindSiliguri = hindSiliguri)
                                    if (index < tiles.lastIndex) {
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = cs.outlineVariant.copy(alpha = 0.45f),
                                            modifier = Modifier.padding(start = 52.dp, end = 16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (role == UserRole.ADMIN) {
                    GoogleSectionHeader(
                        title = "Administration",
                        hindSiliguri = hindSiliguri,
                    )
                    AdminControlsSection(isDark = isDark, hindSiliguri = hindSiliguri)
                }

                SimpleThemeSwitcher(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                )
            }
        }
    }
}

@Composable
private fun SimpleMenuRow(
    tile: MenuTileSpec,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = tile.onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_tile_${tile.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = tile.title,
                tint = cs.primary,
                modifier = Modifier.size(23.dp),
            )

            Spacer(modifier = Modifier.width(15.dp))

            Text(
                text = tile.title,
                fontSize = 16.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = RightArrowIcon,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun AdminControlsSection(
    isDark: Boolean,
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

    val cs = MaterialTheme.colorScheme
    val signInArrow by animateFloatAsState(
        targetValue = if (isSignInExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "SignInArrow",
    )
    val requestsArrow by animateFloatAsState(
        targetValue = if (isRequestsExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "RequestsArrow",
    )

    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(20.dp),
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
                        .padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = ShieldIcon,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Sign-in access control",
                        fontSize = 15.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (isUpdatingControl) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = cs.primary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = "Toggle sign-in controls",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer(rotationZ = signInArrow),
                    )
                }
            }

            AnimatedVisibility(visible = isSignInExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 54.dp, end = 18.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    SignInSwitchRow(
                        title = "Admin sign-in",
                        checked = signInControl.adminSignInEnabled,
                        hindSiliguri = hindSiliguri,
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
                    SignInSwitchRow(
                        title = "Teacher sign-in",
                        checked = signInControl.teacherSignInEnabled,
                        hindSiliguri = hindSiliguri,
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
                    SignInSwitchRow(
                        title = "Student sign-in",
                        checked = signInControl.studentSignInEnabled,
                        hindSiliguri = hindSiliguri,
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

            HorizontalDivider(
                thickness = 0.5.dp,
                color = cs.outlineVariant.copy(alpha = 0.45f),
                modifier = Modifier.padding(start = 56.dp, end = 16.dp),
            )

            Surface(
                onClick = { isRequestsExpanded = !isRequestsExpanded },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = CrownIcon,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (pendingRequests.isNotEmpty()) {
                            "Admin approvals (${pendingRequests.size})"
                        } else {
                            "Admin approvals"
                        },
                        fontSize = 15.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (isLoadingRequests) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = cs.primary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = "Toggle admin approvals",
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer(rotationZ = requestsArrow),
                    )
                }
            }

            AnimatedVisibility(visible = isRequestsExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (pendingRequests.isEmpty()) {
                        Text(
                            text = "No pending access requests.",
                            fontSize = 13.sp,
                            fontFamily = hindSiliguri,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(start = 36.dp),
                        )
                    } else {
                        pendingRequests.forEach { item ->
                            val isItemProcessing = processingEmail == item.email
                            AdminRequestRowItem(
                                item = item,
                                hindSiliguri = hindSiliguri,
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

@Composable
private fun SignInSwitchRow(
    title: String,
    checked: Boolean,
    hindSiliguri: FontFamily,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontFamily = hindSiliguri,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun AdminRequestRowItem(
    item: AdminRequestItem,
    hindSiliguri: FontFamily,
    isApproving: Boolean,
    isDeclining: Boolean,
    isDisabled: Boolean,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cs.surfaceContainerHigh.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column {
                Text(
                    text = item.name.ifBlank { "Admin Applicant" },
                    fontSize = 14.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.email,
                    fontSize = 13.5.sp,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    enabled = !isDisabled,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                ) {
                    if (isDeclining) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = cs.error,
                        )
                    } else {
                        Icon(imageVector = CloseIcon, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline", fontSize = 13.sp, fontFamily = hindSiliguri)
                    }
                }

                Button(
                    onClick = onApprove,
                    enabled = !isDisabled,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                ) {
                    if (isApproving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = cs.onPrimary,
                        )
                    } else {
                        Icon(imageVector = CheckIcon, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Approve", fontSize = 13.sp, fontFamily = hindSiliguri)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleThemeSwitcher(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    hindSiliguri: FontFamily,
    isDark: Boolean,
) {
    val modeLabel: (ThemeMode) -> String = { mode ->
        when (mode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.DEFAULT -> "System"
        }
    }

    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Appearance",
                fontSize = 14.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    val isSelected = mode == currentThemeMode
                    val modeIcon = when (mode) {
                        ThemeMode.LIGHT -> LightModeIcon
                        ThemeMode.DARK -> DarkModeIcon
                        ThemeMode.DEFAULT -> AutoModeIcon
                    }
                    SegmentedButton(
                        selected = isSelected,
                        onClick = { onThemeModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ThemeMode.entries.size,
                        ),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = isSelected) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        },
                    ) {
                        Text(
                            text = modeLabel(mode),
                            fontSize = 13.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
