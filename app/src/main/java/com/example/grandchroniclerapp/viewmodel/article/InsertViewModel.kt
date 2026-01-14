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

// Wrapper Class untuk Gambar + Caption
data class ImageUploadState(
    val uri: Uri,
    var caption: String = ""
)

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
    var tags by mutableStateOf("")

    // UBAH: Menggunakan List of ImageUploadState (Uri + Caption)
    var imageList = mutableStateListOf<ImageUploadState>()
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
    fun updateTags(t: String) { tags = t }

    // Update fungsi Add Images
    fun addImages(uris: List<Uri>) {
        uris.forEach { uri ->
            imageList.add(ImageUploadState(uri))
        }
    }

    // Update fungsi Remove Image
    fun removeImage(item: ImageUploadState) { imageList.remove(item) }

    // Tambahan: Update Caption
    fun updateCaption(index: Int, text: String) {
        if (index in imageList.indices) {
            // Perlu replace item agar Compose mendeteksi perubahan state pada properti var
            val item = imageList[index]
            imageList[index] = item.copy(caption = text)
        }
    }

    fun hasUnsavedChanges(): Boolean = title.isNotEmpty() || content.isNotEmpty() || imageList.isNotEmpty()

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

                // Pisahkan URI dan Caption untuk dikirim
                val urisToSend = imageList.map { it.uri }
                val captionsToSend = imageList.map { it.caption }

                val response = repository.addArticle(
                    title = title,
                    content = if (content.isBlank()) null else content,
                    categoryId = selectedCategory?.category_id?.toString(),
                    userId = userId.toString(),
                    status = status,
                    tags = tags,
                    captions = captionsToSend,
                    imageUris = urisToSend,
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
        imageList.clear()
    }
}