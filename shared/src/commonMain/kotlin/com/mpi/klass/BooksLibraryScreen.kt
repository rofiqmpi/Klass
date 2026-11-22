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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.AwardIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.EditIcon
import com.mpi.klass.theme.FilterIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.MoreVertIcon
import com.mpi.klass.theme.PlusIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.departmentAccent
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
    canEdit: Boolean = true,
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

    val booksAccent = KlassFeaturePalette.books(isDark)
    val anyOverlay = showFormScreen || showFilterSheet || detailBook != null || bookToDelete != null
    LaunchedEffect(anyOverlay) {
        onFullScreenOverlayChange(anyOverlay)
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
        MinimalBackdrop(isDark = isDark, focusAccent = booksAccent.primary) {
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
                    // Single compact Search + Filter Sheet trigger + Refresh row
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
                            placeholder = "Search title, code or dept (${filteredBooks.size})",
                            accent = booksAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            testTag = "books_search_input",
                            clearTestTag = "books_search_clear_button",
                            modifier = Modifier.weight(1f),
                        )

                        Box {
                            KlassHeaderIconButton(
                                icon = FilterIcon,
                                contentDescription = "Filter books",
                                isDark = isDark,
                                containerColor = if (hasActiveFilter) booksAccent.softTint else cs.surfaceContainerHigh,
                                iconTint = if (hasActiveFilter) booksAccent.primary else cs.onSurfaceVariant,
                                borderColor = if (hasActiveFilter) booksAccent.borderTint else Color.Transparent,
                                visualSize = 40.dp,
                                hasActivity = hasActiveFilter,
                                testTag = "books_filter_button",
                                onClick = { showFilterSheet = true },
                            )
                            if (hasActiveFilter) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(booksAccent.primary),
                                )
                            }
                        }

                        KlassHeaderIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh library",
                            isDark = isDark,
                            iconTint = booksAccent.primary,
                            isLoading = isLoading,
                            enabled = !isLoading,
                            testTag = "books_refresh_button",
                            onClick = { loadBooks() },
                        )
                    }

                    // Show slim removable active-filter pills ONLY when a filter is active
                    if (hasActiveFilter) {
                        val badges = buildList {
                            selectedDept?.let { add(it) }
                            selectedSem?.let { semKey ->
                                val shortEn = librarySemesters.firstOrNull { it.key == semKey }?.shortEn ?: semKey
                                add("$shortEn Sem")
                            }
                        }
                        KlassFilterBadgeBar(
                            badges = badges,
                            accent = booksAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClear = {
                                selectedDept = null
                                selectedSem = null
                            },
                        )
                    }

                    SocialSectionDividerBand(isDark = isDark, height = 6.dp)

                    when {
                        isLoading && books.isEmpty() -> {
                            KlassRowListSkeleton(
                                isDark = isDark,
                                showHeaderBar = true,
                                showActionPill = false,
                                badgeSize = 46.dp,
                                badgeCornerRadius = 14.dp,
                                rowLineCount = 3,
                            )
                        }

                        filteredBooks.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val hasActiveFilterOrSearch = searchQuery.isNotBlank() || hasActiveFilter
                                KlassEmptyState(
                                    icon = BookIcon,
                                    title = if (!hasActiveFilterOrSearch) "No books in library yet" else "No matching books found",
                                    subtitle = if (!hasActiveFilterOrSearch) {
                                        "Tap + to add a BTEB curriculum subject."
                                    } else {
                                        "Try adjusting your search or clearing the active filters."
                                    },
                                    accent = booksAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    actionLabel = if (hasActiveFilterOrSearch) "Clear Filters" else null,
                                    actionTestTag = if (hasActiveFilterOrSearch) "books_clear_filters_button" else null,
                                    onActionClick = if (hasActiveFilterOrSearch) {
                                        {
                                            searchQuery = ""
                                            selectedDept = null
                                            selectedSem = null
                                        }
                                    } else null,
                                    modifier = Modifier.widthIn(max = 380.dp),
                                )
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
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.id ?: "row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                rowItems.forEach { indexed ->
                                                    val index = indexed.index
                                                    val book = indexed.value
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        AnimatedStaggeredItem(index = rowIdx) {
                                                            LibraryBookCard(
                                                                book = book,
                                                                colorIndex = index,
                                                                selectedDept = selectedDept,
                                                                canEdit = canEdit,
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
                                            if (rowIdx < indexedRows.lastIndex) {
                                                SocialInsetDivider(startIndent = if (cols == 1) 76.dp else 16.dp)
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

        // Clean Floating Action Button for Add Book (Admin only)
        if (canEdit && !anyOverlay) {
            FloatingActionButton(
                onClick = {
                    editingBook = null
                    showFormScreen = true
                },
                containerColor = booksAccent.primary,
                contentColor = Color.White,
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

        // Level 1: Full-Screen Books Filter Sub-Screen
        KlassSubScreenHost(
            visible = showFilterSheet,
            zIndex = 75f,
        ) {
            MinimalFullScreenSheet(
                title = "Filter Books",
                subtitle = "${filteredBooks.size} matching books",
                levelLabel = "বই লাইব্রেরি • ফিল্টার",
                icon = FilterIcon,
                accent = booksAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onDismiss = { showFilterSheet = false },
                headerActions = {
                    if (hasActiveFilter) {
                        KlassTonalButton(
                            text = "Reset",
                            icon = CloseIcon,
                            accent = KlassFeaturePalette.error(isDark).primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            height = 36.dp,
                            testTag = "books_clear_filters_button",
                            onClick = {
                                selectedDept = null
                                selectedSem = null
                            },
                        )
                    }
                    HeaderSavePillButton(
                        text = "Show ${filteredBooks.size}",
                        accent = booksAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        testTag = "books_apply_filters_button",
                        onClick = { showFilterSheet = false },
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        GoogleSectionHeader(
                            title = "Department",
                            hindSiliguri = hindSiliguri,
                            accent = booksAccent.primary,
                        )
                        FlowRow(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MinimalIconChip(
                                label = "All",
                                selected = selectedDept == null,
                                accent = booksAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_dept_all",
                                onClick = { selectedDept = null },
                            )
                            libraryDepartments.forEach { spec ->
                                val deptAccent = departmentAccent(spec.shortCode, isDark)
                                MinimalIconChip(
                                    label = spec.shortCode,
                                    selected = selectedDept == spec.shortCode,
                                    accent = deptAccent.primary,
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

                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        GoogleSectionHeader(
                            title = "Semester",
                            hindSiliguri = hindSiliguri,
                            accent = KlassFeaturePalette.routine(isDark).primary,
                        )
                        FlowRow(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MinimalIconChip(
                                label = "All",
                                selected = selectedSem == null,
                                accent = KlassFeaturePalette.routine(isDark).primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_sem_all",
                                onClick = { selectedSem = null },
                            )
                            librarySemesters.forEachIndexed { idx, sem ->
                                MinimalIconChip(
                                    label = sem.shortEn,
                                    selected = selectedSem == sem.key,
                                    accent = KlassFeaturePalette.routine(isDark).primary,
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
                }
            }
        }

        // Level 1: Book Detail Sub-Screen (replaces cramped ModalBottomSheet)
        KlassSubScreenHost(
            visible = detailBook != null,
            zIndex = 78f,
        ) {
            detailBook?.let { selected ->
                BookDetailFullSubScreen(
                    book = selected,
                    canEdit = canEdit,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onEditClick = {
                        detailBook = null
                        editingBook = selected
                        showFormScreen = true
                    },
                    onDeleteClick = {
                        detailBook = null
                        bookToDelete = selected
                    },
                    onDismiss = { detailBook = null },
                )
            }
        }
    }

    bookToDelete?.let { target ->
        KlassConfirmDialog(
            title = "Delete Book",
            message = "Permanently remove \"${target.title}\" (${AppStrings.convertBengaliDigitsToAscii(target.subjectCode)}) from the curriculum library?",
            icon = TrashCanIcon,
            accent = KlassFeaturePalette.error(isDark).primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            confirmText = if (isDeletingBook) "Deleting..." else "Delete",
            cancelText = "Cancel",
            isDestructive = true,
            confirmTestTag = "confirm_delete_book_button",
            cancelTestTag = "cancel_delete_book_button",
            onConfirm = {
                if (!isDeletingBook) {
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
                }
            },
            onDismiss = { if (!isDeletingBook) bookToDelete = null },
        )
    }
}

@Composable
private fun BookDetailFullSubScreen(
    book: LibraryBook,
    canEdit: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val booksAccent = KlassFeaturePalette.books(isDark)
    val cleanCode = AppStrings.convertBengaliDigitsToAscii(book.subjectCode)
    val creditClean = AppStrings.convertBengaliDigitsToAscii(book.credit.ifBlank { "0" })
        MinimalFullScreenSheet(
            title = book.title,
            subtitle = if (cleanCode.isNotBlank()) "Subject Code $cleanCode" else "BTEB Curriculum Book",
            levelLabel = "বই লাইব্রেরি • বিস্তারিত",
            icon = BookIcon,
            accent = booksAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            headerActions = {
                if (canEdit) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("book_detail_delete_button"),
                    ) {
                        Icon(
                            imageVector = TrashCanIcon,
                            contentDescription = "Delete Book",
                            tint = KlassFeaturePalette.error(isDark).primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    HeaderSavePillButton(
                        text = "Edit",
                        accent = booksAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        testTag = "book_detail_edit_button",
                        onClick = onEditClick,
                    )
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 28.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    KlassDetailFieldRow(
                        label = "Theory Classes / Week",
                        value = "${book.theoryClassesPerWeek} periods",
                        icon = ClockIcon,
                        accent = KlassFeaturePalette.teachers(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                    SocialInsetDivider(startIndent = 72.dp)
                    KlassDetailFieldRow(
                        label = "Practical Lab / Week",
                        value = "${book.practicalClassesPerWeek} periods",
                        icon = LayerGroupIcon,
                        accent = KlassFeaturePalette.classrooms(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                    SocialInsetDivider(startIndent = 72.dp)
                    KlassDetailFieldRow(
                        label = "Subject Credit",
                        value = "$creditClean Credits",
                        icon = AwardIcon,
                        accent = KlassFeaturePalette.ai(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }

                if (book.departmentSemesters.isNotEmpty()) {
                    SocialSectionDividerBand(isDark = isDark)
                    GoogleSectionHeader(
                        title = "Departments & Semesters (${book.departmentSemesters.size})",
                        hindSiliguri = hindSiliguri,
                        accent = booksAccent.primary,
                    )
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        val entries = book.departmentSemesters.entries.toList()
                        entries.forEachIndexed { idx, (dept, sems) ->
                            val deptShort = dept.substringBefore(" (").trim()
                            val deptAcc = departmentAccent(deptShort, isDark)
                            val semSummary = sems.joinToString(", ") { semKey ->
                                librarySemesters.firstOrNull { it.key == semKey }?.shortEn ?: semKey
                            }
                            KlassDetailFieldRow(
                                label = "$deptShort (${sems.size} Sem)",
                                value = semSummary.ifBlank { "All Semesters" },
                                icon = GraduationCapIcon,
                                accent = deptAcc.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            if (idx < entries.lastIndex) {
                                SocialInsetDivider(startIndent = 72.dp)
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun LibraryBookCard(
    book: LibraryBook,
    colorIndex: Int,
    selectedDept: String?,
    canEdit: Boolean,
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
            depts.isEmpty() -> "General"
            depts.size <= 2 -> depts.joinToString(", ")
            else -> "${depts.take(2).joinToString(", ")} +${depts.size - 2}"
        }
    }

    val firstDept = remember(book.departmentSemesters, selectedDept) {
        selectedDept ?: book.departmentSemesters.keys.firstOrNull()?.substringBefore(" (")?.trim().orEmpty()
    }
    val cardAccent = if (firstDept.isNotBlank()) departmentAccent(firstDept, isDark) else KlassFeaturePalette.books(isDark)

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
            Surface(
                shape = CircleShape,
                color = cardAccent.softTint,
                border = BorderStroke(0.75.dp, cardAccent.borderTint),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = BookIcon,
                        contentDescription = null,
                        tint = cardAccent.primary,
                        modifier = Modifier.size(19.dp),
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
                        text = book.title,
                        color = cs.onSurface,
                        fontSize = 17.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (book.subjectCode.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = cardAccent.softTint,
                            border = BorderStroke(0.75.dp, cardAccent.borderTint),
                        ) {
                            Text(
                                text = AppStrings.convertBengaliDigitsToAscii(book.subjectCode),
                                color = cardAccent.primary,
                                fontSize = 12.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Text(
                    text = deptSummary,
                    color = cs.onSurfaceVariant,
                    fontSize = 14.5.sp,
                    fontFamily = hindSiliguri,
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
                            contentDescription = "Book actions",
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = cs.surfaceContainerLow,
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", fontSize = 15.5.sp, fontFamily = hindSiliguri) },
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
                        text = { Text("Delete", fontSize = 15.5.sp, color = cs.error, fontFamily = hindSiliguri) },
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

    val booksAccent = KlassFeaturePalette.books(isDark)
    MinimalFullScreenSheet(
        title = if (initialBook == null) "Add Book" else "Edit Book",
        subtitle = "BTEB Curriculum Catalog",
        levelLabel = if (initialBook == null) "বই লাইব্রেরি • নতুন বই" else "বই লাইব্রেরি • এডিট",
        icon = BookIcon,
        accent = booksAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        dismissEnabled = !isSaving,
        closeTestTag = "book_dialog_close_button",
        maxContentWidth = 760.dp,
        headerActions = {
            HeaderSavePillButton(
                text = if (isSaving) "Saving..." else "Save",
                accent = booksAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                isLoading = isSaving,
                enabled = !isSaving,
                testTag = "save_book_button",
                onClick = {
                    if (isSaving) return@HeaderSavePillButton
                    val cleanTitle = title.trim()
                    val cleanCode = subjectCode.trim()
                    if (cleanTitle.isBlank() || cleanCode.isBlank()) {
                        showToast("Please enter book title and subject code.")
                        return@HeaderSavePillButton
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
                        return@HeaderSavePillButton
                    }

                    val theoryVal = AppStrings.convertBengaliDigitsToAscii(theoryText.trim())
                        .filter { it.isDigit() }
                        .toIntOrNull() ?: 0
                    val practicalVal = AppStrings.convertBengaliDigitsToAscii(practicalText.trim())
                        .filter { it.isDigit() }
                        .toIntOrNull() ?: 0
                    if (theoryVal + practicalVal <= 0) {
                        showToast("Enter at least 1 weekly theory or practical class.")
                        return@HeaderSavePillButton
                    }
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
                    accent = booksAccent.primary,
                )

                MinimalResponsiveGrid(
                    items = libraryDepartments,
                    twoColMinWidth = 600.dp,
                    spacing = 10.dp,
                ) { _, dept ->
                    val deptAcc = departmentAccent(dept.shortCode, isDark)
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
                        color = if (isSelected) deptAcc.softTint else cs.surfaceContainerLow,
                        border = if (isSelected) {
                            BorderStroke(1.25.dp, deptAcc.borderTint)
                        } else {
                            BorderStroke(0.5.dp, cs.outlineVariant.copy(alpha = 0.45f))
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
                                        fontSize = 16.5.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = FontWeight.Bold,
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
                                                fontSize = 13.sp,
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
                fontSize = 14.sp,
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
            fontSize = 16.sp,
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
