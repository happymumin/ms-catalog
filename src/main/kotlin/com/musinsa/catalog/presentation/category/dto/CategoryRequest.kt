package com.musinsa.catalog.presentation.category.dto

data class CategoryRequest(
    val code: String,
    val name: String,
    val parentId: Int? = null
)