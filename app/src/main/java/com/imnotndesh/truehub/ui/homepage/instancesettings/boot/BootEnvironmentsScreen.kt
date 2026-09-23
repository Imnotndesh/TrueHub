package com.imnotndesh.truehub.ui.homepage.instancesettings.boot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ExpressiveFAB
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection

/**
 * Lists all boot environments on the system. Each entry can be activated, kept (protected
 * from auto-deletion), cloned, or destroyed. A FAB allows cloning (creating a new BE from
 * an existing source).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootEnvironmentsScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val vm: BootEnvironmentsViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var cloneSourceId by remember { mutableStateOf<String?>(null) }
    var destroyTargetId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.loadEnvironments() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearActionMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            UnifiedScreenHeader(
                title = "Boot Environments",
                subtitle = "${uiState.environments.size} environments",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        floatingActionButton = {
            ExpressiveFAB(
                onClick = { cloneSourceId = uiState.environments.firstOrNull()?.id },
                icon = Icons.Default.Add,
                text = "Clone",
                visible = uiState.environments.isNotEmpty(),
                initiallyExpanded = true,
                expandedDurationMillis = 2500
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.environments.isEmpty() ->
                LoadingScreen("Loading boot environments...")
            uiState.environments.isEmpty() ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No boot environments found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(0.dp))
                    Spacer(Modifier.size(16.dp))
                    OutlinedButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
            else ->
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    item {
                        ExpressiveSection(title = "Environments", icon = Icons.Default.PowerSettingsNew) {
                            IconButton(onClick = { vm.refresh() }, modifier = Modifier.align(Alignment.End)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                    items(uiState.environments, key = { it.id ?: it.created ?: it.hashCode() }) { env ->
                        BootEnvironmentCard(
                            env = env,
                            isActing = uiState.isActing,
                            onClick = { onNavigateToDetail(env.id.orEmpty()) },
                            onActivate = { vm.activate(env.id.orEmpty()) },
                            onKeepChange = { value -> vm.keep(env.id.orEmpty(), value) },
                            onClone = { cloneSourceId = env.id },
                            onDestroy = { destroyTargetId = env.id }
                        )
                    }
                    item { Spacer(Modifier.size(88.dp)) }
                }
                    }
        }
    }

    cloneSourceId?.let { source ->
        CloneDialog(
            sourceId = source,
            onDismiss = { cloneSourceId = null },
            onConfirm = { target ->
                vm.clone(source, target)
                cloneSourceId = null
            }
        )
    }

    destroyTargetId?.let { id ->
        AlertDialog(
            onDismissRequest = { destroyTargetId = null },
            title = { Text("Destroy environment?") },
            text = { Text("Permanently destroy boot environment \"$id\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.destroy(id)
                    destroyTargetId = null
                }) { Text("Destroy") }
            },
            dismissButton = {
                TextButton(onClick = { destroyTargetId = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BootEnvironmentCard(
    env: System.BootEnvironmentQueryResultItem,
    isActing: Boolean,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    onKeepChange: (Boolean) -> Unit,
    onClone: () -> Unit,
    onDestroy: () -> Unit
) {
    val isActive = env.active == true
    val keep = env.keep == true
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = env.id ?: "Unknown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    env.used?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isActive) {
                    Text(
                        text = "RUNNING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    enabled = !isActing && !isActive && env.canActivate == true,
                    onClick = onActivate,
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Activate") }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Keep",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = keep,
                        enabled = !isActing,
                        onCheckedChange = onKeepChange
                    )
                }

                IconButton(
                    enabled = !isActing,
                    onClick = onClone,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Clone",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    enabled = !isActing && !isActive,
                    onClick = onDestroy,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Destroy",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CloneDialog(
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
                Spacer(Modifier.size(12.dp))
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
