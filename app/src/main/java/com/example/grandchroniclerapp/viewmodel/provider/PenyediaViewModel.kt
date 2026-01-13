package com.example.grandchroniclerapp.viewmodel.provider

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.grandchroniclerapp.viewmodel.auth.LoginViewModel
import com.example.grandchroniclerapp.viewmodel.auth.RegisterViewModel
import com.example.grandchroniclerapp.GrandChroniclerApp
import com.example.grandchroniclerapp.viewmodel.article.DetailArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.EditArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.InsertViewModel
import com.example.grandchroniclerapp.viewmodel.article.UploadViewModel
import com.example.grandchroniclerapp.viewmodel.home.HomeViewModel
import com.example.grandchroniclerapp.viewmodel.profile.EditProfileViewModel
import com.example.grandchroniclerapp.viewmodel.profile.ProfileViewModel
import com.example.grandchroniclerapp.viewmodel.search.SearchViewModel


object PenyediaViewModel {
    val Factory = viewModelFactory {
        initializer {
            RegisterViewModel(aplikasiGrandChronicler().container.authRepository)
        }
        initializer {
            HomeViewModel(aplikasiGrandChronicler().container.articleRepository)
        }
        initializer {
            SearchViewModel(aplikasiGrandChronicler().container.articleRepository)
        }
        initializer {
            EditProfileViewModel(
                aplikasiGrandChronicler().container.articleRepository,
                aplikasiGrandChronicler().container.userPreferences // TAMBAHKAN BARIS INI
            )
        }
        initializer {
            EditArticleViewModel(aplikasiGrandChronicler().container.articleRepository)
        }
        initializer {
            InsertViewModel(
                aplikasiGrandChronicler().container.articleRepository,
                aplikasiGrandChronicler().container.userPreferences
            )
        }
        initializer {
            DetailArticleViewModel(
                createSavedStateHandle(),
                aplikasiGrandChronicler().container.articleRepository
            )
        }
        initializer {
            LoginViewModel(
                aplikasiGrandChronicler().container.authRepository,
                aplikasiGrandChronicler().container.userPreferences
            )
        }

        initializer {
            ProfileViewModel(
                aplikasiGrandChronicler().container.articleRepository,
                aplikasiGrandChronicler().container.userPreferences // Tambah ini
            )
        }

        initializer {
            UploadViewModel(
                aplikasiGrandChronicler().container.articleRepository,
                aplikasiGrandChronicler().container.userPreferences // Tambah ini
            )
        }
    }
}

fun CreationExtras.aplikasiGrandChronicler(): GrandChroniclerApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GrandChroniclerApp)