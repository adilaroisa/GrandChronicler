package com.example.grandchroniclerapp.serviceapi

import com.example.grandchroniclerapp.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    // --- AUTH ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // --- ARTIKEL (READ & DELETE) ---
    @GET("articles")
    suspend fun getArticles(
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("page") page: Int = 1
    ): ArticleResponse

    @GET("categories")
    suspend fun getCategories(): CategoryResponse

    @GET("articles/{id}")
    suspend fun getArticleDetail(@Path("id") id: Int): DetailArticleResponse

    @GET("users/{id}/articles")
    suspend fun getMyArticles(@Path("id") userId: Int): ArticleResponse

    @DELETE("articles/{id}")
    suspend fun deleteArticle(@Path("id") articleId: Int): UserDetailResponse

    // --- ARTIKEL (CREATE & UPDATE - MULTIPART) ---
    @Multipart
    @POST("articles")
    suspend fun addArticle(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody?,
        @Part("category_id") categoryId: RequestBody?,
        @Part("user_id") userId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("tags") tags: RequestBody?, // <--- TAMBAHAN TAGS
        @Part images: List<MultipartBody.Part>
    ): AddArticleResponse

    @Multipart
    @PUT("articles/{id}")
    suspend fun updateArticle(
        @Path("id") articleId: Int,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody?,
        @Part("category_id") categoryId: RequestBody?,
        @Part("status") status: RequestBody,
        @Part("tags") tags: RequestBody?, // <--- TAMBAHAN TAGS
        @Part images: List<MultipartBody.Part>,
        @Part("deleted_images") deletedImages: RequestBody? = null
    ): AddArticleResponse

    // --- USER ---
    @GET("users/{id}")
    suspend fun getUserDetail(@Path("id") userId: Int): UserDetailResponse

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): AuthResponse

    @Multipart
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Part("full_name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("bio") bio: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part profile_photo: MultipartBody.Part?
    ): UserDetailResponse
}