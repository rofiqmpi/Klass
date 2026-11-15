package com.mpi.klass

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.RefreshIcon
import kotlinx.coroutines.launch

@Composable
fun BloodFinderScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    isGuest: Boolean = false,
    onSignInClick: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var donors by remember { mutableStateOf<List<BloodDonorItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedBloodGroup by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val crimson = Color(0xFFDC2626)

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

    MinimalBackdrop(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (topContentPadding > 0.dp) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Spacer(modifier = Modifier.height(topContentPadding))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 920.dp)
                    .padding(horizontal = 16.dp),
            ) {
                // Clean Minimal Toolbar: Search Bar + Donor Count Badge + Refresh Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MinimalSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search name, dept, city or phone...",
                        accent = crimson,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        testTag = "blood_search_input",
                        modifier = Modifier.weight(1f),
                    )

                    MinimalStatBadge(
                        icon = DropletIcon,
                        text = "${filteredDonors.size}",
                        accent = crimson,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )

                    WelcomeMinimal3DIconButton(
                        icon = RefreshIcon,
                        contentDescription = "Refresh blood donors",
                        isDark = isDark,
                        iconTint = crimson,
                        isLoading = isLoading,
                        enabled = !isLoading,
                        testTag = "blood_refresh_button",
                        onClick = { refreshDonors() },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Blood Group Filter Strip
                val groupChoices = remember { listOf("All") + RoutineRules.BLOOD_GROUPS }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    items(groupChoices) { grp ->
                        MinimalIconChip(
                            label = grp,
                            selected = selectedBloodGroup == grp,
                            accent = crimson,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            icon = if (grp == "All") DropletIcon else null,
                            testTag = "blood_chip_$grp",
                            onClick = { selectedBloodGroup = grp },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading && donors.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = crimson, strokeWidth = 2.5.dp)
                        }
                    }

                    filteredDonors.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            MinimalCardSurface(
                                isDark = isDark,
                                modifier = Modifier.widthIn(max = 380.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = crimson.copy(alpha = if (isDark) 0.16f else 0.10f),
                                        border = BorderStroke(1.dp, crimson.copy(alpha = 0.25f)),
                                        modifier = Modifier.size(52.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = DropletIcon,
                                                contentDescription = null,
                                                tint = crimson,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (isGuest && donors.isEmpty()) {
                                            "Sign in to view live donors"
                                        } else {
                                            "No donors found"
                                        },
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = textColor,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isGuest && donors.isEmpty()) {
                                            "Registered students, teachers & admins appear here automatically."
                                        } else {
                                            "Try clearing filters or update Blood Group in Profile."
                                        },
                                        fontSize = 12.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = subTextColor,
                                        textAlign = TextAlign.Center,
                                    )
                                    if (isGuest && onSignInClick != null && donors.isEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        WelcomeMinimal3DButton(
                                            text = "Sign In Now",
                                            hindSiliguri = hindSiliguri,
                                            isDark = isDark,
                                            testTag = "blood_guest_signin_button",
                                            onClick = onSignInClick,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val cols = if (maxWidth >= 600.dp) 2 else 1
                            val rows = remember(filteredDonors, cols) {
                                filteredDonors.withIndex().chunked(cols)
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                            ) {
                                items(rows.size) { rowIdx ->
                                    val rowItems = rows[rowIdx]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        rowItems.forEach { indexed ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                AnimatedStaggeredItem(index = indexed.index) {
                                                    DonorCard(
                                                        donor = indexed.value,
                                                        index = indexed.index,
                                                        isDark = isDark,
                                                        crimson = crimson,
                                                        textColor = textColor,
                                                        subTextColor = subTextColor,
                                                        hindSiliguri = hindSiliguri,
                                                    )
                                                }
                                            }
                                        }
                                        if (cols == 2 && rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonorCard(
    donor: BloodDonorItem,
    index: Int,
    isDark: Boolean,
    crimson: Color,
    textColor: Color,
    subTextColor: Color,
    hindSiliguri: FontFamily,
) {
    MinimalCardSurface(
        isDark = isDark,
        testTag = "donor_card_$index",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = crimson.copy(alpha = if (isDark) 0.16f else 0.10f),
                border = BorderStroke(1.dp, crimson.copy(alpha = 0.35f)),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = donor.bloodGroup,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = hindSiliguri,
                        color = crimson,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = donor.name.ifBlank { "MPI Member" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    donor.roleLabel.takeIf { it.isNotBlank() },
                    RoutineRules.normalizeDepartmentShort(donor.department)
                        .takeIf { it.isNotBlank() },
                ).joinToString(" • ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = hindSiliguri,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (donor.address.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LocationDotIcon,
                            contentDescription = null,
                            tint = subTextColor,
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = donor.address,
                            fontSize = 11.5.sp,
                            fontFamily = hindSiliguri,
                            color = subTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = donor.phone,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = hindSiliguri,
                    color = textColor.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            WelcomeMinimal3DIconButton(
                icon = PhoneSolidIcon,
                contentDescription = "Call ${donor.name}",
                isDark = isDark,
                iconTint = Color(0xFF059669),
                testTag = "call_donor_$index",
                onClick = { openPhoneDialer(donor.phone) },
            )
        }
    }
}
