package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json

data class RpcRequest(
    @field:Json("jsonrpc")
    val jsonrpc: String = "2.0",
    @field:Json("id")
    val id: Int,
    @field:Json("method")
    val method: String,
    @field:Json("params")
    val params: List<Any?>
)
data class RpcResponse<T>(
    @field:Json("jsonrpc")
    val jsonrpc: String,
    @field:Json("id")
    val id: Int? = null,
    @field:Json("result")
    val result: T? = null,
    @field:Json("error")
    val error: RpcError? = null,
    @field:Json("method")
    val method: String? = null,
    @field:Json("params")
    val params: Any? = null
)
data class RpcError(
    @field:Json("code")
    val code: Int,
    @field:Json("message")
    val message: String,
    @field:Json("data")
    val data: Any? = null
)