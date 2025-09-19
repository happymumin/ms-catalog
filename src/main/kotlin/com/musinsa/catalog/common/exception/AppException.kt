package com.musinsa.catalog.common.exception

import org.springframework.http.HttpStatus

open class AppException(override val message: String, val httpStatus: HttpStatus, val errorCode: ErrorCode? = null) :
    RuntimeException()

fun badRequestException(message: String) = AppException(message, HttpStatus.BAD_REQUEST)
fun badRequestException(errorCode: ErrorCode) = AppException(errorCode.message, HttpStatus.BAD_REQUEST, errorCode)

fun notFoundException(errorCode: ErrorCode) = AppException(errorCode.message, HttpStatus.NOT_FOUND, errorCode)