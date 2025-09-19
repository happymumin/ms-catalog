package com.musinsa.catalog.application.category

import com.musinsa.catalog.application.cache.CacheKey
import com.musinsa.catalog.application.cache.CacheService
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
            repository.findByIdAndEnabledTrue(it) ?: throw badRequestException("잘못된 부모 카테고리입니다.")
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
            throw badRequestException("동일한 코드의 카테고리가 존재합니다.")
        }
    }

    fun delete(cid: Int) {
        transactionOperations.execute {
            val category = repository.findByIdAndEnabledTrue(cid) ?: throw notFoundException("존재하지 않는 카테고리입니다.")
            if (repository.countByCodeStartsWithAndEnabledTrue(category.code.value) > 1) {
                throw badRequestException("리프 카테고리만 제거 가능합니다.")
            }
            category.enabled = false
        }
        cacheService.invalidate(CacheKey.allCategories)
    }

    fun update(cid: Int, request: CategoryRequest) {
        transactionOperations.execute {
            val category = repository.findByIdAndEnabledTrue(cid) ?: throw notFoundException("존재하지 않는 카테고리입니다.")

            val parentCategory: Category? = request.parentId?.let { parentId ->
                repository.findByIdAndEnabledTrue(parentId) ?: throw badRequestException("잘못된 부모 카테고리입니다.")
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
            throw badRequestException("동일한 코드의 카테고리가 존재합니다.")
        }
        if (updatedCount != 1) throw badRequestException("카테고리 변경에 실패했습니다.")
    }

    private fun updateCodeOnly(category: Category, code: CategoryCode) {
        val updatedCount = try {
            repository.updateCodeOnly(id = category.id!!, code = code.value, beforeModifiedAt = category.modifiedAt)
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException("동일한 코드의 카테고리가 존재합니다.")
        }
        if (updatedCount != 1) throw badRequestException("code 변경에 실패했습니다.")
    }

}