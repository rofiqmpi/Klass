package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.mpi.klass.theme.EditIcon
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

@Composable
fun BooksLibraryScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    var books by remember { mutableStateOf<List<LibraryBook>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf<String?>(null) }
    var selectedSem by remember { mutableStateOf<String?>(null) }
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

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val headingColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentEmerald = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
    val accentBlue = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)

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
                    // Responsive Top Toolbar
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWideToolbar = maxWidth >= 560.dp
                        if (isWideToolbar) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                MinimalSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    placeholder = "Search book title, code or department...",
                                    accent = accentEmerald,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    testTag = "books_search_input",
                                    clearTestTag = "books_search_clear_button",
                                    modifier = Modifier.weight(1f),
                                )

                                MinimalStatBadge(
                                    icon = BookIcon,
                                    text = "${filteredBooks.size} Books",
                                    accent = accentEmerald,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                )

                                WelcomeMinimal3DIconButton(
                                    icon = RefreshIcon,
                                    contentDescription = "Refresh library",
                                    isDark = isDark,
                                    iconTint = accentEmerald,
                                    isLoading = isLoading,
                                    enabled = !isLoading,
                                    testTag = "books_refresh_button",
                                    onClick = { loadBooks() },
                                )

                                WelcomeMinimal3DButton(
                                    text = "Add Book",
                                    icon = PlusIcon,
                                    hindSiliguri = hindSiliguri,
                                    isPrimaryAccent = true,
                                    isDark = isDark,
                                    height = 42.dp,
                                    cornerRadius = 12.dp,
                                    testTag = "books_add_button",
                                    modifier = Modifier.widthIn(min = 124.dp),
                                    onClick = {
                                        editingBook = null
                                        showFormScreen = true
                                    },
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    MinimalSearchBar(
                                        query = searchQuery,
                                        onQueryChange = { searchQuery = it },
                                        placeholder = "Search book title, code or dept...",
                                        accent = accentEmerald,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        testTag = "books_search_input",
                                        clearTestTag = "books_search_clear_button",
                                        modifier = Modifier.weight(1f),
                                    )

                                    WelcomeMinimal3DIconButton(
                                        icon = RefreshIcon,
                                        contentDescription = "Refresh library",
                                        isDark = isDark,
                                        iconTint = accentEmerald,
                                        isLoading = isLoading,
                                        enabled = !isLoading,
                                        testTag = "books_refresh_button",
                                        onClick = { loadBooks() },
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    MinimalStatBadge(
                                        icon = BookIcon,
                                        text = "${filteredBooks.size} Books",
                                        accent = accentEmerald,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                    )

                                    WelcomeMinimal3DButton(
                                        text = "Add Book",
                                        icon = PlusIcon,
                                        hindSiliguri = hindSiliguri,
                                        isPrimaryAccent = true,
                                        isDark = isDark,
                                        height = 40.dp,
                                        cornerRadius = 12.dp,
                                        testTag = "books_add_button",
                                        modifier = Modifier.width(124.dp),
                                        onClick = {
                                            editingBook = null
                                            showFormScreen = true
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Department Filter Strip
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item(key = "dept_all") {
                            MinimalIconChip(
                                label = "All Depts",
                                selected = selectedDept == null,
                                accent = accentEmerald,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_dept_all",
                                onClick = { selectedDept = null },
                            )
                        }
                        itemsIndexed(
                            items = libraryDepartments,
                            key = { _, spec -> spec.shortCode },
                        ) { _, spec ->
                            MinimalIconChip(
                                label = spec.shortCode,
                                selected = selectedDept == spec.shortCode,
                                accent = accentEmerald,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_dept_${spec.shortCode.lowercase()}",
                                onClick = {
                                    selectedDept = if (selectedDept == spec.shortCode) null else spec.shortCode
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Semester Filter Strip
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item(key = "sem_all") {
                            MinimalIconChip(
                                label = "All Sem",
                                selected = selectedSem == null,
                                accent = accentBlue,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_sem_all",
                                onClick = { selectedSem = null },
                            )
                        }
                        itemsIndexed(
                            items = librarySemesters,
                            key = { _, sem -> sem.key },
                        ) { idx, sem ->
                            MinimalIconChip(
                                label = sem.shortEn,
                                selected = selectedSem == sem.key,
                                accent = accentBlue,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "books_filter_sem_${idx + 1}",
                                onClick = {
                                    selectedSem = if (selectedSem == sem.key) null else sem.key
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when {
                        isLoading && books.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = accentEmerald,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Loading books...",
                                        color = subTextColor,
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
                                            color = accentEmerald.copy(alpha = if (isDark) 0.16f else 0.10f),
                                            border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.25f)),
                                            modifier = Modifier.size(52.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = BookIcon,
                                                    contentDescription = null,
                                                    tint = accentEmerald,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val hasActiveFilterOrSearch = searchQuery.isNotBlank() || selectedDept != null || selectedSem != null
                                        Text(
                                            text = if (!hasActiveFilterOrSearch) {
                                                "No books in library yet"
                                            } else {
                                                "No matching books found"
                                            },
                                            color = headingColor,
                                            fontSize = 15.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        )
                                        if (!hasActiveFilterOrSearch) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tap Add Book above to register a BTEB curriculum subject.",
                                                color = subTextColor,
                                                fontSize = 12.5.sp,
                                                fontFamily = hindSiliguri,
                                                textAlign = TextAlign.Center,
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            WelcomeMinimal3DButton(
                                                text = "Clear Filters",
                                                hindSiliguri = hindSiliguri,
                                                isPrimaryAccent = false,
                                                isDark = isDark,
                                                height = 40.dp,
                                                cornerRadius = 10.dp,
                                                testTag = "books_clear_filters_button",
                                                modifier = Modifier.width(148.dp),
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
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    itemsIndexed(
                                        items = indexedRows,
                                        key = { rowIdx, row ->
                                            row.firstOrNull()?.value?.id ?: "row_$rowIdx"
                                        },
                                    ) { rowIdx, rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            rowItems.forEach { indexed ->
                                                val index = indexed.index
                                                val book = indexed.value
                                                Box(modifier = Modifier.weight(1f)) {
                                                    AnimatedStaggeredItem(index = rowIdx) {
                                                        LibraryBookCard(
                                                            book = book,
                                                            isDark = isDark,
                                                            cardBorder = cardBorder,
                                                            headingColor = headingColor,
                                                            subTextColor = subTextColor,
                                                            accentEmerald = accentEmerald,
                                                            accentBlue = accentBlue,
                                                            hindSiliguri = hindSiliguri,
                                                            testTag = "book_card_$index",
                                                            editTestTag = "book_edit_button_$index",
                                                            deleteTestTag = "book_delete_button_$index",
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
                cardBg = cardBg,
                cardBorder = cardBorder,
                headingColor = headingColor,
                subTextColor = subTextColor,
                accentEmerald = accentEmerald,
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

    bookToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingBook) bookToDelete = null },
            containerColor = cardBg,
            titleContentColor = headingColor,
            textContentColor = subTextColor,
            title = {
                Text(
                    text = "Delete Book",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
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
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = target.title,
                                color = headingColor,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "Code: ${AppStrings.convertBengaliDigitsToAscii(target.subjectCode)}",
                                color = subTextColor,
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
                    cornerRadius = 10.dp,
                    testTag = "confirm_delete_book_button",
                    modifier = Modifier.width(96.dp),
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
private fun LibraryBookCard(
    book: LibraryBook,
    isDark: Boolean,
    cardBorder: Color,
    headingColor: Color,
    subTextColor: Color,
    accentEmerald: Color,
    accentBlue: Color,
    hindSiliguri: FontFamily,
    testTag: String,
    editTestTag: String,
    deleteTestTag: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    MinimalCardSurface(
        isDark = isDark,
        shape = RoundedCornerShape(16.dp),
        borderColor = cardBorder,
        onClick = onEditClick,
        testTag = testTag,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentEmerald.copy(alpha = if (isDark) 0.16f else 0.10f),
                border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.24f)),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = BookIcon,
                        contentDescription = book.title,
                        tint = accentEmerald,
                        modifier = Modifier.size(19.dp),
                    )
                }
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
                        text = book.title,
                        color = headingColor,
                        fontSize = 14.5.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (book.subjectCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentBlue.copy(alpha = if (isDark) 0.16f else 0.10f),
                        ) {
                            Text(
                                text = AppStrings.convertBengaliDigitsToAscii(book.subjectCode),
                                color = accentBlue,
                                fontSize = 10.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (book.departmentSemesters.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for ((dept, sems) in book.departmentSemesters) {
                            val deptShort = dept.substringBefore(" (").trim()
                            val semSummary = sems.joinToString(", ") { semKey ->
                                librarySemesters.firstOrNull { it.key == semKey }?.shortEn ?: semKey
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                border = BorderStroke(0.8.dp, cardBorder),
                            ) {
                                Text(
                                    text = "$deptShort → $semSummary",
                                    color = headingColor.copy(alpha = 0.88f),
                                    fontSize = 10.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                val creditClean = AppStrings.convertBengaliDigitsToAscii(book.credit.ifBlank { "0" })
                Text(
                    text = "${book.theoryClassesPerWeek}T / wk • ${book.practicalClassesPerWeek}P / wk • $creditClean Cr",
                    color = subTextColor,
                    fontSize = 11.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(editTestTag),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentBlue.copy(alpha = if (isDark) 0.14f else 0.08f),
                        modifier = Modifier.size(34.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = EditIcon,
                                contentDescription = "Edit book",
                                tint = accentBlue,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                }

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
                                contentDescription = "Delete book",
                                tint = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                modifier = Modifier.size(15.dp),
                            )
                        }
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
    cardBg: Color,
    cardBorder: Color,
    headingColor: Color,
    subTextColor: Color,
    accentEmerald: Color,
    hindSiliguri: FontFamily,
    onDismiss: () -> Unit,
    onSave: (LibraryBook, onComplete: () -> Unit) -> Unit,
) {
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

    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val inputBg = if (isDark) Color(0xFF1E293B) else Color.White

    MinimalFullScreenSheet(
        title = if (initialBook == null) "Add Book" else "Edit Book",
        subtitle = "BTEB Curriculum Catalog",
        icon = BookIcon,
        accent = accentEmerald,
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
                WelcomeMinimal3DButton(
                    text = "Cancel",
                    hindSiliguri = hindSiliguri,
                    isPrimaryAccent = false,
                    isDark = isDark,
                    enabled = !isSaving,
                    height = 46.dp,
                    cornerRadius = 12.dp,
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
                    cornerRadius = 12.dp,
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
                            headingColor = headingColor,
                            subTextColor = subTextColor,
                            inputBg = inputBg,
                            inputBorder = cardBorder,
                            accentColor = accentEmerald,
                            hindSiliguri = hindSiliguri,
                            testTag = "book_title_input",
                            modifier = Modifier.weight(1.4f),
                        )
                        MinimalBookTextField(
                            label = "Subject Code *",
                            value = subjectCode,
                            onValueChange = { subjectCode = it },
                            placeholder = "e.g. 25911",
                            headingColor = headingColor,
                            subTextColor = subTextColor,
                            inputBg = inputBg,
                            inputBorder = cardBorder,
                            accentColor = accentEmerald,
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
                            headingColor = headingColor,
                            subTextColor = subTextColor,
                            inputBg = inputBg,
                            inputBorder = cardBorder,
                            accentColor = accentEmerald,
                            hindSiliguri = hindSiliguri,
                            testTag = "book_title_input",
                        )
                        MinimalBookTextField(
                            label = "Subject Code *",
                            value = subjectCode,
                            onValueChange = { subjectCode = it },
                            placeholder = "e.g. 25911",
                            headingColor = headingColor,
                            subTextColor = subTextColor,
                            inputBg = inputBg,
                            inputBorder = cardBorder,
                            accentColor = accentEmerald,
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
                    placeholder = "e.g. 3",
                    headingColor = headingColor,
                    subTextColor = subTextColor,
                    inputBg = inputBg,
                    inputBorder = cardBorder,
                    accentColor = accentEmerald,
                    hindSiliguri = hindSiliguri,
                    keyboardType = KeyboardType.Number,
                    testTag = "book_theory_input",
                    modifier = Modifier.weight(1f),
                )
                MinimalBookTextField(
                    label = "Practical / Wk",
                    value = practicalText,
                    onValueChange = { practicalText = it },
                    placeholder = "e.g. 3",
                    headingColor = headingColor,
                    subTextColor = subTextColor,
                    inputBg = inputBg,
                    inputBorder = cardBorder,
                    accentColor = accentEmerald,
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
                    headingColor = headingColor,
                    subTextColor = subTextColor,
                    inputBg = inputBg,
                    inputBorder = cardBorder,
                    accentColor = accentEmerald,
                    hindSiliguri = hindSiliguri,
                    keyboardType = KeyboardType.Decimal,
                    testTag = "book_credit_input",
                    modifier = Modifier.weight(1f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Departments & Semesters",
                    color = headingColor,
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
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
                        shape = RoundedCornerShape(12.dp),
                        color = cardBg,
                        border = BorderStroke(
                            width = if (isSelected) 1.3.dp else 1.dp,
                            color = if (isSelected) accentEmerald else cardBorder,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable {
                                        expandedDepts[dept.shortCode] = !isExpanded
                                    }
                                    .padding(horizontal = 12.dp)
                                    .testTag("book_dept_toggle_${dept.shortCode}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) accentEmerald else Color.Transparent
                                            )
                                            .border(
                                                1.4.dp,
                                                if (isSelected) accentEmerald else subTextColor.copy(alpha = 0.55f),
                                                RoundedCornerShape(6.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = CheckIcon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp),
                                            )
                                        }
                                    }

                                    Text(
                                        text = dept.fullName,
                                        color = headingColor,
                                        fontSize = 13.sp,
                                        fontFamily = hindSiliguri,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (isSelected) {
                                        val countStr = if (selectedSems.size == librarySemesters.size) {
                                            "All"
                                        } else {
                                            "${selectedSems.size} Sem"
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = accentEmerald.copy(alpha = if (isDark) 0.18f else 0.11f),
                                        ) {
                                            Text(
                                                text = countStr,
                                                color = accentEmerald,
                                                fontSize = 11.sp,
                                                fontFamily = hindSiliguri,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = ChevronDownIcon,
                                        contentDescription = null,
                                        tint = subTextColor,
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
                                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    val allSelected = selectedSems.size == librarySemesters.size
                                    MinimalSemesterChip(
                                        label = "All",
                                        selected = allSelected,
                                        accentColor = accentEmerald,
                                        cardBg = screenBg,
                                        headingColor = subTextColor,
                                        borderColor = cardBorder,
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
                                            accentColor = accentEmerald,
                                            cardBg = screenBg,
                                            headingColor = headingColor,
                                            borderColor = cardBorder,
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
    accentColor: Color,
    cardBg: Color,
    headingColor: Color,
    borderColor: Color,
    hindSiliguri: FontFamily,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accentColor else cardBg,
        border = BorderStroke(1.dp, if (selected) accentColor else borderColor),
        modifier = Modifier.testTag(testTag),
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else headingColor,
            fontSize = 12.sp,
            fontFamily = hindSiliguri,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MinimalBookTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    headingColor: Color,
    subTextColor: Color,
    inputBg: Color,
    inputBorder: Color,
    accentColor: Color,
    hindSiliguri: FontFamily,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String,
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) accentColor else inputBorder,
        animationSpec = tween(160),
        label = "MinimalBookFieldBorder",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = headingColor,
            fontSize = 12.5.sp,
            fontFamily = hindSiliguri,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = inputBg,
            border = BorderStroke(if (isFocused) 1.4.dp else 1.dp, borderColor),
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
                        color = subTextColor.copy(alpha = 0.72f),
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
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
                        color = headingColor,
                        fontSize = 14.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Medium,
                    ),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                        .testTag(testTag),
                )
            }
        }
    }
}
