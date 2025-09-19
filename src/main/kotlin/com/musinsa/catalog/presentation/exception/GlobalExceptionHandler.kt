package com.musinsa.catalog.presentation.exception

import com.musinsa.catalog.common.exception.AppException
import com.musinsa.catalog.presentation.dto.Response
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger { }

    @ExceptionHandler(AppException::class)
    fun handle(e: AppException): ResponseEntity<Response.Error> {
        logger.debug { e }
        return ResponseEntity.status(e.httpStatus).body(Response.Error(e.message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle(e: HttpMessageNotReadableException): ResponseEntity<Response.Error> {
        logger.debug { e }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.Error(BAD_REQUEST_MESSAGE))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handle(e: RuntimeException): ResponseEntity<Response.Error> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.Error(INTERNAL_SERVER_ERROR_MESSAGE))
    }

    companion object {
        const val INTERNAL_SERVER_ERROR_MESSAGE = "서버측 오류입니다."
        const val BAD_REQUEST_MESSAGE = "잘못된 입력입니다."
    }
}