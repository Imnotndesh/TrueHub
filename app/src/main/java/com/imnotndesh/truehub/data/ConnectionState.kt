package com.imnotndesh.truehub.data

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String, val throwable: Throwable? = null) : ConnectionState()
}