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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.compose.ui.unit.Dp
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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
    topContentPadding: Dp = 0.dp,
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
                    text = "সাইন ইন করুন".localized(),
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

    // Student profile state
    var studentProfile by remember { mutableStateOf<StudentProfile?>(null) }
    var isLoadingStudentProfile by remember { mutableStateOf(false) }

    // Native Device Gallery Photo Picker Launcher
    val launchPhotoPicker = rememberPhotoPickerLauncher { pickedUri ->
        currentPhotoUrl = pickedUri
        studentProfile = studentProfile?.copy(photoUrl = pickedUri)
        onUpdateProfile?.invoke(userName, pickedUri)
        showAvatarPicker = false
    }

    // Admin sign in control state
    // signInControl is already declared at the top of ProfileScreen
    var isUpdatingControl by remember { mutableStateOf(false) }

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

    // Saved state tracking for per-card individual Save Info buttons
    var savedSemester by remember { mutableStateOf("") }
    var savedShift by remember { mutableStateOf("") }
    var savedDepartment by remember { mutableStateOf("") }
    var savedSection by remember { mutableStateOf("") }
    var savedPhone by remember { mutableStateOf("") }
    var savedBloodGroup by remember { mutableStateOf("") }
    var savedAddress by remember { mutableStateOf("") }
    var savedRollNumber by remember { mutableStateOf("") }

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
    var isDetectingLocation by remember { mutableStateOf(false) }
    var showMapPickerModal by remember { mutableStateOf(false) }
    var customAddressDetail by remember { mutableStateOf("") }

    var pendingAutoSaveLocation by remember { mutableStateOf<String?>(null) }
    val locationLauncher = rememberLocationPermissionLauncher { realAddress ->
        isDetectingLocation = false
        if (realAddress.isNotBlank()) {
            setupAddress = realAddress
            pendingAutoSaveLocation = realAddress
        }
    }

    val cleanEmail = email.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()

    LaunchedEffect(cleanEmail, role) {
        if (role == UserRole.ADMIN) {
            signInControl = RoleConfig.fetchSignInControl()
        } else if (role == UserRole.STUDENT && cleanEmail.isNotBlank()) {
            isLoadingStudentProfile = true
            val prof = RoleConfig.fetchStudentProfile(cleanEmail)
            studentProfile = prof
            if (prof != null) {
                setupSemester = prof.semester.filter { it.isDigit() }.ifEmpty { prof.semester }
                setupShift = if (prof.shift == "1" || prof.shift.contains("Morning", ignoreCase = true) || prof.shift.contains("১ম")) "1" else if (prof.shift == "2" || prof.shift.contains("Evening", ignoreCase = true) || prof.shift.contains("২য়")) "2" else prof.shift.filter { it.isDigit() }.ifEmpty { prof.shift }
                setupDepartment = prof.department
                setupSection = prof.section
                setupPhone = prof.phone
                setupBloodGroup = prof.bloodGroup
                setupAddress = prof.address
                setupRollNumber = prof.rollNumber

                savedSemester = setupSemester
                savedShift = setupShift
                savedDepartment = setupDepartment
                savedSection = setupSection
                savedPhone = setupPhone
                savedBloodGroup = setupBloodGroup
                savedAddress = setupAddress
                savedRollNumber = setupRollNumber

                val profPhoto = prof.photoUrl
                if (!profPhoto.isNullOrBlank() && (currentPhotoUrl.isBlank() || profPhoto.startsWith("data:image"))) {
                    currentPhotoUrl = profPhoto
                }
            }
            if (setupAddress.isBlank()) {
                isDetectingLocation = true
            }
            locationLauncher()
            isLoadingStudentProfile = false
        }
    }

    // 5-minute countdown for incomplete student profile setup
    LaunchedEffect(studentProfile?.profileSetupComplete) {
        if (role != UserRole.STUDENT) return@LaunchedEffect
        val isComplete = studentProfile?.profileSetupComplete == true
        if (isComplete || setupTimerExpired) return@LaunchedEffect

        var remaining = setupRemainingSeconds
        while (remaining > 0 && !setupTimerExpired && studentProfile?.profileSetupComplete != true) {
            delay(1000L)
            remaining--
            setupRemainingSeconds = remaining
        }
        if (!setupTimerExpired && studentProfile?.profileSetupComplete != true) {
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
        if (topContentPadding > 0.dp) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(modifier = Modifier.height(topContentPadding))
        }
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
                            model = resolveProfileImageModel(currentPhotoUrl),
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
                            contentDescription = "গ্যালারি থেকে ছবি আপলোড".localized(),
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Save Checkmark 3D Button
                val saveInteractionSource = remember { MutableInteractionSource() }
                val isSavePressed by saveInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier.clickable(
                        interactionSource = saveInteractionSource,
                        indication = null,
                        onClick = {
                            val finalName = inlineNameInput.trim().ifEmpty { userName }
                            onUpdateProfile?.invoke(finalName, currentPhotoUrl.ifBlank { null })
                            isEditingNameInline = false
                        },
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = 2.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF047857)),
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier
                            .size(38.dp)
                            .offset(y = if (isSavePressed) 2.dp else 0.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0xFF34D399), Color(0xFF10B981)))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = CheckIcon,
                                contentDescription = "সেভ করুন".localized(),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Cancel 3D Button
                val cancelInteractionSource = remember { MutableInteractionSource() }
                val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier.clickable(
                        interactionSource = cancelInteractionSource,
                        indication = null,
                        onClick = { isEditingNameInline = false },
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = 2.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFF94A3B8)),
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .size(38.dp)
                            .offset(y = if (isCancelPressed) 2.dp else 0.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isDark)
                                        Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                                    else
                                        Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = CloseIcon,
                                contentDescription = "বাতিল".localized(),
                                tint = textColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        } else {
            val editInteractionSource = remember { MutableInteractionSource() }
            val isEditPressed by editInteractionSource.collectIsPressedAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = userName.ifEmpty { "গেস্ট ইউজার" }.localized(),
                    fontSize = 20.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Modern 3D Tactile "Edit" Button
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = editInteractionSource,
                            indication = null,
                            onClick = {
                                inlineNameInput = userName
                                isEditingNameInline = true
                            },
                        ),
                ) {
                    // 3D Shadow Base
                    Box(
                        modifier = Modifier
                            .offset(y = 2.5.dp)
                            .matchParentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0369A1)),
                    )

                    // Front 3D Surface
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.8f)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.offset(y = if (isEditPressed) 2.dp else 0.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                    )
                                )
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = EditIcon,
                                    contentDescription = "নাম এডিট করুন".localized(),
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = "এডিট".localized(),
                                    fontSize = 11.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
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
                            text = "শিক্ষার্থী প্রোফাইল".localized(),
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
                    val timeDisplay = "${timerMinutes}:${timerSecs.toString().padStart(2, '0')}"
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3CD),
                        border = BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = "⚠️ ${"প্রোফাইল তথ্য প্রবেশের সময়সীমা:".localized()} $timeDisplay\n${"নির্দিষ্ট সময়ের পূর্বে সকল তথ্য সঠিকভাবে প্রদান করুন।".localized()}",
                            fontSize = 12.5.sp,
                            fontFamily = hindSiliguri,
                            color = Color(0xFF856404),
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Real-time auto-save helper function
                fun autoSaveStudentData(
                    sem: String = setupSemester,
                    sh: String = setupShift,
                    dept: String = setupDepartment,
                    sec: String = setupSection,
                    roll: String = setupRollNumber,
                    ph: String = setupPhone,
                    blood: String = setupBloodGroup,
                    addr: String = setupAddress,
                ) {
                    val shiftVal = if (sh.contains("Morning") || sh.contains("1") || sh.contains("সকাল")) "1" else if (sh.contains("Evening") || sh.contains("2") || sh.contains("বিকাল")) "2" else sh
                    val preservedPhoto = currentPhotoUrl.ifBlank { studentProfile?.photoUrl ?: "" }.ifBlank { null }
                    val updatedProf = StudentProfile(
                        email = cleanEmail,
                        name = userName,
                        photoUrl = preservedPhoto,
                        semester = sem,
                        shift = shiftVal,
                        department = dept,
                        section = sec,
                        phone = ph,
                        bloodGroup = blood,
                        address = addr,
                        rollNumber = roll,
                        profileSetupComplete = true,
                    )
                    studentProfile = updatedProf
                    savedSemester = sem
                    savedShift = shiftVal
                    savedDepartment = dept
                    savedSection = sec
                    savedRollNumber = roll
                    savedPhone = ph
                    savedBloodGroup = blood
                    savedAddress = addr
                    CoroutineScope(Dispatchers.IO + NonCancellable).launch {
                        RoleConfig.saveStudentProfile(updatedProf)
                    }
                }

                fun detectAutoMapAddress(targetAddr: String? = null) {
                    if (targetAddr != null && targetAddr.isNotBlank()) {
                        setupAddress = targetAddr
                        autoSaveStudentData(addr = targetAddr)
                    } else {
                        isDetectingLocation = true
                        locationLauncher()
                    }
                }

                LaunchedEffect(pendingAutoSaveLocation) {
                    val loc = pendingAutoSaveLocation
                    if (!loc.isNullOrBlank()) {
                        autoSaveStudentData(addr = loc)
                        pendingAutoSaveLocation = null
                    }
                }

                // Card 1: Semester - Options 1 to 7 Only (Compact Uniform 1:1 Buttons)
                Collapsible3DProfileCard(
                    title = "সেমিস্টার".localized(),
                    selectedValue = if (setupSemester.isNotBlank()) {
                        val semNum = setupSemester.filter { it.isDigit() }.ifEmpty { setupSemester }
                        if (isBengali) "${semNum}ম সেমিস্টার" else "${"সেমিস্টার".localized()} $semNum"
                    } else "সেমিস্টার নির্বাচন করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_semester),
                    gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                    isExpanded = expSemester,
                    onToggleExpand = { expSemester = !expSemester },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val semesters = listOf("1", "2", "3", "4", "5", "6", "7")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            semesters.take(4).forEach { sem ->
                                val semNum = setupSemester.filter { it.isDigit() }
                                val isSelected = semNum == sem || setupSemester.equals(sem, ignoreCase = true)
                                ThreeDOptionButton(
                                    label = sem,
                                    subLabel = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        setupSemester = sem
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFF0284C7),
                                    isDark = isDark,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            semesters.drop(4).forEach { sem ->
                                val semNum = setupSemester.filter { it.isDigit() }
                                val isSelected = semNum == sem || setupSemester.equals(sem, ignoreCase = true)
                                ThreeDOptionButton(
                                    label = sem,
                                    subLabel = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        setupSemester = sem
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFF0284C7),
                                    isDark = isDark,
                                )
                            }
                        }
                        CardSaveButton(
                            isChanged = setupSemester != savedSemester && setupSemester.isNotBlank(),
                            onSave = { autoSaveStudentData(sem = setupSemester) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Card 2: Shift - 2 Choice Buttons ("1" & "2", Compact Uniform 1:1 Buttons)
                Collapsible3DProfileCard(
                    title = "শিফট".localized(),
                    selectedValue = if (setupShift.contains("1") || setupShift.contains("Morning", ignoreCase = true) || setupShift.contains("১ম")) {
                        "১ম শিফট".localized()
                    } else if (setupShift.contains("2") || setupShift.contains("Evening", ignoreCase = true) || setupShift.contains("২য়")) {
                        "২য় শিফট".localized()
                    } else "শিফট নির্বাচন করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_shift),
                    gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                    isExpanded = expShift,
                    onToggleExpand = { expShift = !expShift },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val shifts = listOf("1", "2")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            shifts.forEachIndexed { index, sh ->
                                if (index > 0) Spacer(modifier = Modifier.width(14.dp))
                                val isSelected = setupShift == sh || setupShift.contains(sh) ||
                                    (sh == "1" && (setupShift.contains("Morning", ignoreCase = true) || setupShift.contains("সকাল"))) ||
                                    (sh == "2" && (setupShift.contains("Evening", ignoreCase = true) || setupShift.contains("বিকাল")))

                                ThreeDOptionButton(
                                    label = sh,
                                    subLabel = null,
                                    isSelected = isSelected,
                                    onClick = {
                                        setupShift = sh
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = if (sh == "1") Color(0xFFD97706) else Color(0xFF8B5CF6),
                                    isDark = isDark,
                                )
                            }
                        }
                        CardSaveButton(
                            isChanged = setupShift != savedShift && setupShift.isNotBlank(),
                            onSave = { autoSaveStudentData(sh = setupShift) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Card 3: Department - 7 Choice Buttons (Compact Uniform 1:1 Buttons Grid)
                Collapsible3DProfileCard(
                    title = "ডিপার্টমেন্ট".localized(),
                    selectedValue = if (setupDepartment.isNotBlank()) setupDepartment else "ডিপার্টমেন্ট নির্বাচন করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_department),
                    gradientColors = listOf(Color(0xFF059669), Color(0xFF047857)),
                    isExpanded = expDept,
                    onToggleExpand = { expDept = !expDept },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val depts = listOf("CST", "CT", "ET", "MT", "PT", "EnT", "EMT")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            depts.take(4).forEach { dept ->
                                ThreeDOptionButton(
                                    label = dept,
                                    subLabel = null,
                                    isSelected = setupDepartment.equals(dept, ignoreCase = true),
                                    onClick = {
                                        setupDepartment = dept
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFF059669),
                                    isDark = isDark,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            depts.drop(4).forEach { dept ->
                                ThreeDOptionButton(
                                    label = dept,
                                    subLabel = null,
                                    isSelected = setupDepartment.equals(dept, ignoreCase = true),
                                    onClick = {
                                        setupDepartment = dept
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFF059669),
                                    isDark = isDark,
                                )
                            }
                        }
                        CardSaveButton(
                            isChanged = setupDepartment != savedDepartment && setupDepartment.isNotBlank(),
                            onSave = { autoSaveStudentData(dept = setupDepartment) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Card 4: Section - 3 Choice Buttons (Compact Uniform 1:1 Buttons)
                Collapsible3DProfileCard(
                    title = "সেকশন".localized(),
                    selectedValue = if (setupSection.isNotBlank()) "${"সেকশন".localized()} $setupSection" else "সেকশন নির্বাচন করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_section),
                    gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                    isExpanded = expSection,
                    onToggleExpand = { expSection = !expSection },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val sections = listOf("A", "B", "C")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            sections.forEachIndexed { index, sec ->
                                if (index > 0) Spacer(modifier = Modifier.width(14.dp))
                                ThreeDOptionButton(
                                    label = sec,
                                    subLabel = null,
                                    isSelected = setupSection.equals(sec, ignoreCase = true),
                                    onClick = {
                                        setupSection = sec
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFFEC4899),
                                    isDark = isDark,
                                )
                            }
                        }
                        CardSaveButton(
                            isChanged = setupSection != savedSection && setupSection.isNotBlank(),
                            onSave = { autoSaveStudentData(sec = setupSection) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Section Title 1: একাডেমিক তথ্য / Academic Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                ) {
                    Icon(
                        imageVector = GraduationCapIcon,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "একাডেমিক তথ্য".localized(),
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1),
                    )
                }

                // Card 5: Roll Number
                Collapsible3DProfileCard(
                    title = "রোল নম্বর".localized(),
                    selectedValue = if (setupRollNumber.isNotBlank()) setupRollNumber else "রোল নম্বর প্রদান করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_roll),
                    gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                    isExpanded = expRoll,
                    onToggleExpand = { expRoll = !expRoll },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProfileInputField(
                            label = "রোল নম্বর".localized(),
                            value = setupRollNumber,
                            onValueChange = {
                                setupRollNumber = it
                            },
                            placeholder = "যেমন: ১২৩৪৫৬".localized(),
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            textColor = textColor,
                        )
                        CardSaveButton(
                            isChanged = setupRollNumber != savedRollNumber && setupRollNumber.isNotBlank(),
                            onSave = { autoSaveStudentData(roll = setupRollNumber) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Section Title 2: যোগাযোগের তথ্য / Contact Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                ) {
                    Icon(
                        imageVector = GearIcon,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "যোগাযোগের তথ্য".localized(),
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                    )
                }

                // Card 6: Phone Number
                Collapsible3DProfileCard(
                    title = "ফোন নম্বর".localized(),
                    selectedValue = if (setupPhone.isNotBlank()) setupPhone else "ফোন নম্বর প্রদান করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_phone),
                    gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    isExpanded = expPhone,
                    onToggleExpand = { expPhone = !expPhone },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProfileInputField(
                            label = "ফোন নম্বর".localized(),
                            value = setupPhone,
                            onValueChange = {
                                setupPhone = it
                            },
                            placeholder = "যেমন: ০১৭xxxxxxxx".localized(),
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            textColor = textColor,
                        )
                        CardSaveButton(
                            isChanged = setupPhone != savedPhone && setupPhone.isNotBlank(),
                            onSave = { autoSaveStudentData(ph = setupPhone) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Section Title 3: ডোনার তথ্য / Donor Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                ) {
                    Icon(
                        imageVector = StarIcon,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ডোনার তথ্য".localized(),
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                    )
                }

                // Card 7: Blood Group - 8 Choice Buttons (Compact Uniform 1:1 Buttons)
                Collapsible3DProfileCard(
                    title = "ব্লাড গ্রুপ".localized(),
                    selectedValue = if (setupBloodGroup.isNotBlank()) setupBloodGroup else "ব্লাড গ্রুপ নির্বাচন করুন".localized(),
                    imagePainter = painterResource(Res.drawable.img_blood),
                    gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                    isExpanded = expBlood,
                    onToggleExpand = { expBlood = !expBlood },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    val bloods = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            bloods.take(4).forEach { bg ->
                                ThreeDOptionButton(
                                    label = bg,
                                    subLabel = null,
                                    isSelected = setupBloodGroup.equals(bg, ignoreCase = true),
                                    onClick = {
                                        setupBloodGroup = bg
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFFEF4444),
                                    isDark = isDark,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            bloods.drop(4).forEach { bg ->
                                ThreeDOptionButton(
                                    label = bg,
                                    subLabel = null,
                                    isSelected = setupBloodGroup.equals(bg, ignoreCase = true),
                                    onClick = {
                                        setupBloodGroup = bg
                                    },
                                    modifier = Modifier.size(38.dp),
                                    activeColor = Color(0xFFEF4444),
                                    isDark = isDark,
                                )
                            }
                        }
                        CardSaveButton(
                            isChanged = setupBloodGroup != savedBloodGroup && setupBloodGroup.isNotBlank(),
                            onSave = { autoSaveStudentData(blood = setupBloodGroup) },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                // Section Title 4: ঠিকানা / Address
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                ) {
                    Icon(
                        imageVector = ShieldIcon,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ঠিকানা".localized(),
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6),
                    )
                }

                // Card 8: Auto Map Address
                Collapsible3DProfileCard(
                    title = "বর্তমান ঠিকানা".localized(),
                    selectedValue = if (isDetectingLocation) {
                        "ডিভাইস জিপিএস থেকে ঠিকানা সনাক্ত করা হচ্ছে...".localized()
                    } else if (setupAddress.isNotBlank()) {
                        setupAddress.localized()
                    } else {
                        "অটো জিপিএস ঠিকানা পেতে পারমিশন অনুমোদন করুন".localized()
                    },
                    imagePainter = painterResource(Res.drawable.img_address),
                    gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
                    isExpanded = expAddress,
                    onToggleExpand = {
                        val newExp = !expAddress
                        expAddress = newExp
                        if (newExp && setupAddress.isBlank()) {
                            detectAutoMapAddress()
                        }
                    },
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    hindSiliguri = hindSiliguri,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF2E1065) else Color(0xFFF3E8FF),
                            border = BorderStroke(1.2.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = ShieldIcon,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "অটো ডিভাইস জিপিএস ঠিকানা".localized(),
                                        fontSize = 13.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6),
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                if (isDetectingLocation) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF8B5CF6),
                                            strokeWidth = 2.dp,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "ডিভাইসের আসল জিপিএস থেকে ঠিকানা খোঁজা হচ্ছে...".localized(),
                                            fontSize = 12.sp,
                                            fontFamily = hindSiliguri,
                                            color = textColor,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = if (setupAddress.isNotBlank()) setupAddress.localized() else "অ্যান্ড্রয়েড জিপিএস পারমিশন অনুমোদন করুন".localized(),
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ ${"অনলাইনে আসলে ও অবস্থান পরিবর্তনে অটো সিঙ্ক হয়".localized()}",
                                    fontSize = 10.5.sp,
                                    fontFamily = hindSiliguri,
                                    color = Color(0xFF10B981),
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
                    contentDescription = "সাইন আউট".localized(),
                    modifier = Modifier.size(18.dp),
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সাইন আউট করুন".localized(),
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
                    text = "অ্যাকাউন্ট ডিলিট করুন".localized(),
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
                        text = "অ্যাকাউন্ট মুছে ফেলতে চান?".localized(),
                        fontSize = 17.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার অ্যাকাউন্ট, প্রোফাইল তথ্য এবং সমস্ত সেশন ডাটাবেস থেকে স্থায়ীভাবে মুছে যাবে। এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।".localized(),
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
                            Text(text = "বাতিল".localized(), fontFamily = hindSiliguri, fontSize = 13.sp)
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
                                Text(text = "ডিলিট করুন".localized(), fontFamily = hindSiliguri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            text = "নির্বাচন করুন".localized(),
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
    val shape = RoundedCornerShape(9.dp)

    val pressOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else if (isSelected) 1.5.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessHigh)
    )

    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessHigh)
    )

    val shadowColor = if (isSelected) {
        activeColor.copy(alpha = 0.6f)
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

    val labelFontSize = when {
        label.length <= 1 -> 15.sp
        label.length == 2 -> 13.sp
        label.length == 3 -> 11.5.sp
        else -> 10.sp
    }

    Box(
        modifier = modifier,
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
                .graphicsLayer(scaleX = pressScale, scaleY = pressScale)
                .offset(y = pressOffset),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = labelFontSize,
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
    gradientColors: List<Color> = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
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
    val primaryColor = gradientColors.first()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(
            1.2.dp,
            if (isExpanded) primaryColor.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.35f)
        ),
        shadowElevation = if (isExpanded) 6.dp else 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Surface matching ProfessionalMenuCard with full bleed artwork
            Surface(
                onClick = onToggleExpand,
                modifier = Modifier.fillMaxWidth().height(86.dp),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isExpanded) 0.dp else 20.dp,
                    bottomEnd = if (isExpanded) 0.dp else 20.dp,
                ),
                color = cardBg,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Full Card Background Image
                    if (imagePainter != null) {
                        Image(
                            painter = imagePainter,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.45f),
                            contentScale = ContentScale.Crop,
                        )
                        // Gradient Overlay ensuring foreground text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            cardBg.copy(alpha = if (isDark) 0.92f else 0.88f),
                                            cardBg.copy(alpha = if (isDark) 0.65f else 0.55f),
                                            Color.Transparent,
                                        ),
                                        startX = 0f,
                                        endX = 650f,
                                    )
                                )
                        )
                    }

                    // Content Row: Left 3D Thumbnail + Center Text + Right 3D Chevron
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 3D Avatar/Icon Badge
                        if (imagePainter != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = cardBg.copy(alpha = 0.9f),
                                border = BorderStroke(1.2.dp, primaryColor.copy(alpha = 0.6f)),
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(50.dp),
                            ) {
                                Image(
                                    painter = imagePainter,
                                    contentDescription = title,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        } else if (icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(gradientColors)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Text Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (selectedValue.isNotBlank()) selectedValue else "নির্বাচন করতে ট্যাপ করুন".localized(),
                                fontSize = 12.sp,
                                fontFamily = hindSiliguri,
                                color = if (selectedValue.isNotBlank()) primaryColor else subTextColor,
                                fontWeight = if (selectedValue.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                            )
                        }

                        // Modern 3D Animated Chevron Button
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(cardBg.copy(alpha = 0.88f))
                                    .border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = ChevronDownIcon,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = primaryColor,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .graphicsLayer(rotationZ = rotationAngle),
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Options Drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF8FAFC).copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun CardSaveButton(
    isChanged: Boolean,
    onSave: () -> Unit,
    hindSiliguri: FontFamily,
) {
    AnimatedVisibility(
        visible = isChanged,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onSave,
                ),
        ) {
            Box(
                modifier = Modifier
                    .offset(y = 3.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF047857)),
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .offset(y = if (isPressed) 3.dp else 0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CheckIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "তথ্য সংরক্ষণ করুন".localized(),
                            fontSize = 12.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
