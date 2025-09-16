package com.musinsa.catalog.domain.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CategoryRepository : JpaRepository<Category, Int> {

    @Transactional(readOnly = true)
    fun findByIdAndEnabledTrue(id: Int): Category?
}