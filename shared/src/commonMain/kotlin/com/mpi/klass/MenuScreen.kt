package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.NotificationIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.SparkleIcon

@Composable
fun MenuScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onProfileClick: () -> Unit = {},
    onAiClick: () -> Unit = {},
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Gemini AI Professional Card
        ProfessionalMenuCard(
            title = "Gemini AI স্টাডি সহকারী",
            subtitle = "এআই এর সাহায্যে পড়াশোনা করুন ও নোট তৈরি করুন",
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899)),
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            hindSiliguri = hindSiliguri,
            iconVector = SparkleIcon,
            onClick = onAiClick,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Profile Professional Card
        ProfessionalMenuCard(
            title = "প্রোফাইল সেটিংস",
            subtitle = "আপনার অ্যাকাউন্ট ও ব্যক্তিগত তথ্য ম্যানেজ করুন",
            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            hindSiliguri = hindSiliguri,
            iconVector = PersonIcon,
            onClick = onProfileClick,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Books Library Card
        ProfessionalMenuCard(
            title = "বুকস লাইব্রেরি",
            subtitle = "সেমিস্টার অনুযায়ী ডিপার্টমেন্টাল বই ও লেকচার নোট",
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            hindSiliguri = hindSiliguri,
            iconVector = BookIcon,
            onClick = {},
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Teacher Card
        ProfessionalMenuCard(
            title = "শিক্ষকবৃন্দ",
            subtitle = "সম্মানিত শিক্ষকগণের তালিকা ও যোগাযোগের তথ্য",
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            hindSiliguri = hindSiliguri,
            iconVector = GraduationCapIcon,
            onClick = {},
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Classroom Card
        ProfessionalMenuCard(
            title = "ক্লাসরুম ও নোটিশ",
            subtitle = "গুরুত্বপূর্ণ ক্লাস নোটিশ ও রুটিন আপডেট",
            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
            cardBg = cardBg,
            textColor = textColor,
            subTextColor = subTextColor,
            hindSiliguri = hindSiliguri,
            iconVector = NotificationIcon,
            onClick = {},
        )
    }
}

@Composable
private fun ProfessionalMenuCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
    iconVector: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, gradientColors.first().copy(alpha = 0.25f)),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Container with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title.localized(),
                    fontSize = 16.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle.localized(),
                    fontSize = 12.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    color = subTextColor,
                    maxLines = 1,
                )
            }
        }
    }
}
