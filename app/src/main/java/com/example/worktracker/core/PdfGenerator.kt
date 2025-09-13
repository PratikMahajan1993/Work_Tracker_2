package com.example.worktracker.core

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.worktracker.data.database.entity.WorkActivityLog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PdfGenerator {

    private const val FONT_SIZE_TITLE = 18f
    private const val FONT_SIZE_BODY = 12f
    private const val MARGIN_HORIZONTAL = 50f
    private const val MARGIN_VERTICAL = 72f
    private const val LINE_SPACING_FACTOR = 1.3f
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
    private val fileDateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generatePdfFromLog(context: Context, log: WorkActivityLog): Boolean {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = FONT_SIZE_TITLE
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        val bodyPaint = Paint().apply {
            textSize = FONT_SIZE_BODY
            color = android.graphics.Color.DKGRAY
        }
        val labelPaint = Paint().apply {
            textSize = FONT_SIZE_BODY
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        var yPosition = MARGIN_VERTICAL

        canvas.drawText("Work Activity Log Details", (PAGE_WIDTH / 2).toFloat(), yPosition, titlePaint)
        yPosition += (FONT_SIZE_TITLE * LINE_SPACING_FACTOR * 2.5f)

        fun drawDetailLine(label: String, value: String?) {
            if (yPosition > PAGE_HEIGHT - MARGIN_VERTICAL) {
                return
            }
            val nonNullValue = value ?: "N/A"
            canvas.drawText(label, MARGIN_HORIZONTAL, yPosition, labelPaint)
            val labelWidth = labelPaint.measureText(label)
            canvas.drawText(nonNullValue, MARGIN_HORIZONTAL + labelWidth + 10f, yPosition, bodyPaint)
            yPosition += (FONT_SIZE_BODY * LINE_SPACING_FACTOR)
        }

        fun drawMultiLineDetail(label: String, value: String?) {
            if (yPosition > PAGE_HEIGHT - MARGIN_VERTICAL) return
            val nonNullValue = value ?: "N/A"
            canvas.drawText(label, MARGIN_HORIZONTAL, yPosition, labelPaint)
            yPosition += (FONT_SIZE_BODY * LINE_SPACING_FACTOR)

            val textWidth = PAGE_WIDTH - (2 * MARGIN_HORIZONTAL)
            var currentLine = ""
            for (word in nonNullValue.split(" ")) {
                if (bodyPaint.measureText("$currentLine $word") < textWidth) {
                    currentLine += "$word "
                } else {
                    canvas.drawText(currentLine.trim(), MARGIN_HORIZONTAL, yPosition, bodyPaint)
                    yPosition += (FONT_SIZE_BODY * LINE_SPACING_FACTOR)
                    if (yPosition > PAGE_HEIGHT - MARGIN_VERTICAL) return
                    currentLine = "$word "
                }
            }
            if (currentLine.isNotBlank()) {
                canvas.drawText(currentLine.trim(), MARGIN_HORIZONTAL, yPosition, bodyPaint)
                yPosition += (FONT_SIZE_BODY * LINE_SPACING_FACTOR)
            }
            yPosition += (FONT_SIZE_BODY * LINE_SPACING_FACTOR * 0.5f)
        }

        drawDetailLine("Category:", log.categoryName)
        // Corrected: Ensure startTime is not null before formatting (it's non-nullable in Entity)
        drawDetailLine("Start Time:", dateFormatter.format(Date(log.startTime)))
        log.endTime?.let { drawDetailLine("End Time:", dateFormatter.format(Date(it))) }

        val durationString = if (log.endTime != null /*&& log.startTime != null is implied*/) {
            val diff = log.endTime - log.startTime // startTime is non-nullable
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            String.format("%02dh %02dm %02ds", hours, minutes, seconds)
        } else /*if (log.startTime != null)*/ { // startTime is non-nullable
            "Ongoing"
        } 
        // Removed else N/A as startTime is non-nullable so one of the above conditions must be true
        drawDetailLine("Duration:", durationString)
        // Corrected: logDate is non-nullable in Entity
        drawDetailLine("Log Date:", dateFormatter.format(Date(log.logDate)))

        drawMultiLineDetail("Description:", log.description.ifBlank { "N/A" })

        // Corrected: Compare Int with Int for operatorId
        drawDetailLine("Operator ID:", if (log.operatorId != 0) log.operatorId.toString() else "N/A")
        drawDetailLine("Expenses (Rs):", log.expenses.toString())
        drawDetailLine("Task Successful:", log.taskSuccessful?.let { if(it) "Yes" else "No" } ?: "N/A")
        drawDetailLine("Assigned By:", log.assignedBy?.ifBlank { "N/A" })

        pdfDocument.finishPage(page)

        val fileName = "WorkLog_${log.categoryName.replace("\\s+".toRegex(), "_")}_${fileDateFormatter.format(Date())}.pdf"
        try {
            val fos: FileOutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                fos = uri?.let { resolver.openOutputStream(it) } as? FileOutputStream
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = File(downloadsDir, fileName)
                fos = FileOutputStream(file)
            }

            fos.use { outputStream ->
                if (outputStream == null) throw IOException("Failed to get output stream.")
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            return false
        }
    }
}
