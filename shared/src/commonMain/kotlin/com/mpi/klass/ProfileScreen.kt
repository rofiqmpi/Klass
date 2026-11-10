package com.mpi.klass

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.TextButton
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.AwardIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CameraIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.GalleryIcon
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
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.img_address
import klass.shared.generated.resources.img_blood
import klass.shared.generated.resources.img_department
import klass.shared.generated.resources.img_details
import klass.shared.generated.resources.img_phone
import klass.shared.generated.resources.img_roll
import klass.shared.generated.resources.img_section
import klass.shared.generated.resources.img_semester
import klass.shared.generated.resources.img_shift
import org.jetbrains.compose.resources.painterResource
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
    val coroutineScope = rememberCoroutineScope()
    var signInControl by remember { mutableStateOf(SignInControlConfig()) }
    var isLoadingControl by remember { mutableStateOf(false) }

    LaunchedEffect(role) {
        if (role == UserRole.ADMIN) {
            isLoadingControl = true
            signInControl = RoleConfig.fetchSignInControl()
            isLoadingControl = false
        }
    }

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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }

    // Inline Profile Name Edit & Photo Upload State
    var isEditingNameInline by remember { mutableStateOf(false) }
    var inlineNameInput by remember { mutableStateOf(userName) }
    var currentPhotoUrl by remember(photoUrl) { mutableStateOf(photoUrl ?: "") }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // Native Device Gallery Photo Picker Launcher
    val launchPhotoPicker = rememberPhotoPickerLauncher { pickedUri ->
        currentPhotoUrl = pickedUri
        onUpdateProfile?.invoke(userName, pickedUri)
        showAvatarPicker = false
    }

    // Admin sign in control state
    // signInControl is already declared at the top of ProfileScreen
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

    // Expansion states for 8 Student Profile Cards directly on profile screen
    var expSemester by remember { mutableStateOf(false) }
    var expShift by remember { mutableStateOf(false) }
    var expDept by remember { mutableStateOf(false) }
    var expSection by remember { mutableStateOf(false) }
    var expRoll by remember { mutableStateOf(false) }
    var expPhone by remember { mutableStateOf(false) }
    var expBlood by remember { mutableStateOf(false) }
    var expAddress by remember { mutableStateOf(false) }

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
                setupShift = if (prof.shift == "1") "Morning" else if (prof.shift == "2") "Evening" else prof.shift
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
            .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Profile Picture Section with Staggered Wing Orbit Icons & Photo Upload Badge
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(118.dp),
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

            // Main Dynamic Profile Picture Avatar & Camera Upload Badge
            Box(
                modifier = Modifier.size(98.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.5.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    shadowElevation = 5.dp,
                ) {
                    if (currentPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = currentPhotoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        DynamicAvatar(
                            seed = cleanEmail.ifBlank { userName },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                // 3D Camera / Photo Selection Badge
                Surface(
                    onClick = { launchPhotoPicker() },
                    shape = CircleShape,
                    color = Color(0xFF0284C7),
                    border = BorderStroke(1.5.dp, Color.White),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = CameraIcon,
                            contentDescription = "গ্যালারি থেকে ছবি আপলোড",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Profile Name display / Inline editing
        if (isEditingNameInline) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            ) {
                OutlinedTextField(
                    value = inlineNameInput,
                    onValueChange = { inlineNameInput = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Save Checkmark 3D Button
                Surface(
                    onClick = {
                        val finalName = inlineNameInput.trim().ifEmpty { userName }
                        onUpdateProfile?.invoke(finalName, currentPhotoUrl.ifBlank { null })
                        isEditingNameInline = false
                    },
                    shape = CircleShape,
                    color = Color(0xFF10B981),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = CheckIcon,
                            contentDescription = "সেভ করুন",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Cancel 3D Button
                Surface(
                    onClick = { isEditingNameInline = false },
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = CloseIcon,
                            contentDescription = "বাতিল",
                            tint = textColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        } else {
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

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    onClick = {
                        inlineNameInput = userName
                        isEditingNameInline = true
                    },
                    shape = CircleShape,
                    color = Color(0xFF0284C7).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = EditIcon,
                            contentDescription = "ইনলাইন নাম এডিট",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(15.dp),
                        )
                    }
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



        // Student Profile Information & 8 Collapsible 3D Option Cards
        if (role == UserRole.STUDENT) {
            val prof = studentProfile
            val isSetupComplete = prof != null && prof.profileSetupComplete
            val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI

            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Top Title & Countdown Banner
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
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "শিক্ষার্থী প্রোফাইল তথ্য" else "Student Profile Details",
                            fontSize = 15.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                        )
                    }
                }

                // Countdown timer warning for incomplete setup
                if (!isSetupComplete && !setupTimerExpired) {
                    val timerMinutes = setupRemainingSeconds / 60
                    val timerSecs = setupRemainingSeconds % 60
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3CD),
                        border = BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = if (isBengali)
                                "⚠️ প্রোফাইল সেটআপের বাকি সময়: ${timerMinutes}:${timerSecs.toString().padStart(2, '0')}\nসময় শেষ হলে আপনার তথ্য মুছে যাবে।"
                            else
                                "⚠️ Setup time remaining: ${timerMinutes}:${timerSecs.toString().padStart(2, '0')}\nProfile data will be cleared if expired.",
                            fontSize = 12.5.sp,
                            fontFamily = hindSiliguri,
                            color = Color(0xFF856404),
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Card 1: Semester - 7 3D Choice Buttons (Strictly English Buttons)
                Collapsible3DProfileCard(
                    title = if (isBengali) "সেমিস্টার" else "Semester",
                    selectedValue = if (setupSemester.isNotBlank()) setupSemester else (if (isBengali) "সেমিস্টার সিলেক্ট করুন" else "Select Semester"),
                    imagePainter = painterResource(Res.drawable.img_semester),
                    iconColor = Color(0xFF0284C7),
                    isExpanded = expSemester,
                    onToggleExpand = { expSemester = !expSemester },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val semesters = listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            semesters.take(4).forEach { sem ->
                                ThreeDOptionButton(
                                    label = sem,
                                    subLabel = null,
                                    isSelected = setupSemester.startsWith(sem, ignoreCase = true) || setupSemester.contains(sem, ignoreCase = true),
                                    onClick = { setupSemester = sem },
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFF0284C7),
                                    isDark = isDark,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            semesters.drop(4).forEach { sem ->
                                ThreeDOptionButton(
                                    label = sem,
                                    subLabel = null,
                                    isSelected = setupSemester.startsWith(sem, ignoreCase = true) || setupSemester.contains(sem, ignoreCase = true),
                                    onClick = { setupSemester = sem },
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFF0284C7),
                                    isDark = isDark,
                                )
                            }
                        }
                    }
                }

                // Card 2: Shift - 2 3D Choice Buttons (Strictly English Buttons)
                Collapsible3DProfileCard(
                    title = if (isBengali) "শিফট" else "Shift",
                    selectedValue = if (setupShift.contains("1") || setupShift.contains("Morning", ignoreCase = true) || setupShift.contains("সকাল")) (if (isBengali) "সকাল (১ম শিফট)" else "Morning (1st Shift)") else if (setupShift.contains("2") || setupShift.contains("Evening", ignoreCase = true) || setupShift.contains("বিকাল")) (if (isBengali) "বিকাল (২য় শিফট)" else "Evening (2nd Shift)") else (if (isBengali) "শিফট সিলেক্ট করুন" else "Select Shift"),
                    imagePainter = painterResource(Res.drawable.img_shift),
                    iconColor = Color(0xFFD97706),
                    isExpanded = expShift,
                    onToggleExpand = { expShift = !expShift },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThreeDOptionButton(
                            label = "Morning",
                            subLabel = null,
                            isSelected = setupShift.contains("1") || setupShift.contains("Morning", ignoreCase = true) || setupShift.contains("সকাল"),
                            onClick = { setupShift = "Morning (1st)" },
                            modifier = Modifier.weight(1f),
                            activeColor = Color(0xFFD97706),
                            isDark = isDark,
                        )
                        ThreeDOptionButton(
                            label = "Evening",
                            subLabel = null,
                            isSelected = setupShift.contains("2") || setupShift.contains("Evening", ignoreCase = true) || setupShift.contains("বিকাল"),
                            onClick = { setupShift = "Evening (2nd)" },
                            modifier = Modifier.weight(1f),
                            activeColor = Color(0xFF8B5CF6),
                            isDark = isDark,
                        )
                    }
                }

                // Card 3: Department - 7 3D Choice Buttons (Strictly English Buttons)
                Collapsible3DProfileCard(
                    title = if (isBengali) "ডিপার্টমেন্ট" else "Department",
                    selectedValue = if (setupDepartment.isNotBlank()) setupDepartment else (if (isBengali) "ডিপার্টমেন্ট সিলেক্ট করুন" else "Select Department"),
                    imagePainter = painterResource(Res.drawable.img_department),
                    iconColor = Color(0xFF059669),
                    isExpanded = expDept,
                    onToggleExpand = { expDept = !expDept },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val depts = listOf("CST", "CT", "ET", "MT", "PT", "EnT", "EMT")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            depts.take(4).forEach { dept ->
                                ThreeDOptionButton(
                                    label = dept,
                                    subLabel = null,
                                    isSelected = setupDepartment.equals(dept, ignoreCase = true),
                                    onClick = { setupDepartment = dept },
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFF059669),
                                    isDark = isDark,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            depts.drop(4).forEach { dept ->
                                ThreeDOptionButton(
                                    label = dept,
                                    subLabel = null,
                                    isSelected = setupDepartment.equals(dept, ignoreCase = true),
                                    onClick = { setupDepartment = dept },
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFF059669),
                                    isDark = isDark,
                                )
                            }
                        }
                    }
                }

                // Card 4: Section - 3 3D Choice Buttons (Strictly English Buttons)
                Collapsible3DProfileCard(
                    title = if (isBengali) "সেকশন" else "Section",
                    selectedValue = if (setupSection.isNotBlank()) "Section ${setupSection}" else (if (isBengali) "সেকশন সিলেক্ট করুন" else "Select Section"),
                    imagePainter = painterResource(Res.drawable.img_section),
                    iconColor = Color(0xFFEC4899),
                    isExpanded = expSection,
                    onToggleExpand = { expSection = !expSection },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val sections = listOf("A", "B", "C")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        sections.forEach { sec ->
                            ThreeDOptionButton(
                                label = sec,
                                subLabel = null,
                                isSelected = setupSection.equals(sec, ignoreCase = true),
                                onClick = { setupSection = sec },
                                modifier = Modifier.weight(1f),
                                activeColor = Color(0xFFEC4899),
                                isDark = isDark,
                            )
                        }
                    }
                }

                // Card 5: Roll Number
                Collapsible3DProfileCard(
                    title = if (isBengali) "রোল নম্বর" else "Roll Number",
                    selectedValue = if (setupRollNumber.isNotBlank()) setupRollNumber else (if (isBengali) "রোল নম্বর লিখুন" else "Enter Roll Number"),
                    imagePainter = painterResource(Res.drawable.img_roll),
                    iconColor = Color(0xFF6366F1),
                    isExpanded = expRoll,
                    onToggleExpand = { expRoll = !expRoll },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    ProfileInputField(if (isBengali) "রোল নং" else "Roll No", setupRollNumber, { setupRollNumber = it }, if (isBengali) "যেমন: ১২৩৪৫৬" else "e.g. 123456", isDark, hindSiliguri, textColor)
                }

                // Card 6: Phone Number
                Collapsible3DProfileCard(
                    title = if (isBengali) "ফোন নম্বর" else "Phone Number",
                    selectedValue = if (setupPhone.isNotBlank()) setupPhone else (if (isBengali) "ফোন নম্বর লিখুন" else "Enter Phone Number"),
                    imagePainter = painterResource(Res.drawable.img_phone),
                    iconColor = Color(0xFF10B981),
                    isExpanded = expPhone,
                    onToggleExpand = { expPhone = !expPhone },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    ProfileInputField(if (isBengali) "ফোন নাম্বার" else "Phone No", setupPhone, { setupPhone = it }, if (isBengali) "যেমন: ০১৭xxxxxxxx" else "e.g. 017xxxxxxxx", isDark, hindSiliguri, textColor)
                }

                // Card 7: Blood Group - 8 3D Choice Buttons (Strictly English Buttons)
                Collapsible3DProfileCard(
                    title = if (isBengali) "ব্লাড গ্রুপ" else "Blood Group",
                    selectedValue = if (setupBloodGroup.isNotBlank()) setupBloodGroup else (if (isBengali) "ব্লাড গ্রুপ সিলেক্ট করুন" else "Select Blood Group"),
                    imagePainter = painterResource(Res.drawable.img_blood),
                    iconColor = Color(0xFFEF4444),
                    isExpanded = expBlood,
                    onToggleExpand = { expBlood = !expBlood },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val bloods = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            bloods.take(4).forEach { bg ->
                                ThreeDOptionButton(
                                    label = bg,
                                    subLabel = null,
                                    isSelected = setupBloodGroup.equals(bg, ignoreCase = true),
                                    onClick = { setupBloodGroup = bg },
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFFEF4444),
                                    isDark = isDark,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            bloods.drop(4).forEach { bg ->
                                ThreeDOptionButton(
                                    label = bg,
                                    subLabel = null,
                                    isSelected = setupBloodGroup.equals(bg, ignoreCase = true),
                                    onClick = { setupBloodGroup = bg },
                                    modifier = Modifier.weight(1f),
                                    activeColor = Color(0xFFEF4444),
                                    isDark = isDark,
                                )
                            }
                        }
                    }
                }

                // Card 8: Address
                Collapsible3DProfileCard(
                    title = if (isBengali) "বর্তমান ঠিকানা" else "Current Address",
                    selectedValue = if (setupAddress.isNotBlank()) setupAddress else (if (isBengali) "ঠিকানা লিখুন" else "Enter Address"),
                    imagePainter = painterResource(Res.drawable.img_address),
                    iconColor = Color(0xFF8B5CF6),
                    isExpanded = expAddress,
                    onToggleExpand = { expAddress = !expAddress },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    ProfileInputField(if (isBengali) "ঠিকানা" else "Address", setupAddress, { setupAddress = it }, if (isBengali) "আপনার বর্তমান ঠিকানা লিখুন" else "Enter your current address", isDark, hindSiliguri, textColor, singleLine = false)
                }

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

                val isFormValid = setupSemester.isNotBlank() && setupShift.isNotBlank() &&
                    setupDepartment.isNotBlank() && setupSection.isNotBlank() &&
                    setupPhone.isNotBlank() && setupBloodGroup.isNotBlank() &&
                    setupAddress.isNotBlank() && setupRollNumber.isNotBlank()

                // Save Profile Button
                Button(
                    onClick = {
                        if (!isFormValid) {
                            studentProfileError = if (isBengali) "সকল তথ্য পূরণ করুন" else "Please fill in all details"
                            return@Button
                        }
                        studentProfileError = null
                        isSavingStudentProfile = true
                        coroutineScope.launch {
                            val shiftVal = if (setupShift.contains("Morning") || setupShift.contains("1") || setupShift.contains("সকাল")) "1" else "2"
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
                                setupTimerExpired = true // stop countdown timer
                                // Collapse all cards after successful save
                                expSemester = false
                                expShift = false
                                expDept = false
                                expSection = false
                                expRoll = false
                                expPhone = false
                                expBlood = false
                                expAddress = false
                            } else {
                                studentProfileError = if (isBengali) "সংরক্ষণ করা যায়নি। পুনরায় চেষ্টা করুন।" else "Save failed. Please try again."
                            }
                        }
                    },
                    enabled = !isSavingStudentProfile && !setupTimerExpired,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                ) {
                    if (isSavingStudentProfile) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isSavingStudentProfile) (if (isBengali) "সংরক্ষণ করা হচ্ছে..." else "Saving...") else (if (isBengali) "পরিবর্তন সংরক্ষণ করুন" else "Save Changes"),
                        fontSize = 14.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                    )
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

        // Account Delete Button
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

        Spacer(modifier = Modifier.imePadding().height(28.dp))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7),
                uncheckedThumbColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                uncheckedTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
            )
        )
    }
}

