package com.fasaldrishti.app.worker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fasaldrishti.app.MainActivity
import com.fasaldrishti.app.R

class FarmReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FasalDrishti:FarmReminderWakeLock"
        )
        wakeLock?.acquire(10000L) // 10 seconds wake lock

        try {
            val reminderId = intent?.getStringExtra("reminder_id") ?: ""
            val title = intent?.getStringExtra("reminder_title") ?: "🌾 Fasal Drishti Smart Reminder"
            val message = intent?.getStringExtra("reminder_message") ?: "Time for your scheduled farm operation."

            Log.d("FarmReminderReceiver", "Farm reminder alarm triggered for ID: $reminderId")

            CropReminderWorker.createNotificationChannel(context)

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("destination", "reminders")
                putExtra("reminder_id", reminderId)
            }

            val notificationId = if (reminderId.isNotBlank()) reminderId.hashCode() else 99991

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CropReminderWorker.CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e("FarmReminderReceiver", "Error delivering reminder notification", e)
        } finally {
            if (wakeLock?.isHeld == true) {
                wakeLock.release()
            }
        }
    }
}
