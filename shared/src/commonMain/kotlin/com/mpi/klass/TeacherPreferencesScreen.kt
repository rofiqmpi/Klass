package com.mpi.klass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ChevronDownIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.GraduationCapIcon
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
    onDismissOnboarding: (() -> Unit)? = null,
    onSaved: (TeacherSchedulingPref) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val cleanEmail = email.trim().lowercase()

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var teacherName by remember(cleanEmail) { mutableStateOf(userName) }
    var selectedDept by remember(cleanEmail) { mutableStateOf("CST") }
    var selectedShift by remember(cleanEmail) { mutableStateOf(RoutineRules.SHIFT_1) }
    var selectedTimePref by remember(cleanEmail) { mutableStateOf(TeacherTimePreference.ANY) }

    var allBooks by remember { mutableStateOf<List<LibraryBook>>(emptyList()) }
    var bookDeptFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val selectedBySemester = remember(cleanEmail) {
        mutableStateMapOf<String, Set<String>>()
    }
    val expandedSemesters = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put(RoutineRules.ALL_SEMESTER_KEYS.first(), true)
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
            bookDeptFilter = if (selectedDept in RoutineRules.TECH_DEPARTMENTS) selectedDept else "ALL"
        } finally {
            isLoading = false
        }
    }

    val totalSelectedCount = selectedBySemester.values.sumOf { it.size }

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

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary, strokeWidth = 2.5.dp)
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 920.dp)
                    .padding(horizontal = 16.dp),
            ) {
                // Slim 1-line summary bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$totalSelectedCount subject(s) selected • Sem 1–7",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = hindSiliguri,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    if (isOnboardingPrompt && onDismissOnboarding != null) {
                        TextButton(
                            onClick = onDismissOnboarding,
                            modifier = Modifier.testTag("skip_teacher_setup_button"),
                        ) {
                            Text(
                                text = "Skip",
                                color = cs.primary,
                                fontWeight = FontWeight.Medium,
                                fontFamily = hindSiliguri,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    // Responsive 2-col on wide screens for Dept/Shift & Preferred Hours
                    item {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val twoCol = maxWidth >= 640.dp

                            val deptShiftCard: @Composable (Modifier) -> Unit = { mod ->
                                MinimalCardSurface(
                                    isDark = isDark,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = mod,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Text(
                                            text = "Department & Shift",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                        )

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            RoutineRules.ALL_TEACHER_DEPARTMENTS.forEach { dept ->
                                                val isSelected = selectedDept.equals(dept, ignoreCase = true)
                                                MinimalIconChip(
                                                    label = dept,
                                                    selected = isSelected,
                                                    accent = cs.primary,
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
                                                    accent = cs.primary,
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
                                MinimalCardSurface(
                                    isDark = isDark,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = mod,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = "Preferred Class Hours",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                        )

                                        TeacherTimePreference.entries.forEach { pref ->
                                            val isSelected = selectedTimePref == pref
                                            Surface(
                                                onClick = { selectedTimePref = pref },
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isSelected) cs.secondaryContainer else cs.surfaceContainer,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("teacher_time_${pref.key}"),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = pref.label,
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            fontFamily = hindSiliguri,
                                                            color = if (isSelected) cs.onSecondaryContainer else cs.onSurface,
                                                        )
                                                        Text(
                                                            text = pref.subtitle,
                                                            fontSize = 11.5.sp,
                                                            fontFamily = hindSiliguri,
                                                            color = if (isSelected) {
                                                                cs.onSecondaryContainer.copy(alpha = 0.8f)
                                                            } else {
                                                                cs.onSurfaceVariant
                                                            },
                                                        )
                                                    }
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = null,
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = cs.primary,
                                                            unselectedColor = cs.onSurfaceVariant,
                                                        ),
                                                    )
                                                }
                                            }
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
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    deptShiftCard(Modifier.fillMaxWidth())
                                    timePrefCard(Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }

                    // Subject Filter Controls Card
                    item {
                        MinimalCardSurface(isDark = isDark, shape = RoundedCornerShape(14.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "Subjects by Semester (1–7)",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurface,
                                )

                                val filterDepts = remember { listOf("ALL") + RoutineRules.TECH_DEPARTMENTS }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(filterDepts) { dept ->
                                        MinimalIconChip(
                                            label = if (dept == "ALL") "All Depts" else dept,
                                            selected = bookDeptFilter == dept,
                                            accent = cs.primary,
                                            isDark = isDark,
                                            hindSiliguri = hindSiliguri,
                                            onClick = { bookDeptFilter = dept },
                                        )
                                    }
                                }

                                MinimalSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    placeholder = "Filter subjects by code or title",
                                    accent = cs.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                )
                            }
                        }
                    }

                    // Semester 1-7 Accordions
                    items(RoutineRules.ALL_SEMESTER_KEYS) { semKey ->
                        val isExpanded = expandedSemesters[semKey] == true
                        val selectedCodes = selectedBySemester[semKey] ?: emptySet()
                        val q = searchQuery.trim().lowercase()

                        val semBooks = remember(allBooks, bookDeptFilter, q, semKey) {
                            allBooks.filter { book ->
                                val matchesSemAndDept = book.departmentSemesters.any { (dept, sems) ->
                                    val deptOk = bookDeptFilter == "ALL" ||
                                        RoutineRules.normalizeDepartmentShort(dept) == bookDeptFilter
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

                        val chevronRotation by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "SemChevron_$semKey",
                        )

                        MinimalCardSurface(
                            isDark = isDark,
                            shape = RoundedCornerShape(18.dp),
                            borderColor = if (selectedCodes.isNotEmpty()) cs.primary.copy(alpha = 0.45f) else null,
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedSemesters[semKey] = !isExpanded }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = RoutineRules.semesterDisplayLabel(semKey),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = hindSiliguri,
                                            color = cs.onSurface,
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = if (selectedCodes.isNotEmpty()) {
                                                cs.primaryContainer
                                            } else {
                                                cs.surfaceContainerHigh
                                            },
                                        ) {
                                            Text(
                                                text = "${selectedCodes.size} / ${semBooks.size}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = hindSiliguri,
                                                color = if (selectedCodes.isNotEmpty()) cs.onPrimaryContainer else cs.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = ChevronDownIcon,
                                        contentDescription = "Toggle semester",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .graphicsLayer(rotationZ = chevronRotation),
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                                    ) {
                                        if (semBooks.isEmpty()) {
                                            Text(
                                                text = "No matching subjects in this semester.",
                                                fontSize = 12.5.sp,
                                                fontFamily = hindSiliguri,
                                                color = cs.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                            )
                                        } else {
                                            MinimalResponsiveGrid(
                                                items = semBooks,
                                                twoColMinWidth = 560.dp,
                                                spacing = 8.dp,
                                            ) { _, book ->
                                                val code = book.subjectCode.trim()
                                                val checked = code in selectedCodes
                                                Surface(
                                                    onClick = {
                                                        val current = selectedBySemester[semKey] ?: emptySet()
                                                        selectedBySemester[semKey] = if (checked) {
                                                            current - code
                                                        } else {
                                                            current + code
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = if (checked) cs.secondaryContainer else cs.surfaceContainer,
                                                    border = if (checked) {
                                                        BorderStroke(1.dp, cs.primary.copy(alpha = 0.45f))
                                                    } else {
                                                        null
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Checkbox(
                                                            checked = checked,
                                                            onCheckedChange = null,
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = cs.primary,
                                                                uncheckedColor = cs.onSurfaceVariant,
                                                            ),
                                                        )

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = book.title,
                                                                fontSize = 13.5.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                fontFamily = hindSiliguri,
                                                                color = if (checked) cs.onSecondaryContainer else cs.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                            Text(
                                                                text = "${book.subjectCode} • ${book.theoryClassesPerWeek}T • ${book.practicalClassesPerWeek}P",
                                                                fontSize = 11.5.sp,
                                                                fontFamily = hindSiliguri,
                                                                color = if (checked) {
                                                                    cs.onSecondaryContainer.copy(alpha = 0.8f)
                                                                } else {
                                                                    cs.onSurfaceVariant
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
                }
            }

            // Clean M3 Pinned Bottom Bar
            Surface(
                color = cs.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = cs.outlineVariant.copy(alpha = 0.5f),
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
                            height = 46.dp,
                            cornerRadius = 23.dp,
                            testTag = "save_teacher_preferences_button",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { savePrefs() },
                        )
                    }
                }
            }
        }
    }
}
