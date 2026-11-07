package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.mpi.klass.theme.EmailIcon
import com.mpi.klass.theme.GoogleIcon
import com.mpi.klass.theme.LockIcon

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

    val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    val fieldBorderColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
    val iconTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = buttonHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isImeVisible) 8.dp else 10.dp),
        ) {
            Text(
                text = AppStrings.signInTitle(currentLanguage),
                style = TextStyle(
                    fontSize = if (isImeVisible) 19.sp else 21.sp,
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

            // Inputs Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Email input field (matching Google button height 48.dp & radius 8.dp, no focused border change)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = if (currentLanguage == AppLanguage.BENGALI) "ইমেইল অ্যাড্রেস" else "Email address",
                            fontFamily = hindSiliguri,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = EmailIcon,
                            contentDescription = "Email",
                            modifier = Modifier.size(20.dp),
                            tint = iconTint,
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                        lineHeight = 14.sp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = fieldBorderColor,
                        unfocusedBorderColor = fieldBorderColor,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                // Password field split in 2 parts (matching Google button height 48.dp & radius 8.dp, no focused border change)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = {
                            Text(
                                text = if (currentLanguage == AppLanguage.BENGALI) "পাসওয়ার্ড" else "Password",
                                fontFamily = hindSiliguri,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = LockIcon,
                                contentDescription = "Password",
                                modifier = Modifier.size(20.dp),
                                tint = iconTint,
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                            lineHeight = 14.sp,
                        ),
                        modifier = Modifier
                            .weight(2.5f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = fieldBorderColor,
                            unfocusedBorderColor = fieldBorderColor,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )

                    Button(
                        onClick = { onEmailPasswordSignIn(email, password) },
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), // Neutral grey / snow-slate color
                            contentColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                        ),
                    ) {
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Google button
            RoleButton(
                text = "Google",
                icon = GoogleIcon,
                isDark = isDark,
                fontFamily = hindSiliguri,
                onClick = onGoogleClick,
            )

            if (!isImeVisible) {
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
}
