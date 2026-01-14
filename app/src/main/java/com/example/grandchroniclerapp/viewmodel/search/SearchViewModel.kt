package com.example.grandchroniclerapp.viewmodel.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val articles: List<Article>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(private val repository: ArticleRepository) : ViewModel() {

    var searchUiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Menyimpan kategori yang sedang dipilih
    var selectedCategory: Category? by mutableStateOf(null)
        private set

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    private var searchJob: Job? = null

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.status) {
                    categories = response.data
                }
            } catch (e: Exception) {
                // Ignore error for categories currently
            }
        }
    }

    // Fungsi 1: Saat user mengetik di Search Bar
    fun updateQuery(newQuery: String) {
        searchQuery = newQuery
        selectedCategory = null

        if (newQuery.isBlank()) {
            searchUiState = SearchUiState.Idle
            return
        }

        // Debounce
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            searchArticles()
        }
    }

    // Fungsi 2: Saat user KLIK KATEGORI
    fun selectCategory(category: Category) {
        selectedCategory = category
        searchQuery = ""
        searchArticles()
    }

    // Fungsi Pencarian Utama
    fun searchArticles() {
        searchUiState = SearchUiState.Loading
        viewModelScope.launch {
            try {
                // Jika selectedCategory ada -> Kirim categoryId
                // Jika tidak ada -> Kirim searchQuery (text)
                val response = repository.getArticles(
                    query = if (selectedCategory == null) searchQuery else null,
                    categoryId = selectedCategory?.category_id
                )

                if (response.status) {
                    searchUiState = SearchUiState.Success(response.data ?: emptyList())
                } else {
                    searchUiState = SearchUiState.Error(response.message ?: "Gagal memuat")
                }
            } catch (e: Exception) {
                searchUiState = SearchUiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun clearSearch() {
        searchQuery = ""
        selectedCategory = null
        searchUiState = SearchUiState.Idle
    }
}