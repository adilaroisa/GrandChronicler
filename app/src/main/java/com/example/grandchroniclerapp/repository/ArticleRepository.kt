package com.example.grandchroniclerapp.repository

import com.example.grandchroniclerapp.model.AddArticleRequest
import com.example.grandchroniclerapp.model.AddArticleResponse
import com.example.grandchroniclerapp.model.ArticleResponse
import com.example.grandchroniclerapp.model.AuthResponse
import com.example.grandchroniclerapp.model.CategoryResponse
import com.example.grandchroniclerapp.model.DetailArticleResponse
import com.example.grandchroniclerapp.model.UpdateUserRequest
import com.example.grandchroniclerapp.model.UserDetailResponse
import com.example.grandchroniclerapp.serviceapi.ApiService

interface ArticleRepository {
    suspend fun getArticles(query: String? = null): ArticleResponse
    suspend fun getCategories(): CategoryResponse
    suspend fun getArticleDetail(id: Int): DetailArticleResponse
    suspend fun addArticle(request: AddArticleRequest): AddArticleResponse
    suspend fun getMyArticles(userId: Int): ArticleResponse
    suspend fun getUserArticles(userId: Int): ArticleResponse
    suspend fun updateArticle(articleId: Int, request: AddArticleRequest): AddArticleResponse
    suspend fun insertArticle(request: AddArticleRequest): AddArticleResponse
    suspend fun getUserDetail(userId: Int): UserDetailResponse
    suspend fun deleteArticle(articleId: Int): UserDetailResponse
    suspend fun updateUser(userId: Int, request: UpdateUserRequest): UserDetailResponse
    suspend fun deleteUser(id: Int): AuthResponse
}

class NetworkArticleRepository(
    private val apiService: ApiService
) : ArticleRepository {
    override suspend fun getArticles(query: String?): ArticleResponse {
        return apiService.getArticles(query)
    }

    override suspend fun getCategories(): CategoryResponse {
        return apiService.getCategories()
    }

    override suspend fun getArticleDetail(id: Int): DetailArticleResponse {
        return apiService.getArticleDetail(id)
    }

    override suspend fun addArticle(request: AddArticleRequest): AddArticleResponse {
        return apiService.addArticle(request)
    }

    override suspend fun getMyArticles(userId: Int): ArticleResponse {
        return apiService.getMyArticles(userId)
    }

    override suspend fun getUserArticles(userId: Int): ArticleResponse {
        return apiService.getMyArticles(userId)
    }

    override suspend fun updateArticle(articleId: Int, request: AddArticleRequest): AddArticleResponse {
        return apiService.updateArticle(articleId, request)
    }

    override suspend fun insertArticle(request: AddArticleRequest): AddArticleResponse {
        return apiService.insertArticle(request)
    }

    override suspend fun getUserDetail(userId: Int): UserDetailResponse {
        return apiService.getUserDetail(userId)
    }

    override suspend fun deleteArticle(articleId: Int): UserDetailResponse {
        return apiService.deleteArticle(articleId)
    }

    override suspend fun updateUser(userId: Int, request: UpdateUserRequest): UserDetailResponse {
        return apiService.updateUser(userId, request)
    }

    override suspend fun deleteUser(id: Int): AuthResponse {
        return apiService.deleteUser(id) // Sesuaikan dengan nama di ApiService kamu
    }
}