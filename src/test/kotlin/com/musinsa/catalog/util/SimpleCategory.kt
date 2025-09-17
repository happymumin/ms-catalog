package com.musinsa.catalog.util

data class SimpleCategory(val code: String, val list: List<SimpleCategory> = emptyList())