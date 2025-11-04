package com.imnotndesh.truehub.data.models

import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * Represents a saved server configuration
 */
@JsonClass(generateAdapter = true)
data class SavedServer(
    val id: String = UUID.randomUUID().toString(),
    val serverUrl: String,
    val insecure: Boolean,
    val nickname: String? = null,
    val lastUsed: Long = java.lang.System.currentTimeMillis()
)

/**
 * Represents a saved account for a specific server
 */
@JsonClass(generateAdapter = true)
data class SavedAccount(
    val id: String = UUID.randomUUID().toString(),
    val serverId: String,
    val username: String,
    val loginMethod: LoginMethod,
    val lastUsed: Long = java.lang.System.currentTimeMillis(),
    val autoLoginEnabled: Boolean = false
)

enum class LoginMethod {
    PASSWORD,
    API_KEY
}

/**
 * Complete profile with server + account info
 */
data class AccountProfile(
    val server: SavedServer,
    val account: SavedAccount
) {
    val displayName: String
        get() = "${account.username} @ ${server.nickname ?: server.serverUrl}"
}