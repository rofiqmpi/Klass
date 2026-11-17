package com.mpi.klass

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.MoreVertIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.TrashCanIcon
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
                    // Single Search + Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search rooms & labs (${filteredRooms.size})",
                            accent = cs.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                        )

                        WelcomeMinimal3DIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh classrooms",
                            isDark = isDark,
                            iconTint = cs.onSurfaceVariant,
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
                                containerColor = cs.errorContainer,
                                iconTint = cs.onErrorContainer,
                                visualSize = 38.dp,
                                iconSize = 16.dp,
                                testTag = "reset_classrooms_button",
                                onClick = { confirmFreshReset = true },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slim Filter Row
                    val chips = remember {
                        listOf(
                            null to "All",
                            ClassroomType.THEORY to "Theory",
                            ClassroomType.LAB to "Labs",
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(chips) { (typeVal, label) ->
                            MinimalIconChip(
                                label = label,
                                selected = typeFilter == typeVal,
                                accent = cs.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                onClick = { typeFilter = typeVal },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when {
                        isLoading && rooms.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = cs.primary, strokeWidth = 2.5.dp)
                            }
                        }

                        filteredRooms.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                MinimalCardSurface(
                                    isDark = isDark,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.widthIn(max = 360.dp),
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
                                            modifier = Modifier.size(52.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = DoorOpenIcon,
                                                    contentDescription = null,
                                                    tint = cs.onTertiaryContainer,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No Classrooms Found",
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (canEdit) {
                                                "Tap + to add theory rooms and labs."
                                            } else {
                                                "No classroom schedule data is available yet."
                                            },
                                            fontSize = 13.sp,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
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
                                    contentPadding = PaddingValues(top = 2.dp, bottom = if (canEdit) 88.dp else 24.dp),
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
                                                val badgeBg = if (isLab) cs.tertiaryContainer else cs.secondaryContainer
                                                val badgeFg = if (isLab) cs.onTertiaryContainer else cs.onSecondaryContainer

                                                Box(modifier = Modifier.weight(1f)) {
                                                    AnimatedStaggeredItem(index = rowIdx) {
                                                        var menuExpanded by remember { mutableStateOf(false) }

                                                        MinimalCardSurface(
                                                            isDark = isDark,
                                                            shape = RoundedCornerShape(14.dp),
                                                            onClick = { inspectScheduleRoom = room },
                                                            testTag = "room_card_${room.roomNumber}",
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
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
                                                                            fontSize = 15.sp,
                                                                            fontWeight = FontWeight.SemiBold,
                                                                            fontFamily = hindSiliguri,
                                                                            color = cs.onSurface,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                        )
                                                                        Surface(
                                                                            shape = RoundedCornerShape(6.dp),
                                                                            color = badgeBg,
                                                                        ) {
                                                                            Text(
                                                                                text = if (isLab) "Lab" else "Theory",
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.Medium,
                                                                                fontFamily = hindSiliguri,
                                                                                color = badgeFg,
                                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                            )
                                                                        }
                                                                    }

                                                                    val subtitle = listOfNotNull(
                                                                        room.buildingOrFloor.takeIf { it.isNotBlank() },
                                                                        room.department,
                                                                        "Cap ${room.capacity}",
                                                                        "$occupiedCount slots/wk",
                                                                    ).joinToString(" • ")

                                                                    Text(
                                                                        text = subtitle,
                                                                        fontSize = 12.sp,
                                                                        fontFamily = hindSiliguri,
                                                                        color = cs.onSurfaceVariant,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                    )
                                                                }

                                                                if (canEdit) {
                                                                    Box {
                                                                        IconButton(
                                                                            onClick = { menuExpanded = true },
                                                                            modifier = Modifier.size(44.dp),
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
                                                                                        tint = cs.primary,
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
                                                                                text = { Text("Delete", color = cs.error, fontFamily = hindSiliguri) },
                                                                                leadingIcon = {
                                                                                    Icon(
                                                                                        imageVector = TrashCanIcon,
                                                                                        contentDescription = null,
                                                                                        tint = cs.error,
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
                containerColor = cs.primary,
                contentColor = cs.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
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

        if (isAddEditorOpen) {
            Box(modifier = Modifier.fillMaxSize().zIndex(80f)) {
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
        }

        val inspecting = inspectScheduleRoom
        if (inspecting != null) {
            Box(modifier = Modifier.fillMaxSize().zIndex(80f)) {
                RoomScheduleViewerFullScreen(
                    room = inspecting,
                    plan = routinePlan,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { inspectScheduleRoom = null },
                )
            }
        }
    }

    val toDelete = confirmDeleteRoom
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) confirmDeleteRoom = null },
            containerColor = cs.surfaceContainerHigh,
            titleContentColor = cs.onSurface,
            textContentColor = cs.onSurfaceVariant,
            title = {
                Text(
                    text = "Delete Room ${toDelete.roomNumber}?",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp,
                )
            },
            text = {
                Text(
                    text = "This removes Room ${toDelete.roomNumber} from the classroom inventory.",
                    fontFamily = hindSiliguri,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                WelcomeMinimal3DButton(
                    text = "Delete",
                    hindSiliguri = hindSiliguri,
                    isDestructive = true,
                    isDark = isDark,
                    isLoading = isBusy,
                    height = 40.dp,
                    cornerRadius = 20.dp,
                    onClick = {
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
                )
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteRoom = null }, enabled = !isBusy) {
                    Text("Cancel", color = cs.primary, fontFamily = hindSiliguri, fontWeight = FontWeight.Medium)
                }
            },
        )
    }

    if (confirmFreshReset) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) confirmFreshReset = false },
            containerColor = cs.surfaceContainerHigh,
            titleContentColor = cs.onSurface,
            textContentColor = cs.onSurfaceVariant,
            title = {
                Text(
                    text = "Clear All Rooms?",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp,
                )
            },
            text = {
                Text(
                    text = "All existing classrooms and labs will be deleted for fresh entry.",
                    fontFamily = hindSiliguri,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                WelcomeMinimal3DButton(
                    text = "Delete All",
                    hindSiliguri = hindSiliguri,
                    isDestructive = true,
                    isDark = isDark,
                    isLoading = isBusy,
                    height = 40.dp,
                    cornerRadius = 20.dp,
                    onClick = {
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
                )
            },
            dismissButton = {
                TextButton(onClick = { confirmFreshReset = false }, enabled = !isBusy) {
                    Text("Cancel", color = cs.primary, fontFamily = hindSiliguri, fontWeight = FontWeight.Medium)
                }
            },
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

    fun submitRoom() {
        val cleanRoom = roomNumber.trim()
        if (cleanRoom.isBlank()) {
            showToast("Please enter a room number or lab name.")
            return
        }
        val shifts = buildList {
            if (shift1Enabled) add(RoutineRules.SHIFT_1)
            if (shift2Enabled) add(RoutineRules.SHIFT_2)
        }.ifEmpty { listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2) }
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

    MinimalFullScreenSheet(
        title = if (initialRoom == null) "Add Classroom / Lab" else "Edit Room ${initialRoom.roomNumber}",
        subtitle = "Classroom Inventory",
        icon = DoorOpenIcon,
        accent = cs.primary,
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
                    height = 46.dp,
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
                    height = 46.dp,
                    cornerRadius = 23.dp,
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            GoogleSectionHeader(title = "Room Details", hindSiliguri = hindSiliguri)

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
                            onValueChange = { roomNumber = it },
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                        )
                        ClassroomTextInput(
                            label = "Building / Floor",
                            value = buildingOrFloor,
                            placeholder = "e.g. Academic Bldg 2, 3F",
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
                            onValueChange = { roomNumber = it },
                            hindSiliguri = hindSiliguri,
                        )
                        ClassroomTextInput(
                            label = "Building / Floor",
                            value = buildingOrFloor,
                            placeholder = "e.g. Academic Bldg 2, 3F",
                            onValueChange = { buildingOrFloor = it },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val twoCol = maxWidth >= 560.dp
                if (twoCol) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1.4f)) {
                            GoogleSectionHeader(title = "Room Category", hindSiliguri = hindSiliguri)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ClassroomType.entries.forEach { t ->
                                    MinimalIconChip(
                                        label = t.label,
                                        selected = roomType == t,
                                        accent = cs.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        modifier = Modifier.weight(1f),
                                        onClick = { roomType = t },
                                    )
                                }
                            }
                        }

                        ClassroomTextInput(
                            label = "Capacity",
                            value = capacityStr,
                            placeholder = "40",
                            keyboardType = KeyboardType.Number,
                            onValueChange = { capacityStr = it.filter { c -> c.isDigit() } },
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(0.8f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            GoogleSectionHeader(title = "Room Category", hindSiliguri = hindSiliguri)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ClassroomType.entries.forEach { t ->
                                    MinimalIconChip(
                                        label = t.label,
                                        selected = roomType == t,
                                        accent = cs.primary,
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
                            keyboardType = KeyboardType.Number,
                            onValueChange = { capacityStr = it.filter { c -> c.isDigit() } },
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }
            }

            Column {
                GoogleSectionHeader(title = "Assigned Department", hindSiliguri = hindSiliguri)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (listOf("ALL") + RoutineRules.TECH_DEPARTMENTS).forEach { d ->
                        MinimalIconChip(
                            label = if (d == "ALL") "ALL (Shared)" else d,
                            selected = department.equals(d, ignoreCase = true),
                            accent = cs.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { department = d },
                        )
                    }
                }
            }

            Column {
                GoogleSectionHeader(title = "Available Shifts", hindSiliguri = hindSiliguri)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MinimalIconChip(
                        label = RoutineRules.SHIFT_1,
                        selected = shift1Enabled,
                        accent = cs.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        modifier = Modifier.weight(1f),
                        onClick = { shift1Enabled = !shift1Enabled },
                    )
                    MinimalIconChip(
                        label = RoutineRules.SHIFT_2,
                        selected = shift2Enabled,
                        accent = cs.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        modifier = Modifier.weight(1f),
                        onClick = { shift2Enabled = !shift2Enabled },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomScheduleViewerFullScreen(
    room: ClassroomItem,
    plan: RoutinePlan?,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var selectedShift by remember {
        mutableStateOf(room.shifts.firstOrNull() ?: RoutineRules.SHIFT_1)
    }

    val config = plan?.config ?: RoutineConfig()
    val periodsCount = config.periodsPerDay.coerceIn(4, 10)
    val roomEntries = remember(plan, room.roomNumber, selectedShift) {
        (plan?.entries ?: emptyList()).filter {
            it.roomNumber.trim().equals(room.roomNumber.trim(), ignoreCase = true) &&
                RoutineRules.normalizeShift(it.shift) == RoutineRules.normalizeShift(selectedShift)
        }
    }

    MinimalFullScreenSheet(
        title = "Room ${room.roomNumber} Schedule",
        subtitle = "${room.roomType.label} • ${room.department} • Cap ${room.capacity}",
        icon = DoorOpenIcon,
        accent = cs.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        maxContentWidth = 880.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2).forEach { sh ->
                    MinimalIconChip(
                        label = sh,
                        selected = selectedShift == sh,
                        accent = cs.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedShift = sh },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val cols = if (maxWidth >= 640.dp) 2 else 1
                val dayRows = remember(cols) { RoutineRules.WORKING_DAYS.chunked(cols) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    itemsIndexed(dayRows) { _, rowDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            rowDays.forEach { day ->
                                Box(modifier = Modifier.weight(1f)) {
                                    MinimalCardSurface(isDark = isDark, shape = RoundedCornerShape(18.dp)) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = day,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = hindSiliguri,
                                                color = cs.primary,
                                            )
                                            for (p in 0 until periodsCount) {
                                                val slot = roomEntries.firstOrNull {
                                                    it.day.equals(day, ignoreCase = true) && it.periodIndex == p
                                                }
                                                val (st, et) = RoutineRules.periodTimeRange(selectedShift, p, config)
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (slot == null) {
                                                        cs.secondaryContainer
                                                    } else {
                                                        cs.surfaceContainerHigh
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Text(
                                                            text = "P${p + 1} • $st",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            fontFamily = hindSiliguri,
                                                            color = if (slot == null) cs.onSecondaryContainer else cs.onSurfaceVariant,
                                                            modifier = Modifier.width(82.dp),
                                                        )
                                                        if (slot == null) {
                                                            Text(
                                                                text = "Free ($st–$et)",
                                                                fontSize = 12.5.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                fontFamily = hindSiliguri,
                                                                color = cs.onSecondaryContainer,
                                                            )
                                                        } else {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = "${slot.subjectTitle} (${slot.subjectCode})",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    fontFamily = hindSiliguri,
                                                                    color = cs.onSurface,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis,
                                                                )
                                                                Text(
                                                                    text = "${slot.department} • ${RoutineRules.semesterShortLabel(slot.semester)} • ${slot.group} • ${slot.teacherName}",
                                                                    fontSize = 11.5.sp,
                                                                    fontFamily = hindSiliguri,
                                                                    color = cs.onSurfaceVariant,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis,
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
                            if (cols == 2 && rowDays.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
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
            fontSize = 15.sp,
            fontFamily = hindSiliguri,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = cs.primary,
            unfocusedBorderColor = cs.outlineVariant,
            focusedLabelColor = cs.primary,
            unfocusedLabelColor = cs.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
