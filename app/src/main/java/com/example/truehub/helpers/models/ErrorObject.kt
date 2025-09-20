package com.example.truehub.helpers.models

data class ErrorObject(
    val code : Int,
    val message : String,
    val data : Any? = null
)
