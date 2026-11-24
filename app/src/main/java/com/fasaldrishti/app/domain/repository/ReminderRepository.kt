package com.fasaldrishti.app.domain.repository

import com.fasaldrishti.app.domain.model.FarmReminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminders(userId: String): Flow<List<FarmReminder>>
    suspend fun getReminderById(id: String): FarmReminder?
    suspend fun getPendingDueReminders(userId: String, currentTime: Long): List<FarmReminder>
    fun getUntranslatedCount(userId: String): Flow<Int>
    suspend fun saveReminder(reminder: FarmReminder)
    suspend fun markCompleted(id: String)
    suspend fun setPaused(id: String, isPaused: Boolean, pauseUntil: Long = 0L)
    suspend fun updateTranslation(id: String, titleLocalized: String, messageLocalized: String, languageCode: String)
    suspend fun markAllForTranslation(userId: String, newLang: String, needsTranslation: Boolean)
    suspend fun deleteReminder(id: String)
    suspend fun clearUserReminders(userId: String)
}
