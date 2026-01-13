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
import com.example.grandchroniclerapp.model.AddArticleRequest
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    var uiState: UploadUiState by mutableStateOf(UploadUiState.Idle)
        private set

    // Form Data
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)
    var categories: List<Category> by mutableStateOf(emptyList())

    // --- GAMBAR LAMA (Dari Server) ---
    var oldImageUrls = mutableStateListOf<String>()
        private set

    // Antrian Hapus (URL)
    private var deletedImageUrls = mutableListOf<String>()

    // --- GAMBAR BARU (Dari HP) ---
    var newImageUris = mutableStateListOf<Uri>()
        private set

    // Snapshot
    private var initialTitle = ""
    private var initialContent = ""
    private var initialCategoryId = 0

    val totalImagesCount: Int
        get() = oldImageUrls.size + newImageUris.size

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

    fun loadArticleData(articleId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getArticleDetail(articleId)
                if (res.status && res.data != null) {
                    val article = res.data!!
                    title = article.title
                    content = article.content
                    selectedCategory = categories.find { it.category_id == article.category_id }
                        ?: categories.find { it.category_name == article.category_name }

                    oldImageUrls.clear()
                    oldImageUrls.addAll(article.images)
                    deletedImageUrls.clear()
                    newImageUris.clear()

                    initialTitle = article.title
                    initialContent = article.content
                    initialCategoryId = article.category_id
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Gagal memuat data")
            }
        }
    }

    // Logic Hapus
    fun deleteOldImage(url: String) {
        oldImageUrls.remove(url)
        deletedImageUrls.add(url)
    }

    fun updateImages(uris: List<Uri>) {
        newImageUris.addAll(uris)
    }

    fun removeNewImage(uri: Uri) {
        newImageUris.remove(uri)
    }

    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateCategory(c: Category) { selectedCategory = c }

    fun hasChanges(): Boolean {
        val currentCatId = selectedCategory?.category_id ?: 0
        return title != initialTitle ||
                content != initialContent ||
                currentCatId != initialCategoryId ||
                newImageUris.isNotEmpty() ||
                deletedImageUrls.isNotEmpty()
    }

    fun submitUpdate(context: Context, articleId: Int, status: String) {
        // VALIDASI HANYA UNTUK DATA TEKS (GAMBAR OPSIONAL)
        if (title.isBlank() || content.isBlank() || selectedCategory == null) {
            uiState = UploadUiState.Error("Judul, Konten, dan Kategori wajib diisi")
            return
        }

        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                // 1. Convert Gambar BARU
                val imagesBase64 = if (newImageUris.isNotEmpty()) {
                    newImageUris.map { uri ->
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                    }
                } else { emptyList() }

                // 2. Buat Request
                val req = AddArticleRequest(
                    title = title,
                    content = content,
                    category_id = selectedCategory!!.category_id,
                    user_id = 0,
                    status = status,
                    images = imagesBase64,
                    deleted_images = deletedImageUrls
                )

                val res = repository.updateArticle(articleId, req)

                if (res.status) {
                    uiState = UploadUiState.Success
                } else {
                    uiState = UploadUiState.Error(res.message)
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error(e.message ?: "Gagal update")
            }
        }
    }
}