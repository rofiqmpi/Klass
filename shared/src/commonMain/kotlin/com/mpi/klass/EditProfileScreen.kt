package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CameraIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DatabaseIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.IdCardIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.PrayingHandsIcon
import com.mpi.klass.theme.RingIcon
import com.mpi.klass.theme.SolidUserIcon
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserTieIcon
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

@Composable
fun EditProfileContent(
    initialProfile: StudentProfile,
    photoUrl: String?,
    fallbackName: String,
    fallbackEmail: String,
    role: UserRole = UserRole.STUDENT,
    hindSiliguri: FontFamily,
    topContentPadding: Dp,
    isSaving: Boolean,
    isDark: Boolean = false,
    onOverlayStateChange: (Boolean) -> Unit = {},
    onAvatarClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    onSaveProfile: (StudentProfile) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val focusManager = LocalFocusManager.current
    val profileAccent = KlassFeaturePalette.profile(isDark)
    val familyAccent = KlassFeaturePalette.books(isDark)
    val contactAccent = KlassFeaturePalette.teachers(isDark)
    val addressAccent = KlassFeaturePalette.classrooms(isDark)
    val academicAccent = KlassFeaturePalette.routine(isDark)
    var activePicker by remember { mutableStateOf<EditPickerRequest?>(null) }

    LaunchedEffect(activePicker) {
        onOverlayStateChange(activePicker != null)
    }

    var hasUserEdited by remember(fallbackEmail) { mutableStateOf(false) }
    var hasEditedCurrentAddress by remember(fallbackEmail) { mutableStateOf(false) }
    var fullName by remember(fallbackEmail) { mutableStateOf(initialProfile.name.ifBlank { fallbackName }) }
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

    LaunchedEffect(initialProfile) {
        if (!hasUserEdited) {
            fullName = initialProfile.name.ifBlank { fallbackName }
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
        }
    }

    LaunchedEffect(initialProfile.address) {
        if (!hasEditedCurrentAddress && initialProfile.address.isNotBlank()) {
            currentAddress = initialProfile.address
        }
    }

    val launchDatePicker = rememberDatePickerLauncher(initialDate = dob) { picked ->
        hasUserEdited = true
        dob = picked
    }

    val currentAppLanguage = LocalAppLanguage.current
    val gpsSyncLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank()) {
            hasUserEdited = true
            hasEditedCurrentAddress = true
            currentAddress = realAddress
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

    fun submitForm() {
        focusManager.clearFocus(force = true)
        val finalName = fullName.trim().ifBlank { fallbackName }
        val updated = initialProfile.copy(
            email = initialProfile.email.ifBlank { fallbackEmail }.trim().lowercase(),
            name = finalName,
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
        )
        onSaveProfile(updated)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalBackdrop(isDark = isDark, focusAccent = profileAccent.primary) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Scrollable Form Body
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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                focusManager.clearFocus(force = true)
                            }
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (topContentPadding > 0.dp) {
                            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                            Spacer(modifier = Modifier.height(topContentPadding))
                        }

                        // Full-bleed Facebook / Telegram Profile Edit Header
                        SocialSurfaceBlock(
                            isDark = isDark,
                            showBottomHairline = true,
                            accentTint = profileAccent.primary,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(72.dp)) {
                                    Surface(
                                        onClick = {
                                            focusManager.clearFocus(force = true)
                                            onAvatarClick()
                                        },
                                        shape = CircleShape,
                                        color = cs.surfaceContainerHigh,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(2.dp, profileAccent.primary.copy(alpha = 0.65f), CircleShape)
                                            .padding(2.5.dp)
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
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        onAvatarClick()
                                    },
                                    shape = CircleShape,
                                    color = profileAccent.primary,
                                    border = BorderStroke(1.5.dp, cs.surfaceContainerLowest),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = CameraIcon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আপনার প্রোফাইল সম্পাদনা করুন".localized(),
                                    color = cs.onSurface,
                                    fontSize = 18.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট".localized(),
                                    color = cs.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontFamily = hindSiliguri,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            KlassTonalButton(
                                text = "প্রোফাইল দেখুন".localized(),
                                icon = DatabaseIcon,
                                accent = profileAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                height = 40.dp,
                                testTag = "edit_to_view_profile_button",
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    onViewProfileClick()
                                },
                            )
                        }
                    }

                    SocialSectionDividerBand(isDark = isDark)

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        val cols = when {
                            maxWidth >= 820.dp -> 3
                            maxWidth >= 560.dp -> 2
                            else -> 1
                        }

                        val showPersonalExtended = role != UserRole.ADMIN
                        val showFamilySection = role == UserRole.STUDENT
                        val showAcademicSection = role != UserRole.ADMIN
                        val showFullAcademicFields = role == UserRole.STUDENT

                        val contactSectionNum = if (showFamilySection) "৩" else "২"
                        val addressSectionNum = if (showFamilySection) "৪" else "৩"
                        val academicSectionNum = if (showFamilySection) "৫" else "৪"

                        val fullNameLabel = when (role) {
                            UserRole.STUDENT -> "শিক্ষার্থীর পূর্ণ নাম (Full Name)"
                            UserRole.TEACHER -> "শিক্ষকের পূর্ণ নাম (Full Name)"
                            UserRole.ADMIN -> "পূর্ণ নাম (Full Name)"
                        }.localized()

                        Column(modifier = Modifier.fillMaxWidth()) {
                            GoogleSectionHeader(
                                title = "১. ব্যক্তিগত তথ্য (Personal Information)".localized(),
                                hindSiliguri = hindSiliguri,
                                accent = profileAccent.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val personalItems = buildList<Pair<Int, @Composable () -> Unit>> {
                                add(
                                    cols to {
                                        GoogleOutlinedTextField(
                                            label = fullNameLabel,
                                            icon = SolidUserIcon,
                                            value = fullName,
                                            placeholder = "আপনার পুরো নাম লিখুন".localized(),
                                            onValueChange = { hasUserEdited = true; fullName = it },
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_fullname_input",
                                        )
                                    }
                                )
                                add(
                                    1 to {
                                        val dobDisplay = if (dob.isNotBlank()) formatProfileDob(dob, isBengali) else ""
                                        GooglePickerField(
                                            label = "জন্ম তারিখ".localized(),
                                            leadingIcon = CalendarDaysIcon,
                                            trailingIcon = CalendarDaysIcon,
                                            value = dobDisplay,
                                            placeholder = "mm/dd/yyyy",
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_dob_picker",
                                            onClick = { launchDatePicker() },
                                        )
                                    }
                                )
                                add(
                                    1 to {
                                        val bgLabel = "রক্তের গ্রুপ".localized()
                                        GoogleSelectField(
                                            label = bgLabel,
                                            icon = DropletIcon,
                                            value = bloodGroup,
                                            placeholder = "গ্রুপ নির্বাচন করুন".localized(),
                                            translateOptions = false,
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_blood_group_select",
                                            onClick = {
                                                focusManager.clearFocus(force = true)
                                                activePicker = EditPickerRequest(
                                                    title = bgLabel,
                                                    subtitle = "রক্তের গ্রুপ বেছে নিন",
                                                    icon = DropletIcon,
                                                    accent = KlassFeaturePalette.blood(isDark).primary,
                                                    options = bloodOptions,
                                                    selected = bloodGroup,
                                                    translateOptions = false,
                                                    onSelect = { hasUserEdited = true; bloodGroup = it },
                                                )
                                            },
                                        )
                                    }
                                )
                                if (showPersonalExtended) {
                                    add(
                                        1 to {
                                            val relLabel = "ধর্ম".localized()
                                            GoogleSelectField(
                                                label = relLabel,
                                                icon = PrayingHandsIcon,
                                                value = religion,
                                                placeholder = "ধর্ম নির্বাচন করুন".localized(),
                                                translateOptions = true,
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_religion_select",
                                                onClick = {
                                                    focusManager.clearFocus(force = true)
                                                    activePicker = EditPickerRequest(
                                                        title = relLabel,
                                                        subtitle = "ধর্মীয় তথ্য নির্বাচন করুন",
                                                        icon = PrayingHandsIcon,
                                                        accent = profileAccent.primary,
                                                        options = religionOptions,
                                                        selected = religion,
                                                        translateOptions = true,
                                                        onSelect = { hasUserEdited = true; religion = it },
                                                    )
                                                },
                                            )
                                        }
                                    )
                                }
                                add(
                                    1 to {
                                        val genLabel = "লিঙ্গ (Gender)".localized()
                                        GoogleSelectField(
                                            label = genLabel,
                                            icon = VenusMarsIcon,
                                            value = gender,
                                            placeholder = "লিঙ্গ নির্বাচন করুন".localized(),
                                            translateOptions = true,
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_gender_select",
                                            onClick = {
                                                focusManager.clearFocus(force = true)
                                                activePicker = EditPickerRequest(
                                                    title = genLabel,
                                                    subtitle = "একটি অপশন বাছাই করুন",
                                                    icon = VenusMarsIcon,
                                                    accent = KlassFeaturePalette.appearance(isDark).primary,
                                                    options = genderOptions,
                                                    selected = gender,
                                                    translateOptions = true,
                                                    onSelect = { hasUserEdited = true; gender = it },
                                                )
                                            },
                                        )
                                    }
                                )
                                if (showPersonalExtended) {
                                    add(
                                        1 to {
                                            val marLabel = "বৈবাহিক অবস্থা".localized()
                                            GoogleSelectField(
                                                label = marLabel,
                                                icon = RingIcon,
                                                value = maritalStatus,
                                                placeholder = "অবিবাহিত (Unmarried)".localized(),
                                                translateOptions = true,
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_marital_select",
                                                onClick = {
                                                    focusManager.clearFocus(force = true)
                                                    activePicker = EditPickerRequest(
                                                        title = marLabel,
                                                        subtitle = "অবস্থা নির্বাচন করুন",
                                                        icon = RingIcon,
                                                        accent = profileAccent.primary,
                                                        options = maritalOptions,
                                                        selected = maritalStatus,
                                                        translateOptions = true,
                                                        onSelect = { hasUserEdited = true; maritalStatus = it },
                                                    )
                                                },
                                            )
                                        }
                                    )
                                }
                            }

                            HtmlResponsiveGrid(columns = cols, items = personalItems, spacing = 12.dp)

                            if (showFamilySection) {
                                Spacer(modifier = Modifier.height(20.dp))
                                GoogleSectionHeader(
                                    title = "২. পারিবারিক তথ্য (Family Information)".localized(),
                                    hindSiliguri = hindSiliguri,
                                    accent = familyAccent.primary,
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val familyItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                                    1 to {
                                        GoogleOutlinedTextField(
                                            label = "পিতার নাম (Father's Name)".localized(),
                                            icon = UserTieIcon,
                                            value = fatherName,
                                            placeholder = "পিতার নাম লিখুন".localized(),
                                            onValueChange = { hasUserEdited = true; fatherName = it },
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_father_name_input",
                                        )
                                    },
                                    1 to {
                                        GoogleOutlinedTextField(
                                            label = "মাতার নাম (Mother's Name)".localized(),
                                            icon = UserNurseIcon,
                                            value = motherName,
                                            placeholder = "মাতার নাম লিখুন".localized(),
                                            onValueChange = { hasUserEdited = true; motherName = it },
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_mother_name_input",
                                        )
                                    },
                                )
                                HtmlResponsiveGrid(columns = cols, items = familyItems, spacing = 12.dp)
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            GoogleSectionHeader(
                                title = "$contactSectionNum. যোগাযোগের তথ্য (Contact Information)".localized(),
                                hindSiliguri = hindSiliguri,
                                accent = contactAccent.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val contactItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                                1 to {
                                    GoogleOutlinedTextField(
                                        label = "মোবাইল নম্বর".localized(),
                                        icon = PhoneSolidIcon,
                                        value = phone,
                                        placeholder = "01XXXXXXXXX",
                                        keyboardType = KeyboardType.Phone,
                                        onValueChange = { hasUserEdited = true; phone = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_phone_input",
                                    )
                                },
                                1 to {
                                    GoogleOutlinedTextField(
                                        label = "ইমেল ঠিকানা (Email)".localized(),
                                        icon = EnvelopeSolidIcon,
                                        value = contactEmail,
                                        placeholder = "example@gmail.com",
                                        keyboardType = KeyboardType.Email,
                                        onValueChange = { hasUserEdited = true; contactEmail = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_email_input",
                                    )
                                },
                            )
                            HtmlResponsiveGrid(columns = cols, items = contactItems, spacing = 12.dp)

                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GoogleSectionHeader(
                                        title = "$addressSectionNum. ঠিকানার তথ্য (Address Information)".localized(),
                                        hindSiliguri = hindSiliguri,
                                        accent = addressAccent.primary,
                                    )
                                }
                                KlassTonalButton(
                                    text = "GPS",
                                    icon = LocationDotIcon,
                                    accent = addressAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    height = 36.dp,
                                    testTag = "edit_sync_gps_button",
                                    onClick = { gpsSyncLauncher() },
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val addressItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                                1 to {
                                    GoogleOutlinedTextField(
                                        label = "বর্তমান ঠিকানা".localized(),
                                        icon = LocationDotIcon,
                                        value = currentAddress,
                                        placeholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized(),
                                        onValueChange = { hasUserEdited = true; hasEditedCurrentAddress = true; currentAddress = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_current_address_input",
                                    )
                                },
                                1 to {
                                    GoogleOutlinedTextField(
                                        label = "স্থায়ী ঠিকানা".localized(),
                                        icon = LocationDotIcon,
                                        value = permanentAddress,
                                        placeholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized(),
                                        onValueChange = { hasUserEdited = true; permanentAddress = it },
                                        hindSiliguri = hindSiliguri,
                                        testTag = "edit_permanent_address_input",
                                    )
                                },
                            )
                            HtmlResponsiveGrid(columns = cols, items = addressItems, spacing = 12.dp)

                            if (showAcademicSection) {
                                Spacer(modifier = Modifier.height(20.dp))
                                GoogleSectionHeader(
                                    title = "$academicSectionNum. একাডেমিক তথ্য (Academic Information)".localized(),
                                    hindSiliguri = hindSiliguri,
                                    accent = academicAccent.primary,
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val academicItems = buildList<Pair<Int, @Composable () -> Unit>> {
                                    add(
                                        1 to {
                                            val deptLabel = "ডিপার্টমেন্ট (Department)".localized()
                                            GoogleSelectField(
                                                label = deptLabel,
                                                icon = GraduationCapIcon,
                                                value = department,
                                                placeholder = "ডিপার্টমেন্ট নির্বাচন করুন".localized(),
                                                translateOptions = false,
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_department_select",
                                                onClick = {
                                                    focusManager.clearFocus(force = true)
                                                    activePicker = EditPickerRequest(
                                                        title = deptLabel,
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
                                                        },
                                                    )
                                                },
                                            )
                                        }
                                    )
                                    if (showFullAcademicFields) {
                                        add(
                                            1 to {
                                                val semLabel = "সেমিস্টার".localized()
                                                GoogleSelectField(
                                                    label = semLabel,
                                                    icon = LayerGroupIcon,
                                                    value = semester,
                                                    placeholder = "সেমিস্টার নির্বাচন করুন".localized(),
                                                    translateOptions = true,
                                                    hindSiliguri = hindSiliguri,
                                                    testTag = "edit_semester_select",
                                                    onClick = {
                                                        focusManager.clearFocus(force = true)
                                                        activePicker = EditPickerRequest(
                                                            title = semLabel,
                                                            subtitle = "চলমান সেমিস্টার নির্বাচন করুন",
                                                            icon = LayerGroupIcon,
                                                            accent = academicAccent.primary,
                                                            options = semesterOptions,
                                                            selected = semester,
                                                            translateOptions = true,
                                                            onSelect = { hasUserEdited = true; semester = it },
                                                        )
                                                    },
                                                )
                                            }
                                        )
                                    }
                                    add(
                                        1 to {
                                            val shiftLabel = "শিফট".localized()
                                            GoogleSelectField(
                                                label = shiftLabel,
                                                icon = ClockIcon,
                                                value = shift,
                                                placeholder = "শিফট নির্বাচন করুন".localized(),
                                                translateOptions = true,
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_shift_select",
                                                onClick = {
                                                    focusManager.clearFocus(force = true)
                                                    activePicker = EditPickerRequest(
                                                        title = shiftLabel,
                                                        subtitle = "১ম বা ২য় শিফট বেছে নিন",
                                                        icon = ClockIcon,
                                                        accent = academicAccent.primary,
                                                        options = shiftOptions,
                                                        selected = shift,
                                                        translateOptions = true,
                                                        onSelect = { hasUserEdited = true; shift = it },
                                                    )
                                                },
                                            )
                                        }
                                    )
                                    if (showFullAcademicFields) {
                                        add(
                                            1 to {
                                                val secLabel = "সেকশন (Section)".localized()
                                                GoogleSelectField(
                                                    label = secLabel,
                                                    icon = UsersRectangleIcon,
                                                    value = section,
                                                    placeholder = "সেকশন নির্বাচন করুন".localized(),
                                                    translateOptions = false,
                                                    hindSiliguri = hindSiliguri,
                                                    testTag = "edit_section_select",
                                                    onClick = {
                                                        focusManager.clearFocus(force = true)
                                                        activePicker = EditPickerRequest(
                                                            title = secLabel,
                                                            subtitle = "ক্লাস সেকশন বা গ্রুপ বেছে নিন",
                                                            icon = UsersRectangleIcon,
                                                            accent = academicAccent.primary,
                                                            options = sectionOptions,
                                                            selected = section,
                                                            translateOptions = false,
                                                            onSelect = { hasUserEdited = true; section = it },
                                                        )
                                                    },
                                                )
                                            }
                                        )
                                        add(
                                            1 to {
                                                GoogleOutlinedTextField(
                                                    label = "বোর্ড রোল নম্বর (Roll)".localized(),
                                                    icon = IdCardIcon,
                                                    value = rollNumber,
                                                    placeholder = "যেমন: ৬৪৮১২৩".localized(),
                                                    onValueChange = { hasUserEdited = true; rollNumber = it },
                                                    hindSiliguri = hindSiliguri,
                                                    testTag = "edit_roll_input",
                                                )
                                            }
                                        )
                                        add(
                                            1 to {
                                                GoogleOutlinedTextField(
                                                    label = "রেজিস্ট্রেশন নম্বর (Registration)".localized(),
                                                    icon = FileLinesIcon,
                                                    value = registrationNumber,
                                                    placeholder = "যেমন: ১৭৫৬৪২".localized(),
                                                    onValueChange = { hasUserEdited = true; registrationNumber = it },
                                                    hindSiliguri = hindSiliguri,
                                                    testTag = "edit_registration_input",
                                                )
                                            }
                                        )
                                    }
                                }
                                HtmlResponsiveGrid(columns = cols, items = academicItems, spacing = 12.dp)
                            }
                        }
                    }
                }
            }

            // Pinned M3 Bottom Save Bar
            Surface(
                color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = cs.outlineVariant.copy(alpha = 0.5f),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 780.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GoogleOutlinedButton(
                            text = "বাতিল".localized(),
                            hindSiliguri = hindSiliguri,
                            enabled = !isSaving,
                            height = 48.dp,
                            contentColor = cs.onSurface,
                            modifier = Modifier.weight(1f),
                            onClick = onViewProfileClick,
                        )

                        WelcomeMinimal3DButton(
                            text = "সংরক্ষণ করুন".localized(),
                            icon = CheckIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            isLoading = isSaving,
                            enabled = !isSaving,
                            customPrimaryColor = profileAccent.primary,
                            customBaseColor = profileAccent.deep,
                            height = 48.dp,
                            cornerRadius = 14.dp,
                            testTag = "edit_profile_save_button",
                            modifier = Modifier.weight(1.4f),
                            onClick = { submitForm() },
                        )
                    }
                }
            }
        }
    }
    }

    // Full-Screen Picker Sub-Screen for Edit Fields (replaces tiny DropdownMenus)
    KlassSubScreenHost(
        visible = activePicker != null,
        zIndex = 88f,
    ) {
        activePicker?.let { picker ->
            KlassSelectionPickerSubScreen(
                title = picker.title,
                subtitle = picker.subtitle.localized(),
                levelLabel = "প্রোফাইল এডিট • নির্বাচন".localized(),
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
}

@Composable
internal fun HtmlResponsiveGrid(
    columns: Int,
    items: List<Pair<Int, @Composable () -> Unit>>,
    spacing: Dp,
) {
    if (columns <= 1) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            items.forEach { (_, content) ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
        return
    }

    val rows = mutableListOf<List<Pair<Int, @Composable () -> Unit>>>()
    var currentRow = mutableListOf<Pair<Int, @Composable () -> Unit>>()
    var currentSpan = 0
    for (item in items) {
        val span = item.first.coerceIn(1, columns)
        if (currentSpan + span > columns && currentRow.isNotEmpty()) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            currentSpan = 0
        }
        currentRow.add(span to item.second)
        currentSpan += span
        if (currentSpan >= columns) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            currentSpan = 0
        }
    }
    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        rows.forEach { rowItems ->
            val usedSpan = rowItems.sumOf { it.first }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                rowItems.forEach { (span, content) ->
                    Box(modifier = Modifier.weight(span.toFloat())) {
                        content()
                    }
                }
                if (usedSpan < columns) {
                    Spacer(modifier = Modifier.weight((columns - usedSpan).toFloat()))
                }
            }
        }
    }
}

