package com.musinsa.catalog.application.category

import com.musinsa.catalog.application.cache.CacheKey
import com.musinsa.catalog.application.cache.CacheService
import com.musinsa.catalog.common.exception.ErrorCode
import com.musinsa.catalog.common.exception.badRequestException
import com.musinsa.catalog.common.exception.notFoundException
import com.musinsa.catalog.domain.category.Category
import com.musinsa.catalog.domain.category.CategoryRepository
import com.musinsa.catalog.domain.category.model.CategoryCode
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class CategoryCudService(
    private val repository: CategoryRepository,
    private val cacheService: CacheService,
    private val transactionOperations: TransactionOperations
) {

    fun create(request: CategoryRequest): Category {
        val parentCategory = request.parentId?.let {
            repository.findByIdAndEnabledTrue(it) ?: throw notFoundException(ErrorCode.NOT_FOUND_PARENT_CATEGORY)
        }
        try {
            val category = repository.save(
                Category(
                    id = null,
                    parentId = request.parentId,
                    code = CategoryCode.of(request.code, parentCategory?.code),
                    name = request.name,
                    enabled = true
                )
            )
            cacheService.invalidate(CacheKey.allCategories)
            return category
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException(ErrorCode.DUPLICATED_CATEGORY_CODE)
        }
    }

    fun delete(cid: Int) {
        transactionOperations.execute {
            val category =
                repository.findByIdAndEnabledTrue(cid) ?: throw notFoundException(ErrorCode.NOT_FOUND_CATEGORY)
            if (repository.countByCodeStartsWithAndEnabledTrue(category.code.value) > 1) {
                throw badRequestException(ErrorCode.PARENT_CATEGORY_CAN_NOT_DELETE)
            }
            category.enabled = false
        }
        cacheService.invalidate(CacheKey.allCategories)
    }

    fun update(cid: Int, request: CategoryRequest) {
        transactionOperations.execute {
            val category =
                repository.findByIdAndEnabledTrue(cid) ?: throw notFoundException(ErrorCode.NOT_FOUND_CATEGORY)

            val parentCategory: Category? = request.parentId?.let { parentId ->
                repository.findByIdAndEnabledTrue(parentId)
                    ?: throw notFoundException(ErrorCode.NOT_FOUND_PARENT_CATEGORY)
            }

            val newCode = CategoryCode.of(request.code, parentCategory?.code)
            update(category, request, newCode)

            if (newCode != category.code) {
                // code가 변경되면, 자식 카테고리의 code도 변경한다.
                val children = repository.findAllByCodeStartsWithAndEnabledTrue(category.code.value)
                for (child in children) {
                    val childCodeSegment = child.code.segments.last()
                    updateCodeOnly(child, CategoryCode.of(childCodeSegment, newCode))
                }
            }
        }
        cacheService.invalidate(CacheKey.allCategories)
    }

    private fun update(category: Category, request: CategoryRequest, code: CategoryCode) {
        val updatedCount = try {
            repository.update(
                id = category.id!!,
                code = code.value,
                name = request.name,
                parentId = request.parentId,
                beforeModifiedAt = category.modifiedAt
            )
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException(ErrorCode.DUPLICATED_CATEGORY_CODE)
        }
        if (updatedCount != 1) throw badRequestException(ErrorCode.FAILED_UPDATE_CATEGORY)
    }

    private fun updateCodeOnly(category: Category, code: CategoryCode) {
        val updatedCount = try {
            repository.updateCodeOnly(id = category.id!!, code = code.value, beforeModifiedAt = category.modifiedAt)
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException(ErrorCode.DUPLICATED_CATEGORY_CODE)
        }
        if (updatedCount != 1) throw badRequestException(ErrorCode.FAILED_UPDATE_CATEGORY_CODE)
    }

}