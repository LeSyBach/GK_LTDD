package com.example.gk_ltdd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gk_ltdd.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val isAdmin: Boolean = false,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState(user = repository.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        repository.currentUser?.let { user ->
            viewModelScope.launch {
                val isAdmin = repository.isAdmin(user.uid)
                _uiState.update { it.copy(isAdmin = isAdmin) }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.login(email, password)
            result.onSuccess { user ->
                val isAdmin = repository.isAdmin(user.uid)
                _uiState.update { it.copy(isLoading = false, user = user, isAdmin = isAdmin) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Đăng nhập thất bại: ${e.message}") }
            }
        }
    }

    fun register(email: String, password: String, username: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.register(email, password)
            result.onSuccess { user ->
                // Save user data to Firestore
                val saveResult = repository.saveUserData(user.uid, username, email, role)
                saveResult.onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Đăng ký thành công! Vui lòng đăng nhập.") }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Lỗi lưu dữ liệu: ${e.message}") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Đăng ký thất bại: ${e.message}") }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update { AuthUiState() }
    }

    fun refreshAdminStatus() {
        _uiState.value.user?.let { user ->
            viewModelScope.launch {
                val isAdmin = repository.isAdmin(user.uid)
                _uiState.update { it.copy(isAdmin = isAdmin) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}