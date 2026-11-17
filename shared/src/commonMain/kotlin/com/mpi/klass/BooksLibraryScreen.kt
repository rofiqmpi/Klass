package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.FilterIcon
import com.mpi.klass.theme.MoreVertIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.TrashCanIcon
import kotlinx.coroutines.launch

private data class LibraryDepartmentSpec(
    val shortCode: String,
    val fullName: String,
)

private data class LibrarySemesterSpec(
    val key: String,
    val shortEn: String,
)

private val libraryDepartments = listOf(
    LibraryDepartmentSpec("CST", "CST (Computer Science & Technology)"),
    LibraryDepartmentSpec("CT", "CT (Civil Technology)"),
    LibraryDepartmentSpec("ET", "ET (Electrical Technology)"),
    LibraryDepartmentSpec("MT", "MT (Mechanical Technology)"),
    LibraryDepartmentSpec("PT", "PT (Power Technology)"),
    LibraryDepartmentSpec("ENT", "ENT (Electronics Technology)"),
    LibraryDepartmentSpec("EMT", "EMT (Electro-Medical Technology)"),
)

private val librarySemesters = listOf(
    LibrarySemesterSpec("১ম সেমিস্টার", "1st"),
    LibrarySemesterSpec("২য় সেমিস্টার", "2nd"),
    LibrarySemesterSpec("৩য় সেমিস্টার", "3rd"),
    LibrarySemesterSpec("৪র্থ সেমিস্টার", "4th"),
    LibrarySemesterSpec("৫ম সেমিস্টার", "5th"),
    LibrarySemesterSpec("৬ষ্ঠ সেমিস্টার", "6th"),
    LibrarySemesterSpec("৭ম সেমিস্টার", "7th"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BooksLibraryScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    var books by remember { mutableStateOf<List<LibraryBook>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf<String?>(null) }
    var selectedSem by remember { mutableStateOf<String?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var detailBook by remember { mutableStateOf<LibraryBook?>(null) }
    var showFormScreen by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<LibraryBook?>(null) }
    var bookToDelete by remember { mutableStateOf<LibraryBook?>(null) }
    var isDeletingBook by remember { mutableStateOf(false) }

    LaunchedEffect(showFormScreen) {
        onFullScreenOverlayChange(showFormScreen)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    fun loadBooks() {
        scope.launch {
            isLoading = true
            try {
                books = RoleConfig.fetchLibraryBooks()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadBooks()
    }

    val filteredBooks = remember(books, searchQuery, selectedDept, selectedSem) {
        val q = searchQuery.trim().lowercase()
        val deptFilter = selectedDept
        val semFilter = selectedSem
        books.filter { book ->
            val matchesDeptSem = when {
                deptFilter != null && semFilter != null -> {
                    book.departmentSemesters.any { (rawDept, sems) ->
                        rawDept.substringBefore(" (").trim().equals(deptFilter, ignoreCase = true) &&
                            sems.any { it.trim() == semFilter }
                    }
                }
                deptFilter != null -> {
                    book.departmentSemesters.keys.any { rawDept ->
                        rawDept.substringBefore(" (").trim().equals(deptFilter, ignoreCase = true)
                    }
                }
                semFilter != null -> {
                    book.departmentSemesters.values.any { sems ->
                        sems.any { it.trim() == semFilter }
                    }
                }
                else -> true
            }
            if (!matchesDeptSem) return@filter false
            if (q.isBlank()) return@filter true
            book.title.lowercase().contains(q) ||
                book.subjectCode.lowercase().contains(q) ||
                book.credit.lowercase().contains(q) ||
                book.departmentSemesters.any { (dept, sems) ->
                    dept.lowercase().contains(q) || sems.any { it.lowercase().contains(q) }
                }
        }
    }

    val hasActiveFilter = selectedDept != null || selectedSem != null

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
                    // Single compact Search + Filter Sheet trigger + Refresh row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MinimalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search title, code or dept (${filteredBooks.size})",
                            accent = cs.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "books_search_input",
                            clearTestTag = "books_search_clear_button",
                            modifier = Modifier.weight(1f),
                        )

                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("books_filter_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (hasActiveFilter) cs.primaryContainer else cs.surfaceContainerLow,
                                    border = if (hasActiveFilter) null else BorderStroke(0.75.dp, cs.outlineVariant),
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = FilterIcon,
                                            contentDescription = "Filter books",
                                            tint = if (hasActiveFilter) cs.primary else cs.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                if (hasActiveFilter) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(cs.primary),
                                    )
                                }
                            }
                        }

                        WelcomeMinimal3DIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh library",
                            isDark = isDark,
                            iconTint = cs.onSurfaceVariant,
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "books_refresh_button",
                            onClick = { loadBooks() },
                        )
                    }

                    // Show slim removable active-filter pills ONLY when a filter is active
                    if (hasActiveFilter) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            selectedDept?.let { dept ->
                                ActiveFilterTag(
                                    label = dept,
                                    onClear = { selectedDept = null },
                                )
                            }
                            selectedSem?.let { semKey ->
                                val shortEn = librarySemesters.firstOrNull { it.key == semKey }?.shortEn ?: semKey
                                ActiveFilterTag(
                                    label = "$shortEn Sem",
                                    onClear = { selectedSem = null },
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${filteredBooks.size} shown",
                                fontSize = 12.sp,
                                color = cs.onSurfaceVariant,
                                fontFamily = hindSiliguri,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        isLoading && books.isEmpty() -> {
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
                                        text = "Loading books...",
                                        color = cs.onSurfaceVariant,
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        filteredBooks.isEmpty() -> {
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
                                            color = cs.primaryContainer,
                                            modifier = Modifier.size(52.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = BookIcon,
                                                    contentDescription = null,
                                                    tint = cs.onPrimaryContainer,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val hasActiveFilterOrSearch = searchQuery.isNotBlank() || hasActiveFilter
                                        Text(
                                            text = if (!hasActiveFilterOrSearch) {
                                                "No books in library yet"
                                            } else {
                                                "No matching books found"
                                            },
                                            color = cs.onSurface,
                                            fontSize = 15.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                        )
                                        if (!hasActiveFilterOrSearch) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tap + to add a BTEB curriculum subject.",
                                                color = cs.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                fontFamily = hindSiliguri,
                                                textAlign = TextAlign.Center,
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            GoogleOutlinedButton(
                                                text = "Clear Filters",
                                                hindSiliguri = hindSiliguri,
                                                height = 40.dp,
                                                testTag = "books_clear_filters_button",
                                                onClick = {
                                                    searchQuery = ""
                                                    selectedDept = null
                                                    selectedSem = null
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val cols = if (maxWidth >= 620.dp) 2 else 1
                                val indexedRows = remember(filteredBooks, cols) {
                                    filteredBooks.withIndex().chunked(cols)
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 88.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.id ?: "row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            rowItems.forEach { indexed ->
                                                val index = indexed.index
                                                val book = indexed.value
                                                Box(modifier = Modifier.weight(1f)) {
                                                    AnimatedStaggeredItem(index = rowIdx) {
                                                        LibraryBookCard(
                                                            book = book,
                                                            selectedDept = selectedDept,
                                                            isDark = isDark,
                                                            hindSiliguri = hindSiliguri,
                                                            testTag = "book_card_$index",
                                                            editTestTag = "book_edit_button_$index",
                                                            deleteTestTag = "book_delete_button_$index",
                                                            onClick = { detailBook = book },
                                                            onEditClick = {
                                                                editingBook = book
                                                                showFormScreen = true
                                                            },
                                                            onDeleteClick = { bookToDelete = book },
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

        // Clean Floating Action Button for Add Book
        if (!showFormScreen) {
            FloatingActionButton(
                onClick = {
                    editingBook = null
                    showFormScreen = true
                },
                containerColor = cs.primary,
                contentColor = cs.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .testTag("books_add_button"),
            ) {
                Icon(
                    imageVector = PlusIcon,
                    contentDescription = "Add book",
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = showFormScreen,
            enter = fadeIn(tween(220)) + slideInVertically(tween(240)) { it / 6 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 6 },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(80f),
        ) {
            FullScreenBookFormScreen(
                initialBook = editingBook,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = {
                    showFormScreen = false
                    editingBook = null
                },
                onSave = { savedBook, onComplete ->
                    scope.launch {
                        val result = RoleConfig.saveLibraryBook(savedBook)
                        if (result.isSuccess) {
                            books = RoleConfig.fetchLibraryBooks()
                            showFormScreen = false
                            editingBook = null
                            showToast("Book saved successfully!")
                        } else {
                            showToast("Failed to save book. Please try again.")
                        }
                        onComplete()
                    }
                },
            )
        }
    }

    // Filter ModalBottomSheet (keeps main screen completely clean!)
    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = cs.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Filter Books",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = hindSiliguri,
                        color = cs.onSurface,
                    )
                    if (hasActiveFilter) {
                        TextButton(
                            onClick = {
                                selectedDept = null
                                selectedSem = null
                            },
                            modifier = Modifier.testTag("books_clear_filters_button"),
                        ) {
                            Text("Reset", color = cs.primary, fontFamily = hindSiliguri)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Department",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurfaceVariant,
                        fontFamily = hindSiliguri,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MinimalIconChip(
                            label = "All",
                            selected = selectedDept == null,
                            accent = cs.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "books_filter_dept_all",
                            onClick = { selectedDept = null },
                        )
                        libraryDepartments.forEach { spec ->
                            MinimalIconChip(
                                label = spec.shortCode,
                                selected = selectedDept == spec.shortCode,
                                accent = cs.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_dept_${spec.shortCode.lowercase()}",
                                onClick = {
                                    selectedDept = if (selectedDept == spec.shortCode) null else spec.shortCode
                                },
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Semester",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurfaceVariant,
                        fontFamily = hindSiliguri,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MinimalIconChip(
                            label = "All",
                            selected = selectedSem == null,
                            accent = cs.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "books_filter_sem_all",
                            onClick = { selectedSem = null },
                        )
                        librarySemesters.forEachIndexed { idx, sem ->
                            MinimalIconChip(
                                label = sem.shortEn,
                                selected = selectedSem == sem.key,
                                accent = cs.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_sem_${idx + 1}",
                                onClick = {
                                    selectedSem = if (selectedSem == sem.key) null else sem.key
                                },
                            )
                        }
                    }
                }

                WelcomeMinimal3DButton(
                    text = "Show ${filteredBooks.size} Books",
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showFilterSheet = false },
                )
            }
        }
    }

    // Book Detail BottomSheet (on card tap)
    detailBook?.let { selected ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { detailBook = null },
            sheetState = sheetState,
            containerColor = cs.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = selected.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = hindSiliguri,
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected.subjectCode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cs.primaryContainer,
                        ) {
                            Text(
                                text = AppStrings.convertBengaliDigitsToAscii(selected.subjectCode),
                                color = cs.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                val creditClean = AppStrings.convertBengaliDigitsToAscii(selected.credit.ifBlank { "0" })
                Text(
                    text = "${selected.theoryClassesPerWeek} Theory / wk • ${selected.practicalClassesPerWeek} Practical / wk • $creditClean Credits",
                    fontSize = 13.5.sp,
                    color = cs.onSurfaceVariant,
                    fontFamily = hindSiliguri,
                )

                if (selected.departmentSemesters.isNotEmpty()) {
                    HorizontalDivider(thickness = 0.6.dp, color = cs.outlineVariant.copy(alpha = 0.6f))
                    Text(
                        text = "Departments & Semesters",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.primary,
                        fontFamily = hindSiliguri,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        selected.departmentSemesters.forEach { (dept, sems) ->
                            val deptShort = dept.substringBefore(" (").trim()
                            val semSummary = sems.joinToString(", ") { semKey ->
                                librarySemesters.firstOrNull { it.key == semKey }?.shortEn ?: semKey
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = deptShort,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface,
                                    fontFamily = hindSiliguri,
                                )
                                Text(
                                    text = semSummary,
                                    fontSize = 13.sp,
                                    color = cs.onSurfaceVariant,
                                    fontFamily = hindSiliguri,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GoogleOutlinedButton(
                        text = "Delete",
                        icon = TrashCanIcon,
                        hindSiliguri = hindSiliguri,
                        contentColor = cs.error,
                        height = 44.dp,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            detailBook = null
                            bookToDelete = selected
                        },
                    )
                    WelcomeMinimal3DButton(
                        text = "Edit Book",
                        icon = EditIcon,
                        hindSiliguri = hindSiliguri,
                        isDark = isDark,
                        height = 44.dp,
                        modifier = Modifier.weight(1.3f),
                        onClick = {
                            detailBook = null
                            editingBook = selected
                            showFormScreen = true
                        },
                    )
                }
            }
        }
    }

    bookToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingBook) bookToDelete = null },
            containerColor = cs.surfaceContainerLow,
            titleContentColor = cs.onSurface,
            textContentColor = cs.onSurfaceVariant,
            title = {
                Text(
                    text = "Delete Book",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Permanently remove this book from the curriculum library?",
                        fontFamily = hindSiliguri,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cs.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = target.title,
                                color = cs.onSurface,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "Code: ${AppStrings.convertBengaliDigitsToAscii(target.subjectCode)}",
                                color = cs.onSurfaceVariant,
                                fontFamily = hindSiliguri,
                                fontSize = 12.sp,
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
                    isLoading = isDeletingBook,
                    height = 40.dp,
                    cornerRadius = 20.dp,
                    testTag = "confirm_delete_book_button",
                    onClick = {
                        if (isDeletingBook) return@WelcomeMinimal3DButton
                        isDeletingBook = true
                        scope.launch {
                            val result = RoleConfig.deleteLibraryBook(target.id)
                            isDeletingBook = false
                            if (result.isSuccess) {
                                books = books.filterNot { it.id == target.id }
                                bookToDelete = null
                                showToast("Book deleted!")
                            } else {
                                showToast("Could not delete book. Try again.")
                            }
                        }
                    },
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { bookToDelete = null },
                    enabled = !isDeletingBook,
                    modifier = Modifier.testTag("cancel_delete_book_button"),
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
private fun ActiveFilterTag(
    label: String,
    onClear: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClear,
        shape = CircleShape,
        color = cs.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = cs.onPrimaryContainer,
            )
            Icon(
                imageVector = CloseIcon,
                contentDescription = "Remove filter",
                tint = cs.onPrimaryContainer,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun LibraryBookCard(
    book: LibraryBook,
    selectedDept: String?,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    testTag: String,
    editTestTag: String,
    deleteTestTag: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var menuExpanded by remember { mutableStateOf(false) }

    val creditClean = AppStrings.convertBengaliDigitsToAscii(book.credit.ifBlank { "0" })
    val deptSummary = remember(book.departmentSemesters, selectedDept) {
        val depts = book.departmentSemesters.keys.map { it.substringBefore(" (").trim() }.distinct()
        when {
            selectedDept != null -> {
                val sems = book.departmentSemesters.entries
                    .firstOrNull { it.key.substringBefore(" (").trim().equals(selectedDept, true) }
                    ?.value.orEmpty()
                    .joinToString(", ") { semKey ->
                        librarySemesters.firstOrNull { it.key == semKey }?.shortEn ?: semKey
                    }
                if (sems.isNotBlank()) "$selectedDept ($sems)" else selectedDept
            }
            depts.isEmpty() -> "Unassigned"
            depts.size <= 2 -> depts.joinToString(", ")
            else -> "${depts.take(2).joinToString(", ")} +${depts.size - 2}"
        }
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
                        text = book.title,
                        color = cs.onSurface,
                        fontSize = 14.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (book.subjectCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cs.secondaryContainer,
                        ) {
                            Text(
                                text = AppStrings.convertBengaliDigitsToAscii(book.subjectCode),
                                color = cs.onSecondaryContainer,
                                fontSize = 11.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Text(
                    text = "$deptSummary • ${book.theoryClassesPerWeek}T ${book.practicalClassesPerWeek}P • $creditClean Cr",
                    color = cs.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontFamily = hindSiliguri,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = MoreVertIcon,
                        contentDescription = "Book actions",
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
                            onEditClick()
                        },
                        modifier = Modifier.testTag(editTestTag),
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
                            onDeleteClick()
                        },
                        modifier = Modifier.testTag(deleteTestTag),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullScreenBookFormScreen(
    initialBook: LibraryBook?,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onSave: (LibraryBook, onComplete: () -> Unit) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var title by remember(initialBook) { mutableStateOf(initialBook?.title ?: "") }
    var subjectCode by remember(initialBook) { mutableStateOf(initialBook?.subjectCode ?: "") }
    var theoryText by remember(initialBook) {
        mutableStateOf(if (initialBook != null) initialBook.theoryClassesPerWeek.toString() else "")
    }
    var practicalText by remember(initialBook) {
        mutableStateOf(if (initialBook != null) initialBook.practicalClassesPerWeek.toString() else "")
    }
    var creditText by remember(initialBook) { mutableStateOf(initialBook?.credit ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val deptSelections = remember(initialBook) {
        mutableStateMapOf<String, Set<String>>().apply {
            initialBook?.departmentSemesters?.forEach { (rawDept, sems) ->
                val shortDept = rawDept.substringBefore(" (").trim()
                val validSems = sems.filter { s -> librarySemesters.any { it.key == s } }.toSet()
                if (shortDept.isNotBlank() && validSems.isNotEmpty()) {
                    this[shortDept] = validSems
                }
            }
        }
    }
    val expandedDepts = remember(initialBook) {
        mutableStateMapOf<String, Boolean>().apply {
            initialBook?.departmentSemesters?.keys?.forEach { rawDept ->
                val shortDept = rawDept.substringBefore(" (").trim()
                if (shortDept.isNotBlank()) {
                    this[shortDept] = true
                }
            }
        }
    }

    MinimalFullScreenSheet(
        title = if (initialBook == null) "Add Book" else "Edit Book",
        subtitle = "BTEB Curriculum Catalog",
        icon = BookIcon,
        accent = cs.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        dismissEnabled = !isSaving,
        closeTestTag = "book_dialog_close_button",
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
                    testTag = "cancel_book_form_button",
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                )

                WelcomeMinimal3DButton(
                    text = if (isSaving) "Saving..." else "Save Book",
                    icon = CheckIcon,
                    hindSiliguri = hindSiliguri,
                    isPrimaryAccent = true,
                    isDark = isDark,
                    isLoading = isSaving,
                    enabled = !isSaving,
                    height = 46.dp,
                    cornerRadius = 23.dp,
                    testTag = "save_book_button",
                    modifier = Modifier.weight(1.3f),
                    onClick = {
                        if (isSaving) return@WelcomeMinimal3DButton
                        val cleanTitle = title.trim()
                        val cleanCode = subjectCode.trim()
                        if (cleanTitle.isBlank() || cleanCode.isBlank()) {
                            showToast("Please enter book title and subject code.")
                            return@WelcomeMinimal3DButton
                        }

                        val orderedDeptSemMap = linkedMapOf<String, List<String>>()
                        for (dept in libraryDepartments) {
                            val sems = deptSelections[dept.shortCode].orEmpty()
                            if (sems.isNotEmpty()) {
                                val orderedSems = librarySemesters
                                    .map { it.key }
                                    .filter { it in sems }
                                if (orderedSems.isNotEmpty()) {
                                    orderedDeptSemMap[dept.shortCode] = orderedSems
                                }
                            }
                        }
                        if (orderedDeptSemMap.isEmpty()) {
                            showToast("Select at least one department and semester.")
                            return@WelcomeMinimal3DButton
                        }

                        val theoryVal = AppStrings.convertBengaliDigitsToAscii(theoryText.trim())
                            .filter { it.isDigit() }
                            .toIntOrNull() ?: 0
                        val practicalVal = AppStrings.convertBengaliDigitsToAscii(practicalText.trim())
                            .filter { it.isDigit() }
                            .toIntOrNull() ?: 0
                        val cleanCredit = AppStrings.convertBengaliDigitsToAscii(creditText.trim())
                            .ifBlank { "0" }

                        isSaving = true
                        val bookToSave = (initialBook ?: LibraryBook()).copy(
                            title = cleanTitle,
                            subjectCode = AppStrings.convertBengaliDigitsToAscii(cleanCode),
                            departmentSemesters = orderedDeptSemMap,
                            theoryClassesPerWeek = theoryVal,
                            practicalClassesPerWeek = practicalVal,
                            credit = cleanCredit,
                        )
                        onSave(bookToSave) {
                            isSaving = false
                        }
                    },
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
            GoogleSectionHeader(title = "Subject Details", hindSiliguri = hindSiliguri)

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val twoCol = maxWidth >= 560.dp
                if (twoCol) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MinimalBookTextField(
                            label = "Book Title *",
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Enter subject title",
                            hindSiliguri = hindSiliguri,
                            testTag = "book_title_input",
                            modifier = Modifier.weight(1.4f),
                        )
                        MinimalBookTextField(
                            label = "Subject Code *",
                            value = subjectCode,
                            onValueChange = { subjectCode = it },
                            placeholder = "e.g. 25911",
                            hindSiliguri = hindSiliguri,
                            keyboardType = KeyboardType.Number,
                            testTag = "book_code_input",
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MinimalBookTextField(
                            label = "Book Title *",
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Enter subject title",
                            hindSiliguri = hindSiliguri,
                            testTag = "book_title_input",
                        )
                        MinimalBookTextField(
                            label = "Subject Code *",
                            value = subjectCode,
                            onValueChange = { subjectCode = it },
                            placeholder = "e.g. 25911",
                            hindSiliguri = hindSiliguri,
                            keyboardType = KeyboardType.Number,
                            testTag = "book_code_input",
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MinimalBookTextField(
                    label = "Theory / Wk",
                    value = theoryText,
                    onValueChange = { theoryText = it },
                    placeholder = "3",
                    hindSiliguri = hindSiliguri,
                    keyboardType = KeyboardType.Number,
                    testTag = "book_theory_input",
                    modifier = Modifier.weight(1f),
                )
                MinimalBookTextField(
                    label = "Practical / Wk",
                    value = practicalText,
                    onValueChange = { practicalText = it },
                    placeholder = "3",
                    hindSiliguri = hindSiliguri,
                    keyboardType = KeyboardType.Number,
                    testTag = "book_practical_input",
                    modifier = Modifier.weight(1f),
                )
                MinimalBookTextField(
                    label = "Credits",
                    value = creditText,
                    onValueChange = { creditText = it },
                    placeholder = "3.5",
                    hindSiliguri = hindSiliguri,
                    keyboardType = KeyboardType.Decimal,
                    testTag = "book_credit_input",
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GoogleSectionHeader(
                    title = "Departments & Semesters",
                    hindSiliguri = hindSiliguri,
                )

                MinimalResponsiveGrid(
                    items = libraryDepartments,
                    twoColMinWidth = 600.dp,
                    spacing = 10.dp,
                ) { _, dept ->
                    val selectedSems = deptSelections[dept.shortCode].orEmpty()
                    val isSelected = selectedSems.isNotEmpty()
                    val isExpanded = expandedDepts[dept.shortCode] == true
                    val chevronRotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "DeptChevron_${dept.shortCode}",
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) cs.surfaceContainer else cs.surfaceContainerLow,
                        border = if (isSelected) {
                            BorderStroke(1.dp, cs.primary.copy(alpha = 0.45f))
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable {
                                        expandedDepts[dept.shortCode] = !isExpanded
                                    }
                                    .padding(start = 4.dp, end = 14.dp)
                                    .testTag("book_dept_toggle_${dept.shortCode}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = cs.primary,
                                            uncheckedColor = cs.onSurfaceVariant,
                                        ),
                                    )

                                    Text(
                                        text = dept.fullName,
                                        color = cs.onSurface,
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (isSelected) {
                                        val countStr = if (selectedSems.size == librarySemesters.size) {
                                            "All"
                                        } else {
                                            "${selectedSems.size} Sem"
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = cs.primaryContainer,
                                        ) {
                                            Text(
                                                text = countStr,
                                                color = cs.onPrimaryContainer,
                                                fontSize = 11.sp,
                                                fontFamily = hindSiliguri,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = ChevronDownIcon,
                                        contentDescription = null,
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .graphicsLayer(rotationZ = chevronRotation),
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(tween(180)) + fadeIn(tween(150)),
                                exit = shrinkVertically(tween(150)) + fadeOut(tween(120)),
                            ) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val allSelected = selectedSems.size == librarySemesters.size
                                    MinimalSemesterChip(
                                        label = "All",
                                        selected = allSelected,
                                        hindSiliguri = hindSiliguri,
                                        testTag = "book_dept_${dept.shortCode}_all_sem",
                                        onClick = {
                                            if (allSelected) {
                                                deptSelections.remove(dept.shortCode)
                                            } else {
                                                deptSelections[dept.shortCode] = librarySemesters.map { it.key }.toSet()
                                            }
                                        },
                                    )

                                    for ((semIdx, sem) in librarySemesters.withIndex()) {
                                        val semChecked = sem.key in selectedSems
                                        MinimalSemesterChip(
                                            label = sem.shortEn,
                                            selected = semChecked,
                                            hindSiliguri = hindSiliguri,
                                            testTag = "book_dept_${dept.shortCode}_sem_${semIdx + 1}",
                                            onClick = {
                                                val updated = selectedSems.toMutableSet()
                                                if (semChecked) {
                                                    updated.remove(sem.key)
                                                } else {
                                                    updated.add(sem.key)
                                                }
                                                if (updated.isEmpty()) {
                                                    deptSelections.remove(dept.shortCode)
                                                } else {
                                                    deptSelections[dept.shortCode] = updated
                                                }
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
    }
}

@Composable
private fun MinimalSemesterChip(
    label: String,
    selected: Boolean,
    hindSiliguri: FontFamily,
    testTag: String,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) cs.secondaryContainer else cs.surface,
        border = if (selected) null else BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier
            .height(34.dp)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (selected) {
                Icon(
                    imageVector = CheckIcon,
                    contentDescription = null,
                    tint = cs.onSecondaryContainer,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = label,
                color = if (selected) cs.onSecondaryContainer else cs.onSurfaceVariant,
                fontSize = 12.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MinimalBookTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String,
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
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
    )
}
