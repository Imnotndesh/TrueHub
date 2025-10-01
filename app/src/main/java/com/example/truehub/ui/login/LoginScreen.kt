package com.example.truehub.ui.login

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavController
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.helpers.Prefs
import com.example.truehub.data.models.Auth.LoginMode
import com.example.truehub.data.models.Config
import com.example.truehub.ui.Screen
import com.example.truehub.ui.background.AnimatedWavyGradientBackground
import com.example.truehub.ui.components.ToastManager
import com.example.truehub.ui.setup.ServerConfigBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    existingManager: TrueNASApiManager?,
    navController: NavController,
    onManagerInitialized: (TrueNASApiManager) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val (savedUrl, savedInsecure) = remember { Prefs.load(context) }

    // Local state for manager - use existing or create new
    var localManager by remember(existingManager) {
        mutableStateOf(existingManager)
    }
    var showSetupSheet by remember { mutableStateOf(localManager == null || savedUrl == null) }

    val viewModel = remember(localManager) {
        LoginScreenViewModel(localManager)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle manager initialization when URL is configured but manager is null
    LaunchedEffect(savedUrl, savedInsecure, localManager) {
        if (localManager == null && savedUrl != null) {
            // Initialize manager with saved configuration
            lifecycleOwner.lifecycleScope.launch {
                try {
                    ToastManager.showInfo("Connecting to server...")

                    val config = Config.ClientConfig(
                        serverUrl = savedUrl,
                        insecure = savedInsecure ?: false,
                        connectionTimeoutMs = 15000,
                        enablePing = false,
                        enableDebugLogging = true
                    )

                    val client = TrueNASClient(config)
                    val newManager = TrueNASApiManager(client)
                    newManager.connect()

                    localManager = newManager
                    onManagerInitialized(newManager)
                    viewModel.updateManager(newManager)

                    ToastManager.showSuccess("Connected to server!")

                } catch (e: Exception) {
                    // Connection failed, show setup sheet
                    ToastManager.showError("Connection failed: ${e.message}")
                    showSetupSheet = true
                }
            }
        }
    }

    // Handle login success
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.handleEvent(LoginEvent.ResetLoginState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main login UI or connection setup
        when {
            localManager != null -> {
                // We have a manager - show normal login UI
                LoginContent(
                    manager = localManager!!,
                    viewModel = viewModel,
                    uiState = uiState,
                    onChangeServerConfig = { showSetupSheet = true }
                )
            }
            savedUrl != null -> {
                // We have a saved URL but manager is not initialized - show loading
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
                // No URL configured at all
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Server Configuration Required",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "Please configure your TrueNAS server to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(onClick = { showSetupSheet = true }) {
                        Text("Configure Server")
                    }
                }
            }
        }

        // Setup Bottom Sheet
        if (showSetupSheet) {
            ServerConfigBottomSheet(
                onDismiss = {
                    // Only allow dismiss if we have a working manager
                    if (localManager != null && savedUrl != null) {
                        showSetupSheet = false
                    }
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
                            val newManager = TrueNASApiManager(client)
                            newManager.connect()

                            // Save configuration
                            Prefs.save(context, url, insecure)

                            // Update managers
                            localManager = newManager
                            onManagerInitialized(newManager)
                            viewModel.updateManager(newManager)

                            showSetupSheet = false
                            ToastManager.showSuccess("Connected successfully!")

                        } catch (e: Exception) {
                            ToastManager.showError("Failed to connect: ${e.message}")
                        }
                    }
                },
                initialUrl = savedUrl,
                initialInsecure = savedInsecure ?: false,
                showChangeUrlOption = savedUrl != null
            )
        }
    }
}

@Composable
private fun LoginContent(
    manager: TrueNASApiManager,
    viewModel: LoginScreenViewModel,
    uiState: LoginUiState,
    onChangeServerConfig: () -> Unit
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

                // Connection Status Indicator
                ConnectionStatusCard(uiState.connectionStatus) {
                    viewModel.handleEvent(LoginEvent.CheckConnection)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Method Selection
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

                // Input Fields Card
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
                                // Username Field
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

                                // Password Field
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
                            }

                            LoginMode.API_KEY -> {
                                // API Key Field
                                OutlinedTextField(
                                    value = uiState.apiKey,
                                    onValueChange = {
                                        viewModel.handleEvent(LoginEvent.UpdateApiKey(it))
                                    },
                                    label = { Text("API Key") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = "API Key"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    minLines = 3,
                                    maxLines = 4,
                                    enabled = !uiState.isLoading
                                )

                                Text(
                                    text = "Paste your TrueNAS API key here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
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

                // Help Text
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
                        .background(statusColor, shape = androidx.compose.foundation.shape.CircleShape)
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
private fun LoadingScreen(message: String = "Loading...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
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