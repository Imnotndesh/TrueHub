package com.imnotndesh.truehub.ui.login

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.Prefs
import com.imnotndesh.truehub.data.models.Auth.LoginMode
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.ui.background.AnimatedWavyGradientBackground
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.setup.ServerConfigBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    existingManager: TrueNASApiManager?,
    navController: NavController,
    onManagerInitialized: (TrueNASApiManager) -> Unit,
    onLoginSuccess : ()->Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val application = context.applicationContext as Application
    val (savedUrl, savedInsecure) = remember { Prefs.load(context) }

    // Local state for manager - use existing or create new
    var localManager by remember(existingManager) {
        mutableStateOf(existingManager)
    }
    var showSetupSheet by remember { mutableStateOf( savedUrl == null) }

    val viewModel: LoginScreenViewModel = viewModel(
        factory = LoginViewModelFactory(existingManager, application)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle manager initialization when URL is configured but manager is null
    LaunchedEffect(savedUrl, savedInsecure, localManager) {
        if (localManager == null && savedUrl != null) {
            lifecycleOwner.lifecycleScope.launch {
                try {
                    ToastManager.showInfo("Connecting to server...")

                    val config = Config.ClientConfig(
                        serverUrl = savedUrl,
                        insecure = savedInsecure,
                        connectionTimeoutMs = 15000,
                        enablePing = false,
                        enableDebugLogging = true
                    )

                    val client = TrueNASClient(config)
                    val newManager = TrueNASApiManager(client,context)
                    val connected = newManager.connect()

                    if (connected) {
                        localManager = newManager
                        onManagerInitialized(newManager)
                        viewModel.updateManager(newManager)
                        ToastManager.showSuccess("Connected to server!")
                        showSetupSheet = false
                    } else {
                        ToastManager.showError("Initial connection failed: Could not reach server.")
                        showSetupSheet = true
                    }
                } catch (e: Exception) {
                    ToastManager.showError("Connection failed: ${e.message}")
                    showSetupSheet = true
                }
            }
        }
    }

    // Handle login success
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            Log.d("LoginScreen", "Login successful, invoking onLoginSuccess callback.")

            onLoginSuccess()

            viewModel.handleEvent(LoginEvent.LoginNavigationCompleted)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            localManager != null -> {
                LoginContent(
                    manager = localManager!!,
                    viewModel = viewModel,
                    uiState = uiState,
                    onChangeServerConfig = { showSetupSheet = true },
                    apiKey = uiState.apiKey,
                    onApiKeyChange = { newApikey -> viewModel.handleEvent(LoginEvent.UpdateApiKey(newApikey)) },
                    isApiKeyVisible = uiState.isApiKeyVisible,
                    onToggleVisibilityClick = { viewModel.handleEvent(LoginEvent.ToggleApiKeyVisibility) },
                    saveForAutoLogin = uiState.saveDetailsForAutoLogin,
                    onSaveForAutoLoginChange = {change -> viewModel.handleEvent(LoginEvent.UpdateSaveApiKey(change,application)) },
                    isLoading = uiState.isLoading,
                )
            }
            savedUrl != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Connecting to server...")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showSetupSheet = true }) {
                        Text("Configure Server")
                    }
                }
            }
            else -> {
                ServerConfigurationPrompt(
                    onConfigureClick = { showSetupSheet = true }
                )
            }
        }
    }
        if (showSetupSheet) {
            ServerConfigBottomSheet(
                onDismiss = {
                    showSetupSheet = false
                },
                onConfigured = { url, insecure ->
                    lifecycleOwner.lifecycleScope.launch {
                        try {
                            ToastManager.showInfo("Connecting to server...")

                            val config = Config.ClientConfig(
                                serverUrl = url,
                                insecure = insecure,
                                connectionTimeoutMs = 15000,
                                enablePing = false,
                                enableDebugLogging = true
                            )

                            val client = TrueNASClient(config)
                            val newManager = TrueNASApiManager(client,context)
                            val connected = newManager.connect()

                            if (connected) {
                                Prefs.save(context, url, insecure)


                                localManager = newManager
                                onManagerInitialized(newManager)
                                viewModel.updateManager(newManager)

                                showSetupSheet = false
                                ToastManager.showSuccess("Connected successfully!")
                            } else {
                                ToastManager.showError("Failed to connect: Could not reach server with provided details.")
                                showSetupSheet = true
                            }

                        } catch (e: Exception) {
                            ToastManager.showError("Failed to connect: ${e.message}")
                        }
                    }
                },
                initialUrl = savedUrl,
                initialInsecure = savedInsecure,
                showChangeUrlOption = savedUrl != null
            )
        }
    }


