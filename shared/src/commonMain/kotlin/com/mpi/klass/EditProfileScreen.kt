package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CameraIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.HandWaveIcon
import com.mpi.klass.theme.HomeIcon
import com.mpi.klass.theme.IdCardIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LanguageGlobeIcon
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.LockIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.PrayingHandsIcon
import com.mpi.klass.theme.RingIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.SolidUserIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserTieIcon
import com.mpi.klass.theme.UsersGroupIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.bg_menu_profile
import org.jetbrains.compose.resources.painterResource

internal fun formatProfileDob(rawDob: String, isBengali: Boolean): String {
    val clean = rawDob.trim()
    if (clean.isBlank()) return ""
    val parts = clean.split("-")
    if (parts.size != 3) return clean
    val year = parts[0].toIntOrNull() ?: return clean
    val month = parts[1].toIntOrNull() ?: return clean
    val day = parts[2].toIntOrNull() ?: return clean

    val bnMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর",
    )
    val enMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
    val monthIdx = (month - 1).coerceIn(0, 11)
    return if (isBengali) {
        "${toBengaliDigits(day.toString())} ${bnMonths[monthIdx]}, ${toBengaliDigits(year.toString())}"
    } else {
        "$day ${enMonths[monthIdx]}, $year"
    }
}

private fun toBengaliDigits(input: String): String {
    val map = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(input.length)
    for (c in input) {
        if (c in '0'..'9') sb.append(map[c - '0']) else sb.append(c)
    }
    return sb.toString()
}

private fun normalizeSemesterOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    val num = clean.filter { it.isDigit() }
    return when (num) {
        "1" -> "১ম সেমিস্টার"
        "2" -> "২য় সেমিস্টার"
        "3" -> "৩য় সেমিস্টার"
        "4" -> "৪র্থ সেমিস্টার"
        "5" -> "৫ম সেমিস্টার"
        "6" -> "৬ষ্ঠ সেমিস্টার"
        "7" -> "৭ম সেমিস্টার"
        "8" -> ""
        else -> when {
            clean.contains("১ম") || clean.startsWith("1", true) -> "১ম সেমিস্টার"
            clean.contains("২য়") || clean.contains("২য়") || clean.startsWith("2", true) -> "২য় সেমিস্টার"
            clean.contains("৩য়") || clean.contains("৩য়") || clean.startsWith("3", true) -> "৩য় সেমিস্টার"
            clean.contains("৪র্থ") || clean.startsWith("4", true) -> "৪র্থ সেমিস্টার"
            clean.contains("৫ম") || clean.startsWith("5", true) -> "৫ম সেমিস্টার"
            clean.contains("৬ষ্ঠ") || clean.startsWith("6", true) -> "৬ষ্ঠ সেমিস্টার"
            clean.contains("৭ম") || clean.startsWith("7", true) -> "৭ম সেমিস্টার"
            clean.contains("৮ম") || clean.startsWith("8", true) -> ""
            else -> clean
        }
    }
}

private fun normalizeDepartmentOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean.equals("CST", true) || clean.startsWith("CST ", true) -> "CST (Computer Science & Technology)"
        clean.equals("CT", true) || clean.startsWith("CT ", true) -> "CT (Civil Technology)"
        clean.equals("ET", true) || clean.startsWith("ET ", true) -> "ET (Electrical Technology)"
        clean.equals("MT", true) || clean.startsWith("MT ", true) -> "MT (Mechanical Technology)"
        clean.equals("PT", true) || clean.startsWith("PT ", true) -> "PT (Power Technology)"
        clean.equals("ENT", true) || clean.startsWith("ENT ", true) -> "ENT (Electronics Technology)"
        clean.equals("EMT", true) || clean.startsWith("EMT ", true) -> "EMT (Electro-Medical Technology)"
        clean.contains("নন টেক", ignoreCase = true) || clean.contains("নন-টেক", ignoreCase = true) || clean.equals("Non-Tech", true) || clean.equals("Non Tech", true) -> "নন টেক"
        else -> clean
    }
}

private fun normalizeShiftOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean == "1" || clean.contains("1st", true) || clean.contains("১ম") || clean.contains("Morning", true) -> "১ম শিফট"
        clean == "2" || clean.contains("2nd", true) || clean.contains("২য়") || clean.contains("২য়") || clean.contains("Evening", true) -> "২য় শিফট"
        else -> clean
    }
}

private fun normalizeSectionOption(raw: String, dept: String = ""): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return RoutineRules.normalizeGroup(dept, clean)
}

private data class EditPickerRequest(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val options: List<String>,
    val selected: String,
    val translateOptions: Boolean,
    val onSelect: (String) -> Unit,
)

private data class PrivacyPickerRequest(
    val fieldKey: String,
    val fieldLabel: String,
    val currentPrivacy: ProfilePrivacy,
)

private data class EditTextFieldRequest(
    val fieldKey: String,
    val title: String,
    val placeholder: String,
    val initialValue: String,
    val icon: ImageVector,
    val singleLine: Boolean = true,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val initialPrivacy: ProfilePrivacy = ProfilePrivacy.PUBLIC,
    val supportsGps: Boolean = false,
    val testTag: String = "edit_single_field_input",
    val onSave: (String, ProfilePrivacy) -> Unit,
)

internal fun defaultProfileFieldPrivacy(fieldKey: String, role: UserRole): ProfilePrivacy = when (fieldKey) {
    "bio", "name", "department", "semester", "shift", "section", "blood_group" -> ProfilePrivacy.PUBLIC
    "phone", "address", "contact_email" -> if (role == UserRole.TEACHER) ProfilePrivacy.PUBLIC else ProfilePrivacy.CONNECTIONS
    else -> ProfilePrivacy.PRIVATE
}

