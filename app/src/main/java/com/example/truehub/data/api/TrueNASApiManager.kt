package com.example.truehub.data.api

import android.content.Context
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.helpers.Prefs

class TrueNASApiManager(private val client: TrueNASClient) {
    val auth: AuthService by lazy { AuthService(client) }
    val system: SystemService by lazy { SystemService(client) }
    val apps: AppsService by lazy { AppsService(client) }
    val connection: ConnectionService by lazy { ConnectionService(client) }
    val virtService: VirtService by lazy { VirtService(client) }
    val vmService: VmService by lazy { VmService(client) }
    val user: UserService by lazy { UserService(client) }
    val sharing : SharingService by lazy { SharingService(client) }

    // Convenience methods for connection management
    suspend fun connect(): Boolean = client.connect()
    fun disconnect() = client.disconnect()
    fun isConnected(): Boolean = client.isConnected()
    fun configure(context: Context,formattedUrl: String,insecure: Boolean){
        Prefs.save(context,formattedUrl,insecure)
    }

    // State access
    val connectionState = client.connectionState
    val isLoading = client.isLoading

    fun getClient(): TrueNASClient = client
}