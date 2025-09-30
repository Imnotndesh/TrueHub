package com.example.truehub.ui.services

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.Apps
import com.example.truehub.data.models.System
import com.example.truehub.ui.services.containers.ContainerScreen
import com.example.truehub.ui.services.details.AppInfoDialog
import com.example.truehub.ui.services.vm.VmScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(manager: TrueNASApiManager) {
    val viewModel: ServicesScreenViewModel = viewModel(
        factory = ServicesScreenViewModel.ServicesViewModelFactory(manager)
    )
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Apps", "Containers", "VMs")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Services",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "${uiState.apps.size} containers"
                            1 -> "0 containers"
                            2 -> "0 virtual machines"
                            else -> ""
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = { viewModel.refresh() },
                    enabled = !uiState.isLoading && !uiState.isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {},
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(vertical = 12.dp),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedTabIndex == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> {
                    // Apps Tab
                    when {
                        uiState.isLoading -> {
                            LoadingContent()
                        }
                        uiState.apps.isEmpty() && !uiState.isLoading -> {
                            EmptyContent()
                        }
                        else -> {
                            ServicesContent(
                                apps = uiState.apps,
                                isRefreshing = uiState.isRefreshing,
                                onStartApp = {appName ->viewModel.startApp(appName)},
                                onStopApp = {appName -> viewModel.stopApp(appName)},
                                onAppUpgrade = {appName -> viewModel.upgradeApp(appName)},
                                upgradeJobs = uiState.upgradeJobs
                            )
                        }
                    }
                }
                1 -> {
                    // Containers Tab - Empty for now
//                    EmptyTabContent(
//                        title = "No containers found",
//                        description = "Container management coming soon"
//                    )
                    ContainerScreen(manager)
                }
                2 -> {
                    // VMs Tab - Empty for now
//                    EmptyTabContent(
//                        title = "No virtual machines found",
//                        description = "VM management coming soon"
//                    )
                    VmScreen(manager)
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading services...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No services found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Install apps from the catalog to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyTabContent(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ServicesContent(
    apps: List<Apps.AppQueryResponse>,
    isRefreshing: Boolean,
    onStartApp: (String) -> Unit,
    onStopApp: (String) -> Unit,
    onAppUpgrade: (String) -> Unit,
    upgradeJobs: Map<String, System.UpgradeJobState>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (isRefreshing) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        items(apps) { app ->
            ServiceCard(
                app = app,
                onStartApp = onStartApp,
                onStopApp = onStopApp,
                onAppUpgrade = onAppUpgrade,
                upgradeJobs = upgradeJobs
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCard(
    app: Apps.AppQueryResponse,
    onStartApp: (String) -> Unit,
    onStopApp: (String) -> Unit,
    onAppUpgrade: (String) -> Unit,
    upgradeJobs: Map<String, System.UpgradeJobState>
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                app.metadata?.icon.let { iconUrl ->
                    Column {
                        AsyncImage(
                            model = iconUrl,
                            contentDescription = "App icon",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(5.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = app.metadata?.title ?: app.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "ID: ${app.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusChip(state = app.state, icon = getStatusIcon(app.state))
            }

            if (app.upgrade_available || upgradeJobs[app.name] != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Update available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    if (upgradeJobs[app.name] == null) {
                        UpgradeButton(
                            onAppUpgrade = { onAppUpgrade(app.name) }
                        )
                    } else {
                        UpgradeStatusChip(upgradeState = upgradeJobs[app.name]!!)
                    }
                }

                upgradeJobs[app.name]?.let { jobState ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = jobState.description ?: "Upgrading...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${jobState.progress}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (jobState.progress.coerceIn(0, 100).toFloat() / 100f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!app.state.equals("running", ignoreCase = true)){
                    ActionButton(
                        text = "Start",
                        icon = Icons.Default.PlayArrow,
                        enabled = true,
                        isPrimary = true,
                        onClick = { onStartApp(app.name) },
                        modifier = Modifier.weight(1f)
                    )
                }else{
                    ActionButton(
                        text = "Stop",
                        icon = Icons.Default.Stop,
                        enabled = true,
                        isPrimary = true,
                        onClick = { onStopApp(app.name) },
                        modifier = Modifier.weight(1f)
                    )
                }

                ActionButton(
                    text = "View Info",
                    icon = Icons.Default.Info,
                    enabled = true,
                    isPrimary = false,
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showInfoDialog) {
        AppInfoDialog(
            app = app,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
private fun UpgradeButton(
    onAppUpgrade: () -> Unit
) {
    Surface(
        onClick = onAppUpgrade,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Update",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun UpgradeStatusChip(
    upgradeState: System.UpgradeJobState
) {
    Surface(
        color = when (upgradeState.state.lowercase()) {
            "success" -> MaterialTheme.colorScheme.primaryContainer
            "failed", "aborted" -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (upgradeState.state.lowercase()) {
                "success" -> {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                "failed", "aborted" -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                else -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = when (upgradeState.state.lowercase()) {
                    "upgrading" -> "Upgrading..."
                    "success" -> "Updated"
                    "failed" -> "Failed"
                    "aborted" -> "Aborted"
                    else -> upgradeState.state.lowercase().replaceFirstChar { it.uppercase() }
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (upgradeState.state.lowercase()) {
                    "success" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "failed", "aborted" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatusChip(
    state: String,
    icon: ImageVector
) {
    Surface(
        color = getStatusColor(state).copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = getStatusColor(state)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = state.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = getStatusColor(state),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun getStatusColor(state: String): Color {
    return when (state.lowercase()) {
        "running", "active" -> Color(0xFF2E7D32)
        "stopped", "inactive" -> Color(0xFF757575)
        "error", "failed" -> MaterialTheme.colorScheme.error
        "paused" -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusIcon(state: String): ImageVector {
    return when (state.lowercase()) {
        "running", "active" -> Icons.Default.PlayArrow
        "stopped", "inactive" -> Icons.Default.Stop
        "error", "failed" -> Icons.Default.Error
        "paused" -> Icons.Default.Pause
        else -> Icons.Default.Stop
    }
}