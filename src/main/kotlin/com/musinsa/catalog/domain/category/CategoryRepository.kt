package com.musinsa.catalog.domain.category

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface CategoryRepository : JpaRepository<Category, Int> {

    @Transactional(readOnly = true)
    fun findByIdAndEnabledTrue(id: Int): Category?

    @Transactional(readOnly = true)
    fun findAllByEnabledTrue(): List<Category>

    @Transactional(readOnly = true)
    fun findAllByCodeStartsWithAndEnabledTrue(code: String): List<Category>

    @Transactional(readOnly = true)
    fun countByCodeStartsWithAndEnabledTrue(code: String): Int

    @Transactional
    @Modifying
    @Query(
        """
        UPDATE Category c 
        SET  c.parentId = :parentId, c.name = :name, c.code = :code
        WHERE c.id = :id 
            AND c.modifiedAt = :beforeModifiedAt
            and c.enabled = true
    """
    )
    fun update(
        id: Int,
        code: String,
        name: String,
        parentId: Int?,
        beforeModifiedAt: LocalDateTime,
    ): Int

    @Transactional
    @Modifying
    @Query(
        """
        UPDATE Category c 
        SET c.code = :code 
        WHERE c.id = :id 
            and c.modifiedAt = :beforeModifiedAt
            and c.enabled = true
    """
    )
    fun updateCodeOnly(id: Int, code: String, beforeModifiedAt: LocalDateTime): Int
}