@Composable
private fun GoogleOutlinedTextField(
    label: String,
    icon: ImageVector,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    hindSiliguri: FontFamily,
    testTag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val cs = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = {
            Text(
                text = label,
                fontFamily = hindSiliguri,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = hindSiliguri,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        },
        textStyle = TextStyle(
            fontSize = 16.5.sp,
            fontFamily = hindSiliguri,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = cs.primary,
            unfocusedBorderColor = cs.outlineVariant,
            focusedLabelColor = cs.primary,
            unfocusedLabelColor = cs.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
    )
}

@Composable
private fun GooglePickerField(
    label: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector,
    value: String,
    placeholder: String,
    hindSiliguri: FontFamily,
    testTag: String,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = {
                Text(
                    text = label,
                    fontFamily = hindSiliguri,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = hindSiliguri,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = label,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            },
            textStyle = TextStyle(
                fontSize = 16.5.sp,
                fontFamily = hindSiliguri,
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.outlineVariant,
                focusedLabelColor = cs.primary,
                unfocusedLabelColor = cs.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .testTag(testTag)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        )
    }
}

@Composable
private fun GoogleSelectField(
    label: String,
    icon: ImageVector,
    value: String,
    placeholder: String,
    translateOptions: Boolean,
    hindSiliguri: FontFamily,
    testTag: String,
    onClick: () -> Unit,
) {
    val displayedValue = if (value.isNotBlank()) {
        if (translateOptions) value.localized() else value
    } else ""

    GooglePickerField(
        label = label,
        leadingIcon = icon,
        trailingIcon = ChevronRightIcon,
        value = displayedValue,
        placeholder = placeholder,
        hindSiliguri = hindSiliguri,
        testTag = testTag,
        onClick = onClick,
    )
}
