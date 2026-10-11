package com.imnotndesh.truehub.ui.homepage.instancesettings.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val users: List<System.UserCreateUpdateResult> = emptyList(),
    val searchQuery: String = "",
    val filterLocked: Boolean? = null,
    val filterSmb: Boolean? = null,
    val filterBuiltin: Boolean? = null,
    val filterRole: String? = null,
    val error: String? = null
) {
    val filteredUsers: List<System.UserCreateUpdateResult>
        get() {
            var result = users


            if (searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                result = result.filter { user ->
                    user.username.lowercase().contains(query) ||
                            user.full_name.lowercase().contains(query) ||
                            (user.email?.lowercase()?.contains(query) == true)
                }
            }


            if (filterLocked != null) {
                result = result.filter { it.locked == filterLocked }
            }


            if (filterSmb != null) {
                result = result.filter { it.smb == filterSmb }
            }

            if (filterBuiltin != null) {
                result = result.filter { it.builtin == filterBuiltin }
            }


            if (filterRole != null) {
                result = result.filter { user ->
                    user.roles.any { role ->
                        role.equals(filterRole, ignoreCase = true)
                    }
                }
            }

            return result
        }
}

data class UserDetailUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isChangingPassword: Boolean = false,
    val passwordChangeResult: Boolean? = null,
    val isRenewing2fa: Boolean = false,
    val renew2faResult: System.UserRenew2faSecretResult? = null,
    val isUnsetting2fa: Boolean = false,
    val unset2faResult: Boolean? = null,
    val isDeleting: Boolean = false,
    val user: System.UserCreateUpdateResult? = null,
    val updateResult: Boolean? = null,
    val deleteResult: Boolean? = null,
    val error: String? = null
)

