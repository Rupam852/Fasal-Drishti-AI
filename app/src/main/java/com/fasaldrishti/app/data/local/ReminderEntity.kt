package com.fasaldrishti.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasaldrishti.app.domain.model.FarmReminder
import com.fasaldrishti.app.domain.model.ReminderType

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val userId: String = "",
    val cropName: String,
    val diseaseName: String = "",
    val reminderType: String, // SPRAY, INSPECTION, WEATHER
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
    fun toDomain(): FarmReminder {
        val type = try {
            ReminderType.valueOf(reminderType)
        } catch (_: Exception) {
            ReminderType.SPRAY
        }
        return FarmReminder(
            id = id,
            userId = userId,
            cropName = cropName,
            diseaseName = diseaseName,
            reminderType = type,
            titleEn = titleEn,
            messageEn = messageEn,
            titleLocalized = titleLocalized,
            messageLocalized = messageLocalized,
            languageCode = languageCode,
            needsTranslation = needsTranslation,
            dueDate = dueDate,
            isCompleted = isCompleted,
            isPaused = isPaused,
            pauseUntil = pauseUntil,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: FarmReminder): ReminderEntity {
            return ReminderEntity(
                id = domain.id,
                userId = domain.userId,
                cropName = domain.cropName,
                diseaseName = domain.diseaseName,
                reminderType = domain.reminderType.name,
                titleEn = domain.titleEn,
                messageEn = domain.messageEn,
                titleLocalized = domain.titleLocalized,
                messageLocalized = domain.messageLocalized,
                languageCode = domain.languageCode,
                needsTranslation = domain.needsTranslation,
                dueDate = domain.dueDate,
                isCompleted = domain.isCompleted,
                isPaused = domain.isPaused,
                pauseUntil = domain.pauseUntil,
                createdAt = domain.createdAt
            )
        }
    }
}
