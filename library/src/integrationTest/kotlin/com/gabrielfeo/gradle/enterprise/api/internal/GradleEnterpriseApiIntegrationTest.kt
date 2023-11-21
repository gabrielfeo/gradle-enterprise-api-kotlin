package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.Config
import com.gabrielfeo.gradle.enterprise.api.GradleEnterpriseApi
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.Test

class GradleEnterpriseApiIntegrationTest {

    @Test
    fun canFetchBuildsWithDefaultConfig() = runTest {
        env = RealEnv
        keychain = RealKeychain(RealSystemProperties)
        val api = GradleEnterpriseApi.newInstance()
        val builds = api.buildsApi.getBuilds(
            since = 0,
            maxBuilds = 5,
            query = """tag:local value:"Email=gabriel.feo*""""
        )
        assertEquals(5, builds.size)
        api.shutdown()
    }

    @Test
    fun canBuildNewInstanceWithPureCodeConfiguration() = runTest {
        env = FakeEnv()
        keychain = FakeKeychain()
        assertDoesNotThrow {
            val config = Config(
                apiUrl = "https://google.com/api/",
                apiToken = { "" },
            )
            GradleEnterpriseApi.newInstance(config)
        }
    }
}