package com.example.grandchroniclerapp.model

import kotlinx.serialization.Serializable

@Serializable
data class AddArticleRequest(
    val title: String,
    val content: String,
    val category_id: Int,
    val user_id: Int,
    val images: List<String> = emptyList(),
    val deleted_images: List<String> = emptyList(),
    val status: String
)

@Serializable
data class AddArticleResponse(
    val status: Boolean,
    val message: String
)