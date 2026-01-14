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

// State UI
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

    // Channel Notifikasi
    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    // Form Data
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)

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
                if (res.status) {
                    categories = res.data
                }
            } catch (e: Exception) { }
        }
    }

    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateCategory(c: Category) { selectedCategory = c }

    fun addImages(uris: List<Uri>) {
        imageUris.addAll(uris)
    }

    fun removeImage(uri: Uri) {
        imageUris.remove(uri)
    }

    fun hasUnsavedChanges(): Boolean {
        return title.isNotEmpty() || content.isNotEmpty() || imageUris.isNotEmpty()
    }

    // --- FUNGSI SUBMIT PINTAR ---
    fun submitArticle(context: Context, status: String) {
        // 1. VALIDASI
        var errorMessage: String? = null

        // Judul Wajib untuk semua status
        if (title.isBlank()) {
            errorMessage = "Judul artikel wajib diisi!"
        }
        // Validasi Ketat Khusus PUBLISHED
        else if (status == "Published") {
            if (selectedCategory == null) {
                errorMessage = "Pilih kategori untuk menerbitkan!"
            } else if (content.isBlank()) {
                errorMessage = "Isi artikel tidak boleh kosong!"
            }
        }

        // Jika ada Error Validasi
        if (errorMessage != null) {
            uiState = UploadUiState.Error("Validasi Gagal") // Trigger UI Merah
            viewModelScope.launch { _snackbarEvent.send(errorMessage!!) }
            return
        }

        // 2. PROSES UPLOAD
        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                val userId = userPreferences.getUserId.first()
                if (userId == -1) {
                    _snackbarEvent.send("Sesi berakhir. Login ulang.")
                    uiState = UploadUiState.Idle
                    return@launch
                }

                // Handle Data Kosong (Agar tidak error 404/Bad Request)
                // Jika Draf dan kategori/konten kosong, kirim NULL agar Retrofit mengabaikannya
                // Backend akan menerima undefined -> jadi NULL di database
                val catIdToSend = selectedCategory?.category_id?.toString()
                val contentToSend = if (content.isBlank()) null else content

                val response = repository.addArticle(
                    title = title,
                    content = contentToSend,
                    categoryId = catIdToSend,
                    userId = userId.toString(),
                    status = status,
                    imageUris = imageUris,
                    context = context
                )

                if (response.status) {
                    uiState = UploadUiState.Success
                    _snackbarEvent.send(if (status == "Draft") "Draf Disimpan" else "Artikel Terbit")
                } else {
                    uiState = UploadUiState.Error(response.message ?: "Gagal")
                    _snackbarEvent.send(response.message ?: "Gagal upload")
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Error")
                _snackbarEvent.send("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    private fun sendEvent(msg: String) {
        viewModelScope.launch { _snackbarEvent.send(msg) }
    }

    fun resetState() {
        uiState = UploadUiState.Idle
        title = ""
        content = ""
        selectedCategory = null
        imageUris.clear()
    }
}