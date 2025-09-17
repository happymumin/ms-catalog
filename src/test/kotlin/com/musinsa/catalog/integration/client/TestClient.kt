package com.musinsa.catalog.integration.client

import com.musinsa.catalog.presentation.category.dto.CategoryListResponse
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import com.musinsa.catalog.presentation.category.dto.CategoryResponse
import com.musinsa.catalog.presentation.dto.Response
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono

class TestClient(private val client: WebClient) {

    fun createCategory(request: CategoryRequest): CategoryResponse {
        return client.post()
            .uri("/api/v1/categories")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<Response.Ok<CategoryResponse>>()
            .block()!!
            .data
    }

    fun getCategories(cid: Int? = null): CategoryListResponse {
        return client.get()
            .uri { builder ->
                builder.path("/api/v1/categories")
                if (cid != null) {
                    builder.queryParam("cid", cid)
                }
                builder.build()
            }
            .retrieve()
            .bodyToMono<Response.Ok<CategoryListResponse>>()
            .block()!!
            .data
    }
}