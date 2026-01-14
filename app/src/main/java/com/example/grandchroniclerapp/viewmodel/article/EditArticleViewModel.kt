package com.example.grandchroniclerapp.viewmodel.article

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Wrapper Classes
data class ExistingImageState(
    val url: String,
    var caption: String
)

data class NewImageState(
    val uri: Uri,
    var caption: String = ""
)

class EditArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    var uiState: UploadUiState by mutableStateOf(UploadUiState.Idle)
        private set

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    // Form Data
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var tags by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)
    var categories: List<Category> by mutableStateOf(emptyList())

    // Gambar Lama (URL + Caption)
    var oldImages = mutableStateListOf<ExistingImageState>()
        private set
    private var deletedImageUrls = mutableListOf<String>()

    // Gambar Baru (Uri + Caption)
    var newImages = mutableStateListOf<NewImageState>()
        private set

    // State Awal untuk Cek Perubahan
    private var initialTitle = ""
    private var initialContent = ""
    private var initialTags = ""
    private var initialCategoryId = 0

    val totalImagesCount: Int get() = oldImages.size + newImages.size

    init { fetchCategories() }

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
                    tags = article.tags ?: ""

                    selectedCategory = categories.find { it.category_id == article.category_id }
                        ?: categories.find { it.category_name == article.category_name }

                    oldImages.clear()
                    // Map gambar lama ke wrapper class. Asumsi index caption sesuai index image.
                    article.images.forEachIndexed { index, url ->
                        val cap = if (index < article.captions.size) article.captions[index] else ""
                        oldImages.add(ExistingImageState(url, cap))
                    }

                    deletedImageUrls.clear()
                    newImages.clear()

                    // Simpan state awal
                    initialTitle = article.title
                    initialContent = article.content
                    initialTags = article.tags ?: ""
                    initialCategoryId = article.category_id
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Gagal load data")
                _snackbarEvent.send("Gagal memuat: ${e.message}")
            }
        }
    }

    // --- Manipulasi Gambar Lama ---
    fun deleteOldImage(item: ExistingImageState) {
        oldImages.remove(item)
        deletedImageUrls.add(item.url)
    }
    fun updateOldCaption(index: Int, text: String) {
        if (index in oldImages.indices) {
            val item = oldImages[index]
            oldImages[index] = item.copy(caption = text)
        }
    }

    // --- Manipulasi Gambar Baru ---
    fun updateImages(uris: List<Uri>) {
        uris.forEach { newImages.add(NewImageState(it)) }
    }
    fun removeNewImage(item: NewImageState) { newImages.remove(item) }
    fun updateNewCaption(index: Int, text: String) {
        if (index in newImages.indices) {
            val item = newImages[index]
            newImages[index] = item.copy(caption = text)
        }
    }

    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateTags(t: String) { tags = t }
    fun updateCategory(c: Category) { selectedCategory = c }

    fun hasChanges(): Boolean {
        // Logika sederhana: anggap selalu ada perubahan jika user masuk edit mode
        // Anda bisa memperluas logika ini jika mau strict
        return true
    }

    // --- SUBMIT UPDATE ---
    fun submitUpdate(context: Context, articleId: Int, status: String) {
        // 1. VALIDASI
        var errorMessage: String? = null

        if (title.isBlank()) {
            errorMessage = "Judul tidak boleh kosong!"
        } else if (status == "Published") {
            if (selectedCategory == null) errorMessage = "Pilih kategori untuk terbit!"
            else if (content.isBlank()) errorMessage = "Isi artikel tidak boleh kosong!"
        }

        if (errorMessage != null) {
            uiState = UploadUiState.Error("Validasi Gagal")
            viewModelScope.launch { _snackbarEvent.send(errorMessage!!) }
            return
        }

        // 2. PROSES UPDATE
        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                val gson = Gson()
                val deletedJson = if (deletedImageUrls.isNotEmpty()) gson.toJson(deletedImageUrls) else null

                // GABUNGKAN CAPTION LAMA (yang tidak dihapus) DAN CAPTION BARU
                val allCaptions = mutableListOf<String>()
                oldImages.forEach { allCaptions.add(it.caption) }
                newImages.forEach { allCaptions.add(it.caption) }

                val contentToSend = if (content.isBlank()) null else content
                val catIdToSend = selectedCategory?.category_id?.toString()

                val res = repository.updateArticle(
                    articleId = articleId,
                    title = title,
                    content = contentToSend,
                    categoryId = catIdToSend,
                    status = status,
                    tags = tags,
                    captions = allCaptions, // Kirim list gabungan
                    newImageUris = newImages.map { it.uri },
                    deletedImagesJson = deletedJson,
                    context = context
                )

                if (res.status) {
                    uiState = UploadUiState.Success
                    _snackbarEvent.send("Berhasil Diupdate!")
                } else {
                    uiState = UploadUiState.Error(res.message ?: "Gagal")
                    _snackbarEvent.send(res.message ?: "Gagal update")
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Error")
                _snackbarEvent.send("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}