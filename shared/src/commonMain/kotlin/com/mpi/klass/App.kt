package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.AppTheme
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.getHindSiliguriFontFamily
import com.mpi.klass.theme.loadThemeMode
import com.mpi.klass.theme.saveThemeMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.error404
import klass.shared.generated.resources.timeouterror
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

sealed interface AppScreen {
    data object Welcome : AppScreen
    data object GuestHome : AppScreen
    data class RoleHome(
        val role: UserRole = UserRole.ADMIN,
        val email: String,
        val name: String,
        val photoUrl: String? = null,
        // Initial tab to show on entry ("home" or "profile")
        val initialTab: String = "home",
        val showPasswordSetupOnEntry: Boolean = false,
    ) : AppScreen
    data class ProfileSetup(
        val role: UserRole,
        val email: String,
        val name: String,
        val photoUrl: String?,
        val profile: StudentProfile,
        val showPasswordSetupOnEntry: Boolean = false,
    ) : AppScreen
}

enum class ErrorImageType {
    NO_INTERNET,      // error404.webp (No Internet / Network Error)
    GENERAL_ERROR,   // timeouterror.webp (Auth failure, wrong email, timeout, or server error)
    CONFIGURATION_ERROR,
    SIGN_IN_DISABLED,  // Sign-in is disabled by admin
    ADMIN_NOT_AUTHORIZED,
    ADMIN_REQUEST_PENDING,
    ADMIN_REQUEST_DECLINED,
    ROLE_MISMATCH,
}

private fun errorTypeFor(exceptionMessage: String?): ErrorImageType = when (exceptionMessage) {
    "NO_INTERNET" -> ErrorImageType.NO_INTERNET
    "WEB_FIREBASE_CONFIG_MISSING" -> ErrorImageType.CONFIGURATION_ERROR
    else -> ErrorImageType.GENERAL_ERROR
}

