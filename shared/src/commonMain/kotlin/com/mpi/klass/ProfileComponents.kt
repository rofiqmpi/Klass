package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DropletIcon
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
import com.mpi.klass.theme.SolidUserIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import kotlinx.coroutines.launch

enum class ProfileField {
    NAME,
    BIO,
    DEPARTMENT,
    SEMESTER,
    SHIFT,
    SECTION,
    ROLL_NUMBER,
    REGISTRATION_NUMBER,
    PHONE,
    CONTACT_EMAIL,
    BLOOD_GROUP,
    ADDRESS,
    PERMANENT_ADDRESS,
    DOB,
    GENDER,
    RELIGION,
    MARITAL_STATUS,
}

data class ProfileSetupStep(
    val field: ProfileField,
    val title: String,
    val hint: String,
    val required: Boolean = true,
    val multiline: Boolean = false,
)

internal fun profileFieldMeta(
    field: ProfileField,
    role: UserRole,
    isDark: Boolean,
): Triple<String, String, Pair<ImageVector, Color>> {
    val profileAccent = KlassFeaturePalette.profile(isDark).primary
    val academicAccent = KlassFeaturePalette.routine(isDark).primary
    val contactAccent = KlassFeaturePalette.teachers(isDark).primary
    val bloodAccent = KlassFeaturePalette.blood(isDark).primary
    val personalAccent = KlassFeaturePalette.appearance(isDark).primary
    val locationAccent = KlassFeaturePalette.classrooms(isDark).primary

    return when (field) {
        ProfileField.NAME -> Triple(
            "আপনার নাম কী?",
            "প্রোফাইলে যে পূর্ণ নাম দেখানো হবে তা লিখুন",
            SolidUserIcon to profileAccent,
        )
        ProfileField.BIO -> Triple(
            "আপনার সম্পর্কে",
            "আপনার পরিচিতি বা সংক্ষিপ্ত বায়ো লিখুন",
            HandWaveIcon to profileAccent,
        )
        ProfileField.DEPARTMENT -> Triple(
            "আপনার বিভাগ কোনটি?",
            "আপনার টেকনোলজি বা বিভাগ নির্বাচন করুন",
            GraduationCapIcon to academicAccent,
        )
        ProfileField.SEMESTER -> Triple(
            "আপনি কোন সেমিস্টারে পড়েন?",
            "আপনার চলমান সেমিস্টার নির্বাচন করুন",
            LayerGroupIcon to academicAccent,
        )
        ProfileField.SHIFT -> Triple(
            "আপনার শিফট কোনটি?",
            "১ম বা ২য় শিফট নির্বাচন করুন",
            ClockIcon to academicAccent,
        )
        ProfileField.SECTION -> Triple(
            "আপনার গ্রুপ বা সেকশন কোনটি?",
            "আপনার ক্লাস গ্রুপ নির্বাচন করুন",
            UsersRectangleIcon to academicAccent,
        )
        ProfileField.ROLL_NUMBER -> Triple(
            "আপনার রোল নম্বর",
            "বোর্ড রোল নম্বর লিখুন (যেমন: 648123)",
            IdCardIcon to academicAccent,
        )
        ProfileField.REGISTRATION_NUMBER -> Triple(
            "রেজিস্ট্রেশন নম্বর",
            "বোর্ড রেজিস্ট্রেশন নম্বর লিখুন",
            FileLinesIcon to academicAccent,
        )
        ProfileField.PHONE -> Triple(
            "ফোন নম্বর",
            "সচল মোবাইল নম্বর লিখুন (যেমন: 017XXXXXXXX)",
            PhoneSolidIcon to contactAccent,
        )
        ProfileField.CONTACT_EMAIL -> Triple(
            "যোগাযোগের ইমেইল",
            if (role == UserRole.TEACHER) "সহকর্মী ও শিক্ষার্থীদের যোগাযোগের ইমেইল" else "যোগাযোগের জন্য সচল ইমেইল লিখুন",
            EnvelopeSolidIcon to contactAccent,
        )
        ProfileField.BLOOD_GROUP -> Triple(
            "রক্তের গ্রুপ",
            "আপনার রক্তের গ্রুপ নির্বাচন করুন",
            DropletIcon to bloodAccent,
        )
        ProfileField.ADDRESS -> Triple(
            "বর্তমান ঠিকানা",
            "গ্রাম/মহল্লা, উপজেলা, জেলা লিখুন বা GPS ব্যবহার করুন",
            LocationDotIcon to locationAccent,
        )
        ProfileField.PERMANENT_ADDRESS -> Triple(
            "স্থায়ী ঠিকানা",
            "আপনার স্থায়ী ঠিকানা লিখুন",
            HomeIcon to locationAccent,
        )
        ProfileField.DOB -> Triple(
            "জন্মতারিখ কবে?",
            "চাকা ঘুরিয়ে আপনার জন্মতারিখ নির্ধারণ করুন",
            CalendarDaysIcon to personalAccent,
        )
        ProfileField.GENDER -> Triple(
            "লিঙ্গ",
            "আপনার লিঙ্গ নির্বাচন করুন",
            VenusMarsIcon to personalAccent,
        )
        ProfileField.RELIGION -> Triple(
            "ধর্ম",
            "আপনার ধর্ম নির্বাচন করুন",
            PrayingHandsIcon to personalAccent,
        )
        ProfileField.MARITAL_STATUS -> Triple(
            "বৈবাহিক অবস্থা",
            "বর্তমান বৈবাহিক অবস্থা নির্বাচন করুন",
            RingIcon to personalAccent,
        )
    }
}

