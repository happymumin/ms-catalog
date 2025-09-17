package com.musinsa.catalog.integration

import com.musinsa.catalog.presentation.category.dto.CategoryListResponse
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import com.musinsa.catalog.util.SimpleCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class CategoryIntegrationTest : IntegrationTest() {

    @Test
    fun `카테고리를 등록하고 조회한다`() {
        // 카테고리 등록
        val womenClothes = client.createCategory(CategoryRequest("100", "여성 의류"))

        val outer = client.createCategory(CategoryRequest("200", "외투", womenClothes.cid))
        client.createCategory(CategoryRequest("300", "코트", outer.cid))

        client.createCategory(CategoryRequest("300", "상의", womenClothes.cid))
        client.createCategory(CategoryRequest("400", "하의", womenClothes.cid))

        client.createCategory(CategoryRequest("500", "남성 의류"))

        // 전체 카테고리 조회
        client.getCategories().assert(
            listOf(
                SimpleCategory(
                    "100",
                    listOf(
                        SimpleCategory("100200", listOf(SimpleCategory("100200300"))),
                        SimpleCategory("100300"),
                        SimpleCategory("100400"),
                    ),
                ),
                SimpleCategory("500"),
            )
        )

        // 일부 카테고리 조회
        client.getCategories(womenClothes.cid).assert(
            listOf(
                SimpleCategory(
                    "100",
                    listOf(
                        SimpleCategory("100200", listOf(SimpleCategory("100200300"))),
                        SimpleCategory("100300"),
                        SimpleCategory("100400"),
                    ),
                )
            )
        )

        // 존재하지 않는 카테고리 조회
        client.getCategories(404).assert(emptyList())
    }

    @Test
    fun `카테고리 코드가 중복되면 카테고리 등록 실패한다`() {
        assertBadRequest {
            client.createCategory(CategoryRequest("999", "여성 의류"))
            client.createCategory(CategoryRequest("999", "여성 의류"))
        }

        assertBadRequest {
            val parent = client.createCategory(CategoryRequest("998", "여성 의류"))
            // 2차 카테고리 중복
            client.createCategory(CategoryRequest("997", "상의", parent.cid))
            client.createCategory(CategoryRequest("997", "상의", parent.cid))
        }

    }

    @Test
    fun `부모 카테고리가 존재하지 않으면 카테고리 등록 실패한다`() {
        assertBadRequest {
            client.createCategory(CategoryRequest("102", "여성 의류", parentId = 404))
        }
    }

    private fun CategoryListResponse.assert(expected: List<SimpleCategory>) {
        assertThat(list.map { it.simplify() }).isEqualTo(expected)
    }

    private fun CategoryListResponse.Category.simplify(): SimpleCategory {
        return SimpleCategory(
            code = code,
            list = list.map { it.simplify() }
        )
    }
}