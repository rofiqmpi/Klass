package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DropletIcon
import com.mpi.klass.theme.EnvelopeSolidIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PhoneSolidIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.VenusMarsIcon
import com.mpi.klass.theme.dayPaletteAt
import com.mpi.klass.theme.departmentAccent
import kotlinx.coroutines.launch

@Composable
fun TeachersDirectoryScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    canEdit: Boolean = true,
    topContentPadding: Dp = 72.dp,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    val teachersAccent = KlassFeaturePalette.teachers(isDark)
    var teachers by remember { mutableStateOf<List<TeacherDirectoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf("All") }
    var selectedTeacher by remember { mutableStateOf<TeacherDirectoryItem?>(null) }
    var teacherToDelete by remember { mutableStateOf<TeacherDirectoryItem?>(null) }
    var isDeletingTeacher by remember { mutableStateOf(false) }

    val anyOverlay = selectedTeacher != null || teacherToDelete != null
    LaunchedEffect(anyOverlay) {
        onFullScreenOverlayChange(anyOverlay)
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

    val filteredTeachers = remember(teachers, searchQuery, selectedDeptFilter) {
        val q = searchQuery.trim().lowercase()
        teachers.filter { item ->
            val deptOk = selectedDeptFilter == "All" ||
                RoutineRules.normalizeDepartmentShort(item.department).equals(selectedDeptFilter, ignoreCase = true)
            val queryOk = q.isBlank() ||
                item.name.lowercase().contains(q) ||
                item.email.lowercase().contains(q) ||
                item.department.lowercase().contains(q) ||
                item.phone.lowercase().contains(q) ||
                item.shift.lowercase().contains(q)
            deptOk && queryOk
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalBackdrop(isDark = isDark, focusAccent = teachersAccent.primary) {
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
                        .widthIn(max = 960.dp),
                ) {
                    // Clean Search Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search teachers (${filteredTeachers.size})",
                            accent = teachersAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "teachers_search_input",
                            clearTestTag = "teachers_search_clear_button",
                            modifier = Modifier.weight(1f),
                        )

                        KlassHeaderIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh teachers",
                            isDark = isDark,
                            iconTint = teachersAccent.primary,
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "teachers_refresh_button",
                            onClick = { loadTeachers() },
                        )
                    }

                    val deptChoices = remember { listOf("All") + RoutineRules.ALL_TEACHER_DEPARTMENTS }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(deptChoices) { dept ->
                            val chipAcc = if (dept == "All") teachersAccent else departmentAccent(dept, isDark)
                            MinimalIconChip(
                                label = dept,
                                selected = selectedDeptFilter == dept,
                                accent = chipAcc.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { selectedDeptFilter = dept },
                            )
                        }
                    }

                    SocialSectionDividerBand(isDark = isDark, height = 6.dp)

                    when {
                        isLoading && teachers.isEmpty() -> {
                            KlassRowListSkeleton(
                                isDark = isDark,
                                showHeaderBar = true,
                                showActionPill = false,
                                badgeSize = 48.dp,
                                badgeCornerRadius = 24.dp,
                            )
                        }

                        filteredTeachers.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                KlassEmptyState(
                                    icon = GraduationCapIcon,
                                    title = if (searchQuery.isBlank() && selectedDeptFilter == "All") {
                                        "No teachers registered yet"
                                    } else {
                                        "No matching teachers found"
                                    },
                                    subtitle = "Signed-in teachers appear here automatically.",
                                    accent = teachersAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.widthIn(max = 380.dp),
                                )
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
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.email ?: "row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                rowItems.forEach { indexed ->
                                                    val index = indexed.index
                                                    val teacher = indexed.value
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        AnimatedStaggeredItem(index = rowIdx) {
                                                            MessengerTeacherChatCard(
                                                                teacher = teacher,
                                                                canEdit = canEdit,
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
                                            if (rowIdx < indexedRows.lastIndex) {
                                                SocialInsetDivider(startIndent = if (cols == 1) 82.dp else 16.dp)
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
                    canEdit = canEdit,
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
        val displayTarget = target.name.ifBlank { target.email.substringBefore("@") }
        KlassConfirmDialog(
            title = "Remove Teacher",
            message = "Permanently remove $displayTarget (${target.email}) from the database?",
            icon = TrashCanIcon,
            accent = KlassFeaturePalette.error(isDark).primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            confirmText = if (isDeletingTeacher) "Deleting..." else "Delete",
            cancelText = "Cancel",
            isDestructive = true,
            confirmTestTag = "confirm_delete_teacher_button",
            cancelTestTag = "cancel_delete_teacher_button",
            onConfirm = {
                if (!isDeletingTeacher) {
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
                }
            },
            onDismiss = { if (!isDeletingTeacher) teacherToDelete = null },
        )
    }
}

@Composable
private fun MessengerTeacherChatCard(
    teacher: TeacherDirectoryItem,
    canEdit: Boolean,
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
    val deptAcc = if (deptShort.isNotBlank()) departmentAccent(deptShort, isDark) else KlassFeaturePalette.teachers(isDark)
    val bloodAcc = KlassFeaturePalette.blood(isDark)
    val metaLine = remember(deptShort, teacher.shift) {
        listOfNotNull(
            deptShort.takeIf { it.isNotBlank() },
            teacher.shift.takeIf { it.isNotBlank() }?.let { RoutineRules.normalizeShift(it) },
        ).joinToString(" • ")
    }

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = if (canEdit) 6.dp else 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .border(2.dp, deptAcc.primary.copy(alpha = 0.65f), CircleShape)
                    .padding(2.dp)
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

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = teacher.name.ifBlank { teacher.email.substringBefore("@") },
                        color = cs.onSurface,
                        fontSize = 17.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (teacher.bloodGroup.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = bloodAcc.softTint,
                            border = BorderStroke(0.75.dp, bloodAcc.borderTint),
                        ) {
                            Text(
                                text = teacher.bloodGroup,
                                color = bloodAcc.primary,
                                fontSize = 12.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (metaLine.isNotBlank()) {
                    Text(
                        text = metaLine,
                        color = deptAcc.primary,
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (canEdit) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(deleteTestTag),
                ) {
                    com.mpi.klass.theme.TelegramAnimatedIcon(
                        icon = TrashCanIcon,
                        contentDescription = "Remove teacher",
                        tint = KlassFeaturePalette.error(isDark).primary.copy(alpha = 0.85f),
                        size = 19.dp,
                    )
                }
            }
        }
    }
}

private data class TeacherDetailField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
private fun FullScreenTeacherDetailScreen(
    teacher: TeacherDirectoryItem,
    canEdit: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val teachersAccent = KlassFeaturePalette.teachers(isDark)
    val deptShort = teacher.department.substringBefore(" (").trim()
    val deptAcc = if (deptShort.isNotBlank()) departmentAccent(deptShort, isDark) else teachersAccent
    val emptyText = "Not provided"

    var showSubjectsSheet by remember { mutableStateOf(false) }
    var teacherPref by remember(teacher.email) { mutableStateOf<TeacherSchedulingPref?>(null) }
    var isLoadingPref by remember(teacher.email) { mutableStateOf(false) }

    val contactEmail = teacher.contactEmail.ifBlank { teacher.email }
    val fields = remember(teacher, isDark) {
        listOf(
            TeacherDetailField(
                label = "Department",
                value = teacher.department.ifBlank { emptyText },
                icon = GraduationCapIcon,
                accent = deptAcc.primary,
            ),
            TeacherDetailField(
                label = "Shift",
                value = if (teacher.shift.isNotBlank()) RoutineRules.normalizeShift(teacher.shift) else emptyText,
                icon = ClockIcon,
                accent = KlassFeaturePalette.routine(isDark).primary,
            ),
            TeacherDetailField(
                label = "Phone Number",
                value = teacher.phone.ifBlank { emptyText },
                icon = PhoneSolidIcon,
                accent = KlassFeaturePalette.classrooms(isDark).primary,
            ),
            TeacherDetailField(
                label = "Email Address",
                value = contactEmail.ifBlank { emptyText },
                icon = EnvelopeSolidIcon,
                accent = teachersAccent.primary,
            ),
            TeacherDetailField(
                label = "Blood Group",
                value = teacher.bloodGroup.ifBlank { emptyText },
                icon = DropletIcon,
                accent = KlassFeaturePalette.blood(isDark).primary,
            ),
            TeacherDetailField(
                label = "Gender",
                value = teacher.gender.ifBlank { emptyText },
                icon = VenusMarsIcon,
                accent = KlassFeaturePalette.appearance(isDark).primary,
            ),
            TeacherDetailField(
                label = "Present Address",
                value = teacher.address.ifBlank { emptyText },
                icon = LocationDotIcon,
                accent = KlassFeaturePalette.books(isDark).primary,
            ),
            TeacherDetailField(
                label = "Permanent Address",
                value = teacher.permanentAddress.ifBlank { emptyText },
                icon = LocationDotIcon,
                accent = KlassFeaturePalette.books(isDark).primary,
            ),
        )
    }

    LaunchedEffect(showSubjectsSheet, teacher.email) {
        if (showSubjectsSheet && teacherPref == null && teacher.email.isNotBlank()) {
            isLoadingPref = true
            try {
                teacherPref = fetchTeacherSchedulingPrefFromFirestore(teacher.email)
            } finally {
                isLoadingPref = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = "Teacher Profile",
            subtitle = "Faculty Directory",
            levelLabel = "শিক্ষক তালিকা • প্রোফাইল",
            icon = GraduationCapIcon,
            accent = deptAcc.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            closeTestTag = "teacher_detail_close_button",
            maxContentWidth = 760.dp,
            headerActions = {
                if (canEdit) {
                    HeaderSavePillButton(
                        text = "Remove",
                        accent = KlassFeaturePalette.error(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        testTag = "teacher_detail_delete_button",
                        onClick = onDeleteClick,
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .border(3.dp, deptAcc.primary.copy(alpha = 0.72f), CircleShape)
                            .padding(3.5.dp)
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
                        fontSize = 24.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = CircleShape,
                        color = deptAcc.softTint,
                        border = BorderStroke(0.75.dp, deptAcc.borderTint),
                    ) {
                        Text(
                            text = if (deptShort.isNotBlank()) "Faculty • $deptShort" else "Faculty",
                            color = deptAcc.primary,
                            fontSize = 13.5.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    ) {
                        if (teacher.phone.isNotBlank()) {
                            KlassTonalButton(
                                text = "Call",
                                icon = PhoneSolidIcon,
                                accent = KlassFeaturePalette.classrooms(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { openPhoneDialer(teacher.phone) },
                            )
                        }
                        if (contactEmail.isNotBlank()) {
                            KlassTonalButton(
                                text = "Email",
                                icon = EnvelopeSolidIcon,
                                accent = teachersAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { uriHandler.openUri("mailto:$contactEmail") },
                            )
                        }
                        KlassTonalButton(
                            text = "Subjects",
                            icon = BookIcon,
                            accent = KlassFeaturePalette.teaching(isDark).primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { showSubjectsSheet = true },
                        )
                    }
                }

                SocialSectionDividerBand(isDark = isDark)

                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Contact & Academic Info",
                        hindSiliguri = hindSiliguri,
                        accent = deptAcc.primary,
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val useTwoCols = maxWidth >= 560.dp
                        if (useTwoCols) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                            ) {
                                fields.chunked(2).forEachIndexed { idx, pair ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        pair.forEach { field ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                KlassDetailFieldRow(
                                                    label = field.label,
                                                    value = field.value,
                                                    icon = field.icon,
                                                    accent = field.accent,
                                                    isDark = isDark,
                                                    hindSiliguri = hindSiliguri,
                                                    onClick = if (field.value != emptyText) {
                                                        {
                                                            clipboard.setText(AnnotatedString(field.value))
                                                            showToast("Copied ${field.label}")
                                                        }
                                                    } else null,
                                                )
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                    if (idx < (fields.size + 1) / 2 - 1) {
                                        SocialInsetDivider(startIndent = 16.dp, endIndent = 16.dp)
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                fields.forEachIndexed { idx, field ->
                                    KlassDetailFieldRow(
                                        label = field.label,
                                        value = field.value,
                                        icon = field.icon,
                                        accent = field.accent,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        onClick = if (field.value != emptyText) {
                                            {
                                                clipboard.setText(AnnotatedString(field.value))
                                                showToast("Copied ${field.label}")
                                            }
                                        } else null,
                                    )
                                    if (idx < fields.lastIndex) {
                                        SocialInsetDivider(startIndent = 72.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // Level 2: Teacher Assigned Subjects & Preferences Sub-Sub-Screen
        KlassSubScreenHost(
            visible = showSubjectsSheet,
            zIndex = 90f,
        ) {
            MinimalFullScreenSheet(
                title = "Assigned Subjects",
                subtitle = teacher.name.ifBlank { teacher.email.substringBefore("@") },
                levelLabel = "শিক্ষক তালিকা • বিষয়সমূহ",
                icon = BookIcon,
                accent = KlassFeaturePalette.teaching(isDark).primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = { showSubjectsSheet = false },
            ) {
                when {
                    isLoadingPref -> {
                        KlassSubjectsDetailSkeleton(isDark = isDark)
                    }
                    teacherPref == null || teacherPref?.semesterSubjects.orEmpty().isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            KlassEmptyState(
                                icon = BookIcon,
                                title = "No Subjects Configured",
                                subtitle = "This teacher has not saved preferred semester subjects yet.",
                                accent = KlassFeaturePalette.teaching(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                        }
                    }
                    else -> {
                        val pref = teacherPref!!
                        val semEntries = pref.semesterSubjects.entries.toList()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            GoogleSectionHeader(
                                title = "${pref.department.ifBlank { deptShort }} • ${pref.shift} • ${pref.timePreference.label}",
                                accent = KlassFeaturePalette.teaching(isDark).primary,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                                semEntries.forEachIndexed { idx, (semKey, codes) ->
                                    val semAcc = dayPaletteAt(idx, isDark)
                                    KlassDetailFieldRow(
                                        label = RoutineRules.semesterDisplayLabel(semKey),
                                        value = codes.joinToString(", "),
                                        icon = BookIcon,
                                        accent = semAcc.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                    )
                                    if (idx < semEntries.lastIndex) {
                                        SocialInsetDivider(startIndent = 72.dp)
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
