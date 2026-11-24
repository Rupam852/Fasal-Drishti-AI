package com.fasaldrishti.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 points)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(scan.timestamp))

        val leftMargin = 28f
        val rightMargin = 567f
        val contentWidth = rightMargin - leftMargin // 539f

        // ==========================================
        // 1. TOP HEADER BANNER (y: 0 to 86)
        // ==========================================
        paint.color = Color.parseColor("#064e3b") // Deep Emerald
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, 595f, 84f, paint)

        // Accent strip
        paint.color = Color.parseColor("#10b981") // Vivid Emerald
        canvas.drawRect(0f, 84f, 595f, 88f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FASAL DRISHTI AI", leftMargin, 34f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#a7f3d0")
        canvas.drawText("Precision Agronomy & Crop Vision Medical Dossier", leftMargin, 50f, paint)
        canvas.drawText("Digital Pathology Report & Spray Advisory for Agri Shops", leftMargin, 65f, paint)

        // Right side of Header: Report ID & Date
        val reportIdText = "REPORT ID: #${scan.id.take(8).uppercase()}"
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#6ee7b7")
        val reportIdWidth = paint.measureText(reportIdText)
        canvas.drawText(reportIdText, rightMargin - reportIdWidth, 34f, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#e2e8f0")
        val dateWidth = paint.measureText(dateStr)
        canvas.drawText(dateStr, rightMargin - dateWidth, 50f, paint)

        // ==========================================
        // 2. DIAGNOSIS & CROP HEALTH SUMMARY CARD
        // ==========================================
        var curY = 100f
        val diagCardHeight = 112f
        val diagCardRect = RectF(leftMargin, curY, rightMargin, curY + diagCardHeight)

        // Background
        paint.color = Color.parseColor("#f8fafc")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(diagCardRect, 10f, 10f, paint)

        // Border
        paint.color = Color.parseColor("#cbd5e1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(diagCardRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Embedded Specimen Photo on the right side
        val photoSize = 84f
        val photoX = rightMargin - photoSize - 12f
        val photoY = curY + 12f
        var photoDrawn = false

        if (scan.imageUrl.isNotBlank()) {
            try {
                val cleanPath = scan.imageUrl.removePrefix("file://")
                val imgFile = File(cleanPath)
                if (imgFile.exists()) {
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(imgFile.absolutePath, boundsOptions)
                    var sampleSize = 1
                    while (boundsOptions.outWidth / (sampleSize * 2) >= 180) {
                        sampleSize *= 2
                    }
                    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    val rawBmp = BitmapFactory.decodeFile(imgFile.absolutePath, decodeOptions)
                    if (rawBmp != null) {
                        val scaledBmp = Bitmap.createScaledBitmap(rawBmp, photoSize.toInt(), photoSize.toInt(), true)

                        // Frame
                        paint.color = Color.parseColor("#e2e8f0")
                        canvas.drawRoundRect(photoX - 2f, photoY - 2f, photoX + photoSize + 2f, photoY + photoSize + 2f, 6f, 6f, paint)
                        canvas.drawBitmap(scaledBmp, photoX, photoY, null)

                        // Subtitle
                        paint.color = Color.parseColor("#64748b")
                        paint.textSize = 7f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        val caption = "SCANNED SPECIMEN"
                        val capWidth = paint.measureText(caption)
                        canvas.drawText(caption, photoX + (photoSize - capWidth) / 2f, photoY + photoSize + 10f, paint)
                        photoDrawn = true
                    }
                }
            } catch (_: Exception) {
                photoDrawn = false
            }
        }

        // Left Information Area (bounded to prevent collision with photo)
        val textBoundWidth = if (photoDrawn) (photoX - leftMargin - 24f) else (contentWidth - 24f)
        val infoStartX = leftMargin + 14f

        // Line 1: Target Crop & Severity Badge
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("TARGET CROP:", infoStartX, curY + 22f, paint)
        val cropLabelWidth = paint.measureText("TARGET CROP: ")

        paint.textSize = 10.5f
        paint.color = Color.parseColor("#0f172a")
        val cropName = scan.cropName.ifBlank { "Crop Leaf" }
        canvas.drawText(cropName, infoStartX + cropLabelWidth, curY + 22f, paint)
        val cropNameWidth = paint.measureText(cropName)

        // Severity Badge (drawn next to crop name)
        val sevBadgeX = infoStartX + cropLabelWidth + cropNameWidth + 14f
        val severityStr = scan.severity.ifBlank { "Moderate" }.uppercase()
        val (sevBgColor, sevTextColor) = when (severityStr) {
            "HIGH", "SEVERE" -> Pair("#fee2e2", "#b91c1c")
            "MEDIUM", "MODERATE" -> Pair("#fef3c7", "#b45309")
            else -> Pair("#d1fae5", "#047857")
        }
        val sevBadgeText = "SEV: $severityStr"
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val sevTextW = paint.measureText(sevBadgeText)
        val sevRect = RectF(sevBadgeX, curY + 11f, sevBadgeX + sevTextW + 12f, curY + 25f)
        paint.color = Color.parseColor(sevBgColor)
        canvas.drawRoundRect(sevRect, 4f, 4f, paint)
        paint.color = Color.parseColor(sevTextColor)
        canvas.drawText(sevBadgeText, sevBadgeX + 6f, curY + 22f, paint)

        // Line 2: Identified Condition Title
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#64748b")
        canvas.drawText("DIAGNOSED CONDITION / DISEASE:", infoStartX, curY + 41f, paint)

        // Line 3: Identified Disease Name (Wrapped within textBoundWidth so it NEVER overlaps!)
        paint.textSize = 12.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0f172a")
        val diseaseDisplayName = scan.diseaseName.ifBlank { scan.predictedClass }
        val diseaseLines = wrapSimple(diseaseDisplayName, textBoundWidth, paint)
        var diseaseY = curY + 58f
        for (line in diseaseLines.take(2)) {
            canvas.drawText(line, infoStartX, diseaseY, paint)
            diseaseY += 15f
        }

        // Line 4: Confidence Badge + Verification Pill
        val bottomBadgesY = curY + 98f

        // Confidence Pill
        val confPct = (scan.confidence * 100).toInt()
        val confBadgeText = "AI Match: $confPct%"
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val confTextW = paint.measureText(confBadgeText)
        val confRect = RectF(infoStartX, bottomBadgesY - 12f, infoStartX + confTextW + 14f, bottomBadgesY + 4f)
        paint.color = Color.parseColor("#e0f2fe") // light sky
        canvas.drawRoundRect(confRect, 4f, 4f, paint)
        paint.color = Color.parseColor("#0369a1")
        canvas.drawText(confBadgeText, infoStartX + 7f, bottomBadgesY, paint)

        // Verification Pill
        val verBadgeX = infoStartX + confTextW + 22f
        val verText = if (scan.isVerified) "✓ Verified with 2nd Multimodal AI" else "⚡ Rapid On-Device Screening"
        val (verBg, verColor) = if (scan.isVerified) Pair("#dcfce7", "#15803d") else Pair("#ffedd5", "#c2410c")
        val verTextW = paint.measureText(verText)
        val verRect = RectF(verBadgeX, bottomBadgesY - 12f, verBadgeX + verTextW + 14f, bottomBadgesY + 4f)
        paint.color = Color.parseColor(verBg)
        canvas.drawRoundRect(verRect, 4f, 4f, paint)
        paint.color = Color.parseColor(verColor)
        canvas.drawText(verText, verBadgeX + 7f, bottomBadgesY, paint)

        curY += diagCardHeight + 14f

        // ==========================================
        // 3. OBSERVED SYMPTOMS & PATHOLOGY
        // ==========================================
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1e293b")
        canvas.drawText("📋 OBSERVED PATHOLOGY & SYMPTOMS", leftMargin + 4f, curY, paint)
        curY += 14f

        val rawSymptoms = scan.symptoms.ifBlank {
            "Concentric dark spots, chlorotic foliar lesions, and tissue necrosis observed on leaf surface."
        }
        val symptomPoints = splitIntoBullets(rawSymptoms)
        for (item in symptomPoints.take(3)) {
            curY = drawWrappedBullet(
                canvas = canvas,
                bullet = "•",
                text = item,
                startX = leftMargin + 8f,
                startY = curY,
                maxWidth = contentWidth - 16f,
                paint = paint,
                lineHeight = 13.5f,
                textColor = Color.parseColor("#334155")
            ) + 2f
        }

        curY += 8f

        // ==========================================
        // 4. RX: CHEMICAL SPRAY PRESCRIPTION BOX
        // ==========================================
        val chemBoxStartY = curY
        val rawTreatment = scan.treatment.ifBlank {
            "• Recommended Chemical: Mancozeb 75% WP @ 2.5g/L or Azoxystrobin 18.2% + Difenoconazole 11.4% SC @ 1ml/L.\n• 15-Liter Knapsack Tank Dose: 35-40g per 15-liter pump.\n• Spray Timing: Early morning or late evening."
        }
        // Extract ONLY the chemical portion before [ORGANIC]
        val chemicalPart = rawTreatment.substringBefore("[ORGANIC]").trim()
        val chemicalItems = splitIntoBullets(chemicalPart)

        // Calculate height dynamically
        var estimatedChemHeight = 38f
        for (item in chemicalItems) {
            val isTankDose = item.contains("15L", ignoreCase = true) ||
                    item.contains("15-Liter", ignoreCase = true) ||
                    item.contains("Tank Dose", ignoreCase = true) ||
                    item.contains("लीटर", ignoreCase = true)
            val subLines = wrapSimple(item, contentWidth - 36f, paint)
            estimatedChemHeight += (subLines.size * 13.5f) + (if (isTankDose) 8f else 4f)
        }
        val chemCardHeight = estimatedChemHeight.coerceIn(120f, 195f)

        val chemRect = RectF(leftMargin, chemBoxStartY, rightMargin, chemBoxStartY + chemCardHeight)
        paint.color = Color.parseColor("#f0fdf4") // Light mint green fill
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(chemRect, 8f, 8f, paint)

        // Green border
        paint.color = Color.parseColor("#86efac")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(chemRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header Title
        paint.color = Color.parseColor("#065f46")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🧪 RX: CHEMICAL SPRAY PRESCRIPTION & DOSAGE GUIDE", leftMargin + 12f, chemBoxStartY + 18f, paint)

        paint.color = Color.parseColor("#047857")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("(Show this exact formulation & active salt to your certified Krishi Kendra / Agri Shop)", leftMargin + 12f, chemBoxStartY + 30f, paint)

        var chemItemY = chemBoxStartY + 45f
        for (item in chemicalItems) {
            if (chemItemY > chemBoxStartY + chemCardHeight - 14f) break
            val isTankDose = item.contains("15L", ignoreCase = true) ||
                    item.contains("15-Liter", ignoreCase = true) ||
                    item.contains("Tank Dose", ignoreCase = true) ||
                    item.contains("लीटर", ignoreCase = true)

            if (isTankDose) {
                // Highlight Tank Dose with a distinct badge look
                paint.color = Color.parseColor("#047857")
                chemItemY = drawWrappedBullet(
                    canvas = canvas,
                    bullet = "💧",
                    text = item,
                    startX = leftMargin + 12f,
                    startY = chemItemY,
                    maxWidth = contentWidth - 24f,
                    paint = paint,
                    lineHeight = 13.5f,
                    textColor = Color.parseColor("#064e3b"),
                    isBold = true
                ) + 4f
            } else {
                chemItemY = drawWrappedBullet(
                    canvas = canvas,
                    bullet = "✓",
                    text = item,
                    startX = leftMargin + 12f,
                    startY = chemItemY,
                    maxWidth = contentWidth - 24f,
                    paint = paint,
                    lineHeight = 13.5f,
                    textColor = Color.parseColor("#1e293b")
                ) + 3f
            }
        }

        curY = chemBoxStartY + chemCardHeight + 12f

        // ==========================================
        // 5. ORGANIC REMEDIES & FIELD SANITATION
        // ==========================================
        val organicBoxStartY = curY
        val organicPart = rawTreatment.substringAfter("[ORGANIC]", "").trim()
        val organicItems = if (organicPart.isNotBlank()) {
            splitIntoBullets(organicPart)
        } else {
            listOf(
                "Organic Bio-Fungicide: Spray cold-pressed 10,000 PPM Neem Oil @ 3-5ml/L or Trichoderma viride @ 5g/L as preventative foliage wash.",
                "Crop Sanitation: Promptly rogue and destroy heavily infected lower foliage; do not compost blighted leaves.",
                "Field Hygiene: Maintain optimal plant spacing, proper drainage channels, and avoid overhead sprinkler splash."
            )
        }

        var estimatedOrgHeight = 36f
        for (item in organicItems) {
            val subLines = wrapSimple(item, contentWidth - 36f, paint)
            estimatedOrgHeight += (subLines.size * 13f) + 4f
        }
        val organicCardHeight = estimatedOrgHeight.coerceIn(95f, 155f)

        val orgRect = RectF(leftMargin, organicBoxStartY, rightMargin, organicBoxStartY + organicCardHeight)
        paint.color = Color.parseColor("#f8fafc")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(orgRect, 8f, 8f, paint)

        // Slate/Teal border
        paint.color = Color.parseColor("#cbd5e1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(orgRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header Title
        paint.color = Color.parseColor("#0f766e") // Teal
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🌿 ORGANIC REMEDIES & FIELD HYGIENE", leftMargin + 12f, organicBoxStartY + 18f, paint)

        paint.color = Color.parseColor("#0d9488")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("(Bio-fungicides, natural resistance boosters & cultural farm practices)", leftMargin + 12f, organicBoxStartY + 29f, paint)

        var orgItemY = organicBoxStartY + 43f
        for (item in organicItems) {
            if (orgItemY > organicBoxStartY + organicCardHeight - 12f) break
            orgItemY = drawWrappedBullet(
                canvas = canvas,
                bullet = "•",
                text = item,
                startX = leftMargin + 12f,
                startY = orgItemY,
                maxWidth = contentWidth - 24f,
                paint = paint,
                lineHeight = 13f,
                textColor = Color.parseColor("#334155")
            ) + 3f
        }

        curY = organicBoxStartY + organicCardHeight + 10f

        // ==========================================
        // 6. CERTIFIED AGRONOMIST SAFETY ADVISORY
        // ==========================================
        val advBoxRect = RectF(leftMargin, curY, rightMargin, curY + 54f)
        paint.color = Color.parseColor("#fffbeb") // Warm light amber
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(advBoxRect, 6f, 6f, paint)

        paint.color = Color.parseColor("#fcd34d")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(advBoxRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#92400e")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("⚠️ CERTIFIED AGRONOMIST & SPRAY SAFETY ADVISORY", leftMargin + 12f, curY + 16f, paint)

        paint.color = Color.parseColor("#78350f")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("• Ideal Spray Time: 6:30 AM - 9:30 AM or 4:30 PM - 6:30 PM in calm weather (<10 km/h wind). Never spray in midday sun.", leftMargin + 12f, curY + 29f, paint)
        canvas.drawText("• Farm Safety: Wear gloves & mask. Ensure recommended Pre-Harvest Interval (PHI) before picking produce.", leftMargin + 12f, curY + 40f, paint)
        canvas.drawText("• Kisan Helpline: For free government agronomist consultation, dial toll-free 1800-180-1551 (KVK / ICAR).", leftMargin + 12f, curY + 50f, paint)

        // ==========================================
        // 7. FOOTER & DISCLAIMER (Fixed at page bottom)
        // ==========================================
        val footerY = 820f
        paint.color = Color.parseColor("#e2e8f0")
        canvas.drawLine(leftMargin, footerY - 10f, rightMargin, footerY - 10f, paint)

        paint.color = Color.parseColor("#94a3b8")
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Generated by Fasal Drishti Multimodal AI Agronomist • www.fasaldrishti.app • Save Soil, Protect Harvest", leftMargin, footerY, paint)

        val pageInfoText = "Digital Dossier • Valid for Season 2026"
        val pageInfoWidth = paint.measureText(pageInfoText)
        canvas.drawText(pageInfoText, rightMargin - pageInfoWidth, footerY, paint)

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
            putExtra(Intent.EXTRA_TEXT, "🌾 Fasal Drishti AI Crop Prescription Report. Attached is the exact chemical dosage, organic care, and application schedule.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Prescription via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Splits raw multi-line strings or dot-delimited bullets into clean independent items.
     */
    private fun splitIntoBullets(rawText: String): List<String> {
        val lines = rawText.split("\n")
        val items = mutableListOf<String>()

        for (line in lines) {
            val cleanLine = line.trim()
            if (cleanLine.isBlank()) continue

            // Check if line contains inline bullet delimiters like "• " or "✓ "
            if (cleanLine.contains(" • ")) {
                val subItems = cleanLine.split(" • ")
                for (sub in subItems) {
                    val s = cleanBulletSymbol(sub)
                    if (s.isNotBlank()) items.add(s)
                }
            } else {
                val s = cleanBulletSymbol(cleanLine)
                if (s.isNotBlank()) items.add(s)
            }
        }
        return items
    }

    private fun cleanBulletSymbol(text: String): String {
        return text
            .removePrefix("•")
            .removePrefix("✓")
            .removePrefix("-")
            .removePrefix("*")
            .trim()
    }

    /**
     * Draws a bullet item with hanging indentation so that wrapped lines
     * align neatly with the text rather than under the bullet symbol.
     * Optionally draws label prefix (e.g. "Recommended Chemical:") in bold.
     */
    private fun drawWrappedBullet(
        canvas: Canvas,
        bullet: String,
        text: String,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float = 13.5f,
        textColor: Int = Color.parseColor("#1e293b"),
        isBold: Boolean = false
    ): Float {
        // Draw bullet symbol
        paint.color = if (bullet == "💧") Color.parseColor("#0284c7") else if (bullet == "✓") Color.parseColor("#059669") else Color.parseColor("#64748b")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(bullet, startX, startY, paint)

        val indentX = startX + 12f
        val textAvailWidth = maxWidth - 14f

        paint.color = textColor
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)

        val words = text.split(" ")
        val wrappedLines = mutableListOf<String>()
        var curLine = ""

        for (w in words) {
            val test = if (curLine.isEmpty()) w else "$curLine $w"
            if (paint.measureText(test) <= textAvailWidth) {
                curLine = test
            } else {
                if (curLine.isNotEmpty()) wrappedLines.add(curLine)
                curLine = w
            }
        }
        if (curLine.isNotEmpty()) wrappedLines.add(curLine)

        var y = startY
        for (line in wrappedLines) {
            // Check if line contains a label ending in ":"
            if (line.contains(":") && !isBold) {
                val colonIdx = line.indexOf(":")
                val label = line.substring(0, colonIdx + 1)
                val rest = line.substring(colonIdx + 1)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(label, indentX, y, paint)
                val labelWidth = paint.measureText(label)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(rest, indentX + labelWidth, y, paint)
            } else {
                canvas.drawText(line, indentX, y, paint)
            }
            y += lineHeight
        }

        return y
    }

    private fun wrapSimple(text: String, maxWidth: Float, paint: Paint): List<String> {
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
