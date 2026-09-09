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
    val currentVersion: String = "1.0.1",
    val latestVersion: String = "1.0.1",
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
            currentVersion = "1.0.1",
            latestVersion = "1.0.1",
            hasUpdate = false
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
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
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

                    val result = if (isNewerAvailable) {
                        AppUpdateInfo(
                            currentVersion = "v$normCurrentVer",
                            latestVersion = "v$serverVer",
                            hasUpdate = true,
                            downloadUrl = downloadUrl,
                            releaseNotes = "• File: $fileName ($formattedSize)\n• Fast Cloudflare Edge Download CDN\n• PlantVillage Disease Vision & Multilingual NVIDIA NIM Agronomist\n• Bug fixes & 120Hz refresh rate stability",
                            isChecking = false,
                            checkMessage = "New update available: v$serverVer"
                        )
                    } else {
                        AppUpdateInfo(
                            currentVersion = "v$normCurrentVer",
                            latestVersion = "v$serverVer",
                            hasUpdate = false,
                            downloadUrl = downloadUrl,
                            releaseNotes = "You are on the latest version ($fileName)",
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Fasal Drishti Update Available! 🌾")
            .setContentText("Version $newVersion is ready. Tap to download new features and disease model updates.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
