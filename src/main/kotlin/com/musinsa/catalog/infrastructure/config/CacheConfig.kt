package com.musinsa.catalog.infrastructure.config

import com.musinsa.catalog.application.cache.CacheService
import com.musinsa.catalog.infrastructure.cache.NoOpCacheService
import com.musinsa.catalog.infrastructure.cache.RedisTemplateCacheService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class CacheConfig {
    private val logger = KotlinLogging.logger {}

    @Profile("redis")
    @Bean
    fun redisTemplateCacheService(stringRedisTemplate: StringRedisTemplate): CacheService {
        logger.info { "Initialized RedisTemplateCacheService." }
        return RedisTemplateCacheService(stringRedisTemplate)
    }

    @ConditionalOnMissingBean(CacheService::class)
    @Bean
    fun noOpCacheService(): CacheService {
        logger.info { "Initialized NoOpCacheService." }
        return NoOpCacheService()
    }
}