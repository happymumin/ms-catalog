package com.musinsa.catalog.application.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.musinsa.catalog.domain.category.Category
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class CacheKey<T>(
    val key: String,
    val type: TypeReference<T>,
    val timeout: Duration
) {
    companion object Factory {
        val allCategories: CacheKey<List<Category>> = CacheKey(
            key = "catalog:category:all",
            type = jacksonTypeRef(),
            timeout = 1.hours
        )
    }
}