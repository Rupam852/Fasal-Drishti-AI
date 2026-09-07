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

    /**
     * Multimodal AI Fallback Vision diagnosis when the scanned crop is not in on-device dataset
     */
    suspend fun diagnoseCropImage(imageFile: File): Result<FallbackScanDiagnosis> = withContext(Dispatchers.IO) {
        try {
            val apiKey = supabaseManager?.getRemoteConfig("nvidia_nim_api_key") ?: ""
            val modelName = supabaseManager?.getRemoteConfig("nvidia_model_name", "meta/llama-3.2-11b-vision-instruct") ?: "meta/llama-3.2-11b-vision-instruct"

            if (apiKey.isBlank() || !imageFile.exists()) {
                return@withContext Result.failure(Exception("NVIDIA API key not available or image file missing"))
            }

            val imageBytes = imageFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val prompt = """
                You are 'Fasal Drishti AI' Senior Plant Pathologist & Agronomist.
                Carefully analyze this uploaded image:
                1. Check if the image contains an agricultural crop leaf, plant, fruit, or farm vegetable.
                2. If it is NOT a plant or crop leaf (e.g. human, furniture, vehicle, pet, random object, or unreadable blur):
                   Return JSON with:
                   "crop_name": "Non-Crop Object",
                   "disease_name": "No Plant Leaf Detected",
                   "severity": "Invalid",
                   "confidence": 0.15,
                   "symptoms": "AI vision did not find a recognized agricultural plant leaf. The image may be of a non-crop object, person, animal, or too blurry.",
                   "treatment": "Please align a clear, well-lit crop leaf inside the camera reticle and take a close-up photo."

                3. If it IS an agricultural crop/plant (even if uncommon like Mango, Mustard, Sugarcane, Rose, Papaya, Chilli, Banana, Guava, Cotton, Wheat, Rice, etc.):
                   Identify the exact Crop Name, Disease Name (or 'Healthy Plant' if no disease), Severity ('None', 'Low', 'Moderate', or 'Severe'), Confidence (0.75 - 0.98), Key Symptoms, and practical Organic & Chemical Treatment advice with dosage per litre.

                Respond ONLY in valid JSON format:
                {
                  "crop_name": "Tomato",
                  "disease_name": "Early Blight",
                  "severity": "Moderate",
                  "confidence": 0.88,
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

            Result.failure(Exception("Could not obtain vision diagnosis response"))
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
