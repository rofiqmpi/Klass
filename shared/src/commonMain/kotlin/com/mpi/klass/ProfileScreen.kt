package com.mpi.klass

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.AwardIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.GearIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.ToggleOffIcon
import com.mpi.klass.theme.ToggleOnIcon
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.LogoutIcon
import com.mpi.klass.theme.PenIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.StarIcon
import com.mpi.klass.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class OrbitIcons(
    val leftInner: ImageVector,
    val leftOuter: ImageVector,
    val rightInner: ImageVector,
    val rightOuter: ImageVector,
)

private fun getOrbitIconsForRole(role: UserRole?): OrbitIcons = when (role) {
    UserRole.ADMIN -> OrbitIcons(
        leftInner = CrownIcon,
        leftOuter = GearIcon,
        rightInner = ShieldIcon,
        rightOuter = AwardIcon,
    )
    UserRole.TEACHER -> OrbitIcons(
        leftInner = GraduationCapIcon,
        leftOuter = PenIcon,
        rightInner = BookIcon,
        rightOuter = AwardIcon,
    )
    UserRole.STUDENT -> OrbitIcons(
        leftInner = BookIcon,
        leftOuter = StarIcon,
        rightInner = GraduationCapIcon,
        rightOuter = AwardIcon,
    )
    null -> OrbitIcons(
        leftInner = StarIcon,
        leftOuter = ShieldIcon,
        rightInner = BookIcon,
        rightOuter = AwardIcon,
    )
}