@Composable
fun App(
    onLaunchGoogleSignIn: (callback: (email: String?, name: String?, photoUrl: String?, errorMsg: String?) -> Unit) -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onCloseApp: () -> Unit = {},
) {
    var themeMode by remember { mutableStateOf(loadThemeMode()) }
    var appLanguage by remember { mutableStateOf(loadAppLanguage()) }
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    var currentScreen by remember {
        mutableStateOf(
            try {
                val session = loadUserSession()
                when {
                    session == null || session.role.trim().uppercase() == "GUEST" -> {
                        clearUserSession()
                        AppScreen.Welcome
                    }
                    else -> {
                        AppScreen.RoleHome(
                            role = UserRole.fromRaw(session.role) ?: UserRole.STUDENT,
                            email = session.email,
                            name = session.name,
                            photoUrl = session.photoUrl,
                        )
                    }
                }
            } catch (_: Exception) {
                AppScreen.Welcome
            }
        )
    }
    var activeErrorType by remember { mutableStateOf<ErrorImageType?>(null) }
    var lastErrorType by remember { mutableStateOf(ErrorImageType.GENERAL_ERROR) }
    var lastErrorDetail by remember { mutableStateOf<String?>(null) }
    var isLoadingOverlay by remember { mutableStateOf(value = false) }

    val coroutineScope = rememberCoroutineScope()
    val hindSiliguri = getHindSiliguriFontFamily()

    var pendingWelcomePush by remember { mutableStateOf<Pair<String, String>?>(null) }
    val requestNotificationPermission = rememberNotificationPermissionLauncher { granted ->
        val pending = pendingWelcomePush
        if (granted && pending != null) {
            sendWelcomePushNotification(pending.first, pending.second)
            pendingWelcomePush = null
        }
    }

    fun triggerWelcomePush(userName: String, roleLabel: String) {
        if (hasNotificationPermission()) {
            sendWelcomePushNotification(userName, roleLabel)
            pendingWelcomePush = null
        } else {
            pendingWelcomePush = userName to roleLabel
            requestNotificationPermission()
        }
    }

    fun showError(type: ErrorImageType, detail: String? = null) {
        lastErrorType = type
        lastErrorDetail = detail?.takeIf { it.isNotBlank() }
        activeErrorType = type
    }

    fun openAuthenticatedScreen(
        role: UserRole,
        email: String,
        name: String,
        photoUrl: String?,
        showPasswordSetup: Boolean,
    ) {
        coroutineScope.launch {
            if (role != UserRole.ADMIN) {
                val profile = RoleConfig.fetchStudentProfile(email)
                if (profile?.profileSetupComplete != true) {
                    val setupProfile = profile ?: StudentProfile(
                        email = email.trim().lowercase(),
                        name = name,
                        photoUrl = photoUrl,
                        profileSetupComplete = false,
                    )
                    currentScreen = AppScreen.ProfileSetup(
                        role = role,
                        email = email,
                        name = setupProfile.name.ifBlank { name },
                        photoUrl = setupProfile.photoUrl ?: photoUrl,
                        profile = setupProfile,
                        showPasswordSetupOnEntry = showPasswordSetup,
                    )
                    isLoadingOverlay = false
                    return@launch
                }
            }

            currentScreen = AppScreen.RoleHome(
                role = role,
                email = email,
                name = name,
                photoUrl = photoUrl,
                initialTab = "home",
                showPasswordSetupOnEntry = showPasswordSetup,
            )
            isLoadingOverlay = false
        }
    }

    // Verify saved session & initialize online dynamic language dictionary on startup
    LaunchedEffect(Unit) {
        AppStrings.preloadCacheSync()
        coroutineScope.launch {
            AppStrings.initializeOnlineDictionary()
        }
        val session = loadUserSession()
        if (session == null || session.role.trim().uppercase() == "GUEST") {
            return@LaunchedEffect
        }
        val sessionRole = UserRole.fromRaw(session.role) ?: UserRole.STUDENT
        if (sessionRole != UserRole.ADMIN) {
            val profile = RoleConfig.fetchStudentProfile(session.email)
            if (profile?.profileSetupComplete != true) {
                val setupProfile = profile ?: StudentProfile(
                    email = session.email.trim().lowercase(),
                    name = session.name,
                    photoUrl = session.photoUrl,
                    profileSetupComplete = false,
                )
                currentScreen = AppScreen.ProfileSetup(
                    role = sessionRole,
                    email = session.email,
                    name = setupProfile.name.ifBlank { session.name },
                    photoUrl = setupProfile.photoUrl ?: session.photoUrl,
                    profile = setupProfile,
                )
            }
        }
        if (!hasNotificationPermission()) {
            requestNotificationPermission()
        }
        try {
            val isAdmin = session.role.trim().uppercase() == UserRole.ADMIN.name
            if (isAdmin && isNetworkAvailable()) {
                val adminStatus = RoleConfig.getAdminStatusFromFirestore(session.email)
                if (adminStatus == AdminStatus.DECLINED) {
                    clearUserSession()
                    currentScreen = AppScreen.Welcome
                }
            }
        } catch (_: Exception) {}
    }

    fun launchRoleSignIn(targetRole: UserRole) {
        isLoadingOverlay = true
        onLaunchGoogleSignIn { email, name, photoUrl, errorMsg ->
            when {
                errorMsg != null -> {
                    isLoadingOverlay = false
                    onSignOutGoogle()
                    showError(ErrorImageType.GENERAL_ERROR, errorMsg)
                }

                email != null -> {
                    coroutineScope.launch {
                        val cleanEmail = email.trim().lowercase()
                        val control = RoleConfig.fetchSignInControl()

                        // Strict 1-Email = 1-Role category lock
                        val existingRegisteredRole = RoleConfig.resolveRegisteredRole(cleanEmail)
                        if (existingRegisteredRole != null && existingRegisteredRole != targetRole) {
                            val existingLabel = when (existingRegisteredRole) {
                                UserRole.ADMIN -> "এডমিন (Admin)"
                                UserRole.TEACHER -> "শিক্ষক (Teacher)"
                                UserRole.STUDENT -> "শিক্ষার্থী (Student)"
                            }
                            isLoadingOverlay = false
                            onSignOutGoogle()
                            showError(
                                ErrorImageType.ROLE_MISMATCH,
                                "এই ইমেইলটি ($cleanEmail) ইতোমধ্যেই $existingLabel হিসেবে নিবন্ধিত। প্রতিটি ইমেইল দিয়ে শুধুমাত্র একটি ক্যাটাগরিতেই সাইন-ইন করা যাবে।",
                            )
                            return@launch
                        }

                        if (targetRole == UserRole.ADMIN) {
                            if (!control.adminSignInEnabled) {
                                val isExisting = RoleConfig.isUserRegisteredForRole(cleanEmail, UserRole.ADMIN)
                                if (!isExisting) {
                                    isLoadingOverlay = false
                                    onSignOutGoogle()
                                    showError(ErrorImageType.SIGN_IN_DISABLED)
                                    return@launch
                                }
                            }

                            val localAdminProf = RoleConfig.fetchLocalProfile(cleanEmail)
                            val remoteAdminProf = RoleConfig.fetchRemoteUserProfile(cleanEmail)
                            val savedAdminName = localAdminProf?.name?.ifBlank { null }
                                ?: remoteAdminProf?.name?.ifBlank { null }
                            val adminPhotoCandidates = listOfNotNull(
                                localAdminProf?.photoUrl?.ifBlank { null },
                                remoteAdminProf?.photoUrl?.ifBlank { null },
                            )
                            val savedAdminPhoto = adminPhotoCandidates.firstOrNull { it.startsWith("data:image") }
                                ?: adminPhotoCandidates.firstOrNull()

                            val userName = savedAdminName ?: name ?: cleanEmail.substringBefore("@")
                            val finalAdminPhoto = savedAdminPhoto ?: photoUrl
                            val adminStatus = RoleConfig.getAdminStatusFromFirestore(cleanEmail)
                            when (adminStatus) {
                                AdminStatus.APPROVED -> {
                                    RoleConfig.saveAdminEmail(cleanEmail, userName, finalAdminPhoto)
                                    RoleConfig.saveUserProfile(cleanEmail, userName, finalAdminPhoto, "google", UserRole.ADMIN.name)
                                    saveUserSession(UserSession(role = UserRole.ADMIN.name, email = cleanEmail, name = userName, photoUrl = finalAdminPhoto))
                                    val hasPassword = RoleConfig.hasPasswordSet(cleanEmail)
                                    triggerWelcomePush(userName, "Admin")
                                    currentScreen = AppScreen.RoleHome(
                                        role = UserRole.ADMIN,
                                        email = cleanEmail,
                                        name = userName,
                                        photoUrl = finalAdminPhoto,
                                        showPasswordSetupOnEntry = !hasPassword,
                                    )
                                    isLoadingOverlay = false
                                }
                                AdminStatus.PENDING -> {
                                    isLoadingOverlay = false
                                    onSignOutGoogle()
                                    showError(ErrorImageType.ADMIN_REQUEST_PENDING)
                                }
                                AdminStatus.DECLINED -> {
                                    isLoadingOverlay = false
                                    onSignOutGoogle()
                                    showError(ErrorImageType.ADMIN_REQUEST_DECLINED)
                                }
                                AdminStatus.NOT_REQUESTED -> {
                                    RoleConfig.submitAdminRequestToFirestore(cleanEmail, userName, finalAdminPhoto)
                                    isLoadingOverlay = false
                                    onSignOutGoogle()
                                    showError(ErrorImageType.ADMIN_REQUEST_PENDING)
                                }
                            }
                            return@launch
                        }

                        // For TEACHER / STUDENT sign in: preserve strict role lock (actualRole == targetRole)
                        val existingProfile = RoleConfig.fetchUserProfile(cleanEmail)
                        val actualRole = targetRole

                        val isEnabled = when (actualRole) {
                            UserRole.ADMIN -> control.adminSignInEnabled
                            UserRole.TEACHER -> control.teacherSignInEnabled
                            UserRole.STUDENT -> control.studentSignInEnabled
                        }

                        if (!isEnabled) {
                            val isExisting = RoleConfig.isUserRegisteredForRole(cleanEmail, actualRole)
                            if (!isExisting) {
                                isLoadingOverlay = false
                                onSignOutGoogle()
                                showError(ErrorImageType.SIGN_IN_DISABLED)
                                return@launch
                            }
                        }

                        val localProfile = RoleConfig.fetchLocalProfile(cleanEmail)
                        val remoteProfile = RoleConfig.fetchRemoteUserProfile(cleanEmail)
                        val studentProf = RoleConfig.fetchStudentProfile(cleanEmail)

                        val savedName = localProfile?.name?.ifBlank { null }
                            ?: studentProf?.name?.ifBlank { null }
                            ?: existingProfile?.name?.ifBlank { null }
                            ?: remoteProfile?.name?.ifBlank { null }

                        val photoCandidates = listOfNotNull(
                            localProfile?.photoUrl?.ifBlank { null },
                            studentProf?.photoUrl?.ifBlank { null },
                            existingProfile?.photoUrl?.ifBlank { null },
                            remoteProfile?.photoUrl?.ifBlank { null },
                        )
                        val savedPhoto = photoCandidates.firstOrNull { it.startsWith("data:image") }
                            ?: photoCandidates.firstOrNull()

                        val finalName = savedName ?: name ?: cleanEmail.substringBefore("@")
                        val finalPhoto = savedPhoto ?: photoUrl

                        val saveResult = RoleConfig.saveUserProfile(cleanEmail, finalName, finalPhoto, "google", actualRole.name)
                        if (saveResult.isFailure && isNetworkAvailable()) {
                            val errMsg = saveResult.exceptionOrNull()?.localizedMessage.orEmpty()
                            if (errMsg.contains("PERMISSION_DENIED", ignoreCase = true) ||
                                errMsg.contains("NOT_FOUND", ignoreCase = true)
                            ) {
                                isLoadingOverlay = false
                                onSignOutGoogle()
                                showError(
                                    ErrorImageType.GENERAL_ERROR,
                                    "Firestore ডাটাবেসে সংরক্ষণ ব্যর্থ হয়েছে ($errMsg)। Firebase Console-এ Firestore Database তৈরি ও firestore.rules ডিপ্লয় করা আছে কিনা যাচাই করুন।",
                                )
                                return@launch
                            }
                        }
                        saveUserSession(UserSession(role = actualRole.name, email = cleanEmail, name = finalName, photoUrl = finalPhoto))
                        val hasPassword = RoleConfig.hasPasswordSet(cleanEmail)
                        val roleTitle = when (actualRole) {
                            UserRole.ADMIN -> "Admin"
                            UserRole.TEACHER -> "Teacher"
                            UserRole.STUDENT -> "Student"
                        }
                        triggerWelcomePush(finalName, roleTitle)
                        openAuthenticatedScreen(
                            role = actualRole,
                            email = cleanEmail,
                            name = finalName,
                            photoUrl = finalPhoto,
                            showPasswordSetup = !hasPassword,
                        )
                    }
                }

                else -> {
                    isLoadingOverlay = false
                }
            }
        }
    }

    fun launchEmailPasswordLogin(emailInput: String, passwordInput: String) {
        isLoadingOverlay = true
        coroutineScope.launch {
            when (val res = RoleConfig.loginWithEmailPassword(emailInput, passwordInput)) {
                is EmailPasswordLoginResult.Success -> {
                    saveUserSession(
                        UserSession(
                            role = res.role.name,
                            email = res.email,
                            name = res.name,
                            photoUrl = res.photoUrl,
                        )
                    )
                    val roleTitle = when (res.role) {
                        UserRole.ADMIN -> "Admin"
                        UserRole.TEACHER -> "Teacher"
                        UserRole.STUDENT -> "Student"
                    }
                    triggerWelcomePush(res.name, roleTitle)
                    openAuthenticatedScreen(
                        role = res.role,
                        email = res.email,
                        name = res.name,
                        photoUrl = res.photoUrl,
                        showPasswordSetup = false,
                    )
                }
                is EmailPasswordLoginResult.Error -> {
                    isLoadingOverlay = false
                    showError(res.errorType, res.detail)
                }
            }
        }
    }

    fun launchGuestMode() {
        isLoadingOverlay = true
        coroutineScope.launch {
            clearUserSession()
            cancelAllClassPushNotifications()
            triggerWelcomePush("Guest", "Guest")
            currentScreen = AppScreen.GuestHome
            isLoadingOverlay = false
        }
    }



    // Signing out is a purely local action; it must not require internet.
    fun signOutAndReturnToWelcome() {
        clearUserSession()
        cancelAllClassPushNotifications()
        onSignOutGoogle()
        currentScreen = AppScreen.Welcome
    }

    fun navigateBackToWelcomeWithoutSignOut() {
        currentScreen = AppScreen.Welcome
    }

    AppTheme(themeMode = themeMode) {
        CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background,
            ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize(),
                ) { screen ->
                    when (screen) {
                        AppScreen.Welcome -> {
                            WelcomeScreen(
                                currentThemeMode = themeMode,
                                onThemeModeChange = { newMode ->
                                    themeMode = newMode
                                    saveThemeMode(newMode)
                                },
                                currentLanguage = appLanguage,
                                onLanguageChange = { newLanguage ->
                                    appLanguage = newLanguage
                                    saveAppLanguage(newLanguage)
                                    if (newLanguage == AppLanguage.ENGLISH) {
                                        coroutineScope.launch {
                                            AppStrings.initializeOnlineDictionary()
                                        }
                                    }
                                },
                                onGuestClick = { launchGuestMode() },
                                onStudentClick = { launchRoleSignIn(UserRole.STUDENT) },
                                onTeacherClick = { launchRoleSignIn(UserRole.TEACHER) },
                                onAdminClick = { launchRoleSignIn(UserRole.ADMIN) },
                                onEmailPasswordLogin = { emailInput, passwordInput ->
                                    launchEmailPasswordLogin(emailInput, passwordInput)
                                },
                                onCloseApp = onCloseApp,
                            )
                        }

                        is AppScreen.ProfileSetup -> {
                            ProfileSetupScreen(
                                role = screen.role,
                                initialProfile = screen.profile,
                                hindSiliguri = hindSiliguri,
                                isDark = isDark,
                                onSave = { updatedProfile ->
                                    val provider = RoleConfig.fetchUserProfile(screen.email)
                                        ?.provider?.ifBlank { "google" } ?: "google"
                                    val userResult = RoleConfig.saveUserProfile(
                                        email = screen.email,
                                        name = updatedProfile.name,
                                        photoUrl = updatedProfile.photoUrl,
                                        provider = provider,
                                        category = screen.role.name,
                                    )
                                    if (userResult.isFailure) {
                                        userResult
                                    } else {
                                        RoleConfig.saveStudentProfile(updatedProfile)
                                    }
                                },
                                onComplete = { completedProfile ->
                                    saveUserSession(
                                        UserSession(
                                            role = screen.role.name,
                                            email = screen.email,
                                            name = completedProfile.name,
                                            photoUrl = completedProfile.photoUrl,
                                        )
                                    )
                                    currentScreen = AppScreen.RoleHome(
                                        role = screen.role,
                                        email = screen.email,
                                        name = completedProfile.name,
                                        photoUrl = completedProfile.photoUrl,
                                        initialTab = if (screen.role == UserRole.TEACHER) "teacher_prefs" else "home",
                                        showPasswordSetupOnEntry = screen.showPasswordSetupOnEntry,
                                    )
                                },
                            )
                        }

                        AppScreen.GuestHome -> {
                            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                                GuestHomeScreen(
                                    currentThemeMode = themeMode,
                                    onThemeModeChange = { newMode ->
                                        themeMode = newMode
                                        saveThemeMode(newMode)
                                    },
                                    currentLanguage = appLanguage,
                                    onLanguageChange = { newLanguage ->
                                        appLanguage = newLanguage
                                        saveAppLanguage(newLanguage)
                                        if (newLanguage == AppLanguage.ENGLISH) {
                                            coroutineScope.launch {
                                                AppStrings.initializeOnlineDictionary()
                                            }
                                        }
                                    },
                                    onSwitchToLogin = { signOutAndReturnToWelcome() },
                                    onCloseApp = onCloseApp,
                                    onBackToWelcome = { navigateBackToWelcomeWithoutSignOut() },
                                )
                            }
                        }

                        is AppScreen.RoleHome -> {
                            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                                RoleHomeScreen(
                                    role = screen.role,
                                    userName = screen.name,
                                    photoUrl = screen.photoUrl,
                                    email = screen.email,
                                    currentThemeMode = themeMode,
                                    onThemeModeChange = { newMode ->
                                        themeMode = newMode
                                        saveThemeMode(newMode)
                                    },
                                    currentLanguage = appLanguage,
                                    onLanguageChange = { newLanguage ->
                                        appLanguage = newLanguage
                                        saveAppLanguage(newLanguage)
                                        if (newLanguage == AppLanguage.ENGLISH) {
                                            coroutineScope.launch {
                                                AppStrings.initializeOnlineDictionary()
                                            }
                                        }
                                    },
                                    onSignOut = { signOutAndReturnToWelcome() },
                                    onCloseApp = onCloseApp,
                                    onBackToWelcome = { navigateBackToWelcomeWithoutSignOut() },
                                    onProfileUpdated = { newName, newPhotoUrl ->
                                        currentScreen = screen.copy(name = newName, photoUrl = newPhotoUrl)
                                    },
                                    initialTab = screen.initialTab,
                                    promptPasswordSetup = screen.showPasswordSetupOnEntry,
                                )
                            }
                        }
                    }
                }

                // Ultra-Crisp Floating 3 Mechanical Gears Loader
                AnimatedVisibility(
                    visible = isLoadingOverlay,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.zIndex(100f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(115.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(rotationZ = 45f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(rotationZ = -45f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    SmartMechanicalGearsLoader(isDark = isDark)
                                }
                            }
                        }
                    }
                }

                // Error Popup Card Overlay (modal card with scrim)
                AnimatedVisibility(
                    visible = activeErrorType != null,
                    enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = 700f),
                    ),
                    exit = fadeOut(animationSpec = tween(160)) + scaleOut(
                        targetScale = 0.94f,
                        animationSpec = tween(160),
                    ),
                    modifier = Modifier.zIndex(100f),
                ) {
                    CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                        ErrorImagePopup(
                            errorType = activeErrorType ?: lastErrorType,
                            errorDetail = lastErrorDetail,
                            isDark = isDark,
                            onDismiss = {
                                activeErrorType = null
                                lastErrorDetail = null
                            },
                        )
                    }
                }

                // Designed Floating Toast Overlay
                AppToastHost(
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(120f),
                )
            }
        }
    }
}
}

