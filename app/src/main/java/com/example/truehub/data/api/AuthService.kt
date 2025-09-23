package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Auth

class AuthService(override val client: TrueNASClient): BaseApiService(client) {
    data class DefaultAuth(
        val username: String,
        val password: String,
        val otpToken: String? = null
    )
    suspend fun loginUser(details: DefaultAuth): Boolean {
        val defaultParams = listOf(details.username, details.password)
        return client.call(
            method = ApiMethods.Auth.AUTH_LOGIN,
            params = defaultParams,
            resultType = Boolean::class.java
        )
    }

    suspend fun loginWithApiKey(apiKey: String): Boolean {
        val loginParams = listOf(apiKey)
        return client.call(
            method = ApiMethods.Auth.AUTH_API_LOGIN,
            params = loginParams,
            resultType = Boolean::class.java
        )
    }

    suspend fun logoutUser(): Boolean {
        return client.call(
            method = ApiMethods.Auth.AUTH_LOGOUT,
            params = listOf(),
            resultType = Boolean::class.java
        )
    }

    suspend fun getUserDetails(): Auth.AuthResponse {
        return client.call(
            method = ApiMethods.Auth.AUTH_ME,
            params = listOf(),
            resultType = Auth.AuthResponse::class.java
        )
    }
    suspend fun loginUserWithResult(details: DefaultAuth): ApiResult<Boolean> {
        return try {
            val result = loginUser(details)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Login failed: ${e.message}", e)
        }
    }
    suspend fun generateToken(): String {
        return client.call(
            method = ApiMethods.Auth.GEN_AUTH_TOKEN,
            params = listOf(),
            resultType = String::class.java
        )
    }

    suspend fun loginWithApiKeyWithResult(apiKey: String): ApiResult<Boolean> {
        return try {
            val result = loginWithApiKey(apiKey)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("API key login failed: ${e.message}", e)
        }
    }

    suspend fun logoutUserWithResult(): ApiResult<Boolean> {
        return try {
            val result = logoutUser()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Logout failed: ${e.message}", e)
        }
    }

    suspend fun getUserDetailsWithResult(): ApiResult<Auth.AuthResponse> {
        return try {
            val result = getUserDetails()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get user details: ${e.message}", e)
        }
    }

    suspend fun generateTokenWithResult(): ApiResult<String> {
        return try {
            val result = generateToken()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to generate token: ${e.message}", e)
        }
    }

    suspend fun loginWithToken(token: String): Boolean {
        val loginParams = listOf(token)
        return client.call(
            method = ApiMethods.Auth.AUTH_TOKEN_LOGIN,
            params = loginParams,
            resultType = Boolean::class.java
        )
    }
    suspend fun loginWithTokenAndResult(token: String): ApiResult<Boolean>{
        return try {
            val result = loginWithToken(token)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Token login failed: ${e.message}", e)
        }
    }
}