package com.example.grandchroniclerapp.repository

import android.content.Context
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.serviceapi.ApiClient

interface AppContainer {
    val articleRepository: ArticleRepository
    val authRepository: AuthRepository
    val userPreferences: UserPreferences
}

// PERBAIKAN: Tambahkan parameter context di sini
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(ApiClient.apiService)
    }

    override val articleRepository: ArticleRepository by lazy {
        NetworkArticleRepository(ApiClient.apiService)
    }

    override val userPreferences: UserPreferences by lazy {
        UserPreferences(context) // Sekarang context sudah dikenali
    }
}