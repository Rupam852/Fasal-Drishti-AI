package com.fasaldrishti.app.ui.screens.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.domain.model.FarmReminder
import com.fasaldrishti.app.domain.model.ReminderType
import com.fasaldrishti.app.domain.repository.AuthRepository
import com.fasaldrishti.app.domain.repository.ReminderRepository
import com.fasaldrishti.app.util.ReminderTranslator
import com.fasaldrishti.app.util.TextToSpeechHelper
import com.fasaldrishti.app.worker.CropReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RemindersViewModel(
    private val reminderRepository: ReminderRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("fasal_settings_prefs", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(prefs.getString("reminder_language_override", "en") ?: "en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val ttsHelper = TextToSpeechHelper(context)

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUserId.value = user?.id ?: ""
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val reminders: StateFlow<List<FarmReminder>> = _currentUserId.flatMapLatest { userId ->
        reminderRepository.getReminders(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val untranslatedCount: StateFlow<Int> = _currentUserId.flatMapLatest { userId ->
        reminderRepository.getUntranslatedCount(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markCompleted(id: String) {
        viewModelScope.launch {
            reminderRepository.markCompleted(id)
            CropReminderWorker.dismissNotification(context, id)
            com.fasaldrishti.app.worker.FarmAlarmScheduler.cancelReminder(context, id)
        }
    }

    fun pauseReminder(id: String, days: Int = 2) {
        viewModelScope.launch {
            val pauseUntil = System.currentTimeMillis() + (days * 24L * 3600L * 1000L)
            reminderRepository.setPaused(id, true, pauseUntil)
            CropReminderWorker.dismissNotification(context, id)
            com.fasaldrishti.app.worker.FarmAlarmScheduler.cancelReminder(context, id)
        }
    }

    fun resumeReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.setPaused(id, false, 0L)
            val reminder = reminderRepository.getReminderById(id)
            if (reminder != null && !reminder.isCompleted) {
                com.fasaldrishti.app.worker.FarmAlarmScheduler.scheduleReminder(context, reminder)
            }
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(id)
            CropReminderWorker.dismissNotification(context, id)
            com.fasaldrishti.app.worker.FarmAlarmScheduler.cancelReminder(context, id)
        }
    }

    fun setReminderLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        prefs.edit().putString("reminder_language_override", langCode).apply()

        viewModelScope.launch {
            if (langCode == "en") {
                // English is default, clear translation flag
                reminderRepository.markAllForTranslation(_currentUserId.value, "en", false)
            } else {
                // Mark reminders as needing translation
                reminderRepository.markAllForTranslation(_currentUserId.value, langCode, true)
            }
        }
    }

    fun onGlobalLanguageChanged(newGlobalLang: String) {
        // If user hasn't set an explicit custom reminder override, follow global language
        val hasCustomOverride = prefs.contains("reminder_language_override")
        if (!hasCustomOverride || _selectedLanguage.value == newGlobalLang) {
            setReminderLanguage(newGlobalLang)
        }
    }

    fun translateAllNow() {
        viewModelScope.launch {
            _isTranslating.value = true
            try {
                val targetLang = _selectedLanguage.value
                val list = reminders.value.filter { !it.isCompleted }
                for (reminder in list) {
                    val (title, message) = ReminderTranslator.translateReminder(reminder, targetLang)
                    reminderRepository.updateTranslation(
                        id = reminder.id,
                        titleLocalized = title,
                        messageLocalized = message,
                        languageCode = targetLang
                    )
                }
            } finally {
                _isTranslating.value = false
            }
        }
    }

    fun speakReminder(reminder: FarmReminder) {
        val textToSpeak = "${reminder.displayTitle}. ${reminder.displayMessage}"
        ttsHelper.speak(textToSpeak, reminder.languageCode)
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    fun addSprayReminder(cropName: String, diseaseName: String, daysDelay: Int = 7) {
        viewModelScope.launch {
            val targetLang = _selectedLanguage.value
            val due = System.currentTimeMillis() + (daysDelay * 24L * 3600L * 1000L)
            val id = UUID.randomUUID().toString()

            val initialReminder = FarmReminder(
                id = id,
                userId = _currentUserId.value,
                cropName = cropName,
                diseaseName = diseaseName,
                reminderType = ReminderType.SPRAY,
                titleEn = "$cropName: $diseaseName 2nd Spray Due 💧",
                messageEn = "Time for second scheduled fungicide/pesticide spray for $diseaseName on $cropName. Maintain 15L tank dose.",
                dueDate = due,
                languageCode = targetLang,
                needsTranslation = targetLang != "en"
            )

            val (transTitle, transMsg) = ReminderTranslator.translateReminder(initialReminder, targetLang)
            val readyReminder = initialReminder.copy(
                titleLocalized = transTitle,
                messageLocalized = transMsg,
                needsTranslation = false
            )
            reminderRepository.saveReminder(readyReminder)
            com.fasaldrishti.app.worker.FarmAlarmScheduler.scheduleReminder(context, readyReminder)
        }
    }

    fun addInspectionReminder(cropName: String, daysDelay: Int = 5) {
        viewModelScope.launch {
            val targetLang = _selectedLanguage.value
            val due = System.currentTimeMillis() + (daysDelay * 24L * 3600L * 1000L)
            val id = UUID.randomUUID().toString()

            val initialReminder = FarmReminder(
                id = id,
                userId = _currentUserId.value,
                cropName = cropName,
                diseaseName = "",
                reminderType = ReminderType.INSPECTION,
                titleEn = "🌾 $cropName Weekly Field Inspection",
                messageEn = "Inspect $cropName leaves for early disease symptoms or pest infestation. Take an AI leaf scan to confirm health.",
                dueDate = due,
                languageCode = targetLang,
                needsTranslation = targetLang != "en"
            )

            val (transTitle, transMsg) = ReminderTranslator.translateReminder(initialReminder, targetLang)
            val readyReminder = initialReminder.copy(
                titleLocalized = transTitle,
                messageLocalized = transMsg,
                needsTranslation = false
            )
            reminderRepository.saveReminder(readyReminder)
            com.fasaldrishti.app.worker.FarmAlarmScheduler.scheduleReminder(context, readyReminder)
        }
    }

    fun sendImmediateTestNotification() {
        val targetLang = _selectedLanguage.value
        val (title, msg) = when (targetLang) {
            "hi" -> Pair("🌾 फसल दृष्टि टेस्ट अलर्ट 💧", "[लाइव टेस्ट] आपका नोटिफिकेशन सिस्टम बिल्कुल सही काम कर रहा है! (टैप करके रिमाइंडर्स देखें)")
            "bn" -> Pair("🌾 ফসল দৃষ্টি টেস্ট অ্যালার্ট 💧", "[লাইভ টেস্ট] আপনার নোটিফিকেশন সিস্টেম নিখুঁতভাবে কাজ করছে! (ট্যাপ করে রিমাইন্ডার দেখুন)")
            "mr" -> Pair("🌾 पीक दृष्टी चाचणी सूचना 💧", "[थेट चाचणी] तुमची सूचना प्रणाली सुरळीत सुरू आहे! (तपशील पाहण्यासाठी टॅप करा)")
            else -> Pair("🌾 Fasal Drishti Live Test 💧", "[LIVE TEST] Notification system is working perfectly on this phone! (Tap to open)")
        }

        CropReminderWorker.createNotificationChannel(context)
        val intent = android.content.Intent(context, com.fasaldrishti.app.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("destination", "reminders")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            77777,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, CropReminderWorker.CHANNEL_ID)
            .setSmallIcon(com.fasaldrishti.app.R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(msg)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(msg))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            androidx.core.app.NotificationManagerCompat.from(context).notify(77777, notification)
            android.widget.Toast.makeText(context, "⚡ Notification sent! Check status bar.", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            android.widget.Toast.makeText(context, "Permission error: Please allow notifications", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun scheduleDelayed10SecNotification() {
        val targetLang = _selectedLanguage.value
        val (title, msg) = when (targetLang) {
            "hi" -> Pair("🌾 फसल दृष्टि बैकग्राउंड टेस्ट ✅", "[10-सेकंड टेस्ट] ऐप बैकग्राउंड में बंद होने के बाद भी नोटिफिकेशन सफलतापूर्वक पहुंच गया!")
            "bn" -> Pair("🌾 ফসল দৃষ্টি ব্যাকগ্রাউন্ড টেস্ট ✅", "[১০-সেকেন্ড টেস্ট] অ্যাপ বন্ধ থাকা সত্ত্বেও ব্যাকগ্রাউন্ড নোটিফিকেশন সফলভাবে এসেছে!")
            "mr" -> Pair("🌾 पीक दृष्टी बॅकग्राउंड चाचणी ✅", "[१० सेकंद चाचणी] ॲप बंद असतानाही बॅकग्राउंड सूचना यशस्वीपणे आली!")
            else -> Pair("🌾 Fasal Drishti Background Test ✅", "[10-SEC TEST] Success! Notification arrived while app was closed or in background.")
        }

        com.fasaldrishti.app.worker.FarmAlarmScheduler.schedule10SecondTest(context, title, msg)
        android.widget.Toast.makeText(context, "⏰ 10-Second Countdown Started! Close/minimize app now to test.", android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}
