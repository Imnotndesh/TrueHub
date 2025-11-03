package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Auth

class AuthService(val manager: TrueNASApiManager){
    data class DefaultAuth(
        val username: String,
        val password: String,
        val otpToken: String? = null
    )
    suspend fun loginUserWithResult(details: DefaultAuth): ApiResult<Boolean> {
        val defaultParams = listOf(details.username, details.password)
        return manager.callWithResult(
            method = ApiMethods.Auth.AUTH_LOGIN,
            params = defaultParams,
            resultType = Boolean::class.java
        )
    }

    suspend fun loginWithApiKeyWithResult(apiKey: String): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.Auth.AUTH_API_LOGIN,
            params = listOf(apiKey),
            resultType = Boolean::class.java
        )
    }

    suspend fun logoutUserWithResult(): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.Auth.AUTH_LOGOUT,
            params = listOf(),
            resultType = Boolean::class.java
        )
    }

    suspend fun getUserDetailsWithResult(): ApiResult<Auth.AuthResponse>{
        return manager.callWithResult(
            method = ApiMethods.Auth.AUTH_ME,
            params = listOf(),
            resultType = Auth.AuthResponse::class.java
        )
    }

    suspend fun generateTokenWithResult(tokenRequest: Auth.TokenRequest = Auth.TokenRequest()): ApiResult<String> {
        return manager.callWithResult(
            method = ApiMethods.Auth.GEN_AUTH_TOKEN,
            params = listOf(
                tokenRequest.ttl,
                tokenRequest.attrs,
                tokenRequest.matchOrigin,
                tokenRequest.singleUse
            ),
            resultType = String::class.java
        )
    }

    suspend fun loginWithTokenAndResult(token: String): ApiResult<Boolean>{
        return manager.callWithResult(
            method = ApiMethods.Auth.AUTH_TOKEN_LOGIN,
            params = listOf(token),
            resultType = Boolean::class.java
        )
    }
}