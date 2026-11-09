package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
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
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Profile Settings Card (TOP-MOST - For Everyone)
        ProfessionalMenuCard(
            title = "প্রোফাইল সেটিংস",
            subtitle = "আপনার অ্যাকাউন্ট ও ব্যক্তিগত তথ্য ম্যানেজ করুন",
            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            hindSiliguri = hindSiliguri,
            iconVector = PersonIcon,
            onClick = onProfileClick,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Admin-only Menu Items
        if (role == UserRole.ADMIN) {
            Spacer(modifier = Modifier.height(4.dp))

            // Books Library Card (Admin-only)
            ProfessionalMenuCard(
                title = "বুকস লাইব্রেরি",
                subtitle = "সেমিস্টার অনুযায়ী ডিপার্টমেন্টাল বই ও লেকচার নোট",
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                cardBg = cardBg,
                textColor = textColor,
                subTextColor = subTextColor,
                hindSiliguri = hindSiliguri,
                iconVector = BookIcon,
                onClick = onBooksClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Classroom Card (Admin-only)
            ProfessionalMenuCard(
                title = "ক্লাসরুম",
                subtitle = "অনলাইন ক্লাস ও সেমিস্টার রুটিন তথ্য",
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                cardBg = cardBg,
                textColor = textColor,
                subTextColor = subTextColor,
                hindSiliguri = hindSiliguri,
                iconVector = GraduationCapIcon,
                onClick = onClassroomClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Teachers Card (Admin-only)
            ProfessionalMenuCard(
                title = "শিক্ষকবৃন্দ",
                subtitle = "সম্মানিত শিক্ষকগণের তালিকা ও যোগাযোগের তথ্য",
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                cardBg = cardBg,
                textColor = textColor,
                subTextColor = subTextColor,
                hindSiliguri = hindSiliguri,
                iconVector = GraduationCapIcon,
                onClick = onTeachersClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Notice Board Card (Admin-only)
            ProfessionalMenuCard(
                title = "নোটিশ বোর্ড",
                subtitle = "ইনস্টিটিউট ও ডিপার্টমেন্টের জরুরি আপডেট ও নোটিশ",
                gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                cardBg = cardBg,
                textColor = textColor,
                subTextColor = subTextColor,
                hindSiliguri = hindSiliguri,
                iconVector = NotificationIcon,
                onClick = onNoticeClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            val coroutineScope = rememberCoroutineScope()
            var signInControl by remember { mutableStateOf(SignInControlConfig()) }
            var isUpdatingControl by remember { mutableStateOf(false) }
            var isExpanded by remember { mutableStateOf(false) }

            // Admin Requests state
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

            // 8. Sign-In Control Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = BorderStroke(
                    1.2.dp,
                    if (isExpanded) Color(0xFFD97706).copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                ),
                shadowElevation = if (isExpanded) 6.dp else 3.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Clickable Card Header (Toggles Expand / Minimize)
                    Surface(
                        onClick = { isExpanded = !isExpanded },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = ShieldIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "নতুন সাইন ইন নিয়ন্ত্রণ",
                                            fontSize = 15.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                        )
                                        if (isUpdatingControl) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFFD97706),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isExpanded) "অ্যাডমিন, শিক্ষক ও শিক্ষার্থী সাইন ইন পারমিশন" else "ক্লিক করে সেটিংস প্যানেল খুলুন",
                                        fontSize = 11.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = subTextColor,
                                    )
                                }
                            }

                            val arrowRotation by animateFloatAsState(
                                targetValue = if (isExpanded) 270f else 90f,
                                animationSpec = spring(stiffness = 400f),
                                label = "ArrowRotation",
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD97706).copy(alpha = 0.12f),
                                modifier = Modifier.size(34.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = RightArrowIcon,
                                        contentDescription = "Expand or collapse",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .graphicsLayer(rotationZ = arrowRotation),
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "সাইন ইন বন্ধ রাখলে শুধুমাত্র পূর্বের রেজিস্টার্ড ব্যবহারকারীরা লগইন করতে পারবেন। নতুনদের জন্য প্রবেশ বন্ধ থাকবে।",
                                fontSize = 11.5.sp,
                                fontFamily = hindSiliguri,
                                color = subTextColor,
                                lineHeight = 16.sp,
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            ThreeDSignInControlRow(
                                title = "এডমিন সাইন ইন",
                                iconVector = CrownIcon,
                                iconTint = Color(0xFFD97706),
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

                            Spacer(modifier = Modifier.height(8.dp))

                            ThreeDSignInControlRow(
                                title = "শিক্ষক সাইন ইন",
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

                            Spacer(modifier = Modifier.height(8.dp))

                            ThreeDSignInControlRow(
                                title = "শিক্ষার্থী সাইন ইন",
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

            // 9. 3D Admin Requests & Approval Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = BorderStroke(
                    1.2.dp,
                    if (isRequestsExpanded) Color(0xFF8B5CF6).copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                ),
                shadowElevation = if (isRequestsExpanded) 6.dp else 3.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = CrownIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "এডমিন রিকোয়েস্ট ও অনুমোদন",
                                            fontSize = 15.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                        )
                                        if (pendingRequests.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
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
                                                color = Color(0xFF8B5CF6),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (pendingRequests.isNotEmpty()) "${pendingRequests.size} টি আবেদন অনুমোদনের অপেক্ষায়" else "কোনো পেন্ডিং আবেদন নেই",
                                        fontSize = 11.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = subTextColor,
                                    )
                                }
                            }

                            val arrowRotation by animateFloatAsState(
                                targetValue = if (isRequestsExpanded) 270f else 90f,
                                animationSpec = spring(stiffness = 400f),
                                label = "RequestsArrowRotation",
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                modifier = Modifier.size(34.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = RightArrowIcon,
                                        contentDescription = "Expand or collapse requests",
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .graphicsLayer(rotationZ = arrowRotation),
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = isRequestsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (pendingRequests.isEmpty()) {
                                Text(
                                    text = "বর্তমানে কোনো পেন্ডিং এডমিন অ্যাক্সেস রিকোয়েস্ট নেই।",
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    color = subTextColor,
                                    modifier = Modifier.padding(vertical = 8.dp),
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
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme Selection Card (At the very bottom / sobar niche for everyone)
        ThreeDThemeCard(
            currentThemeMode = currentThemeMode,
            onThemeModeChange = onThemeModeChange,
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
        )

    }
}

@Composable
private fun ThreeDThemeCard(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(
            1.2.dp,
            if (isExpanded) Color(0xFF6366F1).copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
        ),
        shadowElevation = if (isExpanded) 6.dp else 3.dp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Clickable Header (Toggles Expand / Minimize)
            Surface(
                onClick = { isExpanded = !isExpanded },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = PaletteIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "অ্যাপ থিম পরিবর্তন",
                                fontSize = 15.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isExpanded) "লাইট, ডার্ক অথবা অটো থিম সিলেক্ট করুন" else "ক্লিক করে থিম সেটিংস খুলুন",
                                fontSize = 11.5.sp,
                                fontFamily = hindSiliguri,
                                color = subTextColor,
                            )
                        }
                    }

                    val arrowRotation by animateFloatAsState(
                        targetValue = if (isExpanded) 270f else 90f,
                        animationSpec = spring(stiffness = 400f),
                        label = "ThemeArrowRotation",
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6366F1).copy(alpha = 0.12f),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = RightArrowIcon,
                                contentDescription = "Expand or collapse theme",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(rotationZ = arrowRotation),
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val modes = listOf(
                            Triple(ThemeMode.LIGHT, "লাইট", LightModeIcon),
                            Triple(ThemeMode.DARK, "ডার্ক", DarkModeIcon),
                            Triple(ThemeMode.DEFAULT, "অটো", AutoModeIcon),
                        )

                        modes.forEach { (mode, label, icon) ->
                            val isSelected = currentThemeMode == mode
                            val activeColor = Color(0xFF6366F1)
                            val activeBaseColor = Color(0xFF4338CA)

                            ThreeDActionButton(
                                text = label,
                                iconVector = icon,
                                baseColor = if (isSelected) activeBaseColor else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                                surfaceColor = if (isSelected) activeColor else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                textColor = if (isSelected) Color.White else textColor,
                                isLoading = false,
                                enabled = true,
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
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
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                    Text(
                        text = if (isEnabled) "চালু আছে" else "বন্ধ রয়েছে",
                        fontSize = 11.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) Color(0xFF10B981) else Color(0xFFEF4444),
                    )
                }
            }

            // Custom Tactile 3D Toggle Switch
            ThreeDSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                isDark = isDark,
                activeColor = iconTint,
                activeBaseColor = iconTint.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun ThreeDSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean,
    activeColor: Color = Color(0xFF10B981),
    activeBaseColor: Color = Color(0xFF047857),
    inactiveColor: Color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
    inactiveBaseColor: Color = if (isDark) Color(0xFF334155) else Color(0xFF94A3B8),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val trackWidth = 54.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp

    val thumbOffsetPx by animateDpAsState(
        targetValue = if (checked) 28.dp else 3.dp,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.75f),
        label = "ThumbOffset",
    )

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
        // 1. 3D Track Shadow Base Layer
        Box(
            modifier = Modifier
                .offset(y = 3.dp)
                .width(trackWidth)
                .height(trackHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(if (checked) activeBaseColor else inactiveBaseColor),
        )

        // 2. Front 3D Track Surface
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
            // 3. Floating 3D White Thumb Circle Knob
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
private fun ProfessionalMenuCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
    iconVector: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, gradientColors.first().copy(alpha = 0.25f)),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Container with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title.localized(),
                    fontSize = 16.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle.localized(),
                    fontSize = 12.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    color = subTextColor,
                    maxLines = 1,
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
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Profile Avatar with Gradient Background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.name.take(1).ifBlank { "A" }.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = item.name.ifBlank { "এডমিন রিকোয়েস্টকারী" },
                            fontSize = 14.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                        )

                        // Pending Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B)),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "অপেক্ষমাণ",
                                    fontSize = 10.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.email,
                        fontSize = 11.5.sp,
                        color = subTextColor,
                        maxLines = 1,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thin separation line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Independent Approve Button (Green 3D)
                ThreeDActionButton(
                    text = "অনুমোদন করুন",
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

                // Independent Decline Button (Red 3D)
                ThreeDActionButton(
                    text = "প্রত্যাখ্যান করুন",
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
        // 3D Shadow Base
        Box(
            modifier = Modifier
                .offset(y = 3.dp)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(if (enabled) baseColor else baseColor.copy(alpha = 0.4f)),
        )

        // 3D Surface
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
                    RoundedCornerShape(10.dp)
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
