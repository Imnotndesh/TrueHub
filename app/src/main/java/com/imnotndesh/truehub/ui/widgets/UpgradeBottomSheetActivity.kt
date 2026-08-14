package com.imnotndesh.truehub.ui.widgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.GlobalJobTracker
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.AppUpdatableStates
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.ui.services.apps.details.upgrade.UpgradeSummaryScreen
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.launch

class UpgradeBottomSheetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra("EXTRA_APP_NAME")
        if (appName == null) {
            finish()
            return
        }

        setContent {
            TrueHubAppTheme {
                BottomSheetContent(
                    appName = appName,
                    onDismiss = { finish() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomSheetContent(appName: String, onDismiss: () -> Unit) {
        val scope = rememberCoroutineScope()
        val appContext = this@UpgradeBottomSheetActivity.applicationContext

        var manager by remember { mutableStateOf<TrueNASApiManager?>(null) }
        var summary by remember { mutableStateOf<Apps.AppUpgradeSummaryResult?>(null) }
        var currentVersion by remember { mutableStateOf("") }
        var appState by remember { mutableStateOf("") }
        var trackingJobId by remember { mutableStateOf<Int?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var loadFailed by remember { mutableStateOf(false) }

        val activeJobs by JobRepository.activeJobs.collectAsState()
        val trackedJob = trackingJobId?.let { id -> activeJobs[id] }

        LaunchedEffect(trackedJob?.state) {
            if (trackedJob?.state == "SUCCESS") {
                WidgetDataStore.removeUpgradableApp(appContext, appName)
                AppsUpdateWidgetUpdater.update(appContext)
            }
        }

        LaunchedEffect(Unit) {
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(appContext) ?: run {
                isLoading = false; loadFailed = true; return@LaunchedEffect
            }
            val server = MultiAccountPrefs.getServer(appContext, serverId) ?: run {
                isLoading = false; loadFailed = true; return@LaunchedEffect
            }
            val account = MultiAccountPrefs.getAccount(appContext, accountId) ?: run {
                isLoading = false; loadFailed = true; return@LaunchedEffect
            }

            val client = TrueNASClient(
                Config.ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure)
            )
            if (!client.connect()) {
                isLoading = false; loadFailed = true; return@LaunchedEffect
            }

            val m = TrueNASApiManager(client, appContext)

            val token = MultiAccountPrefs.getTokenForLastUsed(appContext)
            var authed = token != null &&
                    (m.auth.loginWithTokenAndResult(token) is ApiResult.Success)

            if (!authed) {
                val (credentialPrimary, credentialSecondary) = MultiAccountPrefs.getAccountCredentials(
                    appContext, accountId, account.loginMethod
                )
                val loginResult = when (account.loginMethod) {
                    LoginMethod.API_KEY -> credentialPrimary?.let { m.auth.loginWithApiKeyWithResult(it) }
                    LoginMethod.PASSWORD, LoginMethod.TOTP -> if (credentialPrimary != null && credentialSecondary != null) {
                        m.auth.loginUserWithResult(AuthService.DefaultAuth(credentialPrimary, credentialSecondary))
                    } else null
                }
                authed = loginResult is ApiResult.Success && loginResult.data == true
            }

            if (!authed) {
                isLoading = false; loadFailed = true; return@LaunchedEffect
            }

            manager = m

            val appsResult = m.apps.getInstalledAppsWithResult()
            if (appsResult is ApiResult.Success) {
                appsResult.data.find { it.name == appName }?.let { app ->
                    currentVersion = app.version ?: ""
                    appState = app.state.orEmpty()
                }
            }

            val summaryResult = m.apps.getUpgradeSummaryWithResult(appName)
            if (summaryResult is ApiResult.Success) {
                summary = summaryResult.data
            } else {
                loadFailed = true
            }
            isLoading = false
        }

        when {
            manager != null && summary != null -> {
                ModalBottomSheet(
                    onDismissRequest = onDismiss,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    UpgradeSummaryScreen(
                        appName = appName,
                        currentVersion = currentVersion,
                        currentHumanVersion = null,
                        summary = summary!!,
                        manager = manager!!,
                        canUpgrade = appState.uppercase() in AppUpdatableStates,
                        appState = appState,
                        onConfirmUpgrade = { selectedVersion, backup ->
                            scope.launch {
                                when (val result = manager!!.apps.upgradeAppWithResult(
                                    appName = appName,
                                    version = selectedVersion,
                                    backup = backup
                                )) {
                                    is ApiResult.Success -> {
                                        trackingJobId = result.data
                                        GlobalJobTracker.startTracking(
                                            context = appContext,
                                            manager = manager!!,
                                            jobId = result.data,
                                            appName = appName,
                                            showNotif = true
                                        )
                                    }
                                    is ApiResult.Error -> onDismiss()
                                    else -> {}
                                }
                            }
                        },
                        onNavigateBack = onDismiss
                    )
                }
            }
            isLoading -> {
                ModalBottomSheet(
                    onDismissRequest = onDismiss,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            else -> {
                LaunchedEffect(loadFailed) { onDismiss() }
            }
        }
    }
}