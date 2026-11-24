package com.fasaldrishti.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fasaldrishti.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootCompletedReceiver", "Phone boot completed. Rescheduling all pending farm alarms...")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val currentTime = System.currentTimeMillis()
                    val dueReminders = db.reminderDao().getPendingDueReminders("", currentTime + (30L * 24 * 3600 * 1000))
                    for (entity in dueReminders) {
                        val domain = entity.toDomain()
                        if (!domain.isCompleted && (!domain.isPaused || domain.pauseUntil <= currentTime)) {
                            FarmAlarmScheduler.scheduleReminder(context, domain)
                        }
                    }
                    Log.d("BootCompletedReceiver", "Rescheduled ${dueReminders.size} reminders after boot.")
                } catch (e: Exception) {
                    Log.e("BootCompletedReceiver", "Error rescheduling reminders on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
