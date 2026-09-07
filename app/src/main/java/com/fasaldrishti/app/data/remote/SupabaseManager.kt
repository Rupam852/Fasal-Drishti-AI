package com.fasaldrishti.app.data.remote

import android.content.Context
import android.net.Uri
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
import org.json.JSONObject
import java.io.File

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
