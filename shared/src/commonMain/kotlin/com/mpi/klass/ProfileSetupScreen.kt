package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.RightArrowIcon
import kotlinx.coroutines.launch

internal fun profileSetupSteps(role: UserRole): List<ProfileSetupStep> = buildList {
    val fields = buildList {
        add(ProfileField.NAME)
        add(ProfileField.DEPARTMENT)
        if (role == UserRole.STUDENT) {
            add(ProfileField.SEMESTER)
        }
        add(ProfileField.SHIFT)
        if (role == UserRole.STUDENT) {
            add(ProfileField.SECTION)
            add(ProfileField.ROLL_NUMBER)
            add(ProfileField.REGISTRATION_NUMBER)
        }
        add(ProfileField.PHONE)
        add(ProfileField.BLOOD_GROUP)
        add(ProfileField.ADDRESS)
        add(ProfileField.DOB)
        add(ProfileField.RELIGION)
        add(ProfileField.GENDER)
        add(ProfileField.MARITAL_STATUS)
        add(ProfileField.CONTACT_EMAIL)
        add(ProfileField.PERMANENT_ADDRESS)
        add(ProfileField.BIO)
    }
    fields.forEach { field ->
        val (title, hint, _) = profileFieldMeta(field, role, isDark = false)
        add(
            ProfileSetupStep(
                field = field,
                title = title,
                hint = hint,
                required = true,
                multiline = isProfileFieldMultiline(field),
            ),
        )
    }
}

