package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.VenusMarsIcon
import kotlinx.coroutines.launch

@Composable
fun TeachersDirectoryScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    var teachers by remember { mutableStateOf<List<TeacherDirectoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTeacher by remember { mutableStateOf<TeacherDirectoryItem?>(null) }
    var teacherToDelete by remember { mutableStateOf<TeacherDirectoryItem?>(null) }
    var isDeletingTeacher by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTeacher != null) {
        onFullScreenOverlayChange(selectedTeacher != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    fun loadTeachers() {
        scope.launch {
            isLoading = true
            try {
                teachers = RoleConfig.fetchLoggedInTeachers()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTeachers()
    }

    val filteredTeachers = remember(teachers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            teachers
        } else {
            teachers.filter { item ->
                item.name.lowercase().contains(q) ||
                    item.email.lowercase().contains(q) ||
                    item.department.lowercase().contains(q) ||
                    item.phone.lowercase().contains(q) ||
                    item.shift.lowercase().contains(q)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 960.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    // Clean Search Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search teachers (${filteredTeachers.size})",
                            accent = cs.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "teachers_search_input",
                            clearTestTag = "teachers_search_clear_button",
                            modifier = Modifier.weight(1f),
                        )

                        WelcomeMinimal3DIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh teachers",
                            isDark = isDark,
                            iconTint = cs.onSurfaceVariant,
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "teachers_refresh_button",
                            onClick = { loadTeachers() },
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        isLoading && teachers.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = cs.primary,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Loading faculty directory...",
                                        color = cs.onSurfaceVariant,
                                        fontSize = 15.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        filteredTeachers.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 28.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                MinimalCardSurface(
                                    isDark = isDark,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.widthIn(max = 380.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = cs.tertiaryContainer,
                                            modifier = Modifier.size(56.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = GraduationCapIcon,
                                                    contentDescription = null,
                                                    tint = cs.onTertiaryContainer,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = if (searchQuery.isBlank()) {
                                                "No teachers registered yet"
                                            } else {
                                                "No matching teachers found"
                                            },
                                            color = cs.onSurface,
                                            fontSize = 16.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                        )
                                        if (searchQuery.isBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Signed-in teachers appear here automatically.",
                                                color = cs.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontFamily = hindSiliguri,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val cols = if (maxWidth >= 620.dp) 2 else 1
                                val indexedRows = remember(filteredTeachers, cols) {
                                    filteredTeachers.withIndex().chunked(cols)
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.email ?: "row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            rowItems.forEach { indexed ->
                                                val index = indexed.index
                                                val teacher = indexed.value
                                                Box(modifier = Modifier.weight(1f)) {
                                                    AnimatedStaggeredItem(index = rowIdx) {
                                                        MessengerTeacherChatCard(
                                                            teacher = teacher,
                                                            isDark = isDark,
                                                            hindSiliguri = hindSiliguri,
                                                            testTag = "teacher_card_$index",
                                                            deleteTestTag = "teacher_delete_button_$index",
                                                            onClick = { selectedTeacher = teacher },
                                                            onDeleteClick = { teacherToDelete = teacher },
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

        AnimatedVisibility(
            visible = selectedTeacher != null,
            enter = fadeIn(tween(220)) + slideInVertically(tween(240)) { it / 6 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 6 },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(80f),
        ) {
            selectedTeacher?.let { teacher ->
                FullScreenTeacherDetailScreen(
                    teacher = teacher,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { selectedTeacher = null },
                    onDeleteClick = {
                        selectedTeacher = null
                        teacherToDelete = teacher
                    },
                )
            }
        }
    }

    teacherToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingTeacher) teacherToDelete = null },
            containerColor = cs.surfaceContainerHigh,
            titleContentColor = cs.onSurface,
            textContentColor = cs.onSurfaceVariant,
            title = {
                Text(
                    text = "Remove Teacher",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Permanently remove this teacher from the database?",
                        fontFamily = hindSiliguri,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cs.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                text = target.name.ifBlank { target.email.substringBefore("@") },
                                color = cs.onSurface,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.5.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = target.email,
                                color = cs.onSurfaceVariant,
                                fontFamily = hindSiliguri,
                                fontSize = 13.5.sp,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                WelcomeMinimal3DButton(
                    text = "Delete",
                    hindSiliguri = hindSiliguri,
                    isDestructive = true,
                    isDark = isDark,
                    isLoading = isDeletingTeacher,
                    height = 40.dp,
                    cornerRadius = 20.dp,
                    testTag = "confirm_delete_teacher_button",
                    onClick = {
                        if (isDeletingTeacher) return@WelcomeMinimal3DButton
                        isDeletingTeacher = true
                        scope.launch {
                            val result = RoleConfig.deleteTeacherFromDatabase(target.email)
                            isDeletingTeacher = false
                            if (result.isSuccess) {
                                teachers = teachers.filterNot { it.email.equals(target.email, ignoreCase = true) }
                                teacherToDelete = null
                                showToast("Teacher removed from database!")
                            } else {
                                showToast("Failed to remove teacher. Try again.")
                            }
                        }
                    },
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { teacherToDelete = null },
                    enabled = !isDeletingTeacher,
                    modifier = Modifier.testTag("cancel_delete_teacher_button"),
                ) {
                    Text(
                        text = "Cancel",
                        color = cs.primary,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

@Composable
private fun MessengerTeacherChatCard(
    teacher: TeacherDirectoryItem,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    testTag: String,
    deleteTestTag: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val deptShort = remember(teacher.department) {
        teacher.department.substringBefore(" (").trim()
    }
    val metaLine = remember(deptShort, teacher.shift, teacher.phone, teacher.contactEmail, teacher.email) {
        listOfNotNull(
            deptShort.takeIf { it.isNotBlank() },
            teacher.shift.takeIf { it.isNotBlank() }?.let { RoutineRules.normalizeShift(it) },
            teacher.phone.takeIf { it.isNotBlank() } ?: teacher.contactEmail.ifBlank { teacher.email }.takeIf { it.isNotBlank() },
        ).joinToString(" • ")
    }

    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        testTag = testTag,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(cs.surfaceContainerHigh, CircleShape),
            ) {
                val resolvedModel = resolveProfileImageModel(teacher.photoUrl)
                if (resolvedModel != null) {
                    AsyncImage(
                        model = resolvedModel,
                        contentDescription = teacher.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    DynamicAvatar(
                        seed = teacher.email.ifBlank { teacher.name },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = teacher.name.ifBlank { teacher.email.substringBefore("@") },
                        color = cs.onSurface,
                        fontSize = 14.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (teacher.bloodGroup.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cs.errorContainer,
                        ) {
                            Text(
                                text = teacher.bloodGroup,
                                color = cs.onErrorContainer,
                                fontSize = 13.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                    }
                }

                if (metaLine.isNotBlank()) {
                    Text(
                        text = metaLine,
                        color = cs.onSurfaceVariant,
                        fontSize = 13.5.sp,
                        fontFamily = hindSiliguri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag(deleteTestTag),
            ) {
                Icon(
                    imageVector = TrashCanIcon,
                    contentDescription = "Remove teacher",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

private data class TeacherDetailField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
private fun FullScreenTeacherDetailScreen(
    teacher: TeacherDirectoryItem,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val emptyText = "Not provided"
    val fields = remember(teacher, cs) {
        listOf(
            TeacherDetailField(
                label = "Department",
                value = teacher.department.ifBlank { emptyText },
                icon = GraduationCapIcon,
                containerColor = cs.primaryContainer,
                contentColor = cs.onPrimaryContainer,
            ),
            TeacherDetailField(
                label = "Shift",
                value = if (teacher.shift.isNotBlank()) RoutineRules.normalizeShift(teacher.shift) else emptyText,
                icon = ClockIcon,
                containerColor = cs.tertiaryContainer,
                contentColor = cs.onTertiaryContainer,
            ),
            TeacherDetailField(
                label = "Phone Number",
                value = teacher.phone.ifBlank { emptyText },
                icon = PhoneSolidIcon,
                containerColor = cs.secondaryContainer,
                contentColor = cs.onSecondaryContainer,
            ),
            TeacherDetailField(
                label = "Email Address",
                value = teacher.contactEmail.ifBlank { teacher.email }.ifBlank { emptyText },
                icon = EnvelopeSolidIcon,
                containerColor = cs.primaryContainer,
                contentColor = cs.onPrimaryContainer,
            ),
            TeacherDetailField(
                label = "Blood Group",
                value = teacher.bloodGroup.ifBlank { emptyText },
                icon = DropletIcon,
                containerColor = cs.errorContainer,
                contentColor = cs.onErrorContainer,
            ),
            TeacherDetailField(
                label = "Gender",
                value = teacher.gender.ifBlank { emptyText },
                icon = VenusMarsIcon,
                containerColor = cs.secondaryContainer,
                contentColor = cs.onSecondaryContainer,
            ),
            TeacherDetailField(
                label = "Present Address",
                value = teacher.address.ifBlank { emptyText },
                icon = LocationDotIcon,
                containerColor = cs.surfaceContainerHigh,
                contentColor = cs.onSurfaceVariant,
            ),
            TeacherDetailField(
                label = "Permanent Address",
                value = teacher.permanentAddress.ifBlank { emptyText },
                icon = LocationDotIcon,
                containerColor = cs.surfaceContainerHigh,
                contentColor = cs.onSurfaceVariant,
            ),
        )
    }

    MinimalFullScreenSheet(
        title = "Teacher Profile",
        subtitle = "Faculty Directory",
        icon = GraduationCapIcon,
        accent = cs.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        closeTestTag = "teacher_detail_close_button",
        maxContentWidth = 760.dp,
        bottomBar = {
            WelcomeMinimal3DButton(
                text = "Remove Teacher",
                icon = TrashCanIcon,
                hindSiliguri = hindSiliguri,
                isDestructive = true,
                isDark = isDark,
                height = 46.dp,
                cornerRadius = 23.dp,
                testTag = "teacher_detail_delete_button",
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteClick,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(cs.surfaceContainerHigh, CircleShape),
            ) {
                val resolvedModel = resolveProfileImageModel(teacher.photoUrl)
                if (resolvedModel != null) {
                    AsyncImage(
                        model = resolvedModel,
                        contentDescription = teacher.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    DynamicAvatar(
                        seed = teacher.email.ifBlank { teacher.name },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = teacher.name.ifBlank { teacher.email.substringBefore("@") },
                color = cs.onSurface,
                fontSize = 21.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = CircleShape,
                color = cs.primaryContainer,
            ) {
                Text(
                    text = "Faculty",
                    color = cs.onPrimaryContainer,
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val useTwoCols = maxWidth >= 560.dp
                if (useTwoCols) {
                    MinimalResponsiveGrid(
                        items = fields,
                        twoColMinWidth = 560.dp,
                        spacing = 10.dp,
                    ) { _, field ->
                        MinimalCardSurface(
                            isDark = isDark,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            TeacherDetailRow(
                                field = field,
                                hindSiliguri = hindSiliguri,
                            )
                        }
                    }
                } else {
                    GoogleSectionHeader(title = "Contact & Academic Info", hindSiliguri = hindSiliguri)
                    Spacer(modifier = Modifier.height(4.dp))
                    MinimalCardSurface(
                        isDark = isDark,
                        shape = RoundedCornerShape(22.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            fields.forEachIndexed { idx, field ->
                                TeacherDetailRow(
                                    field = field,
                                    hindSiliguri = hindSiliguri,
                                )
                                if (idx < fields.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                                        thickness = 0.5.dp,
                                        color = cs.outlineVariant.copy(alpha = 0.55f),
                                    )
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
private fun TeacherDetailRow(
    field: TeacherDetailField,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = field.containerColor,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = field.icon,
                    contentDescription = null,
                    tint = field.contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = field.label,
                color = cs.onSurfaceVariant,
                fontSize = 13.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = field.value,
                color = cs.onSurface,
                fontSize = 14.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun formatTeacherRelativeTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    val diffMs = (getCurrentTimeMillis() - timestampMs).coerceAtLeast(0L)
    val mins = diffMs / 60_000L
    val hours = mins / 60L
    val days = hours / 24L
    return when {
        mins < 2L -> "Now"
        mins < 60L -> "${mins}m"
        hours < 24L -> "${hours}h"
        days < 30L -> "${days}d"
        else -> "${(days / 30L).coerceAtLeast(1L)}mo"
    }
}
