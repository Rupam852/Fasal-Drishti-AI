package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.local.ScanDao
import com.fasaldrishti.app.data.local.ScanEntity
import com.fasaldrishti.app.data.ml.TFLiteDiseaseClassifier
import com.fasaldrishti.app.data.remote.PredictApi
import com.fasaldrishti.app.data.remote.SupabaseManager
import com.fasaldrishti.app.domain.model.DiseaseInfo
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.fasaldrishti.app.domain.model.SyncStatus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

class ScanRepositoryImpl(
    private val scanDao: ScanDao,
    private val predictApi: PredictApi,
    private val supabaseManager: SupabaseManager,
    private val onDeviceClassifier: TFLiteDiseaseClassifier,
    private val diseaseRepository: DiseaseRepository
) : ScanRepository {

    override val syncStatus: Flow<SyncStatus> = supabaseManager.syncStatus

    init {
        // Automatically restore scans from Supabase whenever authenticated user connects or app launches
        CoroutineScope(Dispatchers.IO).launch {
            supabaseManager.currentUser.collect { user ->
                if (user != null) {
                    restoreScansFromCloud()
                }
            }
        }
    }

    override suspend fun restoreScansFromCloud() {
        supabaseManager.fetchAndRestoreScansFromCloud(scanDao)
    }

    override fun getAllScans(): Flow<List<ScanRecord>> {
        return scanDao.getAllScans().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getScanById(id: String): ScanRecord? {
        return scanDao.getScanById(id)?.toDomain()
    }

    override suspend fun performScan(imageFile: File): Result<ScanRecord> = withContext(Dispatchers.IO) {
        try {
            // 1. PRIMARY: On-Device MobileNetV2 Neural Network Inference directly on phone
            val onDeviceResult = onDeviceClassifier.classifyImage(imageFile)
            val predictedClass = onDeviceResult.predictedClass
            val confidence = onDeviceResult.confidence

            // 2. Threshold Check: If confidence < 50%, classify as Non-Crop / Invalid Photo
            val isInvalidCrop = confidence < 0.50f

            val cropName: String
            val diseaseName: String
            val severity: String
            val symptoms: String
            val treatment: String

            if (isInvalidCrop) {
                cropName = "Non-Crop Object"
                diseaseName = "No Plant Leaf Detected (अमान्य फोटो)"
                severity = "Invalid"
                symptoms = "AI vision did not detect a recognized agricultural plant leaf. The photo may contain a non-crop object, person, animal, vehicle, or is too blurry/dark."
                treatment = "Please align a clear, well-lit plant leaf inside the camera reticle and take a close-up photo."
            } else {
                // Fetch rich agronomic metadata
                val diseaseInfoResult = diseaseRepository.getDiseaseInfo(predictedClass)
                val diseaseInfo = diseaseInfoResult.getOrNull()

                cropName = diseaseInfo?.cropName ?: predictedClass.substringBefore("___").replace("_", " ")
                diseaseName = diseaseInfo?.diseaseName ?: predictedClass.substringAfter("___").replace("_", " ")
                severity = diseaseInfo?.severity ?: if (diseaseName.contains("healthy", ignoreCase = true)) "None" else "Moderate"
                symptoms = diseaseInfo?.symptoms ?: "Water-soaked lesions on leaf surfaces."
                treatment = diseaseInfo?.treatment ?: "Apply recommended fungicide and maintain proper plant spacing."
            }

            // 3. Upload image to Supabase Storage in background or save local path
            val uploadResult = supabaseManager.uploadCropImage(imageFile)
            val storedImageUrl = uploadResult.getOrDefault(imageFile.absolutePath)

            // 4. Save to Room database for instant offline history access
            val scanRecord = ScanRecord(
                id = UUID.randomUUID().toString(),
                imageUrl = storedImageUrl,
                predictedClass = if (isInvalidCrop) "Invalid_Crop" else predictedClass,
                confidence = confidence,
                cropName = cropName,
                diseaseName = diseaseName,
                severity = severity,
                symptoms = symptoms,
                treatment = treatment,
                timestamp = System.currentTimeMillis()
            )

            scanDao.insertScan(ScanEntity.fromDomain(scanRecord))

            // 5. Automatic background sync with Supabase Cloud
            CoroutineScope(Dispatchers.IO).launch {
                supabaseManager.syncScanRecordToCloud(scanRecord)
            }

            Result.success(scanRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveScan(scan: ScanRecord) {
        scanDao.insertScan(ScanEntity.fromDomain(scan))
    }

    override suspend fun deleteScan(id: String) {
        scanDao.deleteScan(id)
        CoroutineScope(Dispatchers.IO).launch {
            supabaseManager.deleteScanFromCloud(id)
        }
    }

    override suspend fun clearAllScans() {
        scanDao.clearAll()
        CoroutineScope(Dispatchers.IO).launch {
            supabaseManager.clearAllScansFromCloud()
        }
    }
}
