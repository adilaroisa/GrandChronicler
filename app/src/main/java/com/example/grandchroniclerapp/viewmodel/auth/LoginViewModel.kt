package com.example.grandchroniclerapp.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.LoginRequest
import com.example.grandchroniclerapp.repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun updateEmail(input: String) { email = input }
    fun updatePassword(input: String) { password = input }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            loginUiState = LoginUiState.Error("Email dan Password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            loginUiState = LoginUiState.Loading
            try {
                val request = LoginRequest(email = email, password = password)
                val response = authRepository.login(request)

                if (response.status) {
                    // Menyimpan ID USER agar data profil bersifat dinamis
                    userPreferences.saveUserId(response.data!!.user_id)
                    loginUiState = LoginUiState.Success
                } else {
                    loginUiState = LoginUiState.Error(response.message)
                }
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Email atau Password salah"
                    404 -> "Akun tidak ditemukan"
                    else -> "Gagal Login (Kode: ${e.code()})"
                }
                loginUiState = LoginUiState.Error(errorMessage)
            } catch (e: IOException) {
                loginUiState = LoginUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                loginUiState = LoginUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun resetState() {
        loginUiState = LoginUiState.Idle
        email = ""
        password = ""
    }
}