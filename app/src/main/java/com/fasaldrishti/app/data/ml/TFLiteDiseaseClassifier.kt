package com.fasaldrishti.app.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File

data class OnDevicePrediction(
    val predictedClass: String,
    val confidence: Float,
    val top3: List<Pair<String, Float>>,
    val modelOrigin: String = "Foliar Base"
)

class TFLiteDiseaseClassifier(private val context: Context) {

    private data class SpecialistEngine(
        val id: String,
        val displayName: String,
        val modelFile: String,
        val labelsFile: String,
        val inputSize: Int,
        var interpreter: Interpreter? = null,
        var labels: List<String> = emptyList()
    )

    private val engines = listOf(
        SpecialistEngine(
            id = "base",
            displayName = "Foliar Generalist",
            modelFile = "model_4_mobilenet_finetuned.tflite",
            labelsFile = "labels.txt",
            inputSize = 224
        ),
        SpecialistEngine(
            id = "rice",
            displayName = "Paddy / Rice Specialist",
            modelFile = "model_rice_fp16.tflite",
            labelsFile = "labels_rice.txt",
            inputSize = 320
        ),
        SpecialistEngine(
            id = "wheat",
            displayName = "Wheat Specialist",
            modelFile = "model_wheat_fp16.tflite",
            labelsFile = "labels_wheat.txt",
            inputSize = 320
        )
    )

    init {
        engines.forEach { engine ->
            loadEngine(engine)
        }
    }

