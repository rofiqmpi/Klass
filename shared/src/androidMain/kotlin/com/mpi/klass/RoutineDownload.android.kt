package com.mpi.klass

import android.content.ContentValues
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mpi.klass.theme.getAppContext
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun downloadRoutinePdf(
    label: String,
    shift: String,
    config: RoutineConfig,
    entries: List<RoutineEntry>,
): Result<String> = withContext(Dispatchers.IO) {
    val ctx = getAppContext()
        ?: return@withContext Result.failure(IllegalStateException("App context unavailable"))

    val cleanLabel = label.ifBlank { "Class_Routine" }
        .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    val fileName = "MPI_Klass_${cleanLabel}_${System.currentTimeMillis() % 10000}.pdf"

    val doc = PdfDocument()
    try {
        val pageWidth = 842
        val pageHeight = 595
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 28f
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 11f
        }
        val headerFill = Paint().apply { color = Color.parseColor("#EEF2FF") }
        val practicalFill = Paint().apply { color = Color.parseColor("#FEF3C7") }
        val cellStroke = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cellText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
        }
        val cellBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val displayTitle = if (label.isNotBlank()) "MPI Klass Routine • $label" else "MPI Klass Class Routine"
        canvas.drawText(displayTitle, margin, margin + 16f, titlePaint)
        canvas.drawText("$shift • ${entries.size} Scheduled Classes", margin, margin + 32f, subtitlePaint)

        val days = RoutineRules.WORKING_DAYS
        val periods = config.periodsPerDay.coerceIn(4, 8)
        val startY = margin + 46f
        val tableWidth = pageWidth - margin * 2
        val tableHeight = pageHeight - startY - margin
        val dayColW = 68f
        val colW = (tableWidth - dayColW) / periods
        val headerH = 28f
        val rowH = (tableHeight - headerH) / days.size

        // Period headers
        canvas.drawRect(margin, startY, margin + dayColW, startY + headerH, headerFill)
        canvas.drawRect(margin, startY, margin + dayColW, startY + headerH, cellStroke)
        canvas.drawText("Day / Period", margin + 6f, startY + 18f, headerText)

        for (p in 0 until periods) {
            val x = margin + dayColW + p * colW
            canvas.drawRect(x, startY, x + colW, startY + headerH, headerFill)
            canvas.drawRect(x, startY, x + colW, startY + headerH, cellStroke)
            val (sTime, _) = RoutineRules.periodTimeRange(shift, p, config)
            canvas.drawText("P${p + 1} ($sTime)", x + 6f, startY + 18f, headerText)
        }

        val entriesByDay = entries.groupBy { it.day.trim().lowercase() }
        days.forEachIndexed { dIdx, dayName ->
            val y = startY + headerH + dIdx * rowH
            canvas.drawRect(margin, y, margin + dayColW, y + rowH, headerFill)
            canvas.drawRect(margin, y, margin + dayColW, y + rowH, cellStroke)
            canvas.drawText(dayName.take(3).uppercase(), margin + 14f, y + rowH / 2f + 4f, headerText)

            val dayList = entriesByDay[dayName.lowercase()].orEmpty()
            for (p in 0 until periods) {
                val x = margin + dayColW + p * colW
                val slot = dayList.firstOrNull { it.periodIndex == p }
                if (slot != null && slot.isPractical) {
                    canvas.drawRoundRect(RectF(x, y, x + colW, y + rowH), 2f, 2f, practicalFill)
                }
                canvas.drawRect(x, y, x + colW, y + rowH, cellStroke)
                if (slot != null) {
                    val subj = slot.subjectTitle.take(15)
                    val code = slot.subjectCode.take(8)
                    val room = if (slot.roomNumber.isNotBlank()) "Rm ${slot.roomNumber}" else ""
                    canvas.drawText(subj, x + 5f, y + 18f, cellBold)
                    canvas.drawText(code, x + 5f, y + 32f, cellText)
                    if (room.isNotBlank()) {
                        canvas.drawText(room, x + 5f, y + 46f, cellText)
                    }
                }
            }
        }

        doc.finishPage(page)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = ctx.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext Result.failure(IllegalStateException("Could not create Downloads entry"))
            resolver.openOutputStream(uri)?.use { out ->
                doc.writeTo(out)
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            Result.success("Downloads/$fileName")
        } else {
            val dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: ctx.filesDir
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, fileName)
            FileOutputStream(outFile).use { out ->
                doc.writeTo(out)
            }
            Result.success(outFile.absolutePath)
        }
    } catch (e: Exception) {
        Result.failure(e)
    } finally {
        doc.close()
    }
}
