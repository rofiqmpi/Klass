package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.AppleIcon
import com.mpi.klass.theme.FacebookIcon
import com.mpi.klass.theme.GoogleIcon

@Composable
fun RoleSelectionScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    buttonHorizontalPadding: androidx.compose.ui.unit.Dp,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onAppleClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = buttonHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = AppStrings.signInTitle(currentLanguage),
                style = TextStyle(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = if (isDark) Color.Black else Color.White,
                        offset = Offset(0f, 0f),
                        blurRadius = 30f,
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RoleButton(
                    text = "Google",
                    icon = GoogleIcon,
                    isDark = isDark,
                    fontFamily = hindSiliguri,
                    onClick = onGoogleClick,
                )
                RoleButton(
                    text = "Facebook",
                    icon = FacebookIcon,
                    isDark = isDark,
                    fontFamily = hindSiliguri,
                    onClick = onFacebookClick,
                )
                RoleButton(
                    text = "Apple",
                    icon = AppleIcon,
                    isDark = isDark,
                    fontFamily = hindSiliguri,
                    onClick = onAppleClick,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppStrings.signInSubtitle(currentLanguage),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = hindSiliguri,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center,
            )
        }
    }
}
