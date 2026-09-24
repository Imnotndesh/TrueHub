package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCoordinatorTest {
    @Test
    fun clearInvalidatesTheCurrentGeneration() {
        val coordinator = SessionCoordinator()
        val initialGeneration = coordinator.currentGeneration

        coordinator.clear()

        assertEquals(initialGeneration + 1, coordinator.currentGeneration)
        assertNull(coordinator.runtimeState.value.serverId)
        assertEquals(ConnectionState.Disconnected, coordinator.runtimeState.value.transportState)
        assertEquals(
            SessionAuthenticationState.Unauthenticated,
            coordinator.runtimeState.value.authenticationState
        )
    }

    @Test
    fun recoveryBackoffIsBounded() {
        assertEquals(1_000L, recoveryBackoffMillis(0))
        assertEquals(2_000L, recoveryBackoffMillis(1))
        assertEquals(30_000L, recoveryBackoffMillis(20))
    }

    @Test
    fun recoveryDiagnosticsRedactIdentifiersAndResults() {
        val first = redactedIdentifier("server-secret")
        val second = redactedIdentifier("server-secret")
        val other = redactedIdentifier("another-server")

        assertEquals(first, second)
        assertNotEquals(first, other)
        assertTrue(!first.contains("server-secret"))
        assertEquals("otp_required", recoveryResultName(SessionProvider.RecoveryResult.OtpRequired("user")))
        assertEquals("credentials_rejected", recoveryResultName(SessionProvider.RecoveryResult.CredentialsRejected))
    }

    @Test
    fun runtimeStateStartsUnauthenticatedAndDisconnected() {
        val coordinator = SessionCoordinator()

        assertEquals(SessionState.Unauthenticated, coordinator.state.value)
        assertEquals(ConnectionState.Disconnected, coordinator.transportState.value)
        assertEquals(
            SessionAuthenticationState.Unauthenticated,
            coordinator.authenticationState.value
        )
        assertEquals(0L, coordinator.runtimeState.value.generation)
    }
}
