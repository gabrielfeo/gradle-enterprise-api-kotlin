# Gradle Enterprise API Kotlin

[![Release](https://jitpack.io/v/gabrielfeo/gradle-enterprise-api-kotlin.svg)][14]
[![Javadoc](https://img.shields.io/badge/javadoc-latest-orange)][15]

A Kotlin library to access the [Gradle Enterprise API][1], easy to use from Kotlin
scripts:

```kotlin
gradleEnterpriseApi.getBuilds(since = yesterday).forEach {
  println(it)
}
```

## Setup

Set up once and use the library from any script in your machine:

- [`GRADLE_ENTERPRISE_API_URL`][16] environment variable: the URL of your Gradle Enterprise instance
- [`GRADLE_ENTERPRISE_API_TOKEN`][17] environment variable: an API access token for the Gradle
  Enterprise instance. Alternatively, can be a macOS keychain entry labeled
  `gradle-enterprise-api-token` (recommended).

That's it! From any `kts` script, you can add a dependency on the library and start querying the
API:

```kotlin
@file:Repository("https://jitpack.io")
@file:DependsOn("com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0")

gradleEnterpriseApi.getBuild(id = "hy5nxbzfjxe5k")
```

For configuring base URL and token via code and other available options, see the
[`options` object][8]. HTTP caching is available, which can speed up queries significantly, but is
off by default. Enable with [`GRADLE_ENTERPRISE_API_CACHE_ENABLED`][12]. See [`CacheOptions`][13]
for caveats.

<details>
  <summary>Setup in full projects (non-scripts)</summary>

  You can also use it in a full Kotlin project instead of a script. Just add a dependency:

  ```kotlin
  repositories {
    maven(url = "https://jitpack.io")
  }
  dependencies {
    implementation("com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0")
  }
  ```

  <details>
    <summary>Groovy</summary>

    ```groovy
    repositories {
      maven { url = 'https://jitpack.io' }
    }
    dependencies {
      implementation 'com.github.gabrielfeo:gradle-enterprise-api-kotlin:1.0'
    }
    ```

  </details>
</details>

## Usage

API endpoints are provided as a single interface: [`GradleEnterpriseApi`][9]. The Javadoc is a
the same as Gradle's online docs, as they're generated from the same spec. An instance is
initialized and ready-to-use as the global `gradleEnterpriseApi` instance:

```kotlin
gradleEnterpriseApi.getBuild(id = "hy5nxbzfjxe5k")
```

The library also provides a few extension functions on top of the regular API, such as paged
requests and joining. See [`GradleEnterpriseApi` extensions][10].

```kotlin
// Standard query to /api/builds, limited to 1000 builds server-side
gradleEnterpriseApi.getBuilds(since = lastMonth)
// Extension: Streams all available builds since given date (paging underneath)
gradleEnterpriseApi.getBuildsFlow(since = lastMonth)
```

It's recommended to call [`shutdown()`][11] at the end of scripts to release resources and let the
program exit. Otherwise, it'll keep running for an extra ~60s after code finishes, as an [expected
behavior of OkHttp][4].

```kotlin
val builds = gradleEnterpriseApi.getBuilds()
// do work ...
shutdown()
```

## More info

- Currently built for Gradle Enterprise `2022.4`, but should work fine with previous versions.
- Use JDK 8 or 14+ to run, if you want to avoid the ["illegal reflective access" warning about
  Retrofit][3]
- All classes live in these two packages. If you need to make small edits to scripts where
  there's no auto-complete, wildcard imports can be used:

```kotlin
import com.gabrielfeo.gradle.enterprise.api.*
import com.gabrielfeo.gradle.enterprise.api.model.*
```

###  Internals

API classes such as `GradleEnterpriseApi` and response models are generated from the offical
[API spec][5], using the [OpenAPI Generator Gradle Plugin][6].

[1]: https://docs.gradle.com/enterprise/api-manual/
[2]: https://square.github.io/retrofit/
[3]: https://github.com/square/retrofit/issues/3448
[4]: https://github.com/square/retrofit/issues/3144#issuecomment-508300518
[5]: https://docs.gradle.com/enterprise/api-manual/#reference_documentation
[6]: https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
[7]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/
[8]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/options.html
[9]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/-gradle-enterprise-api/
[10]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/-gradle-enterprise-api/index.html#373241164%2FExtensions%2F769193423
[11]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/shutdown.html
[12]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/-options/-cache-options/index.html#1054652077%2FProperties%2F769193423
[13]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/-options/-cache-options/index.html
[14]: https://jitpack.io/#gabrielfeo/gradle-enterprise-api-kotlin
[15]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/
[16]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/-options/-gradle-enterprise-instance-options/index.html#-259580834%2FProperties%2F769193423
[17]: https://gabrielfeo.github.io/gradle-enterprise-api-kotlin/gradle-enterprise-api-kotlin/com.gabrielfeo.gradle.enterprise.api/-options/-gradle-enterprise-instance-options/index.html#-42243308%2FProperties%2F769193423
