package com.fasaldrishti.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: String): ScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteScan(id: String)

    @Query("DELETE FROM scans")
    suspend fun clearAll()
}
