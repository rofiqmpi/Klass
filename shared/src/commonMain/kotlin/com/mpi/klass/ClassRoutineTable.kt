package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val gridBorderColor = Color.White
    val dividerThickness = 1.dp

    val colDayWeight = 1.0f
    val colTimeWeight = 1.6f
    val colSubjectWeight = 2.6f
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

    val slotsMap = remember { mutableStateMapOf<String, RoutineSlot>() }

    var editingSlotKey by remember { mutableStateOf<String?>(null) }
    var editTime by remember { mutableStateOf("") }
    var editSubject by remember { mutableStateOf("") }
    var editCode by remember { mutableStateOf("") }
    var editRoom by remember { mutableStateOf("") }

    if (editingSlotKey != null) {
        val currentKey = editingSlotKey!!
        AlertDialog(
            onDismissRequest = { editingSlotKey = null },
            title = {
                Text(
                    text = "ক্লাস তথ্য সম্পাদনা",
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = editTime,
                        onValueChange = { editTime = it },
                        label = { Text("সময়", fontFamily = hindSiliguri) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("বিষয়", fontFamily = hindSiliguri) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editCode,
                        onValueChange = { editCode = it },
                        label = { Text("কোড", fontFamily = hindSiliguri) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editRoom,
                        onValueChange = { editRoom = it },
                        label = { Text("কক্ষ / ল্যাব", fontFamily = hindSiliguri) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        slotsMap[currentKey] = RoutineSlot(
                            time = editTime.trim(),
                            subject = editSubject.trim(),
                            code = editCode.trim(),
                            room = editRoom.trim(),
                        )
                        editingSlotKey = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangeTimeBg),
                ) {
                    Text("সংরক্ষণ", fontFamily = hindSiliguri, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        slotsMap.remove(currentKey)
                        editingSlotKey = null
                    },
                ) {
                    Text("ফাঁকা করুন", color = Color(0xFFEF4444), fontFamily = hindSiliguri)
                }
            },
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("routine_table"),
        shape = RoundedCornerShape(2.dp),
        color = gridBorderColor,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gridBorderColor),
        ) {
            // Header Row (Fixed height proportion)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .background(gridBorderColor),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // দিন
                Box(
                    modifier = Modifier
                        .weight(colDayWeight)
                        .fillMaxHeight()
                        .background(slateHeaderBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "দিন",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                // সময়
                Box(
                    modifier = Modifier
                        .weight(colTimeWeight)
                        .fillMaxHeight()
                        .background(orangeTimeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "সময়",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                // বিষয়
                Box(
                    modifier = Modifier
                        .weight(colSubjectWeight)
                        .fillMaxHeight()
                        .background(slateHeaderBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "বিষয়",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                // কোড
                Box(
                    modifier = Modifier
                        .weight(colCodeWeight)
                        .fillMaxHeight()
                        .background(orangeCodeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "কোড",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = hindSiliguri,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                // কক্ষ / ল্যাব
                Box(
                    modifier = Modifier
                        .weight(colRoomWeight)
                        .fillMaxHeight()
                        .background(slateHeaderBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "কক্ষ/ল্যাব",
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dividerThickness)
                    .background(gridBorderColor)
            )

            val totalRows = days.sumOf { it.rowCount }

            // Day Blocks sharing remaining height proportionally
            days.forEach { dayConfig ->
                val dayWeight = (dayConfig.rowCount.toFloat() / totalRows) * 19f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(dayWeight)
                        .background(gridBorderColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Day column with vertical text
                    Box(
                        modifier = Modifier
                            .weight(colDayWeight)
                            .fillMaxHeight()
                            .background(slateHeaderBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = dayConfig.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = hindSiliguri,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer(rotationZ = -90f),
                        )
                    }

                    Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                    // Periods Column
                    Column(
                        modifier = Modifier
                            .weight(totalDataWeight)
                            .fillMaxHeight(),
                    ) {
                        for (periodIndex in 0 until dayConfig.rowCount) {
                            if (periodIndex > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dividerThickness)
                                        .background(gridBorderColor)
                                )
                            }

                            val slotKey = "${dayConfig.name}_$periodIndex"
                            val slotData = slotsMap[slotKey] ?: RoutineSlot()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(gridBorderColor),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 1. সময়
                                Box(
                                    modifier = Modifier
                                        .weight(colTimeWeight)
                                        .fillMaxHeight()
                                        .background(dataCellBg)
                                        .clickable {
                                            editingSlotKey = slotKey
                                            editTime = slotData.time
                                            editSubject = slotData.subject
                                            editCode = slotData.code
                                            editRoom = slotData.room
                                        }
                                        .padding(horizontal = 1.dp),
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

                                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                                // 2. বিষয়
                                Box(
                                    modifier = Modifier
                                        .weight(colSubjectWeight)
                                        .fillMaxHeight()
                                        .background(dataCellBg)
                                        .clickable {
                                            editingSlotKey = slotKey
                                            editTime = slotData.time
                                            editSubject = slotData.subject
                                            editCode = slotData.code
                                            editRoom = slotData.room
                                        }
                                        .padding(horizontal = 2.dp),
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

                                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                                // 3. কোড
                                Box(
                                    modifier = Modifier
                                        .weight(colCodeWeight)
                                        .fillMaxHeight()
                                        .background(dataCellBg)
                                        .clickable {
                                            editingSlotKey = slotKey
                                            editTime = slotData.time
                                            editSubject = slotData.subject
                                            editCode = slotData.code
                                            editRoom = slotData.room
                                        }
                                        .padding(horizontal = 1.dp),
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

                                Box(modifier = Modifier.width(dividerThickness).fillMaxHeight().background(gridBorderColor))

                                // 4. কক্ষ / ল্যাব
                                Box(
                                    modifier = Modifier
                                        .weight(colRoomWeight)
                                        .fillMaxHeight()
                                        .background(dataCellBg)
                                        .clickable {
                                            editingSlotKey = slotKey
                                            editTime = slotData.time
                                            editSubject = slotData.subject
                                            editCode = slotData.code
                                            editRoom = slotData.room
                                        }
                                        .padding(horizontal = 1.dp),
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

                if (dayConfig != days.last()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dividerThickness)
                            .background(gridBorderColor)
                    )
                }
            }
        }
    }
}
