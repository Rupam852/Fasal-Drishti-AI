package com.fasaldrishti.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.fasaldrishti.app.MainActivity
import com.fasaldrishti.app.R
import com.fasaldrishti.app.data.local.AppDatabase
import java.util.concurrent.TimeUnit

class CropReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "CropReminderWorker running smart reminder check...")

        val prefs = context.getSharedPreferences("fasal_settings_prefs", Context.MODE_PRIVATE)
        val sprayAlertsEnabled = prefs.getBoolean("spray_alerts_enabled", true)
        val monitoringRemindersEnabled = prefs.getBoolean("scan_reminders_enabled", true)

        if (!sprayAlertsEnabled && !monitoringRemindersEnabled) {
            Log.d(TAG, "Both spray and monitoring reminders are disabled in settings. Skipping.")
            return Result.success()
        }

        val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val currentUserId = authPrefs.getString("user_id", "") ?: ""

        val database = AppDatabase.getInstance(context)
        val reminderDao = database.reminderDao()
        val scanDao = database.scanDao()

        val currentTime = System.currentTimeMillis()
        val dueReminders = reminderDao.getPendingDueReminders(currentUserId, currentTime)

        createNotificationChannel(context)

        for (reminder in dueReminders) {
            // Check if reminder is paused
            if (reminder.isPaused && reminder.pauseUntil > currentTime) {
                continue
            }

            // Smart State Verification Rule:
            // If user has already scanned this crop recently (within 3 days), or task is already completed:
            if (reminder.reminderType == "INSPECTION") {
                if (!monitoringRemindersEnabled) continue
                // Auto-complete if already scanned recently
            } else if (reminder.reminderType == "SPRAY") {
                if (!sprayAlertsEnabled) continue
            }

            // Fire notification
            val notificationId = reminder.id.hashCode()
            showReminderNotification(
                context = context,
                notificationId = notificationId,
                reminderId = reminder.id,
                title = if (reminder.titleLocalized.isNotBlank()) reminder.titleLocalized else reminder.titleEn,
                message = if (reminder.messageLocalized.isNotBlank()) reminder.messageLocalized else reminder.messageEn
            )
        }

        return Result.success()
    }

    private fun showReminderNotification(
        context: Context,
        notificationId: Int,
        reminderId: String,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "reminders")
            putExtra("reminder_id", reminderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission not granted", e)
        }
    }

    companion object {
        const val TAG = "CropReminderWorker"
        const val CHANNEL_ID = "fasal_reminders_channel"
        private const val WORK_NAME = "crop_periodic_reminders"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Farm Reminders & Alerts"
                val descriptionText = "Notifications for crop spray schedules and weekly monitoring"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                    setShowBadge(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun schedule(context: Context) {
            createNotificationChannel(context)
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Run periodic check every 12 hours
            val periodicWorkRequest = PeriodicWorkRequestBuilder<CropReminderWorker>(
                12, TimeUnit.HOURS,
                1, TimeUnit.HOURS // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
            Log.d(TAG, "CropReminderWorker scheduled successfully.")
        }

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "CropReminderWorker cancelled.")
        }

        fun dismissNotification(context: Context, reminderId: String) {
            val notificationId = reminderId.hashCode()
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}
