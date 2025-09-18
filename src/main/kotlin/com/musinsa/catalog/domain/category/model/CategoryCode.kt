package com.musinsa.catalog.domain.category.model

import com.musinsa.catalog.common.exception.badRequestException

private const val CATEGORY_CODE_SIZE = 3

@JvmInline
value class CategorySegmentCode(val value: String) {
    init {
        if (value.length != CATEGORY_CODE_SIZE || runCatching { value.toInt() }.isFailure) {
            throw badRequestException("code 형식은 3글자 숫자로 구성되어야합니다.")
        }
    }
}

/**
 * [CategorySegmentCode]로 구성된 전체 카테고리 code
 */
@JvmInline
value class CategoryCode(val value: String) {
    val depth get(): Int = value.length / CATEGORY_CODE_SIZE
    val segments get(): List<CategorySegmentCode> = value.chunked(CATEGORY_CODE_SIZE).map(::CategorySegmentCode)

    companion object {
        fun of(code: CategorySegmentCode, parentCategoryCode: CategoryCode? = null): CategoryCode {
            return CategoryCode("${parentCategoryCode?.value ?: ""}${code.value}")
        }

        fun from(segments: List<CategorySegmentCode>): CategoryCode {
            return CategoryCode(segments.joinToString("") { it.value })
        }
    }
}

