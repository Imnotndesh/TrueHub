package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkEditScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: NetworkConfigViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadAll() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Network configuration saved")
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    val config = uiState.config

    var hostname by remember(config) { mutableStateOf(config?.hostname ?: "") }
    var domain by remember(config) { mutableStateOf(config?.domain ?: "") }
    var ipv4Gateway by remember(config) { mutableStateOf(config?.ipv4Gateway ?: "") }
    var ipv6Gateway by remember(config) { mutableStateOf(config?.ipv6Gateway ?: "") }
    var nameserver1 by remember(config) { mutableStateOf(config?.nameserver1 ?: "") }
    var nameserver2 by remember(config) { mutableStateOf(config?.nameserver2 ?: "") }
    var nameserver3 by remember(config) { mutableStateOf(config?.nameserver3 ?: "") }
    var httpProxy by remember(config) { mutableStateOf(config?.httpProxy ?: "") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        bottomBar = {
            if (config != null) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !uiState.isSaving
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Discard", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                vm.updateConfig(
                                    System.NetworkConfigurationUpdateArgs(
                                        hostname = hostname.ifBlank { null },
                                        domain = domain.ifBlank { null },
                                        ipv4Gateway = ipv4Gateway.ifBlank { null },
                                        ipv6Gateway = ipv6Gateway.ifBlank { null },
                                        nameserver1 = nameserver1.ifBlank { null },
                                        nameserver2 = nameserver2.ifBlank { null },
                                        nameserver3 = nameserver3.ifBlank { null },
                                        httpProxy = httpProxy.ifBlank { null }
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Changes", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            UnifiedScreenHeader(
                title = "Edit Network",
                subtitle = "Modify network configuration",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
            when {
                uiState.isLoading -> LoadingScreen("Loading network...")
                config != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            ExpressiveSection("Identity", Icons.Default.Public) {
                                ExpressiveInfoCard {
                                    OutlinedTextField(
                                        value = hostname,
                                        onValueChange = { hostname = it },
                                        label = { Text("Hostname") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = domain,
                                        onValueChange = { domain = it },
                                        label = { Text("Domain") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        item {
                            ExpressiveSection("Gateways", Icons.Default.Router) {
                                ExpressiveInfoCard {
                                    OutlinedTextField(
                                        value = ipv4Gateway,
                                        onValueChange = { ipv4Gateway = it },
                                        label = { Text("IPv4 Gateway") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = ipv6Gateway,
                                        onValueChange = { ipv6Gateway = it },
                                        label = { Text("IPv6 Gateway") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        item {
                            ExpressiveSection("DNS Servers", Icons.Default.Language) {
                                ExpressiveInfoCard {
                                    OutlinedTextField(
                                        value = nameserver1,
                                        onValueChange = { nameserver1 = it },
                                        label = { Text("Nameserver 1") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = nameserver2,
                                        onValueChange = { nameserver2 = it },
                                        label = { Text("Nameserver 2") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = nameserver3,
                                        onValueChange = { nameserver3 = it },
                                        label = { Text("Nameserver 3") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        item {
                            ExpressiveSection("HTTP Proxy", Icons.Default.Visibility) {
                                ExpressiveInfoCard {
                                    OutlinedTextField(
                                        value = httpProxy,
                                        onValueChange = { httpProxy = it },
                                        label = { Text("HTTP Proxy URL") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        supportingText = { Text("Leave blank if not used") }
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}