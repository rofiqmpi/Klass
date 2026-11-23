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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.InfoCircleIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.dayPaletteAt
import com.mpi.klass.theme.departmentAccent
import kotlinx.coroutines.launch

private enum class SubjectPickerStep {
    DEPT,
    SEMESTER,
    SUBJECTS,
}

private fun isNonTechSubjectCode(code: String): Boolean {
    val c = code.trim()
    return c.startsWith("257") || c.startsWith("258") || c.startsWith("259")
}

private fun booksForDeptAndSem(
    allBooks: List<LibraryBook>,
    dept: String,
    semKey: String,
): List<LibraryBook> {
    return allBooks.filter { book ->
        if (dept.equals("Non-Tech", ignoreCase = true)) {
            book.departmentSemesters.any { (d, sems) ->
                (d.equals("Non-Tech", ignoreCase = true) || isNonTechSubjectCode(book.subjectCode)) &&
                    sems.any { RoutineRules.canonicalSemesterKey(it) == semKey }
            }
        } else {
            book.departmentSemesters.any { (d, sems) ->
                RoutineRules.normalizeDepartmentShort(d) == dept &&
                    sems.any { RoutineRules.canonicalSemesterKey(it) == semKey }
            }
        }
    }.distinctBy { it.subjectCode.trim() }
        .sortedBy { it.subjectCode }
}

