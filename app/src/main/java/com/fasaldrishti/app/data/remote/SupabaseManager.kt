package com.fasaldrishti.app.data.remote

import android.content.Context
import android.net.Uri
import com.fasaldrishti.app.BuildConfig
import com.fasaldrishti.app.data.local.ScanDao
import com.fasaldrishti.app.data.local.ScanEntity
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.model.SyncStatus
import com.fasaldrishti.app.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Manages Supabase Auth (Google & GitHub OAuth + Deep Linking), Persistent Session, Remote Configs & Storage.
 */
class SupabaseManager(private val context: Context) {

    private val client = OkHttpClient()
    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    private val prefs = context.getSharedPreferences("fasal_auth_prefs", Context.MODE_PRIVATE)

    // Local in-memory config cache
    private val configCache = mutableMapOf<String, String>()

    private val _currentUser = MutableStateFlow<UserProfile?>(loadSavedUser())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private fun loadSavedUser(): UserProfile? {
        val id = prefs.getString("user_id", null) ?: return null
        val name = prefs.getString("user_name", "Farmer") ?: "Farmer"
        val email = prefs.getString("user_email", "") ?: ""
        var avatarUrl = prefs.getString("user_avatar", null)

        if (avatarUrl.isNullOrBlank()) {
            try {
                val googleAccount = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
                if (googleAccount?.photoUrl != null) {
                    avatarUrl = googleAccount.photoUrl.toString()
                    prefs.edit().putString("user_avatar", avatarUrl).apply()
                }
            } catch (_: Exception) {}
        }

        val totalScans = prefs.getInt("total_scans", 0)
        val healthyCount = prefs.getInt("healthy_count", 0)
        val diseasedCount = prefs.getInt("diseased_count", 0)

        return UserProfile(
            id = id,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            totalScans = totalScans,
            healthyCount = healthyCount,
            diseasedCount = diseasedCount
        )
    }

