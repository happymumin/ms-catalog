package com.musinsa.catalog.presentation.dto

object Response {
    data class Ok<T>(val data: T) {
        companion object {
            fun empty() = Ok(Unit)
        }
    }
    data class Error(val message: String, val errorCode: String? = null)
}

fun <T> T.wrapOk() = Response.Ok(this)