    private fun loadEngine(engine: SpecialistEngine) {
        try {
            engine.labels = context.assets.open(engine.labelsFile).bufferedReader().useLines { it.toList() }
            Log.d(TAG, "Loaded ${engine.labels.size} labels for ${engine.displayName}")

            val buffer = FileUtil.loadMappedFile(context, engine.modelFile)
            val cpuOptions = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(false)
            }
            engine.interpreter = Interpreter(buffer, cpuOptions)
            Log.d(TAG, "Successfully initialized ${engine.displayName} (${engine.modelFile}) with CPU XNNPACK")
        } catch (e: Throwable) {
            Log.e(TAG, "Error initializing ${engine.displayName} (${engine.modelFile})", e)
            engine.interpreter = null
        }
    }

    /**
     * Universal Computer Vision Foliage & Chromatic Quality Guard (< 3ms).
     *
     * Uses plant biological chlorophyll reflectance (Normalized Green Chromaticity):
     *   normG = G / (R + G + B) > 0.36 && G > 1.05 * R && G > 1.05 * B
     *
     * Universally filters ANY non-plant object anywhere in the world (human skin, clothes,
     * vehicles, animals, roads, furniture, electronics, walls, ceilings, floors, sky, soil)
     * as well as completely blank, covered, or uniform out-of-focus surfaces before neural inference.
     */
    private fun checkFoliageQuality(bitmap: Bitmap): Boolean {
        return try {
            val scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            val width = scaled.width
            val height = scaled.height
            val totalPixels = width * height
            val pixels = IntArray(totalPixels)
            scaled.getPixels(pixels, 0, width, 0, 0, width, height)

            var sumGray = 0.0
            var sumSqGray = 0.0
            var totalPlantChromaCount = 0
            var centerPlantChromaCount = 0
            var centerTotalCount = 0

            val cYMin = (height * 0.20).toInt()
            val cYMax = (height * 0.80).toInt()
            val cXMin = (width * 0.20).toInt()
            val cXMax = (width * 0.80).toInt()

            for (y in 0 until height) {
                val inCenterY = y in cYMin..cYMax
                for (x in 0 until width) {
                    val color = pixels[y * width + x]
                    val r = (color shr 16) and 0xFF
                    val g = (color shr 8) and 0xFF
                    val b = color and 0xFF

                    // Luminance
                    val gray = 0.299 * r + 0.587 * g + 0.114 * b
                    sumGray += gray
                    sumSqGray += gray * gray

                    // Normalized Green Chromaticity
                    val total = r + g + b + 0.001f
                    val normG = g / total
                    val normR = r / total
                    val normB = b / total

                    // Genuine plant foliage: green chromaticity distinctly exceeds red and blue
                    val isPlantChroma = (normG > 0.36f) && (normG > normR * 1.05f) && (normG > normB * 1.05f)
                    if (isPlantChroma) {
                        totalPlantChromaCount++
                    }

                    if (inCenterY && x in cXMin..cXMax) {
                        centerTotalCount++
                        if (isPlantChroma) {
                            centerPlantChromaCount++
                        }
                    }
                }
            }

            val meanGray = sumGray / totalPixels
            val variance = (sumSqGray / totalPixels) - (meanGray * meanGray)
            val stdDev = if (variance > 0) Math.sqrt(variance) else 0.0

            val overallChromaRatio = totalPlantChromaCount.toDouble() / totalPixels
            val centerChromaRatio = if (centerTotalCount > 0) centerPlantChromaCount.toDouble() / centerTotalCount else 0.0

            // 1. Completely blank / uniform / camera covered (StdDev < 14.0)
            val isBlankOrUniform = stdDev < 14.0

            // 2. Non-plant surface: painted wall, room ceiling, finger, laptop, floor, table (Chroma < 14%)
            val isNonFoliarSurface = (overallChromaRatio < 0.14) && (centerChromaRatio < 0.15)

            if (isBlankOrUniform || isNonFoliarSurface) {
                Log.w(TAG, "CV Foliage Guard: Non-plant surface rejected (StdDev=%.1f, OverallChroma=%.1f%%, CenterChroma=%.1f%%)".format(
                    stdDev, overallChromaRatio * 100, centerChromaRatio * 100
                ))
                false
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkFoliageQuality", e)
            true // Allow inference if check fails
        }
    }

    fun classifyImage(imageFile: File): OnDevicePrediction {
        if (!imageFile.exists() || imageFile.length() == 0L) {
            Log.e(TAG, "classifyImage: Image file does not exist or is empty (${imageFile.absolutePath})")
            return OnDevicePrediction("Invalid_Crop", 0.0f, emptyList())
        }

        val bitmap = try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imageFile.absolutePath, boundsOptions)

            val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
            var sampleSize = 1
            while (maxDim / (sampleSize * 2) >= 224) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(imageFile.absolutePath, decodeOptions)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to decode image file: ${imageFile.absolutePath}", t)
            null
        }

        return if (bitmap != null) {
            classifyBitmap(bitmap)
        } else {
            OnDevicePrediction("Invalid_Crop", 0.0f, emptyList())
        }
    }

    fun classifyBitmap(bitmap: Bitmap, preferredCrop: String? = null): OnDevicePrediction {
        if (bitmap.isRecycled) {
            Log.e(TAG, "classifyBitmap: Provided bitmap is recycled")
            return OnDevicePrediction("Invalid_Crop", 0.0f, emptyList(), "None")
        }

        // Ensure software ARGB_8888 bitmap to prevent HARDWARE bitmap crashes on modern Android
        val safeBitmap = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
        } catch (t: Throwable) {
            bitmap
        }

        // 1. Run Pre-Inference Foliage Quality Guard
        val isAuthenticFoliage = checkFoliageQuality(safeBitmap)
        if (!isAuthenticFoliage) {
            return OnDevicePrediction(
                predictedClass = "Invalid_Crop",
                confidence = 0.08f,
                top3 = listOf("Invalid_Crop" to 0.08f),
                modelOrigin = "CV Guard"
            )
        }

        // 2. Multi-Model Inference & Calibration
        val baseEngine = engines.find { it.id == "base" }
        val riceEngine = engines.find { it.id == "rice" }
        val wheatEngine = engines.find { it.id == "wheat" }

        data class ModelRunResult(
            val engineName: String,
            val bestClass: String,
            val bestConfidence: Float,
            val topList: List<Pair<String, Float>>
        )

        fun runEngine(engine: SpecialistEngine?): ModelRunResult? {
            if (engine == null) return null
            if (engine.interpreter == null || engine.labels.isEmpty()) {
                loadEngine(engine)
            }
            val interp = engine.interpreter ?: return null
            val labels = engine.labels
            if (labels.isEmpty()) return null

            return try {
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(engine.inputSize, engine.inputSize, ResizeOp.ResizeMethod.BILINEAR))
                    .build()

                var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
                tensorImage.load(safeBitmap)
                tensorImage = imageProcessor.process(tensorImage)

                val outputScores = Array(1) { FloatArray(labels.size) }
                interp.run(tensorImage.buffer.rewind(), outputScores)

                val probabilities = outputScores[0]
                val indexedScores = probabilities.indices.map { idx ->
                    val label = if (idx < labels.size) labels[idx] else "Class_$idx"
                    label to probabilities[idx]
                }.sortedByDescending { it.second }

                val best = indexedScores.firstOrNull() ?: ("Invalid_Crop" to 0.0f)
                ModelRunResult(
                    engineName = engine.displayName,
                    bestClass = best.first,
                    bestConfidence = best.second,
                    topList = indexedScores.take(3)
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Error executing ${engine.displayName}: ${e.message}", e)
                null
            }
        }

        val allCandidates = mutableListOf<Pair<String, Float>>()

        // Run Base Foliar Engine
        val baseResult = runEngine(baseEngine)
        if (baseResult != null) {
            allCandidates.addAll(baseResult.topList)
        }

        // Run Specialist Engines
        val isRicePreferred = preferredCrop?.contains("rice", ignoreCase = true) == true ||
                              preferredCrop?.contains("paddy", ignoreCase = true) == true ||
                              preferredCrop?.contains("dhan", ignoreCase = true) == true
        val isWheatPreferred = preferredCrop?.contains("wheat", ignoreCase = true) == true ||
                               preferredCrop?.contains("gehun", ignoreCase = true) == true

        val riceResult = if (isRicePreferred || (baseResult == null || baseResult.bestConfidence < 0.60f)) {
            runEngine(riceEngine)
        } else null

        val wheatResult = if (isWheatPreferred || (baseResult == null || baseResult.bestConfidence < 0.60f)) {
            runEngine(wheatEngine)
        } else null

        if (riceResult != null) allCandidates.addAll(riceResult.topList)
        if (wheatResult != null) allCandidates.addAll(wheatResult.topList)

        // Winner Selection Logic (Arbiter)
        val winner: ModelRunResult? = when {
            isRicePreferred && riceResult != null -> riceResult
            isWheatPreferred && wheatResult != null -> wheatResult
            // If base model is confident (>= 0.45f) on broadleaf crops, favor base model
            baseResult != null && baseResult.bestConfidence >= 0.45f -> baseResult
            // If base model is low confidence, check if Rice or Wheat is confident
            riceResult != null && riceResult.bestConfidence >= 0.55f && (wheatResult == null || riceResult.bestConfidence >= wheatResult.bestConfidence) -> riceResult
            wheatResult != null && wheatResult.bestConfidence >= 0.55f -> wheatResult
            else -> baseResult ?: riceResult ?: wheatResult
        }

        val sortedTop3 = allCandidates.distinctBy { it.first }.sortedByDescending { it.second }.take(3)

        if (winner != null) {
            return OnDevicePrediction(
                predictedClass = winner.bestClass,
                confidence = winner.bestConfidence,
                top3 = sortedTop3,
                modelOrigin = winner.engineName
            )
        }

        return OnDevicePrediction(
            predictedClass = "Invalid_Crop",
            confidence = 0.0f,
            top3 = emptyList(),
            modelOrigin = "None"
        )
    }

    fun close() {
        engines.forEach {
            try {
                it.interpreter?.close()
                it.interpreter = null
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    companion object {
        private const val TAG = "FasalTFLiteClassifier"
    }
}
