package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.GlobalJobTracker
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppDetailsViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _similarApps = MutableStateFlow<List<Apps.AppSimilarResponse>>(emptyList())
    val similarApps: StateFlow<List<Apps.AppSimilarResponse>> = _similarApps

    private val _isLoadingSimilar = MutableStateFlow(false)
    val isLoadingSimilar: StateFlow<Boolean> = _isLoadingSimilar

    private val _appState = MutableStateFlow<String?>(null)
    val appState: StateFlow<String?> = _appState.asStateFlow()

    private val _deletionJobId = MutableStateFlow<Int?>(null)
    val deletionJobId: StateFlow<Int?> = _deletionJobId

    fun loadSimilarApps(appName: String, train: String) {
        viewModelScope.launch {
            _isLoadingSimilar.value = true
            val result = manager.apps.getSimilarApps(appName, train)
            if (result is ApiResult.Success) {
                _similarApps.value = result.data
            }
            _isLoadingSimilar.value = false
        }
    }

    fun startApp(appName: String) = setAppRunning(appName, start = true)

    fun stopApp(appName: String) = setAppRunning(appName, start = false)

    private fun setAppRunning(appName: String, start: Boolean) {
        viewModelScope.launch {
            _appState.value = if (start) "STARTING" else "STOPPING"
            val result = if (start) {
                manager.apps.startAppWithResult(appName)
            } else {
                manager.apps.stopAppWithResult(appName)
            }
            if (result is ApiResult.Error) {
                ToastManager.showError(result.message)
                _appState.value = null
                return@launch
            }
            val target = if (start) "running" else "stopped"
            repeat(STATE_POLL_ATTEMPTS) {
                delay(STATE_POLL_INTERVAL_MS)
                when (val instance = manager.apps.getAppInstanceWithResult(appName)) {
                    is ApiResult.Success -> {
                        _appState.value = instance.data.state
                        if (instance.data.state.equals(target, ignoreCase = true)) return@launch
                    }
                    else -> Unit
                }
            }
        }
    }

    fun deleteApp(
        context: Context,
        appName: String,
        options: Apps.DeleteAppOptions = Apps.DeleteAppOptions()
    ) {
        viewModelScope.launch {
            val result = manager.apps.removeAppWithResult(appName, options)
            if (result is ApiResult.Success) {
                val jobId = result.data
                _deletionJobId.value = jobId

                GlobalJobTracker.startTracking(
                    context = context,
                    manager = manager,
                    jobId = jobId,
                    appName = appName,
                    showNotif = true
                )
            }
        }
    }

    companion object {
        private const val STATE_POLL_ATTEMPTS = 30
        private const val STATE_POLL_INTERVAL_MS = 2000L

        /**
         * Returns a Factory that injects the TrueNASApiManager.
         */
        fun provideFactory(manager: TrueNASApiManager): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppDetailsViewModel(manager)
            }
        }
    }
}
