package com.musinsa.catalog.presentation.category

import com.musinsa.catalog.application.category.CategoryCudService
import com.musinsa.catalog.application.category.CategoryReadService
import com.musinsa.catalog.presentation.category.dto.CategoryListResponse
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import com.musinsa.catalog.presentation.category.dto.CategoryResponse
import com.musinsa.catalog.presentation.dto.Response
import com.musinsa.catalog.presentation.dto.wrapOk
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*


@RestController
class CategoryController(
    private val readService: CategoryReadService,
    private val cudService: CategoryCudService
) {

    @Operation(summary = "카테고리 생성")
    @PostMapping("/api/v1/categories")
    fun createCategory(@RequestBody request: CategoryRequest): Response.Ok<CategoryResponse> {
        val category = cudService.create(request)
        return CategoryResponse(category.id!!).wrapOk()
    }

    @Operation(summary = "카테고리 수정")
    @PutMapping("/api/v1/categories/{cid}")
    fun updateCategory(@PathVariable cid: Int, @RequestBody request: CategoryRequest): Response.Ok<Unit> {
        cudService.update(cid, request)
        return Response.Ok.empty()
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping("/api/v1/categories/{cid}")
    fun deleteCategory(@PathVariable cid: Int): Response.Ok<Unit> {
        cudService.delete(cid)
        return Response.Ok.empty()
    }

    @Operation(summary = "카테고리 조회")
    @GetMapping("/api/v1/categories")
    fun getCategories(@RequestParam cid: Int? = null): Response.Ok<CategoryListResponse> {
        return if (cid == null) {
            CategoryListResponse.from(readService.getAllCategories())
        } else {
            CategoryListResponse.from(readService.findCategories(cid))
        }.wrapOk()
    }
}