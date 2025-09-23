package com.example.truehub.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String,
    val params: List<Any?>
)

@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int? = null,
    val result: T? = null,
    val error: RpcError? = null,
    val method: String? = null,
    val params: Any? = null
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String,
    val data: Any? = null
)