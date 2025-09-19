package com.musinsa.catalog.integration

import com.musinsa.catalog.domain.category.CategoryRepository
import com.musinsa.catalog.domain.category.model.CategorySegmentCode
import com.musinsa.catalog.presentation.category.dto.CategoryListResponse
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import com.musinsa.catalog.util.SimpleCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class CategoryIntegrationTest(private val repository: CategoryRepository) : IntegrationTest() {

    @BeforeEach
    fun beforeEach() {
        repository.deleteAll()
    }


    @Test
    fun `카테고리를 등록하고 조회한다`() {
        // 카테고리 등록
        val womenClothesId = client.createCategory(CategoryRequest("100".toSegmentCode(), "여성 의류"))
            .also { depth1 ->
                client.createCategory(CategoryRequest("200".toSegmentCode(), "외투", depth1.cid)).also { depth2 ->
                    client.createCategory(CategoryRequest("300".toSegmentCode(), "코트", depth2.cid))
                }

                client.createCategory(CategoryRequest("300".toSegmentCode(), "상의", depth1.cid))
                client.createCategory(CategoryRequest("400".toSegmentCode(), "하의", depth1.cid))
            }.cid

        client.createCategory(CategoryRequest("200".toSegmentCode(), "남성 의류"))

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
                SimpleCategory("200"),
            )
        )

        // 일부 카테고리 조회
        client.getCategories(womenClothesId).assert(
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
    fun `카테고리를 등록하고 수정한다`() {
        // 카테고리 등록
        val womenOuterId = client.createCategory(CategoryRequest("100".toSegmentCode(), "여성 의류"))
            .let { depth1 ->
                client.createCategory(CategoryRequest("200".toSegmentCode(), "외투", depth1.cid)).also { depth2 ->
                    client.createCategory(CategoryRequest("300".toSegmentCode(), "코트", depth2.cid))
                }
            }.cid

        val menClothesId = client.createCategory(CategoryRequest("200".toSegmentCode(), "남성 의류")).cid

        client.getCategories().assert(
            listOf(
                SimpleCategory(
                    "100",
                    listOf(
                        SimpleCategory("100200", listOf(SimpleCategory("100200300"))),
                    ),
                ),
                SimpleCategory("200"),
            )
        )

        // 카테고리 수정
        assertDoesNotThrow { client.updateCategory(menClothesId, CategoryRequest("300".toSegmentCode(), "남성 의류")) }

        client.getCategories().assert(
            listOf(
                SimpleCategory(
                    "100",
                    listOf(
                        SimpleCategory("100200", listOf(SimpleCategory("100200300"))),
                    ),
                ),
                SimpleCategory("300"),
            )
        )

        // 서브 카테고리 수정
        assertDoesNotThrow { client.updateCategory(womenOuterId, CategoryRequest("400".toSegmentCode(), "여성 의류2")) }

        client.getCategories().assert(
            listOf(
                SimpleCategory("100"),
                SimpleCategory("300"),
                SimpleCategory("400", listOf(SimpleCategory("400300")))
            )
        )

        // 코드 중복 시 에러
        assertBadRequest {
            client.updateCategory(womenOuterId, CategoryRequest("100".toSegmentCode(), "여성 의류2"))
        }
    }

    @Test
    fun `카테고리를 등록하고 삭제한다`() {
        val (depth1, depth2) = client.createCategory(CategoryRequest("100".toSegmentCode(), "여성 의류"))
            .let { depth1 ->
                depth1 to client.createCategory(CategoryRequest("200".toSegmentCode(), "외투", depth1.cid))
            }

        // 리프 카테고리가 아니면 삭제 불가
        assertBadRequest {
            client.deleteCategory(depth1.cid)
        }

        // 리프 카테고리 삭제
        assertDoesNotThrow {
            client.deleteCategory(depth2.cid)
        }

        // 기존 자식 카테고리가 삭제되어 리프 카테고리로 전환 -> 삭제 가능
        assertDoesNotThrow {
            client.deleteCategory(depth1.cid)
        }
    }

    @Test
    fun `카테고리 코드가 중복되면 카테고리 등록 실패한다`() {
        assertBadRequest {
            client.createCategory(CategoryRequest("999".toSegmentCode(), "여성 의류"))
            client.createCategory(CategoryRequest("999".toSegmentCode(), "여성 의류"))
        }

        assertBadRequest {
            val parent = client.createCategory(CategoryRequest("998".toSegmentCode(), "여성 의류"))
            // 2차 카테고리 중복
            client.createCategory(CategoryRequest("997".toSegmentCode(), "상의", parent.cid))
            client.createCategory(CategoryRequest("997".toSegmentCode(), "상의", parent.cid))
        }

    }

    @Test
    fun `부모 카테고리가 존재하지 않으면 카테고리 등록 실패한다`() {
        assertBadRequest {
            client.createCategory(CategoryRequest("102".toSegmentCode(), "여성 의류", parentId = 404))
        }
    }

    private fun CategoryListResponse.assert(expected: List<SimpleCategory>) {
        assertThat(list.map { it.simplify() }).isEqualTo(expected)
    }

    private fun CategoryListResponse.Category.simplify(): SimpleCategory {
        return SimpleCategory(
            code = code.value,
            list = list.map { it.simplify() }
        )
    }

    private fun String.toSegmentCode() = CategorySegmentCode(this)
}