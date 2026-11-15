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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.mpi.klass.theme.RightArrowIcon
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

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val headingColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentBlue = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
    val accentAmber = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)

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
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 960.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    // Clean Minimal Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search teacher, dept or email...",
                            accent = accentAmber,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "teachers_search_input",
                            clearTestTag = "teachers_search_clear_button",
                            modifier = Modifier.weight(1f),
                        )

                        MinimalStatBadge(
                            icon = GraduationCapIcon,
                            text = "${filteredTeachers.size} Faculty",
                            accent = accentAmber,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )

                        WelcomeMinimal3DIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh teachers",
                            isDark = isDark,
                            iconTint = accentAmber,
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "teachers_refresh_button",
                            onClick = { loadTeachers() },
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isLoading && teachers.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = accentAmber,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Loading faculty directory...",
                                        color = subTextColor,
                                        fontSize = 13.5.sp,
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
                                            color = accentAmber.copy(alpha = if (isDark) 0.16f else 0.10f),
                                            border = BorderStroke(1.dp, accentAmber.copy(alpha = 0.25f)),
                                            modifier = Modifier.size(52.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = GraduationCapIcon,
                                                    contentDescription = null,
                                                    tint = accentAmber,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (searchQuery.isBlank()) {
                                                "No teachers registered yet"
                                            } else {
                                                "No matching teachers found"
                                            },
                                            color = headingColor,
                                            fontSize = 15.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        )
                                        if (searchQuery.isBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Signed-in teachers appear here automatically.",
                                                color = subTextColor,
                                                fontSize = 12.5.sp,
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
                                                            cardBg = cardBg,
                                                            cardBorder = cardBorder,
                                                            headingColor = headingColor,
                                                            subTextColor = subTextColor,
                                                            accentBlue = accentBlue,
                                                            accentAmber = accentAmber,
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
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    headingColor = headingColor,
                    subTextColor = subTextColor,
                    accentBlue = accentBlue,
                    accentAmber = accentAmber,
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
            containerColor = cardBg,
            titleContentColor = headingColor,
            textContentColor = subTextColor,
            title = {
                Text(
                    text = "Remove Teacher",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Permanently remove this teacher from the database?",
                        fontFamily = hindSiliguri,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = target.name.ifBlank { target.email.substringBefore("@") },
                                color = headingColor,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = target.email,
                                color = subTextColor,
                                fontFamily = hindSiliguri,
                                fontSize = 12.5.sp,
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
                    cornerRadius = 10.dp,
                    testTag = "confirm_delete_teacher_button",
                    modifier = Modifier.width(96.dp),
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
                        color = subTextColor,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessengerTeacherChatCard(
    teacher: TeacherDirectoryItem,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    headingColor: Color,
    subTextColor: Color,
    accentBlue: Color,
    accentAmber: Color,
    hindSiliguri: FontFamily,
    testTag: String,
    deleteTestTag: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val deptShort = remember(teacher.department) {
        teacher.department.substringBefore(" (").trim()
    }
    val relativeTime = remember(teacher.updatedAt, teacher.registeredAt) {
        formatTeacherRelativeTime(maxOf(teacher.updatedAt, teacher.registeredAt))
    }

    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(16.dp),
        borderColor = cardBorder,
        onClick = onClick,
        testTag = testTag,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(50.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, cardBorder, CircleShape)
                        .background(cardBg, CircleShape),
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

                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                        .border(2.dp, cardBg, CircleShape),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = teacher.name.ifBlank { teacher.email.substringBefore("@") },
                        color = headingColor,
                        fontSize = 15.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (teacher.bloodGroup.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444).copy(alpha = if (isDark) 0.18f else 0.10f),
                        ) {
                            Text(
                                text = teacher.bloodGroup,
                                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                fontSize = 10.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (deptShort.isNotBlank() || teacher.shift.isNotBlank()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (deptShort.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentBlue.copy(alpha = if (isDark) 0.16f else 0.10f),
                            ) {
                                Text(
                                    text = deptShort,
                                    color = accentBlue,
                                    fontSize = 10.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }
                        if (teacher.shift.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentAmber.copy(alpha = if (isDark) 0.16f else 0.10f),
                            ) {
                                Text(
                                    text = RoutineRules.normalizeShift(teacher.shift),
                                    color = accentAmber,
                                    fontSize = 10.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                val contactPreview = buildList {
                    if (teacher.phone.isNotBlank()) add(teacher.phone)
                    val mail = teacher.contactEmail.ifBlank { teacher.email }
                    if (mail.isNotBlank()) add(mail)
                }.joinToString(" • ")

                if (contactPreview.isNotBlank()) {
                    Text(
                        text = contactPreview,
                        color = subTextColor,
                        fontSize = 12.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (relativeTime.isNotBlank()) {
                    Text(
                        text = relativeTime,
                        color = subTextColor,
                        fontSize = 10.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag(deleteTestTag),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444).copy(alpha = if (isDark) 0.14f else 0.08f),
                            modifier = Modifier.size(34.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = TrashCanIcon,
                                    contentDescription = "Remove teacher",
                                    tint = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = RightArrowIcon,
                        contentDescription = null,
                        tint = subTextColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

private data class TeacherDetailField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val iconTint: Color,
)

@Composable
private fun FullScreenTeacherDetailScreen(
    teacher: TeacherDirectoryItem,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    headingColor: Color,
    subTextColor: Color,
    accentBlue: Color,
    accentAmber: Color,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val emptyText = "Not provided"
    val fields = remember(teacher) {
        listOf(
            TeacherDetailField(
                label = "Department",
                value = teacher.department.ifBlank { emptyText },
                icon = GraduationCapIcon,
                iconTint = Color(0xFF3B82F6),
            ),
            TeacherDetailField(
                label = "Shift",
                value = if (teacher.shift.isNotBlank()) RoutineRules.normalizeShift(teacher.shift) else emptyText,
                icon = ClockIcon,
                iconTint = Color(0xFFF59E0B),
            ),
            TeacherDetailField(
                label = "Phone Number",
                value = teacher.phone.ifBlank { emptyText },
                icon = PhoneSolidIcon,
                iconTint = Color(0xFF10B981),
            ),
            TeacherDetailField(
                label = "Email Address",
                value = teacher.contactEmail.ifBlank { teacher.email }.ifBlank { emptyText },
                icon = EnvelopeSolidIcon,
                iconTint = Color(0xFF6366F1),
            ),
            TeacherDetailField(
                label = "Blood Group",
                value = teacher.bloodGroup.ifBlank { emptyText },
                icon = DropletIcon,
                iconTint = Color(0xFFEF4444),
            ),
            TeacherDetailField(
                label = "Gender",
                value = teacher.gender.ifBlank { emptyText },
                icon = VenusMarsIcon,
                iconTint = Color(0xFFEC4899),
            ),
            TeacherDetailField(
                label = "Present Address",
                value = teacher.address.ifBlank { emptyText },
                icon = LocationDotIcon,
                iconTint = Color(0xFFEF4444),
            ),
            TeacherDetailField(
                label = "Permanent Address",
                value = teacher.permanentAddress.ifBlank { emptyText },
                icon = LocationDotIcon,
                iconTint = Color(0xFFF97316),
            ),
        )
    }

    MinimalFullScreenSheet(
        title = "Teacher Profile",
        subtitle = "Faculty Directory",
        icon = GraduationCapIcon,
        accent = accentAmber,
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
                cornerRadius = 12.dp,
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
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, cardBorder, CircleShape)
                    .background(cardBg, CircleShape),
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = teacher.name.ifBlank { teacher.email.substringBefore("@") },
                color = headingColor,
                fontSize = 19.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentBlue.copy(alpha = if (isDark) 0.16f else 0.10f),
            ) {
                Text(
                    text = "Faculty",
                    color = accentBlue,
                    fontSize = 11.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            MinimalResponsiveGrid(
                items = fields,
                twoColMinWidth = 560.dp,
                spacing = 10.dp,
            ) { _, field ->
                TeacherDetailRow(
                    label = field.label,
                    value = field.value,
                    icon = field.icon,
                    iconTint = field.iconTint,
                    headingColor = headingColor,
                    subTextColor = subTextColor,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }
        }
    }
}

@Composable
private fun TeacherDetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    headingColor: Color,
    subTextColor: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconTint.copy(alpha = if (isDark) 0.16f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = subTextColor,
                    fontSize = 11.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = value,
                    color = headingColor,
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                )
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
