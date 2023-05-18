package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

internal val okHttpClient by lazy {
    buildOkHttpClient(options = options)
}

internal fun buildOkHttpClient(
    options: Options,
) = with(options.httpClient.clientBuilder()) {
    readTimeout(Duration.ofMillis(options.httpClient.readTimeoutMillis))
    if (options.cache.cacheEnabled) {
        cache(buildCache(options))
    }
    addInterceptors(options)
    addNetworkInterceptors(options)
    build().apply {
        options.httpClient.maxConcurrentRequests?.let {
            dispatcher.maxRequests = it
            dispatcher.maxRequestsPerHost = it
        }
    }
}

private fun OkHttpClient.Builder.addInterceptors(options: Options) {
    if (options.debugging.debugLoggingEnabled && options.cache.cacheEnabled) {
        addInterceptor(CacheHitLoggingInterceptor())
    }
}

private fun OkHttpClient.Builder.addNetworkInterceptors(options: Options) {
    if (options.cache.cacheEnabled) {
        addNetworkInterceptor(buildCacheEnforcingInterceptor(options))
    }
    if (options.debugging.debugLoggingEnabled) {
        addNetworkInterceptor(HttpLoggingInterceptor().apply { level = BODY })
    }
    addNetworkInterceptor(HttpBearerAuth("bearer", options.gradleEnterpriseInstance.token()))
}

internal fun buildCache(
    options: Options
): Cache {
    val cacheDir = options.cache.cacheDir
    val maxSize = options.cache.maxCacheSize
    if (options.debugging.debugLoggingEnabled) {
        val logger = Logger.getGlobal()
        logger.log(Level.INFO, "HTTP cache dir: $cacheDir (max ${maxSize}B)")
    }
    return Cache(cacheDir, maxSize)
}

private fun buildCacheEnforcingInterceptor(
    options: Options,
) = CacheEnforcingInterceptor(
    longTermCacheUrlPattern = options.cache.longTermCacheUrlPattern,
    longTermCacheMaxAge = options.cache.longTermCacheMaxAge,
    shortTermCacheUrlPattern = options.cache.shortTermCacheUrlPattern,
    shortTermCacheMaxAge = options.cache.shortTermCacheMaxAge,
)