@Composable
private fun LoginContent(
    manager: TrueNASApiManager,
    viewModel: LoginScreenViewModel,
    uiState: LoginUiState,
    onChangeServerConfig: () -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    isApiKeyVisible: Boolean,
    onToggleVisibilityClick: () -> Unit,
    saveForAutoLogin: Boolean,
    onSaveForAutoLoginChange: (Boolean) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current

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
        AnimatedWavyGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // Header Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Enter your\n")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("Credentials")
                            }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 38.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Please sign in to continue",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
                ConnectionStatusCard(uiState.connectionStatus) {
                    viewModel.handleEvent(LoginEvent.CheckConnection)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    LoginMethodTab(
                        text = "Password",
                        isSelected = uiState.loginMode == LoginMode.PASSWORD,
                        onClick = {
                            viewModel.handleEvent(LoginEvent.UpdateLoginMode(LoginMode.PASSWORD))
                        }
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    LoginMethodTab(
                        text = "API Key",
                        isSelected = uiState.loginMode == LoginMode.API_KEY,
                        onClick = {
                            viewModel.handleEvent(LoginEvent.UpdateLoginMode(LoginMode.API_KEY))
                        }
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (uiState.loginMode) {
                            LoginMode.PASSWORD -> {
                                OutlinedTextField(
                                    value = uiState.username,
                                    onValueChange = {
                                        viewModel.handleEvent(LoginEvent.UpdateUsername(it))
                                    },
                                    label = { Text("Username") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Username"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    enabled = !uiState.isLoading
                                )
                                OutlinedTextField(
                                    value = uiState.password,
                                    onValueChange = {
                                        viewModel.handleEvent(LoginEvent.UpdatePassword(it))
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Password"
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                viewModel.handleEvent(LoginEvent.TogglePasswordVisibility)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (uiState.isPasswordVisible) {
                                                    Icons.Default.VisibilityOff
                                                } else {
                                                    Icons.Default.Visibility
                                                },
                                                contentDescription = if (uiState.isPasswordVisible) {
                                                    "Hide password"
                                                } else {
                                                    "Show password"
                                                }
                                            )
                                        }
                                    },
                                    visualTransformation = if (uiState.isPasswordVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    enabled = !uiState.isLoading
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Save details for autologin?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Switch(
                                        checked = saveForAutoLogin,
                                        onCheckedChange = onSaveForAutoLoginChange,
                                        enabled = !isLoading
                                    )
                                }
                            }

                            LoginMode.API_KEY -> {
                                OutlinedTextField(
                                    value = apiKey,
                                    onValueChange = onApiKeyChange,
                                    label = { Text("Paste your TrueNAS API key here") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = "API Key"
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = onToggleVisibilityClick) {
                                            Icon(
                                                imageVector = if (isApiKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = if (isApiKeyVisible) "Hide API Key" else "Show API Key"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    minLines = 3,
                                    maxLines = 4,
                                    enabled = !isLoading
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Save key for autologin?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Switch(
                                        checked = saveForAutoLogin,
                                        onCheckedChange = onSaveForAutoLoginChange,
                                        enabled = !isLoading
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        viewModel.handleEvent(LoginEvent.Login(context))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && uiState.connectionStatus is ConnectionStatus.Connected
                ) {
                    if (uiState.isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Signing in...")
                        }
                    } else {
                        Text(
                            "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Need help accessing your ${if (uiState.loginMode == LoginMode.PASSWORD) "account" else "API key"}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            ToastManager.showInfo("Contact your administrator for assistance")
                        }
                )

                Spacer(modifier = Modifier.height(30.dp))
                ServerInfoSection(
                    onChangeServerClick = onChangeServerConfig
                )
            }
        }
    }
}


@Composable
private fun ConnectionStatusCard(
    connectionStatus: ConnectionStatus,
    onRetryClick: () -> Unit
) {
    val (statusText, statusColor, showRetryButton) = when (connectionStatus) {
        is ConnectionStatus.Connected -> Triple("Connected", MaterialTheme.colorScheme.primary, false)
        is ConnectionStatus.Connecting -> Triple("Connecting...", MaterialTheme.colorScheme.tertiary, false)
        is ConnectionStatus.Disconnected -> Triple("Disconnected", MaterialTheme.colorScheme.error, true)
        is ConnectionStatus.Error -> Triple("Connection Error", MaterialTheme.colorScheme.error, true)
        is ConnectionStatus.Unknown -> Triple("Checking connection...", MaterialTheme.colorScheme.onSurfaceVariant, false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showRetryButton) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onRetryClick() }
                )
            }
        }
    }
}

@Composable
private fun LoginMethodTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun ServerInfoSection(onChangeServerClick: () -> Unit) {
    val context = LocalContext.current
    val (serverUrl, _) = remember { Prefs.load(context) }

    Card(
        onClick = onChangeServerClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Web,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Accessing from:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = serverUrl ?: "Unknown server",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Composable
private fun ServerConfigurationPrompt(
    onConfigureClick: () -> Unit
) {

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title with gradient effect
            Text(
                text = "Server Setup Required",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Connect to your TrueNAS server to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SetupInfoItem(
                        icon = Icons.Default.Web,
                        title = "Server URL",
                        description = "Enter your TrueNAS server address"
                    )

                    HorizontalDivider(
                        Modifier, DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    SetupInfoItem(
                        icon = Icons.Default.Security,
                        title = "Secure Connection",
                        description = "Configure SSL/TLS settings"
                    )

                    HorizontalDivider(
                        Modifier, DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    SetupInfoItem(
                        icon = Icons.Default.Check,
                        title = "Quick Setup",
                        description = "Connect in just a few steps"
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onConfigureClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Configure Server",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "First time? We'll guide you through",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SetupInfoItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}