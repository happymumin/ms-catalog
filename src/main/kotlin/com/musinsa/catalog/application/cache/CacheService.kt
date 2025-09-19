package com.musinsa.catalog.application.cache

interface CacheService {
    fun <T> getOrNull(key: CacheKey<T>): T?
    fun <T> set(key: CacheKey<T>, value: T)
    fun <T> invalidate(key: CacheKey<T>)
}

inline fun <reified T> CacheService.getOrSet(
    key: CacheKey<T>,
    supplier: () -> T
): T {
    return getOrNull(key) ?: supplier().also { set(key, it) }
}
