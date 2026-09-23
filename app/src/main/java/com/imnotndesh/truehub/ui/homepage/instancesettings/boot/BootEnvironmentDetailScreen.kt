package com.imnotndesh.truehub.ui.homepage.instancesettings.boot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.InfoRow
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection

/**
 * Detail view of a single boot environment with actions: activate, keep, clone, destroy.
 */
@Composable
fun BootEnvironmentDetailScreen(
    manager: TrueNASApiManager,
    environmentId: String,
    onNavigateBack: () -> Unit = {}
) {
    val vm: BootEnvironmentsViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCloneDialog by remember { mutableStateOf(false) }
    var showDestroyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadEnvironments() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearActionMessage()
        }
    }

    val environment = uiState.environments.firstOrNull { it.id == environmentId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            UnifiedScreenHeader(
                title = "Boot Environment",
                subtitle = environmentId,
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && environment == null ->
                LoadingScreen("Loading environment...")
            environment != null ->
                PullToRefreshContent(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                    item {
                        val isActive = environment.active == true
                        androidx.compose.material3.Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = if (isActive)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = environment.id ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isActive) "Currently running" else "Inactive",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        ExpressiveSection(title = "Details", icon = Icons.Default.Storage) {
                            androidx.compose.material3.Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    InfoRow("ID", environment.id ?: "—")
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Dataset", environment.dataset ?: "—")
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Created", environment.created ?: "—")
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Used", environment.used ?: "—")
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Used Bytes", formatBytes(environment.usedBytes))
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Activated for next boot", booleanLabel(environment.activated))
                                }
                            }
                        }
                    }

                    item {
                        ExpressiveSection(title = "State", icon = Icons.Default.PowerSettingsNew) {
                            androidx.compose.material3.Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    InfoRow("Active", booleanLabel(environment.active))
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Can Activate", booleanLabel(environment.canActivate))
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Keep (protect from deletion)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Switch(
                                            checked = environment.keep == true,
                                            enabled = !uiState.isActing,
                                            onCheckedChange = { value -> vm.keep(environmentId, value) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ExpressiveSection(title = "Actions", icon = Icons.Default.Refresh) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !uiState.isActing && environment.active != true && environment.canActivate == true,
                                    onClick = { vm.activate(environmentId) }
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Activate", fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !uiState.isActing,
                                    onClick = { showCloneDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Clone", fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !uiState.isActing && environment.active != true,
                                    onClick = { showDestroyDialog = true }
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Destroy", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                    }
            else ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Environment \"$environmentId\" not found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { vm.refresh() }) {
                        Text("Retry")
                    }
                }
        }
    }

    if (showCloneDialog) {
        CloneDetailDialog(
            sourceId = environmentId,
            onDismiss = { showCloneDialog = false },
            onConfirm = { target ->
                vm.clone(environmentId, target)
                showCloneDialog = false
            }
        )
    }

    if (showDestroyDialog) {
        AlertDialog(
            onDismissRequest = { showDestroyDialog = false },
            title = { Text("Destroy environment?") },
            text = { Text("Permanently destroy boot environment \"$environmentId\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.destroy(environmentId)
                    showDestroyDialog = false
                }) { Text("Destroy") }
            },
            dismissButton = {
                TextButton(onClick = { showDestroyDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CloneDetailDialog(
    sourceId: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var target by remember(sourceId) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clone Environment") },
        text = {
            Column {
                Text(
                    "Source: $sourceId. Choose a name for the new boot environment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("New environment name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = target.isNotBlank(), onClick = { onConfirm(target.trim()) }) {
                Text("Clone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun booleanLabel(value: Boolean?): String =
    when (value) {
        true -> "Yes"
        false -> "No"
        null -> "—"
    }
