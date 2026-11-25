package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private enum class ProfileField {
    NAME,
    DEPARTMENT,
    SEMESTER,
    SHIFT,
    SECTION,
    PHONE,
    BLOOD_GROUP,
    ADDRESS,
    ROLL_NUMBER,
    REGISTRATION_NUMBER,
    FATHER_NAME,
    MOTHER_NAME,
    DOB,
    RELIGION,
    GENDER,
    MARITAL_STATUS,
    CONTACT_EMAIL,
    PERMANENT_ADDRESS,
    BIO,
}

private data class ProfileSetupStep(
    val field: ProfileField,
    val title: String,
    val hint: String,
    val required: Boolean = false,
    val multiline: Boolean = false,
)

private fun profileSetupSteps(role: UserRole): List<ProfileSetupStep> = buildList {
    add(ProfileSetupStep(ProfileField.NAME, "আপনার নাম কী?", "প্রোফাইলে যে নাম দেখাবে", required = true))
    add(ProfileSetupStep(ProfileField.DEPARTMENT, "আপনার বিভাগ কোনটি?", "আপনার বিভাগ নির্বাচন করুন", required = true))
    if (role == UserRole.STUDENT) {
        add(ProfileSetupStep(ProfileField.SEMESTER, "আপনি কোন সেমিস্টারে পড়েন?", "বর্তমান সেমিস্টার নির্বাচন করুন", required = true))
    }
    add(ProfileSetupStep(ProfileField.SHIFT, "আপনার শিফট কোনটি?", "শিফট নির্বাচন করুন", required = true))
    if (role == UserRole.STUDENT) {
        add(ProfileSetupStep(ProfileField.SECTION, "আপনার গ্রুপ কোনটি?", "আপনার গ্রুপ নির্বাচন করুন", required = true))
        add(ProfileSetupStep(ProfileField.ROLL_NUMBER, "আপনার রোল নম্বর", "ঐচ্ছিক"))
        add(ProfileSetupStep(ProfileField.REGISTRATION_NUMBER, "রেজিস্ট্রেশন নম্বর", "ঐচ্ছিক"))
    }
    add(ProfileSetupStep(ProfileField.PHONE, "ফোন নম্বর", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.BLOOD_GROUP, "রক্তের গ্রুপ", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.ADDRESS, "বর্তমান ঠিকানা", "ঐচ্ছিক", multiline = true))
    add(ProfileSetupStep(ProfileField.FATHER_NAME, "বাবার নাম", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.MOTHER_NAME, "মায়ের নাম", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.DOB, "জন্মতারিখ", "ঐচ্ছিক • যেমন: 01 Jan 2000"))
    add(ProfileSetupStep(ProfileField.RELIGION, "ধর্ম", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.GENDER, "লিঙ্গ", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.MARITAL_STATUS, "বৈবাহিক অবস্থা", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.CONTACT_EMAIL, "যোগাযোগের ইমেইল", "ঐচ্ছিক"))
    add(ProfileSetupStep(ProfileField.PERMANENT_ADDRESS, "স্থায়ী ঠিকানা", "ঐচ্ছিক", multiline = true))
    add(ProfileSetupStep(ProfileField.BIO, "আপনার সম্পর্কে", "ঐচ্ছিক", multiline = true))
}

