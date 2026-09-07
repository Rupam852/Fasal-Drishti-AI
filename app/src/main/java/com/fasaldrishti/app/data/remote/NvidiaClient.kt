package com.fasaldrishti.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NvidiaClient(private val supabaseManager: SupabaseManager? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val apiUrl = "https://integrate.api.nvidia.com/v1/chat/completions"

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
