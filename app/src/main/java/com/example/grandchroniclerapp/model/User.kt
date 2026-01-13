package com.example.grandchroniclerapp.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val user_id: Int,
    val email: String,
    val full_name: String,
    val bio: String? = null,
    val profile_photo: String? = null
)

@Serializable
data class UserDetailResponse(
    val status: Boolean,
    val message: String? = null,
    val data: User? = null
)

@Serializable
data class UpdateUserRequest(
    val full_name: String,
    val bio: String,
    val email: String,
    val password: String
)