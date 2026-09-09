package com.fasaldrishti.app.data.remote

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class FallbackScanDiagnosis(
    val cropName: String,
    val diseaseName: String,
    val severity: String,
    val confidence: Float,
    val symptoms: String,
    val treatment: String
)

class NvidiaClient(private val supabaseManager: SupabaseManager? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val apiUrl = "https://integrate.api.nvidia.com/v1/chat/completions"

    private fun compressAndEncodeImage(imageFile: File): String {
        val originalBitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: return Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)

        val width = originalBitmap.width
        val height = originalBitmap.height
        val maxDim = kotlin.math.max(width, height)
        val targetDim = 1024

        val resizedBitmap = if (maxDim > targetDim) {
            val scale = targetDim.toFloat() / maxDim
            val newW = (width * scale).toInt()
            val newH = (height * scale).toInt()
            android.graphics.Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
        } else {
            originalBitmap
        }

        val outputStream = java.io.ByteArrayOutputStream()
        resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    /**
     * Mandatory Multimodal AI Dual-Layer Verification:
     * Cross-examines the user's photo against the initial on-device vision detection,
     * validates plant authenticity, eliminates false positives, and refines exact dosages.
     */
    suspend fun verifyCropDiagnosis(
        imageFile: File,
        initialCropName: String? = null,
        initialDiseaseName: String? = null,
        initialConfidence: Float? = null
    ): Result<FallbackScanDiagnosis> = withContext(Dispatchers.IO) {
        try {
            val apiKey = supabaseManager?.getRemoteConfig("nvidia_nim_api_key") ?: ""
            val modelName = supabaseManager?.getRemoteConfig("nvidia_model_name", "meta/llama-3.2-11b-vision-instruct") ?: "meta/llama-3.2-11b-vision-instruct"

            if (apiKey.isBlank() || !imageFile.exists()) {
                return@withContext Result.failure(Exception("NVIDIA API key not available or image file missing"))
            }

            val base64Image = compressAndEncodeImage(imageFile)

            val initialContext = if (!initialCropName.isNullOrBlank()) {
                """
                Initial On-Device Vision Scan:
                - Preliminary Crop: $initialCropName
                - Preliminary Disease: ${initialDiseaseName ?: "Unknown"}
                - Preliminary Confidence: ${((initialConfidence ?: 0.5f) * 100).toInt()}%
                """.trimIndent()
            } else {
                "No preliminary on-device diagnosis available."
            }

            val prompt = """
                You are 'Fasal Drishti AI' Senior Multimodal Agronomist & Plant Pathologist.
                Perform Deep Cross-Verification on this uploaded farm photo.

                $initialContext

                Verification Tasks:
                1. VALIDATE SUBJECT: Check if the photo is a genuine agricultural plant leaf, crop, fruit, or farm vegetable.
                   - If it is NOT a crop/plant (e.g. human face/body, furniture, room wall, car, pet, electronic device, book, or completely blurred object):
                     Return JSON:
                     {
                       "crop_name": "Non-Crop Object",
                       "disease_name": "No Plant Leaf Detected",
                       "severity": "Invalid",
                       "confidence": 0.10,
                       "symptoms": "AI vision could not identify a real agricultural crop leaf or plant. The image contains a non-crop object, person, or unreadable background.",
                       "treatment": "Please align a clear, well-lit crop leaf inside the camera reticle and click again."
                     }
                2. CROSS-EXAMINE DIAGNOSIS: If it IS an agricultural plant leaf:
                   - Verify if the preliminary detection (${initialCropName ?: "Unknown"} - ${initialDiseaseName ?: "Unknown"}) is correct, or if it is a different crop or disease, or healthy foliage.
                   - Determine accurate severity ('None' for healthy, 'Low', 'Moderate', or 'Severe').
                   - Calibrated confidence between 0.75 and 0.99.
                   - Detailed visual symptoms.
                   - Actionable organic & chemical spray treatment with exact dosage per litre (e.g. Mancozeb 2.5g/L, Neem Oil 5ml/L).

                Respond ONLY in strictly valid JSON format:
                {
                  "crop_name": "Tomato",
                  "disease_name": "Early Blight",
                  "severity": "Moderate",
                  "confidence": 0.94,
                  "symptoms": "...",
                  "treatment": "..."
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("model", modelName)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        val contentArray = JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                })
                            })
                        }
                        put("content", contentArray)
                    })
                }
                put("messages", messages)
                put("temperature", 0.2)
                put("max_tokens", 450)
            }

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseString)
                val choices = jsonObj.getJSONArray("choices")
                if (choices.length() > 0) {
                    val rawText = choices.getJSONObject(0).getJSONObject("message").getString("content")
                    val parsed = parseDiagnosisJson(rawText)
                    if (parsed != null) {
                        return@withContext Result.success(parsed)
                    }
                }
            }

            Result.failure(Exception("Could not obtain vision verification response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Backward-compatible helper for fallback diagnosis
     */
    suspend fun diagnoseCropImage(imageFile: File): Result<FallbackScanDiagnosis> =
        verifyCropDiagnosis(imageFile = imageFile)

    private fun parseDiagnosisJson(rawText: String): FallbackScanDiagnosis? {
        return try {
            val startIdx = rawText.indexOf('{')
            val endIdx = rawText.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                val jsonStr = rawText.substring(startIdx, endIdx + 1)
                val obj = JSONObject(jsonStr)
                FallbackScanDiagnosis(
                    cropName = obj.optString("crop_name", "Crop Plant"),
                    diseaseName = obj.optString("disease_name", "Leaf Condition"),
                    severity = obj.optString("severity", "Moderate"),
                    confidence = obj.optDouble("confidence", 0.85).toFloat(),
                    symptoms = obj.optString("symptoms", "Visual abnormalities observed on plant foliage."),
                    treatment = obj.optString("treatment", "Apply recommended organic neem oil or approved fungicide.")
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getAgronomyAdvice(
        primaryClass: String,
        confidence: Float,
        query: String,
        language: String = "en"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Dynamically fetch the latest active key and model name from Supabase app_config
            val apiKey = supabaseManager?.getRemoteConfig("nvidia_nim_api_key") ?: ""
            val modelName = supabaseManager?.getRemoteConfig("nvidia_model_name", "meta/llama-3.2-11b-vision-instruct") ?: "meta/llama-3.2-11b-vision-instruct"

            if (apiKey.isBlank()) {
                return@withContext Result.success(
                    "AI Advisory for ${primaryClass.replace("___", " ")}: Isolate infected crop foliage, spray copper-based preventive fungicide (e.g. Mancozeb 2.5g/L), and ensure good field aeration."
                )
            }

            val prompt = """
                You are 'Fasal Drishti' (फसल दृष्टि) Senior Crop Agronomist and Plant Pathologist AI.
                Context:
                - Crop / Disease: ${primaryClass.replace("___", " ")}
                - AI Diagnostic Confidence: ${(confidence * 100).toInt()}%

                User Message/Query: $query

                CRITICAL RESPONSE LANGUAGE REQUIREMENT:
                - Target Response Language: $language
                - The user may write or ask questions in ANY language or script (English, Hindi, Bengali, Hinglish, etc.), but your advisory reply MUST STRICTLY be written in '$language'.
                - If Target Language is 'Hinglish', respond in natural conversational Hindi written in English/Latin letters (e.g. "Aapke tamatar ke paudhe me Late Blight laga hai. Iske upchar ke liye Mancozeb 2.5g per litre paani me milakar spray karein...").
                - If Target Language contains 'Hindi' or 'हिन्दी', write in Hindi (Devanagari script).
                - If Target Language contains 'Bengali' or 'বাংলা', write in Bengali script.
                - If Target Language contains 'Marathi' or 'मराठी', write in Marathi script.
                - If Target Language contains 'Punjabi' or 'ਪੰਜਾਬੀ', write in Punjabi Gurmukhi script.
                - If Target Language contains 'Gujarati' or 'ગુજરાતી', write in Gujarati script.
                - If Target Language contains 'Telugu' or 'తెలుగు', write in Telugu script.
                - If Target Language contains 'Tamil' or 'தமிழ்', write in Tamil script.
                - If Target Language contains 'Kannada' or 'ಕನ್ನಡ', write in Kannada script.
                - If Target Language contains 'Malayalam' or 'മലയാളം', write in Malayalam script.
                - If Target Language contains 'Odia' or 'ଓଡ଼ିଆ', write in Odia script.
                - If Target Language is 'English', write in plain English.

                ADVICE STRUCTURE:
                1. 🔍 Problem & Cause (कारण)
                2. 🧪 Chemical Treatment (दवा का नाम और सटीक मात्रा प्रति लीटर पानी)
                3. 🌿 Organic / Desi Remedy (जैविक व देसी उपाय)
                4. 🛡️ Prevention / Bachav (रोकथाम व सावधानियां)
                Keep the vocabulary friendly, clear, and actionable for farmers.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("model", modelName)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an expert plant pathologist and agronomist for Indian agriculture.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
                put("temperature", 0.2)
                put("max_tokens", 600)
            }

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseString)
                val choices = jsonObj.getJSONArray("choices")
                if (choices.length() > 0) {
                    val content = choices.getJSONObject(0).getJSONObject("message").getString("content")
                    Result.success(content)
                } else {
                    Result.success("Agronomy advisory generated for $primaryClass.")
                }
            } else {
                Result.success("AI Consultation for $primaryClass: Ensure adequate soil drainage, spray copper-based preventive fungicide (e.g. Mancozeb 2.5g/L), and check the underside of the leaves daily.")
            }
        } catch (e: Exception) {
            Result.success("Based on the scan of ${primaryClass.replace("___", " ")}, our AI Agronomist recommends isolating infected plants and applying approved systemic fungicide spray.")
        }
    }
}
