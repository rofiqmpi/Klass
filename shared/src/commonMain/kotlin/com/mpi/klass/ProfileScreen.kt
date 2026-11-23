package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.CopyIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.EyeIcon
import com.mpi.klass.theme.EyeOffIcon
import com.mpi.klass.theme.FileLinesIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.IdCardIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LanguageGlobeIcon
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.LockIcon
import com.mpi.klass.theme.MessageBubbleIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.PrayingHandsIcon
import com.mpi.klass.theme.RingIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserPlusIcon
import com.mpi.klass.theme.UserTieIcon
import com.mpi.klass.theme.UsersGroupIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import com.mpi.klass.theme.WarningIcon
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.bg_menu_profile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

internal enum class ProfileFieldActionType {
    NONE,
    PHONE,
    EMAIL,
}

internal data class GoogleContactField(
    val fieldKey: String,
    val label: String,
    val value: String,
    val isEmpty: Boolean,
    val icon: ImageVector,
    val isHighlight: Boolean = false,
    val actionType: ProfileFieldActionType = ProfileFieldActionType.NONE,
    val privacy: ProfilePrivacy = ProfilePrivacy.PUBLIC,
)

internal fun canViewerSeeProfileField(
    fieldKey: String,
    ownerProfile: StudentProfile,
    ownerRole: UserRole,
    isOwnProfile: Boolean,
): Boolean {
    if (isOwnProfile) return true
    val resolved = ownerProfile.fieldPrivacy[fieldKey] ?: defaultProfileFieldPrivacy(fieldKey, ownerRole)
    return resolved == ProfilePrivacy.PUBLIC
}

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
    topContentPadding: Dp = 0.dp,
    isEditingMode: Boolean = false,
    onEditingModeChange: ((Boolean) -> Unit)? = null,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
    onOpenTeachingPrefs: (() -> Unit)? = null,
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

    var internalEditingMode by remember { mutableStateOf(false) }
    val effectiveEditingMode = if (onEditingModeChange != null) isEditingMode else internalEditingMode
    fun setEditingMode(editing: Boolean) {
        if (onEditingModeChange != null) {
            onEditingModeChange(editing)
        } else {
            internalEditingMode = editing
        }
    }

    var isSavingProfile by remember { mutableStateOf(false) }
    val cachedProfileSnapshot = remember(cleanEmail) {
        if (cleanEmail.isNotBlank()) fetchLocalStudentProfile(cleanEmail) else null
    }
    var isLoadingProfile by remember(cleanEmail) { mutableStateOf(cachedProfileSnapshot == null) }
    var hasLoginPassword by remember(cleanEmail) { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPhotoUrl by remember(cleanEmail, photoUrl) { mutableStateOf(photoUrl ?: "") }
    var currentCoverPhotoUrl by remember(cleanEmail) { mutableStateOf("") }

    var profileData by remember(cleanEmail) {
        val cached = cachedProfileSnapshot
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

    LaunchedEffect(profileData.coverPhotoUrl) {
        val cover = profileData.coverPhotoUrl?.ifBlank { null }
        if (cover != null && currentCoverPhotoUrl.isBlank()) {
            currentCoverPhotoUrl = cover
        }
    }

    // Photo & cover pickers live strictly for EditProfileContent (never triggered directly on Profile View)
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

    val launchCoverPhotoPicker = rememberPhotoPickerLauncher { pickedUri ->
        if (pickedUri.isNotBlank()) {
            currentCoverPhotoUrl = pickedUri
            val updated = profileData.copy(coverPhotoUrl = pickedUri)
            profileData = updated
            coroutineScope.launch {
                val result = RoleConfig.saveStudentProfile(updated)
                if (result.isSuccess) {
                    showToast(AppStrings.translate("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!", currentLang))
                } else {
                    showToast(AppStrings.translate("তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।", currentLang))
                }
            }
        }
    }

    LaunchedEffect(cleanEmail, role) {
        if (cleanEmail.isNotBlank()) {
            if (cachedProfileSnapshot == null) isLoadingProfile = true
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
                val resolvedCover = fetched.coverPhotoUrl?.ifBlank { null }
                    ?: currentCoverPhotoUrl.ifBlank { null }
                if (!resolvedCover.isNullOrBlank()) {
                    currentCoverPhotoUrl = resolvedCover
                }
                profileData = fetched.copy(
                    name = fetched.name.ifBlank { userName },
                    photoUrl = resolvedPhoto,
                    coverPhotoUrl = resolvedCover,
                    contactEmail = fetched.contactEmail.ifBlank { cleanEmail },
                )
            } else {
                profileData = StudentProfile(
                    email = cleanEmail,
                    name = userName,
                    photoUrl = currentPhotoUrl.ifBlank { photoUrl },
                    coverPhotoUrl = currentCoverPhotoUrl.ifBlank { null },
                    contactEmail = cleanEmail,
                )
            }
            isLoadingProfile = false
            hasLoginPassword = RoleConfig.hasPasswordSet(cleanEmail)
        } else {
            isLoadingProfile = false
        }
    }

    var activeDetailCategory by remember { mutableStateOf<String?>(null) }
    var isEditPickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(activeDetailCategory, isEditPickerOpen, showPasswordDialog) {
        onFullScreenOverlayChange(activeDetailCategory != null || isEditPickerOpen || showPasswordDialog)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    AppBackHandler(enabled = (effectiveEditingMode && !isEditPickerOpen && !showPasswordDialog) || activeDetailCategory != null) {
        if (activeDetailCategory != null) {
            activeDetailCategory = null
        } else if (effectiveEditingMode && !isEditPickerOpen) {
            setEditingMode(false)
        }
    }

    val activePhotoUrl = currentPhotoUrl.ifBlank { profileData.photoUrl ?: photoUrl ?: "" }.ifBlank { null }
    val activeCoverPhotoUrl = currentCoverPhotoUrl.ifBlank { profileData.coverPhotoUrl ?: "" }.ifBlank { null }

    AnimatedContent(
        targetState = effectiveEditingMode,
        label = "ProfileViewEditTransition",
        modifier = Modifier.fillMaxSize(),
    ) { editing ->
        if (editing) {
            EditProfileContent(
                initialProfile = profileData,
                photoUrl = activePhotoUrl,
                coverPhotoUrl = activeCoverPhotoUrl,
                fallbackName = userName,
                fallbackEmail = cleanEmail,
                role = role,
                hindSiliguri = hindSiliguri,
                topContentPadding = topContentPadding,
                isSaving = isSavingProfile,
                isDark = isDark,
                hasLoginPassword = hasLoginPassword,
                onOverlayStateChange = { isEditPickerOpen = it },
                onAvatarClick = { launchPhotoPicker() },
                onCoverPhotoClick = { launchCoverPhotoPicker() },
                onPasswordSetupClick = { showPasswordDialog = true },
                onOpenTeachingPrefs = onOpenTeachingPrefs,
                onViewProfileClick = {
                    isEditPickerOpen = false
                    setEditingMode(false)
                },
                onSaveProfile = { updated ->
                    coroutineScope.launch {
                        isSavingProfile = true
                        val preservedPhoto = activePhotoUrl ?: updated.photoUrl
                        val preservedCover = activeCoverPhotoUrl ?: updated.coverPhotoUrl
                        val finalProfile = updated.copy(
                            photoUrl = preservedPhoto,
                            coverPhotoUrl = preservedCover,
                            address = updated.address.ifBlank { profileData.address },
                        )
                        val result = RoleConfig.saveStudentProfile(finalProfile)
                        profileData = finalProfile
                        isSavingProfile = false
                        if (result.isSuccess) {
                            onUpdateProfile?.invoke(finalProfile.name, preservedPhoto)
                            showToast(AppStrings.translate("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!", currentLang))
                        } else {
                            showToast(AppStrings.translate("তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।", currentLang))
                        }
                    }
                },
            )
        } else {
            FacebookProfileContent(
                profile = profileData,
                photoUrl = activePhotoUrl,
                coverPhotoUrl = activeCoverPhotoUrl,
                fallbackName = userName,
                cleanEmail = cleanEmail,
                role = role,
                isOwnProfile = true,
                isLoading = isLoadingProfile,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                topContentPadding = topContentPadding,
                activeDetailCategory = activeDetailCategory,
                onSelectDetailCategory = { activeDetailCategory = it },
                onEditProfileClick = { setEditingMode(true) },
            )
        }
    }

    if (showPasswordDialog && cleanEmail.isNotBlank()) {
        LoginPasswordSetupDialog(
            email = cleanEmail,
            hasExistingPassword = hasLoginPassword,
            isOnboardingPrompt = false,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onSuccess = {
                hasLoginPassword = true
                showPasswordDialog = false
            },
            onDismiss = { showPasswordDialog = false },
        )
    }
}

/**
 * Facebook-inspired strictly read-only profile layout.
 */
@Composable
internal fun FacebookProfileContent(
    profile: StudentProfile,
    photoUrl: String?,
    coverPhotoUrl: String?,
    fallbackName: String,
    cleanEmail: String,
    role: UserRole,
    isOwnProfile: Boolean = true,
    isLoading: Boolean = false,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 0.dp,
    activeDetailCategory: String? = null,
    onSelectDetailCategory: (String?) -> Unit = {},
    onEditProfileClick: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val displayName = profile.name.ifBlank { fallbackName }
        .ifBlank { cleanEmail.substringBefore("@") }
        .ifBlank { role.defaultName }

    val fbBlue = Color(0xFF0866FF)
    val sheetBg = if (isDark) Color(0xFF0F172A) else Color.White
    val profileAccent = KlassFeaturePalette.profile(isDark)

    val currentLang = LocalAppLanguage.current
    val roleCategoryLine = remember(role, profile, isBengali, currentLang) {
        val roleText = when (role) {
            UserRole.STUDENT -> "শিক্ষার্থী".localized(currentLang)
            UserRole.TEACHER -> "শিক্ষক".localized(currentLang)
            UserRole.ADMIN -> "এডমিন".localized(currentLang)
        }
        val deptShort = RoutineRules.normalizeDepartmentShort(profile.department)
        val semShort = if (role == UserRole.STUDENT && profile.semester.isNotBlank() && !profile.semester.contains("৮ম") && !profile.semester.startsWith("8")) {
            profile.semester.localized(currentLang)
        } else {
            ""
        }
        listOf(roleText, deptShort, semShort).filter { it.isNotBlank() }.joinToString(" • ")
    }

    val dobDisplay = formatProfileDob(profile.dob, isBengali)
    val emailDisplay = profile.contactEmail.ifBlank { cleanEmail }

    fun privacyOf(key: String): ProfilePrivacy =
        profile.fieldPrivacy[key] ?: defaultProfileFieldPrivacy(key, role)

    val academicFields = buildList {
        if (role != UserRole.ADMIN) {
            val deptValue = if (profile.department == "নন টেক") "নন টেক".localized() else profile.department
            if (deptValue.isNotBlank() && canViewerSeeProfileField("department", profile, role, isOwnProfile)) {
                add(GoogleContactField("department", "Department".localized(), deptValue, false, GraduationCapIcon, privacy = privacyOf("department")))
            }
            if (role == UserRole.STUDENT && canViewerSeeProfileField("semester", profile, role, isOwnProfile)) {
                val isValidSem = profile.semester.isNotBlank() && !profile.semester.contains("৮ম") && !profile.semester.startsWith("8")
                if (isValidSem) add(GoogleContactField("semester", "সেমিস্টার".localized(), profile.semester.localized(), false, LayerGroupIcon, privacy = privacyOf("semester")))
            }
            if (profile.shift.isNotBlank() && canViewerSeeProfileField("shift", profile, role, isOwnProfile)) {
                add(GoogleContactField("shift", "শিফট".localized(), profile.shift.localized(), false, ClockIcon, privacy = privacyOf("shift")))
            }
            if (role == UserRole.STUDENT) {
                if (profile.section.isNotBlank() && canViewerSeeProfileField("section", profile, role, isOwnProfile)) {
                    add(GoogleContactField("section", "সেকশন (Section)".localized(), profile.section, false, UsersRectangleIcon, privacy = privacyOf("section")))
                }
                if (profile.rollNumber.isNotBlank() && canViewerSeeProfileField("roll", profile, role, isOwnProfile)) {
                    add(GoogleContactField("roll", "বোর্ড রোল নম্বর (Roll)".localized(), profile.rollNumber, false, IdCardIcon, privacy = privacyOf("roll")))
                }
                if (profile.registrationNumber.isNotBlank() && canViewerSeeProfileField("registration", profile, role, isOwnProfile)) {
                    add(GoogleContactField("registration", "রেজিস্ট্রেশন নম্বর (Registration)".localized(), profile.registrationNumber, false, FileLinesIcon, privacy = privacyOf("registration")))
                }
            }
        }
    }

    val contactFields = buildList {
        if (profile.phone.isNotBlank() && canViewerSeeProfileField("phone", profile, role, isOwnProfile)) {
            add(GoogleContactField("phone", "মোবাইল নম্বর".localized(), profile.phone, false, PhoneSolidIcon, actionType = ProfileFieldActionType.PHONE, privacy = privacyOf("phone")))
        }
        if (emailDisplay.isNotBlank() && canViewerSeeProfileField("contact_email", profile, role, isOwnProfile)) {
            add(GoogleContactField("contact_email", "ইমেল ঠিকানা".localized(), emailDisplay, false, EnvelopeSolidIcon, actionType = ProfileFieldActionType.EMAIL, privacy = privacyOf("contact_email")))
        }
        if (profile.address.isNotBlank() && canViewerSeeProfileField("address", profile, role, isOwnProfile)) {
            add(GoogleContactField("address", "বর্তমান ঠিকানা".localized(), profile.address, false, LocationDotIcon, privacy = privacyOf("address")))
        }
        if (profile.permanentAddress.isNotBlank() && canViewerSeeProfileField("permanent_address", profile, role, isOwnProfile)) {
            add(GoogleContactField("permanent_address", "স্থায়ী ঠিকানা".localized(), profile.permanentAddress, false, LocationDotIcon, privacy = privacyOf("permanent_address")))
        }
    }

    val personalFields = buildList {
        if (profile.bloodGroup.isNotBlank() && canViewerSeeProfileField("blood_group", profile, role, isOwnProfile)) {
            add(GoogleContactField("blood_group", "রক্তের গ্রুপ".localized(), profile.bloodGroup, false, DropletIcon, isHighlight = true, privacy = privacyOf("blood_group")))
        }
        if (dobDisplay.isNotBlank() && canViewerSeeProfileField("dob", profile, role, isOwnProfile)) {
            add(GoogleContactField("dob", "জন্ম তারিখ".localized(), dobDisplay, false, CalendarDaysIcon, privacy = privacyOf("dob")))
        }
        if (profile.gender.isNotBlank() && canViewerSeeProfileField("gender", profile, role, isOwnProfile)) {
            add(GoogleContactField("gender", "লিঙ্গ".localized(), profile.gender.localized(), false, VenusMarsIcon, privacy = privacyOf("gender")))
        }
        if (role != UserRole.ADMIN && profile.religion.isNotBlank() && canViewerSeeProfileField("religion", profile, role, isOwnProfile)) {
            add(GoogleContactField("religion", "ধর্ম".localized(), profile.religion.localized(), false, PrayingHandsIcon, privacy = privacyOf("religion")))
        }
        if (role != UserRole.ADMIN && profile.maritalStatus.isNotBlank() && canViewerSeeProfileField("marital_status", profile, role, isOwnProfile)) {
            add(GoogleContactField("marital_status", "বৈবাহিক অবস্থা".localized(), profile.maritalStatus.localized(), false, RingIcon, privacy = privacyOf("marital_status")))
        }
        if (role == UserRole.STUDENT && profile.fatherName.isNotBlank() && canViewerSeeProfileField("father_name", profile, role, isOwnProfile)) {
            add(GoogleContactField("father_name", "পিতার নাম".localized(), profile.fatherName, false, UserTieIcon, privacy = privacyOf("father_name")))
        }
        if (role == UserRole.STUDENT && profile.motherName.isNotBlank() && canViewerSeeProfileField("mother_name", profile, role, isOwnProfile)) {
            add(GoogleContactField("mother_name", "মাতার নাম".localized(), profile.motherName, false, UserNurseIcon, privacy = privacyOf("mother_name")))
        }
    }

    val statusBarTop = if (topContentPadding == 0.dp) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }
    val coverHeight = statusBarTop + 188.dp
    val avatarSize = 140.dp
    val headerContainerHeight = coverHeight + 76.dp
    val shoulderOverlap = 22.dp

    var selectedProfileTab by remember(cleanEmail) { mutableIntStateOf(0) } // 0=All, 1=About, 2=Connections

    MinimalBackdrop(isDark = isDark, focusAccent = profileAccent.primary) {
        if (isLoading) {
            KlassProfileSkeleton(
                isDark = isDark,
                topContentPadding = topContentPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 780.dp)
                    .align(Alignment.TopCenter)
                    .testTag("profile_skeleton_loader"),
            )
            return@MinimalBackdrop
        }
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

                // ── FACEBOOK HEADER: COVER + ROUNDED SHOULDER + CENTERED AVATAR + NAME + BADGES + CTAs ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerContainerHeight),
                ) {
                    // Status-bar-inclusive read-only Cover Photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(coverHeight),
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

                        // Subtle top fade scrim so status bar & header icons stay legible
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(statusBarTop + 68.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.45f),
                                            Color.Black.copy(alpha = 0.14f),
                                            Color.Transparent,
                                        )
                                    )
                                ),
                        )
                    }

                    // Rounded top shoulder overlapping cover bottom (matching screenshot cca54269.jpg)
                    Surface(
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                        color = sheetBg,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp + shoulderOverlap)
                            .align(Alignment.BottomCenter),
                    ) {}

                    // Centered large circular shadow-free Profile Picture (strictly read-only)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(avatarSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = cs.surfaceContainerHigh,
                            border = BorderStroke(4.dp, sheetBg),
                            shadowElevation = 0.dp,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("profile_avatar_view"),
                        ) {
                            val resolvedModel = resolveProfileImageModel(photoUrl)
                            if (resolvedModel != null) {
                                AsyncImage(
                                    model = resolvedModel,
                                    contentDescription = displayName,
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
                    }
                }

                // White / dark sheet identity section below avatar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sheetBg)
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Centered Name + Verified Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = displayName,
                            color = cs.onSurface,
                            fontSize = 25.sp,
                            lineHeight = 30.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = fbBlue.copy(alpha = if (isDark) 0.25f else 0.14f),
                            modifier = Modifier.size(20.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = CheckIcon,
                                    contentDescription = null,
                                    tint = fbBlue,
                                    modifier = Modifier.size(11.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Category / Role subtitle
                    Text(
                        text = roleCategoryLine,
                        color = cs.onSurfaceVariant,
                        fontSize = 14.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Centered Bio
                    if (profile.bio.isNotBlank() && canViewerSeeProfileField("bio", profile, role, isOwnProfile)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.bio,
                            color = cs.onSurface,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp,
                            fontFamily = hindSiliguri,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Edit Profile Rounded Pill CTA
                    if (isOwnProfile && onEditProfileClick != null) {
                        Surface(
                            onClick = onEditProfileClick,
                            shape = RoundedCornerShape(22.dp),
                            color = fbBlue,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("profile_cta_edit_button"),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = EditIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = "প্রোফাইল এডিট".localized(),
                                    color = Color.White,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = hindSiliguri,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filter Chips Row (All / About)
                    val tabs = listOf(
                        0 to "All",
                        1 to "সম্পর্কে".localized(),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.forEach { (idx, label) ->
                            val selected = selectedProfileTab == idx
                            val chipBg by animateColorAsState(
                                targetValue = if (selected) {
                                    fbBlue.copy(alpha = if (isDark) 0.28f else 0.14f)
                                } else {
                                    Color.Transparent
                                },
                                animationSpec = tween(160),
                                label = "fb_tab_$idx",
                            )
                            Surface(
                                onClick = { selectedProfileTab = idx },
                                shape = CircleShape,
                                color = chipBg,
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("profile_tab_$idx"),
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) fbBlue else cs.onSurfaceVariant,
                                        fontSize = 14.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontFamily = hindSiliguri,
                                    )
                                }
                            }
                        }
                    }
                }

                // ── TAB CONTENT (STRICTLY READ-ONLY) ──
                val hasAnyVisible = academicFields.isNotEmpty() || contactFields.isNotEmpty() || personalFields.isNotEmpty()

                if (hasAnyVisible) {
                    ProfileAllDetailsSections(
                        academicFields = academicFields,
                        contactFields = contactFields,
                        personalFields = personalFields,
                        isOwnProfile = isOwnProfile,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                } else {
                    SocialSectionDividerBand(isDark = isDark, height = 8.dp)
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        KlassEmptyState(
                            icon = LockIcon,
                            title = "তথ্য সম্পূর্ণ নয়".localized(),
                            subtitle = if (isOwnProfile) "Tap Edit Profile above to add information." else "No visible profile details for your privacy level.",
                            accent = profileAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                    }
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
                onBack = { onSelectDetailCategory(null) },
            )
        }
    }
}

@Composable
private fun ProfileAllDetailsSections(
    academicFields: List<GoogleContactField>,
    contactFields: List<GoogleContactField>,
    personalFields: List<GoogleContactField>,
    isOwnProfile: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val bloodAccent = KlassFeaturePalette.blood(isDark)

    fun handleFieldTap(field: GoogleContactField) {
        when (field.actionType) {
            ProfileFieldActionType.PHONE -> openPhoneDialer(field.value)
            ProfileFieldActionType.EMAIL -> runCatching { uriHandler.openUri("mailto:${field.value}") }
                .onFailure {
                    clipboard.setText(AnnotatedString(field.value))
                    showToast("কপি করা হয়েছে!")
                }
            ProfileFieldActionType.NONE -> {
                clipboard.setText(AnnotatedString(field.value))
                showToast("কপি করা হয়েছে!")
            }
        }
    }

    if (academicFields.isNotEmpty()) {
        SocialSectionDividerBand(isDark = isDark, height = 8.dp)
        GoogleSectionHeader(
            title = "Academic".localized(),
            accent = KlassFeaturePalette.routine(isDark).primary,
            hindSiliguri = hindSiliguri,
        )
        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
            academicFields.forEachIndexed { idx, field ->
                KlassDetailFieldRow(
                    label = field.label,
                    value = field.value,
                    icon = field.icon,
                    accent = KlassFeaturePalette.routine(isDark).primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    trailingContent = if (isOwnProfile) {
                        { ProfileFieldPrivacyTinyIcon(field.privacy, cs.onSurfaceVariant) }
                    } else null,
                    onClick = { handleFieldTap(field) },
                )
                if (idx < academicFields.lastIndex) {
                    SocialInsetDivider(startIndent = 72.dp)
                }
            }
        }
    }

    if (contactFields.isNotEmpty()) {
        SocialSectionDividerBand(isDark = isDark, height = 8.dp)
        GoogleSectionHeader(
            title = "Contact".localized(),
            accent = KlassFeaturePalette.teachers(isDark).primary,
            hindSiliguri = hindSiliguri,
        )
        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
            contactFields.forEachIndexed { idx, field ->
                KlassDetailFieldRow(
                    label = field.label,
                    value = field.value,
                    icon = field.icon,
                    accent = KlassFeaturePalette.teachers(isDark).primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    trailingContent = if (isOwnProfile) {
                        { ProfileFieldPrivacyTinyIcon(field.privacy, cs.onSurfaceVariant) }
                    } else null,
                    onClick = { handleFieldTap(field) },
                )
                if (idx < contactFields.lastIndex) {
                    SocialInsetDivider(startIndent = 72.dp)
                }
            }
        }
    }

    if (personalFields.isNotEmpty()) {
        SocialSectionDividerBand(isDark = isDark, height = 8.dp)
        GoogleSectionHeader(
            title = "Personal".localized(),
            accent = KlassFeaturePalette.appearance(isDark).primary,
            hindSiliguri = hindSiliguri,
        )
        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
            personalFields.forEachIndexed { idx, field ->
                val rowAccent = if (field.isHighlight) bloodAccent.primary else KlassFeaturePalette.appearance(isDark).primary
                KlassDetailFieldRow(
                    label = field.label,
                    value = field.value,
                    icon = field.icon,
                    accent = rowAccent,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    trailingContent = if (isOwnProfile) {
                        { ProfileFieldPrivacyTinyIcon(field.privacy, cs.onSurfaceVariant) }
                    } else null,
                    onClick = { handleFieldTap(field) },
                )
                if (idx < personalFields.lastIndex) {
                    SocialInsetDivider(startIndent = 72.dp)
                }
            }
        }
    }
}

