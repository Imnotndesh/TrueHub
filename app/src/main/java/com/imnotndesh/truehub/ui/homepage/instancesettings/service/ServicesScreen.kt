package com.imnotndesh.truehub.ui.homepage.instancesettings.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

/**
 * Lists all TrueNAS services with quick start/stop control and an entry
 * point into [ServiceDetailScreen] for editing the start-on-boot setting.
 *
 * Mirrors AppsScreen's header / pull-to-refresh / loading-empty pattern,
 * minus multi-select and FAB menus, which don't apply to a fixed system
 * service list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToServiceDetail: (System.ServiceQueryResponse) -> Unit
) {
    val viewModel: ServicesScreenViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val refreshState = rememberPullToRefreshState()

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Services",
            subtitle = "${uiState.services.size} Services",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onDismissError = { viewModel.clearError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = refreshState,
            modifier = Modifier.weight(1f)
        ) {
            when {
                uiState.services.isEmpty() && uiState.isLoading && !uiState.isRefreshing -> {
                    LoadingScreen("Loading Services")
                }
                uiState.services.isEmpty() && !uiState.isLoading && uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Couldn't load services",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.services, key = { it.id }) { service ->
                            ServiceCard(
                                service = service,
                                isBusy = uiState.pendingServiceIds.contains(service.id),
                                onToggleRunning = { running ->
                                    viewModel.setServiceRunning(service, running)
                                },
                                onClick = { onNavigateToServiceDetail(service) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: System.ServiceQueryResponse,
    isBusy: Boolean,
    onToggleRunning: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isRunning = service.state.equals("running", ignoreCase = true)
    val statusColor = getServiceStatusColor(service.state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
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
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getServiceStatusIcon(service.state),
                    contentDescription = service.state,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.service,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBusy) "Updating..." else service.state.lowercase()
                        .replaceFirstChar { it.uppercase() } + if (service.enable) " · Starts on boot" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Switch(
                    checked = isRunning,
                    onCheckedChange = onToggleRunning,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

private fun getServiceStatusIcon(state: String): ImageVector {
    return when (state.lowercase()) {
        "running" -> Icons.Default.PlayArrow
        "stopped" -> Icons.Default.Stop
        "failed", "error" -> Icons.Default.Error
        "paused" -> Icons.Default.Pause
        else -> Icons.Default.Stop
    }
}

private fun getServiceStatusColor(state: String): Color {
    return when (state.lowercase()) {
        "running" -> Color(0xFF2E7D32)
        "failed", "error" -> Color(0xFFD32F2F)
        "paused" -> Color(0xFFF57C00)
        else -> Color(0xFF757575)
    }
}