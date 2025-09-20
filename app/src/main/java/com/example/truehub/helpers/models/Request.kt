package com.example.truehub.helpers.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Request(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String,
    val params: List<Any?>
)