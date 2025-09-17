package com.musinsa.catalog.common.exception

import org.springframework.http.HttpStatus

open class AppException(override val message: String, val httpStatus: HttpStatus): RuntimeException()

fun badRequestException(message: String) = AppException(message, HttpStatus.BAD_REQUEST)
fun notFoundException(message: String) = AppException(message, HttpStatus.NOT_FOUND)