package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.getHindSiliguriFontFamily
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.home
import klass.shared.generated.resources.menu
import klass.shared.generated.resources.notification
import org.jetbrains.compose.resources.painterResource

/**
 * Floating Home Routine header with elevated Profile, AI, and Notification action buttons.
 */
@Composable
fun HomeRoutineHeader(
    isDark: Boolean,
    cardBg: Color,
    photoUrl: String?,
    avatarSeed: String,
    onProfileClick: () -> Unit,
    onAiClick: () -> Unit,
    onNotificationClick: () -> Unit,
    titleText: String = "Class Routine",
    subtitleText: String = "",
) {
    val hindSiliguri = getHindSiliguriFontFamily()
    val primaryAccent = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    val notiAccent = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val sectionBorder = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.35f))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = cardBg,
                border = BorderStroke(1.5.dp, primaryAccent.copy(alpha = 0.55f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .testTag("home_profile_button"),
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = resolveProfileImageModel(photoUrl),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    DynamicAvatar(
                        seed = avatarSeed,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = onProfileClick,
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                border = sectionBorder,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("home_header_profile_badge"),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 13.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = titleText,
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitleText.isNotBlank()) {
                        Text(
                            text = subtitleText,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Medium,
                            color = subTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onAiClick,
                shape = CircleShape,
                color = cardBg,
                border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.45f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("home_ai_button"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SparkleIcon,
                        contentDescription = "Study Assistant",
                        tint = primaryAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Surface(
                onClick = onNotificationClick,
                shape = CircleShape,
                color = cardBg,
                border = BorderStroke(1.dp, notiAccent.copy(alpha = 0.45f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("home_notification_button"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.notification),
                        contentDescription = "Notifications",
                        tint = notiAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/**
 * Tactile 3D split bottom bar keeping the Home and Menu drawable icons intact,
 * with concentric display bottom corner radius adaptation (homeShape / menuShape) for gesture navigation.
 */
@Composable
fun HomeMenuBottomBar(
    isHomeSelected: Boolean,
    isDark: Boolean,
    cardBg: Color,
    hindSiliguri: FontFamily,
    homeShape: Shape,
    menuShape: Shape,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val isMenuSelected = !isHomeSelected
    val isGesture = remember { isGestureNavigation() }
    val horizontalPad = if (isGesture) 6.dp else 0.dp
    val bottomPad = if (isGesture) 4.dp else 0.dp
    val barHeight = 52.dp
    val lipDepth = 3.dp

    val activeFg = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    val activeBase = if (isDark) Color(0xFF0284C7) else Color(0xFF1D4ED8)
    val activeContent = if (isDark) Color(0xFF0F172A) else Color.White

    val inactiveFg = cardBg
    val inactiveBase = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
    val inactiveContent = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    val homeInteraction = remember { MutableInteractionSource() }
    val isHomePressed by homeInteraction.collectIsPressedAsState()

    val menuInteraction = remember { MutableInteractionSource() }
    val isMenuPressed by menuInteraction.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPad)
            .padding(bottom = bottomPad)
            .height(barHeight + lipDepth),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Home 3D Split Button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight + lipDepth),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .offset(y = lipDepth)
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(homeShape)
                    .background(if (isHomeSelected) activeBase else inactiveBase),
            )

            Surface(
                onClick = onHomeClick,
                interactionSource = homeInteraction,
                shape = homeShape,
                color = if (isHomeSelected) activeFg else inactiveFg,
                border = BorderStroke(0.75.dp, if (isHomeSelected) activeBase else inactiveBase),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .offset(y = if (isHomePressed) lipDepth else 0.dp)
                    .testTag("bottom_bar_home"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.home),
                        contentDescription = "হোম".localized(),
                        tint = if (isHomeSelected) activeContent else inactiveContent,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হোম".localized(),
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isHomeSelected) activeContent else inactiveContent,
                        maxLines = 1,
                    )
                }
            }
        }

        // Menu 3D Split Button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight + lipDepth),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .offset(y = lipDepth)
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(menuShape)
                    .background(if (isMenuSelected) activeBase else inactiveBase),
            )

            Surface(
                onClick = onMenuClick,
                interactionSource = menuInteraction,
                shape = menuShape,
                color = if (isMenuSelected) activeFg else inactiveFg,
                border = BorderStroke(0.75.dp, if (isMenuSelected) activeBase else inactiveBase),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .offset(y = if (isMenuPressed) lipDepth else 0.dp)
                    .testTag("bottom_bar_menu"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.menu),
                        contentDescription = "মেনু".localized(),
                        tint = if (isMenuSelected) activeContent else inactiveContent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মেনু".localized(),
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = if (isMenuSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isMenuSelected) activeContent else inactiveContent,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
