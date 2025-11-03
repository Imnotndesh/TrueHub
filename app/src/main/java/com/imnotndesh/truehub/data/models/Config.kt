package com.imnotndesh.truehub.data.models

object Config {
    data class ClientConfig(
        val serverUrl: String,
        val insecure: Boolean = false,
        val enablePing: Boolean = true,
        val pingTimeoutMs: Long = 5000L,
        val connectionTimeoutMs: Long = 10000L,
        val enableDebugLogging: Boolean = true
    )
}