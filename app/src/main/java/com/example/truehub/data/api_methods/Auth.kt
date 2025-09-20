package com.example.truehub.data.api_methods

import com.example.truehub.data.Client


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
}
