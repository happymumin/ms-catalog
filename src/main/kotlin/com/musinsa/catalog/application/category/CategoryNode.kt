package com.musinsa.catalog.application.category

import com.musinsa.catalog.domain.category.Category

class CategoryTree(
    categories: List<Category>
) {
    private val root: Map<String, CategoryNode>
    private val categoryCodeById = categories.associate { it.id to it.code }

    init {
        root = buildTree(categories)
    }

    fun getRootTree(): Map<String, CategoryNode> = root

    fun findSubTree(cid: Int): CategoryNode? {
        val categoryCode = categoryCodeById[cid] ?: return null
        val codes = categoryCode.chunked(CATEGORY_CODE_SIZE)

        var currentMap = root
        var node: CategoryNode? = null

        for (code in codes) {
            node = currentMap[code] ?: return null
            currentMap = node.children
        }

        return node
    }

    private fun buildTree(categories: List<Category>): Map<String, CategoryNode> {
        val nodesByCode = categories.sortedBy { it.code.toBigDecimal() }.associate { category ->
            category.code to CategoryNode(category.id!!, category.code, category.name)
        }.toMutableMap()

        categories.filter { getDepth(it.code) != 1 }.forEach { category ->
            val childCode = category.code
            val parentCode = childCode.dropLast(CATEGORY_CODE_SIZE)
            val lastSegment = childCode.takeLast(CATEGORY_CODE_SIZE)

            // 부모 노드 -> 자식 노드 연결
            nodesByCode.getValue(parentCode).children[lastSegment] = nodesByCode.getValue(childCode)
        }

        // 루트 노드만 필터링
        return nodesByCode.filterValues { getDepth(it.code) == 1 }
    }

    private fun getDepth(code: String): Int {
        return code.length / CATEGORY_CODE_SIZE
    }
}

data class CategoryNode(
    val id: Int,
    val code: String,
    val name: String,
    val children: MutableMap<String, CategoryNode> = mutableMapOf()
) {
    companion object {
        fun root() = CategoryNode(-1, "", "root")
    }
}


