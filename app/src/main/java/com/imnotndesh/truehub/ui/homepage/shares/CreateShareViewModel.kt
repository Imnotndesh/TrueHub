package com.imnotndesh.truehub.ui.homepage.shares

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.utils.AppCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CreateShareType {
    Smb,
    Nfs
}

internal fun normalizeSharePath(path: String): String {
    val trimmed = path.trim()
    return if (trimmed.startsWith('/')) trimmed else "/$trimmed"
}

internal fun smbSharePayload(
    path: String,
    name: String,
    comment: String,
    enabled: Boolean,
    readOnly: Boolean,
    guestAccess: Boolean
): Map<String, Any?> {
    return mapOf(
        "path" to normalizeSharePath(path),
        "name" to name.trim(),
        "comment" to comment.trim(),
        "browsable" to true,
        "ro" to readOnly,
        "guestok" to guestAccess,
        "enabled" to enabled
    )
}

internal fun nfsSharePayload(
    path: String,
    comment: String,
    enabled: Boolean,
    readOnly: Boolean
): Map<String, Any?> {
    return mapOf(
        "path" to normalizeSharePath(path),
        "comment" to comment.trim(),
        "ro" to readOnly,
        "enabled" to enabled
    )
}

data class CreateShareUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val created: Boolean = false
)

@HiltViewModel
class CreateShareViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateShareUiState())
    val uiState: StateFlow<CreateShareUiState> = _uiState.asStateFlow()

    fun create(
        type: CreateShareType,
        path: String,
        name: String,
        comment: String,
        enabled: Boolean,
        readOnly: Boolean,
        guestAccess: Boolean
    ) {
        val normalizedPath = normalizeSharePath(path)
        val normalizedName = name.trim()
        val validationError = when {
            normalizedPath.isBlank() -> "Enter a dataset path"
            type == CreateShareType.Smb && normalizedName.isBlank() -> "Enter a share name"
            else -> null
        }
        if (validationError != null) {
            _uiState.value = CreateShareUiState(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateShareUiState(isSaving = true)
            val payload = when (type) {
                CreateShareType.Smb -> smbSharePayload(
                    path = normalizedPath,
                    name = normalizedName,
                    comment = comment,
                    enabled = enabled,
                    readOnly = readOnly,
                    guestAccess = guestAccess
                )
                CreateShareType.Nfs -> nfsSharePayload(
                    path = normalizedPath,
                    comment = comment,
                    enabled = enabled,
                    readOnly = readOnly
                )
            }

            if (type == CreateShareType.Smb) {
                when (val precheck = manager.sharing.precheckSmbShareWithResult(payload)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Error -> {
                        _uiState.value = CreateShareUiState(error = precheck.message)
                        return@launch
                    }
                    ApiResult.Loading -> return@launch
                }
            }

            when (type) {
                CreateShareType.Smb -> {
                    when (val result = manager.sharing.createSmbShareWithResult(payload)) {
                        is ApiResult.Success -> {
                            AppCache.addSmbShare(result.data)
                            _uiState.value = CreateShareUiState(created = true)
                        }
                        is ApiResult.Error -> _uiState.value = CreateShareUiState(error = result.message)
                        ApiResult.Loading -> _uiState.value = CreateShareUiState()
                    }
                }
                CreateShareType.Nfs -> {
                    when (val result = manager.sharing.createNfsShareWithResult(payload)) {
                        is ApiResult.Success -> {
                            AppCache.addNfsShare(result.data)
                            _uiState.value = CreateShareUiState(created = true)
                        }
                        is ApiResult.Error -> _uiState.value = CreateShareUiState(error = result.message)
                        ApiResult.Loading -> _uiState.value = CreateShareUiState()
                    }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
