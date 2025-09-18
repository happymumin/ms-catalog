package com.musinsa.catalog.domain.category

import com.musinsa.catalog.domain.category.model.CategoryCode
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "category")
data class Category(

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    val id: Int? = null,

    @Column(name = "parent_id")
    val parentId: Int? = null,

    @Column(nullable = false, unique = true)
    val code: CategoryCode,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    var enabled: Boolean
) {

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    lateinit var modifiedAt: LocalDateTime
}

