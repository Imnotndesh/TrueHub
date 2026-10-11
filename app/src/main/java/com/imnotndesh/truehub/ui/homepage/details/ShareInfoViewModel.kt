package com.imnotndesh.truehub.ui.homepage.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.utils.AppCache
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

@HiltViewModel
class ShareInfoViewModel @Inject constructor(
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
                    when (val result = manager.sharing.getSmbShareWithResult(current.share.id)) {
                        is ApiResult.Success -> {
                            _uiState.update { it.copy(smbShare = result.data, smbAcl = null, error = null) }
                            val refreshedName = result.data.name.orEmpty()
                            if (refreshedName.isNotBlank()) {
                                when (val acl = manager.sharing.getSmbShareAclWithResult(refreshedName)) {
                                    is ApiResult.Success -> _uiState.update { it.copy(smbAcl = acl.data) }
                                    is ApiResult.Error -> _uiState.update { it.copy(smbAcl = null, error = acl.message) }
                                    ApiResult.Loading -> Unit
                                }
                            }
                        }
                        is ApiResult.Error -> _uiState.update { it.copy(smbAcl = null, error = result.message) }
                        ApiResult.Loading -> Unit
                    }
                }
                is ShareType.Nfs -> {
                    when (val result = manager.sharing.getNfsShareWithResult(current.share.id)) {
                        is ApiResult.Success -> _uiState.update { it.copy(nfsShare = result.data, error = null) }
                        is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                        ApiResult.Loading -> Unit
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
                is ApiResult.Success -> {
                    if (result.data) {
                        when (current) {
                            is ShareType.Smb -> AppCache.removeSmbShare(current.share.id)
                            is ShareType.Nfs -> AppCache.removeNfsShare(current.share.id)
                        }
                        _uiState.update { it.copy(isDeleting = false, deleted = true) }
                    } else {
                        val message = "The server did not confirm deletion"
                        ToastManager.showError(message)
                        _uiState.update { it.copy(isDeleting = false, error = message) }
                    }
                }
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
}