@Composable
private fun AppToastHost(
    isDark: Boolean,
    hindSiliguri: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier,
) {
    val activeToast = AppToastState.currentToast
    var lastToast by remember { mutableStateOf<AppToastMessage?>(null) }
    if (activeToast != null) {
        lastToast = activeToast
    }

    LaunchedEffect(activeToast?.id) {
        val currentId = activeToast?.id ?: return@LaunchedEffect
        delay(2400L)
        AppToastState.dismiss(currentId)
    }

    AnimatedVisibility(
        visible = activeToast != null,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        val display = activeToast ?: lastToast ?: return@AnimatedVisibility
        val accent = when (display.tone) {
            AppToastTone.SUCCESS -> KlassFeaturePalette.success(isDark).primary
            AppToastTone.ERROR -> KlassFeaturePalette.error(isDark).primary
            AppToastTone.INFO -> KlassFeaturePalette.routine(isDark).primary
        }
        val icon = when (display.tone) {
            AppToastTone.SUCCESS -> CheckIcon
            AppToastTone.ERROR -> ShieldIcon
            AppToastTone.INFO -> InfoCircleIcon
        }
        val surfaceBg = if (isDark) Color(0xFF151F32).copy(alpha = 0.96f) else Color(0xFFFFFFFF).copy(alpha = 0.97f)
        val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceBg,
            border = BorderStroke(1.dp, accent.copy(alpha = if (isDark) 0.55f else 0.40f)),
            shadowElevation = 0.dp,
            onClick = { AppToastState.dismiss(display.id) },
            modifier = Modifier
                .padding(bottom = 52.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.24f else 0.14f),
                    modifier = Modifier.size(26.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Text(
                    text = display.message,
                    color = textColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = hindSiliguri,
                    maxLines = 2,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
    }
}



// ─── Error Popup ─────────────────────────────────────────────────────────────

@Composable
fun ErrorImagePopup(
    errorType: ErrorImageType,
    errorDetail: String? = null,
    isDark: Boolean,
    onDismiss: () -> Unit,
) {
    val drawableRes = when (errorType) {
        ErrorImageType.NO_INTERNET -> Res.drawable.error404
        ErrorImageType.GENERAL_ERROR -> Res.drawable.timeouterror
        ErrorImageType.CONFIGURATION_ERROR -> Res.drawable.timeouterror
        ErrorImageType.SIGN_IN_DISABLED -> Res.drawable.timeouterror
        ErrorImageType.ADMIN_NOT_AUTHORIZED -> Res.drawable.timeouterror
        ErrorImageType.ADMIN_REQUEST_PENDING -> Res.drawable.timeouterror
        ErrorImageType.ADMIN_REQUEST_DECLINED -> Res.drawable.timeouterror
        ErrorImageType.ROLE_MISMATCH -> Res.drawable.timeouterror
    }

    val title = when (errorType) {
        ErrorImageType.NO_INTERNET -> "ইন্টারনেট সংযোগ নেই!"
        ErrorImageType.GENERAL_ERROR -> "অনুরোধ সম্পূর্ণ করা যায়নি!"
        ErrorImageType.CONFIGURATION_ERROR -> "ওয়েব সেটআপ সম্পূর্ণ হয়নি"
        ErrorImageType.SIGN_IN_DISABLED -> "সাইন ইন বন্ধ আছে"
        ErrorImageType.ADMIN_NOT_AUTHORIZED -> "এই Google অ্যাকাউন্ট অনুমোদিত নয়"
        ErrorImageType.ADMIN_REQUEST_PENDING -> "এডমিন রিকোয়েস্ট পেন্ডিং রয়েছে"
        ErrorImageType.ADMIN_REQUEST_DECLINED -> "এডমিন রিকোয়েস্ট প্রত্যাখ্যাত হয়েছে"
        ErrorImageType.ROLE_MISMATCH -> "ভুল ক্যাটাগরি নির্বাচন"
    }

    val description = when (errorType) {
        ErrorImageType.NO_INTERNET -> "আপনার ডিভাইসে সক্রিয় ইন্টারনেট সংযোগ পাওয়া যায়নি। অ্যাপটির সমস্ত সুবিধা পেতে ইন্টারনেট কানেকশন প্রয়োজন।"
        ErrorImageType.GENERAL_ERROR -> if (!errorDetail.isNullOrBlank()) {
            "সাইন-ইন সম্পন্ন করা সম্ভব হয়নি: $errorDetail"
        } else {
            "আপনার অনুরোধটি সম্পূর্ণ করা সম্ভব হয়নি। প্রবেশকৃত জিমেইল অ্যাকাউন্টে সমস্যা থাকতে পারে অথবা নেটওয়ার্ক ও সার্ভার সংযোগে বিলম্ব হচ্ছে।"
        }
        ErrorImageType.CONFIGURATION_ERROR -> "Firebase কনফিগারেশন চেক করুন। Google sign-in provider এবং নেটওয়ার্ক সেটিংস যাচাই করুন।"
        ErrorImageType.SIGN_IN_DISABLED -> "এডমিন এই ধরনের সাইন ইন সাময়িকভাবে বন্ধ রেখেছেন। পরে আবার চেষ্টা করুন অথবা এডমিনের সাথে যোগাযোগ করুন।"
        ErrorImageType.ADMIN_NOT_AUTHORIZED -> "শুধু Firebase Firestore-এর admins কালেকশনে থাকা অনুমোদিত email দিয়ে এডমিন হিসেবে প্রবেশ করা যাবে।"
        ErrorImageType.ADMIN_REQUEST_PENDING -> "আপনার এডমিন অ্যাক্সেস রিকোয়েস্টটি পেন্ডিং রয়েছে। পূর্বে অনুমোদিত কোনো এডমিন আপনার ইমেইলটি অনুমোদন (Approve) করলে আপনি এডমিন হিসেবে প্রবেশ করতে পারবেন।"
        ErrorImageType.ADMIN_REQUEST_DECLINED -> "আপনার এডমিন অ্যাক্সেস রিকোয়েস্টটি এডমিন কর্তৃক বাতিল বা প্রত্যাখ্যান (Decline) করা হয়েছে।"
        ErrorImageType.ROLE_MISMATCH -> errorDetail?.takeIf { it.isNotBlank() }
            ?: "এই ইমেইলটি ইতোমধ্যেই অন্য একটি ক্যাটাগরিতে নিবন্ধিত রয়েছে। এক ইমেইল দিয়ে একাধিক ক্যাটাগরিতে সাইন-ইন করা সম্ভব নয়।"
    }

    val steps = when (errorType) {
        ErrorImageType.NO_INTERNET -> listOf(
            "আপনার ডিভাইসের Wi-Fi অথবা মোবাইল ডাটা চালু আছে কিনা চেক করুন।",
            "ইন্টারনেট সংযোগ সক্রিয় করে পুনরায় চেষ্টা করুন।",
        )
        ErrorImageType.GENERAL_ERROR -> listOf(
            "আপনার জিমেইল অ্যাকাউন্টটি সঠিক এবং নিবন্ধিত কিনা নিশ্চিত করুন।",
            "নেটওয়ার্ক সংযোগ পরীক্ষা করে কিছুক্ষণ পর পুনরায় চেষ্টা করুন।",
        )
        ErrorImageType.CONFIGURATION_ERROR -> listOf(
            "Firebase Project settings থেকে Web App config কপি করুন।",
            "Authentication-এ Google provider এবং Hosting domain অনুমোদন করুন।",
            "পরিবর্তনের পর ওয়েব অ্যাপটি আবার build ও deploy করুন।",
        )
        ErrorImageType.SIGN_IN_DISABLED -> listOf(
            "এডমিন এই রোলের সাইন ইন অনুমতি বন্ধ রেখেছেন।",
            "এডমিনের সাথে যোগাযোগ করুন এবং সাইন ইন চালু করতে অনুরোধ করুন।",
        )
        ErrorImageType.ADMIN_NOT_AUTHORIZED -> listOf(
            "Firebase Console-এ Firestore Database > admins কালেকশনে আপনার email ডকুমেন্ট হিসেবে যোগ করুন।",
            "অনুমোদিত ইমেইল অ্যাকাউন্ট দিয়ে পুনরায় চেষ্টা করুন।",
        )
        ErrorImageType.ADMIN_REQUEST_PENDING -> listOf(
            "বর্তমান অনুমোদিত এডমিনের সাথে যোগাযোগ করুন।",
            "এডমিনকে অ্যাপের মেনু স্ক্রিনের 'এডমিন রিকোয়েস্ট অনুমোদন' প্যানেল থেকে আপনার ইমেইলটি Approve করতে বলুন।",
        )
        ErrorImageType.ADMIN_REQUEST_DECLINED -> listOf(
            "প্রয়োজনে বিদ্যমান অনুমোদিত এডমিনের সাথে কথা বলুন।",
        )
        ErrorImageType.ROLE_MISMATCH -> listOf(
            "যে ক্যাটাগরিতে আপনার অ্যাকাউন্ট খোলা আছে, ঠিক সেই ক্যাটাগরি দিয়ে সাইন-ইন করুন।",
            "অথবা নতুন ক্যাটাগরির জন্য ভিন্ন একটি ইমেইল ব্যবহার করুন।",
        )
    }

    val titleColor = when (errorType) {
        ErrorImageType.NO_INTERNET, ErrorImageType.GENERAL_ERROR ->
            if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
        ErrorImageType.SIGN_IN_DISABLED, ErrorImageType.ADMIN_REQUEST_PENDING, ErrorImageType.ADMIN_REQUEST_DECLINED, ErrorImageType.ADMIN_NOT_AUTHORIZED, ErrorImageType.ROLE_MISMATCH ->
            if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
        else ->
            if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    }

    val cardBgColor = if (isDark) Color(0xFF151F32) else Color.White
    val borderColor = titleColor.copy(alpha = if (isDark) 0.45f else 0.30f)
    val stepsBgColor = titleColor.copy(alpha = if (isDark) 0.12f else 0.06f)
    val stepsBorderColor = titleColor.copy(alpha = if (isDark) 0.30f else 0.20f)

    val textColorPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textColorSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val fontBengali = getHindSiliguriFontFamily()

    AppBackHandler(enabled = true) { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = if (isDark) 0.64f else 0.46f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = cardBgColor,
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .testTag("error_popup_card"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = titleColor.copy(alpha = if (isDark) 0.22f else 0.12f),
                        border = BorderStroke(0.75.dp, titleColor.copy(alpha = 0.35f)),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = ShieldIcon,
                                contentDescription = null,
                                tint = titleColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    androidx.compose.material3.IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("error_screen_close_button"),
                    ) {
                        Icon(
                            imageVector = CloseIcon,
                            contentDescription = "Close",
                            tint = textColorSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Image(
                    painter = painterResource(drawableRes),
                    contentDescription = "Error Illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Fit,
                )

                Text(
                    text = title.localized(),
                    fontFamily = fontBengali,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = description.localized(),
                    fontFamily = fontBengali,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = textColorSecondary,
                    lineHeight = 19.5.sp,
                    textAlign = TextAlign.Center,
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = stepsBgColor,
                    border = BorderStroke(0.75.dp, stepsBorderColor),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "করণীয়:".localized(),
                            fontFamily = fontBengali,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = textColorPrimary,
                        )
                        steps.forEach { stepText ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = "• ",
                                    fontFamily = fontBengali,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = titleColor,
                                )
                                Text(
                                    text = stepText.localized(),
                                    fontFamily = fontBengali,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = textColorSecondary,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                    }
                }

                KlassPrimaryButton(
                    text = "ঠিক আছে".localized(),
                    hindSiliguri = fontBengali,
                    isDark = isDark,
                    customPrimaryColor = titleColor,
                    height = 48.dp,
                    cornerRadius = 24.dp,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun SmartMechanicalGearsLoader(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    primaryColor: Color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8),
    secondaryColor: Color = Color(0xFFF59E0B),
    tertiaryColor: Color = Color(0xFF10B981),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SmartGearsRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "GearAngle",
    )

    val cutoutColor = Color.Black.copy(alpha = 0.65f)

    Canvas(
        modifier = modifier.size(84.dp),
    ) {
        val w = size.width
        val h = size.height

        drawSmartGear(
            center = Offset(w * 0.46f, h * 0.56f),
            radius = 22.dp.toPx(),
            teethCount = 10,
            rotationDegrees = rotationAngle,
            gearColor = primaryColor,
            accentColor = Color.White,
            cutoutColor = cutoutColor,
        )

        drawSmartGear(
            center = Offset(w * 0.76f, h * 0.30f),
            radius = 15.dp.toPx(),
            teethCount = 8,
            rotationDegrees = (-rotationAngle * 1.25f) + 14f,
            gearColor = secondaryColor,
            accentColor = Color.White,
            cutoutColor = cutoutColor,
        )

        drawSmartGear(
            center = Offset(w * 0.20f, h * 0.34f),
            radius = 12.dp.toPx(),
            teethCount = 6,
            rotationDegrees = (-rotationAngle * 1.66f) + 8f,
            gearColor = tertiaryColor,
            accentColor = Color.White,
            cutoutColor = cutoutColor,
        )
    }
}

private fun DrawScope.drawSmartGear(
    center: Offset,
    radius: Float,
    teethCount: Int,
    rotationDegrees: Float,
    gearColor: Color,
    accentColor: Color,
    cutoutColor: Color,
) {
    val pitchRadius = radius * 0.82f
    val innerRadius = radius * 0.70f
    val holeRadius = radius * 0.28f
    val totalSteps = teethCount * 4

    val path = Path()
    for (i in 0 until totalSteps) {
        val angleRad = (((rotationDegrees + ((i * 360f) / totalSteps)) * PI) / 180.0).toFloat()
        val r = when (i % 4) {
            0, 1 -> radius
            else -> innerRadius
        }
        val px = center.x + (r * cos(angleRad))
        val py = center.y + (r * sin(angleRad))
        if (i == 0) path.moveTo(x = px, y = py)
        else path.lineTo(x = px, y = py)
    }
    path.close()

    drawPath(path = path, color = gearColor)

    drawCircle(
        color = accentColor.copy(alpha = 0.5f),
        radius = pitchRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
    )

    drawCircle(
        color = cutoutColor,
        radius = holeRadius,
        center = center,
    )

    drawCircle(
        color = gearColor,
        radius = holeRadius * 0.5f,
        center = center,
    )

    val holeCount = 4
    val holeDistance = (innerRadius + holeRadius) / 2f
    val weightHoleRadius = (innerRadius - holeRadius) / 2.6f
    for (h in 0 until holeCount) {
        val holeAngleRad = (((((rotationDegrees + ((h * 360f) / holeCount) + 45f) * PI)) / 180.0)).toFloat()
        val hx = center.x + (holeDistance * cos(holeAngleRad))
        val hy = center.y + (holeDistance * sin(holeAngleRad))
        drawCircle(
            color = cutoutColor,
            radius = weightHoleRadius,
            center = Offset(hx, hy),
        )
    }
}
