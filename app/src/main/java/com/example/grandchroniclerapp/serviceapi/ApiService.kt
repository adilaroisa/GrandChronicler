package com.example.grandchroniclerapp.serviceapi

import com.example.grandchroniclerapp.model.AddArticleRequest
import com.example.grandchroniclerapp.model.AddArticleResponse
import com.example.grandchroniclerapp.model.ArticleResponse
import com.example.grandchroniclerapp.model.AuthResponse
import com.example.grandchroniclerapp.model.CategoryResponse
import com.example.grandchroniclerapp.model.DetailArticleResponse
import com.example.grandchroniclerapp.model.LoginRequest
import com.example.grandchroniclerapp.model.RegisterRequest
import com.example.grandchroniclerapp.model.UpdateUserRequest
import com.example.grandchroniclerapp.model.UserDetailResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("articles")
    suspend fun getArticles(
        @Query("q") query: String? = null
    ): ArticleResponse

    @GET("categories")
    suspend fun getCategories(): CategoryResponse

    @GET("articles/{id}")
    suspend fun getArticleDetail(
        @Path("id") id: Int
    ): DetailArticleResponse

    @POST("articles")
    suspend fun addArticle(
        @Body request: AddArticleRequest
    ): AddArticleResponse

    @GET("users/{id}/articles")
    suspend fun getMyArticles(
        @Path("id") userId: Int
    ): ArticleResponse

    @DELETE("articles/{id}")
    suspend fun deleteArticle(@Path("id") articleId: Int): UserDetailResponse

    @PUT("articles/{id}")
    suspend fun updateArticle(
        @Path("id") articleId: Int,
        @Body request: AddArticleRequest
    ): AddArticleResponse

    @POST("articles")
    suspend fun insertArticle(
        @Body request: AddArticleRequest
    ): AddArticleResponse

    @GET("users/{id}")
    suspend fun getUserDetail(
        @Path("id") userId: Int
    ): UserDetailResponse

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): UserDetailResponse

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Int
    ): AuthResponse
}
