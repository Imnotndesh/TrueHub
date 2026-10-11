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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.adaptive.AdaptiveFormScreen
import com.imnotndesh.truehub.ui.adaptive.AdaptiveTokens
import com.imnotndesh.truehub.ui.adaptive.CenteredContent
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.ProvideTrueHubWindowForPreview
import com.imnotndesh.truehub.ui.adaptive.WidthClass
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveSection
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme

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
    val window = LocalTrueHubWindow.current

    var hostname by remember(config) { mutableStateOf(config?.hostname ?: "") }
    var domain by remember(config) { mutableStateOf(config?.domain ?: "") }
    var ipv4Gateway by remember(config) { mutableStateOf(config?.ipv4Gateway ?: "") }
    var ipv6Gateway by remember(config) { mutableStateOf(config?.ipv6Gateway ?: "") }
    var nameserver1 by remember(config) { mutableStateOf(config?.nameserver1 ?: "") }
    var nameserver2 by remember(config) { mutableStateOf(config?.nameserver2 ?: "") }
    var nameserver3 by remember(config) { mutableStateOf(config?.nameserver3 ?: "") }
    var httpProxy by remember(config) { mutableStateOf(config?.httpProxy ?: "") }

    val onSave: () -> Unit = {
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
    }

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
                    if (window.isCompactWidth) {
                        NetworkEditSaveBar(
                            isSaving = uiState.isSaving,
                            onDiscard = onNavigateBack,
                            onSave = onSave,
                        )
                    } else {
                        CenteredContent(AdaptiveTokens.FormMaxWidth) {
                            NetworkEditSaveBar(
                                isSaving = uiState.isSaving,
                                onDiscard = onNavigateBack,
                                onSave = onSave,
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (window.isCompactWidth) {
            // Compact keeps the original single-column LazyColumn layout, unchanged.
            Column(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                NetworkEditHeader(
                    manager = manager,
                    isLoading = uiState.isLoading,
                    onBack = onNavigateBack,
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
                                NetworkIdentitySection(
                                    hostname = hostname, onHostnameChange = { hostname = it },
                                    domain = domain, onDomainChange = { domain = it },
                                )
                            }
                            item {
                                NetworkGatewaysSection(
                                    ipv4Gateway = ipv4Gateway, onIpv4GatewayChange = { ipv4Gateway = it },
                                    ipv6Gateway = ipv6Gateway, onIpv6GatewayChange = { ipv6Gateway = it },
                                )
                            }
                            item {
                                NetworkDnsSection(
                                    nameserver1 = nameserver1, onNameserver1Change = { nameserver1 = it },
                                    nameserver2 = nameserver2, onNameserver2Change = { nameserver2 = it },
                                    nameserver3 = nameserver3, onNameserver3Change = { nameserver3 = it },
                                )
                            }
                            item {
                                NetworkHttpProxySection(
                                    httpProxy = httpProxy, onHttpProxyChange = { httpProxy = it },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        } else {
            // Medium and up: form column capped at 640dp and centered.
            AdaptiveFormScreen(
                header = {
                    NetworkEditHeader(
                        manager = manager,
                        isLoading = uiState.isLoading,
                        onBack = onNavigateBack,
                    )
                },
            ) {
                when {
                    uiState.isLoading -> LoadingScreen("Loading network...")
                    config != null -> {
                        NetworkIdentitySection(
                            hostname = hostname, onHostnameChange = { hostname = it },
                            domain = domain, onDomainChange = { domain = it },
                        )
                        NetworkGatewaysSection(
                            ipv4Gateway = ipv4Gateway, onIpv4GatewayChange = { ipv4Gateway = it },
                            ipv6Gateway = ipv6Gateway, onIpv6GatewayChange = { ipv6Gateway = it },
                        )
                        NetworkDnsSection(
                            nameserver1 = nameserver1, onNameserver1Change = { nameserver1 = it },
                            nameserver2 = nameserver2, onNameserver2Change = { nameserver2 = it },
                            nameserver3 = nameserver3, onNameserver3Change = { nameserver3 = it },
                        )
                        NetworkHttpProxySection(
                            httpProxy = httpProxy, onHttpProxyChange = { httpProxy = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkEditHeader(
    manager: TrueNASApiManager,
    isLoading: Boolean,
    onBack: () -> Unit,
) {
    UnifiedScreenHeader(
        title = "Edit Network",
        subtitle = "Modify network configuration",
        isLoading = isLoading,
        isRefreshing = false,
        error = null,
        onDismissError = {},
        manager = manager,
        onBackPressed = onBack
    )
}

@Composable
private fun NetworkEditSaveBar(
    isSaving: Boolean,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDiscard,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = !isSaving
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Discard", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
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

@Composable
private fun NetworkIdentitySection(
    hostname: String,
    onHostnameChange: (String) -> Unit,
    domain: String,
    onDomainChange: (String) -> Unit,
) {
    ExpressiveSection("Identity", Icons.Default.Public) {
        ExpressiveInfoCard {
            OutlinedTextField(
                value = hostname,
                onValueChange = onHostnameChange,
                label = { Text("Hostname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = domain,
                onValueChange = onDomainChange,
                label = { Text("Domain") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun NetworkGatewaysSection(
    ipv4Gateway: String,
    onIpv4GatewayChange: (String) -> Unit,
    ipv6Gateway: String,
    onIpv6GatewayChange: (String) -> Unit,
) {
    ExpressiveSection("Gateways", Icons.Default.Router) {
        ExpressiveInfoCard {
            OutlinedTextField(
                value = ipv4Gateway,
                onValueChange = onIpv4GatewayChange,
                label = { Text("IPv4 Gateway") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = ipv6Gateway,
                onValueChange = onIpv6GatewayChange,
                label = { Text("IPv6 Gateway") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun NetworkDnsSection(
    nameserver1: String,
    onNameserver1Change: (String) -> Unit,
    nameserver2: String,
    onNameserver2Change: (String) -> Unit,
    nameserver3: String,
    onNameserver3Change: (String) -> Unit,
) {
    ExpressiveSection("DNS Servers", Icons.Default.Language) {
        ExpressiveInfoCard {
            OutlinedTextField(
                value = nameserver1,
                onValueChange = onNameserver1Change,
                label = { Text("Nameserver 1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nameserver2,
                onValueChange = onNameserver2Change,
                label = { Text("Nameserver 2") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nameserver3,
                onValueChange = onNameserver3Change,
                label = { Text("Nameserver 3") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun NetworkHttpProxySection(
    httpProxy: String,
    onHttpProxyChange: (String) -> Unit,
) {
    ExpressiveSection("HTTP Proxy", Icons.Default.Visibility) {
        ExpressiveInfoCard {
            OutlinedTextField(
                value = httpProxy,
                onValueChange = onHttpProxyChange,
                label = { Text("HTTP Proxy URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("Leave blank if not used") }
            )
        }
    }
}

@Preview(name = "Network edit form — Compact", widthDp = 412, heightDp = 900)
@Composable
private fun NetworkEditFormCompactPreview() {
    TrueHubAppTheme {
        ProvideTrueHubWindowForPreview(width = WidthClass.Compact) {
            NetworkEditFormPreviewContent()
        }
    }
}

@Preview(name = "Network edit form — Expanded", widthDp = 1280, heightDp = 900)
@Composable
private fun NetworkEditFormExpandedPreview() {
    TrueHubAppTheme {
        ProvideTrueHubWindowForPreview(width = WidthClass.Expanded) {
            NetworkEditFormPreviewContent()
        }
    }
}

@Composable
private fun NetworkEditFormPreviewContent() {
    val window = LocalTrueHubWindow.current
    var hostname by remember { mutableStateOf("truenas.local") }
    var domain by remember { mutableStateOf("example.com") }
    var ipv4Gateway by remember { mutableStateOf("192.168.1.1") }
    var ipv6Gateway by remember { mutableStateOf("") }
    var nameserver1 by remember { mutableStateOf("1.1.1.1") }
    var nameserver2 by remember { mutableStateOf("") }
    var nameserver3 by remember { mutableStateOf("") }
    var httpProxy by remember { mutableStateOf("") }

    if (window.isCompactWidth) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NetworkIdentitySection(hostname, { hostname = it }, domain, { domain = it })
            NetworkGatewaysSection(ipv4Gateway, { ipv4Gateway = it }, ipv6Gateway, { ipv6Gateway = it })
            NetworkDnsSection(nameserver1, { nameserver1 = it }, nameserver2, { nameserver2 = it }, nameserver3, { nameserver3 = it })
            NetworkHttpProxySection(httpProxy, { httpProxy = it })
        }
    } else {
        AdaptiveFormScreen(header = {}) {
            NetworkIdentitySection(hostname, { hostname = it }, domain, { domain = it })
            NetworkGatewaysSection(ipv4Gateway, { ipv4Gateway = it }, ipv6Gateway, { ipv6Gateway = it })
            NetworkDnsSection(nameserver1, { nameserver1 = it }, nameserver2, { nameserver2 = it }, nameserver3, { nameserver3 = it })
            NetworkHttpProxySection(httpProxy, { httpProxy = it })
        }
    }
}
