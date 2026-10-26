package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.AutoModeIcon
import com.mpi.klass.theme.DarkModeIcon
import com.mpi.klass.theme.HomeIcon
import com.mpi.klass.theme.LightModeIcon
import com.mpi.klass.theme.LogoutIcon
import com.mpi.klass.theme.MenuIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.getHindSiliguriFontFamily
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.welcome
import org.jetbrains.compose.resources.painterResource

@Composable
fun GuestHomeScreen(
    currentThemeMode: ThemeMode = ThemeMode.DEFAULT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onSwitchToLogin: () -> Unit = {},
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.DEFAULT -> systemInDark
    }

    val hindSiliguri = getHindSiliguriFontFamily()
    var selectedTab by remember { mutableStateOf("home") }

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val isGesture = remember { isGestureNavigation() }
    val exactCornerRadiusDp = remember { getDisplayBottomCornerRadiusDp() }
    val bottomRadius = if (isGesture) exactCornerRadiusDp.coerceIn(30f, 43f).dp else 0.dp

    val homeShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = bottomRadius,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
    )

    val menuShape = RoundedCornerShape(
        topStart = 0.dp,
        bottomStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = bottomRadius,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Top Bar: Header & Theme Toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ময়মনসিংহ সরকারি পলিটেকনিক",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = hindSiliguri,
                            color = subTextColor,
                        )
                        Text(
                            text = "স্বাগতম, গেস্ট ইউজার",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = textColor,
                        )
                    }

                    // Logout / Switch to Login Button
                    Surface(
                        onClick = onSwitchToLogin,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(40.dp),
                        shadowElevation = 2.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = LogoutIcon,
                                contentDescription = "লগ আউট",
                                tint = textColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            val nextMode = when (currentThemeMode) {
                                ThemeMode.DEFAULT -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.DEFAULT
                            }
                            onThemeModeChange(nextMode)
                        },
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(40.dp),
                        shadowElevation = 2.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            val icon = when (currentThemeMode) {
                                ThemeMode.LIGHT -> LightModeIcon
                                ThemeMode.DARK -> DarkModeIcon
                                ThemeMode.DEFAULT -> AutoModeIcon
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Theme Toggle",
                                tint = textColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            // Center Content: welcome.png Image Displayed in Center of Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.welcome),
                    contentDescription = "Welcome",
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    contentScale = ContentScale.Fit,
                )
            }

            // Bottom Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardBg,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Thinner 0.5dp Top Border Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(if (isDark) Color.White else Color.Black),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp)
                            .height(51.dp),
                    ) {
                        val homeInteractionSource = remember { MutableInteractionSource() }
                        val isHomePressed by homeInteractionSource.collectIsPressedAsState()

                        val menuInteractionSource = remember { MutableInteractionSource() }
                        val isMenuPressed by menuInteractionSource.collectIsPressedAsState()

                        val isHomeSelected = selectedTab == "home"
                        val isMenuSelected = selectedTab == "menu"

                        // 3D Shadow Layer (matches button height 48.dp + 3.dp offset = exactly 51.dp)
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
                                    .clip(homeShape)
                                    .background(if (isHomeSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(menuShape)
                                    .background(if (isMenuSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                            )
                        }

                        // Active Buttons Layer (height 48.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            // 1. Home Icon Button
                            Button(
                                onClick = { selectedTab = "home" },
                                interactionSource = homeInteractionSource,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .offset(y = if (isHomePressed) 3.dp else 0.dp),
                                shape = homeShape,
                                border = BorderStroke(0.5.dp, if (isHomeSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHomeSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                    contentColor = if (isDark) Color.White else Color.Black,
                                ),
                            ) {
                                Icon(
                                    imageVector = HomeIcon,
                                    contentDescription = "Home",
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(26.dp),
                                )
                            }

                            // 2. Menu Icon Button
                            Button(
                                onClick = { selectedTab = "menu" },
                                interactionSource = menuInteractionSource,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .offset(y = if (isMenuPressed) 3.dp else 0.dp),
                                shape = menuShape,
                                border = BorderStroke(0.5.dp, if (isMenuSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMenuSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                    contentColor = if (isDark) Color.White else Color.Black,
                                ),
                            ) {
                                Icon(
                                    imageVector = MenuIcon,
                                    contentDescription = "Menu",
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
