package com.imnotndesh.truehub.ui.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.data.models.AccountProfile
import com.imnotndesh.truehub.data.models.SavedServer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountSwitcherUiState(
    val profiles: List<AccountProfile> = emptyList(),
    val savedServers: List<SavedServer> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountSwitcherViewModel @Inject constructor(
    @ApplicationContext private val application: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountSwitcherUiState())
    val uiState: StateFlow<AccountSwitcherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadProfiles()
        }
    }

    fun reload() {
        viewModelScope.launch {
            loadProfiles()
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            MultiAccountPrefs.deleteAccount(application, accountId)
            PersonalizationManager.deleteForUser(application, accountId)
            loadProfiles()
        }
    }

    fun deleteAllAccounts() {
        viewModelScope.launch {
            val accounts = MultiAccountPrefs.getAccounts(application)
            accounts.forEach { account ->
                MultiAccountPrefs.deleteAccount(application, account.id)
                PersonalizationManager.deleteForUser(application, account.id)
            }
            loadProfiles()
        }
    }

    fun saveNewServer(serverUrl: String, insecure: Boolean) {
        viewModelScope.launch {
            MultiAccountPrefs.saveServer(
                application,
                SavedServer(
                    serverUrl = serverUrl,
                    insecure = insecure
                )
            )
            loadProfiles()
        }
    }

    private suspend fun loadProfiles() {
        val servers = MultiAccountPrefs.getServers(application)
        val accounts = MultiAccountPrefs.getAccounts(application)
        val profiles = accounts.mapNotNull { account ->
            servers.find { it.id == account.serverId }?.let { server ->
                AccountProfile(server, account)
            }
        }.sortedByDescending { it.account.lastUsed }

        _uiState.update {
            it.copy(
                profiles = profiles,
                savedServers = servers,
                isLoading = false
            )
        }
    }
}
