package com.musinsa.catalog.application.category

import com.musinsa.catalog.domain.category.Category

class CategoryTree(
    categories: List<Category>
) {
    private val root = CategoryNode.root()
    private val categoryCodeById = categories.associate { it.id to it.code }

    init {
        buildTree(categories)
    }

    fun getRootTree(): Map<String, CategoryNode> = root.children

    fun findSubTree(cid: Int): CategoryNode? {
        val categoryCode = categoryCodeById[cid] ?: return null
        val codes = categoryCode.chunked(CATEGORY_CODE_SIZE)

        var currentMap = root.children
        var node: CategoryNode? = null

        for (code in codes) {
            node = currentMap[code] ?: return null
            currentMap = node.children
        }

        return node
    }

    private fun buildTree(categories: List<Category>) {
        val rootMap = root.children

        for (category in categories) {
            val codes: List<String> = category.code.chunked(CATEGORY_CODE_SIZE)

            var currentMap = rootMap
            var path = ""

            for ((depth, code) in codes.withIndex()) {
                path += code
                if (depth == codes.lastIndex) {
                    currentMap.putIfAbsent(code, CategoryNode(category.id!!, category.code, category.name))
                } else {
                    val node = currentMap.getOrPut(code) {
                        CategoryNode(-1, category.code, "dummy")
                    }
                    currentMap = node.children
                }
            }
        }
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


