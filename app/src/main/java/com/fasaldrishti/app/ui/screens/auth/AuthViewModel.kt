package com.fasaldrishti.app.ui.screens.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasaldrishti.app.domain.model.UserProfile
import com.fasaldrishti.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val user: UserProfile? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        isSuccess = true,
                        isLoading = false,
                        user = user
                    )
                }
            }
        }
    }

    fun getOAuthUrl(provider: String): String {
        return authRepository.getOAuthUrl(provider)
    }

    fun handleAuthCallback(uri: Uri, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.handleAuthCallback(uri)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, user = user)
                onSuccess?.invoke()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.localizedMessage)
            }
        }
    }

    fun signInWithGoogle(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithGoogle()
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, user = user)
                onSuccess?.invoke()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.localizedMessage)
            }
        }
    }

    fun signInWithGitHub(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithGitHub()
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, user = user)
                onSuccess?.invoke()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.localizedMessage)
            }
        }
    }
}
