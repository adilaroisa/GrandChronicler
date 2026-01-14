package com.example.grandchroniclerapp.viewmodel.article

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface UploadUiState {
    object Idle : UploadUiState
    object Loading : UploadUiState
    object Success : UploadUiState
    data class Error(val message: String) : UploadUiState
}

class InsertViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var uiState: UploadUiState by mutableStateOf(UploadUiState.Idle)
        private set

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)
    // STATE TAGS (BARU)
    var tags by mutableStateOf("")

    var imageUris = mutableStateListOf<Uri>()
        private set
    var categories: List<Category> by mutableStateOf(emptyList())
        private set

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val res = repository.getCategories()
                if (res.status) categories = res.data
            } catch (e: Exception) { }
        }
    }

    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateCategory(c: Category) { selectedCategory = c }
    fun updateTags(t: String) { tags = t } // Setter Tags

    fun addImages(uris: List<Uri>) { imageUris.addAll(uris) }
    fun removeImage(uri: Uri) { imageUris.remove(uri) }

    fun hasUnsavedChanges(): Boolean = title.isNotEmpty() || content.isNotEmpty() || imageUris.isNotEmpty()

    fun submitArticle(context: Context, status: String) {
        // Validasi
        var errorMessage: String? = null
        if (title.isBlank()) errorMessage = "Judul wajib diisi!"
        else if (status == "Published") {
            if (selectedCategory == null) errorMessage = "Pilih kategori!"
            else if (content.isBlank()) errorMessage = "Isi artikel tidak boleh kosong!"
        }

        if (errorMessage != null) {
            uiState = UploadUiState.Error("Validasi Gagal")
            viewModelScope.launch { _snackbarEvent.send(errorMessage!!) }
            return
        }

        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                val userId = userPreferences.getUserId.first()
                if (userId == -1) {
                    _snackbarEvent.send("Login ulang.")
                    uiState = UploadUiState.Idle
                    return@launch
                }

                val response = repository.addArticle(
                    title = title,
                    content = if (content.isBlank()) null else content,
                    categoryId = selectedCategory?.category_id?.toString(),
                    userId = userId.toString(),
                    status = status,
                    tags = tags, // KIRIM TAGS KE SERVER
                    imageUris = imageUris,
                    context = context
                )

                if (response.status) {
                    uiState = UploadUiState.Success
                    _snackbarEvent.send(if (status == "Draft") "Draf Disimpan" else "Artikel Terbit")
                } else {
                    uiState = UploadUiState.Error(response.message ?: "Gagal")
                    _snackbarEvent.send(response.message ?: "Gagal")
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Error")
                _snackbarEvent.send("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        uiState = UploadUiState.Idle
        title = ""
        content = ""
        tags = ""
        selectedCategory = null
        imageUris.clear()
    }
}