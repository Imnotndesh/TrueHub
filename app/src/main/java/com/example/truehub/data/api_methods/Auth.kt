package com.example.truehub.data.api_methods

import com.example.truehub.data.Client
import com.example.truehub.helpers.models.AuthUserDetailsResponse


class Auth(private val client : Client) {
    data class DefaultAuth(
        val username : String,
        val password : String,
        val otpToken : String? = null)
    suspend fun loginUser(details: Auth.DefaultAuth) : Boolean {
        val defaultParams = listOf(details.username,details.password)
        return client.call(
            method = ApiMethod.Auth.AUTH_LOGIN,
            params = defaultParams,
            resultClass = Boolean::class.java
        )
    }
    suspend fun loginWithApiKey(apiKey : String) : Boolean {
        val loginParams = listOf(apiKey)
        return client.call(
            method = ApiMethod.Auth.AUTH_API_LOGIN,
            params = loginParams,
            resultClass = Boolean::class.java
        )
    }

    suspend fun logoutUser() : Boolean {
        return client.call(
            method = ApiMethod.Auth.AUTH_LOGOUT,
            params = listOf(),
            resultClass = Boolean::class.java
        )
    }

    suspend fun getUserDetails() : AuthUserDetailsResponse {
        val raw = client.call(
            method = ApiMethod.Auth.AUTH_ME,
            params = listOf(),
            resultClass = Any::class.java
        ) as Map<*, *>
        return AuthUserDetailsResponse(
            username = raw["pw_name"] as String,
            fullName = raw["pw_gecos"] as String?,
            local = raw["local"] as Boolean,
            accountAttributes = (raw["account_attributes"] as? List<String>).orEmpty()
        )
    }

    suspend fun generateToken() : String {
        return client.call(
            method = ApiMethod.Auth.GEN_AUTH_TOKEN,
            params = listOf(),
            resultClass = String::class.java
        )
    }

    suspend fun loginWithToken(token : String) : Boolean {
        val loginParams = listOf(token)
        return client.call(
            method = ApiMethod.Auth.AUTH_TOKEN_LOGIN,
            params = loginParams,
            resultClass = Boolean::class.java
        )
    }
}
