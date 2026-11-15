package com.mpi.klass

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
    emptyMessage: String = "",
    onSlotClick: ((RoutineEntry) -> Unit)? = null,
) {
    val slateHeaderBg = Color(0xFF355470)
    val orangeTimeBg = Color(0xFFEA6C13)
    val orangeCodeBg = Color(0xFFEB7C24)
    val dataCellBg = if (isDark) Color(0xFF1E293B) else Color(0xFFDFEAF4)
    val cellTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val gapThickness = 2.dp

    val colDayWeight = 0.65f
    val colTimeWeight = 1.55f
    val colSubjectWeight = 2.65f
    val colCodeWeight = 0.95f
    val colRoomWeight = 0.95f
    val totalDataWeight = colTimeWeight + colSubjectWeight + colCodeWeight + colRoomWeight

    val entriesByDay = remember(entries) {
        entries.groupBy { it.day.trim().lowercase() }
            .mapValues { (_, list) -> list.sortedBy { it.periodIndex } }
    }

    val defaultRowCounts = remember { listOf(4, 4, 3, 5, 4) }

    val days = remember(entriesByDay) {
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_table"),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gapThickness),
        ) {
            // Header Row (Individual Card Headers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                horizontalArrangement = Arrangement.spacedBy(gapThickness),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Day
                Surface(
                    modifier = Modifier.weight(colDayWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = slateHeaderBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Day",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // Time
                Surface(
                    modifier = Modifier.weight(colTimeWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = orangeTimeBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Time",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // Subject
                Surface(
                    modifier = Modifier.weight(colSubjectWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = slateHeaderBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Subject",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // Code
                Surface(
                    modifier = Modifier.weight(colCodeWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = orangeCodeBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Code",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // Room
                Surface(
                    modifier = Modifier.weight(colRoomWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = slateHeaderBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Room",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }

            days.forEach { dayConfig ->
                val dayWeight = dayConfig.rowCount.toFloat()
                val dayEntries = entriesByDay[dayConfig.name.lowercase()] ?: emptyList()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(dayWeight),
                    horizontalArrangement = Arrangement.spacedBy(gapThickness),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Day column card
                    Surface(
                        modifier = Modifier
                            .weight(colDayWeight)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(3.dp),
                        color = slateHeaderBg,
                        shadowElevation = 1.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dayConfig.name,
                                color = Color.White,
                                fontSize = 10.sp,
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
                                // 1. Time Card
                                Surface(
                                    modifier = Modifier.weight(colTimeWeight).fillMaxHeight(),
                                    shape = RoundedCornerShape(3.dp),
                                    color = dataCellBg,
                                    shadowElevation = 0.5.dp,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 1.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = slotData.time,
                                            fontSize = 9.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = cellTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                        )
                                    }
                                }

                                // 2. Subject Card
                                Surface(
                                    modifier = Modifier.weight(colSubjectWeight).fillMaxHeight(),
                                    shape = RoundedCornerShape(3.dp),
                                    color = dataCellBg,
                                    shadowElevation = 0.5.dp,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 3.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = slotData.subject,
                                            fontSize = 9.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = cellTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }

                                // 3. Code Card
                                Surface(
                                    modifier = Modifier.weight(colCodeWeight).fillMaxHeight(),
                                    shape = RoundedCornerShape(3.dp),
                                    color = dataCellBg,
                                    shadowElevation = 0.5.dp,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 1.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = slotData.code,
                                            fontSize = 9.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = cellTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                        )
                                    }
                                }

                                // 4. Room Card
                                Surface(
                                    modifier = Modifier.weight(colRoomWeight).fillMaxHeight(),
                                    shape = RoundedCornerShape(3.dp),
                                    color = dataCellBg,
                                    shadowElevation = 0.5.dp,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 1.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = slotData.room,
                                            fontSize = 9.5.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = cellTextColor,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
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