    private fun saveUserToPrefs(user: UserProfile) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_avatar", user.avatarUrl)
            .apply()
    }

    fun setAuthenticatedUser(user: UserProfile) {
        var finalUser = user
        if (finalUser.avatarUrl.isNullOrBlank()) {
            try {
                val googleAccount = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
                if (googleAccount?.photoUrl != null) {
                    finalUser = finalUser.copy(avatarUrl = googleAccount.photoUrl.toString())
                }
            } catch (_: Exception) {}
        }
        saveUserToPrefs(finalUser)
        _currentUser.value = finalUser

        // Background automatic sync to Supabase Cloud
        CoroutineScope(Dispatchers.IO).launch {
            syncUserProfileToCloud(finalUser)
        }
    }

    fun getOAuthUrl(provider: String): String {
        return "$supabaseUrl/auth/v1/authorize?provider=$provider&redirect_to=fasaldrishti://auth"
    }

    suspend fun signInWithGoogle(): Result<UserProfile> {
        // Return active user if already logged in, otherwise indicate OAuth pending
        val current = _currentUser.value
        return if (current != null) {
            Result.success(current)
        } else {
            Result.failure(Exception("Please complete Google sign-in"))
        }
    }

    /**
     * Intercepts and parses deep links from Supabase OAuth redirects (fasaldrishti://auth#access_token=...)
     * Fetches real user profile from Supabase API and saves session persistently.
     */
    suspend fun handleAuthCallback(uri: Uri): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            var token: String? = null
            val fragment = uri.fragment
            if (!fragment.isNullOrBlank()) {
                val params = fragment.split("&").associate {
                    val parts = it.split("=")
                    if (parts.size >= 2) parts[0] to parts[1] else "" to ""
                }
                token = params["access_token"]
            }
            if (token.isNullOrBlank()) {
                token = uri.getQueryParameter("access_token") ?: uri.getQueryParameter("token")
            }

            if (!token.isNullOrBlank()) {
                val userRequest = Request.Builder()
                    .url("$supabaseUrl/auth/v1/user")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val userResponse = client.newCall(userRequest).execute()
                if (userResponse.isSuccessful) {
                    val userBody = userResponse.body?.string() ?: "{}"
                    val jsonObj = JSONObject(userBody)
                    val id = jsonObj.optString("id", "")
                    val email = jsonObj.optString("email", "")
                    val metadata = jsonObj.optJSONObject("user_metadata")
                    val name = metadata?.optString("full_name")
                        ?: metadata?.optString("name")
                        ?: metadata?.optString("user_name")
                        ?: email.substringBefore("@").ifBlank { "Farmer" }
                    val avatarUrl = metadata?.optString("avatar_url")

                    if (id.isNotBlank()) {
                        val user = UserProfile(
                            id = id,
                            name = name,
                            email = email,
                            avatarUrl = avatarUrl,
                            totalScans = prefs.getInt("total_scans", 0),
                            healthyCount = prefs.getInt("healthy_count", 0),
                            diseasedCount = prefs.getInt("diseased_count", 0)
                        )
                        saveUserToPrefs(user)
                        _currentUser.value = user
                        return@withContext Result.success(user)
                    }
                }
            }

            Result.failure(Exception("Could not verify Supabase user session"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    suspend fun syncUserProfileToCloud(user: UserProfile) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", user.id)
                put("name", user.name)
                put("email", user.email)
                put("avatar_url", user.avatarUrl ?: "")
                put("updated_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()))
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().close()
        } catch (_: Exception) {}
    }

    suspend fun syncScanRecordToCloud(scan: com.fasaldrishti.app.domain.model.ScanRecord) = withContext(Dispatchers.IO) {
        try {
            _syncStatus.value = SyncStatus.UPLOADING
            val userId = _currentUser.value?.id ?: prefs.getString("user_id", "guest")
            val json = JSONObject().apply {
                put("id", scan.id)
                put("user_id", userId)
                put("crop_name", scan.cropName)
                put("disease_name", scan.diseaseName)
                put("predicted_class", scan.predictedClass)
                put("confidence", scan.confidence.toDouble())
                put("severity", scan.severity)
                put("symptoms", scan.symptoms)
                put("treatment", scan.treatment)
                put("created_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date(scan.timestamp)))
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/scans")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().close()
        } catch (_: Exception) {
        } finally {
            delay(1200) // Keep indicator smoothly visible
            _syncStatus.value = SyncStatus.IDLE
        }
    }

    /**
     * Downloads and restores past crop scans from Supabase into local Room DB seamlessly on sign-in / reinstall.
     */
    suspend fun fetchAndRestoreScansFromCloud(scanDao: ScanDao) = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: prefs.getString("user_id", null) ?: return@withContext
        try {
            _syncStatus.value = SyncStatus.DOWNLOADING
            val url = "$supabaseUrl/rest/v1/scans?user_id=eq.$userId&order=created_at.desc"
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
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val id = item.optString("id", UUID.randomUUID().toString())
                    val cropName = item.optString("crop_name", "Crop")
                    val diseaseName = item.optString("disease_name", "Disease")
                    val predictedClass = item.optString("predicted_class", "Class")
                    val confidence = item.optDouble("confidence", 0.95).toFloat()
                    val severity = item.optString("severity", "Moderate")
                    val symptoms = item.optString("symptoms", "")
                    val treatment = item.optString("treatment", "")
                    val createdAtStr = item.optString("created_at", "")

                    var timestamp = System.currentTimeMillis()
                    if (createdAtStr.isNotBlank()) {
                        try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                            val date = sdf.parse(createdAtStr.substringBefore("."))
                            if (date != null) timestamp = date.time
                        } catch (_: Exception) {}
                    }

                    val entity = ScanEntity(
                        id = id,
                        imageUrl = "",
                        predictedClass = predictedClass,
                        confidence = confidence,
                        cropName = cropName,
                        diseaseName = diseaseName,
                        severity = severity,
                        symptoms = symptoms,
                        treatment = treatment,
                        timestamp = timestamp
                    )
                    scanDao.insertScan(entity)
                    delay(50) // Smooth asynchronous ingestion pacing
                }
            }
            response.close()
        } catch (_: Exception) {
            // Safe execution
        } finally {
            delay(1200) // Smooth completion transition
            _syncStatus.value = SyncStatus.IDLE
        }
    }

    suspend fun deleteScanFromCloud(scanId: String) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/scans?id=eq.$scanId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (_: Exception) {}
    }

    suspend fun clearAllScansFromCloud() = withContext(Dispatchers.IO) {
        try {
            val userId = _currentUser.value?.id ?: prefs.getString("user_id", null)
            val url = if (userId != null) "$supabaseUrl/rest/v1/scans?user_id=eq.$userId" else "$supabaseUrl/rest/v1/scans?id=neq.0"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .delete()
                .build()
            client.newCall(request).execute().close()
        } catch (_: Exception) {}
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
