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
    val autoLoginEnabled: Boolean = false,
    /**
     * Friendly name for this device, generated at sign-in. Used to label the API key that was
     * minted for a device paired over QR, so it can be identified and revoked later.
     */
    val deviceName: String? = null
)

enum class LoginMethod {
    PASSWORD,
    API_KEY,
    TOTP,

    /**
     * Signed in by scanning a QR code produced by another device that was already signed in.
     * Devices using this method cannot themselves authorise new devices (see Easy Setup).
     */
    PAIRED
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