@Composable
fun ProfileSetupScreen(
    role: UserRole,
    initialProfile: StudentProfile,
    hindSiliguri: FontFamily,
    onSave: suspend (StudentProfile) -> Result<Unit>,
    onComplete: (StudentProfile) -> Unit,
) {
    val steps = remember(role) { profileSetupSteps(role) }
    var profile by remember(initialProfile.email) { mutableStateOf(initialProfile) }
    var stepIndex by remember(initialProfile.email) { mutableStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val step = steps[stepIndex]
    val cs = MaterialTheme.colorScheme
    val value = when (step.field) {
        ProfileField.NAME -> profile.name
        ProfileField.DEPARTMENT -> profile.department
        ProfileField.SEMESTER -> profile.semester
        ProfileField.SHIFT -> profile.shift
        ProfileField.SECTION -> profile.section
        ProfileField.PHONE -> profile.phone
        ProfileField.BLOOD_GROUP -> profile.bloodGroup
        ProfileField.ADDRESS -> profile.address
        ProfileField.ROLL_NUMBER -> profile.rollNumber
        ProfileField.REGISTRATION_NUMBER -> profile.registrationNumber
        ProfileField.FATHER_NAME -> profile.fatherName
        ProfileField.MOTHER_NAME -> profile.motherName
        ProfileField.DOB -> profile.dob
        ProfileField.RELIGION -> profile.religion
        ProfileField.GENDER -> profile.gender
        ProfileField.MARITAL_STATUS -> profile.maritalStatus
        ProfileField.CONTACT_EMAIL -> profile.contactEmail
        ProfileField.PERMANENT_ADDRESS -> profile.permanentAddress
        ProfileField.BIO -> profile.bio
    }
    val choices = when (step.field) {
        ProfileField.DEPARTMENT -> RoutineRules.ALL_TEACHER_DEPARTMENTS
        ProfileField.SEMESTER -> RoutineRules.ALL_SEMESTER_KEYS
        ProfileField.SHIFT -> listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)
        ProfileField.SECTION -> RoutineRules.groupsForDepartment(profile.department)
        ProfileField.BLOOD_GROUP -> RoutineRules.BLOOD_GROUPS
        ProfileField.GENDER -> listOf("পুরুষ / Male", "নারী / Female", "অন্যান্য / Other")
        ProfileField.MARITAL_STATUS -> listOf("অবিবাহিত / Single", "বিবাহিত / Married", "অন্যান্য / Other")
        else -> emptyList()
    }

    fun updateValue(newValue: String) {
        profile = when (step.field) {
            ProfileField.NAME -> profile.copy(name = newValue)
            ProfileField.DEPARTMENT -> profile.copy(department = newValue, section = "")
            ProfileField.SEMESTER -> profile.copy(semester = newValue)
            ProfileField.SHIFT -> profile.copy(shift = newValue)
            ProfileField.SECTION -> profile.copy(section = newValue)
            ProfileField.PHONE -> profile.copy(phone = newValue)
            ProfileField.BLOOD_GROUP -> profile.copy(bloodGroup = newValue)
            ProfileField.ADDRESS -> profile.copy(address = newValue)
            ProfileField.ROLL_NUMBER -> profile.copy(rollNumber = newValue)
            ProfileField.REGISTRATION_NUMBER -> profile.copy(registrationNumber = newValue)
            ProfileField.FATHER_NAME -> profile.copy(fatherName = newValue)
            ProfileField.MOTHER_NAME -> profile.copy(motherName = newValue)
            ProfileField.DOB -> profile.copy(dob = newValue)
            ProfileField.RELIGION -> profile.copy(religion = newValue)
            ProfileField.GENDER -> profile.copy(gender = newValue)
            ProfileField.MARITAL_STATUS -> profile.copy(maritalStatus = newValue)
            ProfileField.CONTACT_EMAIL -> profile.copy(contactEmail = newValue)
            ProfileField.PERMANENT_ADDRESS -> profile.copy(permanentAddress = newValue)
            ProfileField.BIO -> profile.copy(bio = newValue)
        }
        errorMessage = null
    }

    AppBackHandler(enabled = stepIndex > 0) {
        stepIndex = (stepIndex - 1).coerceAtLeast(0)
        errorMessage = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "প্রোফাইল তৈরি",
                color = cs.onSurface,
                fontFamily = hindSiliguri,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${stepIndex + 1} / ${steps.size}",
                color = cs.onSurfaceVariant,
                fontFamily = hindSiliguri,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { (stepIndex + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = cs.primary,
            trackColor = cs.surfaceVariant,
        )

        AnimatedContent(
            targetState = stepIndex,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            label = "profile_setup_step",
        ) { visibleIndex ->
            val visibleStep = steps[visibleIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 42.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Surface(
                    modifier = Modifier.padding(bottom = 24.dp),
                    shape = CircleShape,
                    color = cs.primaryContainer,
                ) {
                    Text(
                        text = (visibleIndex + 1).toString().padStart(2, '0'),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = cs.onPrimaryContainer,
                        fontFamily = hindSiliguri,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = visibleStep.title,
                    color = cs.onSurface,
                    fontFamily = hindSiliguri,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = visibleStep.hint,
                    color = cs.onSurfaceVariant,
                    fontFamily = hindSiliguri,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(30.dp))

                if (choices.isNotEmpty()) {
                    choices.forEach { choice ->
                        val selected = value == choice
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable {
                                    updateValue(choice)
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) cs.primaryContainer else cs.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) cs.primary else cs.outlineVariant,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = choice,
                                    modifier = Modifier.weight(1f),
                                    color = if (selected) cs.onPrimaryContainer else cs.onSurface,
                                    fontFamily = hindSiliguri,
                                    fontSize = 16.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                if (selected) {
                                    Text("✓", color = cs.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = value,
                        onValueChange = ::updateValue,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(visibleStep.hint, fontFamily = hindSiliguri) },
                        singleLine = !visibleStep.multiline,
                        minLines = if (visibleStep.multiline) 3 else 1,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = when (visibleStep.field) {
                                ProfileField.PHONE -> KeyboardType.Phone
                                ProfileField.CONTACT_EMAIL -> KeyboardType.Email
                                else -> KeyboardType.Text
                            },
                        ),
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                color = cs.error,
                fontFamily = hindSiliguri,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (stepIndex > 0) {
                Button(
                    onClick = {
                        stepIndex--
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.surfaceVariant,
                        contentColor = cs.onSurfaceVariant,
                    ),
                    enabled = !isSaving,
                ) {
                    Text("পেছনে", fontFamily = hindSiliguri, fontSize = 16.sp)
                }
            }
            Button(
                onClick = {
                    if (step.required && value.isBlank()) {
                        errorMessage = "এই তথ্যটি পূরণ করুন"
                    } else if (stepIndex < steps.lastIndex) {
                        stepIndex++
                        errorMessage = null
                    } else if (!profile.contactEmail.isBlank() &&
                        (!profile.contactEmail.contains("@") || !profile.contactEmail.contains("."))
                    ) {
                        errorMessage = "সঠিক ইমেইল ঠিকানা লিখুন"
                    } else {
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            val completedProfile = profile.copy(
                                name = profile.name.trim(),
                                email = profile.email.trim().lowercase(),
                                profileSetupComplete = true,
                            )
                            val result = runCatching { onSave(completedProfile) }
                                .getOrElse { Result.failure(it) }
                            isSaving = false
                            if (result.isSuccess) {
                                onComplete(completedProfile)
                            } else {
                                errorMessage = result.exceptionOrNull()?.localizedMessage
                                    ?: "প্রোফাইল সংরক্ষণ করা যায়নি। আবার চেষ্টা করুন।"
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving,
            ) {
                Text(
                    text = if (isSaving) "সংরক্ষণ হচ্ছে…" else if (stepIndex == steps.lastIndex) "সম্পন্ন" else "পরবর্তী",
                    fontFamily = hindSiliguri,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (!step.required && stepIndex < steps.lastIndex) {
            Text(
                text = "এখন বাদ দিন",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving) {
                        stepIndex++
                        errorMessage = null
                    }
                    .padding(vertical = 12.dp),
                color = cs.onSurfaceVariant,
                fontFamily = hindSiliguri,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
