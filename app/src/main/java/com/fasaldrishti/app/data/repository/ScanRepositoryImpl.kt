package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.local.ScanDao
import com.fasaldrishti.app.data.local.ScanEntity
import com.fasaldrishti.app.data.ml.TFLiteDiseaseClassifier
import com.fasaldrishti.app.data.remote.NvidiaClient
import com.fasaldrishti.app.data.remote.PredictApi
import com.fasaldrishti.app.data.remote.SupabaseManager
import com.fasaldrishti.app.domain.model.DiseaseInfo
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.model.SyncStatus
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ScanRepositoryImpl(
    private val scanDao: ScanDao,
    private val predictApi: PredictApi,
    private val supabaseManager: SupabaseManager,
    private val onDeviceClassifier: TFLiteDiseaseClassifier,
    private val diseaseRepository: DiseaseRepository,
    private val nvidiaClient: NvidiaClient? = null
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
            // 1. PRIMARY: High-speed On-Device MobileNetV2 Neural Network Inference directly on phone (100% Offline)
            val onDeviceResult = onDeviceClassifier.classifyImage(imageFile)
            val predictedClass = onDeviceResult.predictedClass
            var confidence = onDeviceResult.confidence

            var cropName: String
            var diseaseName: String
            var severity: String
            var symptoms: String
            var treatment: String
            var isInvalidCrop = false
            var finalPredictedClass = predictedClass

            // 2. CHECK DATASET MATCH vs FALLBACK AI MODEL:
            // If confidence >= 50%, the crop leaf matches our 38 on-device categories
            if (confidence >= 0.50f) {
                val diseaseInfoResult = diseaseRepository.getDiseaseInfo(predictedClass)
                val diseaseInfo = diseaseInfoResult.getOrNull()

                cropName = diseaseInfo?.cropName ?: predictedClass.substringBefore("___").replace("_", " ")
                diseaseName = diseaseInfo?.diseaseName ?: predictedClass.substringAfter("___").replace("_", " ")
                severity = diseaseInfo?.severity ?: if (diseaseName.contains("healthy", ignoreCase = true)) "None" else "Moderate"
                symptoms = diseaseInfo?.symptoms ?: "Water-soaked lesions on leaf surfaces."
                treatment = diseaseInfo?.treatment ?: "Apply recommended fungicide and maintain proper plant spacing."
                finalPredictedClass = predictedClass
            } else {
                // 3. FALLBACK: Scanned photo is NOT in 38-class dataset or low confidence -> Shift to Fallback AI Vision Model
                val fallbackResult = nvidiaClient?.diagnoseCropImage(imageFile)
                val fallbackData = fallbackResult?.getOrNull()

                if (fallbackData != null) {
                    cropName = fallbackData.cropName
                    diseaseName = fallbackData.diseaseName
                    severity = fallbackData.severity
                    confidence = fallbackData.confidence
                    symptoms = fallbackData.symptoms
                    treatment = fallbackData.treatment

                    if (severity.equals("Invalid", ignoreCase = true) || cropName.contains("Non-Crop", ignoreCase = true)) {
                        isInvalidCrop = true
                        finalPredictedClass = "Invalid_Crop"
                    } else {
                        isInvalidCrop = false
                        finalPredictedClass = "${cropName}___${diseaseName}".replace(" ", "_")
                    }
                } else {
                    // If device is offline and cannot reach cloud fallback AI
                    isInvalidCrop = false
                    cropName = "Unclassified Plant / Crop"
                    diseaseName = "Offline Analysis Inconclusive"
                    severity = "Low"
                    symptoms = "Leaf features did not match the 38 on-device offline models with high certainty."
                    treatment = "Connect to mobile data/Wi-Fi to trigger deep AI multimodal vision diagnosis, or retake a close-up photo in good light."
                    finalPredictedClass = "Unclassified_Crop"
                }
            }

            // 4. Upload image to Supabase Storage in background or save local path
            val uploadResult = supabaseManager.uploadCropImage(imageFile)
            val storedImageUrl = uploadResult.getOrDefault(imageFile.absolutePath)

            // 5. Save to Room database for instant offline history access
            val scanRecord = ScanRecord(
                id = UUID.randomUUID().toString(),
                imageUrl = storedImageUrl,
                predictedClass = finalPredictedClass,
                confidence = confidence,
                cropName = cropName,
                diseaseName = diseaseName,
                severity = severity,
                symptoms = symptoms,
                treatment = treatment,
                timestamp = System.currentTimeMillis()
            )

            scanDao.insertScan(ScanEntity.fromDomain(scanRecord))

            // 6. Automatic background sync with Supabase Cloud
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

    override suspend fun deleteScan(scanId: String) {
        scanDao.deleteScan(scanId)
        CoroutineScope(Dispatchers.IO).launch {
            supabaseManager.deleteScanFromCloud(scanId)
        }
    }

    override suspend fun clearAllScans() {
        scanDao.clearAll()
        CoroutineScope(Dispatchers.IO).launch {
            supabaseManager.clearAllScansFromCloud()
        }
    }
}
