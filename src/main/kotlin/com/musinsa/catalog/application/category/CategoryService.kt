package com.musinsa.catalog.application.category

import com.musinsa.catalog.common.exception.badRequestException
import com.musinsa.catalog.domain.category.Category
import com.musinsa.catalog.domain.category.CategoryRepository
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class CategoryService(private val repository: CategoryRepository) {

    fun createCategory(request: CategoryRequest): Category {
        val parentCategory = request.parentId?.let { repository.findByIdAndEnabledTrue(it) ?: throw badRequestException("잘못된 부모 카테고리입니다.") }
        return try {
            repository.save(
                Category(
                    id = null,
                    parentId = request.parentId,
                    code = "${parentCategory?.code ?: ""}${request.code}",
                    name = request.name,
                    enabled = true
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException("동일한 코드의 카테고리가 존재합니다.")
        }
    }

}