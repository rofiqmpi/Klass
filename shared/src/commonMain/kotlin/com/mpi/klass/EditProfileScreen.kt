package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon

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

private fun normalizeGenderOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean.contains("Male", ignoreCase = true) || clean.contains("পুরুষ") -> "পুরুষ / Male"
        clean.contains("Female", ignoreCase = true) || clean.contains("নারী") || clean.contains("মহিলা") -> "নারী / Female"
        clean.contains("Other", ignoreCase = true) || clean.contains("অন্যান্য") -> "অন্যান্য / Other"
        else -> clean
    }
}

private fun normalizeMaritalOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean.contains("Unmarried", ignoreCase = true) || clean.contains("Single", ignoreCase = true) || clean.contains("অবিবাহিত") -> "অবিবাহিত / Single"
        clean.contains("Married", ignoreCase = true) || clean.contains("বিবাহিত") -> "বিবাহিত / Married"
        clean.contains("Other", ignoreCase = true) || clean.contains("অন্যান্য") -> "অন্যান্য / Other"
        else -> clean
    }
}

private data class PrivacyPickerRequest(
    val fieldKey: String,
    val fieldLabel: String,
    val currentPrivacy: ProfilePrivacy,
)

internal fun defaultProfileFieldPrivacy(fieldKey: String, role: UserRole): ProfilePrivacy = when (fieldKey) {
    "bio", "name", "department", "semester", "shift", "section", "blood_group" -> ProfilePrivacy.PUBLIC
    "phone", "address", "contact_email" -> if (role == UserRole.TEACHER) ProfilePrivacy.PUBLIC else ProfilePrivacy.PRIVATE
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
    val locationAccent = KlassFeaturePalette.classrooms(isDark)
    val contactAccent = KlassFeaturePalette.teachers(isDark)
    val personalAccent = KlassFeaturePalette.appearance(isDark)
    val securityAccent = KlassFeaturePalette.notifications(isDark)

    val heroGlassBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.96f)
    val topBarFadeBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)

    var activeFieldEditor by remember { mutableStateOf<ProfileField?>(null) }
    var activePrivacyPicker by remember { mutableStateOf<PrivacyPickerRequest?>(null) }

    LaunchedEffect(activeFieldEditor, activePrivacyPicker) {
        onOverlayStateChange(activeFieldEditor != null || activePrivacyPicker != null)
    }

    var hasUserEdited by remember(fallbackEmail) { mutableStateOf(false) }
    var fullName by remember(fallbackEmail) { mutableStateOf(initialProfile.name.ifBlank { fallbackName }) }
    var bio by remember(fallbackEmail) { mutableStateOf(initialProfile.bio) }
    var dob by remember(fallbackEmail) { mutableStateOf(initialProfile.dob) }
    var bloodGroup by remember(fallbackEmail) { mutableStateOf(initialProfile.bloodGroup) }
    var religion by remember(fallbackEmail) { mutableStateOf(initialProfile.religion) }
    var gender by remember(fallbackEmail) { mutableStateOf(normalizeGenderOption(initialProfile.gender)) }
    var maritalStatus by remember(fallbackEmail) { mutableStateOf(normalizeMaritalOption(initialProfile.maritalStatus)) }
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
            dob = initialProfile.dob
            bloodGroup = initialProfile.bloodGroup
            religion = initialProfile.religion
            gender = normalizeGenderOption(initialProfile.gender)
            maritalStatus = normalizeMaritalOption(initialProfile.maritalStatus)
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
            fatherName = "",
            motherName = "",
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

    val gpsSyncLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank()) {
            hasUserEdited = true
            currentAddress = realAddress
            persistCurrentProfile()
        } else {
            showToast(AppStrings.translate("লোকেশন পাওয়া যায়নি। অনুগ্রহ করে ঠিকানা লিখুন।", currentAppLanguage))
        }
    }

    var introExpanded by remember { mutableStateOf(true) }
    var categoryExpanded by remember { mutableStateOf(true) }
    var personalExpanded by remember { mutableStateOf(true) }
    var securityExpanded by remember { mutableStateOf(true) }

    MinimalBackdrop(isDark = isDark, focusAccent = profileAccent.primary) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
            // ── TRANSLUCENT TOP-TO-BOTTOM FADING HEADER ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                topBarFadeBg,
                                topBarFadeBg.copy(alpha = 0.45f),
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

            // ── SCROLLABLE AURORA EDIT PROFILE BODY ──
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

                    val coverHeight = 150.dp
                    val shoulderOverlap = 24.dp
                    val avatarSize = 140.dp
                    val heroTotalHeight = 210.dp
                    val camBtnBg = if (isDark) cs.surfaceContainerHigh else Color.White
                    val camBtnBorder = if (isDark) profileAccent.borderTint else Color(0xFFD1D5DB)
                    val camIconTint = if (isDark) profileAccent.primary else Color(0xFF111827)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heroTotalHeight),
                    ) {
                        // Cover Photo layer strictly underneath shoulder and avatar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(coverHeight)
                                .align(Alignment.TopCenter)
                                .zIndex(0f)
                                .clipToBounds()
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    profileAccent.softTint.copy(alpha = if (isDark) 0.36f else 0.55f),
                                                    cs.surfaceContainerLow.copy(alpha = if (isDark) 0.50f else 0.68f),
                                                )
                                            )
                                        )
                                        .padding(bottom = shoulderOverlap + 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = CameraIcon,
                                            contentDescription = null,
                                            tint = cs.onSurfaceVariant.copy(alpha = 0.72f),
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = "কভার ফটো যোগ করুন".localized(),
                                            color = cs.onSurfaceVariant.copy(alpha = 0.80f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = hindSiliguri,
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = onCoverPhotoClick,
                                shape = CircleShape,
                                color = camBtnBg,
                                border = BorderStroke(0.75.dp, camBtnBorder),
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
                                        tint = camIconTint,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // Rounded Glass Shoulder overlapping cover bottom (sits cleanly above cover layer)
                        Surface(
                            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                            color = heroGlassBg,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heroTotalHeight - (coverHeight - shoulderOverlap))
                                .align(Alignment.BottomCenter)
                                .zIndex(1f),
                        ) {}

                        // Centered Circular Avatar straddling the curved Cover/Sheet seam (topmost layer)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .size(avatarSize)
                                .zIndex(2f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                onClick = onAvatarClick,
                                shape = CircleShape,
                                color = cs.surfaceContainerHigh,
                                border = BorderStroke(4.dp, heroGlassBg),
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

                            Surface(
                                onClick = onAvatarClick,
                                shape = CircleShape,
                                color = camBtnBg,
                                border = BorderStroke(0.75.dp, camBtnBorder),
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
                                        tint = camIconTint,
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }
                        }
                    }

                    // ── SECTION 1: INTRO ──
                    FbAccordionSection(
                        title = "পরিচিতি".localized(),
                        accent = profileAccent.primary,
                        expanded = introExpanded,
                        onToggle = { introExpanded = !introExpanded },
                        hindSiliguri = hindSiliguri,
                    ) {
                        SocialSurfaceBlock(
                            isDark = isDark,
                            showTopHairline = true,
                            showBottomHairline = true,
                            accentTint = profileAccent.primary,
                        ) {
                            val bioTitle = "বায়ো".localized()
                            val bioPlaceholder = "নিজের সম্পর্কে সংক্ষেপে লিখুন...".localized()
                            val bioPrivacy = privacyForField("bio")
                            FbEditDetailRow(
                                icon = HandWaveIcon,
                                accent = profileAccent.primary,
                                isDark = isDark,
                                title = bioTitle,
                                value = bio.ifBlank { bioPlaceholder },
                                isPlaceholder = bio.isBlank(),
                                privacy = bioPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("bio", bioTitle, bioPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_bio_row",
                                onClick = { activeFieldEditor = ProfileField.BIO },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            val nameTitle = "পূর্ণ নাম".localized()
                            FbEditDetailRow(
                                icon = SolidUserIcon,
                                accent = profileAccent.primary,
                                isDark = isDark,
                                title = nameTitle,
                                value = fullName.ifBlank { fallbackName },
                                isPlaceholder = fullName.isBlank(),
                                privacy = null,
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_fullname_row",
                                onClick = { activeFieldEditor = ProfileField.NAME },
                            )
                        }
                    }

                    // ── SECTION 2: CATEGORY / ACADEMIC (Students & Teachers) ──
                    if (role != UserRole.ADMIN) {
                        SocialSectionDividerBand(isDark = isDark, height = 8.dp)
                        FbAccordionSection(
                            title = "ক্যাটাগরি".localized(),
                            accent = academicAccent.primary,
                            expanded = categoryExpanded,
                            onToggle = { categoryExpanded = !categoryExpanded },
                            hindSiliguri = hindSiliguri,
                        ) {
                            SocialSurfaceBlock(
                                isDark = isDark,
                                showTopHairline = true,
                                showBottomHairline = true,
                                accentTint = academicAccent.primary,
                            ) {
                                val deptTitle = "ডিপার্টমেন্ট (Department)".localized()
                                val notAddedText = "তথ্য যোগ করা হয়নি".localized()
                                val deptPrivacy = privacyForField("department")
                                FbEditDetailRow(
                                    icon = GraduationCapIcon,
                                    accent = academicAccent.primary,
                                    isDark = isDark,
                                    title = department.ifBlank { deptTitle },
                                    value = if (department.isNotBlank()) deptTitle else notAddedText,
                                    isPlaceholder = department.isBlank(),
                                    privacy = deptPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("department", deptTitle, deptPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_department_select",
                                    onClick = { activeFieldEditor = ProfileField.DEPARTMENT },
                                )

                                if (role == UserRole.STUDENT) {
                                    SocialInsetDivider(startIndent = 68.dp)
                                    val semTitle = "সেমিস্টার".localized()
                                    val semVal = if (semester.isNotBlank()) semester.localized() else ""
                                    val semPrivacy = privacyForField("semester")
                                    FbEditDetailRow(
                                        icon = LayerGroupIcon,
                                        accent = academicAccent.primary,
                                        isDark = isDark,
                                        title = semVal.ifBlank { semTitle },
                                        value = if (semVal.isNotBlank()) semTitle else notAddedText,
                                        isPlaceholder = semVal.isBlank(),
                                        privacy = semPrivacy,
                                        onPrivacyClick = {
                                            activePrivacyPicker = PrivacyPickerRequest("semester", semTitle, semPrivacy)
                                        },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_semester_select",
                                        onClick = { activeFieldEditor = ProfileField.SEMESTER },
                                    )
                                }

                                SocialInsetDivider(startIndent = 68.dp)
                                val shiftTitle = "শিফট".localized()
                                val shiftVal = if (shift.isNotBlank()) shift.localized() else ""
                                val shiftPrivacy = privacyForField("shift")
                                FbEditDetailRow(
                                    icon = ClockIcon,
                                    accent = academicAccent.primary,
                                    isDark = isDark,
                                    title = shiftVal.ifBlank { shiftTitle },
                                    value = if (shiftVal.isNotBlank()) shiftTitle else notAddedText,
                                    isPlaceholder = shiftVal.isBlank(),
                                    privacy = shiftPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("shift", shiftTitle, shiftPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_shift_select",
                                    onClick = { activeFieldEditor = ProfileField.SHIFT },
                                )

                                if (role == UserRole.STUDENT) {
                                    SocialInsetDivider(startIndent = 68.dp)
                                    val secTitle = "সেকশন (Section)".localized()
                                    val secPrivacy = privacyForField("section")
                                    FbEditDetailRow(
                                        icon = UsersRectangleIcon,
                                        accent = academicAccent.primary,
                                        isDark = isDark,
                                        title = section.ifBlank { secTitle },
                                        value = if (section.isNotBlank()) secTitle else notAddedText,
                                        isPlaceholder = section.isBlank(),
                                        privacy = secPrivacy,
                                        onPrivacyClick = {
                                            activePrivacyPicker = PrivacyPickerRequest("section", secTitle, secPrivacy)
                                        },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_section_select",
                                        onClick = { activeFieldEditor = ProfileField.SECTION },
                                    )
                                }

                                if (role == UserRole.TEACHER && onOpenTeachingPrefs != null) {
                                    SocialInsetDivider(startIndent = 68.dp)
                                    FbEditDetailRow(
                                        icon = SparkleIcon,
                                        accent = contactAccent.primary,
                                        isDark = isDark,
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
                    }

                    // ── SECTION 3: PERSONAL DETAILS (Location, Home, Blood, Contact, Personal) ──
                    SocialSectionDividerBand(isDark = isDark, height = 8.dp)
                    FbAccordionSection(
                        title = "ব্যক্তিগত বিবরণ".localized(),
                        accent = personalAccent.primary,
                        expanded = personalExpanded,
                        onToggle = { personalExpanded = !personalExpanded },
                        hindSiliguri = hindSiliguri,
                    ) {
                        SocialSurfaceBlock(
                            isDark = isDark,
                            showTopHairline = true,
                            showBottomHairline = true,
                            accentTint = personalAccent.primary,
                        ) {
                            val notAddedText = "তথ্য যোগ করা হয়নি".localized()

                            // Current address
                            val currAddrTitle = "বর্তমান ঠিকানা".localized()
                            val addrPrivacy = privacyForField("address")
                            FbEditDetailRow(
                                icon = LocationDotIcon,
                                accent = locationAccent.primary,
                                isDark = isDark,
                                title = currentAddress.ifBlank { currAddrTitle },
                                value = if (currentAddress.isNotBlank()) currAddrTitle else notAddedText,
                                isPlaceholder = currentAddress.isBlank(),
                                privacy = addrPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("address", currAddrTitle, addrPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_current_address_input",
                                onClick = { activeFieldEditor = ProfileField.ADDRESS },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            // Permanent address
                            val permAddrTitle = "স্থায়ী ঠিকানা".localized()
                            val permPrivacy = privacyForField("permanent_address")
                            FbEditDetailRow(
                                icon = HomeIcon,
                                accent = locationAccent.primary,
                                isDark = isDark,
                                title = permanentAddress.ifBlank { permAddrTitle },
                                value = if (permanentAddress.isNotBlank()) permAddrTitle else notAddedText,
                                isPlaceholder = permanentAddress.isBlank(),
                                privacy = permPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("permanent_address", permAddrTitle, permPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_permanent_address_input",
                                onClick = { activeFieldEditor = ProfileField.PERMANENT_ADDRESS },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            // Blood group
                            val bloodTitle = "রক্তের গ্রুপ".localized()
                            val bloodPrivacy = privacyForField("blood_group")
                            FbEditDetailRow(
                                icon = DropletIcon,
                                accent = bloodAccent.primary,
                                isDark = isDark,
                                title = bloodGroup.ifBlank { bloodTitle },
                                value = if (bloodGroup.isNotBlank()) bloodTitle else notAddedText,
                                isPlaceholder = bloodGroup.isBlank(),
                                privacy = bloodPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("blood_group", bloodTitle, bloodPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_blood_group_select",
                                onClick = { activeFieldEditor = ProfileField.BLOOD_GROUP },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            // Mobile phone
                            val phoneTitle = "মোবাইল নম্বর".localized()
                            val phonePrivacy = privacyForField("phone")
                            FbEditDetailRow(
                                icon = PhoneSolidIcon,
                                accent = contactAccent.primary,
                                isDark = isDark,
                                title = phone.ifBlank { phoneTitle },
                                value = if (phone.isNotBlank()) phoneTitle else notAddedText,
                                isPlaceholder = phone.isBlank(),
                                privacy = phonePrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("phone", phoneTitle, phonePrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_phone_input",
                                onClick = { activeFieldEditor = ProfileField.PHONE },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            // Contact email
                            val emailTitle = "ইমেল ঠিকানা".localized()
                            val emailPrivacy = privacyForField("contact_email")
                            FbEditDetailRow(
                                icon = EnvelopeSolidIcon,
                                accent = contactAccent.primary,
                                isDark = isDark,
                                title = contactEmail.ifBlank { emailTitle },
                                value = if (contactEmail.isNotBlank()) emailTitle else notAddedText,
                                isPlaceholder = contactEmail.isBlank(),
                                privacy = emailPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("contact_email", emailTitle, emailPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_email_input",
                                onClick = { activeFieldEditor = ProfileField.CONTACT_EMAIL },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            // Date of birth (opens Facebook-style 3-column wheel sub-screen)
                            val dobTitle = "জন্ম তারিখ".localized()
                            val dobDisplay = if (dob.isNotBlank()) formatProfileDob(dob, isBengali) else ""
                            val dobPrivacy = privacyForField("dob")
                            FbEditDetailRow(
                                icon = CalendarDaysIcon,
                                accent = personalAccent.primary,
                                isDark = isDark,
                                title = dobDisplay.ifBlank { dobTitle },
                                value = if (dobDisplay.isNotBlank()) dobTitle else notAddedText,
                                isPlaceholder = dobDisplay.isBlank(),
                                privacy = dobPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("dob", dobTitle, dobPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_dob_picker",
                                onClick = { activeFieldEditor = ProfileField.DOB },
                            )

                            SocialInsetDivider(startIndent = 68.dp)

                            // Gender
                            val genTitle = "লিঙ্গ".localized()
                            val genVal = if (gender.isNotBlank()) gender.localized() else ""
                            val genderPrivacy = privacyForField("gender")
                            FbEditDetailRow(
                                icon = VenusMarsIcon,
                                accent = personalAccent.primary,
                                isDark = isDark,
                                title = genVal.ifBlank { genTitle },
                                value = if (genVal.isNotBlank()) genTitle else notAddedText,
                                isPlaceholder = genVal.isBlank(),
                                privacy = genderPrivacy,
                                onPrivacyClick = {
                                    activePrivacyPicker = PrivacyPickerRequest("gender", genTitle, genderPrivacy)
                                },
                                hindSiliguri = hindSiliguri,
                                testTag = "edit_gender_select",
                                onClick = { activeFieldEditor = ProfileField.GENDER },
                            )

                            if (role != UserRole.ADMIN) {
                                SocialInsetDivider(startIndent = 68.dp)
                                val relTitle = "ধর্ম".localized()
                                val relVal = if (religion.isNotBlank()) religion.localized() else ""
                                val relPrivacy = privacyForField("religion")
                                FbEditDetailRow(
                                    icon = PrayingHandsIcon,
                                    accent = personalAccent.primary,
                                    isDark = isDark,
                                    title = relVal.ifBlank { relTitle },
                                    value = if (relVal.isNotBlank()) relTitle else notAddedText,
                                    isPlaceholder = relVal.isBlank(),
                                    privacy = relPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("religion", relTitle, relPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_religion_select",
                                    onClick = { activeFieldEditor = ProfileField.RELIGION },
                                )

                                SocialInsetDivider(startIndent = 68.dp)
                                val marTitle = "বৈবাহিক অবস্থা".localized()
                                val marVal = if (maritalStatus.isNotBlank()) maritalStatus.localized() else ""
                                val marPrivacy = privacyForField("marital_status")
                                FbEditDetailRow(
                                    icon = RingIcon,
                                    accent = personalAccent.primary,
                                    isDark = isDark,
                                    title = marVal.ifBlank { marTitle },
                                    value = if (marVal.isNotBlank()) marTitle else notAddedText,
                                    isPlaceholder = marVal.isBlank(),
                                    privacy = marPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("marital_status", marTitle, marPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_marital_select",
                                    onClick = { activeFieldEditor = ProfileField.MARITAL_STATUS },
                                )
                            }

                            if (role == UserRole.STUDENT) {
                                SocialInsetDivider(startIndent = 68.dp)
                                val rollTitle = "বোর্ড রোল নম্বর (Roll)".localized()
                                val rollPrivacy = privacyForField("roll_number")
                                FbEditDetailRow(
                                    icon = IdCardIcon,
                                    accent = academicAccent.primary,
                                    isDark = isDark,
                                    title = rollNumber.ifBlank { rollTitle },
                                    value = if (rollNumber.isNotBlank()) rollTitle else notAddedText,
                                    isPlaceholder = rollNumber.isBlank(),
                                    privacy = rollPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("roll_number", rollTitle, rollPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_roll_input",
                                    onClick = { activeFieldEditor = ProfileField.ROLL_NUMBER },
                                )

                                SocialInsetDivider(startIndent = 68.dp)
                                val regTitle = "রেজিস্ট্রেশন নম্বর (Registration)".localized()
                                val regPrivacy = privacyForField("registration_number")
                                FbEditDetailRow(
                                    icon = FileLinesIcon,
                                    accent = academicAccent.primary,
                                    isDark = isDark,
                                    title = registrationNumber.ifBlank { regTitle },
                                    value = if (registrationNumber.isNotBlank()) regTitle else notAddedText,
                                    isPlaceholder = registrationNumber.isBlank(),
                                    privacy = regPrivacy,
                                    onPrivacyClick = {
                                        activePrivacyPicker = PrivacyPickerRequest("registration_number", regTitle, regPrivacy)
                                    },
                                    hindSiliguri = hindSiliguri,
                                    testTag = "edit_registration_input",
                                    onClick = { activeFieldEditor = ProfileField.REGISTRATION_NUMBER },
                                )
                            }
                        }
                    }

                    // ── SECTION 4: SECURITY & PASSWORD ──
                    SocialSectionDividerBand(isDark = isDark, height = 8.dp)
                    FbAccordionSection(
                        title = "নিরাপত্তা ও পাসওয়ার্ড".localized(),
                        accent = securityAccent.primary,
                        expanded = securityExpanded,
                        onToggle = { securityExpanded = !securityExpanded },
                        hindSiliguri = hindSiliguri,
                    ) {
                        SocialSurfaceBlock(
                            isDark = isDark,
                            showTopHairline = true,
                            showBottomHairline = true,
                            accentTint = securityAccent.primary,
                        ) {
                            FbEditDetailRow(
                                icon = LockIcon,
                                accent = securityAccent.primary,
                                isDark = isDark,
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
    }

    // ── UNIFIED SIGNUP-STYLE STEP EDITOR SUB-SCREEN (ALL FIELDS & DOB WHEEL) ──
    KlassSubScreenHost(
        visible = activeFieldEditor != null,
        zIndex = 90f,
    ) {
        activeFieldEditor?.let { field ->
            val fieldKey = profileFieldKey(field)
            val currentValue = when (field) {
                ProfileField.NAME -> fullName
                ProfileField.BIO -> bio
                ProfileField.DEPARTMENT -> department
                ProfileField.SEMESTER -> semester
                ProfileField.SHIFT -> shift
                ProfileField.SECTION -> section
                ProfileField.ROLL_NUMBER -> rollNumber
                ProfileField.REGISTRATION_NUMBER -> registrationNumber
                ProfileField.PHONE -> phone
                ProfileField.CONTACT_EMAIL -> contactEmail
                ProfileField.BLOOD_GROUP -> bloodGroup
                ProfileField.ADDRESS -> currentAddress
                ProfileField.PERMANENT_ADDRESS -> permanentAddress
                ProfileField.DOB -> dob
                ProfileField.GENDER -> gender
                ProfileField.RELIGION -> religion
                ProfileField.MARITAL_STATUS -> maritalStatus
            }
            val inputTestTag = when (field) {
                ProfileField.NAME -> "edit_fullname_input"
                ProfileField.BIO -> "edit_bio_input"
                ProfileField.PHONE -> "edit_phone_field"
                ProfileField.CONTACT_EMAIL -> "edit_email_field"
                ProfileField.ADDRESS -> "edit_current_address_field"
                ProfileField.PERMANENT_ADDRESS -> "edit_permanent_address_field"
                ProfileField.ROLL_NUMBER -> "edit_roll_field"
                ProfileField.REGISTRATION_NUMBER -> "edit_registration_field"
                else -> "edit_single_field_input"
            }

            ProfileStepEditorSubScreen(
                field = field,
                initialValue = currentValue,
                initialPrivacy = if (field == ProfileField.NAME) ProfilePrivacy.PUBLIC else privacyForField(fieldKey),
                role = role,
                department = department,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                inputTestTag = inputTestTag,
                onGpsSync = { gpsSyncLauncher() },
                onSave = { newVal, chosenPrivacy ->
                    hasUserEdited = true
                    when (field) {
                        ProfileField.NAME -> fullName = newVal
                        ProfileField.BIO -> bio = newVal
                        ProfileField.DEPARTMENT -> {
                            department = newVal
                            if (section.isNotBlank()) {
                                section = RoutineRules.normalizeGroup(newVal, section)
                            }
                        }
                        ProfileField.SEMESTER -> semester = newVal
                        ProfileField.SHIFT -> shift = newVal
                        ProfileField.SECTION -> section = newVal
                        ProfileField.ROLL_NUMBER -> rollNumber = newVal
                        ProfileField.REGISTRATION_NUMBER -> registrationNumber = newVal
                        ProfileField.PHONE -> phone = newVal
                        ProfileField.CONTACT_EMAIL -> contactEmail = newVal
                        ProfileField.BLOOD_GROUP -> bloodGroup = newVal
                        ProfileField.ADDRESS -> currentAddress = newVal
                        ProfileField.PERMANENT_ADDRESS -> permanentAddress = newVal
                        ProfileField.DOB -> dob = newVal
                        ProfileField.GENDER -> gender = newVal
                        ProfileField.RELIGION -> religion = newVal
                        ProfileField.MARITAL_STATUS -> maritalStatus = newVal
                    }
                    val nextPrivacy = if (field == ProfileField.NAME) {
                        fieldPrivacyMap
                    } else {
                        fieldPrivacyMap.toMutableMap().apply { put(fieldKey, chosenPrivacy) }
                    }
                    fieldPrivacyMap = nextPrivacy
                    persistCurrentProfile(nextPrivacy)
                },
                onDismiss = { activeFieldEditor = null },
            )
        }
    }

    // ── QUICK PER-FIELD PRIVACY PICKER ──
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
                        ProfilePrivacy.PRIVATE -> "প্রাইভেট".localized(currentAppLanguage)
                    }
                },
                optionSubtitle = { p ->
                    when (p) {
                        ProfilePrivacy.PUBLIC -> "Anyone on MPI Klass can see this"
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
    accent: Color,
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = title,
                color = cs.onSurface,
                fontSize = 18.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.14f),
                modifier = Modifier.size(30.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(chevronRotation),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(140)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun FbEditDetailRow(
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
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
    val badgeBg = accent.copy(alpha = if (isDark) 0.22f else 0.12f)
    val badgeBorder = accent.copy(alpha = if (isDark) 0.42f else 0.26f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = badgeBg,
            border = BorderStroke(0.75.dp, badgeBorder),
            shadowElevation = 0.dp,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

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
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontFamily = hindSiliguri,
                )
            }
            if (privacy != null) {
                val scopeIcon = when (privacy) {
                    ProfilePrivacy.PUBLIC -> LanguageGlobeIcon
                    ProfilePrivacy.PRIVATE -> LockIcon
                }
                val scopeLabel = when (privacy) {
                    ProfilePrivacy.PUBLIC -> "পাবলিক".localized()
                    ProfilePrivacy.PRIVATE -> "প্রাইভেট".localized()
                }
                Surface(
                    onClick = { onPrivacyClick?.invoke() },
                    enabled = onPrivacyClick != null,
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.16f else 0.09f),
                    border = BorderStroke(0.5.dp, accent.copy(alpha = 0.28f)),
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
                            tint = accent,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = scopeLabel,
                            color = cs.onSurfaceVariant,
                            fontSize = 12.sp,
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
                tint = accent.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
