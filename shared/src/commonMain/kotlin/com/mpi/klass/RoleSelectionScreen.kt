package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.admin
import klass.shared.generated.resources.student
import klass.shared.generated.resources.teacher

@Composable
fun RoleSelectionScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    buttonHorizontalPadding: androidx.compose.ui.unit.Dp,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onStudentClick: () -> Unit,
    onTeacherClick: () -> Unit,
    onAdminClick: () -> Unit,
) {
    val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = buttonHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isImeVisible) 10.dp else 12.dp),
        ) {
            // 1. Student Button
            RoleButton(
                text = "শিক্ষার্থী".localized(),
                painter = Res.drawable.student,
                isDark = isDark,
                fontFamily = hindSiliguri,
                onClick = onStudentClick,
            )

            // 2. Teacher Button
            RoleButton(
                text = "শিক্ষক".localized(),
                painter = Res.drawable.teacher,
                isDark = isDark,
                fontFamily = hindSiliguri,
                onClick = onTeacherClick,
            )

            // 3. Admin Button
            RoleButton(
                text = "এডমিন".localized(),
                painter = Res.drawable.admin,
                isDark = isDark,
                fontFamily = hindSiliguri,
                onClick = onAdminClick,
            )

            if (!isImeVisible) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
                )


                Text(
                    text = "আপনার অ্যাকাউন্ট ক্যাটাগরি নির্বাচন করুন। দয়া করে ভুল তথ্য দেওয়া থেকে বিরত থাকুন।".localized(),
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
