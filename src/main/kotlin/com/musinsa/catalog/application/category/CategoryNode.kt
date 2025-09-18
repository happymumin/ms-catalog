package com.musinsa.catalog.application.category

import com.musinsa.catalog.domain.category.Category
import com.musinsa.catalog.domain.category.model.CategoryCode
import com.musinsa.catalog.domain.category.model.CategorySegmentCode

class CategoryTree(
    categories: List<Category>
) {
    private val root: Map<CategorySegmentCode, CategoryNode>
    private val categoryCodeById = categories.associate { it.id to it.code }

    init {
        root = buildTree(categories)
    }

    fun getRootTree(): Map<CategorySegmentCode, CategoryNode> = root

    fun findSubTree(cid: Int): CategoryNode? {
        val categoryCode = categoryCodeById[cid] ?: return null
        val segmentCodes = categoryCode.segments

        var currentMap = root
        var node: CategoryNode? = null

        for (code in segmentCodes) {
            node = currentMap[code] ?: return null
            currentMap = node.children
        }

        return node
    }

    private fun buildTree(categories: List<Category>): Map<CategorySegmentCode, CategoryNode> {
        val nodesByCode: Map<CategoryCode, CategoryNode> = categories.sortedBy { it.code.value }
            .associate { category ->
                category.code to CategoryNode(category.id!!, category.code, category.name)
            }
            .toMutableMap()

        categories.filter { it.code.depth != 1 }.forEach { category ->
            val segments = category.code.segments
            val parentCode = CategoryCode.from(segments.take(segments.size - 1))
            val lastSegment = segments.last()

            // 부모 노드 -> 자식 노드 연결
            nodesByCode.getValue(parentCode).children[lastSegment] = nodesByCode.getValue(category.code)
        }

        // 루트 노드만 필터링
        return nodesByCode.filterValues { it.code.depth == 1 }.mapKeys { CategorySegmentCode(it.key.value) }
    }

}

data class CategoryNode(
    val id: Int,
    val code: CategoryCode,
    val name: String,
    val children: MutableMap<CategorySegmentCode, CategoryNode> = mutableMapOf()
)


