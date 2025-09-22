package com.example.truehub.ui.profile

import com.example.truehub.helpers.models.AuthUserDetailsResponse
import com.example.truehub.helpers.models.System


sealed class UiState {
    object Loading : UiState()
    data class Success(
        val user: AuthUserDetailsResponse,
        val system: System.SystemInfo
    ) : UiState()
    data class Error(val message: String) : UiState()
}