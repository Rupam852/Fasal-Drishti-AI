package com.fasaldrishti.app.domain.model

data class FarmReminder(
    val id: String,
    val userId: String = "",
    val cropName: String,
    val diseaseName: String = "",
    val reminderType: ReminderType = ReminderType.SPRAY,
    val titleEn: String,
    val messageEn: String,
    val titleLocalized: String = "",
    val messageLocalized: String = "",
    val languageCode: String = "en",
    val needsTranslation: Boolean = false,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val pauseUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = if (titleLocalized.isNotBlank()) titleLocalized else titleEn

    val displayMessage: String
        get() = if (messageLocalized.isNotBlank()) messageLocalized else messageEn
}

enum class ReminderType {
    SPRAY,
    INSPECTION,
    WEATHER
}
