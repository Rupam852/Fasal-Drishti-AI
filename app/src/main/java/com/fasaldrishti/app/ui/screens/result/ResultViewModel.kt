package com.fasaldrishti.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.data.local.AppLanguage
import com.fasaldrishti.app.data.local.ScanTranslationManager
import com.fasaldrishti.app.data.local.TranslatedDossier
import com.fasaldrishti.app.data.remote.GeminiClient
import com.fasaldrishti.app.data.remote.NvidiaClient
import com.fasaldrishti.app.domain.model.DiseaseInfo
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.repository.DiseaseRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val scanRecord: ScanRecord? = null,
    val diseaseInfo: DiseaseInfo? = null,
    val isLoading: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val translatedDossier: TranslatedDossier? = null,
    val isTranslating: Boolean = false,
    val translationMessage: String? = null
)

class ResultViewModel(
    private val scanRepository: ScanRepository,
    private val diseaseRepository: DiseaseRepository,
    private val geminiClient: GeminiClient? = null,
    private val nvidiaClient: NvidiaClient? = null,
    private val translationManager: ScanTranslationManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private var currentScanId: String = ""

    fun loadScanResult(scanId: String) {
        currentScanId = scanId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val scan = scanRepository.getScanById(scanId)
            if (scan != null) {
                val diseaseInfoResult = diseaseRepository.getDiseaseInfo(scan.predictedClass)
                val diseaseInfo = diseaseInfoResult.getOrNull()

                // Check saved translation for this specific scan
                val savedLangCode = translationManager?.getSavedLanguage(scanId) ?: "en"
                val savedLang = AppLanguage.fromCode(savedLangCode)
                val cachedDossier = if (savedLang != AppLanguage.ENGLISH) {
                    translationManager?.getTranslatedDossier(scanId, savedLangCode)
                } else null

                _uiState.value = _uiState.value.copy(
                    scanRecord = scan,
                    diseaseInfo = diseaseInfo,
                    isLoading = false,
                    currentLanguage = savedLang,
                    translatedDossier = cachedDossier
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun translateTo(targetLanguage: AppLanguage) {
        val scan = _uiState.value.scanRecord ?: return
        val diseaseInfo = _uiState.value.diseaseInfo

        if (targetLanguage == AppLanguage.ENGLISH) {
            translationManager?.setSavedLanguage(currentScanId, "en")
            _uiState.value = _uiState.value.copy(
                currentLanguage = AppLanguage.ENGLISH,
                translatedDossier = null,
                isTranslating = false,
                translationMessage = null
            )
            return
        }

        // Check if already in cache
        val cached = translationManager?.getTranslatedDossier(currentScanId, targetLanguage.code)
        if (cached != null) {
            translationManager?.setSavedLanguage(currentScanId, targetLanguage.code)
            _uiState.value = _uiState.value.copy(
                currentLanguage = targetLanguage,
                translatedDossier = cached,
                isTranslating = false,
                translationMessage = null
            )
            return
        }

        // Execute AI Translation
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTranslating = true,
                translationMessage = "Translating diagnosis into ${targetLanguage.nativeName} with AI..."
            )

            val originalDiseaseName = scan.diseaseName
            val originalSymptoms = scan.symptoms.ifBlank {
                diseaseInfo?.symptoms ?: "Discolored lesions, leaf necrosis, or mold patches observed on leaf surfaces."
            }
            val originalTreatment = scan.treatment.ifBlank {
                diseaseInfo?.treatment ?: "Spray recommended copper-based or systemic fungicide (e.g. Mancozeb 75% WP @ 2.5g/L water)."
            }
            val originalPrevention = diseaseInfo?.prevention ?: "Spray 5% Neem oil extract, prune and destroy severely infected leaves, and maintain proper crop spacing."

            var translationResult: Result<TranslatedDossier>? = null

            if (geminiClient != null) {
                translationResult = geminiClient.translateDossier(
                    languageCode = targetLanguage.code,
                    targetLanguageName = targetLanguage.englishName,
                    targetLanguageNative = targetLanguage.nativeName,
                    diseaseName = originalDiseaseName,
                    symptoms = originalSymptoms,
                    treatment = originalTreatment,
                    prevention = originalPrevention
                )
            }

            if (translationResult == null || translationResult.isFailure) {
                if (nvidiaClient != null) {
                    translationResult = nvidiaClient.translateDossier(
                        languageCode = targetLanguage.code,
                        targetLanguageName = targetLanguage.englishName,
                        targetLanguageNative = targetLanguage.nativeName,
                        diseaseName = originalDiseaseName,
                        symptoms = originalSymptoms,
                        treatment = originalTreatment,
                        prevention = originalPrevention
                    )
                }
            }

            if (translationResult != null && translationResult.isSuccess) {
                val dossier = translationResult.getOrThrow()
                translationManager?.saveTranslatedDossier(currentScanId, dossier)
                _uiState.value = _uiState.value.copy(
                    currentLanguage = targetLanguage,
                    translatedDossier = dossier,
                    isTranslating = false,
                    translationMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    translationMessage = "Translation failed. Please check network connection."
                )
            }
        }
    }
}
