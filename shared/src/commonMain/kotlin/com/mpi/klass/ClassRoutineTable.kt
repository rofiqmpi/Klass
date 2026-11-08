package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

@Composable
fun ClassRoutineTable(
    hindSiliguri: FontFamily,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val slateHeaderBg = Color(0xFF355470)
    val orangeTimeBg = Color(0xFFEA6C13)
    val orangeCodeBg = Color(0xFFEB7C24)
    val dataCellBg = Color(0xFFDFEAF4)
    val cardBackground = Color(0xFFF1F5F9) // Distinct card background for individual items
    val gapThickness = 2.dp // Gap between individual floating cells/cards

    val colDayWeight = 0.65f
    val colTimeWeight = 1.6f
    val colSubjectWeight = 2.7f
    val colCodeWeight = 1.0f
    val colRoomWeight = 1.0f
    val totalDataWeight = colTimeWeight + colSubjectWeight + colCodeWeight + colRoomWeight

    val days = remember {
        listOf(
            DayRoutineConfig("রবিবার", 4),
            DayRoutineConfig("সোমবার", 4),
            DayRoutineConfig("মঙ্গলবার", 3),
            DayRoutineConfig("বুধবার", 5),
            DayRoutineConfig("বৃহস্পতিবার", 4),
        )
    }

    val slotsMap = remember { mapOf<String, RoutineSlot>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_table"),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gapThickness),
        ) {
            // Header Row (Individual Card Headers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gapThickness),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // দিন
                Surface(
                    modifier = Modifier.weight(colDayWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = slateHeaderBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "দিন",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // সময়
                Surface(
                    modifier = Modifier.weight(colTimeWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = orangeTimeBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "সময়",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // বিষয়
                Surface(
                    modifier = Modifier.weight(colSubjectWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = slateHeaderBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "বিষয়",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // কোড
                Surface(
                    modifier = Modifier.weight(colCodeWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = orangeCodeBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "কোড",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                // কক্ষ
                Surface(
                    modifier = Modifier.weight(colRoomWeight).fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = slateHeaderBg,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "কক্ষ",
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(dayWeight),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gapThickness),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Day column as individual styled card
                    Surface(
                        modifier = Modifier
                            .weight(colDayWeight)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(3.dp),
                        color = slateHeaderBg,
                        shadowElevation = 1.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = dayConfig.name,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.graphicsLayer(rotationZ = -90f),
                            )
                        }
                    }

                    // Periods Column (Individual cards for each cell)
                    Column(
                        modifier = Modifier
                            .weight(totalDataWeight)
                            .fillMaxHeight(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gapThickness),
                    ) {
                        for (periodIndex in 0 until dayConfig.rowCount) {
                            val slotKey = "${dayConfig.name}_$periodIndex"
                            val slotData = slotsMap[slotKey] ?: RoutineSlot()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(gapThickness),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 1. সময় Card
                                Surface(
                                    modifier = Modifier
                                        .weight(colTimeWeight)
                                        .fillMaxHeight(),
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
                                            fontSize = 10.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                        )
                                    }
                                }

                                // 2. বিষয় Card
                                Surface(
                                    modifier = Modifier
                                        .weight(colSubjectWeight)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(3.dp),
                                    color = dataCellBg,
                                    shadowElevation = 0.5.dp,
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 2.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = slotData.subject,
                                            fontSize = 10.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                        )
                                    }
                                }

                                // 3. কোড Card
                                Surface(
                                    modifier = Modifier
                                        .weight(colCodeWeight)
                                        .fillMaxHeight(),
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
                                            fontSize = 10.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                        )
                                    }
                                }

                                // 4. কক্ষ Card
                                Surface(
                                    modifier = Modifier
                                        .weight(colRoomWeight)
                                        .fillMaxHeight(),
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
                                            fontSize = 10.sp,
                                            fontFamily = hindSiliguri,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A),
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
