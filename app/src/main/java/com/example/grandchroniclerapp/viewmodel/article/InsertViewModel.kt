package com.example.grandchroniclerapp.viewmodel.article

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.AddArticleRequest
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

// UI State
sealed interface InsertUiState {
    object Idle : InsertUiState
    object Loading : InsertUiState
    object Success : InsertUiState
    data class Error(val message: String) : InsertUiState
}

class InsertViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var uiState: InsertUiState by mutableStateOf(InsertUiState.Idle)
        private set

    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)
    var selectedImageUris = mutableStateListOf<Uri>()
        private set
    var categories: List<Category> by mutableStateOf(emptyList())

    init { fetchCategories() }

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

    fun addImages(uris: List<Uri>) {
        selectedImageUris.addAll(uris)
    }

    fun removeImage(uri: Uri) {
        selectedImageUris.remove(uri)
    }

    fun hasUnsavedChanges(): Boolean {
        return title.isNotBlank() || content.isNotBlank() || selectedCategory != null || selectedImageUris.isNotEmpty()
    }

    fun submitArticle(context: Context, status: String) {
        // VALIDASI HANYA TEKS, GAMBAR OPSIONAL
        if (title.isBlank() || content.isBlank() || selectedCategory == null) {
            uiState = InsertUiState.Error("Judul, Konten, dan Kategori wajib diisi")
            return
        }

        viewModelScope.launch {
            uiState = InsertUiState.Loading
            try {
                val userId = userPreferences.getUserId.first()
                if (userId == -1) {
                    uiState = InsertUiState.Error("Sesi habis, login ulang.")
                    return@launch
                }

                // Convert Images
                val imagesBase64 = selectedImageUris.mapNotNull { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                    } catch (e: Exception) { null }
                }

                val request = AddArticleRequest(
                    title = title,
                    content = content,
                    category_id = selectedCategory!!.category_id,
                    user_id = userId,
                    status = status,
                    images = imagesBase64
                )

                val response = repository.insertArticle(request)
                if (response.status) {
                    uiState = InsertUiState.Success
                } else {
                    uiState = InsertUiState.Error(response.message)
                }
            } catch (e: Exception) {
                uiState = InsertUiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() {
        uiState = InsertUiState.Idle
        title = ""
        content = ""
        selectedCategory = null
        selectedImageUris.clear()
    }
}