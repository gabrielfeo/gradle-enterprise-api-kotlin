package com.gabrielfeo.gradle.enterprise.api.model

import com.gabrielfeo.gradle.enterprise.api.extension.contains
import com.gabrielfeo.gradle.enterprise.api.extension.get
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildAttributesValueExtensionsTest  {

    @Test
    fun get() {
        val list = listOf(
            BuildAttributesValue(name = "foo", "bar"),
            BuildAttributesValue(name = "bar", "foo"),
        )
        assertEquals("bar", list["foo"])
    }

    @Test
    fun contains() {
        val list = listOf(
            BuildAttributesValue(name = "foo", "bar"),
        )
        assertTrue("foo" in list)
        assertTrue("bar" !in list)
    }
}