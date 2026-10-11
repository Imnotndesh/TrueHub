package com.imnotndesh.truehub.ui.login
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.imnotndesh.truehub.ui.setup.OnboardingScaffold
import com.imnotndesh.truehub.ui.setup.OnboardingBrand
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.layout.imePadding
import com.imnotndesh.truehub.ui.Screen

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.Prefs
import com.imnotndesh.truehub.data.models.Auth.LoginMode
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.ui.background.ExpressiveScreenBackground
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    existingManager: TrueNASApiManager?,
    navController: NavController,
    onManagerInitialized: (TrueNASApiManager) -> Unit,
    onLoginSuccess : ()->Unit,
    startInOtpMode: Boolean = false,
    totpUsername: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val application = context.applicationContext as Application
    val (savedUrl, savedInsecure) = remember { Prefs.load(context) }

    // Local state for manager - use existing or create new
    var localManager by remember(existingManager) {
        mutableStateOf(existingManager)
    }

    val viewModel: LoginScreenViewModel = hiltViewModel<LoginScreenViewModel, LoginScreenViewModel.Factory> { factory ->
        factory.create(existingManager)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Enter OTP mode on cold start for TOTP users
    LaunchedEffect(startInOtpMode, totpUsername) {
        if (startInOtpMode && totpUsername != null) {
            viewModel.enterOtpMode(totpUsername)
        }
    }

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
                        enablePing = true,
                        enableDebugLogging = false
                    )

                    val client = TrueNASClient(config)
                    val newManager = TrueNASApiManager(client,context.applicationContext)
                    val connected = newManager.connect()

                    if (connected) {
                        localManager = newManager
                        onManagerInitialized(newManager)
                        viewModel.updateManager(newManager)
                        ToastManager.showSuccess("Connected to server!")
                    } else {
                        ToastManager.showError("Initial connection failed: Could not reach server.")
                        navController.navigate(Screen.ServerSetup.route)
                    }
                } catch (e: Exception) {
                    ToastManager.showError("Connection failed: ${e.message}")
                    navController.navigate(Screen.ServerSetup.route)
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
            // OTP takes over the entire screen — no login form visible
            localManager != null && uiState.showOtpField -> {
                OtpFullScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onChangeServerConfig = { navController.navigate(Screen.ServerSetup.route) }
                )
            }
            localManager != null -> {
                LoginContent(
                    manager = localManager!!,
                    viewModel = viewModel,
                    uiState = uiState,
                    onChangeServerConfig = { navController.navigate(Screen.ServerSetup.route) },
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
                    Button(onClick = { navController.navigate(Screen.ServerSetup.route) }) {
                        Text("Configure Server")
                    }
                }
            }
            else -> {
                ServerConfigurationPrompt(
                    onConfigureClick = { navController.navigate(Screen.ServerSetup.route) }
                )
            }
        }
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
    val window = LocalTrueHubWindow.current

    OnboardingScaffold(
        brand = {
            OnboardingBrand(
                title = "Welcome back",
                tagline = "Sign in to continue",
                onContainer = window.isAtLeastMedium,
                shapeSize = if (window.isAtLeastMedium) 144.dp else 112.dp,
                titleStyle = if (window.isAtLeastMedium) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Only surface the connection card when something needs attention; a healthy
            // connection is already implied by a usable sign-in form.
            if (uiState.connectionStatus !is ConnectionStatus.Connected) {
                ConnectionStatusCard(uiState.connectionStatus) {
                    viewModel.handleEvent(LoginEvent.CheckConnection)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = uiState.loginMode == LoginMode.PASSWORD,
                            onClick = {
                                viewModel.handleEvent(LoginEvent.UpdateLoginMode(LoginMode.PASSWORD))
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = loginSegmentColors()
                        ) {
                            Text("Password")
                        }
                        SegmentedButton(
                            selected = uiState.loginMode == LoginMode.API_KEY,
                            onClick = {
                                viewModel.handleEvent(LoginEvent.UpdateLoginMode(LoginMode.API_KEY))
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = loginSegmentColors()
                        ) {
                            Text("API Key")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = loginFieldColors(),
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
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        viewModel.handleEvent(LoginEvent.Login(context))
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = loginFieldColors(),
                                enabled = !uiState.isLoading
                            )
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
                                shape = RoundedCornerShape(14.dp),
                                colors = loginFieldColors(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        viewModel.handleEvent(LoginEvent.Login(context))
                                    }
                                ),
                                enabled = !isLoading
                            )
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

            Button(
                onClick = {
                    viewModel.handleEvent(LoginEvent.Login(context))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

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

            ServerInfoSection(
                onChangeServerClick = onChangeServerConfig
            )
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

@Composable
private fun loginSegmentColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    activeBorderColor = MaterialTheme.colorScheme.primaryContainer,
    inactiveContainerColor = Color.Transparent,
    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
)

@Composable
private fun OtpFullScreen(
    viewModel: LoginScreenViewModel,
    uiState: LoginUiState,
    onChangeServerConfig: () -> Unit
) {
    TotpScreen(
        username = uiState.otpUsername,
        otpToken = uiState.otpToken,
        onOtpChange = { viewModel.handleEvent(LoginEvent.UpdateOtpToken(it)) },
        onSubmit = { viewModel.handleEvent(LoginEvent.SubmitOtp) },
        onBackToLogin = { viewModel.handleEvent(LoginEvent.ResetLoginState) },
        onChangeServer = onChangeServerConfig,
        isLoading = uiState.isLoading
    )
}

/**
 * Stateless two-factor screen. Public so it can be previewed in the debug mock harness and reused
 * for a future device-pairing / continuity flow.
 */
@Composable
fun TotpScreen(
    username: String,
    otpToken: String,
    onOtpChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    onChangeServer: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val otpFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { otpFocusRequester.requestFocus() }
    val window = LocalTrueHubWindow.current

    OnboardingScaffold(
        modifier = modifier,
        brand = {
            OnboardingBrand(
                title = "Two-factor\nauthentication",
                tagline = "A 6-digit code has been requested for ${username.ifBlank { "your account" }}",
                icon = Icons.Default.Security,
                onContainer = window.isAtLeastMedium,
                shapeSize = if (window.isAtLeastMedium) 144.dp else 112.dp,
                titleStyle = if (window.isAtLeastMedium) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall
            )
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter verification code",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = otpToken,
                    onValueChange = {
                        // Only allow digits, max 6 chars
                        val filtered = it.filter { c -> c.isDigit() }.take(6)
                        onOtpChange(filtered)
                    },
                    placeholder = { Text("000000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(otpFocusRequester),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    colors = loginFieldColors(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isLoading && otpToken.length >= 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying...", fontSize = 15.sp)
                    } else {
                        Text("Verify & Sign In", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onBackToLogin) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back to login", fontWeight = FontWeight.Medium)
            }

            TextButton(onClick = onChangeServer) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Change server", fontWeight = FontWeight.Medium)
            }
        }

        Text(
            text = if (otpToken.isNotEmpty() && otpToken.length < 6) "Enter all 6 digits"
            else "Code from your authenticator app",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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