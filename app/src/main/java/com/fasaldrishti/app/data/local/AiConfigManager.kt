package com.fasaldrishti.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiProvider(val displayName: String) {
    GEMINI("Google Gemini"),
    NVIDIA("NVIDIA NIM")
}

data class AiModelOption(
    val id: String,
    val name: String,
    val description: String,
    val isRecommended: Boolean = false
)

data class DiagnosticResult(
    val provider: AiProvider,
    val model: String,
    val isSuccess: Boolean,
    val latencyMs: Long,
    val message: String
)

data class AiConfigState(
    val isCustomMode: Boolean = false,
    val primaryProvider: AiProvider = AiProvider.GEMINI,
    val secondaryProvider: AiProvider = AiProvider.NVIDIA,
    val geminiModel: String = "gemini-3.7-flash",
    val geminiApiKey: String = "",
    val nvidiaModel: String = "meta/llama-3.2-11b-vision-instruct",
    val nvidiaApiKey: String = ""
)

/**
 * Manages local persistence and diagnostic testing for AI Engines (Default vs Custom).
 * Strictly persists in device-only SharedPreferences (never uploaded to external servers).
 */
class AiConfigManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fasal_ai_config_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val KEY_IS_CUSTOM_MODE = "is_custom_mode"
        private const val KEY_PRIMARY_PROVIDER = "primary_provider"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_NVIDIA_MODEL = "nvidia_model"
        private const val KEY_NVIDIA_API_KEY = "nvidia_api_key"
        private const val KEY_LAST_DEFAULT_TEST = "last_default_test_timestamp"

        val AVAILABLE_GEMINI_MODELS = listOf(
            AiModelOption("gemini-3.7-flash", "Gemini 3.7 Flash", "State-of-the-art hybrid reasoning & fast multimodal vision (Recommended)", true),
            AiModelOption("gemini-3.5-flash", "Gemini 3.5 Flash", "Ultra high-throughput crop disease diagnosis"),
            AiModelOption("gemini-3.5-flash-lite", "Gemini 3.5 Flash Lite", "Lightweight low-latency foliar analysis"),
            AiModelOption("gemini-3.1-flash-lite", "Gemini 3.1 Flash Lite", "Optimized low-bandwidth agronomy engine"),
            AiModelOption("gemini-flash-lite-latest", "Gemini Flash Lite (Latest)", "Automatically tracking latest stable Flash Lite"),
            AiModelOption("gemini-3-flash-preview", "Gemini 3 Flash Preview", "Experimental rapid multimodal vision")
        )

        val AVAILABLE_NVIDIA_MODELS = listOf(
            AiModelOption("meta/llama-3.2-11b-vision-instruct", "Llama 3.2 11B Vision", "Multimodal crop vision & leaf pathology (Recommended)", true),
            AiModelOption("meta/llama-3.1-70b-instruct", "Llama 3.1 70B Instruct", "High-capacity conversational agronomist"),
            AiModelOption("nvidia/neva-22b", "NVIDIA NeVA 22B", "Specialized visual reasoning model"),
            AiModelOption("mistralai/mistral-large-2-instruct", "Mistral Large 2", "Advanced multi-lingual agronomy intelligence")
        )
    }

    private val _configState = MutableStateFlow(loadConfig())
    val configState: StateFlow<AiConfigState> = _configState.asStateFlow()

    private fun loadConfig(): AiConfigState {
        val isCustom = prefs.getBoolean(KEY_IS_CUSTOM_MODE, false)
        val primaryStr = prefs.getString(KEY_PRIMARY_PROVIDER, AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        val primary = try { AiProvider.valueOf(primaryStr) } catch (_: Exception) { AiProvider.GEMINI }
        val secondary = if (primary == AiProvider.GEMINI) AiProvider.NVIDIA else AiProvider.GEMINI
        
        val geminiModel = prefs.getString(KEY_GEMINI_MODEL, "gemini-3.7-flash") ?: "gemini-3.7-flash"
        val geminiKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        val nvidiaModel = prefs.getString(KEY_NVIDIA_MODEL, "meta/llama-3.2-11b-vision-instruct") ?: "meta/llama-3.2-11b-vision-instruct"
        val nvidiaKey = prefs.getString(KEY_NVIDIA_API_KEY, "") ?: ""

        return AiConfigState(
            isCustomMode = isCustom,
            primaryProvider = primary,
            secondaryProvider = secondary,
            geminiModel = geminiModel,
            geminiApiKey = geminiKey,
            nvidiaModel = nvidiaModel,
            nvidiaApiKey = nvidiaKey
        )
    }

    fun saveConfig(state: AiConfigState) {
        val primary = state.primaryProvider
        val secondary = if (primary == AiProvider.GEMINI) AiProvider.NVIDIA else AiProvider.GEMINI
        val sanitizedState = state.copy(secondaryProvider = secondary)

        prefs.edit()
            .putBoolean(KEY_IS_CUSTOM_MODE, sanitizedState.isCustomMode)
            .putString(KEY_PRIMARY_PROVIDER, sanitizedState.primaryProvider.name)
            .putString(KEY_GEMINI_MODEL, sanitizedState.geminiModel)
            .putString(KEY_GEMINI_API_KEY, sanitizedState.geminiApiKey.trim())
            .putString(KEY_NVIDIA_MODEL, sanitizedState.nvidiaModel)
            .putString(KEY_NVIDIA_API_KEY, sanitizedState.nvidiaApiKey.trim())
            .apply()

        _configState.value = sanitizedState
    }

    fun setMode(isCustom: Boolean) {
        val current = _configState.value
        saveConfig(current.copy(isCustomMode = isCustom))
    }

    fun setPrimaryProvider(primary: AiProvider) {
        val current = _configState.value
        val secondary = if (primary == AiProvider.GEMINI) AiProvider.NVIDIA else AiProvider.GEMINI
        saveConfig(current.copy(primaryProvider = primary, secondaryProvider = secondary))
    }

    fun getRemainingDefaultCooldownSeconds(): Int {
        val lastTime = prefs.getLong(KEY_LAST_DEFAULT_TEST, 0L)
        val now = System.currentTimeMillis()
        val elapsed = (now - lastTime) / 1000
        return if (elapsed < 30) (30 - elapsed).toInt() else 0
    }

    fun recordDefaultTestAttempt() {
        prefs.edit().putLong(KEY_LAST_DEFAULT_TEST, System.currentTimeMillis()).apply()
    }

    /**
     * Diagnostic live ping test for Google Gemini
     */
    suspend fun testGemini(modelName: String, apiKey: String): DiagnosticResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        if (apiKey.isBlank()) {
            return@withContext DiagnosticResult(
                provider = AiProvider.GEMINI,
                model = modelName,
                isSuccess = false,
                latencyMs = 0,
                message = "API key is missing or blank"
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val payload = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "Agricultural test ping: Respond with exactly 2 words 'Fasal OK'."))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().put("maxOutputTokens", 20))
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.contains("candidates")) {
                DiagnosticResult(
                    provider = AiProvider.GEMINI,
                    model = modelName,
                    isSuccess = true,
                    latencyMs = latency,
                    message = "Active & Operational ($latency ms)"
                )
            } else {
                val errorMsg = try {
                    val errObj = JSONObject(body).getJSONObject("error")
                    errObj.optString("message", "HTTP ${response.code}")
                } catch (_: Exception) {
                    "HTTP ${response.code}: $body"
                }
                DiagnosticResult(
                    provider = AiProvider.GEMINI,
                    model = modelName,
                    isSuccess = false,
                    latencyMs = latency,
                    message = errorMsg
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            DiagnosticResult(
                provider = AiProvider.GEMINI,
                model = modelName,
                isSuccess = false,
                latencyMs = latency,
                message = e.localizedMessage ?: "Connection error"
            )
        }
    }

    /**
     * Diagnostic live ping test for NVIDIA NIM
     */
    suspend fun testNvidia(modelName: String, apiKey: String): DiagnosticResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        if (apiKey.isBlank()) {
            return@withContext DiagnosticResult(
                provider = AiProvider.NVIDIA,
                model = modelName,
                isSuccess = false,
                latencyMs = 0,
                message = "NVIDIA API key is missing or blank"
            )
        }

        try {
            val url = "https://integrate.api.nvidia.com/v1/chat/completions"
            val payload = JSONObject().apply {
                put("model", modelName)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Agricultural test ping: Respond with exactly 2 words 'Fasal OK'.")
                    })
                }
                put("messages", messages)
                put("max_tokens", 20)
                put("temperature", 0.2)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Accept", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.contains("choices")) {
                DiagnosticResult(
                    provider = AiProvider.NVIDIA,
                    model = modelName,
                    isSuccess = true,
                    latencyMs = latency,
                    message = "Active & Operational ($latency ms)"
                )
            } else {
                val errorMsg = try {
                    val errObj = JSONObject(body)
                    errObj.optString("detail", errObj.optString("message", "HTTP ${response.code}"))
                } catch (_: Exception) {
                    "HTTP ${response.code}: $body"
                }
                DiagnosticResult(
                    provider = AiProvider.NVIDIA,
                    model = modelName,
                    isSuccess = false,
                    latencyMs = latency,
                    message = errorMsg
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            DiagnosticResult(
                provider = AiProvider.NVIDIA,
                model = modelName,
                isSuccess = false,
                latencyMs = latency,
                message = e.localizedMessage ?: "Connection error"
            )
        }
    }
}
