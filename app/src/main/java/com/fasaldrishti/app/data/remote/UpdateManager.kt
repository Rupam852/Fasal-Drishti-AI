package com.fasaldrishti.app.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fasaldrishti.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class AppUpdateInfo(
    val currentVersion: String = "1.2.0",
    val latestVersion: String = "1.2.0",
    val hasUpdate: Boolean = false,
    val downloadUrl: String? = null,
    val releaseNotes: String = "",
    val isChecking: Boolean = false,
    val checkMessage: String? = null
)

class UpdateManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fasal_updater_prefs", Context.MODE_PRIVATE)
    private val channelId = "fasal_drishti_updates"

    private val _updateInfo = MutableStateFlow(
        AppUpdateInfo(
            currentVersion = "v1.2.0",
            latestVersion = "v1.2.0",
            hasUpdate = false,
            releaseNotes = """
• 📸 Permanent On-Device Chat Photo Storage (No blank/green image issues)
• 🧠 Smart Multi-Turn Agronomic Conversational Memory
• 🌾 5-Section In-Depth Diagnostic Dossiers (Dosages per L / 15L tank)
• 🧹 Complete AI Chat History & Storage Cleanup in Profile
• 🎨 Default Light Mode Migration & User Theme Preference Retention
• 🌐 Seamless 12-Language Support with Auto-Synced Greetings
• 💾 Isolated Multi-Session Local Storage per Plant & Clean Top Bar
• 📄 PDF Diagnostic Prescription & WhatsApp Share
• 🧪 Fertilizer NPK Calculator & Mandi Bhav Rates
            """.trimIndent()
        )
    )
    val updateInfo: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private val _autoCheckEnabled = MutableStateFlow(prefs.getBoolean("auto_check_update", true))
    val autoCheckEnabled: StateFlow<Boolean> = _autoCheckEnabled.asStateFlow()

    init {
        createNotificationChannel()
    }

    fun setAutoCheckEnabled(enabled: Boolean) {
        _autoCheckEnabled.value = enabled
        prefs.edit().putBoolean("auto_check_update", enabled).apply()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Updates"
            val descriptionText = "Notifications for new versions of Fasal Drishti"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Check for updates from the Neo Files Transfer live API endpoint.
     */
    suspend fun checkForUpdates(isAutoCheck: Boolean = false): AppUpdateInfo = withContext(Dispatchers.IO) {
        _updateInfo.value = _updateInfo.value.copy(isChecking = true, checkMessage = "Checking for new version...")

        val currentVer = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }

        val apiUrl = "https://neo-files-transfer-p3ot.onrender.com/api/version/apk_2c91d932cde44375"

        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder()
                .url(apiUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val json = org.json.JSONObject(bodyStr)
                if (json.optString("status") == "success") {
                    val rawServerVer = json.optString("version", "1.0.0")
                    val serverVer = rawServerVer.removePrefix("v").removePrefix("V").trim()
                    val normCurrentVer = currentVer.removePrefix("v").removePrefix("V").trim()
                    
                    val downloadUrl = json.optString("download_url").ifBlank {
                        json.optString("web_url", "https://neo-files-transfer.pages.dev/download/mtre516vdmlr4152f2ab")
                    }
                    val fileName = json.optString("file_name", "Fasal Drishti.apk")
                    val fileSize = json.optLong("file_size", 0L)
                    val formattedSize = if (fileSize > 0) String.format("%.1f MB", fileSize / (1024.0 * 1024.0)) else ""

                    val isNewerAvailable = isVersionGreater(serverVer, normCurrentVer)

                    val customNotes = json.optString("release_notes").ifBlank { json.optString("changelog") }
                    val v104Highlights = """
• 🎙️ Multilingual Voice Assistant (Speech-to-Text & Audio Playback)
• 📄 One-Tap PDF Prescription & Direct WhatsApp Share
• 🧪 Fertilizer & NPK Dosage Calculator (Acre, Bigha, Hectare)
• 🌾 Live Mandi Bhav & APMC Rates with Smart AI Advice
• 🏛️ Sarkari Krishi Yojanaen & PM-Kisan Portal Guide
• 🌱 Soil Health & Crop Advisor AI (NPK & pH Testing)
• ⚡ 120Hz Ultra-Fluid UI & Precision Disease Detection
                    """.trimIndent()
                    val activeReleaseNotes = if (customNotes.isNotBlank()) customNotes else v104Highlights

                    val result = if (isNewerAvailable) {
                        AppUpdateInfo(
                            currentVersion = "v$normCurrentVer",
                            latestVersion = "v$serverVer",
                            hasUpdate = true,
                            downloadUrl = downloadUrl,
                            releaseNotes = activeReleaseNotes,
                            isChecking = false,
                            checkMessage = "New update available: v$serverVer"
                        )
                    } else {
                        AppUpdateInfo(
                            currentVersion = "v$normCurrentVer",
                            latestVersion = "v$serverVer",
                            hasUpdate = false,
                            downloadUrl = downloadUrl,
                            releaseNotes = activeReleaseNotes,
                            isChecking = false,
                            checkMessage = "Your app is up to date!"
                        )
                    }

                    _updateInfo.value = result

                    if (result.hasUpdate && isAutoCheck) {
                        sendUpdateNotification(result.latestVersion)
                    }

                    return@withContext result
                }
            }
        } catch (e: Exception) {
            // Fallback in case network or API is unreachable
        }

        val fallback = _updateInfo.value.copy(
            isChecking = false,
            checkMessage = "Checked just now. Running v$currentVer"
        )
        _updateInfo.value = fallback
        fallback
    }

    private fun isVersionGreater(v1: String, v2: String): Boolean {
        return try {
            val parts1 = v1.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            val parts2 = v2.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            val maxLen = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLen) {
                val num1 = parts1.getOrElse(i) { 0 }
                val num2 = parts2.getOrElse(i) { 0 }
                if (num1 > num2) return true
                if (num1 < num2) return false
            }
            false
        } catch (_: Exception) {
            v1 > v2
        }
    }

    fun sendUpdateNotification(newVersion: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "updater")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 1001, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Fasal Drishti Update Available! 🌾")
            .setContentText("Version $newVersion is ready. Tap to download new features and disease model updates.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(1001, builder.build())
            }
        } catch (_: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission
        }
    }

    fun openDownloadUrl(url: String?) {
        val downloadUri = Uri.parse(url ?: "https://github.com/fasal-drishti/app/releases/latest")
        val intent = Intent(Intent.ACTION_VIEW, downloadUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
