package com.imnotndesh.truehub.ui.homepage.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShareInfoUiState(
    val smbShare: Shares.SmbShare? = null,
    val nfsShare: Shares.NfsShare? = null,
    val smbAcl: Shares.SmbAcl? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null
)

class ShareInfoViewModel(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareInfoUiState())
    val uiState: StateFlow<ShareInfoUiState> = _uiState.asStateFlow()

    private var type: ShareType? = null

    fun load(shareType: ShareType) {
        type = shareType
        when (shareType) {
            is ShareType.Smb -> {
                _uiState.update { it.copy(smbShare = shareType.share) }
                refresh()
            }
            is ShareType.Nfs -> {
                _uiState.update { it.copy(nfsShare = shareType.share) }
                refresh()
            }
        }
    }

    fun refresh() {
        val current = type ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (current) {
                is ShareType.Smb -> {
                    val result = manager.sharing.getSmbShareWithResult(current.share.id)
                    if (result is ApiResult.Success) {
                        _uiState.update { it.copy(smbShare = result.data) }
                    }
                    val acl = manager.sharing.getSmbShareAclWithResult(current.share.name)
                    if (acl is ApiResult.Success) {
                        _uiState.update { it.copy(smbAcl = acl.data) }
                    }
                }
                is ShareType.Nfs -> {
                    val result = manager.sharing.getNfsShareWithResult(current.share.id)
                    if (result is ApiResult.Success) {
                        _uiState.update { it.copy(nfsShare = result.data) }
                    }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun delete() {
        val current = type ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            val result = when (current) {
                is ShareType.Smb -> manager.sharing.deleteSmbShareWithResult(current.share.id)
                is ShareType.Nfs -> manager.sharing.deleteNfsShareWithResult(current.share.id)
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isDeleting = false, deleted = true) }
                is ApiResult.Error -> {
                    ToastManager.showError(result.message)
                    _uiState.update { it.copy(isDeleting = false, error = result.message) }
                }
                else -> _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(manager: TrueNASApiManager): ViewModelProvider.Factory = viewModelFactory {
            initializer { ShareInfoViewModel(manager) }
        }
    }
}
