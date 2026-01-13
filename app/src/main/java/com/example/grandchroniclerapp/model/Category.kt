package com.example.grandchroniclerapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val category_id: Int,
    val category_name: String
)

@Serializable
data class CategoryResponse(
    val status: Boolean,
    val message: String,
    val data: List<Category> = emptyList()
)