package com.musinsa.catalog.presentation.category.dto

import com.musinsa.catalog.application.category.CategoryNode
import com.musinsa.catalog.domain.category.model.CategoryCode

data class CategoryResponse(
    val cid: Int
)

data class CategoryListResponse(
    val list: List<Category>
) {

    data class Category(
        val cid: Int,
        val code: CategoryCode,
        val name: String,
        val list: List<Category>
    ) {
        companion object {
            fun from(node: CategoryNode): Category {
                return Category(
                    cid = node.id,
                    code = node.code,
                    name = node.name,
                    list = node.children.values.map { from(it) }
                )
            }
        }
    }

    companion object {
        fun from(categoryNodes: List<CategoryNode>): CategoryListResponse {
            return CategoryListResponse(categoryNodes.map { Category.from(it) })
        }

        fun from(categoryNode: CategoryNode?): CategoryListResponse {
            if (categoryNode == null) return CategoryListResponse(emptyList())
            return CategoryListResponse(listOf(Category.from(categoryNode)))
        }
    }
}
