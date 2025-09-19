package com.musinsa.catalog.common.exception

enum class ErrorCode(val message: String) {
    NOT_FOUND_CATEGORY("존재하지 않는 카테고리입니다."),
    NOT_FOUND_PARENT_CATEGORY("존재하지 않는 부모 카테고리입니다."),
    DUPLICATED_CATEGORY_CODE("동일한 코드의 카테고리가 존재합니다."),
    PARENT_CATEGORY_CAN_NOT_DELETE("리프 카테고리만 제거 가능합니다."),
    FAILED_UPDATE_CATEGORY("카테고리 변경에 실패했습니다."),
    FAILED_UPDATE_CATEGORY_CODE("카테고리 코드 변경에 실패했습니다.")
}