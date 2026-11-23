package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.LocationDotIcon
import com.mpi.klass.theme.PinIcon
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.TrashCanIcon
import com.mpi.klass.theme.departmentAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
fun CampusMapScreen(
    isDark: Boolean,
    hindSiliguri: FontFamily,
    topContentPadding: Dp = 0.dp,
    onSaved: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val mapAccent = KlassFeaturePalette.admin(isDark)
    val classroomAccent = KlassFeaturePalette.classrooms(isDark)
    val errorAccent = KlassFeaturePalette.error(isDark)

    val (sensorState, resetOrigin) = rememberCampusSensorTracker(active = true)

    var selectedDept by remember { mutableStateOf(RoutineRules.TECH_DEPARTMENTS.first()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var originSet by remember { mutableStateOf(false) }
    val pinnedPoints = remember { mutableStateMapOf<String, DepartmentPoint>() }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val loaded = fetchCampusMapConfigFromFirestore()
            originSet = loaded.originSet
            pinnedPoints.clear()
            loaded.departmentPoints.forEach { (dept, pt) ->
                val norm = RoutineRules.normalizeDepartmentShort(dept)
                if (norm in RoutineRules.TECH_DEPARTMENTS) {
                    pinnedPoints[norm] = pt.copy(department = norm)
                }
            }
        } finally {
            isLoading = false
        }
    }

    fun pinDepartmentAt(dept: String, xMeters: Float, yMeters: Float, heading: Float = 0f) {
        pinnedPoints[dept] = DepartmentPoint(
            department = dept,
            xMeters = xMeters.coerceIn(-80f, 80f),
            yMeters = yMeters.coerceIn(-80f, 80f),
            headingDegrees = heading,
            updatedAt = getCurrentTimeMillis(),
        )
        originSet = true
    }

    fun saveMap() {
        if (isSaving) return
        scope.launch {
            isSaving = true
            try {
                val cfg = CampusMapConfig(
                    departmentPoints = pinnedPoints.toMap(),
                    originSet = originSet || pinnedPoints.isNotEmpty(),
                    updatedAt = getCurrentTimeMillis(),
                )
                val res = saveCampusMapConfigInFirestore(cfg)
                if (res.isSuccess) {
                    showToast("Campus coordinates saved (${pinnedPoints.size}/7 depts)!")
                    onSaved()
                } else {
                    showToast("Saved locally; check internet to sync.")
                }
            } finally {
                isSaving = false
            }
        }
    }

    val currentConfig = CampusMapConfig(
        departmentPoints = pinnedPoints.toMap(),
        originSet = originSet,
    )
    val selectedDeptAccent = departmentAccent(selectedDept, isDark)
    val textMeasurer = rememberTextMeasurer()

    MinimalBackdrop(isDark = isDark, focusAccent = mapAccent.primary) {
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
                KlassRowListSkeleton(
                    isDark = isDark,
                    showHeaderBar = true,
                    showChipRow = true,
                    showActionPill = true,
                )
                return@Column
            }

            // Top summary + Save pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 920.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    MinimalStatBadge(
                        icon = LocationDotIcon,
                        text = "${pinnedPoints.size}/7 Pinned",
                        accent = classroomAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                    MinimalStatBadge(
                        icon = PinIcon,
                        text = "Hdg ${sensorState.headingDegrees.roundToInt()}° (${compassCard(sensorState.headingDegrees)})",
                        accent = mapAccent.primary,
                        isDark = isDark,
                        hindSiliguri = hindSiliguri,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                HeaderSavePillButton(
                    text = if (isSaving) "Saving..." else "Save",
                    accent = mapAccent.primary,
                    isDark = isDark,
                    hindSiliguri = hindSiliguri,
                    isLoading = isSaving,
                    enabled = !isSaving,
                    testTag = "campus_map_save_button",
                    onClick = { saveMap() },
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 920.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                // Department Selector Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        items(RoutineRules.TECH_DEPARTMENTS) { dept ->
                            val dAccent = departmentAccent(dept, isDark)
                            val isPinned = pinnedPoints.containsKey(dept)
                            MinimalIconChip(
                                label = if (isPinned) "$dept ✓" else dept,
                                selected = selectedDept == dept,
                                accent = dAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                testTag = "campus_map_dept_$dept",
                                onClick = { selectedDept = dept },
                            )
                        }
                    }
                }

                // Interactive 2D Coordinate Grid Canvas
                item {
                    val gridBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                    val gridLine = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    val axisLine = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                    val halfSpanMeters = 60f

                    val deptColorMap = RoutineRules.TECH_DEPARTMENTS.associateWith {
                        departmentAccent(it, isDark).primary
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = gridBg,
                        border = BorderStroke(1.dp, selectedDeptAccent.borderTint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(290.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(selectedDept) {
                                        detectTapGestures { offset ->
                                            val w = size.width.toFloat().coerceAtLeast(1f)
                                            val h = size.height.toFloat().coerceAtLeast(1f)
                                            val cx = w / 2f
                                            val cy = h / 2f
                                            val scale = minOf(w, h) / (halfSpanMeters * 2f)
                                            val tappedX = (offset.x - cx) / scale
                                            val tappedY = (cy - offset.y) / scale
                                            pinDepartmentAt(
                                                dept = selectedDept,
                                                xMeters = tappedX,
                                                yMeters = tappedY,
                                                heading = sensorState.headingDegrees,
                                            )
                                        }
                                    },
                            ) {
                                val w = size.width
                                val h = size.height
                                val cx = w / 2f
                                val cy = h / 2f
                                val scale = minOf(w, h) / (halfSpanMeters * 2f)

                                // Grid every 10m
                                for (m in -60..60 step 10) {
                                    val px = cx + m * scale
                                    val py = cy - m * scale
                                    val isAxis = m == 0
                                    drawLine(
                                        color = if (isAxis) axisLine else gridLine,
                                        start = Offset(px, 0f),
                                        end = Offset(px, h),
                                        strokeWidth = if (isAxis) 1.8f else 1f,
                                    )
                                    drawLine(
                                        color = if (isAxis) axisLine else gridLine,
                                        start = Offset(0f, py),
                                        end = Offset(w, py),
                                        strokeWidth = if (isAxis) 1.8f else 1f,
                                    )
                                }

                                // Origin marker
                                drawCircle(
                                    color = axisLine.copy(alpha = 0.35f),
                                    radius = 9f,
                                    center = Offset(cx, cy),
                                )
                                drawCircle(
                                    color = axisLine,
                                    radius = 4f,
                                    center = Offset(cx, cy),
                                )

                                // Live sensor cursor + heading cone
                                val ux = (cx + sensorState.xMeters * scale).coerceIn(16f, w - 16f)
                                val uy = (cy - sensorState.yMeters * scale).coerceIn(16f, h - 16f)
                                val rad = (sensorState.headingDegrees * PI / 180.0).toFloat()
                                val coneLen = 28f
                                val spread = 0.42f
                                val conePath = Path().apply {
                                    moveTo(ux, uy)
                                    lineTo(
                                        ux + coneLen * sin(rad - spread),
                                        uy - coneLen * cos(rad - spread),
                                    )
                                    lineTo(
                                        ux + coneLen * sin(rad + spread),
                                        uy - coneLen * cos(rad + spread),
                                    )
                                    close()
                                }
                                drawPath(
                                    path = conePath,
                                    color = mapAccent.primary.copy(alpha = 0.25f),
                                )
                                drawCircle(
                                    color = mapAccent.primary,
                                    radius = 7f,
                                    center = Offset(ux, uy),
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = Offset(ux, uy),
                                )

                                // Pinned departments
                                pinnedPoints.values.forEach { pt ->
                                    val px = (cx + pt.xMeters * scale).coerceIn(22f, w - 22f)
                                    val py = (cy - pt.yMeters * scale).coerceIn(22f, h - 22f)
                                    val col = deptColorMap[pt.department] ?: classroomAccent.primary
                                    val isSel = pt.department == selectedDept

                                    if (isSel) {
                                        drawCircle(
                                            color = col.copy(alpha = 0.22f),
                                            radius = 22f,
                                            center = Offset(px, py),
                                        )
                                        drawCircle(
                                            color = col,
                                            radius = 16f,
                                            center = Offset(px, py),
                                            style = Stroke(width = 2.5f, cap = StrokeCap.Round),
                                        )
                                    }
                                    drawCircle(
                                        color = col,
                                        radius = 10f,
                                        center = Offset(px, py),
                                    )
                                    val labelLayout = textMeasurer.measure(
                                        text = pt.department,
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF0F172A),
                                        ),
                                    )
                                    drawText(
                                        textLayoutResult = labelLayout,
                                        topLeft = Offset(
                                            x = (px - labelLayout.size.width / 2f).coerceIn(4f, w - labelLayout.size.width - 4f),
                                            y = (py + 13f).coerceAtMost(h - labelLayout.size.height - 4f),
                                        ),
                                    )
                                }
                            }

                            // Legend corner tag
                            Surface(
                                shape = CircleShape,
                                color = cs.surface.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp),
                            ) {
                                Text(
                                    text = "Tap grid or walk to pin $selectedDept",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = hindSiliguri,
                                    color = cs.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Sensor & Pinning Controls
                item {
                    SocialSurfaceBlock(
                        isDark = isDark,
                        showTopHairline = true,
                        showBottomHairline = true,
                        accentTint = selectedDeptAccent.primary,
                    ) {
                        GoogleSectionHeader(
                            title = "$selectedDept Sensor & Coordinates",
                            hindSiliguri = hindSiliguri,
                            accent = selectedDeptAccent.primary,
                        )

                        val currentPin = pinnedPoints[selectedDept]
                        val coordSubtitle = if (currentPin != null) {
                            "Pinned at (${formatMeters(currentPin.xMeters)}m E, ${formatMeters(currentPin.yMeters)}m N)"
                        } else {
                            "Not pinned yet — tap the map or press Pin Here"
                        }

                        Text(
                            text = "Live position: (${formatMeters(sensorState.xMeters)}m E, ${formatMeters(sensorState.yMeters)}m N) • ${sensorState.stepCount} steps • $coordSubtitle",
                            fontSize = 13.5.sp,
                            fontFamily = hindSiliguri,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            KlassTonalButton(
                                text = "Origin (0,0)",
                                icon = RefreshIcon,
                                accent = mapAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                                modifier = Modifier.weight(1f),
                                testTag = "campus_map_reset_origin_button",
                                onClick = {
                                    resetOrigin()
                                    originSet = true
                                    showToast("Campus origin set to (0, 0)")
                                },
                            )

                            KlassPrimaryButton(
                                text = "Pin $selectedDept Here",
                                icon = LocationDotIcon,
                                hindSiliguri = hindSiliguri,
                                isDark = isDark,
                                customPrimaryColor = selectedDeptAccent.primary,
                                height = 42.dp,
                                cornerRadius = 14.dp,
                                modifier = Modifier.weight(1.3f),
                                testTag = "campus_map_pin_here_button",
                                onClick = {
                                    pinDepartmentAt(
                                        dept = selectedDept,
                                        xMeters = sensorState.xMeters,
                                        yMeters = sensorState.yMeters,
                                        heading = sensorState.headingDegrees,
                                    )
                                    showToast("Pinned $selectedDept")
                                },
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fine 2m Nudge Controls for Selected Department
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val pt = currentPin ?: DepartmentPoint(selectedDept, sensorState.xMeters, sensorState.yMeters)
                            listOf(
                                Triple("N +2m", 0f, 2f),
                                Triple("S -2m", 0f, -2f),
                                Triple("E +2m", 2f, 0f),
                                Triple("W -2m", -2f, 0f),
                            ).forEach { (label, dx, dy) ->
                                Surface(
                                    onClick = {
                                        pinDepartmentAt(
                                            dept = selectedDept,
                                            xMeters = pt.xMeters + dx,
                                            yMeters = pt.yMeters + dy,
                                            heading = pt.headingDegrees,
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = selectedDeptAccent.softTint,
                                    border = BorderStroke(0.75.dp, selectedDeptAccent.borderTint),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = hindSiliguri,
                                            color = selectedDeptAccent.primary,
                                        )
                                    }
                                }
                            }
                        }

                        if (pinnedPoints.isNotEmpty()) {
                            SocialInsetDivider(startIndent = 16.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (currentPin != null) {
                                    KlassTonalButton(
                                        text = "Remove $selectedDept Pin",
                                        icon = TrashCanIcon,
                                        accent = errorAccent.primary,
                                        isDark = isDark,
                                        hindSiliguri = hindSiliguri,
                                        modifier = Modifier.weight(1f),
                                        onClick = { pinnedPoints.remove(selectedDept) },
                                    )
                                }
                                KlassTonalButton(
                                    text = "Clear All Pins",
                                    icon = TrashCanIcon,
                                    accent = errorAccent.primary,
                                    isDark = isDark,
                                    hindSiliguri = hindSiliguri,
                                    modifier = Modifier.weight(1f),
                                    onClick = { pinnedPoints.clear() },
                                )
                            }
                        }
                    }
                }

                item { SocialSectionDividerBand(isDark = isDark) }

                // Distance Summary for Routine Engine
                item {
                    SocialSurfaceBlock(isDark = isDark, showTopHairline = true, showBottomHairline = true) {
                        GoogleSectionHeader(
                            title = "Distances from $selectedDept",
                            hindSiliguri = hindSiliguri,
                            accent = classroomAccent.primary,
                        )
                        val others = RoutineRules.TECH_DEPARTMENTS.filter { it != selectedDept }
                        others.forEachIndexed { idx, other ->
                            val otherAccent = departmentAccent(other, isDark)
                            val ptOther = pinnedPoints[other]
                            val ptSel = pinnedPoints[selectedDept]
                            val distText = if (ptSel != null && ptOther != null) {
                                val dist = currentConfig.distanceBetween(selectedDept, other)
                                "${dist.roundToInt()} m apart"
                            } else if (ptOther != null) {
                                "Pinned (${formatMeters(ptOther.xMeters)}m, ${formatMeters(ptOther.yMeters)}m)"
                            } else {
                                "Not pinned"
                            }
                            KlassDetailFieldRow(
                                label = "$other Zone",
                                value = distText,
                                icon = if (ptOther != null) CheckIcon else LocationDotIcon,
                                accent = otherAccent.primary,
                                isDark = isDark,
                                hindSiliguri = hindSiliguri,
                            )
                            if (idx < others.lastIndex) {
                                SocialInsetDivider(startIndent = 68.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMeters(v: Float): String {
    val tenths = (v * 10f).roundToInt()
    val whole = tenths / 10
    val frac = kotlin.math.abs(tenths % 10)
    return "$whole.$frac"
}

private fun compassCard(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val idx = (((deg % 360f) + 360f) % 360f / 45f).roundToInt() % 8
    return dirs[idx]
}
