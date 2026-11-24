package com.fasaldrishti.app.worker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fasaldrishti.app.MainActivity
import com.fasaldrishti.app.R

class TestReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("TestReminderReceiver", "10-second background alarm triggered!")

        val title = intent?.getStringExtra("test_title") ?: "🌾 Fasal Drishti Background Test ✅"
        val message = intent?.getStringExtra("test_message")
            ?: "Success! Background notification delivered after 10s delay while app was closed."

        CropReminderWorker.createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "reminders")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            TEST_NOTIFICATION_ID,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CropReminderWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(TEST_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e("TestReminderReceiver", "Permission denied for test notification", e)
        }
    }

    companion object {
        const val TEST_NOTIFICATION_ID = 88888
    }
}
