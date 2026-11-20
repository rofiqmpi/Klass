package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CameraIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.CopyIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.IdCardIcon
import com.mpi.klass.theme.KlassFeaturePalette
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
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
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
            if (profileData.address.isBlank()) {
                locationLauncher()
            }
        } else {
            isLoadingProfile = false
        }
    }

    var activeDetailCategory by remember { mutableStateOf<String?>(null) }
    var isEditPickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(activeDetailCategory, isEditPickerOpen) {
        onFullScreenOverlayChange(activeDetailCategory != null || isEditPickerOpen)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    AppBackHandler(enabled = (isEditingMode && !isEditPickerOpen) || activeDetailCategory != null) {
        if (activeDetailCategory != null) {
            activeDetailCategory = null
        } else if (isEditingMode && !isEditPickerOpen) {
            isEditingMode = false
        }
    }

    val activePhotoUrl = currentPhotoUrl.ifBlank { profileData.photoUrl ?: photoUrl ?: "" }.ifBlank { null }

    AnimatedContent(
        targetState = isEditingMode,
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
                onOverlayStateChange = { isEditPickerOpen = it },
                onAvatarClick = { launchPhotoPicker() },
                onViewProfileClick = {
                    isEditPickerOpen = false
                    isEditingMode = false
                },
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
                            isEditPickerOpen = false
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
                activeDetailCategory = activeDetailCategory,
                onSelectDetailCategory = { activeDetailCategory = it },
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
        KlassConfirmDialog(
            title = "ডিলিট একাউন্ট".localized(),
            message = "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্টটি চিরতরে ডিলিট করতে চান? এটি করলে আপনার সকল তথ্য মুছে যাবে।".localized(),
            icon = TrashCanIcon,
            accent = KlassFeaturePalette.error(isDark).primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            confirmText = if (isDeletingAccount) "মুছে ফেলা হচ্ছে...".localized() else "হ্যাঁ".localized(),
            cancelText = "বাতিল".localized(),
            isDestructive = true,
            confirmTestTag = "confirm_delete_account_button",
            cancelTestTag = "cancel_delete_account_button",
            onConfirm = {
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
            onDismiss = { if (!isDeletingAccount) showDeleteDialog = false },
        )
    }
}

private enum class ProfileFieldActionType {
    NONE,
    PHONE,
    EMAIL,
}

private data class GoogleContactField(
    val label: String,
    val value: String,
    val isEmpty: Boolean,
    val icon: ImageVector,
    val isHighlight: Boolean = false,
    val actionType: ProfileFieldActionType = ProfileFieldActionType.NONE,
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
    activeDetailCategory: String?,
    onSelectDetailCategory: (String?) -> Unit,
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
        if (profile.phone.isNotBlank()) add(GoogleContactField("মোবাইল নম্বর".localized(), profile.phone, false, PhoneSolidIcon, actionType = ProfileFieldActionType.PHONE))
        if (emailDisplay.isNotBlank()) add(GoogleContactField("ইমেল ঠিকানা".localized(), emailDisplay, false, EnvelopeSolidIcon, actionType = ProfileFieldActionType.EMAIL))
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

    val profileAccent = KlassFeaturePalette.profile(isDark)
    val teachingAccent = KlassFeaturePalette.teaching(isDark)
    val roleBadgeAccent = when (role) {
        UserRole.STUDENT -> KlassFeaturePalette.routine(isDark)
        UserRole.TEACHER -> KlassFeaturePalette.teaching(isDark)
        UserRole.ADMIN -> KlassFeaturePalette.admin(isDark)
    }

    MinimalBackdrop(isDark = isDark, focusAccent = profileAccent.primary) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 780.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                }

                // ── FACEBOOK / TELEGRAM HERO HEADER ──────────────────────────────────────
                SocialSurfaceBlock(
                    isDark = isDark,
                    showBottomHairline = true,
                    accentTint = profileAccent.primary,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.size(106.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                onClick = onAvatarClick,
                                shape = CircleShape,
                                color = cs.surfaceContainerHigh,
                                border = BorderStroke(3.dp, profileAccent.primary.copy(alpha = 0.72f)),
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
                                    color = profileAccent.primary,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Surface(
                                onClick = onAvatarClick,
                                shape = CircleShape,
                                color = profileAccent.primary,
                                border = BorderStroke(2.dp, cs.surfaceContainerLowest),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(34.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CameraIcon,
                                        contentDescription = "Change photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = displayName,
                            color = cs.onSurface,
                            fontSize = 24.sp,
                            lineHeight = 30.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = CircleShape,
                            color = roleBadgeAccent.softTint,
                            border = BorderStroke(0.75.dp, roleBadgeAccent.borderTint),
                        ) {
                            Text(
                                text = roleSubtitle,
                                color = roleBadgeAccent.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            )
                        }

                        if (emailDisplay.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = emailDisplay,
                                color = cs.onSurfaceVariant,
                                fontSize = 14.5.sp,
                                fontFamily = hindSiliguri,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            WelcomeMinimal3DButton(
                                text = "এডিট করুন".localized(),
                                icon = EditIcon,
                                hindSiliguri = hindSiliguri,
                                isDark = isDark,
                                customPrimaryColor = profileAccent.primary,
                                customBaseColor = profileAccent.deep,
                                height = 46.dp,
                                cornerRadius = 14.dp,
                                testTag = "profile_edit_button",
                                modifier = Modifier.weight(1f),
                                onClick = onEditClick,
                            )

                            if (role == UserRole.TEACHER && onOpenTeachingPrefs != null) {
                                WelcomeMinimal3DButton(
                                    text = "Subjects",
                                    icon = GraduationCapIcon,
                                    hindSiliguri = hindSiliguri,
                                    isDark = isDark,
                                    customPrimaryColor = teachingAccent.primary,
                                    customBaseColor = teachingAccent.deep,
                                    height = 46.dp,
                                    cornerRadius = 14.dp,
                                    testTag = "profile_teaching_prefs_button",
                                    modifier = Modifier.weight(1f),
                                    onClick = onOpenTeachingPrefs,
                                )
                            }
                        }
                    }
                }

                val academicSummary = academicFields.take(2).joinToString(" • ") { it.value }.ifBlank { null }
                val contactSummary = contactFields.firstOrNull()?.value
                val personalSummary = personalFields.firstOrNull()?.let { "${it.label}: ${it.value}" }

                val detailRows = buildList {
                    if (academicFields.isNotEmpty()) {
                        add(
                            Triple(
                                "Academic",
                                Triple(GraduationCapIcon, KlassFeaturePalette.routine(isDark).primary, academicSummary),
                                academicFields.size,
                            )
                        )
                    }
                    if (contactFields.isNotEmpty()) {
                        add(
                            Triple(
                                "Contact",
                                Triple(PhoneSolidIcon, KlassFeaturePalette.teachers(isDark).primary, contactSummary),
                                contactFields.size,
                            )
                        )
                    }
                    if (personalFields.isNotEmpty()) {
                        add(
                            Triple(
                                "Personal",
                                Triple(PersonIcon, KlassFeaturePalette.appearance(isDark).primary, personalSummary),
                                personalFields.size,
                            )
                        )
                    }
                }

                if (detailRows.isNotEmpty()) {
                    SocialSectionDividerBand(isDark = isDark)
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        detailRows.forEachIndexed { idx, (key, meta, count) ->
                            val (icon, rowAccent, subtitleSummary) = meta
                            val localizedTitle = key.localized()
                            KlassNavigationRow(
                                title = localizedTitle,
                                subtitle = subtitleSummary,
                                icon = icon,
                                accent = rowAccent,
                                badgeText = "$count",
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "profile_category_${key.lowercase()}",
                                onClick = { onSelectDetailCategory(key) },
                            )
                            if (idx < detailRows.lastIndex) {
                                SocialInsetDivider(startIndent = 72.dp)
                            }
                        }
                    }
                } else {
                    SocialSectionDividerBand(isDark = isDark)
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        KlassEmptyState(
                            icon = PersonIcon,
                            title = "তথ্য সম্পূর্ণ নয়".localized(),
                            subtitle = "Tap Edit to complete your info.",
                            accent = profileAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                SocialSectionDividerBand(isDark = isDark)

                // ── ACCOUNT ACTIONS BLOCK ────────────────────────────────────────────────
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    KlassNavigationRow(
                        title = "লগআউট করুন".localized(),
                        subtitle = "বর্তমান অ্যাকাউন্ট থেকে বের হন".localized(),
                        icon = LogoutIcon,
                        accent = KlassFeaturePalette.notifications(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        showChevron = false,
                        testTag = "profile_logout_button",
                        onClick = onSignOutClick,
                    )

                    SocialInsetDivider(startIndent = 72.dp)

                    KlassNavigationRow(
                        title = "ডিলিট একাউন্ট".localized(),
                        subtitle = "সকল তথ্য স্থায়ীভাবে মুছে ফেলুন".localized(),
                        icon = TrashCanIcon,
                        accent = KlassFeaturePalette.error(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        showChevron = false,
                        testTag = "profile_delete_account_button",
                        onClick = onDeleteAccountClick,
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = activeDetailCategory != null,
        enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 8 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 8 },
        modifier = Modifier
            .fillMaxSize()
            .zIndex(80f),
    ) {
        val cat = activeDetailCategory
        val fieldsToShow = when (cat) {
            "Academic" -> academicFields
            "Contact" -> contactFields
            "Personal" -> personalFields
            else -> emptyList()
        }
        val sheetIcon = when (cat) {
            "Academic" -> GraduationCapIcon
            "Contact" -> PhoneSolidIcon
            else -> PersonIcon
        }
        val sheetAccent = when (cat) {
            "Academic" -> KlassFeaturePalette.routine(isDark).primary
            "Contact" -> KlassFeaturePalette.teachers(isDark).primary
            else -> KlassFeaturePalette.appearance(isDark).primary
        }
        if (cat != null) {
            ProfileDetailsSubScreen(
                title = cat.localized(),
                icon = sheetIcon,
                accent = sheetAccent,
                fields = fieldsToShow,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onEditRequested = {
                    onSelectDetailCategory(null)
                    onEditClick()
                },
                onBack = { onSelectDetailCategory(null) },
            )
        }
    }
}

@Composable
private fun ProfileDetailsSubScreen(
    title: String,
    icon: ImageVector,
    accent: Color,
    fields: List<GoogleContactField>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onEditRequested: () -> Unit,
    onBack: () -> Unit,
) {
    var inspectedField by remember { mutableStateOf<GoogleContactField?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = title,
            subtitle = "${fields.size} টি তথ্য",
            levelLabel = "প্রোফাইল • বিবরণ",
            icon = icon,
            accent = accent,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onBack,
            closeTestTag = "profile_details_back_button",
            headerActions = {
                KlassTonalButton(
                    text = "এডিট",
                    icon = EditIcon,
                    accent = accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 36.dp,
                    onClick = onEditRequested,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    KlassHeroBanner(
                        title = title,
                        subtitle = "যেকোনো তথ্যে ট্যাপ করলে বিস্তারিত ও কপি/অ্যাকশন অপশন পাবেন",
                        icon = icon,
                        accent = accent,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badges = listOf("${fields.size} টি আইটেম" to accent),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    fields.forEachIndexed { idx, field ->
                        val rowAccent = if (field.isHighlight) KlassFeaturePalette.blood(isDark).primary else accent
                        KlassDetailFieldRow(
                            label = field.label,
                            value = field.value,
                            icon = field.icon,
                            accent = rowAccent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { inspectedField = field },
                        )
                        if (idx < fields.lastIndex) {
                            SocialInsetDivider(startIndent = 72.dp)
                        }
                    }
                }
            }
        }

        // Level 2: Focused Field Inspection & Quick Action Sub-Sub-Screen
        KlassSubScreenHost(
            visible = inspectedField != null,
            zIndex = 92f,
        ) {
            inspectedField?.let { field ->
                ProfileFieldDetailSubScreen(
                    categoryTitle = title,
                    field = field,
                    accent = if (field.isHighlight) KlassFeaturePalette.blood(isDark).primary else accent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onEditRequested = {
                        inspectedField = null
                        onEditRequested()
                    },
                    onDismiss = { inspectedField = null },
                )
            }
        }
    }
}

@Composable
private fun ProfileFieldDetailSubScreen(
    categoryTitle: String,
    field: GoogleContactField,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onEditRequested: () -> Unit,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val cs = MaterialTheme.colorScheme

    MinimalFullScreenSheet(
        title = field.label,
        subtitle = categoryTitle,
        levelLabel = "প্রোফাইল • $categoryTitle • বিস্তারিত",
        icon = field.icon,
        accent = accent,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GoogleOutlinedButton(
                    text = "কপি করুন",
                    icon = CopyIcon,
                    hindSiliguri = hindSiliguri,
                    contentColor = accent,
                    height = 46.dp,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        clipboard.setText(AnnotatedString(field.value))
                        showToast("কপি করা হয়েছে!")
                    },
                )
                WelcomeMinimal3DButton(
                    text = when (field.actionType) {
                        ProfileFieldActionType.PHONE -> "কল করুন"
                        ProfileFieldActionType.EMAIL -> "ইমেইল পাঠান"
                        ProfileFieldActionType.NONE -> "এডিট করুন"
                    },
                    icon = when (field.actionType) {
                        ProfileFieldActionType.PHONE -> PhoneSolidIcon
                        ProfileFieldActionType.EMAIL -> EnvelopeSolidIcon
                        ProfileFieldActionType.NONE -> EditIcon
                    },
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                    customPrimaryColor = when (field.actionType) {
                        ProfileFieldActionType.PHONE -> KlassFeaturePalette.classrooms(isDark).primary
                        ProfileFieldActionType.EMAIL -> KlassFeaturePalette.teachers(isDark).primary
                        ProfileFieldActionType.NONE -> accent
                    },
                    height = 46.dp,
                    modifier = Modifier.weight(1.25f),
                    onClick = {
                        when (field.actionType) {
                            ProfileFieldActionType.PHONE -> openPhoneDialer(field.value)
                            ProfileFieldActionType.EMAIL -> uriHandler.openUri("mailto:${field.value}")
                            ProfileFieldActionType.NONE -> onEditRequested()
                        }
                    },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KlassHeroBanner(
                title = field.value,
                subtitle = field.label,
                icon = field.icon,
                accent = accent,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                badges = listOf(categoryTitle to accent),
            )

            SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "তথ্যের বিবরণ",
                        color = accent,
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "এই তথ্যটি আপনার প্রোফাইল কার্ড এবং প্রাসঙ্গিক ক্লাস ও ডিরেক্টরি ফিচারে ব্যবহৃত হয়। পরিবর্তন করতে চাইলে নিচের এডিট অপশন ব্যবহার করুন।",
                        color = cs.onSurfaceVariant,
                        fontSize = 14.5.sp,
                        fontFamily = hindSiliguri,
                        lineHeight = 21.sp,
                    )
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
    val profileAccent = KlassFeaturePalette.profile(isDark)
    MinimalBackdrop(isDark = isDark, focusAccent = profileAccent.primary) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = CircleShape,
                    color = profileAccent.softTint,
                    border = BorderStroke(1.25.dp, profileAccent.borderTint),
                    modifier = Modifier.size(76.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PersonIcon,
                            contentDescription = null,
                            tint = profileAccent.primary,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign in to Klass",
                    color = cs.onSurface,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "View your class routine, profile, and automatic class reminders.",
                    color = cs.onSurfaceVariant,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = hindSiliguri,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                WelcomeMinimal3DButton(
                    text = "সাইন ইন করুন".localized(),
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                    customPrimaryColor = profileAccent.primary,
                    customBaseColor = profileAccent.deep,
                    height = 50.dp,
                    cornerRadius = 25.dp,
                    testTag = "guest_sign_in_button",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSignInClick,
                )
            }
        }
    }
}
