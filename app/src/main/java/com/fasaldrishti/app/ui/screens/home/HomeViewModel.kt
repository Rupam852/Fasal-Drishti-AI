package com.fasaldrishti.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.data.remote.WeatherManager
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.model.SyncStatus
import com.fasaldrishti.app.domain.model.UserProfile
import com.fasaldrishti.app.domain.model.WeatherData
import com.fasaldrishti.app.domain.repository.AuthRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val scanRepository: ScanRepository,
    private val authRepository: AuthRepository,
    private val weatherManager: WeatherManager
) : ViewModel() {

    val user: StateFlow<UserProfile?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentScans: StateFlow<List<ScanRecord>> = scanRepository.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncStatus: StateFlow<SyncStatus> = scanRepository.syncStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus.IDLE)

    val weatherData: StateFlow<WeatherData> = weatherManager.weatherData

    fun refreshWeather() {
        viewModelScope.launch {
            weatherManager.refreshWeather()
        }
    }
}
