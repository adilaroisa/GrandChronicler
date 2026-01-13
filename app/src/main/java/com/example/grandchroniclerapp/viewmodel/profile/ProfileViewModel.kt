package com.example.grandchroniclerapp.viewmodel.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.model.User
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: User, val articles: List<Article>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var profileUiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    var deleteMessage by mutableStateOf<String?>(null)

    fun loadUserProfile() {
        viewModelScope.launch {
            profileUiState = ProfileUiState.Loading
            try {
                val userId = userPreferences.getUserId.first()
                if (userId != -1) {
                    // 1. Ambil Data User
                    val userRes = repository.getUserDetail(userId)
                    // 2. Ambil Artikel User
                    val articlesRes = repository.getUserArticles(userId)

                    // --- PERBAIKAN DI SINI ---
                    // Pastikan userRes.data TIDAK NULL sebelum dipakai
                    if (userRes.status && userRes.data != null && articlesRes.status) {
                        profileUiState = ProfileUiState.Success(
                            user = userRes.data, // Kotlin sekarang tau ini User (bukan User?)
                            articles = articlesRes.data
                        )
                    } else {
                        profileUiState = ProfileUiState.Error("Gagal memuat data profil")
                    }
                } else {
                    profileUiState = ProfileUiState.Error("Sesi berakhir")
                }
            } catch (e: Exception) {
                profileUiState = ProfileUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun deleteArticle(articleId: Int) {
        viewModelScope.launch {
            val response = repository.deleteArticle(articleId)
            if (response.status) {
                deleteMessage = "Artikel berhasil dihapus"
                loadUserProfile() // Refresh otomatis setelah hapus
            } else {
                deleteMessage = "Gagal menghapus: ${response.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
        }
    }

    fun messageShown() {
        deleteMessage = null
    }
}