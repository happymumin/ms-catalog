package com.musinsa.catalog.integration

import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClientResponseException

class CategoryIntegrationTest: IntegrationTest() {

    @Test
    fun `카테고리를 등록한다`() {
        val parentCategory = assertDoesNotThrow {
            client.createCategory(CategoryRequest("100", "여성 의류"))
        }

        assertDoesNotThrow {
            client.createCategory(CategoryRequest("200", "가디건", parentCategory.cid))
        }
    }

    @Test
    fun `카테고리 코드가 중복되면 카테고리 등록 실패한다`() {
        assertDoesNotThrow {
            client.createCategory(CategoryRequest("999", "여성 의류"))
        }

        assertBadRequest {
            client.createCategory(CategoryRequest("999", "여성 의류"))
        }
    }

    @Test
    fun `부모 카테고리가 존재하지 않으면 카테고리 등록 실패한다`() {
        assertBadRequest {
            client.createCategory(CategoryRequest("102", "여성 의류", parentId = 404))
        }
    }
}