@Composable
fun ProfileSetupScreen(
    role: UserRole,
    initialProfile: StudentProfile,
    hindSiliguri: FontFamily,
    isDark: Boolean = isSystemInDarkTheme(),
    onSave: suspend (StudentProfile) -> Result<Unit>,
    onComplete: (StudentProfile) -> Unit,
) {
    val steps = remember(role) { profileSetupSteps(role) }
    var profile by remember(initialProfile.email) { mutableStateOf(initialProfile) }
    var stepIndex by remember(initialProfile.email) { mutableStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme
    val currentAppLanguage = LocalAppLanguage.current
    val isBengali = currentAppLanguage == AppLanguage.BENGALI

    val step = steps[stepIndex]
    val stepMeta = profileFieldMeta(step.field, role, isDark)
    val stepIcon = stepMeta.third.first
    val stepAccent = stepMeta.third.second

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
        ProfileField.DOB -> profile.dob
        ProfileField.RELIGION -> profile.religion
        ProfileField.GENDER -> profile.gender
        ProfileField.MARITAL_STATUS -> profile.maritalStatus
        ProfileField.CONTACT_EMAIL -> profile.contactEmail
        ProfileField.PERMANENT_ADDRESS -> profile.permanentAddress
        ProfileField.BIO -> profile.bio
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

    val gpsSyncLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank()) {
            profile = profile.copy(address = realAddress)
            errorMessage = null
        } else {
            showToast(AppStrings.translate("লোকেশন পাওয়া যায়নি। অনুগ্রহ করে ঠিকানা লিখুন।", currentAppLanguage))
        }
    }

    AppBackHandler(enabled = stepIndex > 0 && !isSaving) {
        stepIndex = (stepIndex - 1).coerceAtLeast(0)
        errorMessage = null
    }

    MinimalBackdrop(
        isDark = isDark,
        focusAccent = stepAccent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .widthIn(max = 680.dp)
                .align(Alignment.TopCenter)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusManager.clearFocus(force = true) }
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "প্রোফাইল তৈরি".localized(),
                    color = cs.onSurface,
                    fontFamily = hindSiliguri,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    shape = CircleShape,
                    color = stepAccent.copy(alpha = if (isDark) 0.20f else 0.12f),
                    border = BorderStroke(0.75.dp, stepAccent.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = "${stepIndex + 1} / ${steps.size}",
                        color = stepAccent,
                        fontFamily = hindSiliguri,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (stepIndex + 1).toFloat() / steps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = stepAccent,
                trackColor = cs.surfaceVariant.copy(alpha = 0.65f),
            )

            AnimatedContent(
                targetState = stepIndex,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "profile_setup_step",
            ) { visibleIndex ->
                val visibleStep = steps[visibleIndex]
                val visMeta = profileFieldMeta(visibleStep.field, role, isDark)
                val visIcon = visMeta.third.first
                val visAccent = visMeta.third.second
                val visibleValue = when (visibleStep.field) {
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
                    ProfileField.DOB -> profile.dob
                    ProfileField.RELIGION -> profile.religion
                    ProfileField.GENDER -> profile.gender
                    ProfileField.MARITAL_STATUS -> profile.maritalStatus
                    ProfileField.CONTACT_EMAIL -> profile.contactEmail
                    ProfileField.PERMANENT_ADDRESS -> profile.permanentAddress
                    ProfileField.BIO -> profile.bio
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 28.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Surface(
                        modifier = Modifier.padding(bottom = 18.dp),
                        shape = CircleShape,
                        color = visAccent.copy(alpha = if (isDark) 0.22f else 0.13f),
                        border = BorderStroke(0.75.dp, visAccent.copy(alpha = 0.36f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = visIcon,
                                contentDescription = null,
                                tint = visAccent,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = (visibleIndex + 1).toString().padStart(2, '0'),
                                color = visAccent,
                                fontFamily = hindSiliguri,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Text(
                        text = visibleStep.title.localized(),
                        color = cs.onSurface,
                        fontFamily = hindSiliguri,
                        fontSize = 27.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = visibleStep.hint.localized(),
                        color = cs.onSurfaceVariant,
                        fontFamily = hindSiliguri,
                        fontSize = 15.sp,
                    )
                    Spacer(Modifier.height(24.dp))

                    ProfileFieldInputBody(
                        field = visibleStep.field,
                        value = visibleValue,
                        hint = visibleStep.hint,
                        role = role,
                        department = profile.department,
                        icon = visIcon,
                        accent = visAccent,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        onValueChange = ::updateValue,
                        onGpsClick = if (visibleStep.field == ProfileField.ADDRESS) {
                            { gpsSyncLauncher() }
                        } else null,
                        inputTestTag = "profile_setup_input_${visibleStep.field.name.lowercase()}",
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    color = cs.error,
                    fontFamily = hindSiliguri,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (stepIndex > 0) {
                    KlassPrimaryButton(
                        text = "পেছনে".localized(),
                        isPrimaryAccent = false,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        enabled = !isSaving,
                        height = 52.dp,
                        cornerRadius = 16.dp,
                        testTag = "profile_setup_back_button",
                        modifier = Modifier.weight(0.85f),
                        onClick = {
                            focusManager.clearFocus(force = true)
                            stepIndex--
                            errorMessage = null
                        },
                    )
                }

                KlassPrimaryButton(
                    text = when {
                        isSaving -> "সংরক্ষণ হচ্ছে…".localized()
                        stepIndex == steps.lastIndex -> "সম্পন্ন".localized()
                        else -> "পরবর্তী".localized()
                    },
                    icon = if (stepIndex == steps.lastIndex) CheckIcon else RightArrowIcon,
                    customPrimaryColor = stepAccent,
                    isDark = isDark,
                    isLoading = isSaving,
                    enabled = !isSaving,
                    hindSiliguri = hindSiliguri,
                    height = 52.dp,
                    cornerRadius = 16.dp,
                    testTag = if (stepIndex == steps.lastIndex) "profile_setup_finish_button" else "profile_setup_next_button",
                    modifier = Modifier.weight(1.25f),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        val validationError = validateProfileField(step.field, value, isBengali)
                        if (validationError != null) {
                            errorMessage = validationError
                            return@KlassPrimaryButton
                        }
                        if (stepIndex < steps.lastIndex) {
                            stepIndex++
                            errorMessage = null
                        } else {
                            scope.launch {
                                isSaving = true
                                errorMessage = null
                                val completedProfile = profile.copy(
                                    name = profile.name.trim(),
                                    email = profile.email.trim().lowercase(),
                                    bio = profile.bio.trim(),
                                    phone = profile.phone.trim(),
                                    contactEmail = profile.contactEmail.trim(),
                                    address = profile.address.trim(),
                                    permanentAddress = profile.permanentAddress.trim(),
                                    rollNumber = profile.rollNumber.trim(),
                                    registrationNumber = profile.registrationNumber.trim(),
                                    fatherName = "",
                                    motherName = "",
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
                )
            }
        }
    }
}
