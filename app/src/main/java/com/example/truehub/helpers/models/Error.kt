package com.example.truehub.helpers.models

data class Error(
    val jsonrpc: String,
    val id: Int,
    val error: ErrorObject? = null
)
