package com.gabrielfeo.gradle.enterprise.api.internal

import com.gabrielfeo.gradle.enterprise.api.options
import java.util.logging.Level
import java.util.logging.Logger

interface Keychain {
    operator fun get(entry: String): String?
}

class RealKeychain(
    private val systemProperties: SystemProperties,
) : Keychain {
    override fun get(entry: String): String? {
        val login = systemProperties["user.name"] ?: let {
            Logger.getGlobal().log(Level.INFO, "Failed to get key from keychain (null user.name)")
            return null
        }
        val process = ProcessBuilder(
            "security", "find-generic-password", "-w", "-a", login, "-s", entry
        ).start()
        val status = process.waitFor()
        if (status == 0) {
            return process.inputStream.bufferedReader().use {
                it.readText().trim()
            }
        } else if (options.debugging.debugLoggingEnabled) {
            Logger.getGlobal().log(Level.INFO, "Failed to get key from keychain (exit $status)")
        }
        return null
    }
}
