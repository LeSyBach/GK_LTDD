package com.example.gk_ltdd.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gk_ltdd.model.User
import com.example.gk_ltdd.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getUsersFlow().collect { users ->
                _uiState.update { it.copy(users = users, isLoading = false) }
            }
        }
    }

    fun addUser(user: User, imageUri: Uri?, fileUri: Uri?, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var imageUrl = user.imageUrl
            var fileUrl = user.fileUrl

            if (imageUri != null) {
                val result = repository.uploadFile(imageUri, "images/${System.currentTimeMillis()}.jpg")
                result.onSuccess { url -> imageUrl = url }
            }
            if (fileUri != null) {
                val result = repository.uploadFile(fileUri, "files/${System.currentTimeMillis()}_$fileName")
                result.onSuccess { url -> fileUrl = url }
            }

            val updatedUser = user.copy(imageUrl = imageUrl, fileUrl = fileUrl, fileName = fileName)

            // ✅ Create user with Firebase Auth account + save to Firestore
            val result = repository.createUserWithAuth(user.email, user.password, updatedUser)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Thêm user thành công! Người dùng có thể đăng nhập ngay." ) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Lỗi: ${e.message}") }
            }
        }
    }

    fun updateUser(user: User, imageUri: Uri?, fileUri: Uri?, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var imageUrl = user.imageUrl
            var fileUrl = user.fileUrl
            var updatedFileName = user.fileName

            if (imageUri != null) {
                val result = repository.uploadFile(imageUri, "images/${System.currentTimeMillis()}.jpg")
                result.onSuccess { url -> imageUrl = url }
            }
            if (fileUri != null) {
                val result = repository.uploadFile(fileUri, "files/${System.currentTimeMillis()}_$fileName")
                result.onSuccess { url ->
                    fileUrl = url
                    updatedFileName = fileName
                }
            }

            val updatedUser = user.copy(imageUrl = imageUrl, fileUrl = fileUrl, fileName = updatedFileName)
            val result = repository.updateUser(updatedUser)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Cập nhật thành công!") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteUser(userId)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Xóa user thành công!") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    suspend fun getUserById(userId: String): User? {
        return repository.getUserById(userId).getOrNull()
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}