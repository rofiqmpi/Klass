package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mpi.klass.theme.CalendarDaysIcon
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
import com.mpi.klass.theme.SendIcon
import com.mpi.klass.theme.SolidUserIcon
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserTieIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import kotlin.math.roundToInt

// Exact HTML design palette & tokens
internal object HtmlProfileColors {
    val BsPrimary = Color(0xFF0D6EFD)
    val BsSuccess = Color(0xFF198754)
    val BsDanger = Color(0xFFDC3545)
    val BsWarning = Color(0xFFFFC107)
    val BsInfo = Color(0xFF0DCAF0)
    val BsSecondary = Color(0xFF6C757D)

    val PurpleAccent = Color(0xFF7928CA)
    val PinkAccent = Color(0xFFFF758C)
    val HeadingDark = Color(0xFF1A202C)
    val SubheadingSlate = Color(0xFF4A5568)
    val FormLabelColor = Color(0xFF34495E)
    val InfoLabelGray = Color(0xFF718096)
    val InputBorderGray = Color(0xFFE0E0E0)
    val CardBorderGray = Color(0xFFEDF2F7)
    val SubmitPink = Color(0xFFF857A6)
    val SubmitRed = Color(0xFFFF5858)
}

@Composable
internal fun HtmlProfilePageWrapper(
    topContentPadding: Dp,
    horizontalPadding: Dp = 32.dp,
    brandBar: @Composable (collapseProgress: Float) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bgBrush = remember {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFF9A9E),
                0.20f to Color(0xFFFAD0C4),
                0.40f to Color(0xFFFFD1FF),
                0.60f to Color(0xFFA1C4FD),
                0.80f to Color(0xFFC2E9FB),
                1.0f to Color(0xFFD4FC79),
            ),
            start = Offset.Zero,
            end = Offset(1200f, 1800f),
        )
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val collapseDistancePx = with(density) { 140.dp.toPx() }
    val collapseProgress by remember(collapseDistancePx) {
        derivedStateOf {
            (scrollState.value / collapseDistancePx).coerceIn(0f, 1f)
        }
    }

    val startTopOffset = if (topContentPadding > 0.dp) topContentPadding else 20.dp
    val endTopOffset = (startTopOffset - 22.dp).coerceAtLeast(8.dp)
    val animatedTopOffset = lerp(startTopOffset, endTopOffset, collapseProgress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.92f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding + 88.dp))
                } else {
                    Spacer(modifier = Modifier.height(104.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    content()
                }
            }

            // Floating Brand Card Bar that moves toward the transparent header & collapses to 1:1 avatar on scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .offset(y = animatedTopOffset)
                    .zIndex(5f),
                contentAlignment = Alignment.TopCenter,
            ) {
                brandBar(collapseProgress)
            }
        }
    }
}

@Composable
internal fun HtmlBrandCardBar(
    photoUrl: String?,
    avatarSeed: String,
    buttonText: String,
    buttonIcon: ImageVector,
    hindSiliguri: FontFamily,
    testTag: String,
    collapseProgress: Float = 0f,
    onAvatarClick: () -> Unit,
    onButtonClick: () -> Unit,
) {
    val clampedProgress = collapseProgress.coerceIn(0f, 1f)
    val avatarSize = lerp(55.dp, 46.dp, clampedProgress)
    val barPadding = 8.dp // Equal padding on all 4 sides

    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = lerp(10.dp, 14.dp, clampedProgress),
        border = BorderStroke(2.dp, HtmlProfileColors.PurpleAccent.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(all = barPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onAvatarClick,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(avatarSize)
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
                            .padding(2.dp)
                            .clip(CircleShape),
                    )
                } else {
                    DynamicAvatar(
                        seed = avatarSeed.ifBlank { "MPI" },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape),
                    )
                }
            }

            // Right button + gap container smoothly shrinks horizontally to 0 width on scroll
            val revealFraction = 1f - clampedProgress
            val buttonAlpha = (1f - clampedProgress * 1.6f).coerceIn(0f, 1f)
            val buttonScale = 1f - clampedProgress * 0.25f
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minWidth = 0))
                        val visibleWidth = (placeable.width * revealFraction).roundToInt()
                        layout(visibleWidth, placeable.height) {
                            if (visibleWidth > 0) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                    }
                    .graphicsLayer(
                        alpha = buttonAlpha,
                        scaleX = buttonScale,
                        scaleY = buttonScale,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    Surface(
                        onClick = onButtonClick,
                        enabled = clampedProgress < 0.85f,
                        shape = RoundedCornerShape(30.dp),
                        color = Color.Transparent,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag(testTag),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(HtmlProfileColors.PurpleAccent, HtmlProfileColors.PinkAccent),
                                        start = Offset.Zero,
                                        end = Offset(300f, 120f),
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = buttonIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = buttonText,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
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
internal fun HtmlSectionTitle(
    title: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 18.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(HtmlProfileColors.PurpleAccent.copy(alpha = 0.08f), Color.Transparent),
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(HtmlProfileColors.PinkAccent),
        )
        Text(
            text = title,
            color = HtmlProfileColors.PurpleAccent,
            fontSize = 19.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        )
    }
}

