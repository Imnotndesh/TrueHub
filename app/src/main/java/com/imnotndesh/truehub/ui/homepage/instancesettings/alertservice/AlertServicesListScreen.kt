package com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlertServicesListScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Alerts.AlertServiceEntry) -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToClassesConfig: () -> Unit = {}
) {
    val viewModel: AlertServicesListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colorScheme.surface, colorScheme.surfaceContainer)
                )
            )
    ) {
        UnifiedScreenHeader(
            title = "Alert Services",
            subtitle = "${uiState.services.size} service(s) configured",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = null,
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                AlertServicesFabMenu(
                    onAddClick = onNavigateToCreate,
                    onConfigureCategoriesClick = onNavigateToClassesConfig
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    uiState.isLoading -> LoadingScreen("Loading alert services...")
                    uiState.services.isEmpty() -> EmptyServicesState()
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.services, key = { it.id }) { service ->
                            AlertServiceCard(
                                service = service,
                                onClick = { onNavigateToDetail(service) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlertServicesFabMenu(
    onAddClick: () -> Unit,
    onConfigureCategoriesClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    BackHandler(expanded) { expanded = false }

    FloatingActionButtonMenu(
        expanded = expanded,
        horizontalAlignment = Alignment.End,
        button = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    if (expanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above
                ),
                tooltip = {
                    PlainTooltip(
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Assertive
                            paneTitle = "Alert service options"
                        }
                    ) {
                        Text(if (expanded) "Close" else "Alert service options")
                    }
                },
                state = rememberTooltipState()
            ) {
                ToggleFloatingActionButton(
                    modifier = Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = if (expanded) "Expanded" else "Collapsed"
                            contentDescription = "Alert service options"
                        }
                        .animateFloatingActionButton(
                            visible = true,
                            alignment = Alignment.BottomEnd
                        ),
                    checked = expanded,
                    onCheckedChange = { expanded = it },
                    containerColor = { progress ->
                        androidx.compose.ui.graphics.lerp(
                            colorScheme.primaryContainer,
                            colorScheme.primaryContainer,
                            progress
                        )
                    }
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onAddClick()
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("New Service") }
        )
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onConfigureCategoriesClick()
            },
            icon = { Icon(Icons.Default.Tune, contentDescription = null) },
            text = { Text("Configure Categories") }
        )
    }
}

@Composable
private fun EmptyServicesState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No alert services configured",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add one",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AlertServiceCard(
    service: Alerts.AlertServiceEntry,
    onClick: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = if (service.enabled) Color(0xFF2E7D32) else colorScheme.outline,
        label = "statusColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (service.enabled) Icons.Default.Email else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = service.type__title,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(100.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (service.enabled) "Active" else "Off",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}