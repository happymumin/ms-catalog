package com.musinsa.catalog.presentation.category.dto

import com.musinsa.catalog.domain.category.model.CategorySegmentCode


data class CategoryRequest(
    val code: CategorySegmentCode,
    val name: String,
    val parentId: Int? = null
)