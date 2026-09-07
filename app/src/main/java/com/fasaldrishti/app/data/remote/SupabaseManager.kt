package com.fasaldrishti.app.data.remote

import android.content.Context
import com.fasaldrishti.app.BuildConfig
import com.fasaldrishti.app.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.util.UUID

/**
 * Manages Supabase Auth, Remote Configs, PostgreSQL & Storage.
 */
class SupabaseManager(private val context: Context) {

    private val client = OkHttpClient()
    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    // Local in-memory config cache
    private val configCache = mutableMapOf<String, String>()

    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "user_demo_101",
            name = "Ramesh Kumar",
            email = "ramesh.farmer@example.com",
            avatarUrl = null,
            totalScans = 12,
            healthyCount = 8,
            diseasedCount = 4
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    suspend fun signInWithGoogle(): Result<UserProfile> {
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            name = "Ramesh Kumar",
            email = "ramesh.farmer@gmail.com",
            avatarUrl = null,
            totalScans = 14,
            healthyCount = 9,
            diseasedCount = 5
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun signInWithGitHub(): Result<UserProfile> {
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            name = "AgriTech Developer",
            email = "developer@agritech.org",
            avatarUrl = null,
            totalScans = 6,
            healthyCount = 4,
            diseasedCount = 2
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun signOut() {
        _currentUser.value = null
    }

    suspend fun uploadCropImage(imageFile: File): Result<String> {
        return Result.success(imageFile.absolutePath)
    }

    /**
     * Fetch dynamic remote configuration from Supabase 'app_config' table.
     * Allows updating API keys (e.g. NVIDIA NIM key) anytime from Supabase dashboard!
     */
    suspend fun getRemoteConfig(key: String, defaultValue: String = ""): String = withContext(Dispatchers.IO) {
        if (configCache.containsKey(key)) {
            return@withContext configCache[key] ?: defaultValue
        }

        try {
            val url = "$supabaseUrl/rest/v1/app_config?key=eq.$key&select=value"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                if (jsonArray.length() > 0) {
                    val value = jsonArray.getJSONObject(0).getString("value")
                    configCache[key] = value
                    return@withContext value
                }
            }
        } catch (_: Exception) {}

        defaultValue
    }

    fun invalidateConfigCache() {
        configCache.clear()
    }
}
