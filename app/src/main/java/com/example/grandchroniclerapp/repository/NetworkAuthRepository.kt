package com.example.grandchroniclerapp.repository

import com.example.grandchroniclerapp.model.AuthResponse
import com.example.grandchroniclerapp.model.LoginRequest
import com.example.grandchroniclerapp.model.RegisterRequest
import com.example.grandchroniclerapp.serviceapi.ApiService

class NetworkAuthRepository(
    private val apiService: ApiService
) : AuthRepository {
    override suspend fun register(request: RegisterRequest): AuthResponse {
        return apiService.register(request)
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        return apiService.login(request)
    }
}