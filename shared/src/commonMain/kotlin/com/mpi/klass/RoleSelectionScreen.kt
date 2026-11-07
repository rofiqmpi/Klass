package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.GoogleIcon

@Composable
fun RoleSelectionScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    buttonHorizontalPadding: androidx.compose.ui.unit.Dp,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onGoogleClick: () -> Unit,
    onEmailPasswordSignIn: (String, String) -> Unit = { _, _ -> },
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = buttonHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
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

            // 1. Facebook and Apple buttons removed

            // Inputs Column with IME padding
            Column(
                modifier = Modifier.fillMaxWidth().imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 2. Email input field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text(if (currentLanguage == AppLanguage.BENGALI) "ইমেইল অ্যাড্রেস" else "Email address", fontFamily = hindSiliguri) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )

                // 3. Password field split in 2 parts
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text(if (currentLanguage == AppLanguage.BENGALI) "পাসওয়ার্ড" else "Password", fontFamily = hindSiliguri) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(2.5f),
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                            unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )

                    Button(
                        onClick = { onEmailPasswordSignIn(email, password) },
                        modifier = Modifier.weight(0.7f).fillMaxHeight(),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                            contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                        )
                    ) {
                        Text(
                            text = "→",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // 4. 3rd button is Google
            RoleButton(
                text = "Google",
                icon = GoogleIcon,
                isDark = isDark,
                fontFamily = hindSiliguri,
                onClick = onGoogleClick,
            )

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
