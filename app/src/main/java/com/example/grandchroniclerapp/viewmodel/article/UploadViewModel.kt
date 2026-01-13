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

// 1. Definisi State UI
sealed interface UploadUiState {
    object Idle : UploadUiState
    object Loading : UploadUiState
    object Success : UploadUiState
    data class Error(val message: String) : UploadUiState
}

// 2. Class ViewModel Utama
class UploadViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var uiState: UploadUiState by mutableStateOf(UploadUiState.Idle)
        private set

    // Form Data
    var title by mutableStateOf("")
        private set

    var content by mutableStateOf("")
        private set

    var selectedCategory: Category? by mutableStateOf(null)
        private set

    // --- UBAH DARI SINGLE URI KE LIST URI (SUPPORT BANYAK GAMBAR) ---
    var selectedImageUris = mutableStateListOf<Uri>()
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

    // --- FUNGSI UPDATE STATE ---
    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateCategory(c: Category) { selectedCategory = c }

    // --- LOGIC GAMBAR BARU (BANYAK) ---
    fun addImages(uris: List<Uri>) {
        selectedImageUris.addAll(uris)
    }

    fun removeImage(uri: Uri) {
        selectedImageUris.remove(uri)
    }

    // --- FUNGSI SUBMIT (Perbaikan Error 'No parameter image') ---
    // Kita butuh Context di sini untuk convert Uri ke Base64
    fun submitArticle(context: Context, status: String = "Published") {
        if (title.isBlank() || content.isBlank() || selectedCategory == null) {
            uiState = UploadUiState.Error("Judul, Konten, dan Kategori wajib diisi")
            return
        }

        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                // Ambil User ID dari Preferences (Jangan Hardcode)
                val userId = userPreferences.getUserId.first()
                if (userId == -1) {
                    uiState = UploadUiState.Error("Sesi berakhir, silakan login ulang")
                    return@launch
                }

                // 1. Convert List Uri ke List Base64
                val imagesList = selectedImageUris.mapNotNull { uri ->
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                // 2. Buat Request (Gunakan parameter 'images')
                val request = AddArticleRequest(
                    title = title,
                    content = content,
                    category_id = selectedCategory!!.category_id,
                    user_id = userId,
                    status = status,
                    // PERBAIKAN: Gunakan 'images' (List), bukan 'image'
                    images = imagesList
                )

                // 3. Kirim ke Repository
                val response = repository.insertArticle(request)

                if (response.status) {
                    uiState = UploadUiState.Success
                } else {
                    uiState = UploadUiState.Error(response.message)
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() {
        uiState = UploadUiState.Idle
        title = ""
        content = ""
        selectedCategory = null
        selectedImageUris.clear()
    }
}