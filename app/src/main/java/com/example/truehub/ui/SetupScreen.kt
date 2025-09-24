package com.example.truehub.ui

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.ui.background.AnimatedWavyGradientBackground
import kotlinx.coroutines.launch
import java.util.regex.Pattern

sealed class ConnectivityStatus {
    object Connected : ConnectivityStatus()
    object Disconnected : ConnectivityStatus()
    object Checking : ConnectivityStatus()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    apiManager: TrueNASApiManager? = null,
    onConfigured: (String, Boolean) -> Unit
) {
    var serverUrl by remember { mutableStateOf("") }
    var insecure by remember { mutableStateOf(false) }
    var connectivityStatus by remember { mutableStateOf(ConnectivityStatus.Checking) }
    var isConfiguring by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val urlValidation = validateUrl(serverUrl)
    val isValidUrl = urlValidation.isValid
    val hasInternet = connectivityStatus is ConnectivityStatus.Connected

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
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Welcome to\n")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("TrueHub")
                            }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 38.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Enter your TrueNAS UI URL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // URL Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // URL Input Field
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("TrueNAS Server URL") },
                            placeholder = { Text("ws://192.168.1.100") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Web,
                                    contentDescription = "Server URL"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (serverUrl.isNotEmpty() && !isValidUrl) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                unfocusedBorderColor = if (serverUrl.isNotEmpty() && !isValidUrl) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                }
                            ),
                            isError = serverUrl.isNotEmpty() && !isValidUrl,
                            enabled = !isConfiguring
                        )

                        // URL Format Help
                        if (serverUrl.isEmpty()) {
                            Text(
                                text = "Enter your TrueNAS server URL (e.g., ws://192.168.1.100)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // Validation Messages
                        urlValidation.warnings.forEach { warning ->
                            ValidationMessage(
                                message = warning,
                                isError = false
                            )
                        }

                        urlValidation.errors.forEach { error ->
                            ValidationMessage(
                                message = error,
                                isError = true
                            )
                        }

                        // Security Toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = if (insecure) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Allow insecure connections",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "Enable for self-signed certificates",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                                    )
                                }
                                Switch(
                                    checked = insecure,
                                    onCheckedChange = { insecure = it },
                                    enabled = !isConfiguring
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Next Button (Bottom Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {

                            isConfiguring = true
                            scope.launch {
                                try {
                                    val formattedUrl = formatUrl(serverUrl)

                                    // If we have the API manager, test the connection first
                                    if (apiManager != null) {
                                        // Configure the API manager with new settings
                                        apiManager.configure(context,formattedUrl, insecure)

                                        // Test the connection
                                        val isConnected = try {
                                            apiManager.connect()
                                            apiManager.isConnected()
                                        } catch (e: Exception) {
                                            false
                                        }

                                        if (isConnected) {
                                            // Connection successful, proceed with navigation
                                            onConfigured(formattedUrl, insecure)
                                        } else {
                                            // Connection failed, show error and reset state
                                            isConfiguring = false
                                            // You might want to show an error message here
                                        }
                                    } else {
                                        // No API manager provided, just pass the configuration
                                        onConfigured(formattedUrl, insecure)
                                    }
                                } catch (e: Exception) {
                                    isConfiguring = false
                                }
                            }
                        },
                        enabled = isValidUrl && serverUrl.isNotEmpty() && !isConfiguring,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        if (isConfiguring) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text("Connecting...")
                            }
                        } else {
                            Text(
                                "Next",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectivityStatusCard(
    status: ConnectivityStatus,
    onRetryClick: () -> Unit
) {
    val (statusText, statusColor, icon, showRetryButton) = when (status) {
        is ConnectivityStatus.Connected ->
            Tuple4("Connected to Internet", MaterialTheme.colorScheme.primary, Icons.Default.Wifi, false)
        is ConnectivityStatus.Disconnected ->
            Tuple4("No Internet Connection", MaterialTheme.colorScheme.error, Icons.Default.WifiOff, true)
        is ConnectivityStatus.Checking ->
            Tuple4("Checking Connection...", MaterialTheme.colorScheme.tertiary, Icons.Default.Wifi, false)
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
                if (status is ConnectivityStatus.Checking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = statusColor
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

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

// Helper data class for destructuring
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

data class UrlValidation(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

private fun validateUrl(url: String): UrlValidation {
    if (url.isEmpty()) {
        return UrlValidation(false, emptyList(), emptyList())
    }

    val errors = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    // Check protocol
    when {
        url.startsWith("ws://") -> {
            warnings.add("Using insecure WebSocket (ws://). Consider using wss:// for production.")
        }
        url.startsWith("wss://") -> {
            // Good, secure connection
        }
        url.startsWith("http://") || url.startsWith("https://") -> {
            errors.add("Use WebSocket protocol (ws:// or wss://) instead of HTTP.")
        }
        else -> {
            errors.add("URL must start with ws:// or wss://")
        }
    }

    // Extract the part after protocol
    val urlWithoutProtocol = url.substringAfter("://")

    if (urlWithoutProtocol.isEmpty()) {
        errors.add("URL is incomplete after protocol")
        return UrlValidation(false, errors, warnings)
    }

    // Split into host and path parts
    val parts = urlWithoutProtocol.split("/", limit = 2)
    val hostPart = parts[0]
    val pathPart = if (parts.size > 1) "/${parts[1]}" else ""

    // Validate host part (IP or domain)
    if (hostPart.isEmpty()) {
        errors.add("Host/IP address is missing")
    } else {
        // Check if it looks like an IP address
        val ipPattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})(:\\d+)?$")
        val domainPattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\..*$")
        val localhostPattern = Pattern.compile("^localhost(:\\d+)?$", Pattern.CASE_INSENSITIVE)

        when {
            ipPattern.matcher(hostPart).matches() -> {
                // Validate IP octets
                val ipParts = hostPart.split(":")[0].split(".")
                ipParts.forEach { octet ->
                    val num = octet.toIntOrNull()
                    if (num == null || num > 255) {
                        errors.add("Invalid IP address: $hostPart")
                    }
                }
            }
            localhostPattern.matcher(hostPart).matches() -> {
                // localhost is valid
            }
            domainPattern.matcher(hostPart).matches() || hostPart.contains(".") -> {
                // Looks like a domain name
                if (hostPart.count { it == '.' } < 1 && !hostPart.contains("localhost")) {
                    warnings.add("Domain name might be incomplete")
                }
            }
            hostPart.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) -> {
                errors.add("Incomplete IP address (missing last octet)")
            }
            hostPart.matches(Regex("^\\d{1,3}\\.\\d{1,3}$")) -> {
                errors.add("Incomplete IP address (missing two octets)")
            }
            hostPart.matches(Regex("^\\d{1,3}$")) -> {
                errors.add("Incomplete IP address (only one octet provided)")
            }
            else -> {
                errors.add("Invalid host format. Use IP address, domain name, or localhost")
            }
        }
    }

    // Check if API path is present
    if (!pathPart.contains("/api/current")) {
        warnings.add("URL will be automatically formatted to include /api/current")
    }

    return UrlValidation(
        isValid = errors.isEmpty(),
        errors = errors,
        warnings = warnings
    )
}

private fun formatUrl(url: String): String {
    var formattedUrl = url.trim()

    // Remove trailing slash if present
    if (formattedUrl.endsWith("/")) {
        formattedUrl = formattedUrl.dropLast(1)
    }

    // Add /api/current if not present
    if (!formattedUrl.contains("/api/current")) {
        formattedUrl += "/api/current"
    }

    return formattedUrl
}