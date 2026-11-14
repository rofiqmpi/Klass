package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CalendarDaysIcon
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
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.PrayingHandsIcon
import com.mpi.klass.theme.RingIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.UserNurseIcon
import com.mpi.klass.theme.UserTieIcon
import com.mpi.klass.theme.UsersRectangleIcon
import com.mpi.klass.theme.VenusMarsIcon
import kotlin.math.PI
import kotlin.math.sin
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
    topContentPadding: Dp = 0.dp,
) {
    // Guest mode animated sign-in portal
    if (role == null) {
        GuestSignInPrompt(
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onSignInClick = onSignOut,
        )
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val cleanEmail = email.ifBlank { loadUserSession()?.email ?: "" }.trim().lowercase()

    var isEditingMode by remember { mutableStateOf(false) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var currentPhotoUrl by remember(cleanEmail, photoUrl) { mutableStateOf(photoUrl ?: "") }

    var profileData by remember(cleanEmail) {
        mutableStateOf(
            StudentProfile(
                email = cleanEmail,
                name = userName,
                photoUrl = photoUrl,
                contactEmail = cleanEmail,
            )
        )
    }

    // Native Device Gallery Photo Picker for Profile Picture in Brand Card Bar
    val launchPhotoPicker = rememberPhotoPickerLauncher { pickedUri ->
        if (pickedUri.isNotBlank()) {
            currentPhotoUrl = pickedUri
            val updated = profileData.copy(photoUrl = pickedUri)
            profileData = updated
            val activeName = updated.name.ifBlank { userName }
            onUpdateProfile?.invoke(activeName, pickedUri)
            coroutineScope.launch {
                if (role == UserRole.STUDENT) {
                    RoleConfig.saveStudentProfile(updated)
                } else {
                    saveStudentProfileDataInFirestore(updated)
                }
            }
            showToast("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!")
        }
    }

    // Auto GPS location tracker for Current Address (matches previous auto-tracking logic)
    val locationLauncher = rememberLocationPermissionLauncher { realAddress ->
        if (realAddress.isNotBlank() && realAddress != profileData.address) {
            val updated = profileData.copy(
                address = realAddress,
                photoUrl = currentPhotoUrl.ifBlank { profileData.photoUrl ?: photoUrl ?: "" }.ifBlank { null },
            )
            profileData = updated
            coroutineScope.launch {
                if (role == UserRole.STUDENT) {
                    RoleConfig.saveStudentProfile(updated)
                } else {
                    saveStudentProfileDataInFirestore(updated)
                }
            }
        }
    }

    // Load saved profile from local & Firebase database and start live GPS tracking
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
        transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(220)) },
        label = "ProfileViewEditTransition",
        modifier = Modifier.fillMaxSize(),
    ) { editing ->
        if (editing) {
            EditProfileContent(
                initialProfile = profileData,
                photoUrl = activePhotoUrl,
                fallbackName = userName,
                fallbackEmail = cleanEmail,
                hindSiliguri = hindSiliguri,
                topContentPadding = topContentPadding,
                isSaving = isSavingProfile,
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
                        val result = if (role == UserRole.STUDENT) {
                            RoleConfig.saveStudentProfile(finalProfile)
                        } else {
                            saveStudentProfileDataInFirestore(finalProfile)
                        }
                        profileData = finalProfile
                        onUpdateProfile?.invoke(finalProfile.name, preservedPhoto)
                        isSavingProfile = false
                        if (result.isSuccess) {
                            showToast("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!")
                            isEditingMode = false
                        } else {
                            showToast("তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।")
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
                hindSiliguri = hindSiliguri,
                topContentPadding = topContentPadding,
                onAvatarClick = { launchPhotoPicker() },
                onEditClick = { isEditingMode = true },
                onSignOutClick = {
                    clearUserSession()
                    onSignOut()
                },
                onDeleteAccountClick = { showDeleteDialog = true },
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "ডিলিট একাউন্ট".localized(),
                    color = HtmlProfileColors.BsDanger,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                )
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্টটি চিরতরে ডিলিট করতে চান? এটি করলে আপনার সকল তথ্য মুছে যাবে।".localized(),
                    color = HtmlProfileColors.SubheadingSlate,
                    fontFamily = hindSiliguri,
                    fontSize = 15.sp,
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
                    colors = ButtonDefaults.buttonColors(containerColor = HtmlProfileColors.BsDanger),
                    modifier = Modifier.testTag("confirm_delete_account_button"),
                ) {
                    Text(
                        text = if (isDeletingAccount) "মুছে ফেলা হচ্ছে...".localized() else "হ্যাঁ".localized(),
                        color = Color.White,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
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
                        color = HtmlProfileColors.SubheadingSlate,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
        )
    }
}

@Composable
private fun ProfileViewContent(
    profile: StudentProfile,
    photoUrl: String?,
    userName: String,
    cleanEmail: String,
    role: UserRole,
    isLoading: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
) {
    val isBengali = LocalAppLanguage.current == AppLanguage.BENGALI
    val displayName = profile.name.ifBlank { userName }
        .ifBlank { cleanEmail.substringBefore("@") }
        .ifBlank { role.defaultName }

    val roleSubtitle = when (role) {
        UserRole.STUDENT -> "শিক্ষার্থী প্রোফাইল"
        UserRole.TEACHER -> "শিক্ষক প্রোফাইল"
        UserRole.ADMIN -> "এডমিন প্রোফাইল"
    }.localized()

    val emptyText = "তথ্য প্রদান করা হয়নি".localized()

    HtmlProfilePageWrapper(
        topContentPadding = topContentPadding,
        brandBar = { collapseProgress ->
            HtmlBrandCardBar(
                photoUrl = photoUrl,
                avatarSeed = cleanEmail.ifBlank { displayName },
                buttonText = "এডিট করুন".localized(),
                buttonIcon = EditIcon,
                hindSiliguri = hindSiliguri,
                testTag = "profile_edit_button",
                collapseProgress = collapseProgress,
                onAvatarClick = onAvatarClick,
                onButtonClick = onEditClick,
            )
        },
    ) {
        // Header Section (h1 & p)
        Text(
            text = displayName,
            color = HtmlProfileColors.HeadingDark,
            fontSize = 27.sp,
            lineHeight = 34.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = roleSubtitle,
            color = HtmlProfileColors.BsSecondary,
            fontSize = 18.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        if (isLoading) {
            Spacer(modifier = Modifier.height(14.dp))
            CircularProgressIndicator(
                color = HtmlProfileColors.PurpleAccent,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth >= 840.dp -> 12
                maxWidth >= 560.dp -> 2
                else -> 1
            }

            fun spanFor(lgSpan: Int, mdSpan: Int): Int = when (cols) {
                12 -> lgSpan
                2 -> mdSpan
                else -> 1
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                val dobDisplay = formatProfileDob(profile.dob, isBengali).ifBlank { emptyText }
                val emailDisplay = profile.contactEmail.ifBlank { cleanEmail }.ifBlank { emptyText }

                // Section 1: Personal Information
                HtmlSectionTitle(
                    title = "১. ব্যক্তিগত তথ্য (Personal Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val personalCards: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "জন্ম তারিখ".localized(),
                            value = dobDisplay,
                            isEmpty = profile.dob.isBlank(),
                            icon = CalendarDaysIcon,
                            iconTint = HtmlProfileColors.BsWarning,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "রক্তের গ্রুপ".localized(),
                            value = profile.bloodGroup.ifBlank { emptyText },
                            isEmpty = profile.bloodGroup.isBlank(),
                            icon = DropletIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            valueColor = if (profile.bloodGroup.isNotBlank()) HtmlProfileColors.BsDanger else Color(0xFF94A3B8),
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "ধর্ম".localized(),
                            value = if (profile.religion.isNotBlank()) profile.religion.localized() else emptyText,
                            isEmpty = profile.religion.isBlank(),
                            icon = PrayingHandsIcon,
                            iconTint = HtmlProfileColors.BsInfo,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(6, 1) to {
                        HtmlInfoCard(
                            label = "লিঙ্গ".localized(),
                            value = if (profile.gender.isNotBlank()) profile.gender.localized() else emptyText,
                            isEmpty = profile.gender.isBlank(),
                            icon = VenusMarsIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(6, 1) to {
                        HtmlInfoCard(
                            label = "বৈবাহিক অবস্থা".localized(),
                            value = if (profile.maritalStatus.isNotBlank()) profile.maritalStatus.localized() else emptyText,
                            isEmpty = profile.maritalStatus.isBlank(),
                            icon = RingIcon,
                            iconTint = HtmlProfileColors.BsSecondary,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = personalCards, spacing = 14.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 2: Family Information
                HtmlSectionTitle(
                    title = "২. পারিবারিক তথ্য (Family Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val familyCards: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    spanFor(6, 1) to {
                        HtmlInfoCard(
                            label = "পিতার নাম".localized(),
                            value = profile.fatherName.ifBlank { emptyText },
                            isEmpty = profile.fatherName.isBlank(),
                            icon = UserTieIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(6, 1) to {
                        HtmlInfoCard(
                            label = "মাতার নাম".localized(),
                            value = profile.motherName.ifBlank { emptyText },
                            isEmpty = profile.motherName.isBlank(),
                            icon = UserNurseIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = familyCards, spacing = 14.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 3: Contact Information
                HtmlSectionTitle(
                    title = "৩. যোগাযোগের তথ্য (Contact Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val contactCards: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    spanFor(5, 1) to {
                        HtmlInfoCard(
                            label = "মোবাইল নম্বর".localized(),
                            value = profile.phone.ifBlank { emptyText },
                            isEmpty = profile.phone.isBlank(),
                            icon = PhoneSolidIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(7, 1) to {
                        HtmlInfoCard(
                            label = "ইমেল ঠিকানা".localized(),
                            value = emailDisplay,
                            isEmpty = profile.contactEmail.isBlank() && cleanEmail.isBlank(),
                            icon = EnvelopeSolidIcon,
                            iconTint = HtmlProfileColors.BsPrimary,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = contactCards, spacing = 14.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 4: Address Information
                HtmlSectionTitle(
                    title = "৪. ঠিকানার তথ্য (Address Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val addressCards: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    spanFor(12, 2) to {
                        HtmlInfoCard(
                            label = "বর্তমান ঠিকানা".localized(),
                            value = profile.address.ifBlank { emptyText },
                            isEmpty = profile.address.isBlank(),
                            icon = LocationDotIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(12, 2) to {
                        HtmlInfoCard(
                            label = "স্থায়ী ঠিকানা".localized(),
                            value = profile.permanentAddress.ifBlank { emptyText },
                            isEmpty = profile.permanentAddress.isBlank(),
                            icon = LocationDotIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = addressCards, spacing = 14.dp)

                Spacer(modifier = Modifier.height(6.dp))

                // Section 5: Academic Information
                HtmlSectionTitle(
                    title = "৫. একাডেমিক তথ্য (Academic Information)".localized(),
                    hindSiliguri = hindSiliguri,
                )

                val academicCards: List<Pair<Int, @Composable () -> Unit>> = listOf(
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "ডিপার্টমেন্ট (Department)".localized(),
                            value = profile.department.ifBlank { emptyText },
                            isEmpty = profile.department.isBlank(),
                            icon = GraduationCapIcon,
                            iconTint = HtmlProfileColors.BsPrimary,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "সেমিস্টার".localized(),
                            value = if (profile.semester.isNotBlank()) profile.semester.localized() else emptyText,
                            isEmpty = profile.semester.isBlank(),
                            icon = LayerGroupIcon,
                            iconTint = HtmlProfileColors.BsSuccess,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "শিফট".localized(),
                            value = if (profile.shift.isNotBlank()) profile.shift.localized() else emptyText,
                            isEmpty = profile.shift.isBlank(),
                            icon = ClockIcon,
                            iconTint = HtmlProfileColors.BsWarning,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "সেকশন (Section)".localized(),
                            value = profile.section.ifBlank { emptyText },
                            isEmpty = profile.section.isBlank(),
                            icon = UsersRectangleIcon,
                            iconTint = HtmlProfileColors.BsSecondary,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "বোর্ড রোল নম্বর (Roll)".localized(),
                            value = profile.rollNumber.ifBlank { emptyText },
                            isEmpty = profile.rollNumber.isBlank(),
                            icon = IdCardIcon,
                            iconTint = HtmlProfileColors.BsDanger,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                    spanFor(4, 1) to {
                        HtmlInfoCard(
                            label = "রেজিস্ট্রেশন নম্বর (Registration)".localized(),
                            value = profile.registrationNumber.ifBlank { emptyText },
                            isEmpty = profile.registrationNumber.isBlank(),
                            icon = FileLinesIcon,
                            iconTint = HtmlProfileColors.BsInfo,
                            hindSiliguri = hindSiliguri,
                        )
                    },
                )

                HtmlResponsiveGrid(columns = cols, items = academicCards, spacing = 14.dp)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Logout Gradient Button (.btn-submit)
        HtmlGradientSubmitButton(
            text = "লগআউট করুন".localized(),
            icon = LogoutIcon,
            hindSiliguri = hindSiliguri,
            testTag = "profile_logout_button",
            onClick = onSignOutClick,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Delete Account Gradient Button (.btn-submit)
        HtmlGradientSubmitButton(
            text = "ডিলিট একাউন্ট".localized(),
            icon = TrashCanIcon,
            hindSiliguri = hindSiliguri,
            testTag = "profile_delete_account_button",
            onClick = onDeleteAccountClick,
        )

        HtmlProfileFooter(hindSiliguri = hindSiliguri)
    }
}

@Composable
private fun HtmlInfoCard(
    label: String,
    value: String,
    isEmpty: Boolean,
    icon: ImageVector,
    iconTint: Color,
    hindSiliguri: FontFamily,
    valueColor: Color = HtmlProfileColors.HeadingDark,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HtmlProfileColors.CardBorderGray),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = HtmlProfileColors.InfoLabelGray,
                    fontSize = 14.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isEmpty) Color(0xFF94A3B8) else valueColor,
                fontSize = 16.5.sp,
                lineHeight = 23.sp,
                fontFamily = hindSiliguri,
                fontWeight = if (isEmpty) FontWeight.Medium else FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun GuestSignInPrompt(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSignInClick: () -> Unit,
) {
    val primaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    val ringColor = primaryColor.copy(alpha = 0.15f)
    val transition = rememberInfiniteTransition(label = "RippleTransition")
    val pulseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseProgress",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isButtonClicked by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center,
    ) {
        for (i in 1..6) {
            val animatedScale = if (isButtonClicked) {
                (1f + (pulseProgress * 0.5f) + (i * 0.12f)).coerceIn(1f, 3f)
            } else {
                1f + (sin((pulseProgress * 2f * PI + i * 0.5f)).toFloat() * 0.04f)
            }
            Box(
                modifier = Modifier
                    .size((140 + i * 85).dp)
                    .graphicsLayer(
                        scaleX = animatedScale,
                        scaleY = animatedScale,
                        alpha = (1f - (i * 0.14f)).coerceIn(0.08f, 0.7f),
                    )
                    .clip(CircleShape)
                    .background(ringColor),
            )
        }

        Button(
            onClick = onSignInClick,
            interactionSource = interactionSource,
            modifier = Modifier.size(140.dp).testTag("guest_sign_in_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
        ) {
            Text(
                text = "সাইন ইন করুন".localized(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = hindSiliguri,
                textAlign = TextAlign.Center,
            )
        }
    }
}
