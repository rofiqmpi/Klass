package com.mpi.klass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.notiscreen
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotificationScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.notiscreen),
            contentDescription = "Notification Screen",
            modifier = Modifier.size(130.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "কোনো নতুন নোটিফিকেশন নেই".localized(),
            fontSize = 14.sp,
            fontFamily = hindSiliguri,
            color = subTextColor,
        )
    }
}
