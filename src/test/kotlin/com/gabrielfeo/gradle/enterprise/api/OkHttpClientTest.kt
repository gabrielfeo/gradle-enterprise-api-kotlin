package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.Env
import com.gabrielfeo.gradle.enterprise.api.internal.FakeEnv
import com.gabrielfeo.gradle.enterprise.api.internal.FakeKeychain
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.buildOkHttpClient
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import kotlin.reflect.KClass
import kotlin.test.*

class OkHttpClientTest {

    @Test
    fun `Adds authentication`() {
        val client = buildClient()
        assertTrue(client.interceptors.any { it is HttpBearerAuth })
    }

    @Test
    fun `Sets max concurrency from options`() {
        val client = buildClient(
            "GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS" to "123"
        )
        assertEquals(123, client.dispatcher.maxRequests)
        assertEquals(123, client.dispatcher.maxRequestsPerHost)
    }

    @Test
    fun `Given debug logging and cache enabled, adds logging interceptors`() {
        val client = buildClient(
            "GRADLE_ENTERPRISE_API_DEBUG_LOGGING" to "true",
            "GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "true",
        )
        assertTrue(client.interceptors.any { it is CacheHitLoggingInterceptor })
    }

    @Test
    fun `Given debug logging disabled, doesn't add logging interceptors`() {
        val client = buildClient(
            "GRADLE_ENTERPRISE_API_DEBUG_LOGGING" to "false",
            "GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "true",
        )
        assertTrue(client.interceptors.none { it is CacheHitLoggingInterceptor })
    }

    @Test
    fun `Given cache enabled, configures caching`() {
        val client = buildClient("GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "true")
        assertTrue(client.networkInterceptors.any { it is CacheEnforcingInterceptor })
        assertNotNull(client.cache)
    }

    @Test
    fun `Given cache disabled, no caching or cache logging`() {
        val client = buildClient("GRADLE_ENTERPRISE_API_CACHE_ENABLED" to "false")
        assertTrue(client.networkInterceptors.none { it is CacheEnforcingInterceptor })
        assertTrue(client.interceptors.none { it is CacheHitLoggingInterceptor })
        assertNull(client.cache)
    }

    private fun buildClient(
        vararg envVars: Pair<String, String?>,
    ): OkHttpClient {
        val env = FakeEnv(*envVars)
        if ("GRADLE_ENTERPRISE_API_TOKEN" !in env)
            env["GRADLE_ENTERPRISE_API_TOKEN"] = "example-token"
        if ("GRADLE_ENTERPRISE_API_URL" !in env)
            env["GRADLE_ENTERPRISE_API_URL"] = "example-url"
        return buildOkHttpClient(Options(env, FakeKeychain()))
    }
}
