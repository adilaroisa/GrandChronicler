package com.example.grandchroniclerapp.repository

import android.content.Context
import android.net.Uri
import com.example.grandchroniclerapp.model.AddArticleResponse
import com.example.grandchroniclerapp.model.ArticleResponse
import com.example.grandchroniclerapp.model.AuthResponse
import com.example.grandchroniclerapp.model.CategoryResponse
import com.example.grandchroniclerapp.model.DetailArticleResponse
import com.example.grandchroniclerapp.model.UserDetailResponse
import com.example.grandchroniclerapp.serviceapi.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ArticleRepository(private val apiService: ApiService) {

    private fun createPartFromString(value: String?): RequestBody? {
        if (value.isNullOrBlank()) return null
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // --- ARTIKEL ---
    suspend fun getArticles(query: String? = null, categoryId: Int? = null, page: Int = 1): ArticleResponse {
        return apiService.getArticles(query, categoryId, page)
    }

    suspend fun getCategories(): CategoryResponse = apiService.getCategories()
    suspend fun getArticleDetail(id: Int): DetailArticleResponse = apiService.getArticleDetail(id)
    suspend fun getMyArticles(userId: Int): ArticleResponse = apiService.getMyArticles(userId)
    suspend fun deleteArticle(articleId: Int): UserDetailResponse = apiService.deleteArticle(articleId)

    // Add Article
    suspend fun addArticle(
        title: String,
        content: String?,
        categoryId: String?,
        userId: String,
        status: String,
        tags: String?,
        captions: List<String>, // Parameter baru
        imageUris: List<Uri>,
        context: Context
    ): AddArticleResponse {
        val titlePart = createPartFromString(title)!!
        val contentPart = createPartFromString(content)
        val categoryPart = createPartFromString(categoryId)
        val userPart = createPartFromString(userId)!!
        val statusPart = createPartFromString(status)!!
        val tagsPart = createPartFromString(tags)

        // Convert List String ke JSON String untuk dikirim
        val gson = Gson()
        val captionsJson = gson.toJson(captions)
        val captionsPart = createPartFromString(captionsJson)

        val imageParts = prepareImageParts(imageUris, context)

        return apiService.addArticle(titlePart, contentPart, categoryPart, userPart, statusPart, tagsPart, captionsPart, imageParts)
    }

    // Update Article
    suspend fun updateArticle(
        articleId: Int,
        title: String,
        content: String?,
        categoryId: String?,
        status: String,
        tags: String?,
        captions: List<String>, // Parameter baru (gabungan caption lama & baru)
        newImageUris: List<Uri>,
        deletedImagesJson: String?,
        context: Context
    ): AddArticleResponse {
        val titlePart = createPartFromString(title)!!
        val contentPart = createPartFromString(content)
        val categoryPart = createPartFromString(categoryId)
        val statusPart = createPartFromString(status)!!
        val tagsPart = createPartFromString(tags)
        val deletedPart = createPartFromString(deletedImagesJson)

        // Convert List String ke JSON String
        val gson = Gson()
        val captionsJson = gson.toJson(captions)
        val captionsPart = createPartFromString(captionsJson)

        val imageParts = prepareImageParts(newImageUris, context)

        return apiService.updateArticle(articleId, titlePart, contentPart, categoryPart, statusPart, tagsPart, captionsPart, imageParts, deletedPart)
    }

    // --- USER (PROFIL) ---
    suspend fun getUserDetail(userId: Int): UserDetailResponse = apiService.getUserDetail(userId)
    suspend fun deleteUser(userId: Int): AuthResponse = apiService.deleteUser(userId)
    suspend fun updateUser(
        userId: Int,
        fullName: String,
        email: String,
        bio: String?,
        password: String?,
        photoUri: Uri?,
        context: Context
    ): UserDetailResponse {
        val namePart = createPartFromString(fullName)!!
        val emailPart = createPartFromString(email)!!
        val bioPart = createPartFromString(bio)
        val passPart = if (password.isNullOrBlank()) null else createPartFromString(password)
        var photoPart: MultipartBody.Part? = null
        if (photoUri != null) {
            val file = getFileFromUri(context, photoUri)
            if (file != null) {
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                photoPart = MultipartBody.Part.createFormData("profile_photo", file.name, reqFile)
            }
        }
        return apiService.updateUser(userId, namePart, emailPart, bioPart, passPart, photoPart)
    }

    // --- HELPER FILES ---
    private fun prepareImageParts(imageUris: List<Uri>, context: Context): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()
        imageUris.forEach { uri ->
            val file = getFileFromUri(context, uri)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                parts.add(MultipartBody.Part.createFormData("images", file.name, requestFile))
            }
        }
        return parts
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) { null }
    }
}