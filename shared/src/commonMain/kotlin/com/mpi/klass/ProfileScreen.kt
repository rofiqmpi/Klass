package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CameraIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.IdCardIcon
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.LogoutIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.PrayingHandsIcon
import com.mpi.klass.theme.RingIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserTieIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userName: String,
    role: UserRole?,
    isDark: Boolean,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSignOut: () -> Unit,
    hindSiliguri: FontFamily,
    email: String = "",
    photoUrl: String? = null,
    onUpdateProfile: ((newName: String, newPhotoUrl: String?) -> Unit)? = null,
    onCategoryChanged: ((UserRole) -> Unit)? = null,
    onOpenTeachingPrefs: (() -> Unit)? = null,
    topContentPadding: Dp = 0.dp,
) {
    if (role == null) {
        GuestSignInPrompt(
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onSignInClick = onSignOut,
        )
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val currentLang = LocalAppLanguage.current
    val cleanEmail = email.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()

    var isEditingMode by remember { mutableStateOf(false) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var currentPhotoUrl by remember(cleanEmail, photoUrl) { mutableStateOf(photoUrl ?: "") }

    var profileData by remember(cleanEmail) {
        val cached = if (cleanEmail.isNotBlank()) fetchLocalStudentProfile(cleanEmail) else null
        mutableStateOf(
            cached?.copy(
                name = cached.name.ifBlank { userName },
                photoUrl = cached.photoUrl?.ifBlank { null } ?: photoUrl,
                contactEmail = cached.contactEmail.ifBlank { cleanEmail },
            ) ?: StudentProfile(
                email = cleanEmail,
                name = userName,
                photoUrl = photoUrl,
                contactEmail = cleanEmail,
            )
        )
    }

    val launchPhotoPicker = rememberPhotoPickerLauncher { pickedUri ->
        if (pickedUri.isNotBlank()) {
            currentPhotoUrl = pickedUri
            val updated = profileData.copy(photoUrl = pickedUri)
            profileData = updated
            val activeName = updated.name.ifBlank { userName }
            coroutineScope.launch {
                val result = RoleConfig.saveStudentProfile(updated)
                onUpdateProfile?.invoke(activeName, pickedUri)
                if (result.isSuccess) {
                    showToast(AppStrings.translate("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!", currentLang))
                } else {
                    showToast(AppStrings.translate("তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।", currentLang))
                }
            }
        }
    }

    val locationLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank() && realAddress != profileData.address && cleanEmail.isNotBlank()) {
            profileData = profileData.copy(address = realAddress)
            coroutineScope.launch {
                RoleConfig.saveStudentAddress(cleanEmail, realAddress)
            }
        }
    }

    LaunchedEffect(cleanEmail, role) {
        if (cleanEmail.isNotBlank()) {
            isLoadingProfile = true
            val fetched = RoleConfig.fetchStudentProfile(cleanEmail)
            if (fetched != null) {
                val savedPhoto = fetched.photoUrl?.ifBlank { null }
                val resolvedPhoto = when {
                    savedPhoto != null && savedPhoto.startsWith("data:image", ignoreCase = true) -> savedPhoto
                    !savedPhoto.isNullOrBlank() -> savedPhoto
                    else -> currentPhotoUrl.ifBlank { photoUrl }
                }
                if (!resolvedPhoto.isNullOrBlank()) {
                    currentPhotoUrl = resolvedPhoto
                }
                profileData = fetched.copy(
                    name = fetched.name.ifBlank { userName },
                    photoUrl = resolvedPhoto,
                    contactEmail = fetched.contactEmail.ifBlank { cleanEmail },
                )
            } else {
                profileData = StudentProfile(
                    email = cleanEmail,
                    name = userName,
                    photoUrl = currentPhotoUrl.ifBlank { photoUrl },
                    contactEmail = cleanEmail,
                )
            }
            isLoadingProfile = false
            locationLauncher()
        } else {
            isLoadingProfile = false
        }
    }

    AppBackHandler(enabled = isEditingMode) {
        isEditingMode = false
    }

    val activePhotoUrl = currentPhotoUrl.ifBlank { profileData.photoUrl ?: photoUrl ?: "" }.ifBlank { null }

    AnimatedContent(
        targetState = isEditingMode,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
        label = "ProfileViewEditTransition",
        modifier = Modifier.fillMaxSize(),
    ) { editing ->
        if (editing) {
            EditProfileContent(
                initialProfile = profileData,
                photoUrl = activePhotoUrl,
                fallbackName = userName,
                fallbackEmail = cleanEmail,
                role = role,
                hindSiliguri = hindSiliguri,
                topContentPadding = topContentPadding,
                isSaving = isSavingProfile,
                isDark = isDark,
                onAvatarClick = { launchPhotoPicker() },
                onViewProfileClick = { isEditingMode = false },
                onSaveProfile = { updated ->
                    coroutineScope.launch {
                        isSavingProfile = true
                        val preservedPhoto = activePhotoUrl ?: updated.photoUrl
                        val finalProfile = updated.copy(
                            photoUrl = preservedPhoto,
                            address = updated.address.ifBlank { profileData.address },
                        )
                        val result = RoleConfig.saveStudentProfile(finalProfile)
                        profileData = finalProfile
                        isSavingProfile = false
                        if (result.isSuccess) {
                            onUpdateProfile?.invoke(finalProfile.name, preservedPhoto)
                            showToast(AppStrings.translate("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!", currentLang))
                            isEditingMode = false
                        } else {
                            showToast(AppStrings.translate("তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।", currentLang))
                        }
                    }
                },
            )
        } else {
            ProfileViewContent(
                profile = profileData,
                photoUrl = activePhotoUrl,
                userName = userName,
                cleanEmail = cleanEmail,
                role = role,
                isLoading = isLoadingProfile,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                topContentPadding = topContentPadding,
                onAvatarClick = { launchPhotoPicker() },
                onEditClick = { isEditingMode = true },
                onSignOutClick = {
                    clearUserSession()
                    onSignOut()
                },
                onDeleteAccountClick = { showDeleteDialog = true },
                onOpenTeachingPrefs = onOpenTeachingPrefs,
            )
        }
    }

    if (showDeleteDialog) {
        val cs = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteDialog = false },
            containerColor = cs.surfaceContainerHigh,
            title = {
                Text(
                    text = "ডিলিট একাউন্ট".localized(),
                    color = cs.error,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                )
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্টটি চিরতরে ডিলিট করতে চান? এটি করলে আপনার সকল তথ্য মুছে যাবে।".localized(),
                    color = cs.onSurfaceVariant,
                    fontFamily = hindSiliguri,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isDeletingAccount) {
                            isDeletingAccount = true
                            coroutineScope.launch {
                                if (cleanEmail.isNotBlank()) {
                                    RoleConfig.deleteAccountFromFirebase(cleanEmail, role)
                                }
                                clearUserSession()
                                isDeletingAccount = false
                                showDeleteDialog = false
                                onSignOut()
                            }
                        }
                    },
                    enabled = !isDeletingAccount,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.error,
                        contentColor = cs.onError,
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_button"),
                ) {
                    Text(
                        text = if (isDeletingAccount) "মুছে ফেলা হচ্ছে...".localized() else "হ্যাঁ".localized(),
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeletingAccount,
                    modifier = Modifier.testTag("cancel_delete_account_button"),
                ) {
                    Text(
                        text = "বাতিল".localized(),
                        color = cs.onSurfaceVariant,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

private data class GoogleContactField(
    val label: String,
    val value: String,
    val isEmpty: Boolean,
    val icon: ImageVector,
    val isHighlight: Boolean = false,
)

@Composable
private fun ProfileViewContent(
    profile: StudentProfile,
    photoUrl: String?,
    userName: String,
    cleanEmail: String,
    role: UserRole,
    isLoading: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onOpenTeachingPrefs: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val displayName = profile.name.ifBlank { userName }
        .ifBlank { cleanEmail.substringBefore("@") }
        .ifBlank { role.defaultName }

    val roleSubtitle = when (role) {
        UserRole.STUDENT -> "শিক্ষার্থী প্রোফাইল"
        UserRole.TEACHER -> "শিক্ষক প্রোফাইল"
        UserRole.ADMIN -> "এডমিন প্রোফাইল"
    }.localized()

    val dobDisplay = formatProfileDob(profile.dob, isBengali)
    val emailDisplay = profile.contactEmail.ifBlank { cleanEmail }

    val academicFields = buildList {
        if (role != UserRole.ADMIN) {
            val deptValue = if (profile.department == "নন টেক") "নন টেক".localized() else profile.department
            if (deptValue.isNotBlank()) add(GoogleContactField("Department".localized(), deptValue, false, GraduationCapIcon))
            if (role == UserRole.STUDENT) {
                val isValidSem = profile.semester.isNotBlank() && !profile.semester.contains("৮ম") && !profile.semester.startsWith("8")
                if (isValidSem) add(GoogleContactField("সেমিস্টার".localized(), profile.semester.localized(), false, LayerGroupIcon))
            }
            if (profile.shift.isNotBlank()) add(GoogleContactField("শিফট".localized(), profile.shift.localized(), false, ClockIcon))
            if (role == UserRole.STUDENT) {
                if (profile.section.isNotBlank()) add(GoogleContactField("সেকশন (Section)".localized(), profile.section, false, UsersRectangleIcon))
                if (profile.rollNumber.isNotBlank()) add(GoogleContactField("বোর্ড রোল নম্বর (Roll)".localized(), profile.rollNumber, false, IdCardIcon))
                if (profile.registrationNumber.isNotBlank()) add(GoogleContactField("রেজিস্ট্রেশন নম্বর (Registration)".localized(), profile.registrationNumber, false, FileLinesIcon))
            }
        }
    }

    val contactFields = buildList {
        if (profile.phone.isNotBlank()) add(GoogleContactField("মোবাইল নম্বর".localized(), profile.phone, false, PhoneSolidIcon))
        if (emailDisplay.isNotBlank()) add(GoogleContactField("ইমেল ঠিকানা".localized(), emailDisplay, false, EnvelopeSolidIcon))
        if (profile.address.isNotBlank()) add(GoogleContactField("বর্তমান ঠিকানা".localized(), profile.address, false, LocationDotIcon))
        if (profile.permanentAddress.isNotBlank()) add(GoogleContactField("স্থায়ী ঠিকানা".localized(), profile.permanentAddress, false, LocationDotIcon))
    }

    val personalFields = buildList {
        if (profile.bloodGroup.isNotBlank()) add(GoogleContactField("রক্তের গ্রুপ".localized(), profile.bloodGroup, false, DropletIcon, isHighlight = true))
        if (dobDisplay.isNotBlank()) add(GoogleContactField("জন্ম তারিখ".localized(), dobDisplay, false, CalendarDaysIcon))
        if (profile.gender.isNotBlank()) add(GoogleContactField("লিঙ্গ".localized(), profile.gender.localized(), false, VenusMarsIcon))
        if (role != UserRole.ADMIN && profile.religion.isNotBlank()) add(GoogleContactField("ধর্ম".localized(), profile.religion.localized(), false, PrayingHandsIcon))
        if (role != UserRole.ADMIN && profile.maritalStatus.isNotBlank()) add(GoogleContactField("বৈবাহিক অবস্থা".localized(), profile.maritalStatus.localized(), false, RingIcon))
        if (role == UserRole.STUDENT && profile.fatherName.isNotBlank()) add(GoogleContactField("পিতার নাম".localized(), profile.fatherName, false, UserTieIcon))
        if (role == UserRole.STUDENT && profile.motherName.isNotBlank()) add(GoogleContactField("মাতার নাম".localized(), profile.motherName, false, UserNurseIcon))
    }

    MinimalBackdrop(isDark = isDark) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Clean Centered Hero
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        onClick = onAvatarClick,
                        shape = CircleShape,
                        color = cs.surfaceContainerHigh,
                        border = BorderStroke(0.75.dp, cs.outlineVariant),
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
                                seed = cleanEmail.ifBlank { displayName },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = cs.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Surface(
                        onClick = onAvatarClick,
                        shape = CircleShape,
                        color = cs.primaryContainer,
                        border = BorderStroke(1.5.dp, cs.background),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = CameraIcon,
                                contentDescription = "Change photo",
                                tint = cs.primary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = displayName,
                    color = cs.onSurface,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                Text(
                    text = if (emailDisplay.isNotBlank()) "$roleSubtitle · $emailDisplay" else roleSubtitle,
                    color = cs.onSurfaceVariant,
                    fontSize = 14.5.sp,
                    fontFamily = hindSiliguri,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WelcomeMinimal3DButton(
                        text = "এডিট করুন".localized(),
                        icon = EditIcon,
                        hindSiliguri = hindSiliguri,
                        isPrimaryAccent = true,
                        isDark = isDark,
                        height = 42.dp,
                        cornerRadius = 12.dp,
                        testTag = "profile_edit_button",
                        modifier = Modifier.widthIn(min = 136.dp),
                        onClick = onEditClick,
                    )

                    if (role == UserRole.TEACHER && onOpenTeachingPrefs != null) {
                        WelcomeMinimal3DButton(
                            text = "Subjects",
                            icon = GraduationCapIcon,
                            hindSiliguri = hindSiliguri,
                            isPrimaryAccent = false,
                            isDark = isDark,
                            height = 42.dp,
                            cornerRadius = 12.dp,
                            testTag = "profile_teaching_prefs_button",
                            modifier = Modifier.widthIn(min = 132.dp),
                            onClick = onOpenTeachingPrefs,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (academicFields.isNotEmpty()) {
                    SimpleProfileSection(
                        title = "Academic".localized(),
                        fields = academicFields,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (contactFields.isNotEmpty()) {
                    SimpleProfileSection(
                        title = "Contact".localized(),
                        fields = contactFields,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (personalFields.isNotEmpty()) {
                    SimpleProfileSection(
                        title = "Personal".localized(),
                        fields = personalFields,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (academicFields.isEmpty() && contactFields.size <= 1 && personalFields.isEmpty()) {
                    MinimalCardSurface(isDark = isDark, shape = RoundedCornerShape(14.dp)) {
                        Text(
                            text = "Tap Edit to complete your department, blood group, and contact info.",
                            fontSize = 15.sp,
                            fontFamily = hindSiliguri,
                            color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                GoogleOutlinedButton(
                    text = "লগআউট করুন".localized(),
                    icon = LogoutIcon,
                    hindSiliguri = hindSiliguri,
                    contentColor = cs.onSurface,
                    testTag = "profile_logout_button",
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp),
                    onClick = onSignOutClick,
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDeleteAccountClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("profile_delete_account_button"),
                ) {
                    Icon(
                        imageVector = TrashCanIcon,
                        contentDescription = null,
                        tint = cs.error,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ডিলিট একাউন্ট".localized(),
                        color = cs.error,
                        fontSize = 15.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleProfileSection(
    title: String,
    fields: List<GoogleContactField>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        GoogleSectionHeader(
            title = title,
            hindSiliguri = hindSiliguri,
        )
        Spacer(modifier = Modifier.height(4.dp))
        MinimalCardSurface(
            isDark = isDark,
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                fields.forEachIndexed { idx, field ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = field.label,
                            color = cs.onSurfaceVariant,
                            fontSize = 14.5.sp,
                            fontFamily = hindSiliguri,
                            modifier = Modifier.weight(0.9f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = field.value,
                            color = if (field.isHighlight) cs.error else cs.onSurface,
                            fontSize = 14.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.1f),
                        )
                    }
                    if (idx < fields.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = cs.outlineVariant.copy(alpha = 0.45f),
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestSignInPrompt(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSignInClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    MinimalBackdrop(isDark = isDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            MinimalCardSurface(
                isDark = isDark,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.widthIn(max = 360.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = PersonIcon,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(36.dp),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sign in to Klass",
                        color = cs.onSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = hindSiliguri,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "View your class routine, profile, and notifications.",
                        color = cs.onSurfaceVariant,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontFamily = hindSiliguri,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onSignInClick,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("guest_sign_in_button"),
                    ) {
                        Text(
                            text = "সাইন ইন করুন".localized(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = hindSiliguri,
                        )
                    }
                }
            }
        }
    }
}
