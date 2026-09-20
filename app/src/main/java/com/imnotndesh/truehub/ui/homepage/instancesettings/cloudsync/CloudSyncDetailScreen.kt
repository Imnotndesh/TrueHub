package com.imnotndesh.truehub.ui.homepage.instancesettings.cloudsync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Cloudsync
import com.imnotndesh.truehub.ui.components.ExpressiveIconButton
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveSection
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.InfoRow
import kotlinx.coroutines.launch

@Composable
fun CloudSyncDetailScreen(
    manager: TrueNASApiManager,
    taskId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {}
) {
    var task by remember { mutableStateOf<Cloudsync.Entry?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            isLoading = true
            error = null
            when (val result = manager.cloudsync.getTask(taskId)) {
                is ApiResult.Success -> task = result.data
                is ApiResult.Error -> error = result.message
                else -> Unit
            }
            isLoading = false
        }
    }

    LaunchedEffect(taskId) { reload() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UnifiedScreenHeader(
                title = task?.description?.ifBlank { null } ?: "Task $taskId",
                subtitle = "Cloud Sync Task",
                isLoading = isLoading,
                isRefreshing = false,
                error = error,
                onRefresh = { reload() },
                onDismissError = { error = null },
                manager = manager,
                onBackPressed = onNavigateBack,
                trailingActions = {
                    if (task != null) {
                        ExpressiveIconButton(
                            onClick = { onNavigateToEdit(taskId) },
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit task"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExpressiveSection(title = "Task", icon = Icons.Default.Cloud) {
                ExpressiveInfoCard {
                    InfoRow(label = "Description", value = task?.description?.ifBlank { null } ?: "—")
                    TaskDivider()
                    InfoRow(label = "Path", value = task?.path ?: "—")
                    TaskDivider()
                    InfoRow(label = "Direction", value = task?.direction ?: "—")
                    TaskDivider()
                    InfoRow(label = "Transfer Mode", value = task?.transferMode ?: "—")
                    TaskDivider()
                    InfoRow(label = "Enabled", value = if (task?.enabled == true) "Yes" else "No")
                }
            }
        }
    }
}

@Composable
private fun TaskDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
