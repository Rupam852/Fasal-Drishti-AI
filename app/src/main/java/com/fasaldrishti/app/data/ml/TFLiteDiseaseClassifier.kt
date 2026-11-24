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
    val top3: List<Pair<String, Float>>
)

class TFLiteDiseaseClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private val modelFileName = "model_4_mobilenet_finetuned.tflite"

    init {
        loadLabels()
        loadModel()
    }

    private fun loadLabels() {
        try {
            labels = context.assets.open("labels.txt").bufferedReader().useLines { it.toList() }
            Log.d(TAG, "Loaded ${labels.size} disease classification labels")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels.txt", e)
        }
    }

    private fun loadModel() {
        try {
            val buffer = FileUtil.loadMappedFile(context, modelFileName)
            val cpuOptions = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(false) // Pure XNNPACK CPU acceleration - rock-solid on all devices
            }
            interpreter = Interpreter(buffer, cpuOptions)
            Log.d(TAG, "Successfully initialized On-Device TFLite model ($modelFileName) with CPU XNNPACK")
        } catch (e: Throwable) {
            Log.e(TAG, "Critical failure loading TFLite model ($modelFileName)", e)
            interpreter = null
        }
    }

    /**
     * Fast Computer Vision Foliage & Detail Quality Guard (< 3ms).
     * Prevents empty grey walls, foggy blurs, covered camera lenses, and
     * non-plant flat surfaces from falsely triggering high-confidence disease diagnoses.
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
            var totalSat = 0.0
            var greenPixelCount = 0

            for (color in pixels) {
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF

                // Luminance
                val gray = 0.299 * r + 0.587 * g + 0.114 * b
                sumGray += gray
                sumSqGray += gray * gray

                // Saturation: (max - min) / max
                val max = maxOf(r, maxOf(g, b))
                val min = minOf(r, minOf(g, b))
                if (max > 12) {
                    val sat = (max - min).toDouble() / max.toDouble()
                    totalSat += sat
                }

                // Excess Green vegetation index (ExG = 2G - R - B)
                val exg = 2 * g - r - b
                if (exg > 12) {
                    greenPixelCount++
                }
            }

            val meanGray = sumGray / totalPixels
            val variance = (sumSqGray / totalPixels) - (meanGray * meanGray)
            val stdDev = if (variance > 0) Math.sqrt(variance) else 0.0
            val meanSat = totalSat / totalPixels
            val greenRatio = greenPixelCount.toDouble() / totalPixels

            // A. Completely blank / uniform / camera covered / flat wall (StdDev < 16.0)
            val isBlankOrUniform = stdDev < 16.0
            // B. Monochromatic grey/white/black surface with no plant pigment (Sat < 0.12 && Green < 0.08)
            val isMonochromeNonPlant = (meanSat < 0.12 && greenRatio < 0.08)

            if (isBlankOrUniform || isMonochromeNonPlant) {
                Log.w(TAG, "CV Foliage Guard: Non-plant surface detected (StdDev=%.1f, Sat=%.3f, Green=%.3f)".format(stdDev, meanSat, greenRatio))
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

    fun classifyBitmap(bitmap: Bitmap): OnDevicePrediction {
        if (bitmap.isRecycled) {
            Log.e(TAG, "classifyBitmap: Provided bitmap is recycled")
            return OnDevicePrediction("Invalid_Crop", 0.0f, emptyList())
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
                top3 = listOf("Invalid_Crop" to 0.08f)
            )
        }

        if (interpreter == null) {
            loadModel()
        }
        if (labels.isEmpty()) {
            loadLabels()
        }

        // 2. Neural Network Inference
        if (interpreter != null && labels.isNotEmpty()) {
            try {
                // Preprocess Bitmap to 224x224 RGB
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                    .build()

                var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
                tensorImage.load(safeBitmap)
                tensorImage = imageProcessor.process(tensorImage)

                // Direct native 2D float array (zero buffer position / byte underflow issues)
                val outputScores = Array(1) { FloatArray(labels.size) }
                interpreter?.run(tensorImage.buffer.rewind(), outputScores)

                // Extract and rank softmax probabilities
                val probabilities = outputScores[0]
                val indexedScores = probabilities.indices.map { idx ->
                    val label = if (idx < labels.size) labels[idx] else "Class_$idx"
                    label to probabilities[idx]
                }.sortedByDescending { it.second }

                val best = indexedScores.firstOrNull() ?: ("Invalid_Crop" to 0.0f)
                val top3 = indexedScores.take(3)

                return OnDevicePrediction(
                    predictedClass = best.first,
                    confidence = best.second,
                    top3 = top3
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Inference exception: ${e.message}", e)
            }
        }

        // Return invalid crop if model is uninitialized or threw an exception
        return OnDevicePrediction(
            predictedClass = "Invalid_Crop",
            confidence = 0.0f,
            top3 = emptyList()
        )
    }

    fun close() {
        interpreter?.close()
    }

    companion object {
        private const val TAG = "FasalTFLiteClassifier"
    }
}
