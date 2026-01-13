package com.example.grandchroniclerapp.viewmodel.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.UpdateUserRequest
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface EditProfileUiState {
    object Idle : EditProfileUiState
    object Loading : EditProfileUiState
    object Success : EditProfileUiState
    object DeleteSuccess : EditProfileUiState // TAMBAHKAN INI
    data class Error(val message: String) : EditProfileUiState
}

class EditProfileViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var uiState: EditProfileUiState by mutableStateOf(EditProfileUiState.Idle)
        private set

    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var bio by mutableStateOf("")

    private var initialFullName = ""
    private var initialEmail = ""
    private var initialBio = ""

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val loggedInId = userPreferences.getUserId.first()
                if (loggedInId != -1) {
                    val response = repository.getUserDetail(loggedInId)
                    if (response.status && response.data != null) {
                        fullName = response.data.full_name
                        email = response.data.email
                        bio = response.data.bio ?: ""
                        initialFullName = fullName
                        initialEmail = email
                        initialBio = bio
                    }
                }
            } catch (e: Exception) { /* Silent fail */ }
        }
    }

    fun hasChanges(): Boolean {
        return fullName != initialFullName || email != initialEmail || bio != initialBio || password.isNotEmpty()
    }

    fun submitUpdate() {
        if (fullName.isBlank() || email.isBlank()) {
            uiState = EditProfileUiState.Error("Nama dan Email tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val loggedInId = userPreferences.getUserId.first()
                if (loggedInId != -1) {
                    val request = UpdateUserRequest(fullName, email, password, bio)
                    val response = repository.updateUser(loggedInId, request)
                    if (response.status) uiState = EditProfileUiState.Success
                    else uiState = EditProfileUiState.Error(response.message ?: "Gagal update")
                }
            } catch (e: IOException) {
                uiState = EditProfileUiState.Error("Koneksi bermasalah")
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error("Gagal update: ${e.message}")
            }
        }
    }

    // --- FUNGSI HAPUS AKUN BARU ---
    fun deleteAccount() {
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val loggedInId = userPreferences.getUserId.first()
                if (loggedInId != -1) {
                    val response = repository.deleteUser(loggedInId) // Pastikan fungsi ini ada di repository
                    if (response.status) {
                        userPreferences.saveUserId(-1) // Logout user secara lokal
                        uiState = EditProfileUiState.DeleteSuccess
                    } else {
                        uiState = EditProfileUiState.Error(response.message ?: "Gagal menghapus akun")
                    }
                }
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}