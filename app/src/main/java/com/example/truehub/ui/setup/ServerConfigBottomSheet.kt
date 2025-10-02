package com.example.truehub.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truehub.ui.background.AnimatedWavyGradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigBottomSheet(
    onDismiss: () -> Unit,
    onConfigured: (String, Boolean) -> Unit,
    initialUrl: String? = null,
    initialInsecure: Boolean = false,
    showChangeUrlOption: Boolean = false
) {
    val viewModel = remember { SetupScreenViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Initialize with existing values
    LaunchedEffect(Unit) {
        initialUrl?.let {
            viewModel.handleEvent(SetupEvent.UpdateServerUrl(it.replace("/api/current", "")))
        }
        viewModel.handleEvent(SetupEvent.UpdateInsecure(initialInsecure))
    }

    // Handle successful configuration
    LaunchedEffect(uiState.setupComplete) {
        if (uiState.setupComplete) {
            onConfigured(viewModel.formatUrl(uiState.serverUrl), uiState.insecure)
            viewModel.handleEvent(SetupEvent.ResetSetupComplete)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!uiState.isConfiguring) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        AnimatedWavyGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = if (isLandscape)
                            (configuration.screenHeightDp * 0.95f).dp
                        else
                            (configuration.screenHeightDp * 0.85f).dp
                    )
            ) {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(modifier = Modifier.width(40.dp)) // Balance the close button

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (!uiState.isConfiguring) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                }

                // Title Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (showChangeUrlOption) "Server Configuration" else "Configure Your Server URL",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (showChangeUrlOption)
                            "Update your connection settings"
                        else
                            "Connect to your TrueNAS instance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Server URL Card
                    ServerUrlCard(
                        serverUrl = uiState.serverUrl,
                        urlValidation = uiState.urlValidation,
                        isConfiguring = uiState.isConfiguring,
                        onUrlChange = { viewModel.handleEvent(SetupEvent.UpdateServerUrl(it)) }
                    )

                    // Connection Error
                    uiState.connectionError?.let { error ->
                        ErrorCard(error = error)
                    }

                    // Security Settings Card
                    SecurityCard(
                        insecure = uiState.insecure,
                        isConfiguring = uiState.isConfiguring,
                        onInsecureChange = {
                            viewModel.handleEvent(SetupEvent.UpdateInsecure(it))
                        }
                    )

                    // Info Card
                    InfoCard()

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Action Buttons - Fixed at bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    ActionButtons(
                        isConfiguring = uiState.isConfiguring,
                        isValid = uiState.urlValidation.isValid && uiState.serverUrl.isNotEmpty(),
                        showChangeUrlOption = showChangeUrlOption,
                        onDismiss = onDismiss,
                        onConfigure = { viewModel.handleEvent(SetupEvent.Configure(context)) },
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerUrlCard(
    serverUrl: String,
    urlValidation: com.example.truehub.ui.setup.UrlValidation,
    isConfiguring: Boolean,
    onUrlChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Server Address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = onUrlChange,
                label = { Text("URL") },
                placeholder = { Text("ws://192.168.1.100") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = "Server URL"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (serverUrl.isNotEmpty() && !urlValidation.isValid) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    unfocusedBorderColor = if (serverUrl.isNotEmpty() && !urlValidation.isValid) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                ),
                isError = serverUrl.isNotEmpty() && !urlValidation.isValid,
                enabled = !isConfiguring,
                singleLine = true
            )

            // Validation Messages
            if (urlValidation.errors.isNotEmpty() || urlValidation.warnings.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    urlValidation.errors.forEach { error ->
                        ValidationMessage(message = error, isError = true)
                    }
                    urlValidation.warnings.forEach { warning ->
                        ValidationMessage(message = warning, isError = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityCard(
    insecure: Boolean,
    isConfiguring: Boolean,
    onInsecureChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Security Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (insecure) Icons.Default.Warning else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (insecure) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Allow insecure connections",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "For self-signed certificates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Switch(
                    checked = insecure,
                    onCheckedChange = onInsecureChange,
                    enabled = !isConfiguring
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Connection Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Use ws:// for HTTP or wss:// for HTTPS\n• Include port number if needed\n• Example: ws://192.168.1.100:80",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isConfiguring: Boolean,
    isValid: Boolean,
    showChangeUrlOption: Boolean,
    onDismiss: () -> Unit,
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isConfiguring) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp
                )
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Button(
            onClick = onConfigure,
            enabled = isValid && !isConfiguring,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            if (isConfiguring) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Connecting...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (showChangeUrlOption) Icons.Default.Check else Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        if (showChangeUrlOption) "Update Server" else "Connect Now",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ValidationMessage(
    message: String,
    isError: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.Error else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.tertiary
            }
        )
    }
}