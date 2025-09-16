package com.musinsa.catalog.presentation.dto

object Response {
    data class Ok<T>(val data: T)
    data class Error(val message: String)
}

fun <T> T.wrapOk() = Response.Ok(this)