package com.fasaldrishti.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = false
)

class ResultViewModel(
    private val scanRepository: ScanRepository,
    private val diseaseRepository: DiseaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadScanResult(scanId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val scan = scanRepository.getScanById(scanId)
            if (scan != null) {
                val diseaseInfoResult = diseaseRepository.getDiseaseInfo(scan.predictedClass)
                _uiState.value = _uiState.value.copy(
                    scanRecord = scan,
                    diseaseInfo = diseaseInfoResult.getOrNull(),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