data class UserCreateUiState(
    val isLoading: Boolean = false,
    val setupAdminResult: Boolean? = null,
    val isCreating: Boolean = false,
    val nextUid: Int? = null,
    val shellChoices: Map<String, String> = emptyMap(),
    val createResult: System.UserCreateUpdateResult? = null,
    val error: String? = null
)

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _listState = MutableStateFlow(UserListUiState())
    val listState: StateFlow<UserListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(UserDetailUiState())
    val detailState: StateFlow<UserDetailUiState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(UserCreateUiState())
    val createState: StateFlow<UserCreateUiState> = _createState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, error = null)
            when (val result = manager.system.queryUsersWithResult()) {
                is ApiResult.Success -> _listState.value = _listState.value.copy(
                    isLoading = false, users = result.data
                )
                is ApiResult.Error -> _listState.value = _listState.value.copy(
                    isLoading = false, error = result.message
                )
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }
    fun setupLocalAdministrator(username: String, password: String) {
        viewModelScope.launch {
            _createState.value = _createState.value.copy(isCreating = true, error = null)
            when (val result = manager.system.setupLocalAdministratorWithResult(
                System.UserSetupLocalAdministratorArgs(
                    username = username,
                    password = password
                )
            )) {
                is ApiResult.Success -> _createState.value = _createState.value.copy(
                    isCreating = false, setupAdminResult = true
                )
                is ApiResult.Error -> _createState.value = _createState.value.copy(
                    isCreating = false, error = result.message
                )
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearSetupAdminResult() {
        _createState.value = _createState.value.copy(setupAdminResult = null)
    }

    fun refreshUsers() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isRefreshing = true, error = null)
            when (val result = manager.system.queryUsersWithResult()) {
                is ApiResult.Success -> _listState.value = _listState.value.copy(
                    isRefreshing = false, users = result.data
                )
                is ApiResult.Error -> _listState.value = _listState.value.copy(
                    isRefreshing = false, error = result.message
                )
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun loadUserDetail(userId: Int) {
        viewModelScope.launch {
            _detailState.value = UserDetailUiState(isLoading = true)
            when (val result = manager.system.getUserInstanceWithResult(userId)) {
                is ApiResult.Success -> _detailState.value = UserDetailUiState(
                    user = result.data
                )
                is ApiResult.Error -> _detailState.value = UserDetailUiState(
                    error = result.message
                )
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }
    fun changeUserPassword(username: String, newPassword: String) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isChangingPassword = true, error = null)
            when (val result = manager.system.setUserPasswordWithResult(
                System.UserSetPasswordArgs(username = username, new_password = newPassword)
            )) {
                is ApiResult.Success -> _detailState.value = _detailState.value.copy(
                    isChangingPassword = false, passwordChangeResult = true
                )
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isChangingPassword = false, error = result.message
                )
                is ApiResult.Loading -> {}
            }
        }
    }

    fun renew2faSecret(username: String) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isRenewing2fa = true, error = null)
            when (val result = manager.system.renewUser2faSecretWithResult(
                System.UserRenew2faSecretArgs(
                    username = username,
                    twofactor_options = System.UserTwofactorOptions()
                )
            )) {
                is ApiResult.Success -> _detailState.value = _detailState.value.copy(
                    isRenewing2fa = false, renew2faResult = result.data
                )
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isRenewing2fa = false, error = result.message
                )
                is ApiResult.Loading -> {}
            }
        }
    }

    fun unset2faSecret(username: String) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isUnsetting2fa = true, error = null)
            when (val result = manager.system.unsetUser2faSecretWithResult(username)) {
                is ApiResult.Success -> _detailState.value = _detailState.value.copy(
                    isUnsetting2fa = false, unset2faResult = true
                )
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isUnsetting2fa = false, error = result.message
                )
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearPasswordChangeResult() {
        _detailState.value = _detailState.value.copy(passwordChangeResult = null)
    }

    fun clearRenew2faResult() {
        _detailState.value = _detailState.value.copy(renew2faResult = null)
    }

    fun clearUnset2faResult() {
        _detailState.value = _detailState.value.copy(unset2faResult = null)
    }

    fun updateUser(userId: Int, update: System.UserUpdate) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isUpdating = true, error = null)
            when (val result = manager.system.updateUserWithResult(userId, update)) {
                is ApiResult.Success -> _detailState.value = _detailState.value.copy(
                    isUpdating = false, updateResult = true, user = result.data
                )
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isUpdating = false, error = result.message
                )
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isDeleting = true, error = null)
            when (val result = manager.system.deleteUserWithResult(userId)) {
                is ApiResult.Success -> _detailState.value = _detailState.value.copy(
                    isDeleting = false, deleteResult = true
                )
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isDeleting = false, error = result.message
                )
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun loadCreateFormData() {
        viewModelScope.launch {
            _createState.value = _createState.value.copy(isLoading = true)
            val uidResult = manager.system.getNextUidWithResult()
            val shellResult = manager.system.getShellChoicesWithResult()

            val nextUid = when (uidResult) {
                is ApiResult.Success -> uidResult.data
                else -> null
            }
            val shellChoices = when (shellResult) {
                is ApiResult.Success -> shellResult.data
                else -> emptyMap()
            }
            _createState.value = _createState.value.copy(
                isLoading = false, nextUid = nextUid, shellChoices = shellChoices
            )
        }
    }

    fun createUser(create: System.UserCreate) {
        viewModelScope.launch {
            _createState.value = _createState.value.copy(isCreating = true, error = null)
            when (val result = manager.system.createUserWithResult(create)) {
                is ApiResult.Success -> _createState.value = _createState.value.copy(
                    isCreating = false, createResult = result.data
                )
                is ApiResult.Error -> _createState.value = _createState.value.copy(
                    isCreating = false, error = result.message
                )
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearListError() {
        _listState.value = _listState.value.copy(error = null)
    }

    fun clearDetailError() {
        _detailState.value = _detailState.value.copy(error = null)
    }

    fun clearCreateError() {
        _createState.value = _createState.value.copy(error = null)
    }

    fun clearUpdateResult() {
        _detailState.value = _detailState.value.copy(updateResult = null)
    }

    fun clearCreateResult() {
        _createState.value = _createState.value.copy(createResult = null)
    }
    fun setSearchQuery(query: String) {
        _listState.value = _listState.value.copy(searchQuery = query)
    }

    fun setFilterLocked(value: Boolean?) {
        _listState.value = _listState.value.copy(filterLocked = value)
    }

    fun setFilterSmb(value: Boolean?) {
        _listState.value = _listState.value.copy(filterSmb = value)
    }

    fun setFilterBuiltin(value: Boolean?) {
        _listState.value = _listState.value.copy(filterBuiltin = value)
    }

    fun setFilterRole(value: String?) {
        _listState.value = _listState.value.copy(filterRole = value)
    }
}
