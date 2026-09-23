package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SessionState {
    data object Unauthenticated : SessionState
    data class Pending(val manager: TrueNASApiManager) : SessionState
    data class Authenticated(
        val manager: TrueNASApiManager,
        val serverId: String,
        val accountId: String,
        val tokenPersisted: Boolean
    ) : SessionState
}

@Singleton
class SessionCoordinator @Inject constructor() {
    private val _state = MutableStateFlow<SessionState>(SessionState.Unauthenticated)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val currentAuthenticatedManager: TrueNASApiManager?
        get() = (_state.value as? SessionState.Authenticated)?.manager

    fun publishPending(manager: TrueNASApiManager) {
        if (_state.value is SessionState.Authenticated) clear()
        _state.value = SessionState.Pending(manager)
    }

    fun publishAuthenticated(
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String,
        tokenPersisted: Boolean
    ) {
        _state.value = SessionState.Authenticated(manager, serverId, accountId, tokenPersisted)
        SessionHolder.current = manager
    }

    fun clear() {
        _state.value = SessionState.Unauthenticated
        SessionHolder.current = null
    }
}