private fun booksForDeptAnySem(
    allBooks: List<LibraryBook>,
    dept: String,
): List<LibraryBook> {
    return allBooks.filter { book ->
        if (dept.equals("Non-Tech", ignoreCase = true)) {
            book.departmentSemesters.keys.any { it.equals("Non-Tech", ignoreCase = true) } ||
                isNonTechSubjectCode(book.subjectCode)
        } else {
            book.departmentSemesters.keys.any { RoutineRules.normalizeDepartmentShort(it) == dept }
        }
    }.distinctBy { it.subjectCode.trim() }
}

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
    var wizardStep by remember { mutableStateOf<SubjectPickerStep?>(null) }
    var wizardDept by remember { mutableStateOf("CST") }
    var wizardSem by remember { mutableStateOf(RoutineRules.ALL_SEMESTER_KEYS.first()) }

    val selectedBySemester = remember(cleanEmail) {
        mutableStateMapOf<String, Set<String>>()
    }

    LaunchedEffect(wizardStep != null) {
        onFullScreenOverlayChange(wizardStep != null)
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenOverlayChange(false) }
    }

    AppBackHandler(enabled = wizardStep != null) {
        wizardStep = when (wizardStep) {
            SubjectPickerStep.SUBJECTS -> SubjectPickerStep.SEMESTER
            SubjectPickerStep.SEMESTER -> SubjectPickerStep.DEPT
            SubjectPickerStep.DEPT -> null
            null -> null
        }
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
            wizardDept = selectedDept.takeIf { it in RoutineRules.ALL_TEACHER_DEPARTMENTS } ?: "CST"
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
            showToast("Select at least one teaching subject before saving.")
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
                    KlassHeroAndToggleListSkeleton(
                        isDark = isDark,
                        rowCount = 5,
                        showSwitchPills = false,
                        showChipGrid = true,
                    )
                    return@Column
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 920.dp)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (isOnboardingPrompt && totalSelectedCount == 0) {
                            "Required: Pick subjects from any dept & semester"
                        } else {
                            "$totalSelectedCount subjects selected"
                        },
                        color = cs.onSurfaceVariant,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = hindSiliguri,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HeaderSavePillButton(
                        text = if (isSaving) "Saving..." else "Save",
                        accent = teachingAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        isLoading = isSaving,
                        enabled = !isSaving,
                        testTag = "save_teacher_preferences_button",
                        onClick = { savePrefs() },
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .widthIn(max = 920.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    ) {
                        // Home Department, Shift & Preferred Class Hours
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
                                            title = "Home Department & Shift",
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
                                                        onClick = { selectedDept = dept },
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

                        // Step-by-Step Cross-Department Teaching Subjects Setup
                        item {
                            SocialSurfaceBlock(
                                isDark = isDark,
                                showTopHairline = true,
                                showBottomHairline = true,
                                accentTint = teachingAccent.primary,
                            ) {
                                GoogleSectionHeader(
                                    title = "Teaching Subjects (All Departments)",
                                    hindSiliguri = hindSiliguri,
                                    accent = teachingAccent.primary,
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        text = "Pick subjects sequentially by Department → Semester → Subject across CST, CT, ET, MT, PT, ENT, EMT, and Non-Tech.",
                                        fontSize = 13.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                    )

                                    KlassPrimaryButton(
                                        text = if (totalSelectedCount == 0) {
                                            "Select Teaching Subjects"
                                        } else {
                                            "Add / Edit Teaching Subjects ($totalSelectedCount)"
                                        },
                                        icon = BookIcon,
                                        hindSiliguri = hindSiliguri,
                                        isDark = isDark,
                                        customPrimaryColor = teachingAccent.primary,
                                        height = 48.dp,
                                        cornerRadius = 14.dp,
                                        testTag = "teacher_select_subjects_button",
                                        onClick = { wizardStep = SubjectPickerStep.DEPT },
                                    )
                                }

                                val nonEmptySems = RoutineRules.ALL_SEMESTER_KEYS.filter {
                                    (selectedBySemester[it]?.size ?: 0) > 0
                                }
                                if (nonEmptySems.isNotEmpty()) {
                                    SocialInsetDivider(startIndent = 16.dp)
                                    nonEmptySems.forEachIndexed { idx, semKey ->
                                        val semIdx = RoutineRules.ALL_SEMESTER_KEYS.indexOf(semKey).coerceAtLeast(0)
                                        val dayAccent = dayPaletteAt(semIdx, isDark)
                                        val codes = selectedBySemester[semKey].orEmpty().sorted()
                                        KlassNavigationRow(
                                            title = RoutineRules.semesterDisplayLabel(semKey),
                                            subtitle = codes.joinToString(", "),
                                            icon = BookIcon,
                                            accent = dayAccent.primary,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            badgeText = "${codes.size} selected",
                                            badgeColor = dayAccent.primary,
                                            onClick = {
                                                wizardSem = semKey
                                                wizardStep = SubjectPickerStep.DEPT
                                            },
                                        )
                                        if (idx < nonEmptySems.lastIndex) {
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

        // Sequential Step 1: Choose Any Department (All 8 Departments, Unfiltered)
        KlassSubScreenHost(visible = wizardStep == SubjectPickerStep.DEPT, zIndex = 84f) {
            DepartmentPickerStepScreen(
                allBooks = allBooks,
                selectedBySemester = selectedBySemester,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onSelectDepartment = { dept ->
                    wizardDept = dept
                    wizardStep = SubjectPickerStep.SEMESTER
                },
                onDismiss = { wizardStep = null },
            )
        }

        // Sequential Step 2: Choose Semester (1st - 7th) for Chosen Department
        KlassSubScreenHost(visible = wizardStep == SubjectPickerStep.SEMESTER, zIndex = 88f) {
            SemesterPickerStepScreen(
                department = wizardDept,
                allBooks = allBooks,
                selectedBySemester = selectedBySemester,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onSelectSemester = { semKey ->
                    wizardSem = semKey
                    wizardStep = SubjectPickerStep.SUBJECTS
                },
                onFinish = { wizardStep = null },
                onBackToDepts = { wizardStep = SubjectPickerStep.DEPT },
            )
        }

        // Sequential Step 3: Pick Subjects for (wizardDept, wizardSem)
        KlassSubScreenHost(visible = wizardStep == SubjectPickerStep.SUBJECTS, zIndex = 92f) {
            SubjectChecklistStepScreen(
                department = wizardDept,
                semKey = wizardSem,
                allBooks = allBooks,
                selectedCodes = selectedBySemester[wizardSem] ?: emptySet(),
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onToggleCode = { code ->
                    val cur = selectedBySemester[wizardSem] ?: emptySet()
                    selectedBySemester[wizardSem] = if (code in cur) cur - code else cur + code
                },
                onSelectAll = { visibleCodes ->
                    val cur = selectedBySemester[wizardSem] ?: emptySet()
                    selectedBySemester[wizardSem] = cur + visibleCodes
                },
                onClearDeptVisible = { visibleCodes ->
                    val cur = selectedBySemester[wizardSem] ?: emptySet()
                    selectedBySemester[wizardSem] = cur - visibleCodes
                },
                onFinishAll = { wizardStep = null },
                onBackToSemesters = { wizardStep = SubjectPickerStep.SEMESTER },
            )
        }
    }
}

@Composable
private fun DepartmentPickerStepScreen(
    allBooks: List<LibraryBook>,
    selectedBySemester: Map<String, Set<String>>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSelectDepartment: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val teachingAccent = KlassFeaturePalette.teaching(isDark)
    val totalSelected = selectedBySemester.values.sumOf { it.size }
    val allSelectedCodes = remember(selectedBySemester.toMap()) {
        selectedBySemester.values.flatten().toSet()
    }

    MinimalFullScreenSheet(
        title = "Step 1: Department",
        subtitle = "$totalSelected subject(s) picked so far",
        levelLabel = "Teaching Setup • 1 of 3",
        icon = GraduationCapIcon,
        accent = teachingAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onDismiss,
        headerActions = {
            HeaderSavePillButton(
                text = "Done",
                accent = teachingAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onClick = onDismiss,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            item {
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "All Departments (Select Any)",
                        hindSiliguri = hindSiliguri,
                        accent = teachingAccent.primary,
                    )

                    RoutineRules.ALL_TEACHER_DEPARTMENTS.forEachIndexed { idx, dept ->
                        val dAccent = departmentAccent(dept, isDark)
                        val deptBooks = remember(allBooks, dept) { booksForDeptAnySem(allBooks, dept) }
                        val deptSelectedCount = remember(deptBooks, allSelectedCodes) {
                            deptBooks.count { it.subjectCode.trim() in allSelectedCodes }
                        }
                        KlassNavigationRow(
                            title = if (dept == "Non-Tech") "Non-Tech Subjects" else "$dept Department",
                            subtitle = "${deptBooks.size} subjects across semesters • $deptSelectedCount selected",
                            icon = GraduationCapIcon,
                            accent = dAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            badgeText = if (deptSelectedCount > 0) "$deptSelectedCount picked" else "${deptBooks.size}",
                            badgeColor = if (deptSelectedCount > 0) dAccent.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            testTag = "wizard_dept_$dept",
                            onClick = { onSelectDepartment(dept) },
                        )
                        if (idx < RoutineRules.ALL_TEACHER_DEPARTMENTS.lastIndex) {
                            SocialInsetDivider(startIndent = 72.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SemesterPickerStepScreen(
    department: String,
    allBooks: List<LibraryBook>,
    selectedBySemester: Map<String, Set<String>>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onSelectSemester: (String) -> Unit,
    onFinish: () -> Unit,
    onBackToDepts: () -> Unit,
) {
    val deptAccent = departmentAccent(department, isDark)

    MinimalFullScreenSheet(
        title = "Step 2: $department Semester",
        subtitle = "Pick a semester to select subjects",
        levelLabel = "Teaching Setup • 2 of 3",
        icon = LayerGroupIcon,
        accent = deptAccent.primary,
        isDark = isDark,
        hindSiliguri = hindSiliguri,
        onDismiss = onBackToDepts,
        headerActions = {
            HeaderSavePillButton(
                text = "Finish",
                accent = KlassFeaturePalette.teaching(isDark).primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                onClick = onFinish,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            item {
                SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                    GoogleSectionHeader(
                        title = "$department • All 7 Semesters",
                        hindSiliguri = hindSiliguri,
                        accent = deptAccent.primary,
                    )

                    RoutineRules.ALL_SEMESTER_KEYS.forEachIndexed { idx, semKey ->
                        val dayAccent = dayPaletteAt(idx, isDark)
                        val semBooks = remember(allBooks, department, semKey) {
                            booksForDeptAndSem(allBooks, department, semKey)
                        }
                        val selectedInSem = selectedBySemester[semKey] ?: emptySet()
                        val selectedInDeptSem = semBooks.count { it.subjectCode.trim() in selectedInSem }

                        KlassNavigationRow(
                            title = RoutineRules.semesterDisplayLabel(semKey),
                            subtitle = if (semBooks.isEmpty()) {
                                "No $department subjects listed in this semester"
                            } else {
                                "${semBooks.size} subjects available • $selectedInDeptSem selected here"
                            },
                            icon = BookIcon,
                            accent = dayAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            badgeText = "$selectedInDeptSem / ${semBooks.size}",
                            badgeColor = if (selectedInDeptSem > 0) dayAccent.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            testTag = "wizard_sem_${idx + 1}",
                            onClick = { onSelectSemester(semKey) },
                        )
                        if (idx < RoutineRules.ALL_SEMESTER_KEYS.lastIndex) {
                            SocialInsetDivider(startIndent = 72.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectChecklistStepScreen(
    department: String,
    semKey: String,
    allBooks: List<LibraryBook>,
    selectedCodes: Set<String>,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onToggleCode: (String) -> Unit,
    onSelectAll: (Set<String>) -> Unit,
    onClearDeptVisible: (Set<String>) -> Unit,
    onFinishAll: () -> Unit,
    onBackToSemesters: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val semIdx = RoutineRules.ALL_SEMESTER_KEYS.indexOf(semKey).coerceAtLeast(0)
    val dayAccent = dayPaletteAt(semIdx, isDark)
    val teachingAccent = KlassFeaturePalette.teaching(isDark)

    var searchQuery by remember(department, semKey) { mutableStateOf("") }
    var inspectingBook by remember { mutableStateOf<LibraryBook?>(null) }

    val baseSemBooks = remember(allBooks, department, semKey) {
        booksForDeptAndSem(allBooks, department, semKey)
    }
    val semBooks = remember(baseSemBooks, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) baseSemBooks
        else baseSemBooks.filter { book ->
            book.title.lowercase().contains(q) || book.subjectCode.lowercase().contains(q)
        }
    }

    val visibleCodes = remember(semBooks) { semBooks.map { it.subjectCode.trim() }.toSet() }
    val selectedVisibleCount = semBooks.count { it.subjectCode.trim() in selectedCodes }

    Box(modifier = Modifier.fillMaxSize()) {
        MinimalFullScreenSheet(
            title = "$department • ${RoutineRules.semesterShortLabel(semKey)}",
            subtitle = "$selectedVisibleCount of ${semBooks.size} selected",
            levelLabel = "Teaching Setup • 3 of 3",
            icon = BookIcon,
            accent = dayAccent.primary,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onDismiss = onBackToSemesters,
            headerActions = {
                if (semBooks.isNotEmpty()) {
                    KlassTonalButton(
                        text = "All",
                        icon = CheckIcon,
                        accent = dayAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 36.dp,
                        onClick = { onSelectAll(visibleCodes) },
                    )
                }
                if (selectedVisibleCount > 0) {
                    KlassTonalButton(
                        text = "Clear",
                        icon = CloseIcon,
                        accent = KlassFeaturePalette.error(isDark).primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        height = 36.dp,
                        onClick = { onClearDeptVisible(visibleCodes) },
                    )
                }
                HeaderSavePillButton(
                    text = "Done",
                    accent = teachingAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onClick = onFinishAll,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                MinimalSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search $department ${RoutineRules.semesterShortLabel(semKey)} subjects",
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
                        subtitle = "Go back to pick another semester or department.",
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

        KlassSubScreenHost(visible = inspectingBook != null, zIndex = 96f) {
            inspectingBook?.let { book ->
                val isSelected = book.subjectCode.trim() in selectedCodes
                MinimalFullScreenSheet(
                    title = book.title,
                    subtitle = "Subject Code ${book.subjectCode}",
                    levelLabel = "Subject Details",
                    icon = InfoCircleIcon,
                    accent = dayAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    onDismiss = { inspectingBook = null },
                    headerActions = {
                        HeaderSavePillButton(
                            text = if (isSelected) "Remove" else "Select",
                            accent = if (isSelected) KlassFeaturePalette.error(isDark).primary else dayAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
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
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GoogleSectionHeader(
                            title = "${book.theoryClassesPerWeek}T • ${book.practicalClassesPerWeek}P • ${book.credit} Cr",
                            accent = dayAccent.primary,
                            hindSiliguri = hindSiliguri,
                        )
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
