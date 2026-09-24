package com.imnotndesh.truehub.ui.services.containers.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun ContainerInfoScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit = onNavigateBack,
    onManageDevices: () -> Unit = onNavigateBack,
    onManageVolumes: () -> Unit = onNavigateBack,
    viewModel: ContainerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(containerId) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = state.container?.name ?: "Container",
                subtitle = "Container Details",
                isLoading = state.isLoading,
                 isRefreshing = state.isRefreshing,
                 error = state.error,
                 onDismissError = viewModel::load,
                 manager = manager,
                 onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            PullToRefreshContent(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::load,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.container == null -> LoadingScreen("Loading Container")
                    state.container == null -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.error ?: "Container is unavailable",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = viewModel::load,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    else -> {
                        AnimatedContent(
                            targetState = state.container?.id,
                            transitionSpec = {
                                (fadeIn(tween(500)) + slideInVertically { it / 4 })
                                    .togetherWith(fadeOut(tween(300)) + slideOutVertically { -it / 4 })
                            },
                            label = "container_detail_state"
                        ) { containerId ->
                            if (containerId != null) {
                                state.container?.let { container ->
                                    ContainerInfoPane(
                                        container = container,
                                        onClose = onNavigateBack,
                                        showInlineHeader = false,
                                        bottomActions = {
                                            ContainerActionsCard(
                                                onManageDevices = onManageDevices,
                                                onManageVolumes = onManageVolumes,
                                                onEdit = onEdit
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContainerActionsCard(
    onManageDevices: () -> Unit,
    onManageVolumes: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Container actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onManageDevices,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Devices, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Devices", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onManageVolumes,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Volumes", maxLines = 1)
                }
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", maxLines = 1)
                }
            }
        }
    }
}
