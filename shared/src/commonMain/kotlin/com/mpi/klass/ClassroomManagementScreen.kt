package com.mpi.klass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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

    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val accent = Color(0xFF7C3AED)

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

    val theoryCount = remember(rooms) { rooms.count { it.roomType == ClassroomType.THEORY } }
    val labCount = remember(rooms) { rooms.count { it.roomType == ClassroomType.LAB } }

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
                    // Row 1: Minimal Search + Stat Badge + Refresh + Optional Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search room, building or department...",
                            accent = accent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                        )

                        MinimalStatBadge(
                            icon = DoorOpenIcon,
                            text = "${theoryCount}T • ${labCount}L",
                            accent = accent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )

                        WelcomeMinimal3DIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh classrooms",
                            isDark = isDark,
                            iconTint = accent,
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
                                iconTint = Color(0xFFEF4444),
                                testTag = "reset_classrooms_button",
                                onClick = { confirmFreshReset = true },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Filter Chips + Add Room CTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val chips = remember {
                            listOf(
                                null to "All",
                                ClassroomType.THEORY to "Theory",
                                ClassroomType.LAB to "Labs",
                            )
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(chips) { (typeVal, label) ->
                                MinimalIconChip(
                                    label = label,
                                    selected = typeFilter == typeVal,
                                    accent = accent,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    onClick = { typeFilter = typeVal },
                                )
                            }
                        }

                        if (canEdit) {
                            Spacer(modifier = Modifier.width(8.dp))
                            WelcomeMinimal3DButton(
                                text = "Add Room",
                                icon = PlusIcon,
                                hindSiliguri = hindSiliguri,
                                isDark = isDark,
                                height = 40.dp,
                                cornerRadius = 11.dp,
                                modifier = Modifier.width(126.dp),
                                testTag = "add_classroom_button",
                                onClick = {
                                    editingRoom = null
                                    isAddEditorOpen = true
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isLoading && rooms.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = accent, strokeWidth = 2.5.dp)
                            }
                        }

                        filteredRooms.isEmpty() -> {
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
                                            color = accent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                            border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                                            modifier = Modifier.size(52.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = DoorOpenIcon,
                                                    contentDescription = null,
                                                    tint = accent,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No Classrooms Found",
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = textColor,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (canEdit) {
                                                "Add theory rooms and labs for automated zero-overlap scheduling."
                                            } else {
                                                "No classroom schedule data is available yet."
                                            },
                                            fontSize = 12.5.sp,
                                            fontFamily = hindSiliguri,
                                            color = subTextColor,
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.id ?: "room_row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            rowItems.forEach { indexed ->
                                                val room = indexed.value
                                                val occupiedCount = remember(allEntries, room.roomNumber) {
                                                    allEntries.count {
                                                        it.roomNumber.trim().equals(room.roomNumber.trim(), ignoreCase = true)
                                                    }
                                                }
                                                val typeColor = if (room.roomType == ClassroomType.LAB) {
                                                    Color(0xFF7C3AED)
                                                } else {
                                                    Color(0xFF0284C7)
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    AnimatedStaggeredItem(index = rowIdx) {
                                                        MinimalCardSurface(
                                                            isDark = isDark,
                                                            onClick = { inspectScheduleRoom = room },
                                                            testTag = "room_card_${room.roomNumber}",
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    color = typeColor.copy(alpha = if (isDark) 0.16f else 0.10f),
                                                                    border = BorderStroke(1.dp, typeColor.copy(alpha = 0.24f)),
                                                                    modifier = Modifier.size(44.dp),
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        Icon(
                                                                            imageVector = DoorOpenIcon,
                                                                            contentDescription = room.roomNumber,
                                                                            tint = typeColor,
                                                                            modifier = Modifier.size(19.dp),
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
                                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    ) {
                                                                        Text(
                                                                            text = "Room ${room.roomNumber}",
                                                                            fontSize = 15.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = hindSiliguri,
                                                                            color = textColor,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                        )
                                                                        Surface(
                                                                            shape = RoundedCornerShape(6.dp),
                                                                            color = typeColor.copy(alpha = if (isDark) 0.18f else 0.11f),
                                                                        ) {
                                                                            Text(
                                                                                text = if (room.roomType == ClassroomType.LAB) "Lab" else "Theory",
                                                                                fontSize = 10.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontFamily = hindSiliguri,
                                                                                color = typeColor,
                                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                            )
                                                                        }
                                                                    }

                                                                    if (room.buildingOrFloor.isNotBlank()) {
                                                                        Text(
                                                                            text = room.buildingOrFloor,
                                                                            fontSize = 11.5.sp,
                                                                            fontFamily = hindSiliguri,
                                                                            color = subTextColor,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis,
                                                                        )
                                                                    }

                                                                    Text(
                                                                        text = "${room.department} • Cap ${room.capacity} • $occupiedCount slots/wk",
                                                                        fontSize = 11.5.sp,
                                                                        fontFamily = hindSiliguri,
                                                                        fontWeight = FontWeight.Medium,
                                                                        color = subTextColor,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                    )
                                                                }

                                                                if (canEdit) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        WelcomeMinimal3DIconButton(
                                                                            icon = EditIcon,
                                                                            contentDescription = "Edit room",
                                                                            isDark = isDark,
                                                                            iconTint = Color(0xFF0284C7),
                                                                            visualSize = 34.dp,
                                                                            iconSize = 14.dp,
                                                                            onClick = {
                                                                                editingRoom = room
                                                                                isAddEditorOpen = true
                                                                            },
                                                                        )
                                                                        WelcomeMinimal3DIconButton(
                                                                            icon = TrashCanIcon,
                                                                            contentDescription = "Delete room",
                                                                            isDark = isDark,
                                                                            iconTint = Color(0xFFEF4444),
                                                                            visualSize = 34.dp,
                                                                            iconSize = 14.dp,
                                                                            onClick = { confirmDeleteRoom = room },
                                                                        )
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
            containerColor = cardBg,
            title = {
                Text(
                    text = "Delete Room ${toDelete.roomNumber}?",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                )
            },
            text = {
                Text(
                    text = "This removes Room ${toDelete.roomNumber} from the classroom inventory.",
                    fontFamily = hindSiliguri,
                    color = subTextColor,
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
                    cornerRadius = 10.dp,
                    modifier = Modifier.width(96.dp),
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
                    Text("Cancel", color = subTextColor, fontFamily = hindSiliguri, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    if (confirmFreshReset) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) confirmFreshReset = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "Clear All Rooms?",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                )
            },
            text = {
                Text(
                    text = "All existing classrooms and labs will be deleted for fresh entry.",
                    fontFamily = hindSiliguri,
                    color = subTextColor,
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
                    cornerRadius = 10.dp,
                    modifier = Modifier.width(108.dp),
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
                    Text("Cancel", color = subTextColor, fontFamily = hindSiliguri, fontWeight = FontWeight.Bold)
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
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val accent = Color(0xFF7C3AED)

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
        accent = accent,
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
                WelcomeMinimal3DButton(
                    text = "Cancel",
                    hindSiliguri = hindSiliguri,
                    isPrimaryAccent = false,
                    isDark = isDark,
                    enabled = !isSaving,
                    height = 46.dp,
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
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(1f),
                        )
                        ClassroomTextInput(
                            label = "Building / Floor",
                            value = buildingOrFloor,
                            placeholder = "e.g. Academic Bldg 2, 3F",
                            onValueChange = { buildingOrFloor = it },
                            isDark = isDark,
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
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                        ClassroomTextInput(
                            label = "Building / Floor",
                            value = buildingOrFloor,
                            placeholder = "e.g. Academic Bldg 2, 3F",
                            onValueChange = { buildingOrFloor = it },
                            isDark = isDark,
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
                            Text(
                                text = "Room Category",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = textColor,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ClassroomType.entries.forEach { t ->
                                    MinimalIconChip(
                                        label = t.label,
                                        selected = roomType == t,
                                        accent = accent,
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
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            modifier = Modifier.weight(0.8f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            Text(
                                text = "Room Category",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = textColor,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ClassroomType.entries.forEach { t ->
                                    MinimalIconChip(
                                        label = t.label,
                                        selected = roomType == t,
                                        accent = accent,
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
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Assigned Department",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (listOf("ALL") + RoutineRules.TECH_DEPARTMENTS).forEach { d ->
                        MinimalIconChip(
                            label = if (d == "ALL") "ALL (Shared)" else d,
                            selected = department.equals(d, ignoreCase = true),
                            accent = Color(0xFF0284C7),
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { department = d },
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Available Shifts",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MinimalIconChip(
                        label = RoutineRules.SHIFT_1,
                        selected = shift1Enabled,
                        accent = Color(0xFF10B981),
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        modifier = Modifier.weight(1f),
                        onClick = { shift1Enabled = !shift1Enabled },
                    )
                    MinimalIconChip(
                        label = RoutineRules.SHIFT_2,
                        selected = shift2Enabled,
                        accent = Color(0xFF10B981),
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
    var selectedShift by remember {
        mutableStateOf(room.shifts.firstOrNull() ?: RoutineRules.SHIFT_1)
    }

    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val accent = Color(0xFF7C3AED)

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
        accent = accent,
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
                        accent = accent,
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
                                    MinimalCardSurface(isDark = isDark) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = day,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = accent,
                                            )
                                            for (p in 0 until periodsCount) {
                                                val slot = roomEntries.firstOrNull {
                                                    it.day.equals(day, ignoreCase = true) && it.periodIndex == p
                                                }
                                                val (st, et) = RoutineRules.periodTimeRange(selectedShift, p, config)
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (slot == null) {
                                                        Color(0xFF10B981).copy(alpha = if (isDark) 0.14f else 0.08f)
                                                    } else {
                                                        if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                                                    },
                                                    border = BorderStroke(
                                                        0.8.dp,
                                                        if (slot == null) Color(0xFF10B981).copy(alpha = 0.36f) else borderCol,
                                                    ),
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Text(
                                                            text = "P${p + 1} • $st",
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = hindSiliguri,
                                                            color = subTextColor,
                                                            modifier = Modifier.width(82.dp),
                                                        )
                                                        if (slot == null) {
                                                            Text(
                                                                text = "Free ($st–$et)",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontFamily = hindSiliguri,
                                                                color = Color(0xFF059669),
                                                            )
                                                        } else {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = "${slot.subjectTitle} (${slot.subjectCode})",
                                                                    fontSize = 12.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontFamily = hindSiliguri,
                                                                    color = textColor,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis,
                                                                )
                                                                Text(
                                                                    text = "${slot.department} • ${RoutineRules.semesterShortLabel(slot.semester)} • ${slot.group} • ${slot.teacherName}",
                                                                    fontSize = 11.sp,
                                                                    fontFamily = hindSiliguri,
                                                                    color = subTextColor,
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
    isDark: Boolean,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var isFocused by remember { mutableStateOf(false) }
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val bg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val accent = Color(0xFF7C3AED)
    val animBorder by animateColorAsState(
        targetValue = if (isFocused) accent else borderCol,
        animationSpec = tween(160),
        label = "ClassroomInputBorder",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = hindSiliguri,
            color = textColor,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bg,
            border = BorderStroke(if (isFocused) 1.4.dp else 1.dp, animBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        color = subTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                )
            }
        }
    }
}