internal fun profileFieldChoices(
    field: ProfileField,
    role: UserRole,
    department: String,
): List<String> = when (field) {
    ProfileField.DEPARTMENT -> {
        val baseDepts = listOf(
            "CST (Computer Science & Technology)",
            "CT (Civil Technology)",
            "ET (Electrical Technology)",
            "MT (Mechanical Technology)",
            "PT (Power Technology)",
            "ENT (Electronics Technology)",
            "EMT (Electro-Medical Technology)",
        )
        if (role == UserRole.TEACHER) baseDepts + "নন টেক" else baseDepts
    }
    ProfileField.SEMESTER -> RoutineRules.ALL_SEMESTER_KEYS
    ProfileField.SHIFT -> listOf("১ম শিফট", "২য় শিফট")
    ProfileField.SECTION -> RoutineRules.groupsForDepartment(department)
    ProfileField.BLOOD_GROUP -> RoutineRules.BLOOD_GROUPS
    ProfileField.GENDER -> listOf("পুরুষ / Male", "নারী / Female", "অন্যান্য / Other")
    ProfileField.RELIGION -> listOf("ইসলাম", "হিন্দুধর্ম", "বৌদ্ধধর্ম", "খ্রিস্টধর্ম", "অন্যান্য")
    ProfileField.MARITAL_STATUS -> listOf("অবিবাহিত / Single", "বিবাহিত / Married", "অন্যান্য / Other")
    else -> emptyList()
}

internal fun profileFieldKey(field: ProfileField): String = when (field) {
    ProfileField.NAME -> "name"
    ProfileField.BIO -> "bio"
    ProfileField.DEPARTMENT -> "department"
    ProfileField.SEMESTER -> "semester"
    ProfileField.SHIFT -> "shift"
    ProfileField.SECTION -> "section"
    ProfileField.ROLL_NUMBER -> "roll_number"
    ProfileField.REGISTRATION_NUMBER -> "registration_number"
    ProfileField.PHONE -> "phone"
    ProfileField.CONTACT_EMAIL -> "contact_email"
    ProfileField.BLOOD_GROUP -> "blood_group"
    ProfileField.ADDRESS -> "address"
    ProfileField.PERMANENT_ADDRESS -> "permanent_address"
    ProfileField.DOB -> "dob"
    ProfileField.GENDER -> "gender"
    ProfileField.RELIGION -> "religion"
    ProfileField.MARITAL_STATUS -> "marital_status"
}

