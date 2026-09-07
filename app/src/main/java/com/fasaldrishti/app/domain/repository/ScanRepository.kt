package com.fasaldrishti.app.domain.repository

import com.fasaldrishti.app.domain.model.ScanRecord
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ScanRepository {
    fun getAllScans(): Flow<List<ScanRecord>>
    suspend fun getScanById(id: String): ScanRecord?
    suspend fun performScan(imageFile: File): Result<ScanRecord>
    suspend fun saveScan(scan: ScanRecord)
    suspend fun deleteScan(id: String)
    suspend fun clearAllScans()
}
