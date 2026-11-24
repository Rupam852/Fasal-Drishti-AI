package com.fasaldrishti.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE (userId = :userId OR userId = '') ORDER BY isCompleted ASC, dueDate ASC")
    fun getReminders(userId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE (userId = :userId OR userId = '') AND isCompleted = 0 AND isPaused = 0 AND dueDate <= :currentTime")
    suspend fun getPendingDueReminders(userId: String, currentTime: Long): List<ReminderEntity>

    @Query("SELECT COUNT(*) FROM reminders WHERE (userId = :userId OR userId = '') AND isCompleted = 0 AND needsTranslation = 1")
    fun getUntranslatedCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: String)

    @Query("UPDATE reminders SET isPaused = :isPaused, pauseUntil = :pauseUntil WHERE id = :id")
    suspend fun setPaused(id: String, isPaused: Boolean, pauseUntil: Long)

    @Query("UPDATE reminders SET titleLocalized = :titleLocalized, messageLocalized = :messageLocalized, languageCode = :languageCode, needsTranslation = 0 WHERE id = :id")
    suspend fun updateTranslation(id: String, titleLocalized: String, messageLocalized: String, languageCode: String)

    @Query("UPDATE reminders SET needsTranslation = :needsTranslation, languageCode = :newLang WHERE (userId = :userId OR userId = '') AND isCompleted = 0")
    suspend fun markAllForTranslation(userId: String, newLang: String, needsTranslation: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM reminders WHERE userId = :userId")
    suspend fun clearUserReminders(userId: String)
}
