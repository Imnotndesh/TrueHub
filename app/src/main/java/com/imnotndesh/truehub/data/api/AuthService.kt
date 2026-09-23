package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Auth
import com.imnotndesh.truehub.data.models.LoginExResult
import com.imnotndesh.truehub.data.models.LoginMechanisms
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class AuthService(val manager: TrueNASApiManager) {
    data class DefaultAuth(
        val username: String,
        val password: String,
        val otpToken: String? = null
    )

    suspend fun loginUserWithResult(details: DefaultAuth): ApiResult<Boolean> {
        val defaultParams = listOf(details.username, details.password)
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.AUTH_LOGIN,
            params = defaultParams,
            resultType = Boolean::class.java
        )
    }

    suspend fun loginWithApiKeyWithResult(apiKey: String): ApiResult<Boolean> {
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.AUTH_API_LOGIN,
            params = listOf(apiKey),
            resultType = Boolean::class.java
        )
    }

    suspend fun logoutUserWithResult(): ApiResult<Boolean> {
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.AUTH_LOGOUT,
            params = listOf(),
            resultType = Boolean::class.java
        )
    }

    suspend fun getUserDetailsWithResult(): ApiResult<Auth.AuthResponse> {
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.AUTH_ME,
            params = listOf(),
            resultType = Auth.AuthResponse::class.java
        )
    }

    suspend fun generateTokenWithResult(tokenRequest: Auth.TokenRequest = Auth.TokenRequest()): ApiResult<String> {
        return manager.callWithResultWithoutRecovery(
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

    suspend fun loginWithTokenAndResult(token: String): ApiResult<Boolean> {
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.AUTH_TOKEN_LOGIN,
            params = listOf(token),
            resultType = Boolean::class.java
        )
    }

    /**
     * Generate one time password for username passed
     * @param Auth.AdminMethods.GenerateOneTimePasswordRequest
     * @return String with one time password
     */
    suspend fun generateOneTimePassword(username: String): ApiResult<String> {
        val request = Auth.AdminMethods.GenerateOneTimePasswordRequest(
            username
        )
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.GEN_ONETIME_PASSWORD,
            params = listOf(request),
            resultType = String::class.java
        )

    }

    /**
     * Get list of mechanisms available for login. Useful for loginEx method
     * @return List of string for login mechanisms available
     */
    suspend fun getMechanismChoices(): ApiResult<List<String>> {
        val result = Types.newParameterizedType(List::class.java, String::class.java)
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.GET_MECHANISM_CHOICES,
            params = listOf(),
            resultType = result
        )
    }

    suspend fun loginEx(
        mechanism: LoginMechanisms,
        includeUserInfo: Boolean = false,
        includeReconnectToken: Boolean = false
    ): ApiResult<LoginExResult> {
        val paramsMap = mutableMapOf<String, Any>()
        val loginOptions = mutableMapOf<String, Boolean>()
        if (includeUserInfo) loginOptions["user_info"] = true
        if (includeReconnectToken) loginOptions["reconnect_token"] = true
        if (loginOptions.isNotEmpty()) {
            paramsMap["login_options"] = loginOptions
        }
        when (mechanism) {
            is LoginMechanisms.AuthPasswordPlain -> {
                paramsMap["mechanism"] = "PASSWORD_PLAIN"
                paramsMap["username"] = mechanism.username
                paramsMap["password"] = mechanism.password
            }
            is LoginMechanisms.AuthApiKeyPlain -> {
                paramsMap["mechanism"] = "API_KEY_PLAIN"
                paramsMap["username"] = mechanism.username
                paramsMap["api_key"] = mechanism.api_key
            }
            is LoginMechanisms.AuthTokenPlain -> {
                paramsMap["mechanism"] = "AUTH_TOKEN_PLAIN"
                paramsMap["token"] = mechanism.token
            }
            is LoginMechanisms.AuthOTPToken -> {
                paramsMap["mechanism"] = "OTP_TOKEN"
                paramsMap["otp_token"] = mechanism.otp_token
            }
            else -> return ApiResult.Error("Unsupported authentication mechanism")
        }

        val resType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val result = manager.callWithResultWithoutRecovery<Map<String, Any?>>(
            method = ApiMethods.Auth.LOGIN_EX,
            params = listOf(paramsMap),
            resultType = resType
        )

        return when (result) {
            is ApiResult.Success -> {
                val raw = result.data
                val responseType = raw["response_type"] as? String ?: "AUTH_ERR"
                val loginResult = when (responseType) {
                    "SUCCESS" -> {
                        val authLevelStr = raw["authenticator"] as? String ?: "LEVEL_1"
                        val authenticator = try {
                            LoginExResult.AuthenticatorLevel.valueOf(authLevelStr)
                        } catch (e: IllegalArgumentException) {
                            LoginExResult.AuthenticatorLevel.LEVEL_1
                        }
                        val userInfoMap = raw["user_info"] as? Map<String, Any>
                        val authResponse = userInfoMap?.let { convertToAuthResponse(it) }
                        LoginExResult.AuthRespSuccess(
                            user_info = authResponse,
                            authenticator = authenticator
                        )
                    }
                    "OTP_REQUIRED" -> LoginExResult.AuthRespOTPRequired(
                        username = raw["username"] as? String ?: ""
                    )
                    "AUTH_ERR" -> LoginExResult.AuthRespAuthErr()
                    "EXPIRED" -> LoginExResult.AuthRespAuthExpired()
                    "REDIRECT" -> {
                        val urls = (raw["urls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        LoginExResult.AuthRespAuthRedirect(urls = urls)
                    }
                    else -> LoginExResult.AuthRespAuthErr()
                }
                ApiResult.Success(loginResult)
            }
            is ApiResult.Error -> {
                ApiResult.Error(result.message, result.throwable)
            }
            else -> {
                ApiResult.Error("Unexpected result type: ${result::class.simpleName}")
            }
        }
    }

    private fun convertToAuthResponse(map: Map<String, Any>): Auth.AuthResponse {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(Auth.AuthResponse::class.java)
        return adapter.fromJsonValue(map) ?: error("Failed to parse user_info")
    }
    suspend fun getSessions(): ApiResult<List<Auth.AuthSessionResultItem>>{
        val types = Types.newParameterizedType(List::class.java,Auth.AuthSessionResultItem::class.java)
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.GET_AUTH_SESSIONS,
            params = listOf(),
            resultType = types
        )
    }
    suspend fun terminateOtherSessions(): ApiResult<Boolean>{
        return manager.callWithResultWithoutRecovery(
            method = ApiMethods.Auth.TERMINATE_OTHER_SESSION,
            params = listOf(),
            resultType = Boolean::class.java
        )
    }
    suspend fun terminateSession(id: String): ApiResult<Boolean>{
        return manager.callWithResultWithoutRecovery(
            ApiMethods.Auth.TERMINATE_SESSION,
            listOf(id),
            Boolean::class.java
        )
    }

    suspend fun loginExContinueWithResult(loginData: Any?): ApiResult<Any?> =
        manager.callWithResultWithoutRecovery(ApiMethods.Auth.AUTH_LOGIN_EX_CONTINUE, listOf(loginData), Any::class.java)

    suspend fun setAttributeWithResult(key: Any?, value: Any?): ApiResult<Any?> =
        manager.callWithResultWithoutRecovery(ApiMethods.Auth.AUTH_SET_ATTRIBUTE, listOf(key, value), Any::class.java)

    suspend fun terminateOtherSessionsWithResult(): ApiResult<Any?> =
        manager.callWithResultWithoutRecovery(ApiMethods.Auth.AUTH_TERMINATE_OTHER_SESSIONS, listOf(), Any::class.java)

    suspend fun twofactorConfigWithResult(): ApiResult<Any?> =
        manager.callWithResultWithoutRecovery(ApiMethods.Auth.AUTH_TWOFACTOR_CONFIG, listOf(), Any::class.java)

    suspend fun twofactorUpdateWithResult(authTwofactorUpdate: Any?): ApiResult<Any?> =
        manager.callWithResultWithoutRecovery(ApiMethods.Auth.AUTH_TWOFACTOR_UPDATE, listOf(authTwofactorUpdate), Any::class.java)

}
