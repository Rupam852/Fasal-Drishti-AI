package com.fasaldrishti.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.fasaldrishti.app.domain.model.ScanRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfPrescriptionGenerator {

    fun generatePrescriptionPdf(context: Context, scan: ScanRecord): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(scan.timestamp))

        // 1. Header Banner
        paint.color = Color.parseColor("#052e16") // Emerald Deep
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.parseColor("#10b981") // Emerald Primary
        canvas.drawRect(0f, 100f, 595f, 106f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FASAL DRISHTI AI", 30f, 45f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#a7f3d0")
        canvas.drawText("Precision Agronomy & Crop Vision Medical Dossier", 30f, 65f, paint)
        canvas.drawText("Date: $dateStr • Report ID: #${scan.id.take(8).uppercase()}", 30f, 82f, paint)

        var y = 135f

        // 2. Patient / Crop Diagnosis Card with Plant Photo
        val cardHeight = 115f
        paint.color = Color.parseColor("#f1f5f9")
        canvas.drawRoundRect(25f, y, 570f, y + cardHeight, 12f, 12f, paint)

        // Scanned Plant Photo Loading & Embedding
        var photoDrawn = false
        val photoSize = 95f
        val photoX = 455f
        val photoY = y + 10f

        if (scan.imageUrl.isNotBlank()) {
            try {
                val cleanPath = scan.imageUrl.removePrefix("file://")
                val imgFile = File(cleanPath)
                if (imgFile.exists()) {
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(imgFile.absolutePath, boundsOptions)
                    var sampleSize = 1
                    while (boundsOptions.outWidth / (sampleSize * 2) >= 200) {
                        sampleSize *= 2
                    }
                    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    val rawBmp = BitmapFactory.decodeFile(imgFile.absolutePath, decodeOptions)
                    if (rawBmp != null) {
                        val scaledBmp = Bitmap.createScaledBitmap(rawBmp, photoSize.toInt(), photoSize.toInt(), true)

                        // Draw photo frame background
                        paint.color = Color.parseColor("#cbd5e1")
                        canvas.drawRoundRect(photoX - 2f, photoY - 2f, photoX + photoSize + 2f, photoY + photoSize + 2f, 8f, 8f, paint)

                        // Draw photo
                        canvas.drawBitmap(scaledBmp, photoX, photoY, null)

                        // Photo subtitle
                        paint.color = Color.parseColor("#64748b")
                        paint.textSize = 7.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("SCANNED SPECIMEN", photoX + 6f, photoY + photoSize + 11f, paint)
                        photoDrawn = true
                    }
                }
            } catch (_: Exception) {
                photoDrawn = false
            }
        }

        paint.color = Color.parseColor("#0f172a")
        paint.textSize = 13.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DIAGNOSIS & CROP HEALTH SUMMARY", 40f, y + 24f, paint)

        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#334155")
        canvas.drawText("Target Crop: ${scan.cropName.ifBlank { "Crop Leaf" }}", 40f, y + 46f, paint)
        canvas.drawText("Identified Disease: ${scan.diseaseName.ifBlank { scan.predictedClass }}", 40f, y + 66f, paint)

        val confPct = (scan.confidence * 100).toInt()
        val severityColor = when (scan.severity.lowercase()) {
            "high", "severe" -> Color.parseColor("#ef4444")
            "medium", "moderate" -> Color.parseColor("#f59e0b")
            else -> Color.parseColor("#10b981")
        }
        paint.color = severityColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val statsX = if (photoDrawn) 275f else 340f
        canvas.drawText("Severity: ${scan.severity.ifBlank { "Moderate" }.uppercase()}", statsX, y + 46f, paint)
        paint.color = Color.parseColor("#0284c7")
        canvas.drawText("AI Confidence: $confPct%", statsX, y + 66f, paint)

        // Verification Badge
        paint.textSize = 10f
        if (scan.isVerified) {
            paint.color = Color.parseColor("#059669")
            canvas.drawText("✓ Verified with 2nd Multimodal AI", 40f, y + 88f, paint)
        } else {
            paint.color = Color.parseColor("#d97706")
            canvas.drawText("⚡ On-Device Rapid Field Screening", 40f, y + 88f, paint)
        }

        y += cardHeight + 20f

        // 3. Observed Symptoms
        paint.color = Color.parseColor("#0f172a")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("OBSERVED SYMPTOMS & PATHOLOGY", 30f, y, paint)
        y += 18f

        paint.color = Color.parseColor("#475569")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val symptomsText = scan.symptoms.ifBlank { "Irregular necrotic brown lesions with chlorotic yellow halo across leaf blades and margins." }
        val symptomLines = wrapText(symptomsText, 535f, paint)
        for (line in symptomLines.take(4)) {
            canvas.drawText("• $line", 35f, y, paint)
            y += 15f
        }

        y += 15f

        // 4. Chemical Spray Prescription & Dosage (Dawaai ka Ghol)
        paint.color = Color.parseColor("#ecfdf5")
        canvas.drawRoundRect(25f, y, 570f, y + 160f, 12f, 12f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#10b981")
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(25f, y, 570f, y + 160f, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#065f46")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🧪 RX: CHEMICAL SPRAY PRESCRIPTION & DOSAGE GUIDE", 40f, y + 25f, paint)

        paint.color = Color.parseColor("#047857")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("(Show this exact dosage formulation to your certified Krishi Kendra / Agri Shop)", 40f, y + 42f, paint)

        paint.color = Color.parseColor("#0f172a")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val treatmentText = scan.treatment.ifBlank { "Spray Mancozeb 75% WP @ 2.5g/L or Azoxystrobin 18.2% + Difenoconazole 11.4% SC @ 1ml/L of water. Ensure thorough canopy coverage during calm morning hours." }
        val treatLines = wrapText(treatmentText, 510f, paint)
        var treatY = y + 65f
        for (line in treatLines.take(6)) {
            canvas.drawText("✓ $line", 40f, treatY, paint)
            treatY += 15f
        }

        y += 180f

        // 5. Organic Treatment & Prevention
        paint.color = Color.parseColor("#0f172a")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🌿 ORGANIC REMEDY & SOIL PREVENTION", 30f, y, paint)
        y += 18f

        paint.color = Color.parseColor("#475569")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val organicTips = listOf(
            "Neem Oil Formulation: Spray cold-pressed 10,000 PPM Neem Oil @ 3ml/L with mild surfactant.",
            "Trichoderma viride: Soil drench @ 5g/L around root zones to suppress fungal inoculum.",
            "Field Hygiene: Remove heavily infected lower leaves and burn or bury far from field borders."
        )
        for (tip in organicTips) {
            canvas.drawText("• $tip", 35f, y, paint)
            y += 16f
        }

        y += 20f

        // 6. Certified Agronomist Advisory & Weather Notice
        paint.color = Color.parseColor("#fffbeb")
        canvas.drawRoundRect(25f, y, 570f, y + 65f, 10f, 10f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#f59e0b")
        canvas.drawRoundRect(25f, y, 570f, y + 65f, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#b45309")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("⚠️ CERTIFIED AGRONOMIST & SPRAY SAFETY ADVISORY", 40f, y + 20f, paint)

        paint.color = Color.parseColor("#78350f")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Confirm symptoms & tank dose with a certified KVK agronomist or Kisan Helpline (1800-180-1551) before purchase.", 40f, y + 37f, paint)
        canvas.drawText("Ideal Spray Time: 6:30 AM - 9:30 AM in calm weather. Do not spray during high winds (>15 km/h) or direct midday heat.", 40f, y + 52f, paint)

        // 7. Footer
        paint.color = Color.parseColor("#94a3b8")
        paint.textSize = 9f
        canvas.drawText("Generated by Fasal Drishti Multimodal AI Agronomist • www.fasaldrishti.app • Save Soil, Protect Harvest", 60f, 815f, paint)

        document.finishPage(page)

        return try {
            val dir = File(context.cacheDir, "prescriptions")
            if (!dir.exists()) dir.mkdirs()
            val pdfFile = File(dir, "Fasal_Drishti_Prescription_${scan.id.take(8)}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()
            pdfFile
        } catch (_: Exception) {
            document.close()
            null
        }
    }

    fun sharePrescription(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Fasal Drishti Crop Diagnosis & Spray Prescription")
            putExtra(Intent.EXTRA_TEXT, "🌾 Fasal Drishti AI Crop Prescription Report. Please find attached dosage and treatment details.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Prescription via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