internal fun isProfileFieldMultiline(field: ProfileField): Boolean = when (field) {
    ProfileField.BIO, ProfileField.ADDRESS, ProfileField.PERMANENT_ADDRESS -> true
    else -> false
}

internal fun validateProfileField(
    field: ProfileField,
    value: String,
    isBengali: Boolean,
): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) {
        return if (isBengali) "এই তথ্যটি পূরণ করা আবশ্যক" else "This field is required"
    }
    return when (field) {
        ProfileField.PHONE -> {
            val digits = trimmed.filter { it.isDigit() }
            if (digits.length < 10) {
                if (isBengali) "সঠিক ১০-১১ সংখ্যার মোবাইল নম্বর লিখুন" else "Enter a valid phone number"
            } else {
                null
            }
        }
        ProfileField.CONTACT_EMAIL -> {
            val atIdx = trimmed.indexOf('@')
            val domain = if (atIdx >= 0) trimmed.substring(atIdx + 1) else ""
            if (atIdx <= 0 || !domain.contains('.')) {
                if (isBengali) "সঠিক ইমেইল ঠিকানা লিখুন" else "Enter a valid email address"
            } else {
                null
            }
        }
        ProfileField.DOB -> {
            val parts = trimmed.split("-")
            val valid = parts.size == 3 &&
                parts[0].toIntOrNull() != null &&
                parts[1].toIntOrNull() in 1..12 &&
                parts[2].toIntOrNull() in 1..31
            if (!valid) {
                if (isBengali) "সঠিক জন্মতারিখ নির্বাচন করুন" else "Please select a valid date of birth"
            } else {
                null
            }
        }
        else -> null
    }
}

// ── DOB Wheel Utilities ───────────────────────────────────────────────────────

internal fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

internal fun parseIsoDob(
    raw: String,
    minYear: Int = 1950,
    maxYear: Int = 2020,
): Triple<Int, Int, Int> {
    val clean = raw.trim()
    val parts = clean.split("-")
    val safeMax = maxYear.coerceAtLeast(minYear)
    val year = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(minYear, safeMax) ?: 2005.coerceIn(minYear, safeMax)
    val month = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 12) ?: 8
    val maxDay = daysInMonth(year, month)
    val day = parts.getOrNull(2)?.toIntOrNull()?.coerceIn(1, maxDay) ?: 15.coerceIn(1, maxDay)
    return Triple(year, month, day)
}

internal fun formatIsoDob(year: Int, month: Int, day: Int): String {
    val safeMonth = month.coerceIn(1, 12)
    val safeDay = day.coerceIn(1, daysInMonth(year, safeMonth))
    val y = year.toString().padStart(4, '0')
    val m = safeMonth.toString().padStart(2, '0')
    val d = safeDay.toString().padStart(2, '0')
    return "$y-$m-$d"
}

private fun toBnDigits(input: String): String {
    val map = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return buildString(input.length) {
        for (c in input) {
            append(if (c in '0'..'9') map[c - '0'] else c)
        }
    }
}

internal fun computeAgeFromDob(year: Int, month: Int, day: Int): Int {
    val (todayYear, todayMonth, todayDay) = currentYearMonthDay()
    var age = todayYear - year
    if (todayMonth < month || (todayMonth == month && todayDay < day)) {
        age -= 1
    }
    return age.coerceAtLeast(0)
}

/**
 * Facebook-inspired 3-column snapping drum Date of Birth selector (Day | Month | Year)
 * emitting standard `"YYYY-MM-DD"` dates cleanly across both Signup Setup and Edit Profile.
 */
