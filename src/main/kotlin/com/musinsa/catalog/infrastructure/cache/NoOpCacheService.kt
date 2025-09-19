package com.musinsa.catalog.infrastructure.cache

import com.musinsa.catalog.application.cache.CacheKey
import com.musinsa.catalog.application.cache.CacheService

class NoOpCacheService: CacheService {
    override fun <T> getOrNull(key: CacheKey<T>): T? {
        return null
    }
    override fun <T> set(key: CacheKey<T>, value: T) {}
    override fun <T> invalidate(key: CacheKey<T>) {}
}