@Composable
private fun ProfileFieldPrivacyTinyIcon(
    privacy: ProfilePrivacy,
    tint: Color,
) {
    Icon(
        imageVector = when (privacy) {
            ProfilePrivacy.PUBLIC -> LanguageGlobeIcon
            ProfilePrivacy.PRIVATE -> LockIcon
        },
        contentDescription = privacy.labelBn.localized(),
        tint = tint.copy(alpha = 0.72f),
        modifier = Modifier.size(15.dp),
    )
}

@Composable
private fun ProfileDetailsSubScreen(
    title: String,
    icon: ImageVector,
    accent: Color,
    fields: List<GoogleContactField>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))
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
                        onClick = {
                            when (field.actionType) {
                                ProfileFieldActionType.PHONE -> openPhoneDialer(field.value)
                                ProfileFieldActionType.EMAIL -> runCatching { uriHandler.openUri("mailto:${field.value}") }
                                    .onFailure {
                                        clipboard.setText(AnnotatedString(field.value))
                                        showToast("কপি করা হয়েছে!")
                                    }
                                ProfileFieldActionType.NONE -> {
                                    clipboard.setText(AnnotatedString(field.value))
                                    showToast("কপি করা হয়েছে!")
                                }
                            }
                        },
                    )
                    if (idx < fields.lastIndex) {
                        SocialInsetDivider(startIndent = 72.dp)
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

                KlassPrimaryButton(
                    text = "সাইন ইন করুন".localized(),
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                    customPrimaryColor = profileAccent.primary,
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

@Composable
internal fun LoginPasswordSetupDialog(
    email: String,
    hasExistingPassword: Boolean,
    isOnboardingPrompt: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val currentLang = LocalAppLanguage.current
    val scope = rememberCoroutineScope()
    val accent = KlassFeaturePalette.teaching(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)
    val okAccent = KlassFeaturePalette.success(isDark)

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPw by remember { mutableStateOf(false) }
    var showNewPw by remember { mutableStateOf(false) }
    var showConfirmPw by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val strengthEval = remember(newPassword, email) {
        evaluatePasswordStrength(newPassword, email)
    }
    val strengthTargetColor = when (strengthEval.level) {
        PasswordStrengthLevel.EMPTY -> cs.onSurfaceVariant.copy(alpha = 0.45f)
        PasswordStrengthLevel.WEAK -> Color(0xFFEF4444)
        PasswordStrengthLevel.FAIR -> Color(0xFFF59E0B)
        PasswordStrengthLevel.GOOD -> Color(0xFF3B82F6)
        PasswordStrengthLevel.STRONG -> Color(0xFF10B981)
    }
    val strengthColor by animateColorAsState(
        targetValue = strengthTargetColor,
        animationSpec = tween(220),
        label = "pw_strength_color",
    )
    val strengthLabel = when (strengthEval.level) {
        PasswordStrengthLevel.EMPTY -> "খুবই দুর্বল".localized()
        PasswordStrengthLevel.WEAK -> "দুর্বল".localized()
        PasswordStrengthLevel.FAIR -> "মাঝারি".localized()
        PasswordStrengthLevel.GOOD -> "ভালো".localized()
        PasswordStrengthLevel.STRONG -> "শক্তিশালী".localized()
    }

    val confirmMatches = confirmPassword.isNotEmpty() && newPassword == confirmPassword
    val confirmMismatched = confirmPassword.isNotEmpty() && newPassword != confirmPassword

    val panelSurface = if (isDark) Color(0xFF182438) else Color(0xFFF6F9FC)
    val borderSubtle = if (isDark) Color.White.copy(alpha = 0.11f) else Color(0xFFE2E8F0)

    val screenTitle = if (hasExistingPassword) {
        "পাসওয়ার্ড পরিবর্তন করুন".localized()
    } else {
        "পাসওয়ার্ড সেট করুন".localized()
    }
    val screenDesc = if (hasExistingPassword) {
        "নিরাপত্তার জন্য বর্তমান পাসওয়ার্ড যাচাই করে নতুন শক্তিশালী পাসওয়ার্ড সেট করুন।".localized()
    } else {
        "পরবর্তীতে ইমেইল ও পাসওয়ার্ড দিয়ে সরাসরি লগইন করতে একটি শক্তিশালী পাসওয়ার্ড সেট করুন।".localized()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(95f),
    ) {
        MinimalFullScreenSheet(
            title = screenTitle,
            subtitle = email.ifBlank { "সুরক্ষিত অ্যাকাউন্ট".localized() },
            levelLabel = "সুরক্ষিত অ্যাকাউন্ট".localized(),
            icon = LockIcon,
            accent = accent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            dismissEnabled = !isSaving,
            closeTestTag = "password_setup_close_button",
            maxContentWidth = 640.dp,
            headerActions = {
                HeaderSavePillButton(
                    text = if (isSaving) "..." else "সংরক্ষণ".localized(),
                    accent = accent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    isLoading = isSaving,
                    enabled = !isSaving,
                    testTag = "password_setup_save_button",
                    onClick = {
                        if (hasExistingPassword && currentPassword.isBlank()) {
                            errorMsg = "বর্তমান পাসওয়ার্ডটি লিখুন"
                            return@HeaderSavePillButton
                        }
                        val validation = validateLoginPassword(newPassword, confirmPassword, email)
                        if (validation != null) {
                            errorMsg = validation
                            return@HeaderSavePillButton
                        }
                        isSaving = true
                        scope.launch {
                            val res = RoleConfig.saveLoginPassword(
                                email = email,
                                newPassword = newPassword,
                                currentPassword = if (hasExistingPassword) currentPassword else null,
                            )
                            isSaving = false
                            if (res.isSuccess) {
                                showToast("পাসওয়ার্ড সফলভাবে সংরক্ষিত হয়েছে!".localized(currentLang))
                                onSuccess()
                            } else {
                                errorMsg = res.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                                    ?: "পাসওয়ার্ড সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।"
                            }
                        }
                    },
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = screenDesc,
                    color = cs.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    fontFamily = hindSiliguri,
                )
                if (hasExistingPassword) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            errorMsg = null
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        label = { Text("বর্তমান পাসওয়ার্ড".localized(), fontFamily = hindSiliguri, fontSize = 13.5.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = LockIcon,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        visualTransformation = if (showCurrentPw) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showCurrentPw = !showCurrentPw },
                                modifier = Modifier.testTag("password_setup_toggle_current"),
                            ) {
                                Icon(
                                    imageVector = if (showCurrentPw) EyeOffIcon else EyeIcon,
                                    contentDescription = if (showCurrentPw) "Hide password" else "Show password",
                                    tint = cs.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        },
                        textStyle = TextStyle(fontFamily = hindSiliguri, fontSize = 15.sp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent.primary,
                            unfocusedBorderColor = borderSubtle,
                            focusedLabelColor = accent.primary,
                            focusedContainerColor = panelSurface.copy(alpha = 0.55f),
                            unfocusedContainerColor = panelSurface.copy(alpha = 0.45f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_setup_current_input"),
                    )
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMsg = null
                    },
                    singleLine = true,
                    enabled = !isSaving,
                    label = { Text("নতুন পাসওয়ার্ড".localized(), fontFamily = hindSiliguri, fontSize = 13.5.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = ShieldIcon,
                            contentDescription = null,
                            tint = if (newPassword.isEmpty()) cs.onSurfaceVariant else strengthColor,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    visualTransformation = if (showNewPw) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { showNewPw = !showNewPw },
                            modifier = Modifier.testTag("password_setup_toggle_new"),
                        ) {
                            Icon(
                                imageVector = if (showNewPw) EyeOffIcon else EyeIcon,
                                contentDescription = if (showNewPw) "Hide password" else "Show password",
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    textStyle = TextStyle(fontFamily = hindSiliguri, fontSize = 15.sp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (newPassword.isEmpty()) accent.primary else strengthColor,
                        unfocusedBorderColor = if (newPassword.isEmpty()) borderSubtle else strengthColor.copy(alpha = 0.6f),
                        focusedLabelColor = accent.primary,
                        focusedContainerColor = panelSurface.copy(alpha = 0.55f),
                        unfocusedContainerColor = panelSurface.copy(alpha = 0.45f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_setup_new_input"),
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = panelSurface,
                    border = BorderStroke(1.dp, borderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_strength_card"),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "পাসওয়ার্ডের শক্তি".localized(),
                                fontFamily = hindSiliguri,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cs.onSurfaceVariant,
                            )

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = strengthColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, strengthColor.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = if (newPassword.isEmpty()) "—" else strengthLabel,
                                    fontFamily = hindSiliguri,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = strengthColor,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            for (segment in 1..4) {
                                val filled = newPassword.isNotEmpty() && strengthEval.score >= segment
                                val barColor by animateColorAsState(
                                    targetValue = if (filled) {
                                        strengthColor
                                    } else {
                                        cs.onSurface.copy(alpha = if (isDark) 0.10f else 0.08f)
                                    },
                                    animationSpec = tween(220),
                                    label = "pw_seg_$segment",
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(barColor),
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                PasswordCriterionChip(
                                    label = "৮+ অক্ষর".localized(),
                                    met = strengthEval.hasMinLength,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.weight(1f),
                                )
                                PasswordCriterionChip(
                                    label = "A-Z ও a-z".localized(),
                                    met = strengthEval.hasUppercase && strengthEval.hasLowercase,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                PasswordCriterionChip(
                                    label = "০-৯ সংখ্যা".localized(),
                                    met = strengthEval.hasDigit,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.weight(1f),
                                )
                                PasswordCriterionChip(
                                    label = "@#$ চিহ্ন".localized(),
                                    met = strengthEval.hasSpecialChar,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        if (newPassword.isNotEmpty() && (!strengthEval.hasNoWhitespace || !strengthEval.notCommonOrEmailDerived)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = WarningIcon,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "সতর্কতা: পাসওয়ার্ডে স্পেস, ইমেইলের অংশ বা সহজ প্যাটার্ন ব্যবহার করবেন না।".localized(),
                                    fontFamily = hindSiliguri,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFFD97706),
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMsg = null
                    },
                    singleLine = true,
                    enabled = !isSaving,
                    label = { Text("পাসওয়ার্ড নিশ্চিত করুন".localized(), fontFamily = hindSiliguri, fontSize = 13.5.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (confirmMatches) CheckIcon else LockIcon,
                            contentDescription = null,
                            tint = when {
                                confirmMatches -> okAccent.primary
                                confirmMismatched -> errorAccent.primary
                                else -> cs.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    visualTransformation = if (showConfirmPw) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { showConfirmPw = !showConfirmPw },
                            modifier = Modifier.testTag("password_setup_toggle_confirm"),
                        ) {
                            Icon(
                                imageVector = if (showConfirmPw) EyeOffIcon else EyeIcon,
                                contentDescription = if (showConfirmPw) "Hide password" else "Show password",
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    supportingText = if (confirmMatches || confirmMismatched) {
                        {
                            Text(
                                text = if (confirmMatches) {
                                    "মিলেছে".localized()
                                } else {
                                    "পাসওয়ার্ড মিলছে না".localized()
                                },
                                fontFamily = hindSiliguri,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (confirmMatches) okAccent.primary else errorAccent.primary,
                            )
                        }
                    } else null,
                    textStyle = TextStyle(fontFamily = hindSiliguri, fontSize = 15.sp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = when {
                            confirmMatches -> okAccent.primary
                            confirmMismatched -> errorAccent.primary
                            else -> accent.primary
                        },
                        unfocusedBorderColor = when {
                            confirmMatches -> okAccent.primary.copy(alpha = 0.65f)
                            confirmMismatched -> errorAccent.primary.copy(alpha = 0.7f)
                            else -> borderSubtle
                        },
                        focusedLabelColor = accent.primary,
                        focusedContainerColor = panelSurface.copy(alpha = 0.55f),
                        unfocusedContainerColor = panelSurface.copy(alpha = 0.45f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_setup_confirm_input"),
                )

                errorMsg?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = errorAccent.softTint,
                        border = BorderStroke(1.dp, errorAccent.borderTint),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = WarningIcon,
                                contentDescription = null,
                                tint = errorAccent.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = err.localized(),
                                fontFamily = hindSiliguri,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = errorAccent.primary,
                            )
                        }
                    }
                }

                if (isOnboardingPrompt) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, borderSubtle),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("password_setup_skip_button"),
                    ) {
                        Text(
                            text = "পরে করব".localized(),
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = ShieldIcon,
                        contentDescription = null,
                        tint = okAccent.primary.copy(alpha = 0.85f),
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "PBKDF2-SHA256 এনক্রিপ্টেড ও নিরাপদ স্টোরেজ".localized(),
                        fontFamily = hindSiliguri,
                        fontSize = 11.5.sp,
                        color = cs.onSurfaceVariant.copy(alpha = 0.82f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PasswordCriterionChip(
    label: String,
    met: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val metColor = Color(0xFF10B981)
    val bgColor by animateColorAsState(
        targetValue = if (met) {
            metColor.copy(alpha = if (isDark) 0.16f else 0.11f)
        } else {
            cs.onSurface.copy(alpha = if (isDark) 0.06f else 0.04f)
        },
        animationSpec = tween(200),
        label = "pw_chip_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (met) {
            metColor.copy(alpha = 0.45f)
        } else {
            cs.onSurface.copy(alpha = 0.12f)
        },
        animationSpec = tween(200),
        label = "pw_chip_border",
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (met) metColor else cs.onSurface.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = CheckIcon,
                    contentDescription = null,
                    tint = if (met) Color.White else cs.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(9.dp),
                )
            }
            Text(
                text = label,
                fontFamily = hindSiliguri,
                fontSize = 11.5.sp,
                fontWeight = if (met) FontWeight.Bold else FontWeight.Medium,
                color = if (met) metColor else cs.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
