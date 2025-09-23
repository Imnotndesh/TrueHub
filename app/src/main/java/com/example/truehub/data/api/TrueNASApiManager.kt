package com.example.truehub.data.api

import com.example.truehub.data.TrueNASClient

class TrueNASApiManager(private val client: TrueNASClient) {
    val auth: AuthService by lazy { AuthService(client) }
    val system: SystemService by lazy { SystemService(client) }
    val apps: AppsService by lazy { AppsService(client) }
    val connection: ConnectionService by lazy { ConnectionService(client) }

    // Convenience methods for connection management
    suspend fun connect(): Boolean = client.connect()
    fun disconnect() = client.disconnect()
    fun isConnected(): Boolean = client.isConnected()

    // State access
    val connectionState = client.connectionState
    val isLoading = client.isLoading
}