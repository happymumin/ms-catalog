package com.musinsa.catalog.application.category

import com.musinsa.catalog.domain.category.model.CategoryCode
import com.musinsa.catalog.domain.category.Category
import com.musinsa.catalog.util.SimpleCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CategoryTreeTest {

    private val category = CategoryTree(
        listOf(
            category(1, null, "100", "패션"),
            category(5, 1, "100200", "남성의류"),
            category(10, 5, "100200300", "티셔츠"),
            category(15, 10, "100200300400", "반팔티셔츠"),
            category(16, 10, "100200300500", "긴팔티셔츠"),
            category(11, 5, "100200400", "니트/스웨터"),
            category(17, 11, "100200400500", "가디건"),
            category(6, 1, "100300", "여성의류"),
            category(12, 6, "100300400", "원피스"),

            category(2, null, "200", "가전/디지털"),
            category(7, 2, "200300", "컴퓨터"),

            category(3, null, "300", "가구/인테리어"),
        )
    )

    @Test
    fun `전체 카테고리를 조회한다`() {
        assertThat(category.getRootTree().values.map { it.simplify() })
            .isEqualTo(
                listOf(
                    SimpleCategory(
                        "100",
                        listOf(
                            SimpleCategory(
                                "100200",
                                listOf(
                                    SimpleCategory(
                                        "100200300",
                                        listOf(
                                            SimpleCategory("100200300400"),
                                            SimpleCategory("100200300500"),
                                        )
                                    ),
                                    SimpleCategory(
                                        "100200400",
                                        listOf(
                                            SimpleCategory("100200400500"),
                                        )
                                    ),
                                )
                            ),
                            SimpleCategory(
                                "100300",
                                listOf(
                                    SimpleCategory("100300400"),
                                )
                            )
                        )
                    ),
                    SimpleCategory(
                        "200",
                        listOf(
                            SimpleCategory("200300")
                        )
                    ),
                    SimpleCategory("300"),
                )
            )
    }

    @Test
    fun `서브 카테고리를 조회한다`() {
        assertThat(category.findSubTree(10)!!.simplify())
            .isEqualTo(
                SimpleCategory(
                    "100200300",
                    listOf(
                        SimpleCategory("100200300400"),
                        SimpleCategory("100200300500"),
                    )
                )
            )
    }

    @Test
    fun `서브 카테고리가 없으면 null`() {
        assertNull(category.findSubTree(404))
    }

    private fun category(id: Int, parentId: Int?, code: String, name: String): Category {
        return Category(id, parentId, CategoryCode(code), "", true)
    }

    private fun CategoryNode.simplify(): SimpleCategory {
        return SimpleCategory(
            code = code.value,
            list = children.map { it.value.simplify() }
        )
    }
}