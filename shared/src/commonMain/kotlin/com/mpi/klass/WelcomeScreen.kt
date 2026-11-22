package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.AwardIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.EyeIcon
import com.mpi.klass.theme.EyeOffIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.HelpCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LanguageGlobeIcon
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.LockIcon
import com.mpi.klass.theme.LoginIcon
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.PaletteIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.RegistrationIcon
import com.mpi.klass.theme.RightArrowIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.getHindSiliguriFontFamily
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.admin
import klass.shared.generated.resources.imgdk
import klass.shared.generated.resources.imglt
import klass.shared.generated.resources.policy
import klass.shared.generated.resources.splash
import klass.shared.generated.resources.student
import klass.shared.generated.resources.teacher
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private enum class WelcomeStage {
    INITIAL,
    ROLE_SELECTION,
    EMAIL_LOGIN,
}

@Composable
fun WelcomeScreen(
    currentThemeMode: ThemeMode = ThemeMode.DEFAULT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onGuestClick: () -> Unit = {},
    onStudentClick: () -> Unit = {},
    onTeacherClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onEmailPasswordLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onCloseApp: () -> Unit = {},
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    val backgroundImage = if (isDark) {
        Res.drawable.imgdk
    } else {
        Res.drawable.imglt
    }

    val hindSiliguri = getHindSiliguriFontFamily()

    val supportBtnInteractionSource = remember { MutableInteractionSource() }
    val isSupportBtnPressed by supportBtnInteractionSource.collectIsPressedAsState()

    val guestInteractionSource = remember { MutableInteractionSource() }
    val isGuestPressed by guestInteractionSource.collectIsPressedAsState()
    val isGuestHovered by guestInteractionSource.collectIsHoveredAsState()
    val isGuestActive = isGuestPressed || isGuestHovered

    val startInteractionSource = remember { MutableInteractionSource() }
    val isStartPressed by startInteractionSource.collectIsPressedAsState()

    val themeBtnInteractionSource = remember { MutableInteractionSource() }
    val isThemeBtnPressed by themeBtnInteractionSource.collectIsPressedAsState()

    val loginCloseBtnInteractionSource = remember { MutableInteractionSource() }
    val isLoginCloseBtnPressed by loginCloseBtnInteractionSource.collectIsPressedAsState()

    var isThemeExpanded by remember { mutableStateOf(value = false) }
    var showTermsDialog by remember { mutableStateOf(value = false) }
    var showSupportDialog by remember { mutableStateOf(value = false) }
    var welcomeStage by remember { mutableStateOf(value = WelcomeStage.INITIAL) }
    val isLoginMode = welcomeStage == WelcomeStage.EMAIL_LOGIN
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var activeLoginField by remember { mutableStateOf<LoginInputField?>(null) }
    val isCustomKeyboardVisible = isLoginMode && activeLoginField != null

    LaunchedEffect(welcomeStage) {
        if (welcomeStage != WelcomeStage.EMAIL_LOGIN) {
            activeLoginField = null
        }
    }

    fun submitEmailPasswordLogin() {
        val cleanEmail = emailText.trim().lowercase()
        if (cleanEmail.isBlank() || passwordText.isBlank()) {
            showToast("অনুগ্রহ করে ইমেইল ও পাসওয়ার্ড লিখুন".localized(currentLanguage))
            return
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            showToast("সঠিক ইমেইল ঠিকানা লিখুন".localized(currentLanguage))
            return
        }
        activeLoginField = null
        onEmailPasswordLogin(cleanEmail, passwordText)
    }

    var lastBackPressTime by remember { mutableStateOf(0L) }

    AppBackHandler(enabled = true) {
        when {
            showTermsDialog -> showTermsDialog = false
            showSupportDialog -> showSupportDialog = false
            isThemeExpanded -> isThemeExpanded = false
            isCustomKeyboardVisible -> activeLoginField = null
            welcomeStage != WelcomeStage.INITIAL -> welcomeStage = WelcomeStage.INITIAL
            else -> {
                val now = getCurrentTimeMillis()
                if (now - lastBackPressTime < 2000L) {
                    onCloseApp()
                } else {
                    lastBackPressTime = now
                    showToast("অ্যাপ থেকে বের হতে আবার ব্যাক প্রেস করুন".localized(currentLanguage))
                }
            }
        }
    }

    val themeCardWidth by animateDpAsState(
        targetValue = if (isThemeExpanded) 135.dp else 36.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    )
    val themeCardHeight by animateDpAsState(
        targetValue = if (isThemeExpanded) 185.dp else 36.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    )

    val loginBtnWidth = 36.dp

    val buttonHorizontalPadding = 36.dp

    // Corner shape for Theme Button / Card:
    val themeShape = RoundedCornerShape(
        topStart = 10.dp,
        bottomStart = 10.dp,
        topEnd = 0.dp,
        bottomEnd = if (isThemeExpanded) 10.dp else 0.dp,
    )

    // Corner shape for Login / Close Top-Right Button:
    val loginShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = 0.dp,
        topEnd = 10.dp,
        bottomEnd = 10.dp,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                isThemeExpanded = false
                if (isCustomKeyboardVisible) {
                    activeLoginField = null
                }
            },
    ) {
        // Fullscreen Background Image
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = "Welcome Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        // Background Backdrop for collapsing theme menu when clicking outside
        if (isThemeExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        isThemeExpanded = false
                    },
            )
        }

        // Top Content: 3D Logo (72dp) and College Name (stationary regardless of keyboard state)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 3D Circular Logo
            Box(
                modifier = Modifier.size(width = 72.dp, height = 74.5.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                // 3D Base Circle (#1D4ED8)
                Box(
                    modifier = Modifier
                        .offset(y = 2.5.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D4ED8)),
                )

                // Foreground Circular Image
                Image(
                    painter = painterResource(Res.drawable.splash),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ময়মনসিংহ সরকারি\nপলিটেকনিক ইনস্টিটিউট".localized(),
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
            )
        }

        // Bottom Section: Transitions between Initial Mode, Role Selection Mode, and Email Login Mode
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp)
                .fillMaxWidth()
                .zIndex(20f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedContent(
                targetState = welcomeStage,
                label = "WelcomeStageTransition",
            ) { stage ->
                if (stage == WelcomeStage.INITIAL) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = buttonHorizontalPadding)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Motivational Quote
                        Text(
                            text = AppStrings.quote(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraLight,
                            fontFamily = hindSiliguri,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                        )

                        Spacer(modifier = Modifier.height(60.dp))

                        // 3D Buttons Container (Support & Get Started)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.84f)
                                .height(51.dp)
                                .zIndex(25f),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(51.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                // 3D Support Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(51.dp)
                                        .zIndex(30f),
                                    contentAlignment = Alignment.BottomStart,
                                ) {
                                    // 3D Shadow Base (#94A3B8)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    bottomStart = 12.dp,
                                                    topEnd = 0.dp,
                                                    bottomEnd = 0.dp,
                                                )
                                            )
                                            .background(Color(0xFF94A3B8)),
                                    )

                                    // Foreground 3D Surface Card
                                    Surface(
                                        onClick = {
                                            isThemeExpanded = false
                                            showTermsDialog = false
                                            showSupportDialog = true
                                        },
                                        interactionSource = supportBtnInteractionSource,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(bottom = if (isSupportBtnPressed) 0.dp else 3.dp)
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("welcome_support_button"),
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            bottomStart = 12.dp,
                                            topEnd = 0.dp,
                                            bottomEnd = 0.dp,
                                        ),
                                        border = BorderStroke(0.5.dp, Color(0xFF94A3B8)),
                                        color = MaterialTheme.colorScheme.surface,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = AppStrings.supportBtn(currentLanguage),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                        }
                                    }
                                }

                                // 3D Get Started Button (শুরু করুন)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(51.dp),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    // 3D Shadow underneath (#8E7864)
                                    Box(
                                        modifier = Modifier
                                            .offset(y = 3.dp)
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 0.dp,
                                                    bottomStart = 0.dp,
                                                    topEnd = 12.dp,
                                                    bottomEnd = 12.dp,
                                                ),
                                            )
                                            .background(Color(0xFF8E7864)),
                                    )

                                    // Foreground Button (#8CB9D9)
                                    Button(
                                        onClick = {
                                            isThemeExpanded = false
                                            welcomeStage = WelcomeStage.ROLE_SELECTION
                                        },
                                        interactionSource = startInteractionSource,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .offset(y = if (isStartPressed) 3.dp else 0.dp)
                                            .testTag("welcome_start_button"),
                                        shape = RoundedCornerShape(
                                            topStart = 0.dp,
                                            bottomStart = 0.dp,
                                            topEnd = 12.dp,
                                            bottomEnd = 12.dp,
                                        ),
                                        border = BorderStroke(0.5.dp, Color(0xFF8E7864)),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8CB9D9),
                                            contentColor = Color(0xFF1E293B),
                                        ),
                                    ) {
                                        Text(
                                            text = AppStrings.registrationBtn(currentLanguage),
                                            fontSize = if (currentLanguage == AppLanguage.ENGLISH) 13.5.sp else 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            maxLines = 1,
                                            softWrap = false,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = AppStrings.termsAndConditions(currentLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = hindSiliguri,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            textDecoration = TextDecoration.Underline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                isThemeExpanded = false
                                showTermsDialog = !showTermsDialog
                            },
                        )
                    }
                } else {
                    // Role Selection or Email+Password Login Layout
                    val guestBtnColor = if (isGuestActive) {
                        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.30f) else Color(0xFF2563EB).copy(alpha = 0.22f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.50f)
                    }
                    val guestContentColor = if (isGuestActive) {
                        if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8)
                    } else {
                        if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { it / 2 },
                            exit = fadeOut(animationSpec = tween(350)) + slideOutVertically(animationSpec = tween(350)) { it / 2 },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (stage == WelcomeStage.EMAIL_LOGIN) {
                                EmailLoginPanel(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    buttonHorizontalPadding = buttonHorizontalPadding,
                                    currentLanguage = currentLanguage,
                                    emailText = emailText,
                                    passwordText = passwordText,
                                    activeField = activeLoginField,
                                    isCustomKeyboardVisible = isCustomKeyboardVisible,
                                    onSelectField = { field ->
                                        isThemeExpanded = false
                                        activeLoginField = field
                                    },
                                    onLoginSubmit = { submitEmailPasswordLogin() },
                                )
                            } else {
                                RoleSelectionScreen(
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    buttonHorizontalPadding = buttonHorizontalPadding,
                                    currentLanguage = currentLanguage,
                                    onStudentClick = onStudentClick,
                                    onTeacherClick = onTeacherClick,
                                    onAdminClick = onAdminClick,
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isCustomKeyboardVisible,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                                expandFrom = Alignment.Bottom,
                            ) + fadeIn(animationSpec = tween(durationMillis = 220)),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                shrinkTowards = Alignment.Bottom,
                            ) + fadeOut(animationSpec = tween(durationMillis = 160)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                KlassCustomKeyboard(
                                    activeField = activeLoginField ?: LoginInputField.EMAIL,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    onInsertText = { token ->
                                        if (activeLoginField == LoginInputField.EMAIL) {
                                            emailText += token
                                        } else {
                                            passwordText += token
                                        }
                                    },
                                    onBackspace = {
                                        if (activeLoginField == LoginInputField.EMAIL) {
                                            emailText = popLastKeyboardToken(emailText)
                                        } else {
                                            passwordText = popLastKeyboardToken(passwordText)
                                        }
                                    },
                                    onClearField = {
                                        if (activeLoginField == LoginInputField.EMAIL) {
                                            emailText = ""
                                        } else {
                                            passwordText = ""
                                        }
                                    },
                                    onNextOrSubmit = {
                                        if (activeLoginField == LoginInputField.EMAIL) {
                                            activeLoginField = LoginInputField.PASSWORD
                                        } else {
                                            submitEmailPasswordLogin()
                                        }
                                    },
                                    onDismiss = { activeLoginField = null },
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = !isCustomKeyboardVisible,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                expandFrom = Alignment.Bottom,
                            ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                shrinkTowards = Alignment.Bottom,
                            ) + fadeOut(animationSpec = tween(durationMillis = 140)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(6.dp))

                                // Full 100% Width Deep Bow/Arch Shaped Bottom Guest Mode Button Attached directly to Display Bottom Edge
                                Surface(
                                    shape = DeepBowArchShape,
                                    color = guestBtnColor,
                                    shadowElevation = if (isGuestActive) 6.dp else 0.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clip(DeepBowArchShape)
                                        .clickable(
                                            interactionSource = guestInteractionSource,
                                            indication = ripple(bounded = true, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8)),
                                            onClick = onGuestClick,
                                        ),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 4.dp, bottom = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = RightArrowIcon,
                                            contentDescription = AppStrings.guestMode(currentLanguage),
                                            tint = guestContentColor,
                                            modifier = Modifier.size(16.dp),
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = AppStrings.guestMode(currentLanguage),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Light,
                                            fontFamily = hindSiliguri,
                                            color = guestContentColor,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Right Corner 3D Header Layer (Fixed right edge, zIndex 10f)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 28.dp, end = 16.dp)
                .zIndex(10f),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Theme 3D Button (Expands leftwards & downwards without covering the Login/Close button!)
            Box(
                modifier = Modifier
                    .width(themeCardWidth)
                    .height(themeCardHeight + 3.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                // 3D Base Shape
                Box(
                    modifier = Modifier
                        .offset(y = 3.dp)
                        .width(themeCardWidth)
                        .height(themeCardHeight)
                        .clip(themeShape)
                        .background(Color(0xFF94A3B8)),
                )

                // Foreground Animated 3D Surface Card
                Surface(
                    onClick = {
                        if (!isThemeExpanded) {
                            showTermsDialog = false
                            showSupportDialog = false
                            isThemeExpanded = true
                        }
                    },
                    interactionSource = themeBtnInteractionSource,
                    modifier = Modifier
                        .width(themeCardWidth)
                        .height(themeCardHeight)
                        .offset(y = if (isThemeBtnPressed && !isThemeExpanded) 3.dp else 0.dp),
                    shape = themeShape,
                    border = BorderStroke(0.5.dp, Color(0xFF94A3B8)),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    if (!isThemeExpanded) {
                        // Compact 1:1 Square Vector Palette Icon
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = PaletteIcon,
                                contentDescription = "Theme",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        // Expanded Theme Options Menu
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "থিম নির্বাচন করুন".localized(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    val isSelected = mode == currentThemeMode
                                    val modeIcon = when (mode) {
                                        ThemeMode.LIGHT -> LightModeIcon
                                        ThemeMode.DARK -> DarkModeIcon
                                        ThemeMode.DEFAULT -> AutoModeIcon
                                    }

                                    Surface(
                                        onClick = {
                                            onThemeModeChange(mode)
                                            isThemeExpanded = false
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Transparent
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(
                                                imageVector = modeIcon,
                                                contentDescription = mode.getDisplayName().localized(),
                                                tint = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                modifier = Modifier.size(15.dp),
                                            )

                                            Text(
                                                text = mode.getDisplayName().localized(),
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = hindSiliguri,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right: 3D Login Button (Turns into Close button when Login screen is open)
            Box(
                modifier = Modifier
                    .width(loginBtnWidth)
                    .height(39.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                // 3D Base Shape (#8E7864)
                Box(
                    modifier = Modifier
                        .offset(y = 3.dp)
                        .width(loginBtnWidth)
                        .height(36.dp)
                        .clip(loginShape)
                        .background(Color(0xFF8E7864)),
                )

                // Foreground Animated 3D Surface Card (#8CB9D9)
                Surface(
                    onClick = {
                        isThemeExpanded = false
                        showTermsDialog = false
                        showSupportDialog = false
                        welcomeStage = if (isLoginMode) {
                            WelcomeStage.INITIAL
                        } else {
                            WelcomeStage.EMAIL_LOGIN
                        }
                    },
                    interactionSource = loginCloseBtnInteractionSource,
                    modifier = Modifier
                        .width(loginBtnWidth)
                        .height(36.dp)
                        .offset(y = if (isLoginCloseBtnPressed) 3.dp else 0.dp)
                        .testTag(if (isLoginMode) "welcome_login_close_button" else "welcome_login_button"),
                    shape = loginShape,
                    border = BorderStroke(0.5.dp, Color(0xFF8E7864)),
                    color = Color(0xFF8CB9D9),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedContent(
                            targetState = isLoginMode,
                            label = "welcome_login_icon_morph",
                        ) { showClose ->
                            Icon(
                                imageVector = if (showClose) CloseIcon else LoginIcon,
                                contentDescription = if (showClose) {
                                    "বন্ধ করুন".localized()
                                } else {
                                    AppStrings.loginBtn(currentLanguage)
                                },
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(if (showClose) 16.dp else 17.dp),
                            )
                        }
                    }
                }
            }
        }

        // Full-Screen Help & Support Modal
        if (showSupportDialog) {
            val supportAccent = KlassFeaturePalette.ai(isDark).primary
            val cardSurface = if (isDark) Color(0xFF1E293B).copy(alpha = 0.84f) else Color.White.copy(alpha = 0.88f)
            val headerSurface = if (isDark) Color(0xFF151F32).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)
            val borderSubtle = if (isDark) Color(0xFF334155).copy(alpha = 0.7f) else Color(0xFFE2E8F0)
            val uriHandler = LocalUriHandler.current

            val supportGuides = remember {
                listOf(
                    Triple(
                        LoginIcon,
                        Color(0xFF0284C7),
                        "ইমেইল ও পাসওয়ার্ড লগইন" to "উপরের 'লগইন' বাটনে ট্যাপ করে নিবন্ধিত ইমেইল ও পাসওয়ার্ড দিয়ে সরাসরি প্রবেশ করুন।",
                    ),
                    Triple(
                        LockIcon,
                        Color(0xFF8B5CF6),
                        "পাসওয়ার্ড সেট ও পরিবর্তন" to "প্রথমবার 'শুরু করুন' দিয়ে Google Sign-In করলে পাসওয়ার্ড সেট করার অপশন পাবেন। পরে যেকোনো রোলের প্রোফাইল থেকেও পাসওয়ার্ড পরিবর্তন করা যায়।",
                    ),
                    Triple(
                        CrownIcon,
                        Color(0xFF10B981),
                        "ক্যাম্পাস পোর্টাল (mpi.politech.gov.bd)" to "ইনস্টিটিউটের অফিসিয়াল নোটিশ, ফলাফল ও একাডেমিক আপডেটের জন্য ক্লিক করে ওয়েবসাইট ভিজিট করুন।",
                    ),
                )
            }

            val features = remember {
                listOf(
                    Triple(CrownIcon, Color(0xFF0284C7), "রোল-ভিত্তিক নিরাপদ সাইন-ইন" to "শিক্ষার্থী, শিক্ষক ও এডমিনদের জন্য Google Sign-In এবং অতিথিদের জন্য সহজ গেস্ট মোড সুবিধা।"),
                    Triple(BookIcon, Color(0xFF10B981), "স্মার্ট ক্লাস রুটিন" to "সেমিস্টার, শিফট, ডিপার্টমেন্ট ও সেকশন অনুযায়ী সম্পূর্ণ ক্লাস রুটিন টেবিল এক নজরে দেখার সুবিধা।"),
                    Triple(SparkleIcon, Color(0xFF8B5CF6), "এআই স্টাডি অ্যাসিস্ট্যান্ট" to "পড়াশোনা, কোডিং, অ্যাসাইনমেন্ট ও যেকোনো একাডেমিক প্রশ্নোত্তরের জন্য তাৎক্ষণিক স্মার্ট এআই সহায়তা।"),
                    Triple(NotificationIcon, Color(0xFFEF4444), "নোটিশ বোর্ড ও নোটিফিকেশন" to "ইনস্টিটিউট ও ডিপার্টমেন্টের জরুরি নোটিশ, পরীক্ষার সময়সূচি ও আপডেট রিয়েল-টাইমে জানা।"),
                    Triple(GraduationCapIcon, Color(0xFFF59E0B), "বুকস লাইব্রেরি ও ক্লাসরুম" to "সেমিস্টার ভিত্তিক পাঠ্যবই, লেকচার নোট ও অনলাইন ক্লাসরুম সংক্রান্ত প্রয়োজনীয় তথ্য।"),
                    Triple(PersonIcon, Color(0xFFEC4899), "শিক্ষকবৃন্দের ডিরেক্টরি" to "ডিপার্টমেন্টের সম্মানিত শিক্ষকগণের তালিকা এবং প্রয়োজনীয় যোগাযোগের তথ্য।"),
                    Triple(ShieldIcon, Color(0xFF6366F1), "ক্লাউড প্রোফাইল ও অটো জিপিএস সিঙ্ক" to "গ্যালারি থেকে প্রোফাইল ছবি আপলোড, নাম ও রোল সংরক্ষণ, ব্লাড গ্রুপ (ডোনার তথ্য) এবং অবস্থান পরিবর্তন বা অনলাইনে আসলে অটো জিপিএস ঠিকানা সিঙ্ক।"),
                    Triple(AwardIcon, Color(0xFFD97706), "এডমিন কন্ট্রোল প্যানেল" to "নতুন এডমিন রিকোয়েস্ট অনুমোদন (Approve/Decline) এবং রোল-ভিত্তিক সাইন-ইন নিয়ন্ত্রণ ব্যবস্থা।"),
                    Triple(PaletteIcon, Color(0xFF0EA5E9), "থিম ও দ্বিভাষিক সুবিধা (BN/EN)" to "লাইট, ডার্ক ও অটো সিস্টেম থিম এবং সম্পূর্ণ বাংলা ও ইংরেজি ভাষায় অ্যাপ ব্যবহারের সুবিধা।"),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
                    .rainbowScreenBackground(isDark, supportAccent),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                ) {
                    // Top Bar with Close Button
                    Surface(
                        color = headerSurface,
                        shadowElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = supportAccent.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, supportAccent.copy(alpha = 0.36f)),
                                    modifier = Modifier.size(42.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = HelpCircleIcon,
                                            contentDescription = "হেল্প ও সাপোর্ট".localized(),
                                            tint = supportAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "হেল্প ও সাপোর্ট".localized(),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "MPI Klass • ${"সহায়তা ও তথ্য কেন্দ্র".localized()}",
                                        fontSize = 12.sp,
                                        fontFamily = hindSiliguri,
                                        color = supportAccent,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            Surface(
                                onClick = { showSupportDialog = false },
                                shape = CircleShape,
                                color = supportAccent.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, supportAccent.copy(alpha = 0.36f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(44.dp)
                                    .testTag("support_close_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = CloseIcon,
                                        contentDescription = "বন্ধ করুন".localized(),
                                        tint = supportAccent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Scrollable Full-Screen Help & Support + Features Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Hero Institute Summary Card
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.45f else 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট".localized(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = hindSiliguri,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = "শিক্ষার্থী, শিক্ষক ও প্রশাসনের সকল একাডেমিক কার্যক্রম সহজ, দ্রুত ও ডিজিটাল করার জন্য নির্মিত স্মার্ট ক্লাসরুম প্ল্যাটফর্ম।".localized(),
                                    fontSize = 13.sp,
                                    fontFamily = hindSiliguri,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 19.sp,
                                )
                            }
                        }

                        // Quick Language Toggle Row inside Support Screen
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardSurface,
                            border = BorderStroke(1.dp, borderSubtle),
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = LanguageGlobeIcon,
                                        contentDescription = "ভাষা".localized(),
                                        tint = supportAccent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "ভাষা নির্বাচন করুন".localized(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(AppLanguage.BENGALI to "BN", AppLanguage.ENGLISH to "EN").forEach { (lang, code) ->
                                        val selected = lang == currentLanguage
                                        Surface(
                                            onClick = { onLanguageChange(lang) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selected) supportAccent else supportAccent.copy(alpha = 0.12f),
                                        ) {
                                            Text(
                                                text = code,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = if (selected) Color.White else supportAccent,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Support & Quick Help Cards
                        Text(
                            text = "সহায়তা ও তথ্য কেন্দ্র".localized(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                        )

                        supportGuides.forEachIndexed { idx, (icon, accent, titleDesc) ->
                            val (guideTitle, guideDesc) = titleDesc
                            val isPortal = idx == 2
                            Surface(
                                onClick = {
                                    if (isPortal) {
                                        try {
                                            uriHandler.openUri("https://mpi.politech.gov.bd")
                                        } catch (_: Exception) {}
                                    }
                                },
                                enabled = isPortal,
                                shape = RoundedCornerShape(16.dp),
                                color = cardSurface,
                                border = BorderStroke(1.dp, borderSubtle),
                                shadowElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = accent.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = guideTitle.localized(),
                                                tint = accent,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Text(
                                            text = guideTitle.localized(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = guideDesc.localized(),
                                            fontSize = 12.5.sp,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                            lineHeight = 18.sp,
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "অ্যাপের সকল ফিচারসমূহ".localized(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp, start = 2.dp),
                        )

                        features.forEach { (icon, accent, titleDesc) ->
                            val (featureTitle, featureDesc) = titleDesc
                            val localizedTitle = featureTitle.localized()
                            val localizedDesc = featureDesc.localized()
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardSurface,
                                border = BorderStroke(1.dp, borderSubtle),
                                shadowElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = accent.copy(alpha = 0.14f),
                                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = localizedTitle,
                                                tint = accent,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Text(
                                            text = localizedTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = localizedDesc,
                                            fontSize = 12.5.sp,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                            lineHeight = 18.sp,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        KlassPrimaryButton(
                            text = "বন্ধ করুন".localized(),
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            customPrimaryColor = supportAccent,
                            height = 48.dp,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showSupportDialog = false },
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Terms & Conditions Full-Screen Sub-Screen (No Popup)
        if (showTermsDialog) {
            val termsAccent = MaterialTheme.colorScheme.primary
            val cardSurface = if (isDark) Color(0xFF1E293B).copy(alpha = 0.86f) else Color.White.copy(alpha = 0.90f)
            val borderSubtle = if (isDark) Color(0xFF334155).copy(alpha = 0.7f) else Color(0xFFE2E8F0)
            val points = remember {
                listOf(
                    "গোপনীয়তা ও সুরক্ষা" to "শিক্ষার্থী ও শিক্ষকগণের অ্যাকাউন্টের গোপনীয়তা এবং ব্যক্তিগত তথ্য সম্পূর্ণ সুরক্ষিত রাখা হয়।",
                    "নিরাপদ সাইন-ইন" to "নিরাপদ ও ভেরিফাইড অথেনটিকেশন ব্যবস্থার মাধ্যমে আপনার অ্যাকাউন্ট সুরক্ষিত থাকে।",
                    "এআই স্টাডি সহায়তা" to "জেনারেটিভ এআই অ্যাসিস্ট্যান্ট শিক্ষার্থীদের পড়াশোনা, রুটিন এবং একাডেমিক প্রশ্নোত্তরে সহায়তা করে।",
                    "অফিশিয়াল নোটিশ" to "প্রতিষ্ঠান ও ডিপার্টমেন্টের জরুরি নোটিশ এবং রুটিন নিয়মিত আপডেট করা হয়।",
                    "নিয়মাবলী ও শৃঙ্খলা" to "অ্যাপ ব্যবহারের সময় প্রাতিষ্ঠানিক নিয়মকানুন ও শালীনতা বজায় রাখা বাধ্যতামূলক।",
                    "সেবা আপডেট" to "শিক্ষার্থীদের সুবিধার্থে অ্যাপে নতুন ফিচার ও আপডেট নিয়মিত যুক্ত করা হবে।"
                )
            }

            Box(modifier = Modifier.fillMaxSize().zIndex(100f)) {
                MinimalFullScreenSheet(
                    title = "সেবার শর্তাবলী ও প্রাইভেসি পলিসি".localized(),
                    subtitle = "দয়া করে নিচের নিয়মগুলো দেখে নিন:".localized(),
                    icon = ShieldIcon,
                    accent = termsAccent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    navigationIcon = CloseIcon,
                    closeTestTag = "terms_close_button",
                    onDismiss = { showTermsDialog = false },
                    maxContentWidth = 680.dp,
                    headerActions = {
                        HeaderSavePillButton(
                            text = "ঠিক আছে".localized(),
                            accent = termsAccent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { showTermsDialog = false },
                        )
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = cardSurface,
                            border = BorderStroke(1.dp, termsAccent.copy(alpha = 0.28f)),
                            shadowElevation = 0.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.policy),
                                contentDescription = "Policy Illustration",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit,
                            )
                        }

                        points.forEachIndexed { index, (title, desc) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardSurface,
                                border = BorderStroke(1.dp, borderSubtle),
                                shadowElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = termsAccent.copy(alpha = if (isDark) 0.22f else 0.12f),
                                        border = BorderStroke(1.dp, termsAccent.copy(alpha = 0.35f)),
                                        modifier = Modifier.size(30.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = termsAccent,
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Text(
                                            text = title.localized(),
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = desc.localized(),
                                            fontSize = 13.sp,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                                            lineHeight = 19.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
internal fun RoleButton(
    text: String,
    painter: DrawableResource,
    isDark: Boolean,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    accentColor: Color? = null,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    val resolvedAccent = accentColor ?: MaterialTheme.colorScheme.primary
    val borderColor = if (isDark) resolvedAccent.copy(alpha = 0.42f) else resolvedAccent.copy(alpha = 0.34f)
    val bgColor = if (isDark) Color(0xFF151F32).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.92f)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        color = bgColor,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(resolvedAccent.copy(alpha = if (isDark) 0.08f else 0.05f))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = resolvedAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(painter),
                            contentDescription = text,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Text(
                    text = text,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = textColor,
                )
            }

            Icon(
                imageVector = ChevronRightIcon,
                contentDescription = null,
                tint = resolvedAccent,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun EmailLoginPanel(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    buttonHorizontalPadding: androidx.compose.ui.unit.Dp,
    currentLanguage: AppLanguage,
    emailText: String,
    passwordText: String,
    activeField: LoginInputField?,
    isCustomKeyboardVisible: Boolean,
    onSelectField: (LoginInputField) -> Unit,
    onLoginSubmit: () -> Unit,
) {
    val emailAccent = KlassFeaturePalette.routine(isDark).primary
    val passwordAccent = KlassFeaturePalette.teaching(isDark).primary
    val loginAccent = KlassFeaturePalette.admin(isDark).primary

    val bgColor = if (isDark) Color(0xFF151F32).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.92f)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val placeholderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var passwordVisible by remember { mutableStateOf(false) }

    val cursorTransition = rememberInfiniteTransition(label = "custom_login_cursor")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursor_blink",
    )

    val emailScroll = rememberScrollState()
    LaunchedEffect(emailText.length, activeField) {
        emailScroll.animateScrollTo(emailScroll.maxValue)
    }

    val passwordScroll = rememberScrollState()
    LaunchedEffect(passwordText.length, activeField, passwordVisible) {
        passwordScroll.animateScrollTo(passwordScroll.maxValue)
    }

    val emailFocused = activeField == LoginInputField.EMAIL
    val passwordFocused = activeField == LoginInputField.PASSWORD
    val maskedPassword = remember(passwordText, passwordVisible) {
        if (passwordVisible) passwordText else "•".repeat(countDisplayGlyphs(passwordText))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = buttonHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1. Email Input Button Card (Tapping opens KlassCustomKeyboard instead of system IME)
        val emailBorderWidth = if (emailFocused) 1.75.dp else 1.dp
        val emailBorder = emailAccent.copy(alpha = if (emailFocused) 0.92f else if (isDark) 0.42f else 0.34f)
        Surface(
            onClick = { onSelectField(LoginInputField.EMAIL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_email_input"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(emailBorderWidth, emailBorder),
            color = bgColor,
            shadowElevation = if (emailFocused) 4.dp else 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(emailAccent.copy(alpha = if (emailFocused) (if (isDark) 0.14f else 0.09f) else (if (isDark) 0.08f else 0.05f)))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = emailAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = EnvelopeSolidIcon,
                            contentDescription = "ইমেইল".localized(currentLanguage),
                            tint = emailAccent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(emailScroll),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (emailText.isEmpty()) {
                        if (emailFocused) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(18.dp)
                                    .background(emailAccent.copy(alpha = cursorAlpha)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "ইমেইল".localized(currentLanguage),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = placeholderColor,
                            maxLines = 1,
                        )
                    } else {
                        Text(
                            text = emailText,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = hindSiliguri,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false,
                        )
                        if (emailFocused) {
                            Spacer(modifier = Modifier.width(1.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(18.dp)
                                    .background(emailAccent.copy(alpha = cursorAlpha)),
                            )
                        }
                    }
                }
            }
        }

        // 2. Password Input Button Card (Tapping opens KlassCustomKeyboard instead of system IME)
        val passwordBorderWidth = if (passwordFocused) 1.75.dp else 1.dp
        val passwordBorder = passwordAccent.copy(alpha = if (passwordFocused) 0.92f else if (isDark) 0.42f else 0.34f)
        Surface(
            onClick = { onSelectField(LoginInputField.PASSWORD) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_password_input"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(passwordBorderWidth, passwordBorder),
            color = bgColor,
            shadowElevation = if (passwordFocused) 4.dp else 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(passwordAccent.copy(alpha = if (passwordFocused) (if (isDark) 0.14f else 0.09f) else (if (isDark) 0.08f else 0.05f)))
                    .padding(start = 14.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = passwordAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = LockIcon,
                            contentDescription = "পাসওয়ার্ড".localized(currentLanguage),
                            tint = passwordAccent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(passwordScroll),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (passwordText.isEmpty()) {
                        if (passwordFocused) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(18.dp)
                                    .background(passwordAccent.copy(alpha = cursorAlpha)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "পাসওয়ার্ড".localized(currentLanguage),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = placeholderColor,
                            maxLines = 1,
                        )
                    } else {
                        Text(
                            text = maskedPassword,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = hindSiliguri,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false,
                        )
                        if (passwordFocused) {
                            Spacer(modifier = Modifier.width(1.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(18.dp)
                                    .background(passwordAccent.copy(alpha = cursorAlpha)),
                            )
                        }
                    }
                }

                androidx.compose.material3.IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("login_password_toggle"),
                ) {
                    Icon(
                        imageVector = if (passwordVisible) EyeOffIcon else EyeIcon,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = passwordAccent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // 3. Login Action Button Card (Identical 3rd card in RoleSelectionScreen)
        val loginBorder = loginAccent.copy(alpha = if (isDark) 0.42f else 0.34f)
        Surface(
            onClick = onLoginSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, loginBorder),
            color = bgColor,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(loginAccent.copy(alpha = if (isDark) 0.08f else 0.05f))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = loginAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = LoginIcon,
                                contentDescription = "লগইন করুন".localized(currentLanguage),
                                tint = loginAccent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Text(
                        text = "লগইন করুন".localized(currentLanguage),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        color = textColor,
                    )
                }

                Icon(
                    imageVector = ChevronRightIcon,
                    contentDescription = null,
                    tint = loginAccent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = !isCustomKeyboardVisible,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Top,
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Top,
            ) + fadeOut(animationSpec = tween(durationMillis = 120)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
                )

                Text(
                    text = "ইমেইল ও পাসওয়ার্ড ব্যবহার করে লগইন করুন। নতুন ব্যবহারকারী হলে শুরু করুন বাটন থেকে সাইন ইন করুন।".localized(currentLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = hindSiliguri,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun countDisplayGlyphs(text: String): Int {
    var count = 0
    var i = 0
    while (i < text.length) {
        val c = text[i]
        if (c.isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate()) {
            i += 2
        } else {
            i += 1
        }
        if (c != '\uFE0F' && c != '\uFE0E' && c != '\u200D') {
            count++
        }
    }
    return count
}

private val DeepBowArchShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height

    moveTo(0f, h)
    lineTo(w, h)
    quadraticTo(
        x1 = w / 2f,
        y1 = -h * 0.85f,
        x2 = 0f,
        y2 = h,
    )
    close()
}

