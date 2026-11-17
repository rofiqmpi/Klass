package com.mpi.klass

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DatabaseIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.IdCardIcon
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
    onAvatarClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    onSaveProfile: (StudentProfile) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val focusManager = LocalFocusManager.current

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

    MinimalBackdrop(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding(),
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
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (topContentPadding > 0.dp) {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        Spacer(modifier = Modifier.height(topContentPadding))
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Compact Google Contacts Edit Header Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = cs.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(64.dp)) {
                                Surface(
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        onAvatarClick()
                                    },
                                    shape = CircleShape,
                                    color = cs.surfaceContainerHigh,
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
                                    onClick = {
                                        focusManager.clearFocus(force = true)
                                        onAvatarClick()
                                    },
                                    shape = CircleShape,
                                    color = cs.primaryContainer,
                                    border = BorderStroke(1.5.dp, cs.surfaceContainerLow),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = CameraIcon,
                                            contentDescription = null,
                                            tint = cs.onPrimaryContainer,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আপনার প্রোফাইল সম্পাদনা করুন".localized(),
                                    color = cs.onSurface,
                                    fontSize = 16.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট".localized(),
                                    color = cs.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledTonalButton(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    onViewProfileClick()
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("edit_to_view_profile_button"),
                            ) {
                                Icon(
                                    imageVector = DatabaseIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "প্রোফাইল দেখুন".localized(),
                                    fontSize = 12.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
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
                                        GoogleSelectField(
                                            label = "রক্তের গ্রুপ".localized(),
                                            icon = DropletIcon,
                                            value = bloodGroup,
                                            placeholder = "গ্রুপ নির্বাচন করুন".localized(),
                                            options = bloodOptions,
                                            translateOptions = false,
                                            onSelect = { hasUserEdited = true; bloodGroup = it },
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_blood_group_select",
                                        )
                                    }
                                )
                                if (showPersonalExtended) {
                                    add(
                                        1 to {
                                            GoogleSelectField(
                                                label = "ধর্ম".localized(),
                                                icon = PrayingHandsIcon,
                                                value = religion,
                                                placeholder = "ধর্ম নির্বাচন করুন".localized(),
                                                options = religionOptions,
                                                translateOptions = true,
                                                onSelect = { hasUserEdited = true; religion = it },
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_religion_select",
                                            )
                                        }
                                    )
                                }
                                add(
                                    1 to {
                                        GoogleSelectField(
                                            label = "লিঙ্গ (Gender)".localized(),
                                            icon = VenusMarsIcon,
                                            value = gender,
                                            placeholder = "লিঙ্গ নির্বাচন করুন".localized(),
                                            options = genderOptions,
                                            translateOptions = true,
                                            onSelect = { hasUserEdited = true; gender = it },
                                            hindSiliguri = hindSiliguri,
                                            testTag = "edit_gender_select",
                                        )
                                    }
                                )
                                if (showPersonalExtended) {
                                    add(
                                        1 to {
                                            GoogleSelectField(
                                                label = "বৈবাহিক অবস্থা".localized(),
                                                icon = RingIcon,
                                                value = maritalStatus,
                                                placeholder = "অবিবাহিত (Unmarried)".localized(),
                                                options = maritalOptions,
                                                translateOptions = true,
                                                onSelect = { hasUserEdited = true; maritalStatus = it },
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_marital_select",
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
                            GoogleSectionHeader(
                                title = "$addressSectionNum. ঠিকানার তথ্য (Address Information)".localized(),
                                hindSiliguri = hindSiliguri,
                            )
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
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val academicItems = buildList<Pair<Int, @Composable () -> Unit>> {
                                    add(
                                        1 to {
                                            GoogleSelectField(
                                                label = "ডিপার্টমেন্ট (Department)".localized(),
                                                icon = GraduationCapIcon,
                                                value = department,
                                                placeholder = "ডিপার্টমেন্ট নির্বাচন করুন".localized(),
                                                options = deptOptions,
                                                translateOptions = false,
                                                onSelect = {
                                                    hasUserEdited = true
                                                    department = it
                                                    if (section.isNotBlank()) {
                                                        section = RoutineRules.normalizeGroup(it, section)
                                                    }
                                                },
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_department_select",
                                            )
                                        }
                                    )
                                    if (showFullAcademicFields) {
                                        add(
                                            1 to {
                                                GoogleSelectField(
                                                    label = "সেমিস্টার".localized(),
                                                    icon = LayerGroupIcon,
                                                    value = semester,
                                                    placeholder = "সেমিস্টার নির্বাচন করুন".localized(),
                                                    options = semesterOptions,
                                                    translateOptions = true,
                                                    onSelect = { hasUserEdited = true; semester = it },
                                                    hindSiliguri = hindSiliguri,
                                                    testTag = "edit_semester_select",
                                                )
                                            }
                                        )
                                    }
                                    add(
                                        1 to {
                                            GoogleSelectField(
                                                label = "শিফট".localized(),
                                                icon = ClockIcon,
                                                value = shift,
                                                placeholder = "শিফট নির্বাচন করুন".localized(),
                                                options = shiftOptions,
                                                translateOptions = true,
                                                onSelect = { hasUserEdited = true; shift = it },
                                                hindSiliguri = hindSiliguri,
                                                testTag = "edit_shift_select",
                                            )
                                        }
                                    )
                                    if (showFullAcademicFields) {
                                        add(
                                            1 to {
                                                GoogleSelectField(
                                                    label = "সেকশন (Section)".localized(),
                                                    icon = UsersRectangleIcon,
                                                    value = section,
                                                    placeholder = "সেকশন নির্বাচন করুন".localized(),
                                                    options = sectionOptions,
                                                    translateOptions = false,
                                                    onSelect = { hasUserEdited = true; section = it },
                                                    hindSiliguri = hindSiliguri,
                                                    testTag = "edit_section_select",
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
                color = cs.surface,
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
                            contentColor = cs.onSurface,
                            modifier = Modifier.weight(1f),
                            onClick = onViewProfileClick,
                        )

                        Button(
                            onClick = { submitForm() },
                            enabled = !isSaving,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("edit_profile_save_button"),
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = cs.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = CheckIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সংরক্ষণ করুন".localized(),
                                    fontSize = 14.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
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
            fontSize = 15.sp,
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
                fontSize = 15.sp,
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
    options: List<String>,
    translateOptions: Boolean,
    onSelect: (String) -> Unit,
    hindSiliguri: FontFamily,
    testTag: String,
) {
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val fieldWidthDp = with(density) { fieldWidthPx.toDp() }

    val displayedValue = if (value.isNotBlank()) {
        if (translateOptions) value.localized() else value
    } else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { fieldWidthPx = it.width },
    ) {
        GooglePickerField(
            label = label,
            leadingIcon = icon,
            trailingIcon = ChevronDownIcon,
            value = displayedValue,
            placeholder = placeholder,
            hindSiliguri = hindSiliguri,
            testTag = testTag,
            onClick = { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = cs.surfaceContainerHigh,
            modifier = Modifier.then(if (fieldWidthDp > 0.dp) Modifier.width(fieldWidthDp) else Modifier),
        ) {
            options.forEach { option ->
                val optionLabel = if (translateOptions) option.localized() else option
                val isSelected = option.equals(value, ignoreCase = true)
                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Text(
                            text = optionLabel,
                            fontFamily = hindSiliguri,
                            fontSize = 14.5.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) cs.primary else cs.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
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
