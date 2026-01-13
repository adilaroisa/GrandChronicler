package com.example.grandchroniclerapp.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val status: Boolean,
    val message: String,
    val token: String? = null,
    val data: User? = null
)