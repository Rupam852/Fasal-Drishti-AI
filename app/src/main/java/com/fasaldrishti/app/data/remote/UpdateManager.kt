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
    val currentVersion: String = "1.0.0",
    val latestVersion: String = "1.0.0",
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
            currentVersion = "1.0.0",
            latestVersion = "1.0.0",
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
     * Check for updates. Connects to backend API or simulates update availability.
     * When user provides their real API endpoint, replace the remote fetch logic.
     */
    suspend fun checkForUpdates(isAutoCheck: Boolean = false): AppUpdateInfo = withContext(Dispatchers.IO) {
        _updateInfo.value = _updateInfo.value.copy(isChecking = true, checkMessage = "Checking for new version...")

        // Simulated check (Can be hooked to user's remote endpoint easily)
        kotlinx.coroutines.delay(1200)

        val currentVer = "1.0.0"
        val serverLatestVer = "1.1.0" // Simulated newer version available
        val isNewerAvailable = serverLatestVer > currentVer

        val result = if (isNewerAvailable) {
            AppUpdateInfo(
                currentVersion = currentVer,
                latestVersion = serverLatestVer,
                hasUpdate = true,
                downloadUrl = "https://github.com/fasal-drishti/app/releases/latest",
                releaseNotes = "• Improved MobileNetV2 inference speed with NNAPI\n• Added Hindi agronomy chat support\n• Bug fixes & 120Hz refresh rate stability",
                isChecking = false,
                checkMessage = "New update available: v$serverLatestVer"
            )
        } else {
            AppUpdateInfo(
                currentVersion = currentVer,
                latestVersion = currentVer,
                hasUpdate = false,
                downloadUrl = null,
                releaseNotes = "",
                isChecking = false,
                checkMessage = "Your app is up to date!"
            )
        }

        _updateInfo.value = result

        // Post system notification if auto-check found a newer version
        if (result.hasUpdate && isAutoCheck) {
            sendUpdateNotification(result.latestVersion)
        }

        result
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