@Composable
fun ProfileScreen(
    userName: String,
    role: UserRole?,
    isDark: Boolean,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSignOut: () -> Unit,
    hindSiliguri: FontFamily,
    email: String = "",
    photoUrl: String? = null,
    onUpdateProfile: ((newName: String, newPhotoUrl: String?) -> Unit)? = null,
    onCategoryChanged: ((UserRole) -> Unit)? = null,
) {
    if (role == null) {
        val currentLang = LocalAppLanguage.current
        val primaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
        val ringColor = primaryColor.copy(alpha = 0.15f)

        val transition = rememberInfiniteTransition(label = "RippleTransition")
        val pulseProgress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "PulseProgress"
        )

        val interactionSource = remember { MutableInteractionSource() }
        val isButtonClicked by interactionSource.collectIsPressedAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center,
        ) {
            // Multiple concentric circle layers spanning outward across the screen
            val numberOfRings = 6
            for (i in 1..numberOfRings) {
                val animatedScale = if (isButtonClicked) {
                    (1f + (pulseProgress * 0.5f) + (i * 0.12f)).coerceIn(1f, 3f)
                } else {
                    1f + (sin((pulseProgress * 2f * PI + i * 0.5f)).toFloat() * 0.04f)
                }

                Box(
                    modifier = Modifier
                        .size((140 + i * 85).dp)
                        .graphicsLayer(
                            scaleX = animatedScale,
                            scaleY = animatedScale,
                            alpha = (1f - (i * 0.14f)).coerceIn(0.08f, 0.7f)
                        )
                        .clip(CircleShape)
                        .background(ringColor)
                )
            }

            // Center Pure Circle Sign In Button
            Button(
                onClick = onSignOut,
                interactionSource = interactionSource,
                modifier = Modifier
                    .size(140.dp), // Pure circle
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
            ) {
                Text(
                    text = if (currentLang == AppLanguage.BENGALI) "সাইন ইন করুন" else "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var showAdminRequestsDialog by remember { mutableStateOf(false) }

    // Admin sign in control state
    var signInControl by remember { mutableStateOf(SignInControlConfig()) }
    var isUpdatingControl by remember { mutableStateOf(false) }

    // Student profile state
    var studentProfile by remember { mutableStateOf<StudentProfile?>(null) }
    var isLoadingStudentProfile by remember { mutableStateOf(false) }

    // Student profile setup timer (5 minutes to complete setup)
    var setupRemainingSeconds by remember { mutableStateOf(300) }
    var setupTimerExpired by remember { mutableStateOf(false) }

    // Student profile setup form state (inline on profile screen)
    var setupSemester by remember { mutableStateOf("") }
    var setupShift by remember { mutableStateOf("") }
    var setupDepartment by remember { mutableStateOf("") }
    var setupSection by remember { mutableStateOf("") }
    var setupPhone by remember { mutableStateOf("") }
    var setupBloodGroup by remember { mutableStateOf("") }
    var setupAddress by remember { mutableStateOf("") }
    var setupRollNumber by remember { mutableStateOf("") }
    var isSavingStudentProfile by remember { mutableStateOf(false) }
    var studentProfileError by remember { mutableStateOf<String?>(null) }
    var isStudentEditing by remember { mutableStateOf(false) }
    var adminCheckError by remember { mutableStateOf<String?>(null) }

    val cleanEmail = email.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()

    LaunchedEffect(cleanEmail, role) {
        if (role == UserRole.ADMIN) {
            signInControl = RoleConfig.fetchSignInControl()
        } else if (role == UserRole.STUDENT && cleanEmail.isNotBlank()) {
            isLoadingStudentProfile = true
            val prof = RoleConfig.fetchStudentProfile(cleanEmail)
            studentProfile = prof
            if (prof != null) {
                setupSemester = prof.semester
                setupShift = if (prof.shift == "1") "সকাল → ১" else "বিকাল → ২"
                setupDepartment = prof.department
                setupSection = prof.section
                setupPhone = prof.phone
                setupBloodGroup = prof.bloodGroup
                setupAddress = prof.address
                setupRollNumber = prof.rollNumber
            }
            isLoadingStudentProfile = false
        }
    }

    // 5-minute countdown for incomplete student profile setup
    LaunchedEffect(Unit) {
        if (role != UserRole.STUDENT) return@LaunchedEffect
        val prof = studentProfile
        val isComplete = prof != null && prof.profileSetupComplete
        if (isComplete || setupTimerExpired) return@LaunchedEffect

        var remaining = setupRemainingSeconds
        while (remaining > 0 && !setupTimerExpired) {
            delay(1000L)
            remaining--
            setupRemainingSeconds = remaining
        }
        if (!setupTimerExpired) {
            setupTimerExpired = true
            coroutineScope.launch {
                RoleConfig.deleteStudentSignInData(cleanEmail)
                clearUserSession()
                onSignOut()
            }
        }
    }

    val roleColor = when (role) {
        UserRole.ADMIN -> Color(0xFFD97706)
        UserRole.TEACHER -> Color(0xFF059669)
        UserRole.STUDENT -> Color(0xFF0284C7)
    }

    val roleBorderColor = roleColor.copy(alpha = if (isDark) 0.5f else 0.35f)
    val orbitBgColor = cardBg.copy(alpha = if (isDark) 0.65f else 0.75f)
    val orbitBorderColor = roleBorderColor.copy(alpha = if (isDark) 0.45f else 0.35f)
    val orbitIcons = getOrbitIconsForRole(role)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile Picture Section with Staggered Wing Orbit Icons
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(115.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Left Inner Icon
            Surface(
                shape = CircleShape,
                color = orbitBgColor,
                border = BorderStroke(1.2.dp, orbitBorderColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .offset(x = (-80).dp, y = (-2).dp)
                    .size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = orbitIcons.leftInner,
                        contentDescription = "Left Inner Icon",
                        tint = roleColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            // Right Inner Icon
            Surface(
                shape = CircleShape,
                color = orbitBgColor,
                border = BorderStroke(1.2.dp, orbitBorderColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .offset(x = (80).dp, y = (-2).dp)
                    .size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = orbitIcons.rightInner,
                        contentDescription = "Right Inner Icon",
                        tint = roleColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            // Left Outer Icon
            Surface(
                shape = CircleShape,
                color = orbitBgColor,
                border = BorderStroke(1.2.dp, orbitBorderColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .offset(x = (-118).dp, y = 18.dp)
                    .size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = orbitIcons.leftOuter,
                        contentDescription = "Left Outer Icon",
                        tint = roleColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Right Outer Icon
            Surface(
                shape = CircleShape,
                color = orbitBgColor,
                border = BorderStroke(1.2.dp, orbitBorderColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .offset(x = (118).dp, y = 18.dp)
                    .size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = orbitIcons.rightOuter,
                        contentDescription = "Right Outer Icon",
                        tint = roleColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Main Dynamic Genderless Non-Text Profile Picture Avatar
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(2.5.dp, if (isDark) Color.White else Color(0xFF0F172A)),
                modifier = Modifier
                    .size(95.dp)
                    .clip(CircleShape),
                shadowElevation = 5.dp,
            ) {
                DynamicAvatar(
                    seed = cleanEmail.ifBlank { userName },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // User Name & Edit Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = userName.ifEmpty { "গেস্ট ইউজার" },
                fontSize = 20.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                onClick = { showEditDialog = true },
                shape = CircleShape,
                color = Color(0xFF8CB9D9).copy(alpha = 0.2f),
                modifier = Modifier.size(28.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = EditIcon,
                        contentDescription = "এডিট নাম",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        if (cleanEmail.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = cleanEmail,
                fontSize = 12.sp,
                color = subTextColor,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selection Card (Profile Category Selection)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, roleColor.copy(alpha = 0.35f)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "অ্যাকাউন্টের ক্যাটাগরি নির্বাচন করুন",
                    fontSize = 14.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = roleColor,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val categories = listOf(
                        UserRole.STUDENT to "শিক্ষার্থী",
                        UserRole.TEACHER to "শিক্ষক",
                        UserRole.ADMIN to "এডমিন",
                    )
                    categories.forEach { (catRole, catLabel) ->
                        val isSelected = role == catRole
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (catRole == UserRole.ADMIN) {
                                        val isAuth = RoleConfig.isAuthorizedAdminEmail(cleanEmail)
                                        if (!isAuth) {
                                            adminCheckError = "আপনার ইমেইলটি Firebase-এর admins তালিকায় অনুমোদিত নয়।"
                                            return@launch
                                        }
                                    }
                                    adminCheckError = null
                                    RoleConfig.updateUserCategory(cleanEmail, catRole.name)
                                    onCategoryChanged?.invoke(catRole)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) roleColor else if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                                contentColor = if (isSelected) Color.White else textColor,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 13.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                if (!adminCheckError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = adminCheckError ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Edit Profile Action Card
        Surface(
            onClick = { showEditDialog = true },
            shape = RoundedCornerShape(14.dp),
            color = cardBg,
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = EditIcon,
                        contentDescription = "প্রোফাইল এডিট",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "প্রোফাইল নাম পরিবর্তন করুন",
                        fontSize = 13.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.15f),
                ) {
                    Text(
                        text = "এডিট",
                        fontSize = 12.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }

        // Student Profile Setup & Information Card (Visible ONLY for Students)
        if (role == UserRole.STUDENT) {
            val prof = studentProfile
            val isSetupComplete = prof != null && prof.profileSetupComplete

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = GraduationCapIcon,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSetupComplete && !isStudentEditing) "শিক্ষার্থী প্রোফাইল বিবরণ" else "শিক্ষার্থী প্রোফাইল সেটআপ",
                                fontSize = 14.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                            )
                        }
                        if (isSetupComplete) {
                            OutlinedButton(
                                onClick = { isStudentEditing = !isStudentEditing },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(text = if (isStudentEditing) "বাতিল" else "এডিট", fontSize = 11.5.sp, fontFamily = hindSiliguri)
                            }
                        }
                    }

                    // Countdown timer warning for incomplete setup
                    if (!isSetupComplete && !setupTimerExpired && !isStudentEditing) {
                        val timerMinutes = setupRemainingSeconds / 60
                        val timerSecs = setupRemainingSeconds % 60
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3CD),
                            border = BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f)),
                        ) {
                            Text(
                                text = "⚠️ প্রোফাইল সেটআপের বাকি সময়: ${timerMinutes}:${timerSecs.toString().padStart(2, '0')}\nসময় শেষ হলে আপনার সমস্ত তথ্য ডাটাবেস থেকে মুছে যাবে।",
                                fontSize = 12.5.sp,
                                fontFamily = hindSiliguri,
                                color = Color(0xFF856404),
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isSetupComplete || isStudentEditing) {
                        // Student Profile Setup / Edit Form
                        val semesterOptions = listOf("১ম", "২য়", "৩য়", "৪র্থ", "৫ম", "৬ষ্ঠ", "৭ম")
                        val shiftOptions = listOf("সকাল → ১", "বিকাল → ২")
                        val departmentOptions = listOf("CST", "CT", "ET", "PT", "MT", "EnT", "EMT")
                        val sectionOptions = listOf("A", "B", "C")
                        val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

                        val isFormValid = setupSemester.isNotBlank() && setupShift.isNotBlank() &&
                            setupDepartment.isNotBlank() && setupSection.isNotBlank() &&
                            setupPhone.isNotBlank() && setupBloodGroup.isNotBlank() &&
                            setupAddress.isNotBlank() && setupRollNumber.isNotBlank()

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileDropdownField("সেমিস্টার", setupSemester, semesterOptions, { setupSemester = it }, isDark, hindSiliguri, cardBg, textColor, subTextColor)
                            ProfileDropdownField("শিফট", setupShift, shiftOptions, { setupShift = it }, isDark, hindSiliguri, cardBg, textColor, subTextColor)
                            ProfileDropdownField("ডিপার্টমেন্ট", setupDepartment, departmentOptions, { setupDepartment = it }, isDark, hindSiliguri, cardBg, textColor, subTextColor)
                            ProfileDropdownField("সেকশন", setupSection, sectionOptions, { setupSection = it }, isDark, hindSiliguri, cardBg, textColor, subTextColor)

                            ProfileInputField("রোল নং", setupRollNumber, { setupRollNumber = it }, "যেমন: ১২৩৪৫৬", isDark, hindSiliguri, textColor)
                            ProfileInputField("ফোন নাম্বার", setupPhone, { setupPhone = it }, "যেমন: ০১৭xxxxxxxx", isDark, hindSiliguri, textColor)
                            ProfileDropdownField("ব্লাড গ্রুপ", setupBloodGroup, bloodGroupOptions, { setupBloodGroup = it }, isDark, hindSiliguri, cardBg, textColor, subTextColor)
                            ProfileInputField("ঠিকানা", setupAddress, { setupAddress = it }, "আপনার বর্তমান ঠিকানা লিখুন", isDark, hindSiliguri, textColor, singleLine = false)

                            if (studentProfileError != null) {
                                Text(
                                    text = studentProfileError!!,
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    color = Color(0xFFEF4444),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isFormValid) {
                                        studentProfileError = "সকল তথ্য পূরণ করুন"
                                        return@Button
                                    }
                                    studentProfileError = null
                                    isSavingStudentProfile = true
                                    coroutineScope.launch {
                                        val shiftVal = if (setupShift.contains("সকাল")) "1" else "2"
                                        val newProfile = StudentProfile(
                                            email = cleanEmail,
                                            name = userName,
                                            semester = setupSemester,
                                            shift = shiftVal,
                                            department = setupDepartment,
                                            section = setupSection,
                                            phone = setupPhone,
                                            bloodGroup = setupBloodGroup,
                                            address = setupAddress,
                                            rollNumber = setupRollNumber,
                                            profileSetupComplete = true,
                                        )
                                        val res = RoleConfig.saveStudentProfile(newProfile)
                                        isSavingStudentProfile = false
                                        if (res.isSuccess) {
                                            studentProfile = newProfile
                                            isStudentEditing = false
                                            setupTimerExpired = true // stop the countdown timer
                                        } else {
                                            studentProfileError = "সংরক্ষণ করা যায়নি। পুনরায় চেষ্টা করুন।"
                                        }
                                    }
                                },
                                enabled = !isSavingStudentProfile && !setupTimerExpired,
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            ) {
                                if (isSavingStudentProfile) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isSavingStudentProfile) "সংরক্ষণ করা হচ্ছে..." else "প্রোফাইল সংরক্ষণ করুন",
                                    fontSize = 14.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    } else {
                        // Display Profile Info Summary
                        ProfileInfoRow("সেমিস্টার", prof.semester, textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("শিফট", if (prof.shift == "1") "সকাল (১)" else "বিকাল (২)", textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("ডিপার্টমেন্ট", prof.department, textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("সেকশন", prof.section, textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("রোল নং", prof.rollNumber, textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("ফোন নাম্বার", prof.phone, textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("ব্লাড গ্রুপ", prof.bloodGroup, textColor, subTextColor, hindSiliguri)
                        ProfileInfoRow("ঠিকানা", prof.address, textColor, subTextColor, hindSiliguri)
                    }
                }
            }
        }

        // Admin Controls (Visible ONLY for Admin)
        if (role == UserRole.ADMIN) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Login Requests Button
            Surface(
                onClick = { showAdminRequestsDialog = true },
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ShieldIcon,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "লগইন রিকোয়েস্ট ও অনুমোদন",
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD97706).copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = "পর্যালোচনা",
                            fontSize = 12.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Sign-in Control Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "নতুন সাইন ইন নিয়ন্ত্রণ",
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        if (isUpdatingControl) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বন্ধ রাখলে শুধুমাত্র বিদ্যমান ব্যবহারকারীরা লগইন করতে পারবে, নতুন কেউ সাইন ইন করতে পারবে না।",
                        fontSize = 11.5.sp,
                        fontFamily = hindSiliguri,
                        color = subTextColor,
                        lineHeight = 16.sp,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SignInToggleRow(
                        title = "এডমিন সাইন ইন",
                        isEnabled = signInControl.adminSignInEnabled,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        textColor = textColor,
                        onToggle = { newValue ->
                            val updated = signInControl.copy(adminSignInEnabled = newValue)
                            signInControl = updated
                            isUpdatingControl = true
                            coroutineScope.launch {
                                RoleConfig.saveSignInControl(updated)
                                isUpdatingControl = false
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SignInToggleRow(
                        title = "শিক্ষক সাইন ইন",
                        isEnabled = signInControl.teacherSignInEnabled,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        textColor = textColor,
                        onToggle = { newValue ->
                            val updated = signInControl.copy(teacherSignInEnabled = newValue)
                            signInControl = updated
                            isUpdatingControl = true
                            coroutineScope.launch {
                                RoleConfig.saveSignInControl(updated)
                                isUpdatingControl = false
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SignInToggleRow(
                        title = "শিক্ষার্থী সাইন ইন",
                        isEnabled = signInControl.studentSignInEnabled,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        textColor = textColor,
                        onToggle = { newValue ->
                            val updated = signInControl.copy(studentSignInEnabled = newValue)
                            signInControl = updated
                            isUpdatingControl = true
                            coroutineScope.launch {
                                RoleConfig.saveSignInControl(updated)
                                isUpdatingControl = false
                            }
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Theme Selector Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = "অ্যাপ থিম নির্বাচন করুন",
                    fontSize = 14.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

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
                        Button(
                            onClick = { onThemeModeChange(mode) },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
                                contentColor = if (isDark) Color.White else Color.Black,
                            ),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isDark) Color.White else Color.Black,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Out Button
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = LogoutIcon,
                    contentDescription = "সাইন আউট",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সাইন আউট করুন",
                    fontSize = 14.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Account Delete Button (For all registered users)
        if (cleanEmail.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444),
                ),
            ) {
                Text(
                    text = "অ্যাকাউন্ট ডিলিট করুন",
                    fontSize = 13.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                )
            }
        }
    }

    // Account Delete Confirmation Dialog
    if (showDeleteDialog) {
        Dialog(onDismissRequest = { if (!isDeletingAccount) showDeleteDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "⚠️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "অ্যাকাউন্ট মুছে ফেলতে চান?",
                        fontSize = 17.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার অ্যাকাউন্ট, প্রোফাইল তথ্য এবং সমস্ত সেশন ডাটাবেস থেকে স্থায়ীভাবে মুছে যাবে। এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।",
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        color = subTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = false },
                            enabled = !isDeletingAccount,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(text = "বাতিল", fontFamily = hindSiliguri, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                isDeletingAccount = true
                                coroutineScope.launch {
                                    RoleConfig.deleteAccountFromFirebase(cleanEmail, role)
                                    isDeletingAccount = false
                                    showDeleteDialog = false
                                    onSignOut()
                                }
                            },
                            enabled = !isDeletingAccount,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White,
                            ),
                        ) {
                            if (isDeletingAccount) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text(text = "ডিলিট করুন", fontFamily = hindSiliguri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }



    // Full-Screen Edit Profile Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(userName) }

        Dialog(
            onDismissRequest = { showEditDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Top Bar Header
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        color = cardBg,
                        shadowElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showEditDialog = false }) {
                                    Icon(
                                        imageVector = CloseIcon,
                                        contentDescription = "বন্ধ করুন",
                                        tint = textColor,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "প্রোফাইল নাম পরিবর্তন",
                                    fontSize = 18.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                )
                            }

                            Button(
                                onClick = {
                                    val finalName = editName.trim().ifEmpty { userName }
                                    onUpdateProfile?.invoke(finalName, null)
                                    showEditDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    contentColor = Color.White,
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = "সংরক্ষণ করুন",
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                )
                            }
                        }
                    }

                    // Form Body
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // 1. Live Preview Dynamic Avatar
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(3.dp, if (isDark) Color.White else Color(0xFF0F172A)),
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            shadowElevation = 6.dp,
                        ) {
                            DynamicAvatar(
                                seed = cleanEmail.ifBlank { editName.ifEmpty { userName } },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Name Input Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "আপনার নাম লিখুন",
                                    fontSize = 13.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0284C7),
                                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                    ),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontFamily = hindSiliguri,
            color = subTextColor,
        )
        Text(
            text = value.ifBlank { "-" },
            fontSize = 13.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    textColor: Color,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = hindSiliguri, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 11.sp) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0284C7),
            unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
            focusedLabelColor = Color(0xFF0284C7),
            unfocusedLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
        ),
    )
}

@Composable
private fun ProfileDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
) {
    var expanded by remember { mutableStateOf(false) }
    val accentColor = Color(0xFF0284C7)

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp),
            color = cardBg,
            border = BorderStroke(
                1.dp,
                if (value.isNotBlank()) accentColor.copy(alpha = 0.6f) else (if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontFamily = hindSiliguri,
                        color = if (value.isNotBlank()) accentColor else subTextColor,
                    )
                    if (value.isNotBlank()) {
                        Text(
                            text = value,
                            fontSize = 13.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        )
                    } else {
                        Text(
                            text = "নির্বাচন করুন",
                            fontSize = 12.5.sp,
                            fontFamily = hindSiliguri,
                            color = subTextColor,
                        )
                    }
                }
                Text(
                    text = "▼",
                    fontSize = 11.sp,
                    color = subTextColor,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = hindSiliguri,
                            fontSize = 13.5.sp,
                            color = if (option == value) accentColor else textColor,
                            fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SignInToggleRow(
    title: String,
    isEnabled: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    textColor: Color,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        onClick = { onToggle(!isEnabled) },
        shape = RoundedCornerShape(10.dp),
        color = if (isEnabled) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isEnabled) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )

            Icon(
                imageVector = if (isEnabled) ToggleOnIcon else ToggleOffIcon,
                contentDescription = null,
                tint = if (isEnabled) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
