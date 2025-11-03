package com.imnotndesh.truehub.ui.profile

import com.imnotndesh.truehub.data.models.Auth.AuthResponse
import com.imnotndesh.truehub.data.models.System


sealed class UiState {
    object Loading : UiState()
    data class Success(
        val user: AuthResponse,
        val system: System.SystemInfo
    ) : UiState()
    data class Error(val message: String) : UiState()
}