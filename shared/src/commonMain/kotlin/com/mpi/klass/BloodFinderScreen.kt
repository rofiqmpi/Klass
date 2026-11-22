package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CopyIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.departmentAccent
import kotlinx.coroutines.launch

@Composable
fun BloodFinderScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    isGuest: Boolean = false,
    onSignInClick: (() -> Unit)? = null,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val bloodAccent = KlassFeaturePalette.blood(isDark)

    var donors by remember { mutableStateOf<List<BloodDonorItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedBloodGroup by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDonor by remember { mutableStateOf<BloodDonorItem?>(null) }

    LaunchedEffect(selectedDonor != null) {
        onFullScreenOverlayChange(selectedDonor != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    fun refreshDonors() {
        scope.launch {
            isLoading = true
            try {
                donors = fetchBloodDonorsFromFirestore()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshDonors()
    }

    val filteredDonors = remember(donors, selectedBloodGroup, searchQuery) {
        val q = searchQuery.trim().lowercase()
        donors.filter { d ->
            val groupMatch = selectedBloodGroup == "All" ||
                d.bloodGroup.trim().equals(selectedBloodGroup, ignoreCase = true)
            val queryMatch = q.isBlank() ||
                d.name.lowercase().contains(q) ||
                d.department.lowercase().contains(q) ||
                d.address.lowercase().contains(q) ||
                d.phone.lowercase().contains(q) ||
                d.roleLabel.lowercase().contains(q)
            groupMatch && queryMatch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalBackdrop(isDark = isDark, focusAccent = bloodAccent.primary) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (topContentPadding > 0.dp) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(topContentPadding))
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 880.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            MinimalSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search donors (${filteredDonors.size})",
                                accent = bloodAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "blood_search_input",
                                modifier = Modifier.weight(1f),
                            )

                            KlassHeaderIconButton(
                                icon = RefreshIcon,
                                contentDescription = "Refresh blood donors",
                                isDark = isDark,
                                containerColor = bloodAccent.softTint,
                                iconTint = bloodAccent.primary,
                                borderColor = bloodAccent.borderTint,
                                isLoading = isLoading,
                                enabled = !isLoading,
                                testTag = "blood_refresh_button",
                                onClick = { refreshDonors() },
                            )
                        }

                        val groupChoices = remember { listOf("All") + RoutineRules.BLOOD_GROUPS }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(groupChoices) { grp ->
                                MinimalIconChip(
                                    label = grp,
                                    selected = selectedBloodGroup == grp,
                                    accent = bloodAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    icon = if (grp == "All") DropletIcon else null,
                                    testTag = "blood_chip_$grp",
                                    onClick = { selectedBloodGroup = grp },
                                )
                            }
                        }
                    }

                    SocialSectionDividerBand(isDark = isDark, height = 4.dp)

                    when {
                        isLoading && donors.isEmpty() -> {
                            KlassRowListSkeleton(
                                isDark = isDark,
                                showHeaderBar = true,
                                showActionPill = false,
                                badgeSize = 46.dp,
                                badgeCornerRadius = 23.dp,
                            )
                        }

                        filteredDonors.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                KlassEmptyState(
                                    icon = DropletIcon,
                                    title = if (isGuest && donors.isEmpty()) {
                                        "Sign in to view donors"
                                    } else {
                                        "No donors found"
                                    },
                                    subtitle = if (isGuest && donors.isEmpty()) {
                                        "Campus donor contacts are protected for verified MPI members."
                                    } else {
                                        "Try another blood group or clear your search filter."
                                    },
                                    accent = bloodAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    actionLabel = if (isGuest && onSignInClick != null && donors.isEmpty()) "Sign In Now" else null,
                                    actionTestTag = "blood_guest_signin_button",
                                    onActionClick = onSignInClick,
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 28.dp),
                            ) {
                                item {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                        MinimalStatBadge(
                                            icon = DropletIcon,
                                            text = "${filteredDonors.size} Verified Donors • $selectedBloodGroup",
                                            accent = bloodAccent.primary,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                        )
                                    }
                                }

                                itemsIndexed(
                                    items = filteredDonors,
                                    key = { idx, d -> "${d.id}_$idx" },
                                ) { index, donor ->
                                    SocialSurfaceBlock(isDark = isDark) {
                                        DonorMinimalRow(
                                            donor = donor,
                                            index = index,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            showDivider = index < filteredDonors.lastIndex,
                                            onClick = { selectedDonor = donor },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        KlassSubScreenHost(visible = selectedDonor != null, zIndex = 80f) {
            selectedDonor?.let { donor ->
                DonorDetailSubScreen(
                    donor = donor,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { selectedDonor = null },
                )
            }
        }
    }
}

@Composable
private fun DonorMinimalRow(
    donor: BloodDonorItem,
    index: Int,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val bloodAccent = KlassFeaturePalette.blood(isDark)
    val callAccent = KlassFeaturePalette.success(isDark)

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("donor_card_$index"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val deptShort = RoutineRules.normalizeDepartmentShort(donor.department)
            val deptAccent = if (deptShort.isNotBlank()) departmentAccent(deptShort, isDark) else bloodAccent
            val subtitleText = listOfNotNull(
                deptShort.takeIf { it.isNotBlank() },
                donor.roleLabel.takeIf { it.isNotBlank() },
            ).joinToString(" • ")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = bloodAccent.softTint,
                    border = BorderStroke(1.dp, bloodAccent.borderTint),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = donor.bloodGroup,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = bloodAccent.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = donor.name.ifBlank { "MPI Member" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = hindSiliguri,
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitleText.isNotBlank()) {
                        Text(
                            text = subtitleText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = hindSiliguri,
                            color = deptAccent.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                KlassHeaderIconButton(
                    icon = PhoneSolidIcon,
                    contentDescription = "Call ${donor.name}",
                    isDark = isDark,
                    containerColor = callAccent.softTint,
                    iconTint = callAccent.primary,
                    borderColor = callAccent.borderTint,
                    visualSize = 42.dp,
                    iconSize = 18.dp,
                    testTag = "call_donor_$index",
                    onClick = { openPhoneDialer(donor.phone) },
                )
            }

            if (showDivider) {
                SocialInsetDivider(startIndent = 78.dp)
            }
        }
    }
}

@Composable
private fun DonorDetailSubScreen(
    donor: BloodDonorItem,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val bloodAccent = KlassFeaturePalette.blood(isDark)
    val callAccent = KlassFeaturePalette.success(isDark)
    val deptAccent = if (donor.department.isNotBlank()) departmentAccent(donor.department, isDark) else bloodAccent

    MinimalFullScreenSheet(
        title = donor.name.ifBlank { "Donor Details" },
        subtitle = "Blood Group ${donor.bloodGroup}",
        levelLabel = "Blood Finder • Level 1",
        icon = DropletIcon,
        accent = bloodAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        closeTestTag = "donor_detail_back_button",
        headerActions = {
            if (donor.phone.isNotBlank()) {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(donor.phone))
                        showToast("Phone number copied!")
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    com.mpi.klass.theme.TelegramAnimatedIcon(
                        icon = CopyIcon,
                        contentDescription = "Copy Phone",
                        tint = bloodAccent.primary,
                        size = 20.dp,
                    )
                }
            }
            HeaderSavePillButton(
                text = "Call",
                accent = callAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                testTag = "donor_detail_call_button",
                onClick = { openPhoneDialer(donor.phone) },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                KlassDetailFieldRow(
                    label = "Blood Group",
                    value = donor.bloodGroup,
                    icon = DropletIcon,
                    accent = bloodAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
                if (donor.roleLabel.isNotBlank()) {
                    SocialInsetDivider(startIndent = 68.dp)
                    KlassDetailFieldRow(
                        label = "Role",
                        value = donor.roleLabel,
                        icon = PersonIcon,
                        accent = KlassFeaturePalette.profile(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }
                if (donor.department.isNotBlank()) {
                    SocialInsetDivider(startIndent = 68.dp)
                    KlassDetailFieldRow(
                        label = "Department",
                        value = donor.department,
                        icon = GraduationCapIcon,
                        accent = deptAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }
                if (donor.phone.isNotBlank()) {
                    SocialInsetDivider(startIndent = 68.dp)
                    KlassDetailFieldRow(
                        label = "Phone (Tap to Copy)",
                        value = donor.phone,
                        icon = PhoneSolidIcon,
                        accent = callAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        onClick = {
                            clipboard.setText(AnnotatedString(donor.phone))
                            showToast("Phone number copied!")
                        },
                        trailingContent = {
                            com.mpi.klass.theme.TelegramAnimatedIcon(
                                icon = CopyIcon,
                                contentDescription = "Copy phone",
                                tint = callAccent.primary,
                                size = 18.dp,
                            )
                        },
                    )
                }
                if (donor.address.isNotBlank()) {
                    SocialInsetDivider(startIndent = 68.dp)
                    KlassDetailFieldRow(
                        label = "Present Address (Tap to Copy)",
                        value = donor.address,
                        icon = LocationDotIcon,
                        accent = KlassFeaturePalette.books(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        onClick = {
                            clipboard.setText(AnnotatedString(donor.address))
                            showToast("Address copied!")
                        },
                    )
                }
            }
        }
    }
}
