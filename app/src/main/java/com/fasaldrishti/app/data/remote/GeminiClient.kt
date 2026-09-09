package com.fasaldrishti.app.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Google Gemini Multimodal Vision & Agronomy Advisory Client (Primary Cloud AI).
 * Backed by Supabase Remote Config for secure, zero-hardcode key updates.
 */
class GeminiClient(private val supabaseManager: SupabaseManager? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    /**
     * Resizes and compresses image to ~1024px and ~150KB JPEG for ultra-fast <500ms Gemini Vision inference.
     */
    private fun compressAndEncodeImage(imageFile: File): String {
        val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: return Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)

        val width = originalBitmap.width
        val height = originalBitmap.height
        val maxDim = max(width, height)
        val targetDim = 1024

        val resizedBitmap = if (maxDim > targetDim) {
            val scale = targetDim.toFloat() / maxDim
            val newW = (width * scale).toInt()
            val newH = (height * scale).toInt()
            Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    /**
     * Primary Multimodal AI Dual-Layer Verification:
     * Cross-examines the uploaded crop leaf image with Google Gemini Vision,
     * strictly validates plant authenticity, eliminates non-crop objects, and computes exact spray dosages.
     */
    suspend fun verifyCropDiagnosis(
        imageFile: File,
        initialCropName: String? = null,
        initialDiseaseName: String? = null,
        initialConfidence: Float? = null
    ): Result<FallbackScanDiagnosis> = withContext(Dispatchers.IO) {
        try {
            val apiKey = supabaseManager?.getRemoteConfig("gemini_api_key", "") ?: ""
            val modelName = supabaseManager?.getRemoteConfig("gemini_model_name", "gemini-flash-latest") ?: "gemini-flash-latest"

            if (apiKey.isBlank() || !imageFile.exists()) {
                return@withContext Result.failure(Exception("Gemini API key not configured or image missing"))
            }

            val base64Image = compressAndEncodeImage(imageFile)

            val initialContext = if (!initialCropName.isNullOrBlank()) {
                """
                Preliminary On-Device Vision Scan:
                - Preliminary Crop: $initialCropName
                - Preliminary Disease: ${initialDiseaseName ?: "Unknown"}
                - Preliminary Confidence: ${((initialConfidence ?: 0.5f) * 100).toInt()}%
                """.trimIndent()
            } else {
                "No preliminary on-device diagnosis available."
            }

            val prompt = """
                You are 'Fasal Drishti AI' Senior Multimodal Agronomist & Chief Plant Pathologist.
                Perform Strict, High-Precision Multimodal Verification on this uploaded photo.

                $initialContext

                MANDATORY RULES:
                RULE 1 - SUBJECT VALIDATION (ZERO FALSE POSITIVES):
                Examine if the image contains an authentic agricultural crop leaf, plant foliage, farm vegetable, or crop fruit.
                If the image is:
                - A human person, face, selfie, hand, arm, body, clothes, or shoes
                - A room, wall, ceiling, floor, furniture, table, chair, or bed
                - A vehicle, car, bike, or street
                - A pet, dog, cat, insect, or animal
                - An electronic screen, laptop, monitor, keyboard, or book/paper
                - A random non-agricultural item or completely unreadable blur
                
                You MUST return this exact JSON:
                {
                  "is_plant": false,
                  "crop_name": "Non-Crop Object",
                  "disease_name": "No Plant Leaf Detected",
                  "severity": "Invalid",
                  "confidence": 0.10,
                  "symptoms": "AI Vision checked the photo: No agricultural crop leaf or plant was detected. The photo appears to be a person, room, object, or non-plant background.",
                  "treatment": "Please align a real diseased agricultural crop leaf inside the camera reticle in good lighting and scan again."
                }

                RULE 2 - ACCURATE PLANT PATHOLOGY (IF REAL CROP/PLANT):
                If it IS a real agricultural plant leaf or crop:
                - Correctly identify the Crop Name (e.g. Tomato, Potato, Rice, Wheat, Corn, Cotton, Mango, Chilli, Sugarcane, Mustard, Apple, Grape, etc.).
                - Accurately identify the exact Disease Name (or 'Healthy Plant' if vibrant and disease-free).
                - Assess Disease Severity ('None' for healthy, 'Low', 'Moderate', or 'Severe').
                - Provide Calibrated Confidence (0.75 - 0.99).
                - Give clear visual Symptoms observed on the foliage.
                - Provide practical, exact Chemical Treatment (dosage per litre, e.g. Mancozeb 2.5g/L) AND Desi Organic Treatment (e.g. Neem Oil 5ml/L, Trichoderma).

                Respond ONLY in valid JSON matching this schema:
                {
                  "is_plant": true,
                  "crop_name": "Tomato",
                  "disease_name": "Early Blight",
                  "severity": "Moderate",
                  "confidence": 0.95,
                  "symptoms": "Concentric dark brown target-board rings on lower foliage with chlorotic yellow halo.",
                  "treatment": "Chemical: Spray Mancozeb 75 WP (2.5g/L) or Copper Oxychloride (3g/L). Organic: Spray 5% Neem seed kernel extract (NSKE)."
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inline_data", inlineData)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.15)
                    put("maxOutputTokens", 600)
                    put("responseMimeType", "application/json")
                })
            }

            val requestUrl = "$baseUrl/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(requestUrl)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseString)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val rawText = parts.getJSONObject(0).optString("text", "")
                        val parsed = parseDiagnosisJson(rawText)
                        if (parsed != null) {
                            return@withContext Result.success(parsed)
                        }
                    }
                }
            }

            Result.failure(Exception("Gemini verification failed with code ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Primary Multilingual Agronomy Advisory Chat:
     * Responds in user's selected language (Hindi, Bengali, Hinglish, Marathi, etc.)
     */
    suspend fun getAgronomyAdvice(
        primaryClass: String,
        confidence: Float,
        query: String,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = supabaseManager?.getRemoteConfig("gemini_api_key", "") ?: ""
            val modelName = supabaseManager?.getRemoteConfig("gemini_model_name", "gemini-flash-latest") ?: "gemini-flash-latest"

            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            val prompt = """
                You are 'Fasal Drishti AI Krishi Doctor' 🌾, an expert agronomist and plant pathologist helping Indian farmers.
                Current diagnosed crop & disease: $primaryClass (confidence: ${(confidence * 100).toInt()}%).
                Farmer's question: "$query"
                Preferred Response Language: $language

                Guidelines:
                1. Respond directly, warmly, and clearly in $language (e.g. Hindi, Hinglish, Bengali, Marathi, etc.).
                2. Provide exact chemical dosages (e.g. grams/ml per litre of water or per 15L spray tank) and safe organic/desi upchar.
                3. Keep the advice practical, cost-effective for smallholder farmers, and concise (under 120 words).
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 400)
                })
            }

            val requestUrl = "$baseUrl/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(requestUrl)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseString)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val reply = parts.getJSONObject(0).optString("text", "").trim()
                        if (reply.isNotBlank()) {
                            return@withContext Result.success(reply)
                        }
                    }
                }
            }

            Result.failure(Exception("Gemini chat advisory failed with code ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseDiagnosisJson(rawText: String): FallbackScanDiagnosis? {
        return try {
            val startIdx = rawText.indexOf('{')
            val endIdx = rawText.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                val jsonStr = rawText.substring(startIdx, endIdx + 1)
                val obj = JSONObject(jsonStr)

                val isPlant = obj.optBoolean("is_plant", true)
                val severity = obj.optString("severity", "Moderate")
                val cropName = obj.optString("crop_name", if (isPlant) "Crop Plant" else "Non-Crop Object")
                val diseaseName = obj.optString("disease_name", if (isPlant) "Leaf Condition" else "No Plant Leaf Detected")

                if (!isPlant || severity.equals("Invalid", ignoreCase = true) || cropName.contains("Non-Crop", ignoreCase = true)) {
                    FallbackScanDiagnosis(
                        cropName = "Non-Crop Object",
                        diseaseName = "No Plant Leaf Detected",
                        severity = "Invalid",
                        confidence = 0.10f,
                        symptoms = obj.optString("symptoms", "No recognized agricultural crop leaf was detected. Image contains non-plant object or background."),
                        treatment = obj.optString("treatment", "Please align a clear diseased crop leaf inside the camera reticle.")
                    )
                } else {
                    FallbackScanDiagnosis(
                        cropName = cropName,
                        diseaseName = diseaseName,
                        severity = severity,
                        confidence = obj.optDouble("confidence", 0.92).toFloat(),
                        symptoms = obj.optString("symptoms", "Foliar discoloration or lesions observed on crop surface."),
                        treatment = obj.optString("treatment", "Apply recommended organic neem oil or approved fungicide.")
                    )
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
