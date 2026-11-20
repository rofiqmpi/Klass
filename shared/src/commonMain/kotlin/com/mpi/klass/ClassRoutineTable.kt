package com.mpi.klass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RoutineSlot(
    val time: String = "",
    val subject: String = "",
    val code: String = "",
    val room: String = "",
)

data class DayRoutineConfig(
    val name: String,
    val rowCount: Int,
)

private fun Modifier.verticalDayText(): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = 0,
            maxWidth = constraints.maxHeight,
            minHeight = 0,
            maxHeight = constraints.maxWidth,
        )
    )
    layout(placeable.height, placeable.width) {
        placeable.placeRelativeWithLayer(
            x = -(placeable.width - placeable.height) / 2,
            y = -(placeable.height - placeable.width) / 2,
            layerBlock = {
                rotationZ = -90f
            }
        )
    }
}

@Composable
fun ClassRoutineTable(
    hindSiliguri: FontFamily,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    entries: List<RoutineEntry> = emptyList(),
    periodsPerDay: Int = 7,
    shift: String = RoutineRules.SHIFT_1,
    config: RoutineConfig = RoutineConfig(),
    showGroupOrRoomHint: Boolean = false,
    onSlotClick: ((RoutineEntry) -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val routineAccent = com.mpi.klass.theme.KlassFeaturePalette.routine(isDark)
    val labAccent = com.mpi.klass.theme.KlassFeaturePalette.teaching(isDark)
    val dayHeaderBg = routineAccent.softTint.copy(alpha = if (isDark) 0.34f else 0.16f)
    val dayHeaderFg = routineAccent.primary
    val headerBg = if (isDark) cs.surfaceContainerHigh.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f)
    val headerTextColor = cs.onSurfaceVariant
    val theoryCellBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.80f) else Color.White.copy(alpha = 0.86f)
    val practicalCellBg = labAccent.softTint.copy(alpha = if (isDark) 0.28f else 0.15f)
    val practicalBorderCol = labAccent.borderTint
    val emptyCellBg = if (isDark) cs.surfaceContainerLow.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.54f)
    val gapThickness = 3.dp
    val cellShape = RoundedCornerShape(7.dp)

    val colDayWeight = 0.65f
    val colTimeWeight = 1.50f
    val colSubjectWeight = 2.70f
    val colCodeWeight = 0.95f
    val colRoomWeight = 0.95f
    val totalDataWeight = colTimeWeight + colSubjectWeight + colCodeWeight + colRoomWeight

    val entriesByDay = remember(entries) {
        entries.groupBy { it.day.trim().lowercase() }
            .mapValues { (_, list) -> list.sortedBy { it.periodIndex } }
    }

    val defaultRowCounts = remember { listOf(4, 4, 3, 5, 4) }

    val days = remember(entriesByDay, entries.isEmpty()) {
        RoutineRules.WORKING_DAYS.mapIndexed { idx, dayName ->
            val daySlots = entriesByDay[dayName.lowercase()] ?: emptyList()
            val count = if (entries.isEmpty()) {
                defaultRowCounts.getOrElse(idx) { 4 }
            } else {
                daySlots.size.coerceAtLeast(3)
            }
            DayRoutineConfig(dayName, count)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_table"),
        verticalArrangement = Arrangement.spacedBy(gapThickness),
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f),
            horizontalArrangement = Arrangement.spacedBy(gapThickness),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoutineHeaderCell("Day", colDayWeight, dayHeaderBg, dayHeaderFg, hindSiliguri, cellShape)
            RoutineHeaderCell("Time", colTimeWeight, headerBg, headerTextColor, hindSiliguri, cellShape)
            RoutineHeaderCell("Subject", colSubjectWeight, headerBg, headerTextColor, hindSiliguri, cellShape)
            RoutineHeaderCell("Code", colCodeWeight, headerBg, headerTextColor, hindSiliguri, cellShape)
            RoutineHeaderCell("Room", colRoomWeight, headerBg, headerTextColor, hindSiliguri, cellShape)
        }

        days.forEachIndexed { dayIndex, dayConfig ->
            val dayWeight = dayConfig.rowCount.toFloat()
            val dayEntries = entriesByDay[dayConfig.name.lowercase()] ?: emptyList()
            val dayAccent = com.mpi.klass.theme.dayPaletteAt(dayIndex, isDark)
            val dayBadgeBg = dayAccent.softTint.copy(alpha = if (isDark) 0.32f else 0.16f)
            val dayBadgeFg = dayAccent.primary

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(dayWeight),
                horizontalArrangement = Arrangement.spacedBy(gapThickness),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Day column card with distinct sun-thu rainbow semantic color
                Surface(
                    modifier = Modifier
                        .weight(colDayWeight)
                        .fillMaxHeight(),
                    shape = cellShape,
                    color = dayBadgeBg,
                    border = BorderStroke(0.75.dp, dayAccent.borderTint),
                    shadowElevation = 0.dp,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = dayConfig.name.uppercase(),
                            color = dayBadgeFg,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.verticalDayText(),
                        )
                    }
                }

                // Periods Column
                Column(
                    modifier = Modifier
                        .weight(totalDataWeight)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(gapThickness),
                ) {
                    for (rowIndex in 0 until dayConfig.rowCount) {
                        val entry = dayEntries.getOrNull(rowIndex)
                        val slotData = if (entry != null) {
                            val timeText = if (entry.startTime.isNotBlank() && entry.endTime.isNotBlank()) {
                                "${entry.startTime.replace(" AM", "").replace(" PM", "")}-${entry.endTime}"
                            } else {
                                ""
                            }
                            val subjText = if (showGroupOrRoomHint) {
                                "${entry.subjectTitle} (${entry.department} ${entry.group})"
                            } else {
                                entry.subjectTitle
                            }
                            RoutineSlot(
                                time = timeText,
                                subject = subjText,
                                code = entry.subjectCode,
                                room = entry.roomNumber,
                            )
                        } else {
                            RoutineSlot()
                        }

                        val isPractical = entry?.isPractical == true
                        val rowBg = when {
                            entry == null -> emptyCellBg
                            isPractical -> practicalCellBg
                            else -> theoryCellBg
                        }
                        val cellBorder = when {
                            entry == null -> null
                            isPractical -> BorderStroke(0.75.dp, practicalBorderCol)
                            else -> BorderStroke(0.5.dp, cs.outlineVariant.copy(alpha = 0.48f))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .then(
                                    if (entry != null && onSlotClick != null) {
                                        Modifier.clickable { onSlotClick(entry) }
                                    } else {
                                        Modifier
                                    }
                                ),
                            horizontalArrangement = Arrangement.spacedBy(gapThickness),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RoutineDataCell(
                                text = slotData.time,
                                weight = colTimeWeight,
                                bg = rowBg,
                                border = cellBorder,
                                textColor = cs.onSurfaceVariant,
                                hindSiliguri = hindSiliguri,
                                shape = cellShape,
                                singleLineClip = true,
                            )
                            RoutineDataCell(
                                text = slotData.subject,
                                weight = colSubjectWeight,
                                bg = rowBg,
                                border = cellBorder,
                                textColor = if (isPractical) labAccent.primary else cs.onSurface,
                                fontWeight = if (isPractical) FontWeight.Bold else FontWeight.Medium,
                                hindSiliguri = hindSiliguri,
                                shape = cellShape,
                                singleLineClip = false,
                            )
                            RoutineDataCell(
                                text = slotData.code,
                                weight = colCodeWeight,
                                bg = rowBg,
                                border = cellBorder,
                                textColor = cs.onSurfaceVariant,
                                hindSiliguri = hindSiliguri,
                                shape = cellShape,
                                singleLineClip = true,
                            )
                            RoutineDataCell(
                                text = slotData.room,
                                weight = colRoomWeight,
                                bg = rowBg,
                                border = cellBorder,
                                textColor = if (entry != null) dayAccent.primary else cs.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                hindSiliguri = hindSiliguri,
                                shape = cellShape,
                                singleLineClip = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.RoutineHeaderCell(
    label: String,
    weight: Float,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    hindSiliguri: FontFamily,
    shape: RoundedCornerShape,
) {
    Surface(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight(),
        shape = shape,
        color = bg,
        shadowElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontFamily = hindSiliguri,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.RoutineDataCell(
    text: String,
    weight: Float,
    bg: androidx.compose.ui.graphics.Color,
    border: BorderStroke?,
    textColor: androidx.compose.ui.graphics.Color,
    hindSiliguri: FontFamily,
    shape: RoundedCornerShape,
    singleLineClip: Boolean,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Surface(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight(),
        shape = shape,
        color = bg,
        border = border,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = if (singleLineClip) 1.dp else 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                fontSize = 9.5.sp,
                fontFamily = hindSiliguri,
                fontWeight = fontWeight,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = !singleLineClip,
                overflow = if (singleLineClip) TextOverflow.Clip else TextOverflow.Ellipsis,
            )
        }
    }
}
