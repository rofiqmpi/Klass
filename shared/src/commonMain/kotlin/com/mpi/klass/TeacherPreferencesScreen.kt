package com.mpi.klass

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.dayPaletteAt
import com.mpi.klass.theme.departmentAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherPreferencesScreen(
    email: String,
    userName: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 72.dp,
    isOnboardingPrompt: Boolean = false,
    onFullScreenOverlayChange: (Boolean) -> Unit = {},
    onDismissOnboarding: (() -> Unit)? = null,
    onSaved: (TeacherSchedulingPref) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val cleanEmail = email.trim().lowercase()
    val teachingAccent = KlassFeaturePalette.teaching(isDark)

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var teacherName by remember(cleanEmail) { mutableStateOf(userName) }
    var selectedDept by remember(cleanEmail) { mutableStateOf("CST") }
    var selectedShift by remember(cleanEmail) { mutableStateOf(RoutineRules.SHIFT_1) }
    var selectedTimePref by remember(cleanEmail) { mutableStateOf(TeacherTimePreference.ANY) }

    var allBooks by remember { mutableStateOf<List<LibraryBook>>(emptyList()) }
    var bookDeptFilter by remember { mutableStateOf("ALL") }
    var activeSemesterKey by remember { mutableStateOf<String?>(null) }

    val selectedBySemester = remember(cleanEmail) {
        mutableStateMapOf<String, Set<String>>()
    }

    LaunchedEffect(activeSemesterKey != null) {
        onFullScreenOverlayChange(activeSemesterKey != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    LaunchedEffect(cleanEmail) {
        isLoading = true
        try {
            val loadedBooks = RoleConfig.fetchLibraryBooks()
                .ifEmpty { BtebBooksCatalog.books }
            allBooks = loadedBooks

            val existingPref = if (cleanEmail.isNotBlank()) {
                fetchTeacherSchedulingPrefFromFirestore(cleanEmail)
            } else {
                null
            }
            val profile = if (cleanEmail.isNotBlank()) {
                RoleConfig.fetchStudentProfile(cleanEmail)
            } else {
                null
            }

            if (existingPref != null) {
                teacherName = existingPref.name.ifBlank { userName }
                selectedDept = RoutineRules.normalizeDepartmentShort(
                    existingPref.department.ifBlank { profile?.department ?: "CST" }
                ).ifBlank { "CST" }
                selectedShift = RoutineRules.normalizeShift(
                    existingPref.shift.ifBlank { profile?.shift ?: RoutineRules.SHIFT_1 }
                )
                selectedTimePref = existingPref.timePreference
                selectedBySemester.clear()
                existingPref.semesterSubjects.forEach { (sem, codes) ->
                    val canon = RoutineRules.canonicalSemesterKey(sem).ifBlank { sem }
                    val existing = selectedBySemester[canon] ?: emptySet()
                    selectedBySemester[canon] = existing + codes.map { it.trim() }.filter { it.isNotBlank() }
                }
            } else if (profile != null) {
                teacherName = profile.name.ifBlank { userName }
                val profDept = RoutineRules.normalizeDepartmentShort(profile.department)
                if (profDept.isNotBlank()) selectedDept = profDept
                if (profile.shift.isNotBlank()) {
                    selectedShift = RoutineRules.normalizeShift(profile.shift)
                }
            }
            bookDeptFilter = if (selectedDept in RoutineRules.TECH_DEPARTMENTS) selectedDept else "ALL"
        } finally {
            isLoading = false
        }
    }

    val totalSelectedCount = selectedBySemester.values.sumOf { it.size }
    val deptColor = departmentAccent(selectedDept, isDark)

    fun savePrefs() {
        if (cleanEmail.isBlank()) {
            showToast("Sign in required to save preferences.")
            return
        }
        if (totalSelectedCount == 0) {
            showToast("Select at least one subject in any semester.")
            return
        }
        scope.launch {
            isSaving = true
            try {
                val cleanedMap = selectedBySemester
                    .filterValues { it.isNotEmpty() }
                    .mapValues { (_, v) -> v.toList().sorted() }
                val pref = TeacherSchedulingPref(
                    email = cleanEmail,
                    name = teacherName.ifBlank { cleanEmail.substringBefore("@") },
                    department = selectedDept,
                    shift = selectedShift,
                    timePreference = selectedTimePref,
                    semesterSubjects = cleanedMap,
                    schedulingSetupComplete = true,
                    updatedAt = getCurrentTimeMillis(),
                )
                val res = saveTeacherSchedulingPrefInFirestore(pref)
                if (res.isSuccess) {
                    showToast("Teaching preferences saved!")
                    onSaved(pref)
                } else {
                    showToast("Could not sync to cloud; check network.")
                }
            } finally {
                isSaving = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalBackdrop(isDark = isDark, focusAccent = teachingAccent.primary) {
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

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = teachingAccent.primary, strokeWidth = 2.5.dp)
                    }
                    return@Column
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .widthIn(max = 920.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
                    ) {
                        // Top Hero Summary
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                KlassHeroBanner(
                                    title = teacherName.ifBlank { "Teaching Setup" },
                                    subtitle = "Select department, shift & subjects per semester",
                                    icon = GraduationCapIcon,
                                    accent = teachingAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    badges = listOf(
                                        selectedDept to deptColor.primary,
                                        selectedShift to teachingAccent.primary,
                                        "$totalSelectedCount Subjects" to KlassFeaturePalette.books(isDark).primary,
                                    ),
                                    trailingContent = if (isOnboardingPrompt && onDismissOnboarding != null) {
                                        {
                                            KlassTonalButton(
                                                text = "Skip",
                                                accent = teachingAccent.primary,
                                                isDark = isDark,
                                                hindSiliguri = hindSiliguri,
                                                testTag = "skip_teacher_setup_button",
                                                onClick = onDismissOnboarding,
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                )
                            }
                            SocialSectionDividerBand(isDark = isDark, height = 10.dp)
                        }

                        // Department, Shift & Preferred Class Hours (2-col on wide screens)
                        item {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val twoCol = maxWidth >= 640.dp

                                val deptShiftCard: @Composable (Modifier) -> Unit = { mod ->
                                    SocialSurfaceBlock(
                                        isDark = isDark,
                                        showTopHairline = true,
                                        showBottomHairline = true,
                                        accentTint = deptColor.primary,
                                        modifier = mod,
                                    ) {
                                        GoogleSectionHeader(
                                            title = "Department & Shift",
                                            hindSiliguri = hindSiliguri,
                                            accent = deptColor.primary,
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                RoutineRules.ALL_TEACHER_DEPARTMENTS.forEach { dept ->
                                                    val isSelected = selectedDept.equals(dept, ignoreCase = true)
                                                    val dAccent = departmentAccent(dept, isDark)
                                                    MinimalIconChip(
                                                        label = dept,
                                                        selected = isSelected,
                                                        accent = dAccent.primary,
                                                        isDark = isDark,
                                                        hindSiliguri = hindSiliguri,
                                                        testTag = "teacher_dept_$dept",
                                                        onClick = {
                                                            selectedDept = dept
                                                            if (dept in RoutineRules.TECH_DEPARTMENTS) {
                                                                bookDeptFilter = dept
                                                            }
                                                        },
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2).forEach { sh ->
                                                    val isSelected = selectedShift == sh
                                                    MinimalIconChip(
                                                        label = sh,
                                                        selected = isSelected,
                                                        accent = teachingAccent.primary,
                                                        isDark = isDark,
                                                        hindSiliguri = hindSiliguri,
                                                        testTag = "teacher_shift_$sh",
                                                        modifier = Modifier.weight(1f),
                                                        onClick = { selectedShift = sh },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                val timePrefCard: @Composable (Modifier) -> Unit = { mod ->
                                    SocialSurfaceBlock(
                                        isDark = isDark,
                                        showTopHairline = true,
                                        showBottomHairline = true,
                                        accentTint = teachingAccent.primary,
                                        modifier = mod,
                                    ) {
                                        GoogleSectionHeader(
                                            title = "Preferred Class Hours",
                                            hindSiliguri = hindSiliguri,
                                            accent = teachingAccent.primary,
                                        )
                                        TeacherTimePreference.entries.forEachIndexed { idx, pref ->
                                            val isSelected = selectedTimePref == pref
                                            Surface(
                                                onClick = { selectedTimePref = pref },
                                                color = if (isSelected) teachingAccent.softTint else Color.Transparent,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("teacher_time_${pref.key}"),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = pref.label,
                                                            fontSize = 16.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = hindSiliguri,
                                                            color = if (isSelected) teachingAccent.primary else cs.onSurface,
                                                        )
                                                        Text(
                                                            text = pref.subtitle,
                                                            fontSize = 14.sp,
                                                            fontFamily = hindSiliguri,
                                                            color = cs.onSurfaceVariant,
                                                        )
                                                    }
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = null,
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = teachingAccent.primary,
                                                            unselectedColor = cs.onSurfaceVariant,
                                                        ),
                                                    )
                                                }
                                            }
                                            if (idx < TeacherTimePreference.entries.lastIndex) {
                                                SocialInsetDivider(startIndent = 16.dp)
                                            }
                                        }
                                    }
                                }

                                if (twoCol) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        deptShiftCard(Modifier.weight(1f))
                                        timePrefCard(Modifier.weight(1f))
                                    }
                                } else {
                                    Column {
                                        deptShiftCard(Modifier.fillMaxWidth())
                                        SocialSectionDividerBand(isDark = isDark)
                                        timePrefCard(Modifier.fillMaxWidth())
                                    }
                                }
                            }
                            SocialSectionDividerBand(isDark = isDark)
                        }

                        // Clean Semester Navigation Rows (Tap to open Level 1 Subject Picker Sub-Screen)
                        item {
                            SocialSurfaceBlock(
                                isDark = isDark,
                                showTopHairline = true,
                                showBottomHairline = true,
                            ) {
                                GoogleSectionHeader(
                                    title = "Subjects by Semester (Tap to Pick)",
                                    hindSiliguri = hindSiliguri,
                                    accent = teachingAccent.primary,
                                )

                                RoutineRules.ALL_SEMESTER_KEYS.forEachIndexed { idx, semKey ->
                                    val dayAccent = dayPaletteAt(idx, isDark)
                                    val selectedCodes = selectedBySemester[semKey] ?: emptySet()
                                    val availableCount = remember(allBooks, bookDeptFilter, semKey) {
                                        allBooks.count { book ->
                                            book.departmentSemesters.any { (dept, sems) ->
                                                val deptOk = bookDeptFilter == "ALL" ||
                                                    RoutineRules.normalizeDepartmentShort(dept) == bookDeptFilter
                                                val semOk = sems.any { RoutineRules.canonicalSemesterKey(it) == semKey }
                                                deptOk && semOk
                                            }
                                        }
                                    }
                                    val subtitleText = if (selectedCodes.isEmpty()) {
                                        "Tap to choose from $availableCount subjects"
                                    } else {
                                        "${selectedCodes.size} selected • ${selectedCodes.sorted().joinToString(", ")}"
                                    }

                                    KlassNavigationRow(
                                        title = RoutineRules.semesterDisplayLabel(semKey),
                                        subtitle = subtitleText,
                                        icon = BookIcon,
                                        accent = dayAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        badgeText = "${selectedCodes.size} / $availableCount",
                                        badgeColor = if (selectedCodes.isNotEmpty()) dayAccent.primary else cs.onSurfaceVariant,
                                        testTag = "teacher_sem_row_${idx + 1}",
                                        onClick = { activeSemesterKey = semKey },
                                    )
                                    if (idx < RoutineRules.ALL_SEMESTER_KEYS.lastIndex) {
                                        SocialInsetDivider(startIndent = 72.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Calibrated Bottom Save Bar
                Surface(
                    color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.94f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = teachingAccent.borderTint,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 920.dp)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            WelcomeMinimal3DButton(
                                text = "Save Preferences ($totalSelectedCount)",
                                icon = CheckIcon,
                                hindSiliguri = hindSiliguri,
                                isDark = isDark,
                                isLoading = isSaving,
                                customPrimaryColor = teachingAccent.primary,
                                customBaseColor = teachingAccent.deep,
                                height = 48.dp,
                                cornerRadius = 16.dp,
                                testTag = "save_teacher_preferences_button",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { savePrefs() },
                            )
                        }
                    }
                }
            }
        }

        // Level 1 Sub-Screen: Semester Subject Picker
        KlassSubScreenHost(visible = activeSemesterKey != null, zIndex = 85f) {
            val semKey = activeSemesterKey
            if (semKey != null) {
                SemesterSubjectPickerSubScreen(
                    semKey = semKey,
                    allBooks = allBooks,
                    initialDeptFilter = bookDeptFilter,
                    selectedCodes = selectedBySemester[semKey] ?: emptySet(),
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDeptFilterChange = { bookDeptFilter = it },
                    onToggleCode = { code ->
                        val cur = selectedBySemester[semKey] ?: emptySet()
                        selectedBySemester[semKey] = if (code in cur) cur - code else cur + code
                    },
                    onSelectAll = { visibleCodes ->
                        val cur = selectedBySemester[semKey] ?: emptySet()
                        selectedBySemester[semKey] = cur + visibleCodes
                    },
                    onClearSemester = {
                        selectedBySemester[semKey] = emptySet()
                    },
                    onDismiss = { activeSemesterKey = null },
                )
            }
        }
    }
}

@Composable
private fun SemesterSubjectPickerSubScreen(
    semKey: String,
    allBooks: List<LibraryBook>,
    initialDeptFilter: String,
    selectedCodes: Set<String>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onDeptFilterChange: (String) -> Unit,
    onToggleCode: (String) -> Unit,
    onSelectAll: (Set<String>) -> Unit,
    onClearSemester: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val semIdx = RoutineRules.ALL_SEMESTER_KEYS.indexOf(semKey).coerceAtLeast(0)
    val dayAccent = dayPaletteAt(semIdx, isDark)
    val teachingAccent = KlassFeaturePalette.teaching(isDark)

    var searchQuery by remember(semKey) { mutableStateOf("") }
    var inspectingBook by remember { mutableStateOf<LibraryBook?>(null) }

    val semBooks = remember(allBooks, initialDeptFilter, searchQuery, semKey) {
        val q = searchQuery.trim().lowercase()
        allBooks.filter { book ->
            val matchesSemAndDept = book.departmentSemesters.any { (dept, sems) ->
                val deptOk = initialDeptFilter == "ALL" ||
                    RoutineRules.normalizeDepartmentShort(dept) == initialDeptFilter
                val semOk = sems.any { RoutineRules.canonicalSemesterKey(it) == semKey }
                deptOk && semOk
            }
            val queryOk = q.isBlank() ||
                book.title.lowercase().contains(q) ||
                book.subjectCode.lowercase().contains(q)
            matchesSemAndDept && queryOk
        }.distinctBy { it.subjectCode.trim() }
            .sortedBy { it.subjectCode }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = RoutineRules.semesterDisplayLabel(semKey),
            subtitle = "${selectedCodes.size} of ${semBooks.size} selected",
            levelLabel = "Teaching Setup • Level 1",
            icon = BookIcon,
            accent = dayAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onDismiss,
            headerActions = {
                if (semBooks.isNotEmpty()) {
                    KlassTonalButton(
                        text = "All",
                        icon = CheckIcon,
                        accent = dayAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 36.dp,
                        onClick = { onSelectAll(semBooks.map { it.subjectCode.trim() }.toSet()) },
                    )
                }
                if (selectedCodes.isNotEmpty()) {
                    KlassTonalButton(
                        text = "Clear",
                        icon = CloseIcon,
                        accent = KlassFeaturePalette.error(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 36.dp,
                        onClick = onClearSemester,
                    )
                }
            },
            bottomBar = {
                WelcomeMinimal3DButton(
                    text = "Done (${selectedCodes.size} selected)",
                    icon = CheckIcon,
                    hindSiliguri = hindSiliguri,
                    isDark = isDark,
                    customPrimaryColor = teachingAccent.primary,
                    customBaseColor = teachingAccent.deep,
                    height = 48.dp,
                    cornerRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val filterDepts = remember { listOf("ALL") + RoutineRules.TECH_DEPARTMENTS }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterDepts) { dept ->
                        val chipAccent = if (dept == "ALL") dayAccent.primary else departmentAccent(dept, isDark).primary
                        MinimalIconChip(
                            label = if (dept == "ALL") "All Depts" else dept,
                            selected = initialDeptFilter == dept,
                            accent = chipAccent,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { onDeptFilterChange(dept) },
                        )
                    }
                }

                MinimalSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search subject code or title",
                    accent = dayAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }

            if (semBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    KlassEmptyState(
                        icon = BookIcon,
                        title = "No Subjects Match",
                        subtitle = "Try another department filter or clear your search.",
                        accent = dayAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                ) {
                    item {
                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            semBooks.forEachIndexed { idx, book ->
                                val code = book.subjectCode.trim()
                                val checked = code in selectedCodes
                                Surface(
                                    onClick = { onToggleCode(code) },
                                    color = if (checked) dayAccent.softTint else Color.Transparent,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = dayAccent.primary,
                                                uncheckedColor = cs.onSurfaceVariant,
                                            ),
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = book.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = hindSiliguri,
                                                color = if (checked) dayAccent.primary else cs.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${book.subjectCode} • ${book.theoryClassesPerWeek}T • ${book.practicalClassesPerWeek}P",
                                                fontSize = 14.sp,
                                                fontFamily = hindSiliguri,
                                                color = cs.onSurfaceVariant,
                                            )
                                        }

                                        IconButton(
                                            onClick = { inspectingBook = book },
                                            modifier = Modifier.size(48.dp),
                                        ) {
                                            Icon(
                                                imageVector = InfoCircleIcon,
                                                contentDescription = "Subject details",
                                                tint = dayAccent.primary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                                if (idx < semBooks.lastIndex) {
                                    SocialInsetDivider(startIndent = 56.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Level 2 Sub-Sub-Screen: Quick Subject Syllabus Inspection
        KlassSubScreenHost(visible = inspectingBook != null, zIndex = 95f) {
            inspectingBook?.let { book ->
                val isSelected = book.subjectCode.trim() in selectedCodes
                MinimalFullScreenSheet(
                    title = book.title,
                    subtitle = "Subject Code ${book.subjectCode}",
                    levelLabel = "Subject Details • Level 2",
                    icon = InfoCircleIcon,
                    accent = dayAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { inspectingBook = null },
                    bottomBar = {
                        WelcomeMinimal3DButton(
                            text = if (isSelected) "Remove Subject" else "Select Subject",
                            icon = if (isSelected) CloseIcon else CheckIcon,
                            hindSiliguri = hindSiliguri,
                            isDark = isDark,
                            isDestructive = isSelected,
                            customPrimaryColor = if (isSelected) null else dayAccent.primary,
                            customBaseColor = if (isSelected) null else dayAccent.deep,
                            height = 48.dp,
                            cornerRadius = 16.dp,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onToggleCode(book.subjectCode.trim())
                                inspectingBook = null
                            },
                        )
                    },
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
                                title = book.title,
                                subtitle = "Code ${book.subjectCode}",
                                icon = BookIcon,
                                accent = dayAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                badges = listOf(
                                    "${book.theoryClassesPerWeek} Theory/wk" to KlassFeaturePalette.routine(isDark).primary,
                                    "${book.practicalClassesPerWeek} Practical/wk" to teachingAccent.primary,
                                    "${book.credit} Credit" to KlassFeaturePalette.books(isDark).primary,
                                ),
                            )
                        }

                        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                            book.departmentSemesters.entries.forEachIndexed { idx, (dept, sems) ->
                                val dAccent = departmentAccent(dept, isDark)
                                KlassDetailFieldRow(
                                    label = "$dept Department",
                                    value = sems.joinToString(", ") { RoutineRules.semesterDisplayLabel(it) },
                                    icon = LayerGroupIcon,
                                    accent = dAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                )
                                if (idx < book.departmentSemesters.size - 1) {
                                    SocialInsetDivider(startIndent = 68.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
