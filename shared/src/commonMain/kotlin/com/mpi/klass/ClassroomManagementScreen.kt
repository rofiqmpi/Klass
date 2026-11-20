package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.MoreVertIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.dayPaletteAt
import com.mpi.klass.theme.departmentAccent
import kotlinx.coroutines.launch

@Composable
fun ClassroomManagementScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    canEdit: Boolean = true,
    topContentPadding: Dp = 72.dp,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val classroomAccent = KlassFeaturePalette.classrooms(isDark)
    val theoryAccent = KlassFeaturePalette.teachers(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)

    var rooms by remember { mutableStateOf<List<ClassroomItem>>(emptyList()) }
    var routinePlan by remember { mutableStateOf<RoutinePlan?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var typeFilter by remember { mutableStateOf<ClassroomType?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var editingRoom by remember { mutableStateOf<ClassroomItem?>(null) }
    var isAddEditorOpen by remember { mutableStateOf(false) }
    var inspectScheduleRoom by remember { mutableStateOf<ClassroomItem?>(null) }
    var confirmDeleteRoom by remember { mutableStateOf<ClassroomItem?>(null) }
    var confirmFreshReset by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

    val anyOverlay = isAddEditorOpen || editingRoom != null || inspectScheduleRoom != null
    LaunchedEffect(anyOverlay) {
        onFullScreenOverlayChange(anyOverlay)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    fun refreshAll() {
        scope.launch {
            isLoading = true
            try {
                rooms = fetchClassroomsFromFirestore()
                routinePlan = fetchRoutinePlanFromFirestore("master_6m_published")
                    ?: fetchRoutinePlanFromFirestore("master_6m")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    val filteredRooms = remember(rooms, typeFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        rooms.filter { r ->
            val typeOk = typeFilter == null || r.roomType == typeFilter
            val queryOk = q.isBlank() ||
                r.roomNumber.lowercase().contains(q) ||
                r.buildingOrFloor.lowercase().contains(q) ||
                r.department.lowercase().contains(q)
            typeOk && queryOk
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalBackdrop(isDark = isDark, focusAccent = classroomAccent.primary) {
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
                    // Single Search + Colored Actions Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search rooms & labs (${filteredRooms.size})",
                            accent = classroomAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                        )

                        WelcomeMinimal3DIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh classrooms",
                            isDark = isDark,
                            containerColor = classroomAccent.softTint,
                            iconTint = classroomAccent.primary,
                            baseColor = classroomAccent.borderTint,
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "classroom_refresh_button",
                            onClick = { refreshAll() },
                        )

                        if (canEdit && rooms.isNotEmpty()) {
                            WelcomeMinimal3DIconButton(
                                icon = TrashCanIcon,
                                contentDescription = "Reset all rooms",
                                isDark = isDark,
                                containerColor = errorAccent.softTint,
                                iconTint = errorAccent.primary,
                                baseColor = errorAccent.borderTint,
                                visualSize = 40.dp,
                                iconSize = 17.dp,
                                testTag = "reset_classrooms_button",
                                onClick = { confirmFreshReset = true },
                            )
                        }
                    }

                    // Colored Filter Chips Row
                    val chips = remember {
                        listOf(
                            null to "All",
                            ClassroomType.THEORY to "Theory",
                            ClassroomType.LAB to "Labs",
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        itemsIndexed(chips) { _, (typeVal, label) ->
                            val chipAccent = when (typeVal) {
                                ClassroomType.THEORY -> theoryAccent.primary
                                ClassroomType.LAB -> classroomAccent.primary
                                null -> KlassFeaturePalette.routine(isDark).primary
                            }
                            MinimalIconChip(
                                label = label,
                                selected = typeFilter == typeVal,
                                accent = chipAccent,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { typeFilter = typeVal },
                            )
                        }
                    }

                    SocialSectionDividerBand(isDark = isDark, height = 6.dp)

                    when {
                        isLoading && rooms.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = classroomAccent.primary, strokeWidth = 2.5.dp)
                            }
                        }

                        filteredRooms.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                KlassEmptyState(
                                    icon = DoorOpenIcon,
                                    title = "No Classrooms Found",
                                    subtitle = if (canEdit) {
                                        "Tap Add Room to configure theory rooms and labs."
                                    } else {
                                        "No classroom schedule data is available yet."
                                    },
                                    accent = classroomAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    actionLabel = if (canEdit) "Add Room" else null,
                                    onActionClick = if (canEdit) {
                                        {
                                            editingRoom = null
                                            isAddEditorOpen = true
                                        }
                                    } else {
                                        null
                                    },
                                )
                            }
                        }

                        else -> {
                            val allEntries = routinePlan?.entries ?: emptyList()
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val cols = if (maxWidth >= 620.dp) 2 else 1
                                val indexedRows = remember(filteredRooms, cols) {
                                    filteredRooms.withIndex().chunked(cols)
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        end = 12.dp,
                                        top = 4.dp,
                                        bottom = if (canEdit) 88.dp else 24.dp,
                                    ),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.id ?: "room_row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            rowItems.forEach { indexed ->
                                                val room = indexed.value
                                                val occupiedCount = remember(allEntries, room.roomNumber) {
                                                    allEntries.count {
                                                        it.roomNumber.trim().equals(room.roomNumber.trim(), ignoreCase = true)
                                                    }
                                                }
                                                val isLab = room.roomType == ClassroomType.LAB
                                                val roomAccent = if (isLab) classroomAccent else theoryAccent
                                                val deptAccent = if (room.department.equals("ALL", true)) {
                                                    roomAccent
                                                } else {
                                                    departmentAccent(room.department, isDark)
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    AnimatedStaggeredItem(index = rowIdx) {
                                                        var menuExpanded by remember { mutableStateOf(false) }

                                                        MinimalCardSurface(
                                                            isDark = isDark,
                                                            shape = RoundedCornerShape(16.dp),
                                                            borderColor = roomAccent.borderTint,
                                                            onClick = { inspectScheduleRoom = room },
                                                            testTag = "room_card_${room.roomNumber}",
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
                                                                Surface(
                                                                    shape = CircleShape,
                                                                    color = roomAccent.softTint,
                                                                    border = BorderStroke(0.75.dp, roomAccent.borderTint),
                                                                    modifier = Modifier.size(44.dp),
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                                        Icon(
                                                                            imageVector = DoorOpenIcon,
                                                                            contentDescription = null,
                                                                            tint = roomAccent.primary,
                                                                            modifier = Modifier.size(20.dp),
                                                                        )
                                                                    }
                                                                }

                                                                Spacer(modifier = Modifier.width(12.dp))

                                                                Column(
                                                                    modifier = Modifier.weight(1f),
                                                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    ) {
                                                                        Text(
                                                                            text = "Room ${room.roomNumber}",
                                                                            fontSize = 16.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = hindSiliguri,
                                                                            color = cs.onSurface,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                        )
                                                                        Surface(
                                                                            shape = CircleShape,
                                                                            color = roomAccent.softTint,
                                                                            border = BorderStroke(0.75.dp, roomAccent.borderTint),
                                                                        ) {
                                                                            Text(
                                                                                text = if (isLab) "Lab" else "Theory",
                                                                                fontSize = 12.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontFamily = hindSiliguri,
                                                                                color = roomAccent.primary,
                                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                                            )
                                                                        }
                                                                    }

                                                                    val subtitle = listOfNotNull(
                                                                        room.department.takeIf { it.isNotBlank() },
                                                                        "$occupiedCount slots",
                                                                        room.buildingOrFloor.takeIf { it.isNotBlank() },
                                                                    ).joinToString(" • ")

                                                                    Text(
                                                                        text = subtitle,
                                                                        fontSize = 13.5.sp,
                                                                        fontFamily = hindSiliguri,
                                                                        color = deptAccent.primary,
                                                                        fontWeight = FontWeight.Medium,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                    )
                                                                }

                                                                if (canEdit) {
                                                                    Box {
                                                                        IconButton(
                                                                            onClick = { menuExpanded = true },
                                                                            modifier = Modifier.size(48.dp),
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = MoreVertIcon,
                                                                                contentDescription = "Room actions",
                                                                                tint = cs.onSurfaceVariant,
                                                                                modifier = Modifier.size(18.dp),
                                                                            )
                                                                        }

                                                                        DropdownMenu(
                                                                            expanded = menuExpanded,
                                                                            onDismissRequest = { menuExpanded = false },
                                                                            containerColor = cs.surfaceContainerLow,
                                                                        ) {
                                                                            DropdownMenuItem(
                                                                                text = { Text("Edit", fontFamily = hindSiliguri) },
                                                                                leadingIcon = {
                                                                                    Icon(
                                                                                        imageVector = EditIcon,
                                                                                        contentDescription = null,
                                                                                        tint = classroomAccent.primary,
                                                                                        modifier = Modifier.size(18.dp),
                                                                                    )
                                                                                },
                                                                                onClick = {
                                                                                    menuExpanded = false
                                                                                    editingRoom = room
                                                                                    isAddEditorOpen = true
                                                                                },
                                                                            )
                                                                            DropdownMenuItem(
                                                                                text = { Text("Delete", color = errorAccent.primary, fontFamily = hindSiliguri) },
                                                                                leadingIcon = {
                                                                                    Icon(
                                                                                        imageVector = TrashCanIcon,
                                                                                        contentDescription = null,
                                                                                        tint = errorAccent.primary,
                                                                                        modifier = Modifier.size(18.dp),
                                                                                    )
                                                                                },
                                                                                onClick = {
                                                                                    menuExpanded = false
                                                                                    confirmDeleteRoom = room
                                                                                },
                                                                            )
                                                                        }
                                                                    }
                                                                } else {
                                                                    Icon(
                                                                        imageVector = ChevronRightIcon,
                                                                        contentDescription = null,
                                                                        tint = cs.onSurfaceVariant.copy(alpha = 0.7f),
                                                                        modifier = Modifier
                                                                            .padding(end = 10.dp)
                                                                            .size(18.dp),
                                                                    )
                                                                }
                                                            }
                                                        }
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

        if (canEdit && !anyOverlay) {
            FloatingActionButton(
                onClick = {
                    editingRoom = null
                    isAddEditorOpen = true
                },
                containerColor = classroomAccent.primary,
                contentColor = if (isDark) Color(0xFF064E3B) else Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .testTag("add_classroom_button"),
            ) {
                Icon(
                    imageVector = PlusIcon,
                    contentDescription = "Add room",
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        KlassSubScreenHost(visible = isAddEditorOpen, zIndex = 80f) {
            RoomEditorFullScreen(
                initialRoom = editingRoom,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                isSaving = isBusy,
                onDismiss = {
                    if (!isBusy) {
                        isAddEditorOpen = false
                        editingRoom = null
                    }
                },
                onSave = { roomToSave ->
                    scope.launch {
                        isBusy = true
                        try {
                            val res = saveClassroomInFirestore(roomToSave)
                            if (res.isSuccess) {
                                showToast("Room saved successfully!")
                                isAddEditorOpen = false
                                editingRoom = null
                                refreshAll()
                            } else {
                                showToast("Failed to save room.")
                            }
                        } finally {
                            isBusy = false
                        }
                    }
                },
            )
        }

        KlassSubScreenHost(visible = inspectScheduleRoom != null, zIndex = 80f) {
            inspectScheduleRoom?.let { inspecting ->
                RoomScheduleViewerFullScreen(
                    room = inspecting,
                    plan = routinePlan,
                    canEdit = canEdit,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onEditRoom = {
                        editingRoom = inspecting
                        inspectScheduleRoom = null
                        isAddEditorOpen = true
                    },
                    onDismiss = { inspectScheduleRoom = null },
                )
            }
        }
    }

    val toDelete = confirmDeleteRoom
    if (toDelete != null) {
        KlassConfirmDialog(
            title = "Delete Room ${toDelete.roomNumber}?",
            message = "This removes Room ${toDelete.roomNumber} from the classroom inventory.",
            icon = TrashCanIcon,
            accent = errorAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            confirmText = "Delete",
            cancelText = "Cancel",
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    isBusy = true
                    try {
                        deleteClassroomFromFirestore(toDelete.id)
                        confirmDeleteRoom = null
                        refreshAll()
                        showToast("Room deleted.")
                    } finally {
                        isBusy = false
                    }
                }
            },
            onDismiss = { if (!isBusy) confirmDeleteRoom = null },
        )
    }

    if (confirmFreshReset) {
        KlassConfirmDialog(
            title = "Clear All Rooms?",
            message = "All existing classrooms and labs will be deleted for fresh entry.",
            icon = TrashCanIcon,
            accent = errorAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            confirmText = "Delete All",
            cancelText = "Cancel",
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    isBusy = true
                    try {
                        deleteAllClassroomsFromFirestore()
                        confirmFreshReset = false
                        refreshAll()
                        showToast("All classrooms cleared for fresh entry.")
                    } finally {
                        isBusy = false
                    }
                }
            },
            onDismiss = { if (!isBusy) confirmFreshReset = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomEditorFullScreen(
    initialRoom: ClassroomItem?,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ClassroomItem) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val classroomAccent = KlassFeaturePalette.classrooms(isDark)
    val theoryAccent = KlassFeaturePalette.teachers(isDark)

    var roomNumber by remember(initialRoom) { mutableStateOf(initialRoom?.roomNumber ?: "") }
    var buildingOrFloor by remember(initialRoom) { mutableStateOf(initialRoom?.buildingOrFloor ?: "") }
    var roomType by remember(initialRoom) { mutableStateOf(initialRoom?.roomType ?: ClassroomType.THEORY) }
    var department by remember(initialRoom) { mutableStateOf(initialRoom?.department ?: "ALL") }
    var capacityStr by remember(initialRoom) { mutableStateOf((initialRoom?.capacity ?: 40).toString()) }
    var shift1Enabled by remember(initialRoom) {
        mutableStateOf(initialRoom == null || RoutineRules.SHIFT_1 in initialRoom.shifts)
    }
    var shift2Enabled by remember(initialRoom) {
        mutableStateOf(initialRoom == null || RoutineRules.SHIFT_2 in initialRoom.shifts)
    }
    var showDeptPicker by remember { mutableStateOf(false) }

    fun submitRoom() {
        val cleanRoom = roomNumber.trim()
        if (cleanRoom.isBlank()) {
            showToast("Please enter a room number or lab name.")
            return
        }
        val shifts = buildList {
            if (shift1Enabled) add(RoutineRules.SHIFT_1)
            if (shift2Enabled) add(RoutineRules.SHIFT_2)
        }
        if (shifts.isEmpty()) {
            showToast("Select at least one shift.")
            return
        }
        val safeId = initialRoom?.id?.takeIf { it.isNotBlank() }
            ?: ("room_" + cleanRoom.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_') + "_" + getCurrentTimeMillis())
        onSave(
            ClassroomItem(
                id = safeId,
                roomNumber = cleanRoom,
                buildingOrFloor = buildingOrFloor.trim(),
                roomType = roomType,
                department = department,
                shifts = shifts,
                capacity = capacityStr.toIntOrNull()?.coerceIn(5, 300) ?: 40,
                updatedAt = getCurrentTimeMillis(),
            )
        )
    }

    val activeTypeAccent = if (roomType == ClassroomType.LAB) classroomAccent else theoryAccent
    val deptAccent = if (department.equals("ALL", true)) classroomAccent else departmentAccent(department, isDark)

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = if (initialRoom == null) "Add Classroom / Lab" else "Edit Room ${initialRoom.roomNumber}",
            subtitle = "Classroom Inventory",
            levelLabel = "Classrooms • Level 1",
            icon = DoorOpenIcon,
            accent = activeTypeAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            dismissEnabled = !isSaving,
            maxContentWidth = 760.dp,
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GoogleOutlinedButton(
                        text = "Cancel",
                        hindSiliguri = hindSiliguri,
                        enabled = !isSaving,
                        height = 48.dp,
                        contentColor = cs.onSurface,
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    )
                    WelcomeMinimal3DButton(
                        text = if (initialRoom == null) "Save Room" else "Update Room",
                        icon = CheckIcon,
                        hindSiliguri = hindSiliguri,
                        isDark = isDark,
                        isLoading = isSaving,
                        customPrimaryColor = classroomAccent.primary,
                        customBaseColor = classroomAccent.deep,
                        height = 48.dp,
                        cornerRadius = 16.dp,
                        modifier = Modifier.weight(1.3f),
                        testTag = "save_room_button",
                        onClick = { submitRoom() },
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Room Details",
                        hindSiliguri = hindSiliguri,
                        accent = activeTypeAccent.primary,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val twoCol = maxWidth >= 560.dp
                            if (twoCol) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    ClassroomTextInput(
                                        label = "Room Number / Lab *",
                                        value = roomNumber,
                                        placeholder = "e.g. 301 or CST-Lab-1",
                                        accent = activeTypeAccent.primary,
                                        onValueChange = { roomNumber = it },
                                        hindSiliguri = hindSiliguri,
                                        modifier = Modifier.weight(1f),
                                    )
                                    ClassroomTextInput(
                                        label = "Building / Floor",
                                        value = buildingOrFloor,
                                        placeholder = "e.g. Academic Bldg 2, 3F",
                                        accent = activeTypeAccent.primary,
                                        onValueChange = { buildingOrFloor = it },
                                        hindSiliguri = hindSiliguri,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ClassroomTextInput(
                                        label = "Room Number / Lab *",
                                        value = roomNumber,
                                        placeholder = "e.g. 301 or CST-Lab-1",
                                        accent = activeTypeAccent.primary,
                                        onValueChange = { roomNumber = it },
                                        hindSiliguri = hindSiliguri,
                                    )
                                    ClassroomTextInput(
                                        label = "Building / Floor",
                                        value = buildingOrFloor,
                                        placeholder = "e.g. Academic Bldg 2, 3F",
                                        accent = activeTypeAccent.primary,
                                        onValueChange = { buildingOrFloor = it },
                                        hindSiliguri = hindSiliguri,
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Room Category",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                color = cs.onSurfaceVariant,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ClassroomType.entries.forEach { t ->
                                    val tAccent = if (t == ClassroomType.LAB) classroomAccent.primary else theoryAccent.primary
                                    MinimalIconChip(
                                        label = t.label,
                                        selected = roomType == t,
                                        accent = tAccent,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        modifier = Modifier.weight(1f),
                                        onClick = { roomType = t },
                                    )
                                }
                            }
                        }

                        ClassroomTextInput(
                            label = "Seating Capacity",
                            value = capacityStr,
                            placeholder = "40",
                            accent = activeTypeAccent.primary,
                            keyboardType = KeyboardType.Number,
                            onValueChange = { capacityStr = it.filter { c -> c.isDigit() } },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }

                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "Assignment & Shifts",
                        hindSiliguri = hindSiliguri,
                        accent = deptAccent.primary,
                    )
                    KlassNavigationRow(
                        title = "Assigned Department",
                        subtitle = if (department.equals("ALL", true)) "ALL (Shared across departments)" else "$department Department",
                        icon = GraduationCapIcon,
                        accent = deptAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        badgeText = department,
                        onClick = { showDeptPicker = true },
                    )
                    SocialInsetDivider(startIndent = 16.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MinimalIconChip(
                            label = RoutineRules.SHIFT_1,
                            selected = shift1Enabled,
                            accent = classroomAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                            onClick = { shift1Enabled = !shift1Enabled },
                        )
                        MinimalIconChip(
                            label = RoutineRules.SHIFT_2,
                            selected = shift2Enabled,
                            accent = classroomAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                            onClick = { shift2Enabled = !shift2Enabled },
                        )
                    }
                }
            }
        }

        // Level 2 Sub-Sub-Screen: Department Picker
        KlassSubScreenHost(visible = showDeptPicker, zIndex = 90f) {
            KlassSelectionPickerSubScreen(
                title = "Assigned Department",
                subtitle = "Choose primary department or Shared",
                levelLabel = "Room Editor • Level 2",
                icon = GraduationCapIcon,
                accent = deptAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                options = listOf("ALL") + RoutineRules.TECH_DEPARTMENTS,
                selectedOption = department,
                optionLabel = { if (it == "ALL") "ALL (Shared)" else it },
                optionSubtitle = { if (it == "ALL") "Available for all departments" else "$it dedicated room" },
                onSelect = { department = it },
                onDismiss = { showDeptPicker = false },
            )
        }
    }
}

