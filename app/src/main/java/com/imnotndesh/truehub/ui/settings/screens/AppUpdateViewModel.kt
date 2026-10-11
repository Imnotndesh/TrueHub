package com.imnotndesh.truehub.ui.settings.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.imnotndesh.truehub.BuildConfig
import com.imnotndesh.truehub.data.helpers.ApkInstaller
import com.imnotndesh.truehub.data.helpers.UpdatePrefs
import com.imnotndesh.truehub.data.helpers.UpdateRepository
import com.imnotndesh.truehub.data.models.UpdateInfo
import com.imnotndesh.truehub.data.workers.AppUpdateDownloadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class UpdatePhase { IDLE, DOWNLOADING, READY, ERROR }

data class AppUpdateUiState(
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val currentVersionCode: Int = BuildConfig.VERSION_CODE,
    val isPlayStoreBuild: Boolean = BuildConfig.IS_PLAYSTORE_BUILD,
    val latest: UpdateInfo? = null,
    val isChecking: Boolean = false,
    val autoCheckEnabled: Boolean = true,
    val wifiOnly: Boolean = false,
    val dismissedVersion: String? = null,
    val error: String? = null,
    val phase: UpdatePhase = UpdatePhase.IDLE,
    val downloadProgress: Int = 0,
    val readyApkPath: String? = null
) {
    val hasUpdate: Boolean
        get() = latest != null && latest.versionName != dismissedVersion
}

sealed interface AppUpdateEvent {
    data object Check : AppUpdateEvent
    data object DismissError : AppUpdateEvent
    data object StartDownload : AppUpdateEvent
    data object Install : AppUpdateEvent
    data class SkipVersion(val versionName: String) : AppUpdateEvent
    data class SetAutoCheck(val enabled: Boolean) : AppUpdateEvent
    data class SetWifiOnly(val enabled: Boolean) : AppUpdateEvent
}

class AppUpdateViewModel(
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    private val playStoreBuild = BuildConfig.IS_PLAYSTORE_BUILD

    init {
        if (!playStoreBuild) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    autoCheckEnabled = UpdatePrefs.isAutoCheckEnabled(application),
                    wifiOnly = UpdatePrefs.isWifiOnly(application),
                    dismissedVersion = UpdatePrefs.getDismissedVersion(application),
                    latest = UpdatePrefs.getLatest(application)
                )
                restoreReadyState()
            }
            viewModelScope.launch {
                UpdatePrefs.latestFlow(application).collect { info ->
                    _uiState.value = _uiState.value.copy(latest = info)
                }
            }
            viewModelScope.launch {
                WorkManager.getInstance(application)
                    .getWorkInfosForUniqueWorkFlow(AppUpdateDownloadWorker.WORK_NAME)
                    .collect(::applyWorkInfo)
            }
        }
    }

    fun handleEvent(event: AppUpdateEvent) {
        when (event) {
            AppUpdateEvent.Check -> checkForUpdates()
            AppUpdateEvent.DismissError -> _uiState.value = _uiState.value.copy(error = null)
            AppUpdateEvent.StartDownload -> startDownload()
            AppUpdateEvent.Install -> installReady()
            is AppUpdateEvent.SkipVersion -> skip(event.versionName)
            is AppUpdateEvent.SetAutoCheck -> setAutoCheck(event.enabled)
            is AppUpdateEvent.SetWifiOnly -> setWifiOnly(event.enabled)
        }
    }

    private suspend fun restoreReadyState() {
        val version = UpdatePrefs.getReadyVersion(application) ?: return
        val path = UpdatePrefs.getReadyApkPath(application) ?: return
        val file = File(path)
        when {
            version == BuildConfig.VERSION_NAME -> {
                file.delete()
                UpdatePrefs.clearReady(application)
            }
            file.exists() -> _uiState.value = _uiState.value.copy(
                phase = UpdatePhase.READY,
                readyApkPath = path
            )
            else -> UpdatePrefs.clearReady(application)
        }
    }

    private suspend fun applyWorkInfo(infos: List<WorkInfo>) {
        val info = infos.firstOrNull() ?: return
        when (info.state) {
            WorkInfo.State.RUNNING -> _uiState.value = _uiState.value.copy(
                phase = UpdatePhase.DOWNLOADING,
                downloadProgress = info.progress.getInt(AppUpdateDownloadWorker.KEY_PROGRESS, 0),
                error = null
            )
            WorkInfo.State.SUCCEEDED -> {
                val version = UpdatePrefs.getReadyVersion(application)
                val path = UpdatePrefs.getReadyApkPath(application)
                if (version != null && version != BuildConfig.VERSION_NAME && path != null && File(path).exists()) {
                    _uiState.value = _uiState.value.copy(
                        phase = UpdatePhase.READY,
                        downloadProgress = 100,
                        readyApkPath = path
                    )
                }
            }
            WorkInfo.State.FAILED, WorkInfo.State.CANCELLED ->
                _uiState.value = _uiState.value.copy(
                    phase = UpdatePhase.ERROR,
                    error = "Update download failed. Please try again."
                )
            else -> Unit
        }
    }

    private fun checkForUpdates() {
        if (playStoreBuild) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, error = null)
            val result = UpdateRepository.check(application)
            _uiState.value = _uiState.value.copy(
                isChecking = false,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    private fun startDownload() {
        if (playStoreBuild) return
        val info = _uiState.value.latest ?: return
        if (info.asset == null) return
        viewModelScope.launch {
            if (!ApkInstaller.canInstall(application)) {
                _uiState.value = _uiState.value.copy(
                    error = "Allow TrueHub to install unknown apps, then try again."
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                phase = UpdatePhase.DOWNLOADING,
                downloadProgress = 0,
                error = null
            )
            AppUpdateDownloadWorker.enqueue(application, info)
        }
    }

    private fun installReady() {
        if (playStoreBuild) return
        val path = _uiState.value.readyApkPath ?: return
        viewModelScope.launch {
            if (!ApkInstaller.canInstall(application)) {
                _uiState.value = _uiState.value.copy(
                    error = "Allow TrueHub to install unknown apps, then try again."
                )
                return@launch
            }
            val file = File(path)
            if (!file.exists()) {
                UpdatePrefs.clearReady(application)
                _uiState.value = _uiState.value.copy(
                    phase = UpdatePhase.IDLE,
                    readyApkPath = null,
                    error = "The downloaded update is missing. Download it again."
                )
                return@launch
            }
            val result = ApkInstaller.install(application, file)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    phase = UpdatePhase.ERROR,
                    error = result.exceptionOrNull()?.message ?: "Install failed"
                )
            }
        }
    }

    private fun skip(versionName: String) {
        viewModelScope.launch {
            UpdatePrefs.setDismissedVersion(application, versionName)
            _uiState.value = _uiState.value.copy(dismissedVersion = versionName)
        }
    }

    private fun setAutoCheck(enabled: Boolean) {
        viewModelScope.launch {
            UpdatePrefs.setAutoCheckEnabled(application, enabled)
            _uiState.value = _uiState.value.copy(autoCheckEnabled = enabled)
        }
    }

    private fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            UpdatePrefs.setWifiOnly(application, enabled)
            _uiState.value = _uiState.value.copy(wifiOnly = enabled)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppUpdateViewModel::class.java)) {
                return AppUpdateViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
