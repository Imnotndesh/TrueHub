package com.imnotndesh.truehub.ui.homepage.instancesettings.cloudsync
import androidx.compose.runtime.saveable.rememberSaveable
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.ListDetailLayout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.ui.components.expressive.StatusTone
import com.imnotndesh.truehub.ui.components.expressive.color
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Cloudsync
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun CloudSyncScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {}
) {
    val viewModel: CloudSyncViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<Cloudsync.Entry?>(null) }
    var selectedTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
    val window = LocalTrueHubWindow.current
    val showDetail = window.isAtLeastExpanded && selectedTaskId != null

    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            ToastManager.showSuccess("Task deleted")
            viewModel.clearDeleted()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadTasks() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        UnifiedScreenHeader(
            title = "Cloud Sync",
            subtitle = "Backup and sync tasks",
            isLoading = state.isLoading,
            isRefreshing = false,
            error = state.error,
            onDismissError = { viewModel.dismissError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        ListDetailLayout(
            showDetail = showDetail,
            modifier = Modifier.weight(1f),
            list = {
                Box(modifier = Modifier.fillMaxSize()) {
                    PullToRefreshContent(
                        isRefreshing = state.isLoading,
                        onRefresh = { viewModel.loadTasks() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.tasks.isEmpty() && !state.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "No cloud sync tasks",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.tasks, key = { it.id ?: it.hashCode() }) { task ->
                                    CloudSyncTaskRow(
                                        task = task,
                                        onClick = { task.id?.let { onNavigateToDetail(it) } },
                                        onDelete = { pendingDelete = task }
                                    )
                                }
                            }
                        }
                    }
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToCreate,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("New Task") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            },
            detail = {
                selectedTaskId?.let { taskId ->
                    CloudSyncDetailScreen(
                        manager = manager,
                        taskId = taskId,
                        onNavigateBack = { selectedTaskId = null },
                        onNavigateToEdit = onNavigateToEdit
                    )
                }
            }
        )
    }

    pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete task?") },
            text = { Text("This removes the cloud sync task configuration.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        task.id?.let(viewModel::deleteTask)
                        pendingDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CloudSyncTaskRow(
    task: Cloudsync.Entry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val enabled = task.enabled == true
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.description?.ifBlank { null } ?: "Task ${task.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.path ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = if (enabled) StatusTone.POSITIVE.color().copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = if (enabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) StatusTone.POSITIVE.color() else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
