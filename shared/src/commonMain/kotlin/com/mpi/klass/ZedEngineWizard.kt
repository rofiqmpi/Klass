package com.mpi.klass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.BookIcon
import com.mpi.klass.theme.CalendarDaysIcon
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.ClockIcon
import com.mpi.klass.theme.DoorOpenIcon
import com.mpi.klass.theme.GraduationCapIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LayerGroupIcon
import com.mpi.klass.theme.PersonIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.RightArrowIcon
import com.mpi.klass.theme.SearchIcon
import com.mpi.klass.theme.SparkleIcon
import com.mpi.klass.theme.UsersGroupIcon
import com.mpi.klass.theme.WarningIcon
import com.mpi.klass.theme.dayPaletteAt
import com.mpi.klass.theme.departmentAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal val ZedBowArchShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, h)
    lineTo(w, h)
    quadraticTo(
        x1 = w / 2f,
        y1 = -h * 0.85f,
        x2 = 0f,
        y2 = h,
    )
    close()
}

internal data class ZedBubbleSpec(
    val id: String,
    val label: String,
    val category: String,
    val accent: Color,
    val fromLeftCorner: Boolean,
)

@Composable
internal fun ZedEngineWizardHost(
    wizardStep: Int,
    onWizardStepChange: (Int) -> Unit,
    books: List<LibraryBook>,
    activeBookIds: Set<String>,
    onActiveBookIdsChange: (Set<String>) -> Unit,
    teachers: List<TeacherSchedulingPref>,
    activeTeacherEmails: Set<String>,
    onActiveTeacherEmailsChange: (Set<String>) -> Unit,
    classrooms: List<ClassroomItem>,
    activeClassroomIds: Set<String>,
    onActiveClassroomIdsChange: (Set<String>) -> Unit,
    shift1Start: String,
    onShift1Change: (String) -> Unit,
    shift2Start: String,
    onShift2Change: (String) -> Unit,
    periodDuration: Int,
    onDurationChange: (Int) -> Unit,
    periodsPerDay: Int,
    onPeriodsPerDayChange: (Int) -> Unit,
    practicalBlockMode: String,
    onPracticalBlockModeChange: (String) -> Unit,
    activeSemesters: Set<String>,
    onToggleSemester: (String) -> Unit,
    onActivateAllSemesters: () -> Unit,
    currentPlan: RoutinePlan?,
    isProcessing: Boolean,
    isLoading: Boolean,
    isSeedingDemo: Boolean = false,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onRefreshData: () -> Unit,
    onSeedDemoCampus: (() -> Unit)? = null,
    onHoldCompleteGenerate: () -> Unit,
    onPublishPlan: (RoutinePlan) -> Unit,
    onOpenExplorer: () -> Unit,
    onOpenConflicts: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val genAccent = KlassFeaturePalette.routine(isDark)
    val bookAccent = KlassFeaturePalette.books(isDark)
    val teacherAccent = KlassFeaturePalette.teaching(isDark)
    val roomAccent = KlassFeaturePalette.classrooms(isDark)
    val publishAccent = KlassFeaturePalette.success(isDark)

    AppBackHandler(enabled = wizardStep > 0) {
        onWizardStepChange((wizardStep - 1).coerceAtLeast(0))
    }

    if (wizardStep < 5) {
        val stepMeta = when (wizardStep) {
            0 -> Triple("Step 1 of 5 • Active Books", "${activeBookIds.size}/${books.size} selected", bookAccent.primary)
            1 -> Triple("Step 2 of 5 • Active Teachers", "${activeTeacherEmails.size}/${teachers.size} selected", teacherAccent.primary)
            2 -> Triple("Step 3 of 5 • Active Classrooms", "${activeClassroomIds.size}/${classrooms.size} selected", roomAccent.primary)
            3 -> Triple("Step 4 of 5 • Schedule Settings", "$periodsPerDay periods/day • Practical 2–3p", genAccent.primary)
            else -> Triple("Step 5 of 5 • Active Semesters", "${activeSemesters.size}/7 semesters", publishAccent.primary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            // Compact Stepper Header
            Surface(
                color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.80f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stepMeta.first,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = cs.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stepMeta.second,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = hindSiliguri,
                                color = stepMeta.third,
                                maxLines = 1,
                            )
                        }
                        KlassHeaderIconButton(
                            icon = RefreshIcon,
                            contentDescription = "Refresh data",
                            isDark = isDark,
                            containerColor = stepMeta.third.copy(alpha = if (isDark) 0.18f else 0.10f),
                            iconTint = stepMeta.third,
                            borderColor = stepMeta.third.copy(alpha = 0.35f),
                            isLoading = isLoading,
                            enabled = !isLoading,
                            onClick = onRefreshData,
                        )
                    }

                    // 5-Step Progress Dots Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(5) { idx ->
                            val reached = idx <= wizardStep
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (reached) stepMeta.third
                                        else cs.outlineVariant.copy(alpha = 0.45f)
                                    )
                                    .clickable { onWizardStepChange(idx) },
                            )
                        }
                    }
                }
            }

            // Sub-screen Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                AnimatedContent(
                    targetState = wizardStep,
                    label = "ZedWizardStepTransition",
                ) { step ->
                    when (step) {
                        0 -> WizardBooksStep(
                            books = books,
                            activeBookIds = activeBookIds,
                            onActiveBookIdsChange = onActiveBookIdsChange,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                        1 -> WizardTeachersStep(
                            teachers = teachers,
                            activeTeacherEmails = activeTeacherEmails,
                            onActiveTeacherEmailsChange = onActiveTeacherEmailsChange,
                            isSeedingDemo = isSeedingDemo,
                            onSeedDemoCampus = onSeedDemoCampus,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                        2 -> WizardClassroomsStep(
                            classrooms = classrooms,
                            activeClassroomIds = activeClassroomIds,
                            onActiveClassroomIdsChange = onActiveClassroomIdsChange,
                            isSeedingDemo = isSeedingDemo,
                            onSeedDemoCampus = onSeedDemoCampus,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                        3 -> WizardScheduleSettingsStep(
                            shift1Start = shift1Start,
                            shift2Start = shift2Start,
                            periodDuration = periodDuration,
                            periodsPerDay = periodsPerDay,
                            practicalBlockMode = practicalBlockMode,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onShift1Change = onShift1Change,
                            onShift2Change = onShift2Change,
                            onDurationChange = onDurationChange,
                            onPeriodsPerDayChange = onPeriodsPerDayChange,
                            onPracticalBlockModeChange = onPracticalBlockModeChange,
                        )
                        else -> WizardSemestersStep(
                            activeSemesters = activeSemesters,
                            onToggleSemester = onToggleSemester,
                            onActivateAllSemesters = onActivateAllSemesters,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                        )
                    }
                }
            }

            // Bottom Wizard Navigation Bar (Back / Next)
            Surface(
                color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f),
                border = BorderStroke(0.5.dp, cs.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (wizardStep > 0) {
                        KlassTonalButton(
                            text = "Back",
                            icon = BackArrowIcon,
                            accent = cs.onSurfaceVariant,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            height = 46.dp,
                            modifier = Modifier.testTag("wizard_back_button"),
                            onClick = { onWizardStepChange(wizardStep - 1) },
                        )
                    }
                    KlassPrimaryButton(
                        text = if (wizardStep == 4) "Continue to Generate" else "Next Step",
                        icon = RightArrowIcon,
                        hindSiliguri = hindSiliguri,
                        isDark = isDark,
                        customPrimaryColor = stepMeta.third,
                        height = 46.dp,
                        cornerRadius = 16.dp,
                        modifier = Modifier.weight(1f),
                        testTag = "wizard_next_button",
                        onClick = {
                            when {
                                wizardStep == 0 && activeBookIds.isEmpty() ->
                                    showToast("Select at least one active book.")
                                wizardStep == 1 && activeTeacherEmails.isEmpty() ->
                                    showToast("Select at least one active teacher.")
                                wizardStep == 2 && activeClassroomIds.isEmpty() ->
                                    showToast("Select at least one active classroom.")
                                wizardStep == 4 && activeSemesters.isEmpty() ->
                                    showToast("Select at least one active semester.")
                                else -> onWizardStepChange(wizardStep + 1)
                            }
                        },
                    )
                }
            }
        }
    } else {
        // Screen 6: Clean Display with ONLY one bottom DeepBowArchShape Generate button
        ZedHoldGenerateDisplay(
            books = books,
            activeBookIds = activeBookIds,
            teachers = teachers,
            activeTeacherEmails = activeTeacherEmails,
            classrooms = classrooms,
            activeClassroomIds = activeClassroomIds,
            activeSemesters = activeSemesters,
            periodsPerDay = periodsPerDay,
            practicalBlockMode = practicalBlockMode,
            currentPlan = currentPlan,
            isProcessing = isProcessing,
            isDark = isDark,
            hindSiliguri = hindSiliguri,
            onBackToSetup = { onWizardStepChange(4) },
            onHoldCompleteGenerate = onHoldCompleteGenerate,
            onPublishPlan = onPublishPlan,
            onOpenExplorer = onOpenExplorer,
            onOpenConflicts = onOpenConflicts,
        )
    }
}

