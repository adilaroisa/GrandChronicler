package com.example.grandchroniclerapp.serviceapi

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {
    // GANTI IP INI:
    // Jika Emulator: "http://10.0.2.2:3000/api/"
    // Jika HP Fisik: "http://192.168.1.XX:3000/api/" (Cek IP Laptop via cmd -> ipconfig)
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private val json = Json {
        ignoreUnknownKeys = true // Agar app tidak crash jika server kirim field berlebih
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Menampilkan detail request/response di Logcat
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // Pasang logging client
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}