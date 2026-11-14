package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle
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
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CrownIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.LanguageGlobeIcon
import com.mpi.klass.theme.LightModeIcon
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

    val languageInteractionSource = remember { MutableInteractionSource() }
    val isLanguagePressed by languageInteractionSource.collectIsPressedAsState()

    val guestInteractionSource = remember { MutableInteractionSource() }
    val isGuestPressed by guestInteractionSource.collectIsPressedAsState()
    val isGuestHovered by guestInteractionSource.collectIsHoveredAsState()
    val isGuestActive = isGuestPressed || isGuestHovered

    val startInteractionSource = remember { MutableInteractionSource() }
    val isStartPressed by startInteractionSource.collectIsPressedAsState()

    val themeBtnInteractionSource = remember { MutableInteractionSource() }
    val isThemeBtnPressed by themeBtnInteractionSource.collectIsPressedAsState()

    val closeBtnInteractionSource = remember { MutableInteractionSource() }
    val isCloseBtnPressed by closeBtnInteractionSource.collectIsPressedAsState()

    var isThemeExpanded by remember { mutableStateOf(value = false) }
    var isLanguageExpanded by remember { mutableStateOf(value = false) }
    var showTermsDialog by remember { mutableStateOf(value = false) }
    var showAboutDialog by remember { mutableStateOf(value = false) }
    var isRegistrationMode by remember { mutableStateOf(value = false) }

    var lastBackPressTime by remember { mutableStateOf(0L) }

    AppBackHandler(enabled = true) {
        when {
            showTermsDialog -> showTermsDialog = false
            showAboutDialog -> showAboutDialog = false
            isThemeExpanded -> isThemeExpanded = false
            isLanguageExpanded -> isLanguageExpanded = false
            isRegistrationMode -> isRegistrationMode = false
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

    val languageCardWidth by animateDpAsState(
        targetValue = if (isLanguageExpanded) 118.dp else 42.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    )
    val languageCardHeight by animateDpAsState(
        targetValue = if (isLanguageExpanded) 146.dp else 36.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    )

    val buttonHorizontalPadding = 36.dp

    // Corner shape for Theme Button / Card:
    val themeShape = RoundedCornerShape(
        topStart = 10.dp,
        bottomStart = 10.dp,
        topEnd = 0.dp,
        bottomEnd = if (isThemeExpanded) 10.dp else 0.dp,
    )

    // Corner shape for Language Button / Card:
    val languageShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = if (isLanguageExpanded) 10.dp else 0.dp,
        topEnd = 10.dp,
        bottomEnd = 10.dp,
    )

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Fullscreen Background Image
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = "Welcome Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        // Background Backdrop for collapsing theme or language menu or terms dialog when clicking outside
        if (isThemeExpanded || showTermsDialog || isLanguageExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f)
                    .clickable {
                        isThemeExpanded = false
                        showTermsDialog = false
                        isLanguageExpanded = false
                    },
            )
        }

        // Top Content: 3D Logo (72dp) and College Name
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

        // Bottom Section: Transitions between Initial Mode and Registration Mode
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp)
                .fillMaxWidth()
                .zIndex(20f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedContent(
                targetState = isRegistrationMode,
                transitionSpec = {
                    val duration = 550
                    val easing = FastOutSlowInEasing

                    if (targetState) {
                        (slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight / 4 },
                            animationSpec = tween(durationMillis = duration, easing = easing),
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = duration, easing = easing),
                        )).togetherWith(
                            slideOutVertically(
                                targetOffsetY = { fullHeight -> -fullHeight / 4 },
                                animationSpec = tween(durationMillis = duration, easing = easing),
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = duration, easing = easing),
                            ),
                        )
                    } else {
                        (slideInVertically(
                            initialOffsetY = { fullHeight -> -fullHeight / 4 },
                            animationSpec = tween(durationMillis = duration, easing = easing),
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = duration, easing = easing),
                        )).togetherWith(
                            slideOutVertically(
                                targetOffsetY = { fullHeight -> fullHeight / 4 },
                                animationSpec = tween(durationMillis = duration, easing = easing),
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = duration, easing = easing),
                            ),
                        )
                    }
                },
                label = "RegistrationModeTransition",
            ) { registrationMode ->
                if (!registrationMode) {
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

                        // 3D Buttons Container (About & Continue — slightly narrower, icon-free)
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
                                // 3D About Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(51.dp)
                                        .zIndex(30f),
                                    contentAlignment = Alignment.BottomStart,
                                ) {
                                    // 3D Shadow Base
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
                                            isLanguageExpanded = false
                                            showTermsDialog = false
                                            showAboutDialog = true
                                        },
                                        interactionSource = languageInteractionSource,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(bottom = if (isLanguagePressed) 0.dp else 3.dp)
                                            .fillMaxWidth()
                                            .height(48.dp),
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
                                                text = "অ্যাপ সম্পর্কে".localized(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                        }
                                    }
                                }

                                // 3D Continue / Registration Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(51.dp),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    // 3D Shadow underneath
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

                                    // Foreground Button
                                    Button(
                                        onClick = {
                                            isThemeExpanded = false
                                            isLanguageExpanded = false
                                            isRegistrationMode = true
                                        },
                                        interactionSource = startInteractionSource,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .offset(y = if (isStartPressed) 3.dp else 0.dp),
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
                    // Registration Mode Layout
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

                    val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { it / 2 },
                            exit = fadeOut(animationSpec = tween(350)) + slideOutVertically(animationSpec = tween(350)) { it / 2 },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
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

                        if (!isImeVisible) {
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
            // Theme 3D Button (Expands leftwards & downwards without covering the Close button!)
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
                            isLanguageExpanded = false
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

            // Right: 3D Language Expandable Button (Expands width + height like Theme button, with header + BN & EN options)
            Box(
                modifier = Modifier
                    .width(languageCardWidth)
                    .height(languageCardHeight + 3.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                // 3D Base Shape
                Box(
                    modifier = Modifier
                        .offset(y = 3.dp)
                        .width(languageCardWidth)
                        .height(languageCardHeight)
                        .clip(languageShape)
                        .background(Color(0xFF8E7864)),
                )

                // Foreground Animated 3D Surface Card
                Surface(
                    onClick = {
                        if (!isLanguageExpanded) {
                            showTermsDialog = false
                            isThemeExpanded = false
                            isLanguageExpanded = true
                        }
                    },
                    interactionSource = closeBtnInteractionSource,
                    modifier = Modifier
                        .width(languageCardWidth)
                        .height(languageCardHeight)
                        .offset(y = if (isCloseBtnPressed && !isLanguageExpanded) 3.dp else 0.dp),
                    shape = languageShape,
                    border = BorderStroke(0.5.dp, Color(0xFF8E7864)),
                    color = if (isLanguageExpanded) MaterialTheme.colorScheme.surface else Color(0xFF8CB9D9),
                ) {
                    if (!isLanguageExpanded) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = LanguageGlobeIcon,
                                contentDescription = "ভাষা".localized(),
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "ভাষা নির্বাচন করুন".localized(),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(
                                    AppLanguage.BENGALI to "BN",
                                    AppLanguage.ENGLISH to "EN",
                                ).forEach { (lang, code) ->
                                    val isSelected = lang == currentLanguage
                                    Surface(
                                        onClick = {
                                            onLanguageChange(lang)
                                            isLanguageExpanded = false
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
                                                imageVector = LanguageGlobeIcon,
                                                contentDescription = code,
                                                tint = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                modifier = Modifier.size(15.dp),
                                            )

                                            Text(
                                                text = code,
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
        }

        // Full-Screen About App Modal
        if (showAboutDialog) {
            val cardSurface = if (isDark) Color(0xFF1E293B) else Color.White
            val borderSubtle = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

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

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                ) {
                    // Top Bar with Close Button
                    Surface(
                        color = cardSurface,
                        shadowElevation = 3.dp,
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.splash),
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Column {
                                    Text(
                                        text = "অ্যাপ সম্পর্কে".localized(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "MPI Klass • ${"ভার্সন ১.০.০".localized()}",
                                        fontSize = 11.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            Surface(
                                onClick = { showAboutDialog = false },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier.size(40.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = CloseIcon,
                                        contentDescription = "বন্ধ করুন".localized(),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Scrollable Full-Screen Features Content
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

                        Text(
                            text = "অ্যাপের সকল ফিচারসমূহ".localized(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                        )

                        features.forEach { (icon, accent, titleDesc) ->
                            val (featureTitle, featureDesc) = titleDesc
                            val localizedTitle = featureTitle.localized()
                            val localizedDesc = featureDesc.localized()
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardSurface,
                                border = BorderStroke(1.dp, borderSubtle),
                                shadowElevation = 2.dp,
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

                        Button(
                            onClick = { showAboutDialog = false },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Text(
                                text = "বন্ধ করুন".localized(),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Terms & Conditions Center Modal Popup with policy image at the very top and full-height scrollbar indicator
        if (showTermsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { showTermsDialog = false },
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = showTermsDialog,
                    enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
                    ),
                    exit = fadeOut(animationSpec = tween(200)),
                ) {
                    val scrollState = rememberScrollState()

                    // Outer Column to hold Surface and 5dp-below Close Button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight()
                            .clickable(enabled = false) {},
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            shadowElevation = 16.dp,
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Scrollable Content with 100% smooth linear scrolling
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(scrollState)
                                        .padding(horizontal = 22.dp, vertical = 22.dp)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    // 1. Policy Image at the top
                                    Image(
                                        painter = painterResource(Res.drawable.policy),
                                        contentDescription = "Policy Illustration",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit,
                                    )

                                    // 2. Title & Subtitle (normal flow)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = "সেবার শর্তাবলী ও প্রাইভেসি পলিসি".localized(),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "দয়া করে নিচের নিয়মগুলো দেখে নিন:".localized(),
                                            fontSize = 13.sp,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        )
                                    }

                                    // 3. Points items
                                    val points = listOf(
                                        "গোপনীয়তা ও সুরক্ষা" to "শিক্ষার্থী ও শিক্ষকগণের অ্যাকাউন্টের গোপনীয়তা এবং ব্যক্তিগত তথ্য সম্পূর্ণ সুরক্ষিত রাখা হয়।",
                                        "নিরাপদ সাইন-ইন" to "নিরাপদ ও ভেরিফাইড অথেনটিকেশন ব্যবস্থার মাধ্যমে আপনার অ্যাকাউন্ট সুরক্ষিত থাকে।",
                                        "এআই স্টাডি সহায়তা" to "জেনারেটিভ এআই অ্যাসিস্ট্যান্ট শিক্ষার্থীদের পড়াশোনা, রুটিন এবং একাডেমিক প্রশ্নোত্তরে সহায়তা করে।",
                                        "অফিশিয়াল নোটিশ" to "প্রতিষ্ঠান ও ডিপার্টমেন্টের জরুরি নোটিশ এবং রুটিন নিয়মিত আপডেট করা হয়।",
                                        "নিয়মাবলী ও শৃঙ্খলা" to "অ্যাপ ব্যবহারের সময় প্রাতিষ্ঠানিক নিয়মকানুন ও শালীনতা বজায় রাখা বাধ্যতামূলক।",
                                        "সেবা আপডেট" to "শিক্ষার্থীদের সুবিধার্থে অ্যাপে নতুন ফিচার ও আপডেট নিয়মিত যুক্ত করা হবে।"
                                    )

                                    points.forEachIndexed { index, (title, desc) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            Text(
                                                text = "${index + 1}.",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = title.localized(),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = hindSiliguri,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = desc.localized(),
                                                    fontSize = 13.sp,
                                                    fontFamily = hindSiliguri,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                    lineHeight = 19.sp,
                                                )
                                            }
                                        }
                                    }
                                }

                                // Sticky Header Overlay when scrolled past image threshold (~140px)
                                val showStickyHeader by remember {
                                    derivedStateOf { scrollState.value > 140 }
                                }
                                if (showStickyHeader) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .zIndex(10f),
                                        shadowElevation = 4.dp,
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 22.dp)
                                                .padding(top = 14.dp, bottom = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = "সেবার শর্তাবলী ও প্রাইভেসি পলিসি".localized(),
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            Text(
                                                text = "দয়া করে নিচের নিয়মগুলো দেখে নিন:".localized(),
                                                fontSize = 13.sp,
                                                fontFamily = hindSiliguri,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                            )
                                        }
                                    }
                                }

                                // Butter-smooth linear scrollbar indicator with zIndex(20f) so it's always above sticky header
                                val hasMaxValue by remember {
                                    derivedStateOf { scrollState.maxValue > 0 }
                                }
                                if (hasMaxValue) {
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .zIndex(20f)
                                    ) {
                                        val viewportHeight = maxHeight
                                        val scrollProgress by remember {
                                            derivedStateOf {
                                                if (scrollState.maxValue > 0) {
                                                    scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                                                } else 0f
                                            }
                                        }
                                        val thumbHeight = 44.dp
                                        val maxOffset = (viewportHeight - thumbHeight - 32.dp).coerceAtLeast(0.dp)
                                        val thumbOffset = maxOffset * scrollProgress

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 8.dp, top = 16.dp, bottom = 16.dp)
                                                .width(7.dp)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(3.5.dp)),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .offset(y = thumbOffset)
                                                    .width(7.dp)
                                                    .height(thumbHeight)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), RoundedCornerShape(3.5.dp)),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Clean, elegant solid primary Close Button
                        Surface(
                            onClick = { showTermsDialog = false },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                            shadowElevation = 6.dp,
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = CloseIcon,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(20.dp),
                                )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val borderColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        color = cardBgColor,
        shadowElevation = if (isDark) 2.dp else 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = textColor,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textColor,
            )
        }
    }
}

@Composable
internal fun RoleButton(
    text: String,
    painter: DrawableResource,
    isDark: Boolean,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val borderColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        color = cardBgColor,
        shadowElevation = if (isDark) 2.dp else 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(painter),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textColor,
            )
        }
    }
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

private fun Modifier.expandUpwards(targetHeight: Dp): Modifier = this.layout { measurable, constraints ->
    val heightPx = targetHeight.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minHeight = heightPx,
            maxHeight = heightPx,
        ),
    )
    val layoutHeight = constraints.maxHeight
    layout(placeable.width, layoutHeight) {
        placeable.placeRelative(0, layoutHeight - placeable.height)
    }
}

