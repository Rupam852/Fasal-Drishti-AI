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

        // Smart Re-engagement / Inactivity Alert:
        // If monitoring reminders are enabled, check if the farmer hasn't scanned for > 5 days
        if (monitoringRemindersEnabled) {
            checkAndSendInactivityAlert(context, scanDao, prefs, currentTime)
        }

        return Result.success()
    }

    private suspend fun checkAndSendInactivityAlert(
        context: Context,
        scanDao: com.fasaldrishti.app.data.local.ScanDao,
        prefs: android.content.SharedPreferences,
        currentTime: Long
    ) {
        try {
            val latestScan = scanDao.getLatestScan()
            val fiveDaysMillis = 5L * 24L * 3600L * 1000L
            val threeDaysMillis = 3L * 24L * 3600L * 1000L

            val lastScanTime = latestScan?.timestamp ?: prefs.getLong("app_first_install_time", currentTime)
            val timeSinceLastScan = currentTime - lastScanTime

            // Check if user has been inactive for more than 5 days
            if (timeSinceLastScan >= fiveDaysMillis) {
                val lastAlertTime = prefs.getLong("last_inactivity_alert_timestamp", 0L)
                // Don't spam: wait at least 3 days before sending another inactivity reminder
                if (currentTime - lastAlertTime >= threeDaysMillis) {
                    // Priority 1: Check if user explicitly set reminder_language_override in Reminders screen
                    // Priority 2: Fall back to global app_language
                    val langPrefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
                    val targetLang = prefs.getString("reminder_language_override", null)
                        ?: langPrefs.getString("app_language", "en")
                        ?: "en"

                    val (title, message) = getInactivityMessage(targetLang)

                    showInactivityNotification(context, title, message)
                    prefs.edit().putLong("last_inactivity_alert_timestamp", currentTime).apply()
                    Log.d(TAG, "Sent inactivity re-engagement alert in language: $targetLang")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to run inactivity alert check", e)
        }
    }

    private fun getInactivityMessage(lang: String): Pair<String, String> = when (lang) {
        "hi" -> Pair(
            "🌾 फसल स्वास्थ्य निरीक्षण",
            "आपने कई दिनों से फसल की जांच नहीं की है! अपने खेत को सुरक्षित रखने के लिए एक बार AI स्कैन करें।"
        )
        "bn" -> Pair(
            "🌾 ফসল স্বাস্থ্য পর্যবেক্ষণ",
            "আপনি বেশ কয়েকদিন ধরে ফসলের স্বাস্থ্য পরীক্ষা করেননি! আপনার ফসল সুরক্ষিত রাখতে একটি AI স্ক্যান করুন।"
        )
        "mr" -> Pair(
            "🌾 पीक आरोग्य तपासणी",
            "तुम्ही अनेक दिवसांपासून पिकाची तपासणी केलेली नाही! शेत सुरक्षित ठेवण्यासाठी कृपया नवीन AI स्कॅन करा."
        )
        "pa" -> Pair(
            "🌾 ਫਸਲ ਸਿਹਤ ਨਿਰੀਖਣ",
            "ਤੁਸੀਂ ਕਈ ਦਿਨਾਂ ਤੋਂ ਫਸਲ ਦੀ ਜਾਂਚ ਨਹੀਂ ਕੀਤੀ! ਆਪਣੇ ਖੇਤ ਨੂੰ ਸੁਰੱਖਿਅਤ ਰੱਖਣ ਲਈ ਇੱਕ ਨਵਾਂ AI ਸਕੈਨ ਕਰੋ।"
        )
        "te" -> Pair(
            "🌾 పంట ఆరోగ్య తనిఖీ",
            "మీరు చాలా రోజులుగా పంటను పరిశీలించలేదు! మీ పంటను రక్షించుకోవడానికి కొత్త AI స్కాన్ చేయండి."
        )
        "ta" -> Pair(
            "🌾 பயிர் சுகாதார ஆய்வு",
            "நீங்கள் பல நாட்களாக பயிரை ஆய்வு செய்யவில்லை! உங்கள் பயிரைப் பாதுகாக்க ஒரு புதிய AI ஸ்கேன் செய்யவும்."
        )
        "gu" -> Pair(
            "🌾 પાક આરોગ્ય નિરીક્ષણ",
            "તમે ઘણા દિવસોથી પાકની તપાસ કરી નથી! તમારા ખેતરને સુરક્ષિત રાખવા માટે નવું AI સ્કેન કરો."
        )
        else -> Pair(
            "🌾 Field Health Inspection",
            "You haven't scanned your crops in several days! Take a quick AI leaf scan to keep your farm protected."
        )
    }

    private fun showInactivityNotification(
        context: Context,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "scan")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            INACTIVITY_NOTIFICATION_ID,
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
            NotificationManagerCompat.from(context).notify(INACTIVITY_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission not granted for inactivity alert", e)
        }
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
        const val INACTIVITY_NOTIFICATION_ID = 88888
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

        fun dismissInactivityNotification(context: Context) {
            NotificationManagerCompat.from(context).cancel(INACTIVITY_NOTIFICATION_ID)
        }
    }
}
