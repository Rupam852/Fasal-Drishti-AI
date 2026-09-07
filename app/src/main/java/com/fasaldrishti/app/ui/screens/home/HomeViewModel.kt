package com.fasaldrishti.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.domain.model.ScanRecord
import com.fasaldrishti.app.domain.model.UserProfile
import com.fasaldrishti.app.domain.repository.AuthRepository
import com.fasaldrishti.app.domain.repository.ScanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val scanRepository: ScanRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val user: StateFlow<UserProfile?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentScans: StateFlow<List<ScanRecord>> = scanRepository.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
