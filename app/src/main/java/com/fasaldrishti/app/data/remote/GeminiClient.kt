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

/**
 * Google Gemini Multimodal Vision & Agronomy Advisory Client (Primary Cloud AI).
 * Backed by Supabase Remote Config for secure, zero-hardcode key updates.
 */
class GeminiClient(private val supabaseManager: SupabaseManager? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    /**
     * Primary Multimodal AI Dual-Layer Verification:
     * Cross-examines the uploaded crop leaf image with Google Gemini Vision,
     * validates plant authenticity, eliminates non-crop objects, and computes exact spray dosages.
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

            val imageBytes = imageFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

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
                You are 'Fasal Drishti AI' Senior Multimodal Agronomist & Plant Pathologist.
                Perform Deep Multimodal Cross-Verification on this uploaded farm photo.

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
                   - Detailed visual foliar symptoms.
                   - Actionable organic & chemical spray treatment with exact dosage per litre (e.g. Mancozeb 2.5g/L, Neem Oil 5ml/L).

                Respond ONLY in valid JSON matching this schema:
                {
                  "crop_name": "Tomato",
                  "disease_name": "Early Blight",
                  "severity": "Moderate",
                  "confidence": 0.94,
                  "symptoms": "Concentric dark brown rings on lower leaves with yellow halos.",
                  "treatment": "Spray Mancozeb (2.5 g/L) or Copper Oxychloride (3 g/L) every 7-10 days."
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
                    put("temperature", 0.2)
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
                FallbackScanDiagnosis(
                    cropName = obj.optString("crop_name", "Crop Plant"),
                    diseaseName = obj.optString("disease_name", "Leaf Condition"),
                    severity = obj.optString("severity", "Moderate"),
                    confidence = obj.optDouble("confidence", 0.90).toFloat(),
                    symptoms = obj.optString("symptoms", "Foliar discoloration or lesions observed on crop surface."),
                    treatment = obj.optString("treatment", "Apply recommended organic neem oil or approved fungicide.")
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
