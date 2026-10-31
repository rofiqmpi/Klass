package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.GraduationCapIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudentProfileSetupScreen(
    email: String,
    displayName: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSetupComplete: (StudentProfile) -> Unit,
    onSetupAbandoned: () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = Color(0xFF0284C7)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    val coroutineScope = rememberCoroutineScope()

    // Form state
    var semester by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    // Countdown timer: 5 minutes to complete setup
    var remainingSeconds by remember { mutableStateOf(300) }
    var timerExpired by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        // Start countdown
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        timerExpired = true
        // Delete data and navigate away
        coroutineScope.launch {
            RoleConfig.deleteStudentSignInData(email)
            onSetupAbandoned()
        }
    }

    val semesterOptions = listOf("১ম", "২য়", "৩য়", "৪র্থ", "৫ম", "৬ষ্ঠ", "৭ম")
    val shiftOptions = listOf("সকাল → ১", "বিকাল → ২")
    val departmentOptions = listOf("CST", "CT", "ET", "PT", "MT", "EnT", "EMT")
    val sectionOptions = listOf("A", "B", "C")
    val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    val isFormValid = semester.isNotBlank() && shift.isNotBlank() &&
        department.isNotBlank() && section.isNotBlank() &&
        phone.isNotBlank() && bloodGroup.isNotBlank() &&
        address.isNotBlank() && rollNumber.isNotBlank()

    val timerMinutes = remainingSeconds / 60
    val timerSecs = remainingSeconds % 60
    val timerColor = if (remainingSeconds < 60) Color(0xFFEF4444) else accentColor

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { it / 3 },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBg,
                    shadowElevation = 4.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = GraduationCapIcon,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "প্রোফাইল সেটআপ",
                                        fontSize = 18.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                    )
                                    Text(
                                        text = "সকল তথ্য পূরণ করুন",
                                        fontSize = 12.sp,
                                        fontFamily = hindSiliguri,
                                        color = subTextColor,
                                    )
                                }
                            }

                            // Countdown Timer
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = timerColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, timerColor.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = "${timerMinutes}:${timerSecs.toString().padStart(2, '0')}",
                                    fontSize = 16.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = timerColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Warning Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3CD),
                    border = BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "⚠️ সময় শেষ হলে আপনার অ্যাকাউন্টের সমস্ত তথ্য স্বয়ংক্রিয়ভাবে মুছে যাবে। দ্রুত পূরণ করুন।",
                        fontSize = 12.5.sp,
                        fontFamily = hindSiliguri,
                        color = Color(0xFF856404),
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Academic Info Section
                    SectionHeader(text = "একাডেমিক তথ্য", isDark = isDark, hindSiliguri = hindSiliguri, icon = GraduationCapIcon)

                    // Semester
                    DropdownField(
                        label = "সেমিস্টার",
                        value = semester,
                        options = semesterOptions,
                        onSelect = { semester = it },
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        borderColor = borderColor,
                        accentColor = accentColor,
                    )

                    // Shift
                    DropdownField(
                        label = "শিফট",
                        value = shift,
                        options = shiftOptions,
                        onSelect = { shift = it },
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        borderColor = borderColor,
                        accentColor = accentColor,
                    )

                    // Department
                    DropdownField(
                        label = "ডিপার্টমেন্ট",
                        value = department,
                        options = departmentOptions,
                        onSelect = { department = it },
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        borderColor = borderColor,
                        accentColor = accentColor,
                    )

                    // Section
                    DropdownField(
                        label = "সেকশন",
                        value = section,
                        options = sectionOptions,
                        onSelect = { section = it },
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        borderColor = borderColor,
                        accentColor = accentColor,
                    )

                    // Roll Number
                    ProfileTextField(
                        label = "রোল নং",
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        placeholder = "যেমন: ১২৩৪৫৬",
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        accentColor = accentColor,
                        textColor = textColor,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    SectionHeader(text = "ব্যক্তিগত তথ্য", isDark = isDark, hindSiliguri = hindSiliguri, icon = BookIcon)

                    // Phone
                    ProfileTextField(
                        label = "ফোন নাম্বার",
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "যেমন: ০১৭xxxxxxxx",
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        accentColor = accentColor,
                        textColor = textColor,
                    )

                    // Blood Group
                    DropdownField(
                        label = "ব্লাড গ্রুপ",
                        value = bloodGroup,
                        options = bloodGroupOptions,
                        onSelect = { bloodGroup = it },
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        cardBg = cardBg,
                        textColor = textColor,
                        borderColor = borderColor,
                        accentColor = accentColor,
                    )

                    // Address
                    ProfileTextField(
                        label = "ঠিকানা",
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "আপনার বর্তমান ঠিকানা লিখুন",
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        accentColor = accentColor,
                        textColor = textColor,
                        singleLine = false,
                    )

                    // Error Message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            fontSize = 13.sp,
                            fontFamily = hindSiliguri,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (!isFormValid) {
                                errorMessage = "সকল তথ্য পূরণ করুন"
                                return@Button
                            }
                            errorMessage = null
                            isSaving = true
                            coroutineScope.launch {
                                val shiftValue = if (shift.contains("সকাল")) "1" else "2"
                                val profile = StudentProfile(
                                    email = email.trim().lowercase(),
                                    name = displayName,
                                    semester = semester,
                                    shift = shiftValue,
                                    department = department,
                                    section = section,
                                    phone = phone,
                                    bloodGroup = bloodGroup,
                                    address = address,
                                    rollNumber = rollNumber,
                                    profileSetupComplete = true,
                                )
                                val result = RoleConfig.saveStudentProfile(profile)
                                isSaving = false
                                if (result.isSuccess) {
                                    onSetupComplete(profile)
                                } else {
                                    errorMessage = "সংরক্ষণ করা যায়নি। পুনরায় চেষ্টা করুন।"
                                }
                            }
                        },
                        enabled = !isSaving && !timerExpired,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White,
                            disabledContainerColor = accentColor.copy(alpha = 0.4f),
                        ),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isSaving) "সংরক্ষণ করা হচ্ছে..." else "প্রোফাইল সংরক্ষণ করুন",
                            fontSize = 15.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    icon: ImageVector,
) {
    val accentColor = Color(0xFF0284C7)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            color = accentColor,
        )
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    accentColor: Color,
    textColor: Color,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = hindSiliguri, fontSize = 13.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
            focusedLabelColor = accentColor,
            unfocusedLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
        ),
    )
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    accentColor: Color,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = cardBg,
            border = BorderStroke(
                1.dp,
                if (value.isNotBlank()) accentColor.copy(alpha = 0.6f) else borderColor,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontFamily = hindSiliguri,
                        color = if (value.isNotBlank()) accentColor else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                    )
                    if (value.isNotBlank()) {
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        )
                    } else {
                        Text(
                            text = "নির্বাচন করুন",
                            fontSize = 13.sp,
                            fontFamily = hindSiliguri,
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        )
                    }
                }
                Text(
                    text = "▼",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = hindSiliguri,
                            fontSize = 14.sp,
                            color = if (option == value) accentColor else textColor,
                            fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
