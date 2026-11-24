package com.fasaldrishti.app.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.fasaldrishti.app.FasalDrishtiApp
import com.fasaldrishti.app.data.remote.UpdateManager
import java.util.concurrent.TimeUnit

/**
 * Background worker that periodically checks for app updates (every 1 hour)
 * even when the app is completely closed or killed.
 */
class AppUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "AppUpdateWorker checking for updates in background...")

        val prefs = context.getSharedPreferences("fasal_updater_prefs", Context.MODE_PRIVATE)
        val autoCheckEnabled = prefs.getBoolean("auto_check_update", true)
        if (!autoCheckEnabled) {
            Log.d(TAG, "Auto check for update is disabled in settings. Skipping.")
            return Result.success()
        }

        try {
            val app = context.applicationContext as? FasalDrishtiApp
            val updateManager = app?.updateManager ?: UpdateManager(context)

            val updateInfo = updateManager.checkForUpdates(isAutoCheck = true)
            if (updateInfo.hasUpdate) {
                val latestVer = updateInfo.latestVersion
                val lastNotifiedVer = prefs.getString("last_notified_version", "")
                val lastNotifiedTime = prefs.getLong("last_notified_time", 0L)
                val currentTime = System.currentTimeMillis()

                // Don't spam: Notify if this is a new version or if at least 4 hours passed since last reminder
                val fourHoursMillis = 4L * 3600L * 1000L
                if (lastNotifiedVer != latestVer || (currentTime - lastNotifiedTime) > fourHoursMillis) {
                    updateManager.sendUpdateNotification(latestVer)
                    prefs.edit()
                        .putString("last_notified_version", latestVer)
                        .putLong("last_notified_time", currentTime)
                        .apply()
                    Log.d(TAG, "Triggered background update notification for version: $latestVer")
                } else {
                    Log.d(TAG, "Update available ($latestVer) but notification was recently shown. Cooldown active.")
                }
            } else {
                Log.d(TAG, "App is already up to date. Latest version: ${updateInfo.latestVersion}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates in background", e)
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        const val TAG = "AppUpdateWorker"
        private const val WORK_NAME = "fasal_app_update_periodic"

        /**
         * Schedules periodic background update checks (every 1 hour)
         * Only triggers when the device is actively connected to the internet.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 1 Hour interval with 15 minutes flex window
            val periodicWorkRequest = PeriodicWorkRequestBuilder<AppUpdateWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )

            Log.d(TAG, "AppUpdateWorker periodic check scheduled (every 1 hour with network constraint).")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "AppUpdateWorker cancelled.")
        }
    }
}
