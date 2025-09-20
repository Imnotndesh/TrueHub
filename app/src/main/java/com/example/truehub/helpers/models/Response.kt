package com.example.truehub.helpers.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Response<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: Error? = null
)