@Composable
fun FacebookDobWheelPicker(
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val (todayYear, _, _) = remember { currentYearMonthDay() }
    val minYear = 1955
    val maxYear = (todayYear - 8).coerceAtLeast(2016)
    val years = remember(minYear, maxYear) { (minYear..maxYear).toList() }
    val monthsBn = remember {
        listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর",
        )
    }
    val monthsEn = remember {
        listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )
    }

    val initialTriple = remember { parseIsoDob(value, minYear = minYear, maxYear = maxYear) }
    var selectedYear by remember { mutableStateOf(initialTriple.first) }
    var selectedMonth by remember { mutableStateOf(initialTriple.second) }
    var selectedDay by remember { mutableStateOf(initialTriple.third) }

    // Ensure a valid initial ISO DOB is emitted once on open when raw value is empty
    LaunchedEffect(Unit) {
        val formatted = formatIsoDob(selectedYear, selectedMonth, selectedDay)
        if (value.trim() != formatted) {
            onValueChange(formatted)
        }
    }

    val maxDaysForMonth = remember(selectedYear, selectedMonth) {
        daysInMonth(selectedYear, selectedMonth)
    }
    val days = remember(maxDaysForMonth) { (1..maxDaysForMonth).toList() }

    LaunchedEffect(maxDaysForMonth) {
        if (selectedDay > maxDaysForMonth) {
            selectedDay = maxDaysForMonth
            onValueChange(formatIsoDob(selectedYear, selectedMonth, selectedDay))
        }
    }

    val cardBg = if (isDark) {
        cs.surfaceContainerLow.copy(alpha = 0.78f)
    } else {
        Color.White.copy(alpha = 0.86f)
    }
    val bandBg = accent.copy(alpha = if (isDark) 0.22f else 0.12f)
    val bandBorder = accent.copy(alpha = if (isDark) 0.48f else 0.34f)
    val fadeTop = cardBg
    val itemHeightDp = 44.dp
    val age = computeAgeFromDob(selectedYear, selectedMonth, selectedDay)
    val formattedPreview = formatProfileDob(
        formatIsoDob(selectedYear, selectedMonth, selectedDay),
        isBengali,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("facebook_dob_wheel_picker"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = cardBg,
            border = BorderStroke(1.dp, accent.copy(alpha = if (isDark) 0.34f else 0.24f)),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (isBengali) "দিন" else "Day",
                        modifier = Modifier.weight(0.85f),
                        textAlign = TextAlign.Center,
                        color = accent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                    )
                    Text(
                        text = if (isBengali) "মাস" else "Month",
                        modifier = Modifier.weight(1.35f),
                        textAlign = TextAlign.Center,
                        color = accent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                    )
                    Text(
                        text = if (isBengali) "বছর" else "Year",
                        modifier = Modifier.weight(1.0f),
                        textAlign = TextAlign.Center,
                        color = accent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                    )
                }

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp * 5),
                    contentAlignment = Alignment.Center,
                ) {
                    // Center highlighted selection band (Facebook-style pill bar)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bandBg,
                        border = BorderStroke(1.dp, bandBorder),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp),
                    ) {}

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DobWheelColumn(
                            items = days,
                            selectedIndex = (selectedDay - 1).coerceIn(0, days.lastIndex),
                            itemLabel = { dayVal ->
                                val raw = dayVal.toString().padStart(2, '0')
                                if (isBengali) toBnDigits(raw) else raw
                            },
                            onIndexSelected = { idx ->
                                val newDay = days.getOrNull(idx) ?: return@DobWheelColumn
                                if (newDay != selectedDay) {
                                    selectedDay = newDay
                                    onValueChange(formatIsoDob(selectedYear, selectedMonth, selectedDay))
                                }
                            },
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier
                                .weight(0.85f)
                                .testTag("dob_wheel_day"),
                        )

                        val monthLabels = if (isBengali) monthsBn else monthsEn
                        DobWheelColumn(
                            items = (1..12).toList(),
                            selectedIndex = (selectedMonth - 1).coerceIn(0, 11),
                            itemLabel = { monthNum -> monthLabels[(monthNum - 1).coerceIn(0, 11)] },
                            onIndexSelected = { idx ->
                                val newMonth = idx + 1
                                if (newMonth != selectedMonth) {
                                    selectedMonth = newMonth
                                    val clampedDay = selectedDay.coerceIn(1, daysInMonth(selectedYear, newMonth))
                                    selectedDay = clampedDay
                                    onValueChange(formatIsoDob(selectedYear, newMonth, clampedDay))
                                }
                            },
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("dob_wheel_month"),
                        )

                        DobWheelColumn(
                            items = years,
                            selectedIndex = (selectedYear - minYear).coerceIn(0, years.lastIndex),
                            itemLabel = { yearVal ->
                                val raw = yearVal.toString()
                                if (isBengali) toBnDigits(raw) else raw
                            },
                            onIndexSelected = { idx ->
                                val newYear = years.getOrNull(idx) ?: return@DobWheelColumn
                                if (newYear != selectedYear) {
                                    selectedYear = newYear
                                    val clampedDay = selectedDay.coerceIn(1, daysInMonth(newYear, selectedMonth))
                                    selectedDay = clampedDay
                                    onValueChange(formatIsoDob(newYear, selectedMonth, clampedDay))
                                }
                            },
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier
                                .weight(1.0f)
                                .testTag("dob_wheel_year"),
                        )
                    }

                    // Top & Bottom gradient drum depth fade
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp * 1.4f)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(fadeTop, fadeTop.copy(alpha = 0f)),
                                )
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp * 1.4f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(fadeTop.copy(alpha = 0f), fadeTop),
                                )
                            ),
                    )
                }
            }
        }

        // Live formatted date & age readout pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bandBg,
            border = BorderStroke(0.75.dp, bandBorder),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.25f else 0.16f),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CalendarDaysIcon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedPreview,
                        color = cs.onSurface,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                    )
                    Text(
                        text = if (isBengali) "বয়স: ${toBnDigits(age.toString())} বছর" else "Age: $age years",
                        color = cs.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> DobWheelColumn(
    items: List<T>,
    selectedIndex: Int,
    itemLabel: (T) -> String,
    onIndexSelected: (Int) -> Unit,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val itemHeightDp = 44.dp
    val density = LocalDensity.current
    val itemHeightPx = remember(density) { with(density) { itemHeightDp.toPx() } }
    val safeInitial = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeInitial)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.size, selectedIndex) {
        val target = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != target) {
            listState.scrollToItem(target)
        }
    }

    val centeredIndex by remember(items.size, itemHeightPx) {
        derivedStateOf {
            val rawOffset = listState.firstVisibleItemScrollOffset
            val idx = listState.firstVisibleItemIndex + if (rawOffset > itemHeightPx * 0.5f) 1 else 0
            idx.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        }
    }

    LaunchedEffect(listState.isScrollInProgress, centeredIndex) {
        if (!listState.isScrollInProgress) {
            onIndexSelected(centeredIndex)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeightDp * 5),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Two top spacer slots so index 0 centers cleanly
        items(2) {
            Spacer(modifier = Modifier.height(itemHeightDp))
        }
        items(items.size) { idx ->
            val isCentered = idx == centeredIndex
            val distance = kotlin.math.abs(idx - centeredIndex)
            val textAlpha = when (distance) {
                0 -> 1f
                1 -> 0.58f
                else -> 0.30f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            listState.animateScrollToItem(idx)
                            onIndexSelected(idx)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = itemLabel(items[idx]),
                    color = cs.onSurface.copy(alpha = textAlpha),
                    fontFamily = hindSiliguri,
                    fontSize = if (isCentered) 18.sp else 14.5.sp,
                    fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // Two bottom spacer slots so last index centers cleanly
        items(2) {
            Spacer(modifier = Modifier.height(itemHeightDp))
        }
    }
}

/**
 * Shared input body used identically inside both Signup Step Wizard (`ProfileSetupScreen`)
 * and Edit Profile Sub-Screens (`ProfileStepEditorSubScreen`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileFieldInputBody(
    field: ProfileField,
    value: String,
    hint: String,
    role: UserRole,
    department: String,
    icon: ImageVector,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onValueChange: (String) -> Unit,
    onGpsClick: (() -> Unit)? = null,
    inputTestTag: String = "profile_step_input",
    showPrivacySelector: Boolean = false,
    privacy: ProfilePrivacy = ProfilePrivacy.PUBLIC,
    onPrivacyChange: (ProfilePrivacy) -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    val choices = remember(field, role, department) {
        profileFieldChoices(field, role, department)
    }
    val multiline = isProfileFieldMultiline(field)
    val surfaceBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.74f) else Color.White.copy(alpha = 0.86f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when {
            field == ProfileField.DOB -> {
                FacebookDobWheelPicker(
                    value = value,
                    onValueChange = onValueChange,
                    accent = accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }
            choices.isNotEmpty() -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    choices.forEach { choice ->
                        val selected = value.equals(choice, ignoreCase = true)
                        Surface(
                            onClick = { onValueChange(choice) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) {
                                accent.copy(alpha = if (isDark) 0.22f else 0.13f)
                            } else {
                                surfaceBg
                            },
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 0.75.dp,
                                color = if (selected) accent else cs.outlineVariant.copy(alpha = 0.65f),
                            ),
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_choice_${choice.take(12)}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) accent else accent.copy(alpha = if (isDark) 0.20f else 0.12f),
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (selected) CheckIcon else icon,
                                            contentDescription = null,
                                            tint = if (selected) Color.White else accent,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = choice.localized(),
                                    modifier = Modifier.weight(1f),
                                    color = cs.onSurface,
                                    fontFamily = hindSiliguri,
                                    fontSize = 16.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(inputTestTag),
                    placeholder = {
                        Text(
                            text = hint.localized(),
                            fontFamily = hindSiliguri,
                            color = cs.onSurfaceVariant,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(19.dp),
                        )
                    },
                    singleLine = !multiline,
                    minLines = if (multiline) 3 else 1,
                    maxLines = if (multiline) 6 else 1,
                    textStyle = TextStyle(
                        fontSize = 16.5.sp,
                        fontFamily = hindSiliguri,
                        color = cs.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (field) {
                            ProfileField.PHONE -> KeyboardType.Phone
                            ProfileField.CONTACT_EMAIL -> KeyboardType.Email
                            ProfileField.ROLL_NUMBER, ProfileField.REGISTRATION_NUMBER -> KeyboardType.Number
                            else -> KeyboardType.Text
                        },
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = cs.outlineVariant,
                        focusedContainerColor = surfaceBg,
                        unfocusedContainerColor = surfaceBg,
                        focusedLabelColor = accent,
                    ),
                )

                if (field == ProfileField.ADDRESS && onGpsClick != null) {
                    KlassTonalButton(
                        text = "অটো-লোকেশন (GPS)".localized(),
                        icon = LocationDotIcon,
                        accent = KlassFeaturePalette.classrooms(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 44.dp,
                        testTag = "edit_sync_gps_button",
                        onClick = onGpsClick,
                    )
                }
            }
        }

        if (showPrivacySelector && field != ProfileField.NAME) {
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
                        val pIcon = when (option) {
                            ProfilePrivacy.PUBLIC -> LanguageGlobeIcon
                            ProfilePrivacy.PRIVATE -> LockIcon
                        }
                        val pLabel = when (option) {
                            ProfilePrivacy.PUBLIC -> "পাবলিক".localized()
                            ProfilePrivacy.PRIVATE -> "প্রাইভেট".localized()
                        }
                        MinimalIconChip(
                            label = pLabel,
                            icon = pIcon,
                            selected = privacy == option,
                            accent = accent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.testTag("edit_privacy_${option.key.lowercase()}"),
                            onClick = { onPrivacyChange(option) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Edit Profile sub-screen mirroring the exact 1-question-per-screen Signup UX
 * so editing any profile attribute feels completely unified with onboarding.
 */
@Composable
internal fun ProfileStepEditorSubScreen(
    field: ProfileField,
    initialValue: String,
    initialPrivacy: ProfilePrivacy,
    role: UserRole,
    department: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onGpsSync: () -> Unit,
    onSave: (String, ProfilePrivacy) -> Unit,
    onDismiss: () -> Unit,
    inputTestTag: String = "edit_single_field_input",
) {
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val (titleRaw, hintRaw, iconAndAccent) = remember(field, role, isDark) {
        profileFieldMeta(field, role, isDark)
    }
    val icon = iconAndAccent.first
    val accent = iconAndAccent.second
    val badgeColor = accent.copy(alpha = if (isDark) 0.22f else 0.13f)

    var draftValue by remember(field, initialValue) { mutableStateOf(initialValue) }
    var draftPrivacy by remember(field, initialPrivacy) { mutableStateOf(initialPrivacy) }
    var errorMessage by remember(field) { mutableStateOf<String?>(null) }

    LaunchedEffect(initialValue) {
        if (field == ProfileField.ADDRESS && initialValue.isNotBlank()) {
            draftValue = initialValue
        }
    }

    fun trySave() {
        val validationError = validateProfileField(field, draftValue, isBengali)
        if (validationError != null) {
            errorMessage = validationError
            return
        }
        focusManager.clearFocus(force = true)
        onSave(draftValue.trim(), draftPrivacy)
        onDismiss()
    }

    AppBackHandler(enabled = true) {
        onDismiss()
    }

    MinimalBackdrop(isDark = isDark, focusAccent = accent) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusManager.clearFocus(force = true) },
        ) {
            // Top bar with Back & Save pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(58.dp)
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
                        contentDescription = "Back".localized(),
                        tint = cs.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }

                HeaderSavePillButton(
                    text = "সংরক্ষণ".localized(),
                    accent = accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    testTag = "edit_single_field_top_save",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp),
                    onClick = ::trySave,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 18.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeColor,
                    border = BorderStroke(0.75.dp, accent.copy(alpha = 0.36f)),
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "প্রোফাইল এডিট".localized(),
                            color = accent,
                            fontFamily = hindSiliguri,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = titleRaw.localized(),
                    color = cs.onSurface,
                    fontFamily = hindSiliguri,
                    fontSize = 27.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = hintRaw.localized(),
                    color = cs.onSurfaceVariant,
                    fontFamily = hindSiliguri,
                    fontSize = 15.sp,
                )

                Spacer(Modifier.height(26.dp))

                ProfileFieldInputBody(
                    field = field,
                    value = draftValue,
                    hint = hintRaw,
                    role = role,
                    department = department,
                    icon = icon,
                    accent = accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onValueChange = {
                        draftValue = it
                        errorMessage = null
                    },
                    onGpsClick = onGpsSync,
                    inputTestTag = inputTestTag,
                    showPrivacySelector = field != ProfileField.NAME,
                    privacy = draftPrivacy,
                    onPrivacyChange = { draftPrivacy = it },
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        color = cs.error,
                        fontFamily = hindSiliguri,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            ) {
                KlassPrimaryButton(
                    text = "সংরক্ষণ করুন".localized(),
                    icon = CheckIcon,
                    customPrimaryColor = accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 52.dp,
                    cornerRadius = 16.dp,
                    testTag = "edit_single_field_bottom_save",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = ::trySave,
                )
            }
        }
    }
}
