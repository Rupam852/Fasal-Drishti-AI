package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.local.ReminderDao
import com.fasaldrishti.app.data.local.ReminderEntity
import com.fasaldrishti.app.domain.model.FarmReminder
import com.fasaldrishti.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getReminders(userId: String): Flow<List<FarmReminder>> {
        return reminderDao.getReminders(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: String): FarmReminder? {
        return reminderDao.getReminderById(id)?.toDomain()
    }

    override suspend fun getPendingDueReminders(userId: String, currentTime: Long): List<FarmReminder> {
        return reminderDao.getPendingDueReminders(userId, currentTime).map { it.toDomain() }
    }

    override fun getUntranslatedCount(userId: String): Flow<Int> {
        return reminderDao.getUntranslatedCount(userId)
    }

    override suspend fun saveReminder(reminder: FarmReminder) {
        reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun markCompleted(id: String) {
        reminderDao.markCompleted(id)
    }

    override suspend fun setPaused(id: String, isPaused: Boolean, pauseUntil: Long) {
        reminderDao.setPaused(id, isPaused, pauseUntil)
    }

    override suspend fun updateTranslation(id: String, titleLocalized: String, messageLocalized: String, languageCode: String) {
        reminderDao.updateTranslation(id, titleLocalized, messageLocalized, languageCode)
    }

    override suspend fun markAllForTranslation(userId: String, newLang: String, needsTranslation: Boolean) {
        reminderDao.markAllForTranslation(userId, newLang, needsTranslation)
    }

    override suspend fun deleteReminder(id: String) {
        reminderDao.deleteReminder(id)
    }

    override suspend fun clearUserReminders(userId: String) {
        reminderDao.clearUserReminders(userId)
    }
}
