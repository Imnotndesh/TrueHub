package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
