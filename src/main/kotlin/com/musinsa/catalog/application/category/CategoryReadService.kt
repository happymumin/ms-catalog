package com.musinsa.catalog.application.category

import com.musinsa.catalog.application.cache.CacheKey
import com.musinsa.catalog.application.cache.CacheService
import com.musinsa.catalog.application.cache.getOrSet
import com.musinsa.catalog.domain.category.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryReadService(
    private val repository: CategoryRepository,
    private val cacheService: CacheService
) {

    fun findCategories(cid: Int): CategoryNode? {
        return getCategoryTree().findSubTree(cid)
    }

    fun getAllCategories(): List<CategoryNode> {
        return getCategoryTree().getRootTree().values.toList()
    }

    private fun getCategoryTree(): CategoryTree {
        val categories = cacheService.getOrSet(CacheKey.allCategories) {
            repository.findAllByEnabledTrue()
        }
        return CategoryTree(categories)
    }
}