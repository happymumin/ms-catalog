package com.musinsa.catalog.presentation.category

import com.musinsa.catalog.application.category.CategoryService
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import com.musinsa.catalog.presentation.category.dto.CategoryResponse
import com.musinsa.catalog.presentation.dto.Response
import com.musinsa.catalog.presentation.dto.wrapOk
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class CategoryController(private val service: CategoryService) {

    @PostMapping("/api/v1/categories")
    fun createCategory(@RequestBody request: CategoryRequest): Response.Ok<CategoryResponse> {
        val category = service.createCategory(request)
        return CategoryResponse(category.id!!).wrapOk()
    }
}