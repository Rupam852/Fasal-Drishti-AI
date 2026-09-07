package com.fasaldrishti.app.ui.screens.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

sealed class ScanState {
    object Idle : ScanState()
    object Capturing : ScanState()
    object Analyzing : ScanState()
    data class Success(val scanRecord: ScanRecord) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScanViewModel(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun processCapturedImage(file: File) {
        viewModelScope.launch {
            _scanState.value = ScanState.Analyzing
            val result = scanRepository.performScan(file)
            result.onSuccess { scanRecord ->
                _scanState.value = ScanState.Success(scanRecord)
            }.onFailure { error ->
                _scanState.value = ScanState.Error(error.localizedMessage ?: "Analysis failed")
            }
        }
    }

    fun processGalleryUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _scanState.value = ScanState.Analyzing
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("gallery_crop_", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                processCapturedImage(tempFile)
            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Failed to load gallery image")
            }
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Idle
    }
}
