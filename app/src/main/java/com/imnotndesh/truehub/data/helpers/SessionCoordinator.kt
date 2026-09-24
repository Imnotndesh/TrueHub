package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.ConnectionState
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class SessionAuthenticationState {
    Unauthenticated,
    Pending,
    Authenticated,
    AuthenticatedTemporary
}

enum class RecoveryTrigger {
    TransportEvent,
    NetworkEvent,
    Foreground,
    ExplicitRetry,
    Worker
}

internal fun recoveryBackoffMillis(failureCount: Int): Long {
    val exponent = failureCount.coerceIn(0, 5)
    return (1_000L shl exponent).coerceAtMost(30_000L)
}

data class SessionRuntimeState(
    val generation: Long,
    val serverId: String?,
    val accountId: String?,
    val transportState: ConnectionState,
    val authenticationState: SessionAuthenticationState
)

sealed interface SessionState {
    data object Unauthenticated : SessionState
    data class Pending(
        val manager: TrueNASApiManager,
        val generation: Long
    ) : SessionState

    data class Authenticated(
        val manager: TrueNASApiManager,
        val serverId: String,
        val accountId: String,
        val tokenPersisted: Boolean,
        val generation: Long
    ) : SessionState
}

@Singleton
class SessionCoordinator @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<SessionState>(SessionState.Unauthenticated)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _transportState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val transportState: StateFlow<ConnectionState> = _transportState.asStateFlow()

    private val _authenticationState = MutableStateFlow(SessionAuthenticationState.Unauthenticated)
    val authenticationState: StateFlow<SessionAuthenticationState> = _authenticationState.asStateFlow()

    private val _runtimeState = MutableStateFlow(
        SessionRuntimeState(
            generation = 0,
            serverId = null,
            accountId = null,
            transportState = ConnectionState.Disconnected,
            authenticationState = SessionAuthenticationState.Unauthenticated
        )
    )
    val runtimeState: StateFlow<SessionRuntimeState> = _runtimeState.asStateFlow()

    private var generationCounter = 0L
    private var transportObservationJob: Job? = null
    private var deferredRecoveryJob: Job? = null
    private var recoveryFailureCount = 0

    val currentGeneration: Long
        get() = generationCounter

    val currentAuthenticatedManager: TrueNASApiManager?
        get() = (_state.value as? SessionState.Authenticated)?.manager

    fun publishPending(manager: TrueNASApiManager): Long {
        val generation = nextGeneration()
        cancelTransportObservation()
        cancelDeferredRecovery()
        recoveryFailureCount = 0
        SessionHolder.current = null
        _state.value = SessionState.Pending(manager, generation)
        _transportState.value = manager.connectionState.value
        _authenticationState.value = SessionAuthenticationState.Pending
        _runtimeState.value = SessionRuntimeState(
            generation = generation,
            serverId = null,
            accountId = null,
            transportState = manager.connectionState.value,
            authenticationState = SessionAuthenticationState.Pending
        )
        observeTransport(manager, generation)
        return generation
    }

    fun publishAuthenticated(
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String,
        tokenPersisted: Boolean,
        expectedGeneration: Long? = null
    ): Boolean {
        val currentState = _state.value
        val generation = when {
            expectedGeneration != null -> {
                if (expectedGeneration != generationCounter) return false
                expectedGeneration
            }
            currentState is SessionState.Pending -> generationCounter
            currentState is SessionState.Authenticated &&
                currentState.manager === manager &&
                currentState.serverId == serverId &&
                currentState.accountId == accountId -> generationCounter
            else -> nextGeneration()
        }
        cancelTransportObservation()
        cancelDeferredRecovery()
        recoveryFailureCount = 0
        manager.bindSessionProfile(serverId, accountId)
        _state.value = SessionState.Authenticated(
            manager = manager,
            serverId = serverId,
            accountId = accountId,
            tokenPersisted = tokenPersisted,
            generation = generation
        )
        _transportState.value = manager.connectionState.value
        val authenticationState = if (tokenPersisted) {
            SessionAuthenticationState.Authenticated
        } else {
            SessionAuthenticationState.AuthenticatedTemporary
        }
        _authenticationState.value = authenticationState
        _runtimeState.value = SessionRuntimeState(
            generation = generation,
            serverId = serverId,
            accountId = accountId,
            transportState = manager.connectionState.value,
            authenticationState = authenticationState
        )
        SessionHolder.current = manager
        observeTransport(manager, generation)
        return true
    }

    fun replaceAuthenticated(
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String,
        tokenPersisted: Boolean
    ): TrueNASApiManager? {
        val previousManager = SessionHolder.current
        val generation = nextGeneration()
        cancelTransportObservation()
        cancelDeferredRecovery()
        recoveryFailureCount = 0
        manager.bindSessionProfile(serverId, accountId)
        _state.value = SessionState.Authenticated(
            manager = manager,
            serverId = serverId,
            accountId = accountId,
            tokenPersisted = tokenPersisted,
            generation = generation
        )
        _transportState.value = manager.connectionState.value
        val authenticationState = if (tokenPersisted) {
            SessionAuthenticationState.Authenticated
        } else {
            SessionAuthenticationState.AuthenticatedTemporary
        }
        _authenticationState.value = authenticationState
        _runtimeState.value = SessionRuntimeState(
            generation = generation,
            serverId = serverId,
            accountId = accountId,
            transportState = manager.connectionState.value,
            authenticationState = authenticationState
        )
        SessionHolder.current = manager
        observeTransport(manager, generation)
        return previousManager
    }

    fun clear() {
        nextGeneration()
        cancelTransportObservation()
        cancelDeferredRecovery()
        recoveryFailureCount = 0
        _state.value = SessionState.Unauthenticated
        _transportState.value = ConnectionState.Disconnected
        _authenticationState.value = SessionAuthenticationState.Unauthenticated
        _runtimeState.value = SessionRuntimeState(
            generation = generationCounter,
            serverId = null,
            accountId = null,
            transportState = ConnectionState.Disconnected,
            authenticationState = SessionAuthenticationState.Unauthenticated
        )
        SessionHolder.current = null
    }

    fun requestRecovery(trigger: RecoveryTrigger): Boolean {
        val state = _state.value as? SessionState.Authenticated ?: return false
        val immediate = trigger == RecoveryTrigger.ExplicitRetry || trigger == RecoveryTrigger.Foreground
        val skipIfConnected = trigger == RecoveryTrigger.TransportEvent
        return scheduleDeferredRecovery(
            manager = state.manager,
            generation = state.generation,
            immediate = immediate,
            skipIfConnected = skipIfConnected
        )
    }

    fun isCurrent(manager: TrueNASApiManager, generation: Long): Boolean {
        val state = _state.value as? SessionState.Authenticated ?: return false
        return state.manager === manager && state.generation == generation
    }

    private fun nextGeneration(): Long {
        generationCounter += 1
        return generationCounter
    }

    private fun cancelTransportObservation() {
        transportObservationJob?.cancel()
        transportObservationJob = null
    }

    private fun cancelDeferredRecovery() {
        deferredRecoveryJob?.cancel()
        deferredRecoveryJob = null
    }

    private fun scheduleDeferredRecovery(
        manager: TrueNASApiManager,
        generation: Long,
        immediate: Boolean,
        skipIfConnected: Boolean
    ): Boolean {
        if (!isCurrent(manager, generation) || deferredRecoveryJob?.isActive == true) return false
        val delayMillis = if (immediate) 0L else recoveryBackoffMillis(recoveryFailureCount)
        deferredRecoveryJob = scope.launch {
            if (delayMillis > 0) delay(delayMillis)
            if (!isCurrent(manager, generation)) return@launch
            if (skipIfConnected && manager.isConnected()) {
                recoveryFailureCount = 0
                return@launch
            }
            when (manager.recoverAfterDisconnect()) {
                SessionProvider.RecoveryResult.Recovered,
                SessionProvider.RecoveryResult.AuthenticatedTemporary -> recoveryFailureCount = 0
                SessionProvider.RecoveryResult.Retryable -> {
                    recoveryFailureCount = (recoveryFailureCount + 1).coerceAtMost(5)
                    scheduleDeferredRecovery(manager, generation, immediate, skipIfConnected)
                }
                is SessionProvider.RecoveryResult.OtpRequired,
                SessionProvider.RecoveryResult.CredentialsRejected,
                SessionProvider.RecoveryResult.Unauthenticated -> Unit
            }
        }
        return true
    }

    private fun observeTransport(manager: TrueNASApiManager, generation: Long) {
        transportObservationJob = scope.launch {
            var previousTransportState: ConnectionState? = null
            manager.connectionState.collectLatest { transportState ->
                val currentState = _state.value
                val belongsToCurrentSession = when (currentState) {
                    is SessionState.Pending -> currentState.manager === manager && currentState.generation == generation
                    is SessionState.Authenticated -> currentState.manager === manager && currentState.generation == generation
                    SessionState.Unauthenticated -> false
                }
                if (generation != generationCounter || !belongsToCurrentSession) return@collectLatest
                val wasConnected = previousTransportState is ConnectionState.Connected
                previousTransportState = transportState
                _transportState.value = transportState
                _runtimeState.value = _runtimeState.value.copy(
                    transportState = transportState
                )
                if (wasConnected && transportState !is ConnectionState.Connected &&
                    currentState is SessionState.Authenticated
                ) {
                    scheduleDeferredRecovery(
                        manager = manager,
                        generation = generation,
                        immediate = false,
                        skipIfConnected = true
                    )
                }
            }
        }
    }
}
