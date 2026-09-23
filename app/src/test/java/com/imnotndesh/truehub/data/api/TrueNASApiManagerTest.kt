package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASRpcException
import com.imnotndesh.truehub.data.helpers.SessionProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrueNASApiManagerTest {
    @Test
    fun directMethodErrorIsNotAuthenticationError() {
        val result = ApiResult.Error(
            "Method error",
            TrueNASRpcException(-32001, "Method error")
        )

        assertFalse(isAuthenticationError(result))
    }

    @Test
    fun nestedAuthenticationCodeIsAuthenticationError() {
        val result = ApiResult.Error(
            "Not authenticated",
            TrueNASRpcException(207, "Not authenticated")
        )

        assertTrue(isAuthenticationError(result))
    }

    @Test
    fun authenticationMessagesAreAuthenticationErrors() {
        assertTrue(
            isAuthenticationError(
                ApiResult.Error("enotauthenticated", TrueNASRpcException(22, "enotauthenticated"))
            )
        )
        assertTrue(
            isAuthenticationError(
                ApiResult.Error("Invalid session", TrueNASRpcException(22, "Invalid session"))
            )
        )
    }

    @Test
    fun successIsNotAuthenticationError() {
        assertFalse(isAuthenticationError(ApiResult.Success(true)))
    }

    @Test
    fun onlyAuthenticatedRecoveryResultsAllowRequestRetry() {
        assertTrue(allowsRequestRetry(SessionProvider.RecoveryResult.Recovered))
        assertTrue(allowsRequestRetry(SessionProvider.RecoveryResult.AuthenticatedTemporary))
        assertFalse(allowsRequestRetry(SessionProvider.RecoveryResult.OtpRequired("user")))
        assertFalse(allowsRequestRetry(SessionProvider.RecoveryResult.CredentialsRejected))
        assertFalse(allowsRequestRetry(SessionProvider.RecoveryResult.Unauthenticated))
        assertFalse(allowsRequestRetry(SessionProvider.RecoveryResult.Retryable))
    }
}
