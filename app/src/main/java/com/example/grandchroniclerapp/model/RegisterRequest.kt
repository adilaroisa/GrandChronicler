package com.example.grandchroniclerapp.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)