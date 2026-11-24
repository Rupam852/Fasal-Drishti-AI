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

class NvidiaClient(
    private val supabaseManager: SupabaseManager? = null,
    private val aiConfigManager: com.fasaldrishti.app.data.local.AiConfigManager? = null
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(35, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .build()

    private val apiUrl = "https://integrate.api.nvidia.com/v1/chat/completions"

    private suspend fun resolveApiKey(): String {
        val config = aiConfigManager?.configState?.value
        if (config?.isCustomMode == true && config.nvidiaApiKey.isNotBlank()) {
            return config.nvidiaApiKey
        }
        return supabaseManager?.getRemoteConfig("nvidia_nim_api_key") ?: ""
    }

    private suspend fun resolveModelName(): String {
        val config = aiConfigManager?.configState?.value
        if (config?.isCustomMode == true && config.nvidiaModel.isNotBlank()) {
            val m = config.nvidiaModel
            if (!m.contains("llama-3") && !m.contains("phi-3.5") && !m.contains("mistral-large")) {
                return m
            }
        }
        val remote = supabaseManager?.getRemoteConfig("nvidia_model_name", "") ?: ""
        if (remote.isNotBlank() && !remote.contains("llama-3") && !remote.contains("phi-3.5") && !remote.contains("mistral-large")) {
            return remote
        }
        return "nvidia/nemotron-3.5-bolt-e4b"
    }

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
            val apiKey = resolveApiKey()

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
                   - PRACTICAL CHEMICAL PRESCRIPTION ("treatment"):
                     Format as clear bullet points:
                     • Recommended Chemical: exact trade name & active ingredient (e.g. Mancozeb, Ridomil Gold, Tilt, Beam, etc.)
                     • 15-Liter Knapsack Tank Dose: exact grams or ml per 15L spray pump (e.g. 35-40g per 15L tank)
                     • Field Acre Dose: amount in 200L water per acre
                     • Spray Timing: calm morning (7:00-9:30 AM) or evening (4:00-6:00 PM)
                     • Resistance Rotation: alternate with a different chemical group after 8-10 days
                   - PRACTICAL ORGANIC & FIELD MANAGEMENT ("prevention"):
                     Format as clear bullet points:
                     • Organic Remedy: 5% Neem oil @ 5ml/L, Trichoderma viride, or sour buttermilk
                     • Crop Sanitation: crop-specific field care (staking, pruning lower leaves, earthing up, water drainage)
                     • Harvest Safety (PHI): days to wait before picking

                Respond ONLY in strictly valid JSON format:
                {
                  "crop_name": "Tomato",
                  "disease_name": "Early Blight",
                  "severity": "Moderate",
                  "confidence": 0.94,
                  "symptoms": "Dark brown circular spots with concentric target-board rings on lower leaves.",
                  "treatment": "• Chemical: Mancozeb 75% WP @ 2.5g/L\n• 15L Tank Dose: 35-40g per 15-liter spray pump\n• Field Acre Dose: 600-800g in 200L water per acre\n• Spray Timing: Calm morning or cool evening\n• Rotation: Alternate with Azoxystrobin after 8-10 days",
                  "prevention": "• Organic Remedy: 5% Neem oil (5ml/L) or Trichoderma viride\n• Crop Sanitation: Prune lower leaves touching soil, stake plants\n• Harvest Safety: 7-10 days PHI waiting period before picking"
                }
            """.trimIndent()

            val primaryModel = resolveModelName()
            val candidateModels = listOf(
                primaryModel,
                "nvidia/nemotron-3.5-bolt-e4b",
                "minimaxai/minimax-m3.1-flash",
                "nvidia/nemotron-3.5-lightning-30b-a3b",
                "nvidia/nemotron-3.5-super-vl-120b-a12b"
            ).distinct()

            var lastError: Exception = Exception("Could not obtain vision verification response")

            for (currModel in candidateModels) {
                try {
                    val jsonBody = JSONObject().apply {
                        put("model", currModel)
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
                        put("max_tokens", 2048)
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
                        val choices = jsonObj.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val msgObj = choices.getJSONObject(0).optJSONObject("message")
                            val contentStr = msgObj?.optString("content", "") ?: ""
                            val reasoningStr = msgObj?.optString("reasoning_content", "") ?: ""
                            val rawText = if (contentStr.isNotBlank() && contentStr != "null") contentStr else reasoningStr
                            val parsed = parseDiagnosisJson(rawText)
                            if (parsed != null) {
                                return@withContext Result.success(parsed)
                            }
                        }
                    } else {
                        lastError = Exception("HTTP ${response.code} from $currModel: ${response.message}")
                    }
                } catch (e: Exception) {
                    lastError = e
                }
            }

            Result.failure(lastError)
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
                val rawTreatment = obj.optString("treatment", "Apply recommended organic neem oil or approved fungicide.")
                val rawPrevention = obj.optString("prevention", "")
                val fullTreatment = if (rawPrevention.isNotBlank()) {
                    "$rawTreatment\n\n[ORGANIC]\n$rawPrevention"
                } else {
                    rawTreatment
                }

                FallbackScanDiagnosis(
                    cropName = obj.optString("crop_name", "Crop Plant"),
                    diseaseName = obj.optString("disease_name", "Leaf Condition"),
                    severity = obj.optString("severity", "Moderate"),
                    confidence = obj.optDouble("confidence", 0.85).toFloat(),
                    symptoms = obj.optString("symptoms", "Visual abnormalities observed on plant foliage."),
                    treatment = fullTreatment
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
        language: String = "en",
        conversationHistory: List<com.fasaldrishti.app.domain.model.ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = resolveApiKey()
            val modelName = resolveModelName()

            if (apiKey.isBlank()) {
                return@withContext Result.success(
                    "AI Advisory for ${primaryClass.replace("___", " ")}: Isolate infected crop foliage, spray copper-based preventive fungicide (e.g. Mancozeb 2.5g/L), and ensure good field aeration."
                )
            }

            val historySnippet = if (conversationHistory.isNotEmpty()) {
                val recentTurns = conversationHistory.filterNot { it.isError || it.text.isBlank() }.takeLast(6)
                if (recentTurns.isNotEmpty()) {
                    "RECENT CONVERSATION CONTEXT (Last ${recentTurns.size} messages in this thread):\n" +
                    recentTurns.joinToString("\n") { msg ->
                        val sender = if (msg.isUser) "Farmer" else "AI Salahkar"
                        val imageNote = if (msg.imageUris.isNotEmpty()) " [Attached ${msg.imageUris.size} crop photo(s)]" else ""
                        "- $sender$imageNote: ${msg.text.take(300)}"
                    } + "\n\n"
                } else ""
            } else ""

            val prompt = """
                You are 'Fasal Drishti' (फसल दृष्टि) Senior Crop Agronomist and Plant Pathologist AI helping Indian farmers.
                Context:
                - Active Diagnosed Crop/Condition: ${primaryClass.replace("___", " ")}
                - AI Diagnostic Confidence: ${(confidence * 100).toInt()}%

                Farmer's Message / Query: $query

                $historySnippet
                CRITICAL RESPONSE PROTOCOL:
                - Target Response Language: $language
                - If Target Language is 'Hinglish': Respond in conversational Hindi in Latin alphabet.
                - If Target Language is 'Hindi', 'Bengali', 'Marathi', etc.: Write in the respective native script.
                - Smart Follow-up Memory: If the farmer is asking a follow-up question (e.g. spray dosage, frequency, remedy), refer to the RECENT CONVERSATION CONTEXT to answer with full knowledge of the discussed crop.

                MANDATORY 5-SECTION ADVICE STRUCTURE:
                🌾 1. Fasal & Rog ki Pehchan (Crop & Condition Identification)
                🔍 2. Karan aur Phailav (Root Cause & Weather Factors)
                🧪 3. Chemical Spray Upchar (Standard Fungicide/Insecticide, EXACT dosage per 1 Litre AND per 15L spray tank)
                🌿 4. Jaivik & Desi Upchar (Neem oil 5ml/L, bio-agents, pruning)
                🛡️ 5. Theek Hone ka Samay & Mausam Savdhani (Recovery timeline, best spray time, weather precautions)

                Provide a thorough, practical, and highly detailed agricultural response. Do not give a one-line answer.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("model", modelName)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an expert plant pathologist and chief crop agronomist for Indian agriculture.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
                put("temperature", 0.25)
                put("max_tokens", 1500)
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
                val code = response.code
                if (code == 401 || code == 403) {
                    Result.failure(com.fasaldrishti.app.domain.model.AiApiKeyException())
                } else {
                    Result.failure(com.fasaldrishti.app.domain.model.AiUnreachableException())
                }
            }
        } catch (e: Exception) {
            Result.failure(com.fasaldrishti.app.domain.model.AiUnreachableException())
        }
    }

    suspend fun translateDossier(
        languageCode: String,
        targetLanguageName: String,
        targetLanguageNative: String,
        diseaseName: String,
        symptoms: String,
        treatment: String,
        prevention: String
    ): Result<com.fasaldrishti.app.data.local.TranslatedDossier> = withContext(Dispatchers.IO) {
        try {
            val apiKey = resolveApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("NVIDIA API key is not configured"))
            }

            val prompt = """
                You are an expert Indian agricultural agronomist and professional translator. Translate the following crop disease diagnosis and management advice into $targetLanguageName ($targetLanguageNative).
                Maintain clear, farmer-friendly terminology for agricultural terms, spray dosages, and chemical names (keep active chemical ingredients or spray brand names easily readable/transliterated in $targetLanguageNative script).
                
                Return ONLY a valid JSON object with EXACTLY these keys:
                {
                  "disease_name": "translated disease name in $targetLanguageNative",
                  "symptoms": "translated symptoms & identification in $targetLanguageNative",
                  "treatment": "translated chemical fungicide & spray dosages in $targetLanguageNative",
                  "prevention": "translated organic remedies & prevention tips in $targetLanguageNative"
                }

                Original English Content:
                - Condition: $diseaseName
                - Symptoms: $symptoms
                - Chemical Treatment: $treatment
                - Prevention: $prevention
            """.trimIndent()

            val modelName = resolveModelName()
            val jsonBody = JSONObject().apply {
                put("model", modelName)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an expert plant pathologist and translator for Indian agriculture. Output only pure JSON.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
                put("temperature", 0.2)
                put("max_tokens", 800)
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
                val choices = jsonObj.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val msgObj = choices.getJSONObject(0).optJSONObject("message")
                    val content = msgObj?.optString("content", "") ?: ""
                    val reasoning = msgObj?.optString("reasoning_content", "") ?: ""
                    val rawText = if (content.isNotBlank() && content != "null") content else reasoning
                    val parsed = parseTranslationJson(rawText, languageCode)
                    if (parsed != null) {
                        return@withContext Result.success(parsed)
                    }
                }
            }

            Result.failure(Exception("NVIDIA translation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTranslationJson(rawText: String, languageCode: String): com.fasaldrishti.app.data.local.TranslatedDossier? {
        return try {
            val startIdx = rawText.indexOf('{')
            val endIdx = rawText.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                val jsonStr = rawText.substring(startIdx, endIdx + 1)
                val obj = JSONObject(jsonStr)
                com.fasaldrishti.app.data.local.TranslatedDossier(
                    languageCode = languageCode,
                    diseaseName = obj.optString("disease_name", "").ifBlank { "Crop Condition" },
                    symptoms = obj.optString("symptoms", "").ifBlank { "Symptoms details" },
                    treatment = obj.optString("treatment", "").ifBlank { "Treatment recommendations" },
                    prevention = obj.optString("prevention", "").ifBlank { "Prevention guidance" }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
