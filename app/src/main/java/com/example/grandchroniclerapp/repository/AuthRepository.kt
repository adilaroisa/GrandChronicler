package com.example.grandchroniclerapp.repository

import com.example.grandchroniclerapp.model.AuthResponse
import com.example.grandchroniclerapp.model.LoginRequest
import com.example.grandchroniclerapp.model.RegisterRequest

interface AuthRepository {
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
}