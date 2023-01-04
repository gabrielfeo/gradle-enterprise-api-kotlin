@file:Suppress("MemberVisibilityCanBePrivate")

package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.Options.Cache.clear
import com.gabrielfeo.gradle.enterprise.api.Options.Cache.longTermCacheUrlPattern
import com.gabrielfeo.gradle.enterprise.api.Options.Cache.shortTermCacheUrlPattern
import com.gabrielfeo.gradle.enterprise.api.internal.buildCache
import com.gabrielfeo.gradle.enterprise.api.internal.requireBaseUrl
import com.gabrielfeo.gradle.enterprise.api.internal.requireToken
import java.io.File
import kotlin.time.Duration.Companion.days

/**
 * Library configuration options. Should not be changed after accessing the [api] object for the
 * first time.
 */
object Options {

    /**
     * Options about the GE instance, such as URL and API token.
     */
    object GradleEnterpriseInstance {

        /**
         * Provides the URL of a Gradle Enterprise API instance (without `/api`). By default, uses
         * environment variable `GRADLE_ENTERPRISE_URL`.
         */
        var url: () -> String = {
            requireBaseUrl(envName = "GRADLE_ENTERPRISE_URL")
        }

        /**
         * Provides the access token for a Gradle Enterprise API instance. By default, uses keychain entry
         * `gradle-enterprise-api-token` or environment variable `GRADLE_ENTERPRISE_URL`.
         */
        var token: () -> String = {
            requireToken(
                keychainName = "gradle-enterprise-api-token",
                envName = "GRADLE_ENTERPRISE_API_TOKEN",
            )
        }
    }

    /**
     * Concurrency options.
     */
    object Concurrency {

        /**
         * Maximum amount of concurrent requests allowed. Further requests will be queued. By default,
         * uses environment variable `GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS` or 15.
         *
         * https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher
         */
        var maxConcurrentRequests =
            System.getenv("GRADLE_ENTERPRISE_API_MAX_CONCURRENT_REQUESTS")?.toInt()
                ?: 15
    }

    /**
     * HTTP cache is off by default, but can speed up requests significantly. The Gradle Enterprise
     * API disallows HTTP caching, but this library forcefully enables it by overwriting
     * cache-related headers in API responses. Enable with [cacheEnabled].
     *
     * Responses can be:
     *
     * - cached short-term: default max-age of 1 day
     *   - `/api/builds`
     * - cached long-term: default max-age of 1 year
     *   - `/api/builds/{id}/gradle-attributes`
     *   - `/api/builds/{id}/maven-attributes`
     * - not cached
     *   - all other paths
     *
     * Whether a response is cached short-term, long-term or not cached at
     * all depends on whether it was matched by [shortTermCacheUrlPattern] or
     * [longTermCacheUrlPattern].
     *
     * Whenever GE is upgraded, cache should be [clear]ed.
     *
     * ### Caveats
     *
     * While not encouraged by the API, caching shouldn't have any major downsides other than a
     * time gap for certain queries, or having to reset cache when GE is upgraded.
     *
     * #### Time gap
     *
     * `/api/builds` responses always change as new builds are uploaded. Caching this path
     * short-term (default 1 day) means new builds uploaded after the cached response won't be
     * included in the query until the cache is invalidated 24h later. If that's a problem,
     * caching can be disabled for this `/api/builds` by changing [shortTermCacheUrlPattern].
     *
     * #### GE upgrades
     *
     * When GE is upgraded, any API response can change. New data might be available in API
     * endpoints such as `/api/build/{id}/gradle-attributes`. Thus, whenever the GE version
     * itself is upgraded, cache should be [clear]ed.
     */
    object Cache {

        /**
         * Whether caching is enabled. By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_CACHE_ENABLED` or `false`.
         */
        var cacheEnabled: Boolean =
            System.getenv("GRADLE_ENTERPRISE_API_CACHE_ENABLED").toBoolean()

        /**
         * Clears [cacheDir] including files that weren't created by the cache.
         */
        fun clear() {
            buildCache().delete()
        }

        /**
         * HTTP cache location. By default, uses environment variable `GRADLE_ENTERPRISE_API_CACHE_DIR`
         * or the system temporary folder (`java.io.tmpdir` / gradle-enterprise-api-kotlin-cache).
         */
        var cacheDir =
            System.getenv("GRADLE_ENTERPRISE_API_CACHE_DIR")?.let(::File)
                ?: File(System.getProperty("java.io.tmpdir"), "gradle-enterprise-api-kotlin-cache")

        /**
         * Max size of the HTTP cache. By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE` or ~1 GB.
         */
        var maxCacheSize =
            System.getenv("GRADLE_ENTERPRISE_API_MAX_CACHE_SIZE")?.toLong()
                ?: 1_000_000_000L

        /**
         * Regex pattern to match API URLs that are OK to store long-term in the HTTP cache, up to
         * [longTermCacheMaxAge] (1y by default, max value). By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds/{id}/gradle-attributes
         * - {host}/api/builds/{id}/maven-attributes
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        var longTermCacheUrlPattern: Regex =
            System.getenv("GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_URL_PATTERN")?.toRegex()
                ?: """.*/api/builds/[\d\w]+/(?:gradle|maven)-attributes""".toRegex()

        /**
         * Max age in seconds for URLs to be cached long-term (matched by [longTermCacheUrlPattern]).
         * By default, uses environment variable `GRADLE_ENTERPRISE_API_LONG_TERM_CACHE_MAX_AGE` or 1 year.
         */
        var longTermCacheMaxAge: Long =
            System.getenv("GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE")?.toLong()
                ?: 365.days.inWholeSeconds

        /**
         * Regex pattern to match API URLs that are OK to store short-term in the HTTP cache, up to
         * [shortTermCacheMaxAge] (1d by default). By default, uses environment variable
         * `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN` or a pattern matching:
         * - {host}/api/builds
         *
         * Use `|` to define multiple patterns in one, e.g. `.*gradle-attributes|.*test-distribution`.
         */
        var shortTermCacheUrlPattern: Regex =
            System.getenv("GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_URL_PATTERN")?.toRegex()
                ?: """.*/builds(?:\?.*|\Z)""".toRegex()

        /**
         * Max age in seconds for URLs to be cached short-term (matched by [shortTermCacheUrlPattern]).
         * By default, uses environment variable `GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE` or 1 day.
         */
        var shortTermCacheMaxAge: Long =
            System.getenv("GRADLE_ENTERPRISE_API_SHORT_TERM_CACHE_MAX_AGE")?.toLong()
                ?: 1.days.inWholeSeconds
    }

    /**
     * Library debugging options.
     */
    object Debugging {

        /**
         * Enables debug logging from the library. All logging is output to stderr. By default, uses
         * environment variable `GRADLE_ENTERPRISE_API_DEBUG_LOGGING` or `false`.
         */
        var debugLoggingEnabled =
            System.getenv("GRADLE_ENTERPRISE_API_DEBUG_LOGGING").toBoolean()
    }
}