@Composable
private fun RoomScheduleViewerFullScreen(
    room: ClassroomItem,
    plan: RoutinePlan?,
    canEdit: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onEditRoom: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isLab = room.roomType == ClassroomType.LAB
    val roomAccent = if (isLab) KlassFeaturePalette.classrooms(isDark) else KlassFeaturePalette.teachers(isDark)
    val freeAccent = KlassFeaturePalette.success(isDark)

    var selectedShift by remember {
        mutableStateOf(RoutineRules.normalizeShift(room.shifts.firstOrNull() ?: RoutineRules.SHIFT_1))
    }
    var inspectingSlot by remember { mutableStateOf<Pair<RoutineEntry, Int>?>(null) }

    val config = plan?.config ?: RoutineConfig()
    val periodsCount = config.periodsPerDay.coerceIn(4, 10)
    val roomEntries = remember(plan, room.roomNumber, selectedShift) {
        (plan?.entries ?: emptyList()).filter {
            it.roomNumber.trim().equals(room.roomNumber.trim(), ignoreCase = true) &&
                RoutineRules.normalizeShift(it.shift) == RoutineRules.normalizeShift(selectedShift)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = "Room ${room.roomNumber} Schedule",
            subtitle = "${room.roomType.label} • ${room.department}",
            levelLabel = "Classroom Schedule • Level 1",
            icon = DoorOpenIcon,
            accent = roomAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            maxContentWidth = 880.dp,
            headerActions = {
                if (canEdit) {
                    KlassTonalButton(
                        text = "Edit",
                        icon = EditIcon,
                        accent = roomAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 36.dp,
                        onClick = onEditRoom,
                    )
                }
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2).forEach { sh ->
                        MinimalIconChip(
                            label = sh,
                            selected = selectedShift == sh,
                            accent = roomAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedShift = sh },
                        )
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val cols = if (maxWidth >= 640.dp) 2 else 1
                    val dayRows = remember(cols) { RoutineRules.WORKING_DAYS.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                KlassHeroBanner(
                                    title = "Room ${room.roomNumber}",
                                    subtitle = room.buildingOrFloor.ifBlank { "${room.roomType.label} Space" },
                                    icon = DoorOpenIcon,
                                    accent = roomAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    badges = listOf(
                                        room.roomType.label to roomAccent.primary,
                                        "Cap ${room.capacity}" to KlassFeaturePalette.books(isDark).primary,
                                        "${roomEntries.size} Occupied" to KlassFeaturePalette.routine(isDark).primary,
                                    ),
                                )
                            }
                            SocialSectionDividerBand(isDark = isDark, height = 8.dp)
                        }

                        itemsIndexed(dayRows) { rowIdx, rowDays ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                            ) {
                                rowDays.forEach { day ->
                                    val dayIdx = RoutineRules.WORKING_DAYS.indexOf(day).coerceAtLeast(0)
                                    val dayAccent = dayPaletteAt(dayIdx, isDark)
                                    Box(modifier = Modifier.weight(1f)) {
                                        SocialSurfaceBlock(
                                            isDark = isDark,
                                            showTopHairline = true,
                                            showBottomHairline = true,
                                            accentTint = dayAccent.primary,
                                        ) {
                                            GoogleSectionHeader(
                                                title = day,
                                                hindSiliguri = hindSiliguri,
                                                accent = dayAccent.primary,
                                            )
                                            for (p in 0 until periodsCount) {
                                                val slot = roomEntries.firstOrNull {
                                                    it.day.equals(day, ignoreCase = true) && it.periodIndex == p
                                                }
                                                val (st, et) = RoutineRules.periodTimeRange(selectedShift, p, config)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .then(
                                                            if (slot != null) {
                                                                Modifier.clickable { inspectingSlot = slot to dayIdx }
                                                            } else {
                                                                Modifier
                                                            }
                                                        )
                                                        .padding(horizontal = 16.dp, vertical = 11.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (slot == null) freeAccent.softTint else dayAccent.softTint,
                                                        border = BorderStroke(
                                                            0.75.dp,
                                                            if (slot == null) freeAccent.borderTint else dayAccent.borderTint,
                                                        ),
                                                    ) {
                                                        Text(
                                                            text = "P${p + 1} • $st",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = hindSiliguri,
                                                            color = if (slot == null) freeAccent.primary else dayAccent.primary,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    if (slot == null) {
                                                        Text(
                                                            text = "Free ($st–$et)",
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            fontFamily = hindSiliguri,
                                                            color = freeAccent.primary,
                                                        )
                                                    } else {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "${slot.subjectTitle} (${slot.subjectCode})",
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                fontFamily = hindSiliguri,
                                                                color = cs.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                            Text(
                                                                text = "${slot.department} • ${RoutineRules.semesterShortLabel(slot.semester)} • ${slot.group} • ${slot.teacherName}",
                                                                fontSize = 13.5.sp,
                                                                fontFamily = hindSiliguri,
                                                                color = cs.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = ChevronRightIcon,
                                                            contentDescription = null,
                                                            tint = dayAccent.primary,
                                                            modifier = Modifier.size(17.dp),
                                                        )
                                                    }
                                                }
                                                if (p < periodsCount - 1) {
                                                    SocialInsetDivider(startIndent = 16.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                                if (cols == 2 && rowDays.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            if (rowIdx < dayRows.lastIndex) {
                                SocialSectionDividerBand(isDark = isDark)
                            }
                        }
                    }
                }
            }
        }

        // Level 2 Sub-Sub-Screen: Occupied Room Slot Details
        KlassSubScreenHost(visible = inspectingSlot != null, zIndex = 90f) {
            inspectingSlot?.let { (slot, dayIdx) ->
                val dayAccent = dayPaletteAt(dayIdx, isDark)
                val deptAccent = departmentAccent(slot.department, isDark)
                MinimalFullScreenSheet(
                    title = slot.subjectTitle,
                    subtitle = "${slot.day} • Period ${slot.periodIndex + 1}",
                    levelLabel = "Room Slot • Level 2",
                    icon = BookIcon,
                    accent = dayAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { inspectingSlot = null },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            KlassHeroBanner(
                                title = slot.subjectTitle,
                                subtitle = "Code ${slot.subjectCode} • ${slot.startTime}–${slot.endTime}",
                                icon = BookIcon,
                                accent = dayAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                badges = listOf(
                                    "Room ${slot.roomNumber}" to roomAccent.primary,
                                    (if (slot.isPractical) "Practical" else "Theory") to KlassFeaturePalette.teaching(isDark).primary,
                                ),
                            )
                        }

                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            KlassDetailFieldRow(
                                label = "Department & Group",
                                value = "${slot.department} • ${RoutineRules.semesterDisplayLabel(slot.semester)} • ${slot.group}",
                                icon = LayerGroupIcon,
                                accent = deptAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Instructor",
                                value = slot.teacherName.ifBlank { slot.teacherEmail },
                                icon = PersonIcon,
                                accent = KlassFeaturePalette.teachers(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Shift & Timing",
                                value = "${slot.shift} • ${slot.day} (${slot.startTime} – ${slot.endTime})",
                                icon = ClockIcon,
                                accent = dayAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            SocialInsetDivider(startIndent = 68.dp)
                            KlassDetailFieldRow(
                                label = "Session Type",
                                value = if (slot.isPractical) "Practical / Lab Class" else "Theory Lecture",
                                icon = SparkleIcon,
                                accent = KlassFeaturePalette.ai(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassroomTextInput(
    label: String,
    value: String,
    placeholder: String,
    accent: Color,
    onValueChange: (String) -> Unit,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val cs = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = {
            Text(
                text = label,
                fontFamily = hindSiliguri,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = hindSiliguri,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = hindSiliguri,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = cs.outlineVariant,
            focusedLabelColor = accent,
            unfocusedLabelColor = cs.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
