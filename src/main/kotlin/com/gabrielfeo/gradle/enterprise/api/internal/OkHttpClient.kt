package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.logging.Level
import java.util.logging.Logger

// TODO Integration test
internal val okHttpClient: OkHttpClient by lazy {
    val cache = buildCache()
    with(OkHttpClient.Builder()) {
        cache(cache)
        if (options.debugging.debugLoggingEnabled && options.cache.cacheEnabled) {
            addInterceptor(CacheHitLoggingInterceptor())
        }
        addInterceptor(HttpBearerAuth("bearer", options.gradleEnterpriseInstance.token()))
        if (options.cache.cacheEnabled) {
            addNetworkInterceptor(buildCacheEnforcingInterceptor())
        }
        build().apply {
            dispatcher.maxRequests = options.concurrency.maxConcurrentRequests
            dispatcher.maxRequestsPerHost = options.concurrency.maxConcurrentRequests
        }
    }
}

internal fun buildCache(): Cache {
    val cacheDir = options.cache.cacheDir
    val maxSize = options.cache.maxCacheSize
    if (options.debugging.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(Level.INFO, "HTTP cache dir: $cacheDir (max ${maxSize}B)")
    }
    return Cache(cacheDir, maxSize)
}

private fun buildCacheEnforcingInterceptor() = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = options.cache.longTermCacheUrlPattern,
    longTermCacheMaxAge = options.cache.longTermCacheMaxAge,
    shortTermCacheUrlPattern = options.cache.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = options.cache.shortTermCacheMaxAge,
)
