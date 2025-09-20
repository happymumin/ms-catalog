package com.musinsa.catalog.application.category

import com.musinsa.catalog.domain.category.Category
import com.musinsa.catalog.domain.category.model.CategoryCode
import com.musinsa.catalog.domain.category.model.CategorySegmentCode

/**
 * 카테고리 전체 리스트를 기반으로 계층형 트리를 구성하고,
 * 특정 카테고리 ID 기준으로 서브 트리를 조회할 수 있는 클래스.
 */
class CategoryTree(
    categories: List<Category>
) {
    // 카테고리 트리의 루트 노드
    private val root: Map<CategorySegmentCode, CategoryNode>
    private val categoryCodeById = categories.associate { it.id to it.code }

    init {
        root = buildTree(categories)
    }

    fun getRootTree(): Map<CategorySegmentCode, CategoryNode> = root

    /**
     * 특정 카테고리 ID를 기반으로 서브 트리를 찾는다.
     * @param cid 카테고리 ID
     * @return 해당 카테고리 ID로부터 시작되는 서브 트리의 루트 노드
     */
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

    /**
     * 주어진 카테고리 목록을 기반으로 카테고리 트리를 빌드한다.
     * @param categories 전체 카테고리 리스트
     * @return 루트 세그먼트 코드를 키로 가지는 트리 구조
     */
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