@Composable
private fun ThreeDOptionButton(
    label: String,
    subLabel: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF0284C7),
    isDark: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(12.dp)

    val pressOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessHigh)
    )

    val shadowColor = if (isSelected) {
        activeColor.copy(alpha = 0.5f)
    } else {
        if (isDark) Color(0xFF0F172A) else Color(0xFFCBD5E1)
    }

    val bgColor = if (isSelected) {
        activeColor
    } else {
        if (isDark) Color(0xFF334155) else Color(0xFFF8FAFC)
    }

    val contentColor = if (isSelected) {
        Color.White
    } else {
        if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    }

    val borderColor = if (isSelected) {
        activeColor.copy(alpha = 0.85f)
    } else {
        if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
    }

    Box(
        modifier = modifier.height(46.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        // 3D Shadow Base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 3.5.dp)
                .clip(shape)
                .background(shadowColor),
        )

        // Interactive 3D Surface
        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = shape,
            color = bgColor,
            border = BorderStroke(1.2.dp, borderColor),
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffset),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                    )
                    if (subLabel != null) {
                        Text(
                            text = subLabel,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Collapsible3DProfileCard(
    title: String,
    selectedValue: String,
    icon: ImageVector? = null,
    imagePainter: androidx.compose.ui.graphics.painter.Painter? = null,
    iconColor: Color = Color(0xFF0284C7),
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
    content: @Composable ColumnScope.() -> Unit,
) {
    val rotationAngle by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (isExpanded) iconColor.copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))),
        shadowElevation = if (isExpanded) 4.dp else 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            // Header Row (Clickable to collapse/expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    if (imagePainter != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, iconColor.copy(alpha = 0.4f)),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(38.dp),
                        ) {
                            Image(
                                painter = imagePainter,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    } else if (icon != null) {
                        Surface(
                            shape = CircleShape,
                            color = iconColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Text(
                            text = if (selectedValue.isNotBlank()) selectedValue else "নির্বাচন করতে ট্যাপ করুন",
                            fontSize = 12.sp,
                            fontFamily = hindSiliguri,
                            color = if (selectedValue.isNotBlank()) iconColor else subTextColor,
                            fontWeight = if (selectedValue.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }

                // Animated 3D Chevron Toggle Button
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = 2.5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExpanded) iconColor.copy(alpha = 0.5f)
                                else (if (isDark) Color(0xFF0F172A) else Color(0xFFCBD5E1))
                            ),
                    )
                    Surface(
                        onClick = onToggleExpand,
                        shape = CircleShape,
                        color = if (isExpanded) iconColor else (if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
                        border = BorderStroke(
                            1.dp,
                            if (isExpanded) iconColor else (if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
                        ),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = ChevronDownIcon,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = if (isExpanded) Color.White else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569)),
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(rotationZ = rotationAngle),
                            )
                        }
                    }
                }
            }

            // Expanded Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}
