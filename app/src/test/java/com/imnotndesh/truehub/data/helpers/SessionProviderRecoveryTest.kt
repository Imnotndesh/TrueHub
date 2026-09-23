package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.LoginExResult
import com.imnotndesh.truehub.data.models.LoginMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionProviderRecoveryTest {
    @Test
    fun booleanLoginResultRequiresTrue() {
        assertTrue(SessionProvider.isSuccessfulBooleanResult(ApiResult.Success(true)))
        assertFalse(SessionProvider.isSuccessfulBooleanResult(ApiResult.Success(false)))
        assertFalse(SessionProvider.isSuccessfulBooleanResult(ApiResult.Error("failed")))
    }

    @Test
    fun tokenIdentityRequiresValidAuthenticatedUsername() {
        assertFalse(SessionProvider.isValidTokenIdentity(null, LoginMethod.PASSWORD, "user"))
        assertFalse(SessionProvider.isValidTokenIdentity(" ", LoginMethod.PASSWORD, "user"))
        assertFalse(SessionProvider.isValidTokenIdentity("missing", LoginMethod.API_KEY, "api_key"))
    }

    @Test
    fun passwordAndTotpTokenIdentityMatchesSavedUsernameIgnoringCase() {
        assertTrue(SessionProvider.isValidTokenIdentity("Alice", LoginMethod.PASSWORD, "alice"))
        assertTrue(SessionProvider.isValidTokenIdentity("Alice", LoginMethod.TOTP, "alice"))
        assertFalse(SessionProvider.isValidTokenIdentity("Bob", LoginMethod.PASSWORD, "alice"))
    }

    @Test
    fun apiKeyTokenIdentityDoesNotCompareSyntheticUsername() {
        assertTrue(SessionProvider.isValidTokenIdentity("key-owner", LoginMethod.API_KEY, "api_key"))
    }

    @Test
    fun loginExCredentialFailuresAreNotRetryable() {
        assertEquals(
            SessionProvider.AuthOutcome.CredentialsRejected,
            SessionProvider.mapLoginExAuthResult(LoginExResult.AuthRespAuthErr())
        )
        assertEquals(
            SessionProvider.AuthOutcome.CredentialsRejected,
            SessionProvider.mapLoginExAuthResult(LoginExResult.AuthRespAuthExpired())
        )
    }

    @Test
    fun loginExOtpResponseIsPreserved() {
        assertEquals(
            SessionProvider.AuthOutcome.OtpRequired("user"),
            SessionProvider.mapLoginExAuthResult(LoginExResult.AuthRespOTPRequired(username = "user"))
        )
    }

    @Test
    fun refreshContinuesOnlyAfterSuccessfulRecovery() {
        assertTrue(SessionProvider.allowsRefreshAfterRecovery(SessionProvider.RecoveryResult.Recovered))
        assertTrue(SessionProvider.allowsRefreshAfterRecovery(SessionProvider.RecoveryResult.AuthenticatedTemporary))
        assertFalse(SessionProvider.allowsRefreshAfterRecovery(SessionProvider.RecoveryResult.Retryable))
        assertFalse(SessionProvider.allowsRefreshAfterRecovery(SessionProvider.RecoveryResult.CredentialsRejected))
    }
}
