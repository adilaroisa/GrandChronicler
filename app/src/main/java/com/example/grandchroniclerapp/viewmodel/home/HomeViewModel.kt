package com.example.grandchroniclerapp.viewmodel.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.launch
import java.io.IOException

// 1. Definisi State UI Home
sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val articles: List<Article>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

// 2. Class ViewModel
class HomeViewModel(private val repository: ArticleRepository) : ViewModel() {

    // Menyimpan state UI saat ini
    var homeUiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    init {
        getArticles()
    }

    // Fungsi untuk mengambil semua artikel publik
    fun getArticles() {
        viewModelScope.launch {
            homeUiState = HomeUiState.Loading
            try {
                // Mengambil data dari repository
                val response = repository.getArticles(null)

                if (response.status) {
                    // Pastikan response.data tidak null, jika null berikan list kosong
                    homeUiState = HomeUiState.Success(response.data ?: emptyList())
                } else {
                    // PERBAIKAN: Gunakan operator Elvis untuk menangani String? ke String
                    homeUiState = HomeUiState.Error(response.message ?: "Gagal memuat artikel dari server")
                }
            } catch (e: IOException) {
                // Error koneksi internet
                homeUiState = HomeUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                // Error sistem lainnya, pastikan message aman dari null
                homeUiState = HomeUiState.Error("Terjadi kesalahan: ${e.message ?: "Kesalahan tidak diketahui"}")
            }
        }
    }
}