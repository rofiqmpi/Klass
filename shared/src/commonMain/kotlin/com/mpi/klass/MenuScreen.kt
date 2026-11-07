package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onProfileClick: () -> Unit = {},
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "মেনু অপশনসমূহ".localized(),
                    fontSize = 18.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(18.dp))
                MenuItem("প্রোফাইল", hindSiliguri, textColor, subTextColor, onProfileClick)
                MenuItem("বুকস লাইব্রেরি", hindSiliguri, textColor, subTextColor)
                MenuItem("শিক্ষক", hindSiliguri, textColor, subTextColor)
                MenuItem("ক্লাসরুম", hindSiliguri, textColor, subTextColor)
            }
        }
    }
}

@Composable
private fun MenuItem(
    title: String,
    hindSiliguri: FontFamily,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = subTextColor.copy(alpha = 0.08f),
    ) {
        Text(
            text = title.localized(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            fontSize = 15.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
