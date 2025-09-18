package com.musinsa.catalog.application.category

import com.musinsa.catalog.common.exception.badRequestException
import com.musinsa.catalog.common.exception.notFoundException
import com.musinsa.catalog.domain.category.Category
import com.musinsa.catalog.domain.category.CategoryRepository
import com.musinsa.catalog.presentation.category.dto.CategoryRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val repository: CategoryRepository,
) {

    fun findCategories(cid: Int): CategoryNode? {
        return getCategoryTree().findSubTree(cid)
    }

    fun getAllCategories(): List<CategoryNode> {
        return getCategoryTree().getRootTree().values.toList()
    }

    private fun getCategoryTree(): CategoryTree {
        return CategoryTree(repository.findAllByEnabledTrue())
    }

    fun createCategory(request: CategoryRequest): Category {
        val parentCategory = request.parentId?.let {
            repository.findByIdAndEnabledTrue(it) ?: throw badRequestException("잘못된 부모 카테고리입니다.")
        }
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

    @Transactional
    fun updateCategory(cid: Int, request: CategoryRequest) {
        val category = repository.findByIdAndEnabledTrue(cid) ?: throw notFoundException("존재하지 않는 카테고리입니다.")

        val parentCategory: Category? = request.parentId?.let { parentId ->
            repository.findByIdAndEnabledTrue(parentId) ?: throw badRequestException("잘못된 부모 카테고리입니다.")
        }

        val newCode = "${parentCategory?.code ?: ""}${request.code}"
        update(category, request, parentCategory)

        if (newCode != category.code) {
            // code가 변경되면, 자식 카테고리의 code도 변경한다.
            val children = repository.findAllByCodeStartsWithAndEnabledTrue(category.code)
            for (c in children) {
                updateCodeOnly(c, "${newCode}${c.code.drop(category.code.length)}")
            }
        }
    }

    private fun update(category: Category, request: CategoryRequest, parentCategory: Category?) {
        val updatedCount = try {
            repository.update(
                id = category.id!!,
                code = "${parentCategory?.code ?: ""}${request.code}",
                name = request.name,
                parentId = request.parentId,
                beforeModifiedAt = category.modifiedAt
            )
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException("동일한 코드의 카테고리가 존재합니다.")
        }
        if (updatedCount != 1) throw badRequestException("카테고리 변경에 실패했습니다.")
    }

    private fun updateCodeOnly(category: Category, code: String) {
        val updatedCount = try {
            repository.updateCodeOnly(id = category.id!!, code = code, beforeModifiedAt = category.modifiedAt)
        } catch (e: DataIntegrityViolationException) {
            throw badRequestException("동일한 코드의 카테고리가 존재합니다.")
        }
        if (updatedCount != 1) throw badRequestException("code 변경에 실패했습니다.")
    }

    @Transactional
    fun deleteCategory(cid: Int) {
        val category = repository.findByIdAndEnabledTrue(cid) ?: throw notFoundException("존재하지 않는 카테고리입니다.")
        if (repository.countByCodeStartsWithAndEnabledTrue(category.code) > 1) {
            throw badRequestException("리프 카테고리만 제거 가능합니다.")
        }
        category.enabled = false
    }


}