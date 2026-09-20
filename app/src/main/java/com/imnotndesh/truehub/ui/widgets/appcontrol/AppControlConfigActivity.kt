package com.imnotndesh.truehub.ui.widgets.appcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.IconCache
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.AppControlEntry
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.AppIcon
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import com.imnotndesh.truehub.ui.widgets.appcontrol.AppControlWidgetUpdater
import kotlinx.coroutines.launch

class AppControlConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrueHubAppTheme {
                ConfigSheet(onDismiss = { finish() })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ConfigSheet(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var isLoading by remember { mutableStateOf(true) }
        var isSaving by remember { mutableStateOf(false) }
        var loadError by remember { mutableStateOf<String?>(null) }
        var installedApps by remember { mutableStateOf<List<Apps.AppQueryResponse>>(emptyList()) }
        var selectedAppName by remember { mutableStateOf<String?>(null) }
        var existing by remember { mutableStateOf<AppControlEntry?>(null) }

        LaunchedEffect(Unit) {
            existing = WidgetDataStore.getAppControlEntry(context)
            selectedAppName = existing?.appName

            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(context) ?: run {
                isLoading = false; loadError = "No server configured"; return@LaunchedEffect
            }
            val server = MultiAccountPrefs.getServer(context, serverId) ?: run {
                isLoading = false; loadError = "Server not found"; return@LaunchedEffect
            }
            val account = MultiAccountPrefs.getAccount(context, accountId) ?: run {
                isLoading = false; loadError = "Account not found"; return@LaunchedEffect
            }

            val client = TrueNASClient(Config.ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure))
            if (!client.connect()) {
                isLoading = false; loadError = "Could not connect to server"; return@LaunchedEffect
            }

            val m = TrueNASApiManager(client, context)
            val token = MultiAccountPrefs.getTokenForLastUsed(context)
            var authed = token != null && (m.auth.loginWithTokenAndResult(token) is ApiResult.Success)

            if (!authed) {
                val (credentialPrimary, credentialSecondary) = MultiAccountPrefs.getAccountCredentials(
                    context, accountId, account.loginMethod
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
                isLoading = false; loadError = "Authentication failed"; return@LaunchedEffect
            }

            when (val result = m.apps.getInstalledAppsWithResult()) {
                is ApiResult.Success -> installedApps = result.data
                is ApiResult.Error -> loadError = result.message
                else -> {}
            }
            isLoading = false
        }

        ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(text = "App Control", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Choose an installed app to monitor and control from your home screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                when {
                    isLoading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator() }
                    loadError != null -> Text(loadError ?: "Something went wrong", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 24.dp))
                    installedApps.isEmpty() -> Text("No installed apps found.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(installedApps, key = { it.name }) { app ->
                                val isSelected = selectedAppName == app.name

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAppName = app.name }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIcon(iconUrl = app.metadata?.icon, title = app.metadata?.title ?: app.name, size = 40)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.metadata?.title ?: app.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = app.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedAppName = app.name }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            val chosenApp = installedApps.find { it.name == selectedAppName }
                            if (chosenApp != null) {
                                val previouslyCached = if (existing?.appName == chosenApp.name) existing?.cachedIconPath else null
                                val cachedIconPath = previouslyCached
                                    ?: IconCache.cacheIcon(context, chosenApp.name, chosenApp.metadata?.icon)

                                if (existing != null && existing?.appName != chosenApp.name) {
                                    IconCache.deleteCachedIcon(existing?.cachedIconPath)
                                }

                                val entry = AppControlEntry(
                                    appName = chosenApp.name,
                                    title = chosenApp.metadata?.title ?: chosenApp.name,
                                    iconUrl = chosenApp.metadata?.icon,
                                    cachedIconPath = cachedIconPath,
                                    state = chosenApp.state
                                )

                                WidgetDataStore.saveAppControlEntry(context, entry)
                                AppControlWidgetUpdater.update(context)
                            }
                            isSaving = false
                            onDismiss()
                        }
                    },
                    enabled = !isSaving && !isLoading && selectedAppName != null,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (selectedAppName != null) "Select App (1)" else "Select App")
                }
            }
        }
    }
}
