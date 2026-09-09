package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.local.ScanDao
import com.fasaldrishti.app.data.local.ScanEntity
import com.fasaldrishti.app.data.ml.TFLiteDiseaseClassifier
import com.fasaldrishti.app.data.remote.GeminiClient
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
    private val geminiClient: GeminiClient? = null,
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
            // 1. LAYER 1: Fast On-Device Neural Vision Model Inference (Crop & Disease preliminary classification)
            val onDeviceResult = onDeviceClassifier.classifyImage(imageFile)
            val preliminaryClass = onDeviceResult.predictedClass
            val preliminaryConfidence = onDeviceResult.confidence

            val diseaseInfoResult = diseaseRepository.getDiseaseInfo(preliminaryClass)
            val localDiseaseInfo = diseaseInfoResult.getOrNull()

            val preliminaryCrop = localDiseaseInfo?.cropName ?: preliminaryClass.substringBefore("___").replace("_", " ")
            val preliminaryDisease = localDiseaseInfo?.diseaseName ?: preliminaryClass.substringAfter("___").replace("_", " ")

            var cropName = preliminaryCrop
            var diseaseName = preliminaryDisease
            var severity = localDiseaseInfo?.severity ?: if (diseaseName.contains("healthy", ignoreCase = true)) "None" else "Moderate"
            var confidence = preliminaryConfidence
            var symptoms = localDiseaseInfo?.symptoms ?: "Visual foliar lesions or discoloration observed on crop leaf surface."
            var treatment = localDiseaseInfo?.treatment ?: "Apply recommended fungicide and maintain proper plant spacing."
            var finalPredictedClass = preliminaryClass

            var verificationSuccess = false

            // 2. LAYER 2 (PRIMARY): Google Gemini Multimodal AI Vision Verification
            if (geminiClient != null) {
                val geminiResult = geminiClient.verifyCropDiagnosis(
                    imageFile = imageFile,
                    initialCropName = preliminaryCrop,
                    initialDiseaseName = preliminaryDisease,
                    initialConfidence = preliminaryConfidence
                )
                val verifiedData = geminiResult.getOrNull()
                if (verifiedData != null) {
                    cropName = verifiedData.cropName
                    diseaseName = verifiedData.diseaseName
                    severity = verifiedData.severity
                    confidence = verifiedData.confidence
                    symptoms = verifiedData.symptoms
                    treatment = verifiedData.treatment

                    if (severity.equals("Invalid", ignoreCase = true) || cropName.contains("Non-Crop", ignoreCase = true)) {
                        finalPredictedClass = "Invalid_Crop"
                    } else {
                        finalPredictedClass = "${cropName}___${diseaseName}".replace(" ", "_")
                    }
                    verificationSuccess = true
                }
            }

            // 3. LAYER 2 (FALLBACK / SECONDARY): NVIDIA NIM Vision AI (if Gemini didn't respond or failed)
            if (!verificationSuccess && nvidiaClient != null) {
                val nvidiaResult = nvidiaClient.verifyCropDiagnosis(
                    imageFile = imageFile,
                    initialCropName = preliminaryCrop,
                    initialDiseaseName = preliminaryDisease,
                    initialConfidence = preliminaryConfidence
                )
                val verifiedData = nvidiaResult.getOrNull()
                if (verifiedData != null) {
                    cropName = verifiedData.cropName
                    diseaseName = verifiedData.diseaseName
                    severity = verifiedData.severity
                    confidence = verifiedData.confidence
                    symptoms = verifiedData.symptoms
                    treatment = verifiedData.treatment

                    if (severity.equals("Invalid", ignoreCase = true) || cropName.contains("Non-Crop", ignoreCase = true)) {
                        finalPredictedClass = "Invalid_Crop"
                    } else {
                        finalPredictedClass = "${cropName}___${diseaseName}".replace(" ", "_")
                    }
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
