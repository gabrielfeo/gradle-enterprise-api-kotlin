package com.gabrielfeo.gradle.enterprise.api

import com.gabrielfeo.gradle.enterprise.api.internal.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains

class GradleEnterpriseApiTest {

    @Test
    fun `Fails eagerly if no API URL`() {
        env = FakeEnv()
        keychain = FakeKeychain()
        systemProperties = FakeSystemProperties.linux
        val error = assertThrows<Exception> {
            GradleEnterpriseApi.newInstance(Config())
        }
        error.assertRootMessageContains("GRADLE_ENTERPRISE_API_URL")
    }

    @Test
    fun `Fails lazily if no API token`() {
        env = FakeEnv("GRADLE_ENTERPRISE_API_URL" to "example-url")
        keychain = FakeKeychain()
        systemProperties = FakeSystemProperties.linux
        val api = assertDoesNotThrow {
            GradleEnterpriseApi.newInstance(Config())
        }
        val error = assertThrows<Exception> {
            api.buildsApi.toString()
        }
        error.assertRootMessageContains("GRADLE_ENTERPRISE_API_TOKEN")
    }

    private fun Throwable.assertRootMessageContains(text: String) {
        cause?.assertRootMessageContains(text) ?: assertContains(message.orEmpty(), text)
    }
}