@Composable
fun EditProfileContent(
    initialProfile: StudentProfile,
    photoUrl: String?,
    coverPhotoUrl: String? = null,
    fallbackName: String,
    fallbackEmail: String,
    role: UserRole = UserRole.STUDENT,
    hindSiliguri: FontFamily,
    topContentPadding: Dp,
    isSaving: Boolean,
    isDark: Boolean = false,
    hasLoginPassword: Boolean = false,
    onOverlayStateChange: (Boolean) -> Unit = {},
    onAvatarClick: () -> Unit,
    onCoverPhotoClick: () -> Unit = {},
    onPasswordSetupClick: () -> Unit = {},
    onOpenTeachingPrefs: (() -> Unit)? = null,
    onViewProfileClick: () -> Unit,
    onSaveProfile: (StudentProfile) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val currentAppLanguage = LocalAppLanguage.current
    val focusManager = LocalFocusManager.current
    val profileAccent = KlassFeaturePalette.profile(isDark)
    val academicAccent = KlassFeaturePalette.routine(isDark)
    val bloodAccent = KlassFeaturePalette.blood(isDark)

    val sheetBg = if (isDark) Color(0xFF0B1120) else Color.White

    var activePicker by remember { mutableStateOf<EditPickerRequest?>(null) }
    var activePrivacyPicker by remember { mutableStateOf<PrivacyPickerRequest?>(null) }
    var activeTextEditor by remember { mutableStateOf<EditTextFieldRequest?>(null) }

    LaunchedEffect(activePicker, activePrivacyPicker, activeTextEditor) {
        onOverlayStateChange(activePicker != null || activePrivacyPicker != null || activeTextEditor != null)
    }

    var hasUserEdited by remember(fallbackEmail) { mutableStateOf(false) }
    var fullName by remember(fallbackEmail) { mutableStateOf(initialProfile.name.ifBlank { fallbackName }) }
    var bio by remember(fallbackEmail) { mutableStateOf(initialProfile.bio) }
    var fatherName by remember(fallbackEmail) { mutableStateOf(initialProfile.fatherName) }
    var motherName by remember(fallbackEmail) { mutableStateOf(initialProfile.motherName) }
    var dob by remember(fallbackEmail) { mutableStateOf(initialProfile.dob) }
    var bloodGroup by remember(fallbackEmail) { mutableStateOf(initialProfile.bloodGroup) }
    var religion by remember(fallbackEmail) { mutableStateOf(initialProfile.religion) }
    var gender by remember(fallbackEmail) { mutableStateOf(initialProfile.gender) }
    var maritalStatus by remember(fallbackEmail) { mutableStateOf(initialProfile.maritalStatus.ifBlank { "অবিবাহিত (Unmarried)" }) }
    var phone by remember(fallbackEmail) { mutableStateOf(initialProfile.phone) }
    var contactEmail by remember(fallbackEmail) { mutableStateOf(initialProfile.contactEmail.ifBlank { fallbackEmail }) }
    var currentAddress by remember(fallbackEmail) { mutableStateOf(initialProfile.address) }
    var permanentAddress by remember(fallbackEmail) { mutableStateOf(initialProfile.permanentAddress) }

    var department by remember(fallbackEmail) { mutableStateOf(normalizeDepartmentOption(initialProfile.department)) }
    var semester by remember(fallbackEmail) { mutableStateOf(normalizeSemesterOption(initialProfile.semester)) }
    var shift by remember(fallbackEmail) { mutableStateOf(normalizeShiftOption(initialProfile.shift)) }
    var section by remember(fallbackEmail) {
        val normDept = normalizeDepartmentOption(initialProfile.department)
        mutableStateOf(normalizeSectionOption(initialProfile.section, normDept))
    }
    var rollNumber by remember(fallbackEmail) { mutableStateOf(initialProfile.rollNumber) }
    var registrationNumber by remember(fallbackEmail) { mutableStateOf(initialProfile.registrationNumber) }
    var fieldPrivacyMap by remember(fallbackEmail) { mutableStateOf(initialProfile.fieldPrivacy) }

    fun privacyForField(fieldKey: String): ProfilePrivacy =
        fieldPrivacyMap[fieldKey] ?: defaultProfileFieldPrivacy(fieldKey, role)

    LaunchedEffect(initialProfile) {
        if (!hasUserEdited) {
            fullName = initialProfile.name.ifBlank { fallbackName }
            bio = initialProfile.bio
            fatherName = initialProfile.fatherName
            motherName = initialProfile.motherName
            dob = initialProfile.dob
            bloodGroup = initialProfile.bloodGroup
            religion = initialProfile.religion
            gender = initialProfile.gender
            maritalStatus = initialProfile.maritalStatus.ifBlank { "অবিবাহিত (Unmarried)" }
            phone = initialProfile.phone
            contactEmail = initialProfile.contactEmail.ifBlank { fallbackEmail }
            currentAddress = initialProfile.address
            permanentAddress = initialProfile.permanentAddress
            val normDept = normalizeDepartmentOption(initialProfile.department)
            department = normDept
            semester = normalizeSemesterOption(initialProfile.semester)
            shift = normalizeShiftOption(initialProfile.shift)
            section = normalizeSectionOption(initialProfile.section, normDept)
            rollNumber = initialProfile.rollNumber
            registrationNumber = initialProfile.registrationNumber
            fieldPrivacyMap = initialProfile.fieldPrivacy
        }
    }

    fun persistCurrentProfile(privacyOverride: Map<String, ProfilePrivacy> = fieldPrivacyMap) {
        val finalName = fullName.trim().ifBlank { fallbackName }
        val updated = initialProfile.copy(
            email = initialProfile.email.ifBlank { fallbackEmail }.trim().lowercase(),
            name = finalName,
            bio = bio.trim(),
            fatherName = fatherName.trim(),
            motherName = motherName.trim(),
            dob = dob.trim(),
            bloodGroup = bloodGroup.trim(),
            religion = religion.trim(),
            gender = gender.trim(),
            maritalStatus = maritalStatus.trim(),
            phone = phone.trim(),
            contactEmail = contactEmail.trim(),
            address = currentAddress.trim(),
            permanentAddress = permanentAddress.trim(),
            department = department.trim(),
            semester = semester.trim(),
            shift = shift.trim(),
            section = section.trim(),
            rollNumber = rollNumber.trim(),
            registrationNumber = registrationNumber.trim(),
            profileSetupComplete = true,
            fieldPrivacy = privacyOverride,
        )
        onSaveProfile(updated)
    }

    fun updateFieldPrivacy(fieldKey: String, privacy: ProfilePrivacy) {
        hasUserEdited = true
        val next = fieldPrivacyMap.toMutableMap().apply { put(fieldKey, privacy) }
        fieldPrivacyMap = next
        persistCurrentProfile(next)
    }

    val launchDatePicker = rememberDatePickerLauncher(initialDate = dob) { picked ->
        hasUserEdited = true
        dob = picked
        persistCurrentProfile()
    }

    val gpsSyncLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank()) {
            hasUserEdited = true
            currentAddress = realAddress
            activeTextEditor = activeTextEditor?.copy(initialValue = realAddress)
            persistCurrentProfile()
        } else {
            showToast(AppStrings.translate("লোকেশন পাওয়া যায়নি। অনুগ্রহ করে ঠিকানা লিখুন।", currentAppLanguage))
        }
    }

    val bloodOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    val religionOptions = listOf("ইসলাম", "হিন্দুধর্ম", "বৌদ্ধধর্ম", "খ্রিস্টধর্ম", "অন্যান্য")
    val genderOptions = listOf("পুরুষ (Male)", "মহিলা (Female)", "অন্যান্য (Other)")
    val maritalOptions = listOf("অবিবাহিত (Unmarried)", "বিবাহিত (Married)")
    val baseDeptOptions = listOf(
        "CST (Computer Science & Technology)",
        "CT (Civil Technology)",
        "ET (Electrical Technology)",
        "MT (Mechanical Technology)",
        "PT (Power Technology)",
        "ENT (Electronics Technology)",
        "EMT (Electro-Medical Technology)",
    )
    val deptOptions = if (role == UserRole.TEACHER) baseDeptOptions + "নন টেক" else baseDeptOptions
    val semesterOptions = listOf(
        "১ম সেমিস্টার", "২য় সেমিস্টার", "৩য় সেমিস্টার", "৪র্থ সেমিস্টার",
        "৫ম সেমিস্টার", "৬ষ্ঠ সেমিস্টার", "৭ম সেমিস্টার",
    )
    val shiftOptions = listOf("১ম শিফট", "২য় শিফট")
    val sectionOptions = remember(department) { RoutineRules.groupsForDepartment(department) }

    var introExpanded by remember { mutableStateOf(true) }
    var categoryExpanded by remember { mutableStateOf(true) }
    var personalExpanded by remember { mutableStateOf(true) }
    var securityExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sheetBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            // ── TRANSPARENT TOP-TO-BOTTOM FADING HEADER ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                sheetBg.copy(alpha = 0.88f),
                                sheetBg.copy(alpha = 0.42f),
                                Color.Transparent,
                            )
                        )
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(54.dp)
                        .padding(horizontal = 4.dp),
                ) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onViewProfileClick()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(48.dp)
                            .testTag("edit_to_view_profile_button"),
                    ) {
                        Icon(
                            imageVector = BackArrowIcon,
                            contentDescription = "Back",
                            tint = cs.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Text(
                        text = "প্রোফাইল এডিট".localized(),
                        color = cs.onSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    if (isSaving) {
                        Surface(
                            shape = CircleShape,
                            color = profileAccent.softTint,
                            border = BorderStroke(0.75.dp, profileAccent.borderTint),
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp),
                        ) {
                            Text(
                                text = "Saving...".localized(),
                                color = profileAccent.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // ── SCROLLABLE FACEBOOK EDIT PROFILE BODY ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 780.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 36.dp),
                ) {
                    if (topContentPadding > 0.dp) {
                        Spacer(modifier = Modifier.height(topContentPadding))
                    }

                    // ── FACEBOOK HERO: EDGE-TO-EDGE COVER + ROUNDED WHITE SHOULDER + CENTERED AVATAR + WHITE CAMERA BUTTONS ──
                    val coverHeight = 208.dp
                    val shoulderOverlap = 24.dp
                    val avatarSize = 140.dp
                    val heroTotalHeight = 268.dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heroTotalHeight),
                    ) {
                        // Cover Photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(coverHeight)
                                .align(Alignment.TopCenter)
                                .clickable { onCoverPhotoClick() },
                        ) {
                            val resolvedCover = resolveProfileImageModel(coverPhotoUrl)
                            if (resolvedCover != null) {
                                AsyncImage(
                                    model = resolvedCover,
                                    contentDescription = "কভার ফটো".localized(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Image(
                                    painter = painterResource(Res.drawable.bg_menu_profile),
                                    contentDescription = "কভার ফটো".localized(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            // Crisp white circular Camera icon on Cover right (above curved shoulder)
                            Surface(
                                onClick = onCoverPhotoClick,
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(0.75.dp, Color(0xFFD1D5DB)),
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = shoulderOverlap + 12.dp)
                                    .size(38.dp)
                                    .testTag("edit_cover_photo_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = CameraIcon,
                                        contentDescription = "কভার ফটো".localized(),
                                        tint = Color(0xFF111827),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // Rounded Top Shoulder Sheet rising over Cover bottom
                        Surface(
                            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                            color = sheetBg,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heroTotalHeight - (coverHeight - shoulderOverlap))
                                .align(Alignment.BottomCenter),
                        ) {}

                        // Centered Circular Avatar straddling the curved Cover/Sheet seam
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .size(avatarSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                onClick = onAvatarClick,
                                shape = CircleShape,
                                color = cs.surfaceContainerHigh,
                                border = BorderStroke(4.dp, sheetBg),
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("profile_avatar_button"),
                            ) {
                                val resolvedModel = resolveProfileImageModel(photoUrl)
                                if (resolvedModel != null) {
                                    AsyncImage(
                                        model = resolvedModel,
                                        contentDescription = "গ্যালারি থেকে ছবি আপলোড".localized(),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                    )
                                } else {
                                    DynamicAvatar(
                                        seed = fallbackEmail.ifBlank { fullName },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                    )
                                }
                            }

                            // Crisp white circular Camera icon overlapping bottom-right of Avatar
                            Surface(
                                onClick = onAvatarClick,
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(0.75.dp, Color(0xFFD1D5DB)),
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = CameraIcon,
                                        contentDescription = "Change photo",
                                        tint = Color(0xFF111827),
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }
                        }
                    }

                    // ── SECTION 1: INTRO ──
                    FbAccordionSection(
                        title = "পরিচিতি".localized(),
                        expanded = introExpanded,
                        onToggle = { introExpanded = !introExpanded },
                        hindSiliguri = hindSiliguri,
                    ) {
                        val bioTitle = "বায়ো".localized()
                        val bioPlaceholder = "নিজের সম্পর্কে সংক্ষেপে লিখুন...".localized()
                        val bioPrivacy = privacyForField("bio")
                        FbEditDetailRow(
                            icon = HandWaveIcon,
                            title = bioTitle,
                            value = bio.ifBlank { bioPlaceholder },
                            isPlaceholder = bio.isBlank(),
                            privacy = bioPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("bio", bioTitle, bioPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_bio_row",
                            onClick = {
                                activeTextEditor = EditTextFieldRequest(
                                    fieldKey = "bio",
                                    title = bioTitle,
                                    placeholder = bioPlaceholder,
                                    initialValue = bio,
                                    icon = HandWaveIcon,
                                    singleLine = false,
                                    initialPrivacy = bioPrivacy,
                                    testTag = "edit_bio_input",
                                    onSave = { newVal, chosenPrivacy ->
                                        hasUserEdited = true
                                        bio = newVal
                                        val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("bio", chosenPrivacy) }
                                        fieldPrivacyMap = nextPriv
                                        persistCurrentProfile(nextPriv)
                                    },
                                )
                            },
                        )

                        val nameTitle = "পূর্ণ নাম".localized()
                        val namePlaceholder = "আপনার পুরো নাম লিখুন".localized()
                        FbEditDetailRow(
                            icon = SolidUserIcon,
                            title = nameTitle,
                            value = fullName.ifBlank { fallbackName },
                            isPlaceholder = fullName.isBlank(),
                            privacy = null,
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_fullname_row",
                            onClick = {
                                activeTextEditor = EditTextFieldRequest(
                                    fieldKey = "name",
                                    title = nameTitle,
                                    placeholder = namePlaceholder,
                                    initialValue = fullName,
                                    icon = SolidUserIcon,
                                    singleLine = true,
                                    initialPrivacy = ProfilePrivacy.PUBLIC,
                                    testTag = "edit_fullname_input",
                                    onSave = { newVal, _ ->
                                        hasUserEdited = true
                                        fullName = newVal
                                        persistCurrentProfile()
                                    },
                                )
                            },
                        )
                    }

                    // ── SECTION 2: CATEGORY / ACADEMIC (Students & Teachers) ──
                    if (role != UserRole.ADMIN) {
                        FbAccordionSection(
                            title = "ক্যাটাগরি".localized(),
                            expanded = categoryExpanded,
                            onToggle = { categoryExpanded = !categoryExpanded },
                            hindSiliguri = hindSiliguri,
                        ) {
                            val deptTitle = "ডিপার্টমেন্ট (Department)".localized()
                            val notAddedText = "তথ্য যোগ করা হয়নি".localized()
                            val deptPrivacy = privacyForField("department")
                            FbEditDetailRow(
                                icon = GraduationCapIcon,
                                title = department.ifBlank { deptTitle },
                                value = if (department.isNotBlank()) deptTitle else notAddedText,
                                isPlaceholder = department.isBlank(),
                                privacy = deptPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("department", deptTitle, deptPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_department_select",
                                onClick = {
                                    activePicker = EditPickerRequest(
                                        title = deptTitle,
                                        subtitle = "আপনার টেকনোলজি বেছে নিন",
                                        icon = GraduationCapIcon,
                                        accent = academicAccent.primary,
                                        options = deptOptions,
                                        selected = department,
                                        translateOptions = false,
                                        onSelect = {
                                            hasUserEdited = true
                                            department = it
                                            if (section.isNotBlank()) {
                                                section = RoutineRules.normalizeGroup(it, section)
                                            }
                                            persistCurrentProfile()
                                        },
                                    )
                                },
                            )

                            if (role == UserRole.STUDENT) {
                                val semTitle = "সেমিস্টার".localized()
                                val semVal = if (semester.isNotBlank()) semester.localized() else ""
                                val semPrivacy = privacyForField("semester")
                                FbEditDetailRow(
                                    icon = LayerGroupIcon,
                                    title = semVal.ifBlank { semTitle },
                                    value = if (semVal.isNotBlank()) semTitle else notAddedText,
                                    isPlaceholder = semVal.isBlank(),
                                    privacy = semPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("semester", semTitle, semPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_semester_select",
                                    onClick = {
                                        activePicker = EditPickerRequest(
                                            title = semTitle,
                                            subtitle = "চলমান সেমিস্টার নির্বাচন করুন",
                                            icon = LayerGroupIcon,
                                            accent = academicAccent.primary,
                                            options = semesterOptions,
                                            selected = semester,
                                            translateOptions = true,
                                            onSelect = {
                                                hasUserEdited = true
                                                semester = it
                                                persistCurrentProfile()
                                            },
                                        )
                                    },
                                )
                            }

                            val shiftTitle = "শিফট".localized()
                            val shiftVal = if (shift.isNotBlank()) shift.localized() else ""
                            val shiftPrivacy = privacyForField("shift")
                            FbEditDetailRow(
                                icon = ClockIcon,
                                title = shiftVal.ifBlank { shiftTitle },
                                value = if (shiftVal.isNotBlank()) shiftTitle else notAddedText,
                                isPlaceholder = shiftVal.isBlank(),
                                privacy = shiftPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("shift", shiftTitle, shiftPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_shift_select",
                                onClick = {
                                    activePicker = EditPickerRequest(
                                        title = shiftTitle,
                                        subtitle = "১ম বা ২য় শিফট বেছে নিন",
                                        icon = ClockIcon,
                                        accent = academicAccent.primary,
                                        options = shiftOptions,
                                        selected = shift,
                                        translateOptions = true,
                                        onSelect = {
                                            hasUserEdited = true
                                            shift = it
                                            persistCurrentProfile()
                                        },
                                    )
                                },
                            )

                            if (role == UserRole.STUDENT) {
                                val secTitle = "সেকশন (Section)".localized()
                                val secPrivacy = privacyForField("section")
                                FbEditDetailRow(
                                    icon = UsersRectangleIcon,
                                    title = section.ifBlank { secTitle },
                                    value = if (section.isNotBlank()) secTitle else notAddedText,
                                    isPlaceholder = section.isBlank(),
                                    privacy = secPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("section", secTitle, secPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_section_select",
                                    onClick = {
                                        activePicker = EditPickerRequest(
                                            title = secTitle,
                                            subtitle = "ক্লাস সেকশন বা গ্রুপ বেছে নিন",
                                            icon = UsersRectangleIcon,
                                            accent = academicAccent.primary,
                                            options = sectionOptions,
                                            selected = section,
                                            translateOptions = false,
                                            onSelect = {
                                                hasUserEdited = true
                                                section = it
                                                persistCurrentProfile()
                                            },
                                        )
                                    },
                                )
                            }

                            if (role == UserRole.TEACHER && onOpenTeachingPrefs != null) {
                                FbEditDetailRow(
                                    icon = SparkleIcon,
                                    title = "শিক্ষণ বিষয়সমূহ".localized(),
                                    value = "নির্ধারিত কোর্স ও সময়সূচি".localized(),
                                    isPlaceholder = false,
                                    privacy = null,
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_teaching_prefs_row",
                                    onClick = onOpenTeachingPrefs,
                                )
                            }
                        }
                    }

                    // ── SECTION 3: PERSONAL DETAILS (Location, Home, Blood, Contact, Family) ──
                    FbAccordionSection(
                        title = "ব্যক্তিগত বিবরণ".localized(),
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded },
                        hindSiliguri = hindSiliguri,
                    ) {
                        val notAddedText = "তথ্য যোগ করা হয়নি".localized()

                        // Current address
                        val currAddrTitle = "বর্তমান ঠিকানা".localized()
                        val addrPlaceholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized()
                        val addrPrivacy = privacyForField("address")
                        FbEditDetailRow(
                            icon = LocationDotIcon,
                            title = currentAddress.ifBlank { currAddrTitle },
                            value = if (currentAddress.isNotBlank()) currAddrTitle else notAddedText,
                            isPlaceholder = currentAddress.isBlank(),
                            privacy = addrPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("address", currAddrTitle, addrPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_current_address_input",
                            onClick = {
                                activeTextEditor = EditTextFieldRequest(
                                    fieldKey = "address",
                                    title = currAddrTitle,
                                    placeholder = addrPlaceholder,
                                    initialValue = currentAddress,
                                    icon = LocationDotIcon,
                                    singleLine = false,
                                    initialPrivacy = addrPrivacy,
                                    supportsGps = true,
                                    testTag = "edit_current_address_field",
                                    onSave = { newVal, chosenPrivacy ->
                                        hasUserEdited = true
                                        currentAddress = newVal
                                        val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("address", chosenPrivacy) }
                                        fieldPrivacyMap = nextPriv
                                        persistCurrentProfile(nextPriv)
                                    },
                                )
                            },
                        )

                        // Permanent address
                        val permAddrTitle = "স্থায়ী ঠিকানা".localized()
                        val permPrivacy = privacyForField("permanent_address")
                        FbEditDetailRow(
                            icon = HomeIcon,
                            title = permanentAddress.ifBlank { permAddrTitle },
                            value = if (permanentAddress.isNotBlank()) permAddrTitle else notAddedText,
                            isPlaceholder = permanentAddress.isBlank(),
                            privacy = permPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("permanent_address", permAddrTitle, permPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_permanent_address_input",
                            onClick = {
                                activeTextEditor = EditTextFieldRequest(
                                    fieldKey = "permanent_address",
                                    title = permAddrTitle,
                                    placeholder = addrPlaceholder,
                                    initialValue = permanentAddress,
                                    icon = HomeIcon,
                                    singleLine = false,
                                    initialPrivacy = permPrivacy,
                                    testTag = "edit_permanent_address_field",
                                    onSave = { newVal, chosenPrivacy ->
                                        hasUserEdited = true
                                        permanentAddress = newVal
                                        val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("permanent_address", chosenPrivacy) }
                                        fieldPrivacyMap = nextPriv
                                        persistCurrentProfile(nextPriv)
                                    },
                                )
                            },
                        )

                        // Blood group
                        val bloodTitle = "রক্তের গ্রুপ".localized()
                        val bloodPrivacy = privacyForField("blood_group")
                        FbEditDetailRow(
                            icon = DropletIcon,
                            title = bloodGroup.ifBlank { bloodTitle },
                            value = if (bloodGroup.isNotBlank()) bloodTitle else notAddedText,
                            isPlaceholder = bloodGroup.isBlank(),
                            privacy = bloodPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("blood_group", bloodTitle, bloodPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_blood_group_select",
                            onClick = {
                                activePicker = EditPickerRequest(
                                    title = bloodTitle,
                                    subtitle = "রক্তের গ্রুপ বেছে নিন",
                                    icon = DropletIcon,
                                    accent = bloodAccent.primary,
                                    options = bloodOptions,
                                    selected = bloodGroup,
                                    translateOptions = false,
                                    onSelect = {
                                        hasUserEdited = true
                                        bloodGroup = it
                                        persistCurrentProfile()
                                    },
                                )
                            },
                        )

                        // Mobile phone
                        val phoneTitle = "মোবাইল নম্বর".localized()
                        val phonePrivacy = privacyForField("phone")
                        FbEditDetailRow(
                            icon = PhoneSolidIcon,
                            title = phone.ifBlank { phoneTitle },
                            value = if (phone.isNotBlank()) phoneTitle else notAddedText,
                            isPlaceholder = phone.isBlank(),
                            privacy = phonePrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("phone", phoneTitle, phonePrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_phone_input",
                            onClick = {
                                activeTextEditor = EditTextFieldRequest(
                                    fieldKey = "phone",
                                    title = phoneTitle,
                                    placeholder = "01XXXXXXXXX",
                                    initialValue = phone,
                                    icon = PhoneSolidIcon,
                                    singleLine = true,
                                    keyboardType = KeyboardType.Phone,
                                    initialPrivacy = phonePrivacy,
                                    testTag = "edit_phone_field",
                                    onSave = { newVal, chosenPrivacy ->
                                        hasUserEdited = true
                                        phone = newVal
                                        val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("phone", chosenPrivacy) }
                                        fieldPrivacyMap = nextPriv
                                        persistCurrentProfile(nextPriv)
                                    },
                                )
                            },
                        )

                        // Contact email
                        val emailTitle = "ইমেল ঠিকানা".localized()
                        val emailPrivacy = privacyForField("contact_email")
                        FbEditDetailRow(
                            icon = EnvelopeSolidIcon,
                            title = contactEmail.ifBlank { emailTitle },
                            value = if (contactEmail.isNotBlank()) emailTitle else notAddedText,
                            isPlaceholder = contactEmail.isBlank(),
                            privacy = emailPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("contact_email", emailTitle, emailPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_email_input",
                            onClick = {
                                activeTextEditor = EditTextFieldRequest(
                                    fieldKey = "contact_email",
                                    title = emailTitle,
                                    placeholder = "example@gmail.com",
                                    initialValue = contactEmail,
                                    icon = EnvelopeSolidIcon,
                                    singleLine = true,
                                    keyboardType = KeyboardType.Email,
                                    initialPrivacy = emailPrivacy,
                                    testTag = "edit_email_field",
                                    onSave = { newVal, chosenPrivacy ->
                                        hasUserEdited = true
                                        contactEmail = newVal
                                        val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("contact_email", chosenPrivacy) }
                                        fieldPrivacyMap = nextPriv
                                        persistCurrentProfile(nextPriv)
                                    },
                                )
                            },
                        )

                        // Date of birth
                        val dobTitle = "জন্ম তারিখ".localized()
                        val dobDisplay = if (dob.isNotBlank()) formatProfileDob(dob, isBengali) else ""
                        val dobPrivacy = privacyForField("dob")
                        FbEditDetailRow(
                            icon = CalendarDaysIcon,
                            title = dobDisplay.ifBlank { dobTitle },
                            value = if (dobDisplay.isNotBlank()) dobTitle else notAddedText,
                            isPlaceholder = dobDisplay.isBlank(),
                            privacy = dobPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("dob", dobTitle, dobPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_dob_picker",
                            onClick = { launchDatePicker() },
                        )

                        // Gender
                        val genTitle = "লিঙ্গ".localized()
                        val genVal = if (gender.isNotBlank()) gender.localized() else ""
                        val genderPrivacy = privacyForField("gender")
                        FbEditDetailRow(
                            icon = VenusMarsIcon,
                            title = genVal.ifBlank { genTitle },
                            value = if (genVal.isNotBlank()) genTitle else notAddedText,
                            isPlaceholder = genVal.isBlank(),
                            privacy = genderPrivacy,
                            onPrivacyClick = {
                                activePrivacyPicker = PrivacyPickerRequest("gender", genTitle, genderPrivacy)
                            },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_gender_select",
                            onClick = {
                                activePicker = EditPickerRequest(
                                    title = genTitle,
                                    subtitle = "একটি অপশন বাছাই করুন",
                                    icon = VenusMarsIcon,
                                    accent = KlassFeaturePalette.appearance(isDark).primary,
                                    options = genderOptions,
                                    selected = gender,
                                    translateOptions = true,
                                    onSelect = {
                                        hasUserEdited = true
                                        gender = it
                                        persistCurrentProfile()
                                    },
                                )
                            },
                        )

                        if (role != UserRole.ADMIN) {
                            val relTitle = "ধর্ম".localized()
                            val relVal = if (religion.isNotBlank()) religion.localized() else ""
                            val relPrivacy = privacyForField("religion")
                            FbEditDetailRow(
                                icon = PrayingHandsIcon,
                                title = relVal.ifBlank { relTitle },
                                value = if (relVal.isNotBlank()) relTitle else notAddedText,
                                isPlaceholder = relVal.isBlank(),
                                privacy = relPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("religion", relTitle, relPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_religion_select",
                                onClick = {
                                    activePicker = EditPickerRequest(
                                        title = relTitle,
                                        subtitle = "ধর্মীয় তথ্য নির্বাচন করুন",
                                        icon = PrayingHandsIcon,
                                        accent = profileAccent.primary,
                                        options = religionOptions,
                                        selected = religion,
                                        translateOptions = true,
                                        onSelect = {
                                            hasUserEdited = true
                                            religion = it
                                            persistCurrentProfile()
                                        },
                                    )
                                },
                            )

                            val marTitle = "বৈবাহিক অবস্থা".localized()
                            val marVal = if (maritalStatus.isNotBlank()) maritalStatus.localized() else ""
                            val marPrivacy = privacyForField("marital_status")
                            FbEditDetailRow(
                                icon = RingIcon,
                                title = marVal.ifBlank { marTitle },
                                value = if (marVal.isNotBlank()) marTitle else notAddedText,
                                isPlaceholder = marVal.isBlank(),
                                privacy = marPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("marital_status", marTitle, marPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_marital_select",
                                onClick = {
                                    activePicker = EditPickerRequest(
                                        title = marTitle,
                                        subtitle = "অবস্থা নির্বাচন করুন",
                                        icon = RingIcon,
                                        accent = profileAccent.primary,
                                        options = maritalOptions,
                                        selected = maritalStatus,
                                        translateOptions = true,
                                        onSelect = {
                                            hasUserEdited = true
                                            maritalStatus = it
                                            persistCurrentProfile()
                                        },
                                    )
                                },
                            )
                        }

                        if (role == UserRole.STUDENT) {
                            val rollTitle = "বোর্ড রোল নম্বর (Roll)".localized()
                            val rollPlaceholder = "যেমন: ৬৪৮১২৩".localized()
                            val rollPrivacy = privacyForField("roll_number")
                            FbEditDetailRow(
                                icon = IdCardIcon,
                                title = rollNumber.ifBlank { rollTitle },
                                value = if (rollNumber.isNotBlank()) rollTitle else notAddedText,
                                isPlaceholder = rollNumber.isBlank(),
                                privacy = rollPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("roll_number", rollTitle, rollPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_roll_input",
                                onClick = {
                                    activeTextEditor = EditTextFieldRequest(
                                        fieldKey = "roll_number",
                                        title = rollTitle,
                                        placeholder = rollPlaceholder,
                                        initialValue = rollNumber,
                                        icon = IdCardIcon,
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number,
                                        initialPrivacy = rollPrivacy,
                                        testTag = "edit_roll_field",
                                        onSave = { newVal, chosenPrivacy ->
                                            hasUserEdited = true
                                            rollNumber = newVal
                                            val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("roll_number", chosenPrivacy) }
                                            fieldPrivacyMap = nextPriv
                                            persistCurrentProfile(nextPriv)
                                        },
                                    )
                                },
                            )

                            val regTitle = "রেজিস্ট্রেশন নম্বর (Registration)".localized()
                            val regPlaceholder = "যেমন: ১৭৫৬৪২".localized()
                            val regPrivacy = privacyForField("registration_number")
                            FbEditDetailRow(
                                icon = FileLinesIcon,
                                title = registrationNumber.ifBlank { regTitle },
                                value = if (registrationNumber.isNotBlank()) regTitle else notAddedText,
                                isPlaceholder = registrationNumber.isBlank(),
                                privacy = regPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("registration_number", regTitle, regPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_registration_input",
                                onClick = {
                                    activeTextEditor = EditTextFieldRequest(
                                        fieldKey = "registration_number",
                                        title = regTitle,
                                        placeholder = regPlaceholder,
                                        initialValue = registrationNumber,
                                        icon = FileLinesIcon,
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number,
                                        initialPrivacy = regPrivacy,
                                        testTag = "edit_registration_field",
                                        onSave = { newVal, chosenPrivacy ->
                                            hasUserEdited = true
                                            registrationNumber = newVal
                                            val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("registration_number", chosenPrivacy) }
                                            fieldPrivacyMap = nextPriv
                                            persistCurrentProfile(nextPriv)
                                        },
                                    )
                                },
                            )

                            val fatherTitle = "পিতার নাম".localized()
                            val fatherPlaceholder = "পিতার নাম লিখুন".localized()
                            val fatherPrivacy = privacyForField("father_name")
                            FbEditDetailRow(
                                icon = UserTieIcon,
                                title = fatherName.ifBlank { fatherTitle },
                                value = if (fatherName.isNotBlank()) fatherTitle else notAddedText,
                                isPlaceholder = fatherName.isBlank(),
                                privacy = fatherPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("father_name", fatherTitle, fatherPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_father_name_input",
                                onClick = {
                                    activeTextEditor = EditTextFieldRequest(
                                        fieldKey = "father_name",
                                        title = fatherTitle,
                                        placeholder = fatherPlaceholder,
                                        initialValue = fatherName,
                                        icon = UserTieIcon,
                                        singleLine = true,
                                        initialPrivacy = fatherPrivacy,
                                        testTag = "edit_father_name_field",
                                        onSave = { newVal, chosenPrivacy ->
                                            hasUserEdited = true
                                            fatherName = newVal
                                            val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("father_name", chosenPrivacy) }
                                            fieldPrivacyMap = nextPriv
                                            persistCurrentProfile(nextPriv)
                                        },
                                    )
                                },
                            )

                            val motherTitle = "মাতার নাম".localized()
                            val motherPlaceholder = "মাতার নাম লিখুন".localized()
                            val motherPrivacy = privacyForField("mother_name")
                            FbEditDetailRow(
                                icon = UserNurseIcon,
                                title = motherName.ifBlank { motherTitle },
                                value = if (motherName.isNotBlank()) motherTitle else notAddedText,
                                isPlaceholder = motherName.isBlank(),
                                privacy = motherPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("mother_name", motherTitle, motherPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_mother_name_input",
                                onClick = {
                                    activeTextEditor = EditTextFieldRequest(
                                        fieldKey = "mother_name",
                                        title = motherTitle,
                                        placeholder = motherPlaceholder,
                                        initialValue = motherName,
                                        icon = UserNurseIcon,
                                        singleLine = true,
                                        initialPrivacy = motherPrivacy,
                                        testTag = "edit_mother_name_field",
                                        onSave = { newVal, chosenPrivacy ->
                                            hasUserEdited = true
                                            motherName = newVal
                                            val nextPriv = fieldPrivacyMap.toMutableMap().apply { put("mother_name", chosenPrivacy) }
                                            fieldPrivacyMap = nextPriv
                                            persistCurrentProfile(nextPriv)
                                        },
                                    )
                                },
                            )
                        }
                    }

                    // ── SECTION 4: SECURITY & PASSWORD ──
                    FbAccordionSection(
                        title = "নিরাপত্তা ও পাসওয়ার্ড".localized(),
                        expanded = securityExpanded,
                        onToggle = { securityExpanded = !securityExpanded },
                        hindSiliguri = hindSiliguri,
                    ) {
                        FbEditDetailRow(
                            icon = LockIcon,
                            title = if (hasLoginPassword) "পাসওয়ার্ড পরিবর্তন করুন".localized() else "পাসওয়ার্ড সেট করুন".localized(),
                            value = if (hasLoginPassword) "পাসওয়ার্ড সেট করা আছে".localized() else "সেট করা নেই".localized(),
                            isPlaceholder = !hasLoginPassword,
                            privacy = null,
                            hindSiliguri = hindSiliguri,
                            testTag = "profile_password_button",
                            onClick = { onPasswordSetupClick() },
                        )
                    }
                }
            }
        }
    }

    // ── SINGLE FIELD FULL-SCREEN EDITOR SUB-SCREEN ──
    KlassSubScreenHost(
        visible = activeTextEditor != null,
        zIndex = 90f,
    ) {
        activeTextEditor?.let { req ->
            FbSingleFieldEditorSubScreen(
                request = req,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onGpsSync = { gpsSyncLauncher() },
                onDismiss = { activeTextEditor = null },
            )
        }
    }

    // ── FULL-SCREEN SELECTION PICKER SUB-SCREEN ──
    KlassSubScreenHost(
        visible = activePicker != null,
        zIndex = 88f,
    ) {
        activePicker?.let { picker ->
            KlassSelectionPickerSubScreen(
                title = picker.title,
                subtitle = picker.subtitle.localized(),
                levelLabel = "প্রোফাইল এডিট".localized(),
                icon = picker.icon,
                accent = picker.accent,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = picker.options,
                selectedOption = picker.options.firstOrNull { it.equals(picker.selected, ignoreCase = true) },
                optionLabel = { opt -> if (picker.translateOptions) AppStrings.translate(opt, currentAppLanguage) else opt },
                searchable = picker.options.size > 6,
                onSelect = picker.onSelect,
                onDismiss = { activePicker = null },
            )
        }
    }

    // ── FULL-SCREEN PER-FIELD PRIVACY PICKER ──
    KlassSubScreenHost(
        visible = activePrivacyPicker != null,
        zIndex = 89f,
    ) {
        activePrivacyPicker?.let { req ->
            KlassSelectionPickerSubScreen(
                title = "${req.fieldLabel} • ${"গোপনীয়তা".localized()}",
                subtitle = "Who can see this on your profile?",
                levelLabel = "গোপনীয়তা".localized(),
                icon = ShieldIcon,
                accent = profileAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = ProfilePrivacy.entries,
                selectedOption = req.currentPrivacy,
                optionLabel = { p ->
                    when (p) {
                        ProfilePrivacy.PUBLIC -> "পাবলিক".localized(currentAppLanguage)
                        ProfilePrivacy.CONNECTIONS -> "শুধু কানেকশন".localized(currentAppLanguage)
                        ProfilePrivacy.PRIVATE -> "প্রাইভেট".localized(currentAppLanguage)
                    }
                },
                optionSubtitle = { p ->
                    when (p) {
                        ProfilePrivacy.PUBLIC -> "Anyone on MPI Klass can see this"
                        ProfilePrivacy.CONNECTIONS -> "Visible to your accepted connections only"
                        ProfilePrivacy.PRIVATE -> "Only you can see this"
                    }
                },
                searchable = false,
                onSelect = { chosen ->
                    updateFieldPrivacy(req.fieldKey, chosen)
                },
                onDismiss = { activePrivacyPicker = null },
            )
        }
    }
}

@Composable
private fun FbAccordionSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    hindSiliguri: FontFamily,
    content: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "fb_accordion_$title",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                color = cs.onSurface,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
            )
            Icon(
                imageVector = ChevronDownIcon,
                contentDescription = title,
                tint = cs.onSurface,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(140)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun FbEditDetailRow(
    icon: ImageVector,
    title: String,
    value: String,
    isPlaceholder: Boolean,
    privacy: ProfilePrivacy?,
    hindSiliguri: FontFamily,
    testTag: String? = null,
    onPrivacyClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 6.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = cs.onSurface,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = if (isPlaceholder) cs.onSurfaceVariant else cs.onSurface,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    color = if (isPlaceholder) cs.onSurfaceVariant.copy(alpha = 0.78f) else cs.onSurfaceVariant,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                    fontFamily = hindSiliguri,
                )
            }
            if (privacy != null) {
                val scopeIcon = when (privacy) {
                    ProfilePrivacy.PUBLIC -> LanguageGlobeIcon
                    ProfilePrivacy.CONNECTIONS -> UsersGroupIcon
                    ProfilePrivacy.PRIVATE -> LockIcon
                }
                val scopeLabel = when (privacy) {
                    ProfilePrivacy.PUBLIC -> "পাবলিক".localized()
                    ProfilePrivacy.CONNECTIONS -> "শুধু কানেকশন".localized()
                    ProfilePrivacy.PRIVATE -> "প্রাইভেট".localized()
                }
                Surface(
                    onClick = { onPrivacyClick?.invoke() },
                    enabled = onPrivacyClick != null,
                    shape = CircleShape,
                    color = cs.surfaceContainerHigh.copy(alpha = 0.6f),
                    shadowElevation = 0.dp,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            imageVector = scopeIcon,
                            contentDescription = "গোপনীয়তা".localized(),
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = scopeLabel,
                            color = cs.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = hindSiliguri,
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = EditIcon,
                contentDescription = "এডিট করুন".localized(),
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FbSingleFieldEditorSubScreen(
    request: EditTextFieldRequest,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onGpsSync: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val profileAccent = KlassFeaturePalette.profile(isDark)
    val sheetBg = if (isDark) Color(0xFF0B1120) else Color.White
    var textValue by remember(request.title, request.initialValue) { mutableStateOf(request.initialValue) }
    var selectedPrivacy by remember(request.fieldKey, request.initialPrivacy) {
        mutableStateOf(request.initialPrivacy)
    }

    AppBackHandler(enabled = true) {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sheetBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                focusManager.clearFocus(force = true)
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                sheetBg.copy(alpha = 0.88f),
                                sheetBg.copy(alpha = 0.42f),
                                Color.Transparent,
                            )
                        )
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 6.dp),
                ) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(48.dp)
                            .testTag("edit_single_field_back_button"),
                    ) {
                        Icon(
                            imageVector = BackArrowIcon,
                            contentDescription = "Back",
                            tint = cs.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Text(
                        text = request.title,
                        color = cs.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    HeaderSavePillButton(
                        text = "সংরক্ষণ".localized(),
                        accent = profileAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        testTag = "edit_single_field_top_save",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                        onClick = {
                            focusManager.clearFocus(force = true)
                            request.onSave(textValue, selectedPrivacy)
                            onDismiss()
                        },
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    singleLine = request.singleLine,
                    minLines = if (request.singleLine) 1 else 3,
                    maxLines = if (request.singleLine) 1 else 6,
                    label = {
                        Text(
                            text = request.title,
                            fontFamily = hindSiliguri,
                        )
                    },
                    placeholder = {
                        Text(
                            text = request.placeholder,
                            fontFamily = hindSiliguri,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = request.icon,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(19.dp),
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 16.5.sp,
                        fontFamily = hindSiliguri,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = request.keyboardType),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = profileAccent.primary,
                        unfocusedBorderColor = cs.outlineVariant,
                        focusedLabelColor = profileAccent.primary,
                        unfocusedLabelColor = cs.onSurfaceVariant,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(request.testTag),
                )

                if (request.fieldKey != "name") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "গোপনীয়তা".localized(),
                            color = cs.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ProfilePrivacy.entries.forEach { option ->
                                val icon = when (option) {
                                    ProfilePrivacy.PUBLIC -> LanguageGlobeIcon
                                    ProfilePrivacy.CONNECTIONS -> UsersGroupIcon
                                    ProfilePrivacy.PRIVATE -> LockIcon
                                }
                                val label = when (option) {
                                    ProfilePrivacy.PUBLIC -> "পাবলিক".localized()
                                    ProfilePrivacy.CONNECTIONS -> "শুধু কানেকশন".localized()
                                    ProfilePrivacy.PRIVATE -> "প্রাইভেট".localized()
                                }
                                MinimalIconChip(
                                    label = label,
                                    icon = icon,
                                    selected = selectedPrivacy == option,
                                    accent = profileAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.testTag("edit_privacy_${option.key.lowercase()}"),
                                    onClick = { selectedPrivacy = option },
                                )
                            }
                        }
                    }
                }

                if (request.supportsGps) {
                    KlassTonalButton(
                        text = "অটো-লোকেশন (GPS)".localized(),
                        icon = LocationDotIcon,
                        accent = KlassFeaturePalette.classrooms(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 42.dp,
                        testTag = "edit_sync_gps_button",
                        onClick = onGpsSync,
                    )
                }
            }
        }
    }
}
