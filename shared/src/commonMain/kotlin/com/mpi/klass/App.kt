package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.AppTheme
import com.mpi.klass.theme.CloseIcon
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

sealed interface AppScreen {
    data object Welcome : AppScreen
    data object GuestHome : AppScreen
    data class RoleHome(
        val role: UserRole,
        val email: String,
        val name: String,
    ) : AppScreen
}

enum class ErrorImageType {
    NO_INTERNET,      // error404.webp (No Internet / Network Error)
    GENERAL_ERROR,   // timeouterror.webp (Auth failure, wrong email, timeout, or server error)
    CONFIGURATION_ERROR,
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
    isBackendConfigured: Boolean = true,
) {
    var themeMode by remember { mutableStateOf(loadThemeMode()) }
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    var currentScreen by remember {
        mutableStateOf<AppScreen>(
            try {
                val session = loadUserSession()
                when {
                    session == null -> AppScreen.Welcome
                    session.role.trim().uppercase() == "GUEST" -> AppScreen.GuestHome
                    else -> {
                        val role = UserRole.fromRaw(session.role)
                        if (role != null) AppScreen.RoleHome(role = role, email = session.email, name = session.name)
                        else AppScreen.Welcome
                    }
                }
            } catch (_: Exception) {
                AppScreen.Welcome
            }
        )
    }
    var activeErrorType by remember { mutableStateOf<ErrorImageType?>(null) }
    var lastErrorType by remember { mutableStateOf(ErrorImageType.GENERAL_ERROR) }
    var isLoadingOverlay by remember { mutableStateOf(value = false) }

    val coroutineScope = rememberCoroutineScope()

    fun showError(type: ErrorImageType) {
        lastErrorType = type
        activeErrorType = type
    }

    // Verify saved session in background silently without blocking or logging out on network failure
    LaunchedEffect(Unit) {
        val session = loadUserSession() ?: return@LaunchedEffect
        if (session.role.trim().uppercase() == "GUEST") return@LaunchedEffect

        val role = UserRole.fromRaw(session.role) ?: return@LaunchedEffect

        try {
            val result = RoleConfig.verifyRoleInFirebase(session.email, role)
            result.onSuccess { isVerified ->
                if (isVerified == false) {
                    clearUserSession()
                    currentScreen = AppScreen.Welcome
                }
            }
        } catch (_: Exception) {}
    }

    fun launchGuestMode() {
        if (!isBackendConfigured) {
            showError(ErrorImageType.CONFIGURATION_ERROR)
            return
        }

        isLoadingOverlay = true
        coroutineScope.launch {
            if (!isNetworkAvailable()) {
                isLoadingOverlay = false
                showError(ErrorImageType.NO_INTERNET)
            } else {
                val result = GuestConfig.fetchGuestConfig()
                if (result.isFailure) {
                    isLoadingOverlay = false
                    showError(errorTypeFor(result.exceptionOrNull()?.message))
                } else {
                    saveUserSession(UserSession(role = "GUEST", email = "guest@mpi.edu.bd", name = "গেস্ট ইউজার"))
                    currentScreen = AppScreen.GuestHome
                    isLoadingOverlay = false
                }
            }
        }
    }

    fun launchRoleSignIn(role: UserRole) {
        if (!isBackendConfigured) {
            showError(ErrorImageType.CONFIGURATION_ERROR)
            return
        }

        isLoadingOverlay = true
        coroutineScope.launch {
            if (!isNetworkAvailable()) {
                isLoadingOverlay = false
                showError(ErrorImageType.NO_INTERNET)
            } else {
                onLaunchGoogleSignIn { email, name, _, errorMsg ->
                    when {
                        errorMsg != null -> {
                            // A sign-in failure is not necessarily "no internet"
                            // (it can be a server/internal error), so the generic
                            // error popup is shown.
                            isLoadingOverlay = false
                            onSignOutGoogle()
                            showError(ErrorImageType.GENERAL_ERROR)
                        }

                        email != null -> {
                            coroutineScope.launch {
                                var result = RoleConfig.verifyRoleInFirebase(email, role)
                                // Teachers/students self-register on their first
                                // sign-in; admins must already exist in the console.
                                if ((result.getOrNull() == false) && (role != UserRole.ADMIN)) {
                                    result = RoleConfig.registerRoleMember(email, role, name).map { true }
                                }
                                isLoadingOverlay = false
                                result.onSuccess { isVerified ->
                                    if (isVerified) {
                                        val userName = name ?: role.defaultName
                                        saveUserSession(UserSession(role = role.name, email = email, name = userName))
                                        currentScreen = AppScreen.RoleHome(
                                            role = role,
                                            email = email,
                                            name = userName,
                                        )
                                    } else {
                                        onSignOutGoogle()
                                        showError(ErrorImageType.GENERAL_ERROR)
                                    }
                                }.onFailure { exception ->
                                    onSignOutGoogle()
                                    showError(errorTypeFor(exception.message))
                                }
                            }
                        }

                        else -> {
                            // User cancelled the account picker — no error popup.
                            isLoadingOverlay = false
                        }
                    }
                }
            }
        }
    }

    // Signing out is a purely local action; it must not require internet.
    fun signOutAndReturnToWelcome() {
        clearUserSession()
        onSignOutGoogle()
        currentScreen = AppScreen.Welcome
    }

    AppTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val screen = currentScreen) {
                    AppScreen.Welcome -> {
                        WelcomeScreen(
                            currentThemeMode = themeMode,
                            onThemeModeChange = { newMode ->
                                themeMode = newMode
                                saveThemeMode(newMode)
                            },
                            onGuestClick = { launchGuestMode() },
                            onAdminClick = { launchRoleSignIn(UserRole.ADMIN) },
                            onTeacherClick = { launchRoleSignIn(UserRole.TEACHER) },
                            onStudentClick = { launchRoleSignIn(UserRole.STUDENT) },
                            onCloseApp = onCloseApp,
                        )
                    }

                    AppScreen.GuestHome -> {
                        GuestHomeScreen(
                            currentThemeMode = themeMode,
                            onThemeModeChange = { newMode ->
                                themeMode = newMode
                                saveThemeMode(newMode)
                            },
                            onSwitchToLogin = { signOutAndReturnToWelcome() },
                        )
                    }

                    is AppScreen.RoleHome -> {
                        RoleHomeScreen(
                            role = screen.role,
                            userName = screen.name,
                            currentThemeMode = themeMode,
                            onThemeModeChange = { newMode ->
                                themeMode = newMode
                                saveThemeMode(newMode)
                            },
                            onSignOut = { signOutAndReturnToWelcome() },
                        )
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
                            // Enabled-but-empty click consumes touches so the dimmed
                            // UI underneath cannot be tapped while loading.
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.94f) else Color.White.copy(alpha = 0.95f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1D4ED8).copy(alpha = 0.3f),
                            ),
                            shadowElevation = 12.dp,
                        ) {
                            Box(
                                modifier = Modifier.padding(10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                SmartMechanicalGearsLoader(isDark = isDark)
                            }
                        }
                    }
                }

                // Error Image Popup Overlay with Theme-Aware Card & Floating Close Button
                AnimatedVisibility(
                    visible = activeErrorType != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.zIndex(100f),
                ) {
                    // lastErrorType is kept so the popup can play its exit animation
                    // even after activeErrorType has become null.
                    ErrorImagePopup(
                        errorType = activeErrorType ?: lastErrorType,
                        isDark = isDark,
                        onDismiss = { activeErrorType = null },
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorImagePopup(
    errorType: ErrorImageType,
    isDark: Boolean,
    onDismiss: () -> Unit,
) {
    val drawableRes = when (errorType) {
        ErrorImageType.NO_INTERNET -> Res.drawable.error404
        ErrorImageType.GENERAL_ERROR -> Res.drawable.timeouterror
        ErrorImageType.CONFIGURATION_ERROR -> Res.drawable.timeouterror
    }

    val title = when (errorType) {
        ErrorImageType.NO_INTERNET -> "ইন্টারনেট সংযোগ নেই!"
        ErrorImageType.GENERAL_ERROR -> "অনুরোধ সম্পূর্ণ করা যায়নি!"
        ErrorImageType.CONFIGURATION_ERROR -> "ওয়েব সেটআপ সম্পূর্ণ হয়নি"
    }

    val description = when (errorType) {
        ErrorImageType.NO_INTERNET -> "আপনার ডিভাইসে সক্রিয় ইন্টারনেট সংযোগ পাওয়া যায়নি। অ্যাপটির সমস্ত সুবিধা পেতে ইন্টারনেট কানেকশন প্রয়োজন।"
        ErrorImageType.GENERAL_ERROR -> "আপনার অনুরোধটি সম্পূর্ণ করা সম্ভব হয়নি। প্রবেশকৃত জিমেইল অ্যাকাউন্টে সমস্যা থাকতে পারে অথবা নেটওয়ার্ক ও সার্ভার সংযোগে বিলম্ব হচ্ছে।"
        ErrorImageType.CONFIGURATION_ERROR -> "Firebase কনফিগারেশন চেক করুন। Google sign-in provider এবং নেটওয়ার্ক সেটিংস যাচাই করুন।"
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
    }

    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val stepsBgColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.8f) else Color(0xFFF8FAFC)
    val stepsBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val titleColor = if (errorType == ErrorImageType.GENERAL_ERROR) {
        if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    } else {
        if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    }

    val textColorPrimary = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val textColorSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val fontBengali = getHindSiliguriFontFamily()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Outer Column to hold Card Surface and 6dp-below Close Button at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
                .clickable(enabled = false) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Theme-Aware Surface Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                shape = RoundedCornerShape(22.dp),
                color = cardBgColor,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 1. Prominent Image
                    Image(
                        painter = painterResource(drawableRes),
                        contentDescription = "Error Illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Title
                    Text(
                        text = title,
                        fontFamily = fontBengali,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Description Message
                    Text(
                        text = description,
                        fontFamily = fontBengali,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = textColorSecondary,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Actionable Steps Card ("কী করবেন:" / "করণীয়:")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = stepsBgColor,
                        border = BorderStroke(1.dp, stepsBorderColor),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                        ) {
                            Text(
                                text = "করণীয়:",
                                fontFamily = fontBengali,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColorPrimary,
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            steps.forEach { stepText ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
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
                                        text = stepText,
                                        fontFamily = fontBengali,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = textColorSecondary,
                                        lineHeight = 17.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Clean, elegant solid Close Button at bottom (like Terms Popup!)
            Surface(
                onClick = onDismiss,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = titleColor,
                contentColor = Color.White,
                border = BorderStroke(2.dp, cardBgColor),
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Close Error Popup",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SmartMechanicalGearsLoader(
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    primaryColor: Color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8),   // Cyan / Blue
    secondaryColor: Color = Color(0xFFF59E0B), // Amber Gold
    tertiaryColor: Color = Color(0xFF10B981),  // Mint Emerald
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

    val cutoutColor = if (isDark) Color(0xFF1E293B) else Color.White

    Canvas(
        modifier = modifier.size(125.dp),
    ) {
        val w = size.width
        val h = size.height

        // Gear 1: Center Main Gear (Clockwise)
        drawSmartGear(
            center = Offset(w * 0.46f, h * 0.56f),
            radius = 32.dp.toPx(),
            teethCount = 10,
            rotationDegrees = rotationAngle,
            gearColor = primaryColor,
            accentColor = Color.White,
            cutoutColor = cutoutColor,
        )

        // Gear 2: Top-Right Gear (Counter-Clockwise, Meshed)
        drawSmartGear(
            center = Offset(w * 0.76f, h * 0.30f),
            radius = 22.dp.toPx(),
            teethCount = 8,
            rotationDegrees = (-rotationAngle * 1.25f) + 14f,
            gearColor = secondaryColor,
            accentColor = Color.White,
            cutoutColor = cutoutColor,
        )

        // Gear 3: Bottom-Left Gear (Counter-Clockwise, Meshed)
        drawSmartGear(
            center = Offset(w * 0.20f, h * 0.34f),
            radius = 18.dp.toPx(),
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

    // 1. Solid Gear Body & Precision Teeth
    drawPath(path = path, color = gearColor)

    // 2. Pitch Circle Accent Line
    drawCircle(
        color = accentColor.copy(alpha = 0.5f),
        radius = pitchRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
    )

    // 3. Center Hole Cutout
    drawCircle(
        color = cutoutColor,
        radius = holeRadius,
        center = center,
    )

    // 4. Center Axle Pin Dot
    drawCircle(
        color = gearColor,
        radius = holeRadius * 0.5f,
        center = center,
    )

    // 5. 4 Circular Weight-Reduction Cutout Holes inside Gear Body
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
