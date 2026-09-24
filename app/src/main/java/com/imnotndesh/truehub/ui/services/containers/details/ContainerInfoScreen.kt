package com.imnotndesh.truehub.ui.services.containers.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun ContainerInfoScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit = onNavigateBack,
    viewModel: ContainerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(containerId) {
        contentVisible = true
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
                onRefresh = viewModel::load,
                onDismissError = viewModel::load,
                manager = manager,
                onBackPressed = onNavigateBack,
                trailingActions = {
                    androidx.compose.material3.IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit container")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
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
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow,
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                    ) {
                        state.container?.let { container ->
                            ContainerInfoPane(
                                container = container,
                                onClose = onNavigateBack,
                                showInlineHeader = false
                            )
                        }
                    }
                }
            }
        }
    }
}
