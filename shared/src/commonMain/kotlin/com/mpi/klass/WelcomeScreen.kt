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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.PaletteIcon
import com.mpi.klass.theme.RightArrowIcon
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
    onGuestClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onTeacherClick: () -> Unit = {},
    onStudentClick: () -> Unit = {},
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
    var showTermsDialog by remember { mutableStateOf(value = false) }
    var isRegistrationMode by remember { mutableStateOf(value = false) }

    AppBackHandler(enabled = isRegistrationMode) {
        isRegistrationMode = false
    }

    val themeCardWidth by animateDpAsState(
        targetValue = if (isThemeExpanded) 135.dp else 36.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    )
    val themeCardHeight by animateDpAsState(
        targetValue = if (isThemeExpanded) 185.dp else 36.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
    )

    val buttonHorizontalPadding = 56.dp

    // Corner shape for Theme Button / Card:
    // When CLOSED: both right corners are FLAT (0.dp) to join the close button!
    // When EXPANDED: top-right is FLAT (0.dp), bottom-right is ROUNDED (10.dp)
    val themeShape = RoundedCornerShape(
        topStart = 10.dp,
        bottomStart = 10.dp,
        topEnd = 0.dp,
        bottomEnd = if (isThemeExpanded) 10.dp else 0.dp,
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

        // Background Backdrop for collapsing theme menu or terms dialog when clicking outside
        if (isThemeExpanded || showTermsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f)
                    .clickable {
                        isThemeExpanded = false
                        showTermsDialog = false
                    },
            )
        }

        // Top Content: 3D Logo (72dp) and College Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp),
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
                text = "ময়মনসিংহ সরকারি\nপলিটেকনিক ইনস্টিটিউট",
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
                .fillMaxWidth(),
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
                            text = "ফোকাস মানে হলো হাজারটা ভালো আইডিয়াকে 'না' বলা। আর সেই একটা 'হ্যাঁ'-এর জন্য শেষ পর্যন্ত লড়ে যাওয়া।",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraLight,
                            fontFamily = hindSiliguri,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                        )

                        Spacer(modifier = Modifier.height(60.dp))

                        // 3D Buttons Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(51.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            Row(
                                modifier = Modifier
                                    .offset(y = 3.dp)
                                    .fillMaxWidth()
                                    .height(48.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                bottomStart = 12.dp,
                                                topEnd = 0.dp,
                                                bottomEnd = 0.dp,
                                            ),
                                        )
                                        .background(Color(0xFF94A3B8)),
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
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
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(51.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                OutlinedButton(
                                    onClick = onGuestClick,
                                    interactionSource = guestInteractionSource,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .offset(y = if (isGuestPressed) 3.dp else 0.dp),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        bottomStart = 12.dp,
                                        topEnd = 0.dp,
                                        bottomEnd = 0.dp,
                                    ),
                                    border = BorderStroke(0.5.dp, Color(0xFF94A3B8)),
                                    contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                ) {
                                    Text(
                                        text = "গেস্ট মোড",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                    )
                                }

                                Button(
                                    onClick = {
                                        isRegistrationMode = true
                                    },
                                    interactionSource = startInteractionSource,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .offset(y = if (isStartPressed) 3.dp else 0.dp),
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 0.dp,
                                        topEnd = 12.dp,
                                        bottomEnd = 12.dp,
                                    ),
                                    border = BorderStroke(0.5.dp, Color(0xFF8E7864)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8CB9D9),
                                        contentColor = Color(0xFF1E293B),
                                    ),
                                ) {
                                    Text(
                                        text = "রেজিস্ট্রেশন",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "টার্মস অ্যান্ড কন্ডিশন",
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
                            RoleSelectionScreen(
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                buttonHorizontalPadding = buttonHorizontalPadding,
                                onAdminClick = onAdminClick,
                                onTeacherClick = onTeacherClick,
                                onStudentClick = onStudentClick,
                            )
                        }

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
                                    contentDescription = "গেস্ট মোড",
                                    tint = guestContentColor,
                                    modifier = Modifier.size(16.dp),
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "গেস্ট মোড",
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

        // Top Right Corner 3D Header Layer (Fixed right edge, zIndex 10f)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
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
                                text = "থিম নির্বাচন করুন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
                                                contentDescription = mode.getDisplayName(),
                                                tint = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                modifier = Modifier.size(15.dp),
                                            )

                                            Text(
                                                text = mode.getDisplayName(),
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

            // Right: Smart 1:1 3D Close Button (Original 2 behaviors: Close Theme Menu or Close App)
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(39.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = 3.dp)
                        .width(36.dp)
                        .height(36.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = 10.dp,
                                bottomEnd = 10.dp,
                            ),
                        )
                        .background(Color(0xFF8E7864)),
                )

                Button(
                    onClick = {
                        if (isThemeExpanded) {
                            isThemeExpanded = false
                        } else if (isRegistrationMode) {
                            isRegistrationMode = false
                        } else {
                            onCloseApp()
                        }
                    },
                    interactionSource = closeBtnInteractionSource,
                    modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                        .offset(y = if (isCloseBtnPressed) 3.dp else 0.dp),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 10.dp,
                        bottomEnd = 10.dp,
                    ),
                    border = BorderStroke(0.5.dp, Color(0xFF8E7864)),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8CB9D9),
                        contentColor = Color(0xFF1E293B),
                    ),
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Close",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(16.dp),
                    )
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
                                            text = "সেবার শর্তাবলী ও প্রাইভেসি পলিসি",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "দয়া করে নিচের নিয়মগুলো দেখে নিন:",
                                            fontSize = 13.sp,
                                            fontFamily = hindSiliguri,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        )
                                    }

                                    // 3. Points items
                                    val points = listOf(
                                        "ডাটা সিকিউরিটি ও প্রাইভেসি" to "আপনার ডাটা ব্যাকএন্ডে এনক্রিপ্ট করে সংরক্ষিত হয় এবং কোনো থার্ড-পার্টির নিকট হস্তান্তরযোগ্য নয়।",
                                        "রিয়েলটাইম অথেনটিকেশন" to "আপনার অ্যাকাউন্টের নিরাপত্তা সর্বোচ্চ। আপনার ব্যক্তিগত তথ্য সংগ্রহ বা সংরক্ষণ করা হয় না।",
                                        "এআই মডেল ট্রেনিং" to "সার্ভিস অপটিমাইজেশন, এআই লার্নিং ও এলএলএম (LLM) ফাইন-টিউনিংয়ের জন্য ব্যবহারকারীর অ্যানোনিমাস (Anonymous) ডায়ালগ ও ইন্টারেকশন ডাটা ব্যবহৃত হতে পারে।",
                                        "অ্যালগরিদমিক ডিসক্লেইমার" to "এআই অ্যাসিস্ট্যান্ট বা অটো-জেনারেটেড রুটিন সহায়তামূলক ইনফারেন্স মাত্র; যেকোনো চূড়ান্ত তথ্যের জন্য প্রাতিষ্ঠানিক নোটিশই প্রামাণিক।",
                                        "সিস্টেম ইন্টিগ্রিটি" to "কোনো প্রকার রিভার্স ইঞ্জিনিয়ারিং, এপিআই এক্সপ্লয়েট, বা অননুমোদিত অ্যাকসেসের চেষ্টা করা হলে সিস্টেম থেকে স্থায়ীভাবে ব্লক করা হবে।",
                                        "পলিসি আপডেট" to "সিস্টেম আর্কিটেকচার বা সার্ভিস আপগ্রেডের প্রয়োজনে টার্মস কন্ডিশন যেকোনো সময় পরিবর্তিত হতে পারে."
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
                                                    text = title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = hindSiliguri,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = desc,
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
                                                text = "সেবার শর্তাবলী ও প্রাইভেসি পলিসি",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            Text(
                                                text = "দয়া করে নিচের নিয়মগুলো দেখে নিন:",
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
    icon: DrawableResource,
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
                painter = painterResource(icon),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
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