@Composable
private fun WizardBooksStep(
    books: List<LibraryBook>,
    activeBookIds: Set<String>,
    onActiveBookIdsChange: (Set<String>) -> Unit,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    val bookAccent = KlassFeaturePalette.books(isDark)
    val successAccent = KlassFeaturePalette.success(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)

    var searchQuery by remember { mutableStateOf("") }
    var deptFilter by remember { mutableStateOf("ALL") }

    val filteredBooks = remember(books, searchQuery, deptFilter) {
        val q = searchQuery.trim().lowercase()
        books.filter { book ->
            val deptOk = deptFilter == "ALL" ||
                book.departmentSemesters.keys.any { RoutineRules.normalizeDepartmentShort(it) == deptFilter }
            val queryOk = q.isBlank() ||
                book.title.lowercase().contains(q) ||
                book.subjectCode.lowercase().contains(q)
            deptOk && queryOk
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search + Activate All / Deactivate All Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = SearchIcon,
                        contentDescription = null,
                        tint = bookAccent.primary,
                        modifier = Modifier.size(18.dp),
                    )
                },
                placeholder = {
                    Text(
                        text = "Search books by title or code",
                        fontFamily = hindSiliguri,
                        fontSize = 14.sp,
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = bookAccent.primary,
                    unfocusedBorderColor = cs.outlineVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wizard_books_search"),
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val depts = listOf("ALL") + RoutineRules.TECH_DEPARTMENTS
                itemsIndexed(depts) { _, dept ->
                    val chipAccent = if (dept == "ALL") bookAccent.primary else departmentAccent(dept, isDark).primary
                    MinimalIconChip(
                        label = if (dept == "ALL") "All Depts" else dept,
                        selected = deptFilter == dept,
                        accent = chipAccent,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        onClick = { deptFilter = dept },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KlassTonalButton(
                    text = "Activate All",
                    icon = CheckIcon,
                    accent = successAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 42.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wizard_books_activate_all"),
                    onClick = {
                        onActiveBookIdsChange(books.map { it.id }.toSet())
                    },
                )
                KlassTonalButton(
                    text = "Deactivate All",
                    accent = errorAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 42.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wizard_books_deactivate_all"),
                    onClick = {
                        onActiveBookIdsChange(emptySet())
                    },
                )
            }
        }

        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                KlassEmptyState(
                    icon = BookIcon,
                    title = "No Books Found",
                    subtitle = "Adjust the search query or department filter.",
                    accent = bookAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
            ) {
                item {
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        filteredBooks.forEachIndexed { idx, book ->
                            val isActive = book.id in activeBookIds
                            val firstDept = book.departmentSemesters.keys.firstOrNull()
                                ?.let { RoutineRules.normalizeDepartmentShort(it) } ?: "CST"
                            val accent = departmentAccent(firstDept, isDark)
                            val deptsSummary = book.departmentSemesters.keys
                                .map { RoutineRules.normalizeDepartmentShort(it) }
                                .distinct()
                                .joinToString(", ")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onActiveBookIdsChange(
                                            if (isActive) activeBookIds - book.id else activeBookIds + book.id
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accent.softTint,
                                    border = BorderStroke(0.75.dp, accent.borderTint),
                                ) {
                                    Text(
                                        text = book.subjectCode,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = hindSiliguri,
                                        color = accent.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = book.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "$deptsSummary • ${book.theoryClassesPerWeek}T + ${book.practicalClassesPerWeek}P",
                                        fontSize = 12.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { checked ->
                                        onActiveBookIdsChange(
                                            if (checked) activeBookIds + book.id else activeBookIds - book.id
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = bookAccent.primary,
                                    ),
                                    modifier = Modifier.testTag("wizard_book_switch_${book.subjectCode}"),
                                )
                            }
                            if (idx < filteredBooks.lastIndex) {
                                SocialInsetDivider(startIndent = 16.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardTeachersStep(
    teachers: List<TeacherSchedulingPref>,
    activeTeacherEmails: Set<String>,
    onActiveTeacherEmailsChange: (Set<String>) -> Unit,
    isSeedingDemo: Boolean = false,
    onSeedDemoCampus: (() -> Unit)? = null,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    val teacherAccent = KlassFeaturePalette.teaching(isDark)
    val successAccent = KlassFeaturePalette.success(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)
    var searchQuery by remember { mutableStateOf("") }

    val filteredTeachers = remember(teachers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) teachers
        else teachers.filter {
            it.name.lowercase().contains(q) ||
                it.department.lowercase().contains(q) ||
                it.shift.lowercase().contains(q)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = SearchIcon,
                        contentDescription = null,
                        tint = teacherAccent.primary,
                        modifier = Modifier.size(18.dp),
                    )
                },
                placeholder = {
                    Text("Search teachers or department", fontFamily = hindSiliguri, fontSize = 14.sp)
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = teacherAccent.primary,
                    unfocusedBorderColor = cs.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KlassTonalButton(
                    text = "Activate All",
                    icon = CheckIcon,
                    accent = successAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 40.dp,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onActiveTeacherEmailsChange(teachers.map { it.email.trim().lowercase() }.toSet())
                    },
                )
                KlassTonalButton(
                    text = "Deactivate All",
                    accent = errorAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 40.dp,
                    modifier = Modifier.weight(1f),
                    onClick = { onActiveTeacherEmailsChange(emptySet()) },
                )
            }
        }

        if (filteredTeachers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                KlassEmptyState(
                    icon = PersonIcon,
                    title = "No Teachers Available",
                    subtitle = if (onSeedDemoCampus != null && teachers.isEmpty()) {
                        "No teachers have configured subjects yet. Load realistic MPI campus teachers & labs to test immediately."
                    } else {
                        "No teachers matched your search filter."
                    },
                    accent = teacherAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    actionLabel = if (onSeedDemoCampus != null && teachers.isEmpty()) {
                        if (isSeedingDemo) "Loading Demo Campus..." else "Load Demo Campus"
                    } else null,
                    onActionClick = if (onSeedDemoCampus != null && teachers.isEmpty() && !isSeedingDemo) {
                        onSeedDemoCampus
                    } else null,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
            ) {
                item {
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        filteredTeachers.forEachIndexed { idx, teacher ->
                            val cleanEmail = teacher.email.trim().lowercase()
                            val isActive = cleanEmail in activeTeacherEmails
                            val dept = RoutineRules.normalizeDepartmentShort(teacher.department.ifBlank { "CST" })
                            val deptColor = departmentAccent(dept, isDark)
                            val subjectCount = teacher.semesterSubjects.values.sumOf { it.size }
                            val displayName = teacher.name.ifBlank { cleanEmail.substringBefore("@") }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onActiveTeacherEmailsChange(
                                            if (isActive) activeTeacherEmails - cleanEmail
                                            else activeTeacherEmails + cleanEmail
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = deptColor.softTint,
                                    border = BorderStroke(0.75.dp, deptColor.borderTint),
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = PersonIcon,
                                            contentDescription = null,
                                            tint = deptColor.primary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "$dept • ${teacher.shift.ifBlank { "All Shifts" }} • $subjectCount subjects",
                                        fontSize = 12.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { checked ->
                                        onActiveTeacherEmailsChange(
                                            if (checked) activeTeacherEmails + cleanEmail
                                            else activeTeacherEmails - cleanEmail
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = teacherAccent.primary,
                                    ),
                                    modifier = Modifier.testTag("wizard_teacher_switch_$idx"),
                                )
                            }
                            if (idx < filteredTeachers.lastIndex) {
                                SocialInsetDivider(startIndent = 68.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardClassroomsStep(
    classrooms: List<ClassroomItem>,
    activeClassroomIds: Set<String>,
    onActiveClassroomIdsChange: (Set<String>) -> Unit,
    isSeedingDemo: Boolean = false,
    onSeedDemoCampus: (() -> Unit)? = null,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    val roomAccent = KlassFeaturePalette.classrooms(isDark)
    val labAccent = KlassFeaturePalette.teaching(isDark)
    val successAccent = KlassFeaturePalette.success(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KlassTonalButton(
                text = "Activate All",
                icon = CheckIcon,
                accent = successAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                height = 40.dp,
                modifier = Modifier.weight(1f),
                onClick = { onActiveClassroomIdsChange(classrooms.map { it.id }.toSet()) },
            )
            KlassTonalButton(
                text = "Deactivate All",
                accent = errorAccent.primary,
                isDark = isDark,
                hindSiliguri = hindSiliguri,
                height = 40.dp,
                modifier = Modifier.weight(1f),
                onClick = { onActiveClassroomIdsChange(emptySet()) },
            )
        }

        if (classrooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                KlassEmptyState(
                    icon = DoorOpenIcon,
                    title = "No Classrooms Added",
                    subtitle = if (onSeedDemoCampus != null) {
                        "Add rooms in Classrooms or load realistic MPI Theory rooms, Physics/Chemistry Labs & Teachers."
                    } else {
                        "Add Theory rooms and Lab rooms from Classrooms first."
                    },
                    accent = roomAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    actionLabel = if (onSeedDemoCampus != null) {
                        if (isSeedingDemo) "Loading Demo Campus..." else "Load Demo Campus"
                    } else null,
                    onActionClick = if (onSeedDemoCampus != null && !isSeedingDemo) onSeedDemoCampus else null,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
            ) {
                item {
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        classrooms.forEachIndexed { idx, room ->
                            val isActive = room.id in activeClassroomIds
                            val isLab = room.roomType == ClassroomType.LAB
                            val rAccent = if (isLab) labAccent else roomAccent
                            val nearList = room.effectiveNearestDepartments()
                            val nearTag = if (nearList.isNotEmpty()) "Near ${nearList.joinToString(", ")}" else "All Depts"
                            val typeTag = if (isLab) room.effectiveLabSpecialty().label else room.roomType.label

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onActiveClassroomIdsChange(
                                            if (isActive) activeClassroomIds - room.id
                                            else activeClassroomIds + room.id
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = rAccent.softTint,
                                    border = BorderStroke(0.75.dp, rAccent.borderTint),
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = DoorOpenIcon,
                                            contentDescription = null,
                                            tint = rAccent.primary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Room ${room.roomNumber}",
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurface,
                                    )
                                    Text(
                                        text = "$typeTag • $nearTag",
                                        fontSize = 12.5.sp,
                                        fontFamily = hindSiliguri,
                                        color = cs.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { checked ->
                                        onActiveClassroomIdsChange(
                                            if (checked) activeClassroomIds + room.id
                                            else activeClassroomIds - room.id
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = rAccent.primary,
                                    ),
                                    modifier = Modifier.testTag("wizard_room_switch_${room.roomNumber}"),
                                )
                            }
                            if (idx < classrooms.lastIndex) {
                                SocialInsetDivider(startIndent = 68.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WizardScheduleSettingsStep(
    shift1Start: String,
    shift2Start: String,
    periodDuration: Int,
    periodsPerDay: Int,
    practicalBlockMode: String,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onShift1Change: (String) -> Unit,
    onShift2Change: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onPeriodsPerDayChange: (Int) -> Unit,
    onPracticalBlockModeChange: (String) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val genAccent = KlassFeaturePalette.routine(isDark)
    val bookAccent = KlassFeaturePalette.books(isDark)
    val teacherAccent = KlassFeaturePalette.teaching(isDark)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp),
    ) {
        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
            GoogleSectionHeader(
                title = "Shift Timings & Daily Periods",
                hindSiliguri = hindSiliguri,
                accent = genAccent.primary,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = shift1Start,
                        onValueChange = onShift1Change,
                        singleLine = true,
                        label = { Text("1st Shift (24h)", fontFamily = hindSiliguri) },
                        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = hindSiliguri),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = shift2Start,
                        onValueChange = onShift2Change,
                        singleLine = true,
                        label = { Text("2nd Shift (24h)", fontFamily = hindSiliguri) },
                        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = hindSiliguri),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    WizardStepperCard(
                        label = "Duration",
                        valueText = "$periodDuration min",
                        accent = genAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        onMinus = { if (periodDuration > 30) onDurationChange(periodDuration - 5) },
                        onPlus = { if (periodDuration < 75) onDurationChange(periodDuration + 5) },
                        modifier = Modifier.weight(1f),
                    )
                    WizardStepperCard(
                        label = "Periods / Day",
                        valueText = "$periodsPerDay",
                        accent = bookAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                        onMinus = { if (periodsPerDay > 4) onPeriodsPerDayChange(periodsPerDay - 1) },
                        onPlus = { if (periodsPerDay < 10) onPeriodsPerDayChange(periodsPerDay + 1) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        SocialSectionDividerBand(isDark = isDark)

        SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
            GoogleSectionHeader(
                title = "Practical / Lab Period Allocation",
                hindSiliguri = hindSiliguri,
                accent = teacherAccent.primary,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Each day has $periodsPerDay slots, not necessarily $periodsPerDay separate subjects. Practical labs automatically span 2 or 3 consecutive periods.",
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    color = cs.onSurfaceVariant,
                    lineHeight = 19.sp,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "AUTO_2_3" to "Auto (2–3 Periods)",
                        "TWO_PERIODS" to "2 Periods / Lab",
                        "THREE_PERIODS" to "3 Periods / Lab",
                    ).forEach { (key, label) ->
                        MinimalIconChip(
                            label = label,
                            selected = practicalBlockMode == key,
                            accent = teacherAccent.primary,
                            isDark = isDark,
                            hindSiliguri = hindSiliguri,
                            onClick = { onPracticalBlockModeChange(key) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStepperCard(
    label: String,
    valueText: String,
    accent: Color,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = if (isDark) 0.14f else 0.08f),
        border = BorderStroke(0.75.dp, accent.copy(alpha = 0.28f)),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = hindSiliguri,
                color = cs.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    onClick = onMinus,
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.26f else 0.16f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accent)
                    }
                }
                Text(
                    text = valueText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = cs.onSurface,
                )
                Surface(
                    onClick = onPlus,
                    shape = CircleShape,
                    color = accent.copy(alpha = if (isDark) 0.26f else 0.16f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardSemestersStep(
    activeSemesters: Set<String>,
    onToggleSemester: (String) -> Unit,
    onActivateAllSemesters: () -> Unit,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val cs = MaterialTheme.colorScheme
    val semAccent = KlassFeaturePalette.routine(isDark)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Select semesters for routine generation",
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    color = cs.onSurfaceVariant,
                )
                KlassTonalButton(
                    text = "All 7",
                    icon = CheckIcon,
                    accent = semAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    height = 34.dp,
                    onClick = onActivateAllSemesters,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                RoutineRules.ALL_SEMESTER_KEYS.forEachIndexed { idx, sem ->
                    val checked = sem in activeSemesters
                    val accent = dayPaletteAt(idx, isDark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSemester(sem) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accent.softTint,
                            border = BorderStroke(0.75.dp, accent.borderTint),
                            modifier = Modifier.size(38.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = hindSiliguri,
                                    color = accent.primary,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = RoutineRules.semesterDisplayLabel(sem),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = hindSiliguri,
                                color = cs.onSurface,
                            )
                            Text(
                                text = if (checked) "Included in Zed engine run" else "Excluded",
                                fontSize = 12.5.sp,
                                fontFamily = hindSiliguri,
                                color = cs.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = checked,
                            onCheckedChange = { onToggleSemester(sem) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accent.primary,
                            ),
                            modifier = Modifier.testTag("wizard_sem_switch_${idx + 1}"),
                        )
                    }
                    if (idx < RoutineRules.ALL_SEMESTER_KEYS.lastIndex) {
                        SocialInsetDivider(startIndent = 66.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZedHoldGenerateDisplay(
    books: List<LibraryBook>,
    activeBookIds: Set<String>,
    teachers: List<TeacherSchedulingPref>,
    activeTeacherEmails: Set<String>,
    classrooms: List<ClassroomItem>,
    activeClassroomIds: Set<String>,
    activeSemesters: Set<String>,
    periodsPerDay: Int,
    practicalBlockMode: String,
    currentPlan: RoutinePlan?,
    isProcessing: Boolean,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onBackToSetup: () -> Unit,
    onHoldCompleteGenerate: () -> Unit,
    onPublishPlan: (RoutinePlan) -> Unit,
    onOpenExplorer: () -> Unit,
    onOpenConflicts: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val genAccent = KlassFeaturePalette.routine(isDark)
    val publishAccent = KlassFeaturePalette.success(isDark)
    val conflictAccent = KlassFeaturePalette.error(isDark)

    // Strictly build floating bubbles ONLY from selected active semesters, departments, groups & classrooms
    val activeBubbles = remember(
        books,
        activeBookIds,
        classrooms,
        activeClassroomIds,
        activeSemesters,
        isDark,
    ) {
        val list = mutableListOf<ZedBubbleSpec>()
        var sideToggle = true

        val selectedSems = RoutineRules.ALL_SEMESTER_KEYS.filter { it in activeSemesters }
        selectedSems.forEachIndexed { idx, sem ->
            list.add(
                ZedBubbleSpec(
                    id = "sem_$sem",
                    label = RoutineRules.semesterShortLabel(sem),
                    category = "SEM",
                    accent = dayPaletteAt(idx, isDark).primary,
                    fromLeftCorner = sideToggle,
                )
            )
            sideToggle = !sideToggle
        }

        val activeDepts = RoutineRules.TECH_DEPARTMENTS.filter { dept ->
            books.any { b ->
                b.id in activeBookIds &&
                    b.departmentSemesters.any { (bDept, bSems) ->
                        RoutineRules.normalizeDepartmentShort(bDept) == dept &&
                            bSems.any { RoutineRules.canonicalSemesterKey(it) in activeSemesters }
                    }
            }
        }

        activeDepts.forEach { dept ->
            val dColor = departmentAccent(dept, isDark).primary
            list.add(
                ZedBubbleSpec(
                    id = "dept_$dept",
                    label = dept,
                    category = "DEPT",
                    accent = dColor,
                    fromLeftCorner = sideToggle,
                )
            )
            sideToggle = !sideToggle

            RoutineRules.groupsForDepartment(dept).forEach { grp ->
                val shortGrp = grp.removePrefix("Group ").trim()
                list.add(
                    ZedBubbleSpec(
                        id = "grp_${dept}_$shortGrp",
                        label = "$dept-$shortGrp",
                        category = "GRP",
                        accent = dColor,
                        fromLeftCorner = sideToggle,
                    )
                )
                sideToggle = !sideToggle
            }
        }

        classrooms.filter { it.id in activeClassroomIds }.forEach { room ->
            val rColor = if (room.roomType == ClassroomType.LAB) {
                KlassFeaturePalette.teaching(isDark).primary
            } else {
                KlassFeaturePalette.classrooms(isDark).primary
            }
            list.add(
                ZedBubbleSpec(
                    id = "room_${room.id}",
                    label = "R-${room.roomNumber}",
                    category = "ROOM",
                    accent = rColor,
                    fromLeftCorner = sideToggle,
                )
            )
            sideToggle = !sideToggle
        }

        // Representative active book bubbles strictly from activeBookIds & activeSemesters
        val bookAccentColor = KlassFeaturePalette.books(isDark).primary
        books.asSequence()
            .filter { b ->
                b.id in activeBookIds &&
                    b.departmentSemesters.values.any { sems ->
                        sems.any { RoutineRules.canonicalSemesterKey(it) in activeSemesters }
                    }
            }
            .take(12)
            .forEach { b ->
                list.add(
                    ZedBubbleSpec(
                        id = "book_${b.id}",
                        label = b.subjectCode,
                        category = "BOOK",
                        accent = bookAccentColor,
                        fromLeftCorner = sideToggle,
                    )
                )
                sideToggle = !sideToggle
            }
        list
    }

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var streamClock by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHolding, isProcessing) {
        if (isHolding) {
            var lastNanos = 0L
            while (isHolding) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dtSec = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        streamClock += dtSec
                        if (holdProgress < 1f) {
                            holdProgress = (holdProgress + dtSec / 2.2f).coerceAtMost(1f)
                        }
                    }
                    lastNanos = frameNanos
                }
            }
        } else if (isProcessing) {
            holdProgress = 1f
            var lastNanos = 0L
            while (isProcessing) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dtSec = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        streamClock += dtSec
                    }
                    lastNanos = frameNanos
                }
            }
            holdProgress = 0f
        } else {
            var lastNanos = 0L
            while (!isHolding && holdProgress > 0f) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dtSec = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        holdProgress = (holdProgress - dtSec * 1.8f).coerceAtLeast(0f)
                    }
                    lastNanos = frameNanos
                }
            }
        }
    }

    val activeEffect = isHolding || isProcessing || holdProgress > 0.01f
    val buttonSurfaceColor = if (activeEffect) {
        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.30f) else Color(0xFF2563EB).copy(alpha = 0.22f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.50f)
    }
    val buttonContentColor = if (activeEffect) {
        if (isDark) Color(0xFF38BDF8) else Color(0xFF1D4ED8)
    } else {
        if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val stageW = with(density) { maxWidth.toPx() }
        val stageH = with(density) { maxHeight.toPx() }
        val bowButtonHeightPx = with(density) { 64.dp.toPx() }

        // Top-left back button to step 5
        Surface(
            onClick = onBackToSetup,
            shape = CircleShape,
            color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
            border = BorderStroke(0.75.dp, cs.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .testTag("wizard_back_from_generate"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = BackArrowIcon,
                    contentDescription = "Back",
                    tint = cs.onSurface,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Setup",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = hindSiliguri,
                    color = cs.onSurface,
                )
            }
        }

        // Center Status / Post-Generation Result Card (hidden while holding so bubbles shine)
        AnimatedVisibility(
            visible = !isHolding && !isProcessing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 72.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = genAccent.softTint,
                    border = BorderStroke(1.dp, genAccent.borderTint),
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = SparkleIcon,
                            contentDescription = null,
                            tint = genAccent.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Text(
                    text = "Ready to Generate",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = hindSiliguri,
                    color = cs.onSurface,
                )
                Text(
                    text = "${activeBookIds.size} books • ${activeTeacherEmails.size} teachers • ${activeClassroomIds.size} rooms • ${activeSemesters.size} semesters • $periodsPerDay periods/day",
                    fontSize = 13.5.sp,
                    fontFamily = hindSiliguri,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                val plan = currentPlan
                if (plan != null) {
                    val isLive = plan.status.equals("PUBLISHED", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.6f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "${plan.status} • ${plan.entries.size} class slots",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = hindSiliguri,
                                color = if (isLive) publishAccent.primary else genAccent.primary,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KlassTonalButton(
                                    text = "Explore",
                                    icon = CalendarDaysIcon,
                                    accent = genAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    height = 38.dp,
                                    onClick = onOpenExplorer,
                                )
                                if (!isLive) {
                                    KlassTonalButton(
                                        text = "Publish Live",
                                        icon = CheckIcon,
                                        accent = publishAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        height = 38.dp,
                                        testTag = "publish_routine_button",
                                        onClick = { onPublishPlan(plan) },
                                    )
                                }
                                if (plan.conflicts.isNotEmpty()) {
                                    KlassTonalButton(
                                        text = "${plan.conflicts.size} Conflicts",
                                        icon = WarningIcon,
                                        accent = conflictAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        height = 38.dp,
                                        onClick = onOpenConflicts,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Flying Active Bubbles Streaming from Top-Left & Top-Right Corners into Generate Bow Button
        if (activeEffect && activeBubbles.isNotEmpty() && stageW > 0f && stageH > 0f) {
            val totalBubbles = activeBubbles.size
            val cycleDuration = 1.6f
            activeBubbles.forEachIndexed { idx, bubble ->
                val phaseOffset = (idx.toFloat() / totalBubbles.coerceAtLeast(1))
                val t = ((streamClock / cycleDuration) + phaseOffset) % 1f
                val oneMinusT = 1f - t

                val startX = if (bubble.fromLeftCorner) stageW * 0.08f else stageW * 0.92f
                val startY = stageH * 0.08f + ((idx % 4) * 18f)
                val ctrlX = if (bubble.fromLeftCorner) stageW * 0.16f else stageW * 0.84f
                val ctrlY = stageH * 0.56f
                val endX = if (bubble.fromLeftCorner) stageW * 0.44f else stageW * 0.56f
                val endY = (stageH - bowButtonHeightPx * 0.48f).coerceAtLeast(stageH * 0.5f)

                val bx = (oneMinusT * oneMinusT * startX) + (2f * oneMinusT * t * ctrlX) + (t * t * endX)
                val by = (oneMinusT * oneMinusT * startY) + (2f * oneMinusT * t * ctrlY) + (t * t * endY)
                val scale = when {
                    t < 0.15f -> 0.65f + (t / 0.15f) * 0.35f
                    t > 0.80f -> 1f - ((t - 0.80f) / 0.20f) * 0.55f
                    else -> 1f
                }
                val alpha = when {
                    t < 0.10f -> t / 0.10f
                    t > 0.84f -> ((1f - t) / 0.16f).coerceIn(0f, 1f)
                    else -> 1f
                }

                val bubbleIcon = when (bubble.category) {
                    "SEM" -> CalendarDaysIcon
                    "DEPT" -> GraduationCapIcon
                    "GRP" -> UsersGroupIcon
                    "ROOM" -> DoorOpenIcon
                    else -> BookIcon
                }
                Surface(
                    shape = CircleShape,
                    color = bubble.accent.copy(alpha = if (isDark) 0.28f else 0.18f),
                    border = BorderStroke(1.dp, bubble.accent.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (bx - 48f).roundToInt(),
                                y = (by - 14f).roundToInt(),
                            )
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = bubbleIcon,
                            contentDescription = null,
                            tint = if (isDark) Color.White else bubble.accent,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = bubble.label,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = hindSiliguri,
                            color = if (isDark) Color.White else bubble.accent,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // Bottom Display Dock: Curved Bow Canvas Progress Bar + ONLY One DeepBowArchShape Generate Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .align(Alignment.BottomCenter),
        ) {
            // Curved Bow Progress Bar hugging right above DeepBowArchShape
            if (holdProgress > 0f || isProcessing) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp),
                ) {
                    val w = size.width
                    val btnH = 64.dp.toPx()
                    val yShift = 5.dp.toPx()
                    val bowPath = Path().apply {
                        moveTo(0f, size.height - yShift)
                        quadraticTo(
                            x1 = w / 2f,
                            y1 = (size.height - btnH) - (btnH * 0.85f) - yShift,
                            x2 = w,
                            y2 = size.height - yShift,
                        )
                    }
                    drawPath(
                        path = bowPath,
                        color = genAccent.primary.copy(alpha = 0.22f),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                    )

                    val measure = PathMeasure()
                    measure.setPath(bowPath, false)
                    val segLen = measure.length * holdProgress.coerceIn(0f, 1f)
                    if (segLen > 0f) {
                        val activePath = Path()
                        measure.getSegment(0f, segLen, activePath, true)
                        drawPath(
                            path = activePath,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    genAccent.primary,
                                    Color(0xFF10B981),
                                )
                            ),
                            style = Stroke(width = 5.5.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
            }

            // Bottom Bow-Arch Generate Button (exact DeepBowArchShape geometry of Guest Mode button)
            Surface(
                shape = ZedBowArchShape,
                color = buttonSurfaceColor,
                shadowElevation = if (activeEffect) 6.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .clip(ZedBowArchShape)
                    .testTag("generate_routine_button")
                    .pointerInput(
                        isProcessing,
                        activeBookIds,
                        activeTeacherEmails,
                        activeClassroomIds,
                        activeSemesters,
                    ) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            if (isProcessing) return@awaitEachGesture
                            if (activeBookIds.isEmpty()) {
                                showToast("Please activate at least one book first.")
                                return@awaitEachGesture
                            }
                            if (activeTeacherEmails.isEmpty()) {
                                showToast("Please activate at least one teacher first.")
                                return@awaitEachGesture
                            }
                            if (activeClassroomIds.isEmpty()) {
                                showToast("Please activate at least one classroom first.")
                                return@awaitEachGesture
                            }
                            if (activeSemesters.isEmpty()) {
                                showToast("Please activate at least one semester first.")
                                return@awaitEachGesture
                            }
                            isHolding = true
                            waitForUpOrCancellation()
                            val readyOnRelease = holdProgress >= 0.995f
                            isHolding = false
                            if (readyOnRelease && !isProcessing) {
                                onHoldCompleteGenerate()
                            } else if (!readyOnRelease && !isProcessing) {
                                showToast("Hold until the bow loader reaches 100%, then release.")
                            }
                        }
                    },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 6.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = buttonContentColor,
                        )
                    } else {
                        Icon(
                            imageVector = if (holdProgress >= 0.995f) CheckIcon else SparkleIcon,
                            contentDescription = "Generate",
                            tint = buttonContentColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isProcessing -> "Generating..."
                            isHolding && holdProgress >= 0.995f -> "Release to Generate!"
                            isHolding -> "Hold ${(holdProgress * 100).roundToInt()}%"
                            else -> "Hold to Generate"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = hindSiliguri,
                        color = buttonContentColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
