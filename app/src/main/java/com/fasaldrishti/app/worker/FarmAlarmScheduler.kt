package com.fasaldrishti.app.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fasaldrishti.app.domain.model.FarmReminder

object FarmAlarmScheduler {

    private const val TAG = "FarmAlarmScheduler"

    fun scheduleReminder(context: Context, reminder: FarmReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = reminder.dueDate
        if (triggerAt <= System.currentTimeMillis()) {
            Log.d(TAG, "Reminder ${reminder.id} is already past due. Skipping alarm schedule.")
            return
        }

        val intent = Intent(context, FarmReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", if (reminder.titleLocalized.isNotBlank()) reminder.titleLocalized else reminder.titleEn)
            putExtra("reminder_message", if (reminder.messageLocalized.isNotBlank()) reminder.messageLocalized else reminder.messageEn)
            putExtra("crop_name", reminder.cropName)
            putExtra("disease_name", reminder.diseaseName)
        }

        val notificationId = reminder.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(alarmManager, triggerAt, pendingIntent)
        Log.d(TAG, "Scheduled exact alarm for reminder ${reminder.id} at $triggerAt")
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FarmReminderReceiver::class.java)
        val notificationId = reminderId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for reminder $reminderId")
        }
    }

    fun schedule10SecondTest(context: Context, title: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAt = System.currentTimeMillis() + 10000L // 10 seconds

        val intent = Intent(context, TestReminderReceiver::class.java).apply {
            putExtra("test_title", title)
            putExtra("test_message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            TestReminderReceiver.TEST_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(alarmManager, triggerAt, pendingIntent)
        Log.d(TAG, "Scheduled 10-second test alarm at $triggerAt")
    }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        triggerAt: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    Log.w(TAG, "Exact alarm permission not granted. Falling back to setAndAllowWhileIdle")
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm, attempting inexact fallback", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed inexact alarm fallback", fallbackEx)
            }
        }
    }
}
