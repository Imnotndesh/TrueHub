package com.imnotndesh.truehub.ui.widgets.quicklaunch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.imnotndesh.truehub.data.helpers.SessionProvider
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.QuickLaunchApp
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.AppIcon
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.launch

private const val MAX_QUICK_LAUNCH_APPS = 6

class QuickLaunchConfigActivity : ComponentActivity() {

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
        var eligibleApps by remember { mutableStateOf<List<Apps.AppQueryResponse>>(emptyList()) }
        var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
        var existing by remember { mutableStateOf<List<QuickLaunchApp>>(emptyList()) }

        LaunchedEffect(Unit) {
            existing = WidgetDataStore.getQuickLaunchApps(context)
            selected = existing.map { it.appName }.toSet()

            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(context) ?: run {
                isLoading = false; loadError = "No server configured"; return@LaunchedEffect
            }
            val server = MultiAccountPrefs.getServer(context, serverId) ?: run {
                isLoading = false; loadError = "Server not found"; return@LaunchedEffect
            }
            val account = MultiAccountPrefs.getAccount(context, accountId) ?: run {
                isLoading = false; loadError = "Account not found"; return@LaunchedEffect
            }

            val session = SessionProvider.open(context, server, account)
            if (session !is SessionProvider.OpenResult.Ready) {
                isLoading = false; loadError = "Authentication failed"; return@LaunchedEffect
            }
            val m = session.manager

            when (val result = m.apps.getInstalledAppsWithResult()) {
                is ApiResult.Success -> eligibleApps = result.data.filter { !it.portals.isNullOrEmpty() }
                is ApiResult.Error -> loadError = result.message
                else -> {}
            }
            isLoading = false
        }

        ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(text = "Quick Launch Apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Choose up to $MAX_QUICK_LAUNCH_APPS apps with a web UI to pin to the widget.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                when {
                    isLoading -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator() }
                    loadError != null -> Text(loadError ?: "Something went wrong", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 24.dp))
                    eligibleApps.isEmpty() -> Text("None of your installed apps expose a web UI yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(eligibleApps, key = { it.name }) { app ->
                                val isChecked = selected.contains(app.name)
                                val atLimit = selected.size >= MAX_QUICK_LAUNCH_APPS && !isChecked

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AppIcon(iconUrl = app.metadata?.icon, title = app.metadata?.title ?: app.name, size = 40)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = app.metadata?.title ?: app.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                        Text(text = app.portals?.keys?.firstOrNull() ?: "Web UI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Checkbox(
                                        checked = isChecked,
                                        enabled = !atLimit,
                                        onCheckedChange = { checked -> selected = if (checked) selected + app.name else selected - app.name }
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
                            val chosenApps = eligibleApps.filter { selected.contains(it.name) }

                            val updatedEntries = chosenApps.map { app ->
                                val webUiUrl = app.portals?.values?.firstOrNull() ?: ""
                                val previouslyCached = existing.find { it.appName == app.name }?.cachedIconPath
                                val cachedIconPath = previouslyCached
                                    ?: IconCache.cacheIcon(context, app.name, app.metadata?.icon)

                                QuickLaunchApp(
                                    appName = app.name,
                                    title = app.metadata?.title ?: app.name,
                                    iconUrl = app.metadata?.icon,
                                    webUiUrl = webUiUrl,
                                    cachedIconPath = cachedIconPath
                                )
                            }

                            existing.filter { it.appName !in selected }
                                .forEach { IconCache.deleteCachedIcon(it.cachedIconPath) }

                            WidgetDataStore.saveQuickLaunchApps(context, updatedEntries)
                            QuickLaunchWidgetUpdater.update(context)

                            isSaving = false
                            onDismiss()
                        }
                    },
                    enabled = !isSaving && !isLoading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save (${selected.size}/$MAX_QUICK_LAUNCH_APPS)")
                }
            }
        }
    }
}