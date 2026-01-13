package com.example.grandchroniclerapp.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.RegisterRequest
import com.example.grandchroniclerapp.repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface RegisterUiState {
    object Idle : RegisterUiState
    object Loading : RegisterUiState
    object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var registerUiState: RegisterUiState by mutableStateOf(RegisterUiState.Idle)
        private set

    var fullName by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun updateFullName(input: String) { fullName = input }
    fun updateEmail(input: String) { email = input }
    fun updatePassword(input: String) { password = input }

    fun register() {
        // --- VALIDASI INPUT ---

        // Gunakan .trim() agar spasi tidak sengaja terhitung
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPassword.isBlank()) {
            registerUiState = RegisterUiState.Error("Semua kolom harus diisi")
            return
        }

        //  Cek apakah mengandung '@' dan '.', bukan endsWith
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            registerUiState = RegisterUiState.Error("Format Email Salah")
            return
        }

        if (cleanPassword.length < 6) {
            registerUiState = RegisterUiState.Error("Password minimal 6 karakter")
            return
        }

        // --- PROSES REGISTER ---
        viewModelScope.launch {
            registerUiState = RegisterUiState.Loading
            try {
                // Gunakan data yang sudah di-trim
                val request = RegisterRequest(
                    full_name = cleanName,
                    email = cleanEmail,
                    password = cleanPassword
                )

                val response = authRepository.register(request)
                if (response.status) {
                    registerUiState = RegisterUiState.Success
                } else {
                    registerUiState = RegisterUiState.Error(response.message)
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> registerUiState = RegisterUiState.Error("Email ini sudah terdaftar")
                    else -> registerUiState = RegisterUiState.Error("Gagal Daftar (Kode: ${e.code()})")
                }
            } catch (e: IOException) {
                registerUiState = RegisterUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                registerUiState = RegisterUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun resetState() {
        registerUiState = RegisterUiState.Idle
        fullName = ""
        email = ""
        password = ""
    }
}