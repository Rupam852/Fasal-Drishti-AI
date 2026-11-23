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
    private val nvidiaClient: NvidiaClient? = null,
    private val aiConfigManager: com.fasaldrishti.app.data.local.AiConfigManager? = null,
    private val scanSettingsManager: com.fasaldrishti.app.data.local.ScanSettingsManager? = null
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

            // Guard for Non-Crop / Random objects in offline detection:
            // If model confidence is low, mark as Invalid_Crop / No Plant Leaf Detected
            if (preliminaryConfidence < 0.45f) {
                cropName = "Non-Crop Object"
                diseaseName = "No Plant Leaf Detected"
                severity = "Invalid"
                finalPredictedClass = "Invalid_Crop"
                symptoms = "No recognized agricultural crop leaf was detected. The photo may contain a non-plant object, person, animal, vehicle, or is blurry."
                treatment = "Please align a real diseased agricultural crop leaf inside the camera reticle in good lighting and scan again."
            }

            var verificationSuccess = false

            val primaryEngine = aiConfigManager?.configState?.value?.primaryProvider ?: com.fasaldrishti.app.data.local.AiProvider.GEMINI

            // Function to verify with Gemini
            val verifyWithGemini: suspend () -> Boolean = {
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
                        true
                    } else false
                } else false
            }

            // Function to verify with NVIDIA
            val verifyWithNvidia: suspend () -> Boolean = {
                if (nvidiaClient != null) {
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
                        true
                    } else false
                } else false
            }

            val isOfflineEnabled = scanSettingsManager?.isOfflineModelEnabled?.value ?: true
            val isOnlineEnabled = scanSettingsManager?.isOnlineVerificationEnabled?.value ?: true

            // Execute in user-configured priority order (Primary -> Secondary fallback) if online verification is enabled
            if (isOnlineEnabled) {
                if (primaryEngine == com.fasaldrishti.app.data.local.AiProvider.NVIDIA) {
                    verificationSuccess = verifyWithNvidia()
                    if (!verificationSuccess) {
                        verificationSuccess = verifyWithGemini()
                    }
                } else {
                    verificationSuccess = verifyWithGemini()
                    if (!verificationSuccess) {
                        verificationSuccess = verifyWithNvidia()
                    }
                }
            }

            // If online verification failed and offline model was disabled by user, fail
            if (!verificationSuccess && !isOfflineEnabled) {
                val isCustomKey = aiConfigManager?.configState?.value?.isCustomMode == true
                if (isCustomKey) {
                    return@withContext Result.failure(
                        com.fasaldrishti.app.domain.model.AiApiKeyException()
                    )
                } else {
                    return@withContext Result.failure(
                        com.fasaldrishti.app.domain.model.AiUnreachableException()
                    )
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
                timestamp = System.currentTimeMillis(),
                isVerified = verificationSuccess
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

    override suspend fun verifyExistingScan(scanId: String): Result<ScanRecord> = withContext(Dispatchers.IO) {
        try {
            val scan = scanDao.getScanById(scanId)?.toDomain() 
                ?: return@withContext Result.failure(Exception("Scan record not found"))

            val localCandidate = File(scan.imageUrl)
            val imageFile = if (localCandidate.exists()) {
                localCandidate
            } else if (scan.imageUrl.startsWith("http://") || scan.imageUrl.startsWith("https://")) {
                val tempFile = File.createTempFile("verify_scan_", ".jpg")
                val url = java.net.URL(scan.imageUrl)
                url.openStream().use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } else {
                return@withContext Result.failure(Exception("Image file not found for verification"))
            }

            val primaryEngine = aiConfigManager?.configState?.value?.primaryProvider ?: com.fasaldrishti.app.data.local.AiProvider.GEMINI

            var verifiedCrop = scan.cropName
            var verifiedDisease = scan.diseaseName
            var verifiedSeverity = scan.severity
            var verifiedConfidence = scan.confidence
            var verifiedSymptoms = scan.symptoms
            var verifiedTreatment = scan.treatment
            var verifiedClass = scan.predictedClass

            var success = false

            val runGemini: suspend () -> Boolean = {
                if (geminiClient != null) {
                    val res = geminiClient.verifyCropDiagnosis(
                        imageFile = imageFile,
                        initialCropName = scan.cropName,
                        initialDiseaseName = scan.diseaseName,
                        initialConfidence = scan.confidence
                    )
                    val data = res.getOrNull()
                    if (data != null) {
                        verifiedCrop = data.cropName
                        verifiedDisease = data.diseaseName
                        verifiedSeverity = data.severity
                        verifiedConfidence = data.confidence
                        verifiedSymptoms = data.symptoms
                        verifiedTreatment = data.treatment
                        verifiedClass = "${data.cropName}___${data.diseaseName}".replace(" ", "_")
                        true
                    } else false
                } else false
            }

            val runNvidia: suspend () -> Boolean = {
                if (nvidiaClient != null) {
                    val res = nvidiaClient.verifyCropDiagnosis(
                        imageFile = imageFile,
                        initialCropName = scan.cropName,
                        initialDiseaseName = scan.diseaseName,
                        initialConfidence = scan.confidence
                    )
                    val data = res.getOrNull()
                    if (data != null) {
                        verifiedCrop = data.cropName
                        verifiedDisease = data.diseaseName
                        verifiedSeverity = data.severity
                        verifiedConfidence = data.confidence
                        verifiedSymptoms = data.symptoms
                        verifiedTreatment = data.treatment
                        verifiedClass = "${data.cropName}___${data.diseaseName}".replace(" ", "_")
                        true
                    } else false
                } else false
            }

            if (primaryEngine == com.fasaldrishti.app.data.local.AiProvider.NVIDIA) {
                success = runNvidia()
                if (!success) success = runGemini()
            } else {
                success = runGemini()
                if (!success) success = runNvidia()
            }

            if (!success) {
                return@withContext Result.failure(com.fasaldrishti.app.domain.model.AiUnreachableException())
            }

            val verifiedScanRecord = scan.copy(
                cropName = verifiedCrop,
                diseaseName = verifiedDisease,
                severity = verifiedSeverity,
                confidence = verifiedConfidence,
                symptoms = verifiedSymptoms,
                treatment = verifiedTreatment,
                predictedClass = verifiedClass,
                isVerified = true
            )

            Result.success(verifiedScanRecord)
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
