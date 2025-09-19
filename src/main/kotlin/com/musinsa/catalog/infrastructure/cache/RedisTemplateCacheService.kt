package com.musinsa.catalog.infrastructure.cache

import com.musinsa.catalog.application.cache.CacheKey
import com.musinsa.catalog.application.cache.CacheService
import com.musinsa.catalog.common.util.objectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import kotlin.time.toJavaDuration

class RedisTemplateCacheService(
    private val stringRedisTemplate: StringRedisTemplate
) : CacheService {
    private val logger = KotlinLogging.logger { }

    override fun <T> getOrNull(key: CacheKey<T>): T? {
        return try {
            logger.debug { "read cache from redis. key: ${key.key}" }
            val value = stringRedisTemplate.opsForValue().get(key.key) ?: return null
            objectMapper.readValue(value, key.type)
        } catch (e: Exception) {
            logger.error(e) { "failed to read cache from redis. key: ${key.key}" }
            return null
        }
    }

    override fun <T> set(key: CacheKey<T>, value: T) {
        try {
            logger.debug { "set cache from redis. key: ${key.key}" }
            stringRedisTemplate
                .opsForValue()
                .set(key.key, objectMapper.writeValueAsString(value), key.timeout.toJavaDuration())
        } catch (e: Exception) {
            logger.error(e) { "failed to set cache from redis. key: ${key.key}" }
        }
    }

    override fun <T> invalidate(key: CacheKey<T>) {
        try {
            logger.debug { "invalidate cache from redis. key: ${key.key}" }
            stringRedisTemplate.delete(key.key)
        } catch (e: Exception) {
            logger.error(e) { "failed to invalidate cache from redis. key: ${key.key}" }
        }
    }
}