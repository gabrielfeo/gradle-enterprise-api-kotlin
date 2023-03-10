package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.FakeEnv
import com.gabrielfeo.gradle.enterprise.api.internal.FakeKeychain
import com.gabrielfeo.gradle.enterprise.api.internal.auth.HttpBearerAuth
import com.gabrielfeo.gradle.enterprise.api.internal.buildOkHttpClient
import com.gabrielfeo.gradle.enterprise.api.internal.buildRetrofit
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheEnforcingInterceptor
import com.gabrielfeo.gradle.enterprise.api.internal.caching.CacheHitLoggingInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.test.*

class RetrofitTest {

    @Test
    fun `Sets instance URL from options, stripping api segment`() {
        val retrofit = buildRetrofit(
            "GRADLE_ENTERPRISE_API_URL" to "https://example.com/api/",
        )
        // That's what generated classes expect
        assertEquals("https://example.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun `Rejects invalid URL`() {
        assertFails {
            buildRetrofit(
                "GRADLE_ENTERPRISE_API_URL" to "https://example.com/",
            )
        }
    }

    private fun buildRetrofit(
        vararg envVars: Pair<String, String?>,
    ): Retrofit {
        val env = FakeEnv(*envVars)
        if ("GRADLE_ENTERPRISE_API_TOKEN" !in env)
            env["GRADLE_ENTERPRISE_API_TOKEN"] = "example-token"
        val options = Options(env, FakeKeychain())
        return buildRetrofit(
            options = options,
            client = buildOkHttpClient(options),
            moshi = Moshi.Builder().build()
        )
    }
}