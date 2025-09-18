package com.musinsa.catalog.application.category

import com.musinsa.catalog.domain.category.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryReadService(
    private val repository: CategoryRepository,
) {

    fun findCategories(cid: Int): CategoryNode? {
        return getCategoryTree().findSubTree(cid)
    }

    fun getAllCategories(): List<CategoryNode> {
        return getCategoryTree().getRootTree().values.toList()
    }

    private fun getCategoryTree(): CategoryTree {
        return CategoryTree(repository.findAllByEnabledTrue())
    }

}