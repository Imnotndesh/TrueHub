package com.example.truehub.helpers.models

import com.squareup.moshi.Json

data class AuthUserDetailsResponse (
    @field:Json(name="pw_name")
    val username : String,
    @field:Json(name="pw_gecos")
    val fullName : String?,
    @field:Json(name="local")
    val local: Boolean,
    @field:Json(name="account_attributes")
    val accountAttributes : List<String>,
)