@Composable
internal fun HtmlGradientSubmitButton(
    text: String,
    icon: ImageVector,
    hindSiliguri: FontFamily,
    testTag: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = { if (enabled) onClick() },
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .heightIn(min = 54.dp)
                .testTag(testTag),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                HtmlProfileColors.SubmitPink,
                                HtmlProfileColors.SubmitRed,
                                HtmlProfileColors.PurpleAccent,
                            ),
                            start = Offset.Zero,
                            end = Offset(800f, 300f),
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

@Composable
internal fun HtmlProfileFooter(hindSiliguri: FontFamily) {
    Text(
        text = "© 2026 Mymensingh Polytechnic Institute | Designed by Klass",
        color = HtmlProfileColors.SubheadingSlate,
        fontSize = 14.sp,
        fontFamily = hindSiliguri,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp, bottom = 28.dp),
    )
}

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
        "8" -> "৮ম সেমিস্টার"
        else -> when {
            clean.contains("১ম") || clean.startsWith("1", true) -> "১ম সেমিস্টার"
            clean.contains("২য়") || clean.contains("২য়") || clean.startsWith("2", true) -> "২য় সেমিস্টার"
            clean.contains("৩য়") || clean.contains("৩য়") || clean.startsWith("3", true) -> "৩য় সেমিস্টার"
            clean.contains("৪র্থ") || clean.startsWith("4", true) -> "৪র্থ সেমিস্টার"
            clean.contains("৫ম") || clean.startsWith("5", true) -> "৫ম সেমিস্টার"
            clean.contains("৬ষ্ঠ") || clean.startsWith("6", true) -> "৬ষ্ঠ সেমিস্টার"
            clean.contains("৭ম") || clean.startsWith("7", true) -> "৭ম সেমিস্টার"
            clean.contains("৮ম") || clean.startsWith("8", true) -> "৮ম সেমিস্টার"
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

private fun normalizeSectionOption(raw: String): String {
    val clean = raw.trim()
    if (clean.isBlank()) return ""
    return when {
        clean.equals("A", true) || clean.equals("Section A", true) -> "Section A"
        clean.equals("B", true) || clean.equals("Section B", true) -> "Section B"
        clean.equals("C", true) || clean.equals("Section C", true) -> "Section C"
        else -> clean
    }
}

@Composable
fun EditProfileContent(
    initialProfile: StudentProfile,
    photoUrl: String?,
    fallbackName: String,
    fallbackEmail: String,
    hindSiliguri: FontFamily,
    topContentPadding: Dp,
    isSaving: Boolean,
    onAvatarClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    onSaveProfile: (StudentProfile) -> Unit,
) {
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI

    var hasUserEdited by remember(fallbackEmail) { mutableStateOf(false) }
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
    var section by remember(fallbackEmail) { mutableStateOf(normalizeSectionOption(initialProfile.section)) }
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
            department = normalizeDepartmentOption(initialProfile.department)
            semester = normalizeSemesterOption(initialProfile.semester)
            shift = normalizeShiftOption(initialProfile.shift)
            section = normalizeSectionOption(initialProfile.section)
            rollNumber = initialProfile.rollNumber
            registrationNumber = initialProfile.registrationNumber
        }
    }

    // Live GPS tracking updates Current Address automatically
    LaunchedEffect(initialProfile.address) {
        if (initialProfile.address.isNotBlank()) {
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
    val deptOptions = listOf(
        "CST (Computer Science & Technology)",
        "CT (Civil Technology)",
        "ET (Electrical Technology)",
        "MT (Mechanical Technology)",
        "PT (Power Technology)",
        "ENT (Electronics Technology)",
        "EMT (Electro-Medical Technology)",
    )
    val semesterOptions = listOf(
        "১ম সেমিস্টার", "২য় সেমিস্টার", "৩য় সেমিস্টার", "৪র্থ সেমিস্টার",
        "৫ম সেমিস্টার", "৬ষ্ঠ সেমিস্টার", "৭ম সেমিস্টার", "৮ম সেমিস্টার",
    )
    val shiftOptions = listOf("১ম শিফট", "২য় শিফট")
    val sectionOptions = listOf("Section A", "Section B", "Section C")

    HtmlProfilePageWrapper(
        topContentPadding = topContentPadding,
        brandBar = { collapseProgress ->
            HtmlBrandCardBar(
                photoUrl = photoUrl,
                avatarSeed = fallbackEmail.ifBlank { fullName },
                buttonText = "প্রোফাইল দেখুন".localized(),
                buttonIcon = DatabaseIcon,
                hindSiliguri = hindSiliguri,
                testTag = "edit_to_view_profile_button",
                collapseProgress = collapseProgress,
                onAvatarClick = onAvatarClick,
                onButtonClick = onViewProfileClick,
            )
        },
    ) {
        // Header Section Titles
        Text(
            text = "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট".localized(),
            color = HtmlProfileColors.HeadingDark,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "আপনার প্রোফাইল সম্পাদনা করুন".localized(),
            color = HtmlProfileColors.SubheadingSlate,
            fontSize = 17.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main Responsive Form Content
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth >= 840.dp -> 3
                maxWidth >= 560.dp -> 2
                else -> 1
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 1: Personal Information
                HtmlSectionTitle(
                    title = "১. ব্যক্তিগত তথ্য (Personal Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val personalItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    cols to {
                        HtmlFormTextField(
                            label = "শিক্ষার্থীর পূর্ণ নাম (Full Name)".localized(),
                            icon = SolidUserIcon,
                            iconTint = HtmlProfileColors.BsPrimary,
                            value = fullName,
                            placeholder = "আপনার পুরো নাম লিখুন".localized(),
                            onValueChange = { hasUserEdited = true; fullName = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_fullname_input",
                        )
                    },
                    1 to {
                        val dobDisplay = if (dob.isNotBlank()) formatProfileDob(dob, isBengali) else ""
                        HtmlFormDatePickerField(
                            label = "জন্ম তারিখ".localized(),
                            icon = CalendarDaysIcon,
                            iconTint = HtmlProfileColors.BsWarning,
                            value = dobDisplay,
                            placeholder = "mm/dd/yyyy",
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_dob_picker",
                            onClick = launchDatePicker,
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "রক্তের গ্রুপ".localized(),
                            icon = DropletIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = bloodGroup,
                            placeholder = "গ্রুপ নির্বাচন করুন".localized(),
                            options = bloodOptions,
                            translateOptions = false,
                            onSelect = { hasUserEdited = true; bloodGroup = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_blood_group_select",
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "ধর্ম".localized(),
                            icon = PrayingHandsIcon,
                            iconTint = HtmlProfileColors.BsInfo,
                            value = religion,
                            placeholder = "ধর্ম নির্বাচন করুন".localized(),
                            options = religionOptions,
                            translateOptions = true,
                            onSelect = { hasUserEdited = true; religion = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_religion_select",
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "লিঙ্গ (Gender)".localized(),
                            icon = VenusMarsIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = gender,
                            placeholder = "লিঙ্গ নির্বাচন করুন".localized(),
                            options = genderOptions,
                            translateOptions = true,
                            onSelect = { hasUserEdited = true; gender = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_gender_select",
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "বৈবাহিক অবস্থা".localized(),
                            icon = RingIcon,
                            iconTint = HtmlProfileColors.BsSecondary,
                            value = maritalStatus,
                            placeholder = "অবিবাহিত (Unmarried)".localized(),
                            options = maritalOptions,
                            translateOptions = true,
                            onSelect = { hasUserEdited = true; maritalStatus = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_marital_select",
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = personalItems, spacing = 16.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 2: Family Information
                HtmlSectionTitle(
                    title = "২. পারিবারিক তথ্য (Family Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val familyItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    1 to {
                        HtmlFormTextField(
                            label = "পিতার নাম (Father's Name)".localized(),
                            icon = UserTieIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            value = fatherName,
                            placeholder = "পিতার নাম লিখুন".localized(),
                            onValueChange = { hasUserEdited = true; fatherName = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_father_name_input",
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "মাতার নাম (Mother's Name)".localized(),
                            icon = UserNurseIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = motherName,
                            placeholder = "মাতার নাম লিখুন".localized(),
                            onValueChange = { hasUserEdited = true; motherName = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_mother_name_input",
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = familyItems, spacing = 16.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 3: Contact Information
                HtmlSectionTitle(
                    title = "৩. যোগাযোগের তথ্য (Contact Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val contactItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    1 to {
                        HtmlFormTextField(
                            label = "মোবাইল নম্বর".localized(),
                            icon = PhoneSolidIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            value = phone,
                            placeholder = "01XXXXXXXXX",
                            keyboardType = KeyboardType.Phone,
                            onValueChange = { hasUserEdited = true; phone = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_phone_input",
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "ইমেল ঠিকানা (Email)".localized(),
                            icon = EnvelopeSolidIcon,
                            iconTint = HtmlProfileColors.BsPrimary,
                            value = contactEmail,
                            placeholder = "example@gmail.com",
                            keyboardType = KeyboardType.Email,
                            onValueChange = { hasUserEdited = true; contactEmail = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_email_input",
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = contactItems, spacing = 16.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 4: Address Information
                HtmlSectionTitle(
                    title = "৪. ঠিকানার তথ্য (Address Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val addressItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    1 to {
                        HtmlFormTextField(
                            label = "বর্তমান ঠিকানা".localized(),
                            icon = LocationDotIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = currentAddress,
                            placeholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized(),
                            onValueChange = { hasUserEdited = true; currentAddress = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_current_address_input",
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "স্থায়ী ঠিকানা".localized(),
                            icon = LocationDotIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = permanentAddress,
                            placeholder = "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা".localized(),
                            onValueChange = { hasUserEdited = true; permanentAddress = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_permanent_address_input",
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = addressItems, spacing = 16.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 5: Academic Information
                HtmlSectionTitle(
                    title = "৫. একাডেমিক তথ্য (Academic Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val academicItems: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    1 to {
                        HtmlFormSelectField(
                            label = "ডিপার্টমেন্ট (Department)".localized(),
                            icon = GraduationCapIcon,
                            iconTint = HtmlProfileColors.BsPrimary,
                            value = department,
                            placeholder = "ডিপার্টমেন্ট নির্বাচন করুন".localized(),
                            options = deptOptions,
                            translateOptions = false,
                            onSelect = { hasUserEdited = true; department = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_department_select",
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "সেমিস্টার".localized(),
                            icon = LayerGroupIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            value = semester,
                            placeholder = "সেমিস্টার নির্বাচন করুন".localized(),
                            options = semesterOptions,
                            translateOptions = true,
                            onSelect = { hasUserEdited = true; semester = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_semester_select",
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "শিফট".localized(),
                            icon = ClockIcon,
                            iconTint = HtmlProfileColors.BsWarning,
                            value = shift,
                            placeholder = "শিফট নির্বাচন করুন".localized(),
                            options = shiftOptions,
                            translateOptions = true,
                            onSelect = { hasUserEdited = true; shift = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_shift_select",
                        )
                    },
                    1 to {
                        HtmlFormSelectField(
                            label = "সেকশন (Section)".localized(),
                            icon = UsersRectangleIcon,
                            iconTint = HtmlProfileColors.BsSecondary,
                            value = section,
                            placeholder = "সেকশন নির্বাচন করুন".localized(),
                            options = sectionOptions,
                            translateOptions = false,
                            onSelect = { hasUserEdited = true; section = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_section_select",
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "বোর্ড রোল নম্বর (Roll)".localized(),
                            icon = IdCardIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            value = rollNumber,
                            placeholder = "যেমন: ৬৪৮১২৩".localized(),
                            onValueChange = { hasUserEdited = true; rollNumber = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_roll_input",
                        )
                    },
                    1 to {
                        HtmlFormTextField(
                            label = "রেজিস্ট্রেশন নম্বর (Registration)".localized(),
                            icon = FileLinesIcon,
                            iconTint = HtmlProfileColors.BsInfo,
                            value = registrationNumber,
                            placeholder = "যেমন: ১৭৫৬৪২".localized(),
                            onValueChange = { hasUserEdited = true; registrationNumber = it },
                            hindSiliguri = hindSiliguri,
                            testTag = "edit_registration_input",
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = academicItems, spacing = 16.dp)
            }
        }

        Spacer(modifier = Modifier.height(34.dp))

        // Submit Button (.btn-submit)
        HtmlGradientSubmitButton(
            text = if (isSaving) "সংরক্ষণ করা হচ্ছে...".localized() else "সংরক্ষণ করুন".localized(),
            icon = SendIcon,
            hindSiliguri = hindSiliguri,
            testTag = "edit_profile_save_button",
            enabled = !isSaving,
            onClick = {
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
            },
        )

        HtmlProfileFooter(hindSiliguri = hindSiliguri)
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

    // Pack items into rows based on column spans
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
private fun HtmlFieldLabel(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    hindSiliguri: FontFamily,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 15.5.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.SemiBold,
            color = HtmlProfileColors.FormLabelColor,
        )
    }
}

@Composable
private fun HtmlFormTextField(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    hindSiliguri: FontFamily,
    testTag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) HtmlProfileColors.PurpleAccent else HtmlProfileColors.InputBorderGray

    Column(modifier = Modifier.fillMaxWidth()) {
        HtmlFieldLabel(label = label, icon = icon, iconTint = iconTint, hindSiliguri = hindSiliguri)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = HtmlProfileColors.HeadingDark,
                    fontSize = 15.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Normal,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(HtmlProfileColors.PurpleAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
                    .onFocusChanged { isFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontFamily = hindSiliguri,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun HtmlFormDatePickerField(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    hindSiliguri: FontFamily,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HtmlFieldLabel(label = label, icon = icon, iconTint = iconTint, hindSiliguri = hindSiliguri)

        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, HtmlProfileColors.InputBorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag(testTag),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    color = if (value.isNotBlank()) HtmlProfileColors.HeadingDark else Color(0xFF94A3B8),
                    fontSize = 15.5.sp,
                    fontFamily = hindSiliguri,
                )
                Icon(
                    imageVector = CalendarDaysIcon,
                    contentDescription = label,
                    tint = HtmlProfileColors.SubheadingSlate,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun HtmlFormSelectField(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    options: List<String>,
    translateOptions: Boolean,
    onSelect: (String) -> Unit,
    hindSiliguri: FontFamily,
    testTag: String,
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = if (expanded) HtmlProfileColors.PurpleAccent else HtmlProfileColors.InputBorderGray
    val displayedValue = if (value.isNotBlank()) {
        if (translateOptions) value.localized() else value
    } else ""

    Column(modifier = Modifier.fillMaxWidth()) {
        HtmlFieldLabel(label = label, icon = icon, iconTint = iconTint, hindSiliguri = hindSiliguri)

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(2.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag(testTag),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayedValue.ifBlank { placeholder },
                        color = if (displayedValue.isNotBlank()) HtmlProfileColors.HeadingDark else Color(0xFF64748B),
                        fontSize = 15.5.sp,
                        fontFamily = hindSiliguri,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = ChevronDownIcon,
                        contentDescription = null,
                        tint = HtmlProfileColors.SubheadingSlate,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                options.forEach { option ->
                    val optionLabel = if (translateOptions) option.localized() else option
                    val isSelected = option.equals(value, ignoreCase = true)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel,
                                fontFamily = hindSiliguri,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) HtmlProfileColors.PurpleAccent else HtmlProfileColors.HeadingDark,
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
}
