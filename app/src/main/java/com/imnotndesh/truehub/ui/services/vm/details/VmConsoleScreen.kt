package com.imnotndesh.truehub.ui.services.vm.details

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

private data class SslChallenge(
    val handler: SslErrorHandler,
    val error: SslError?
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VmConsoleScreen(
    vmId: Int,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    viewModel: VmConsoleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var sslChallenge by remember { mutableStateOf<SslChallenge?>(null) }
    var pageLoading by remember { mutableStateOf(false) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val notReady = state.notReadyReason
    val canStart = notReady?.contains("Start the VM", ignoreCase = true) == true

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = state.vm?.name ?: "VM console",
                subtitle = "Console",
                isLoading = state.isLoading,
                isRefreshing = false,
                error = state.error,
                onDismissError = viewModel::clearError,
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            when {
                state.isLoading -> LoadingScreen("Connecting to console")
                notReady != null -> ConsoleMessage(
                    message = notReady,
                    actionLabel = if (canStart) "Start VM" else "Retry",
                    onAction = { if (canStart) viewModel.startVm() else viewModel.load() }
                )
                pageError != null -> ConsoleMessage(
                    message = pageError!!,
                    actionLabel = "Retry",
                    onAction = {
                        pageError = null
                        webView?.reload()
                    }
                )
                state.uri == null -> ConsoleMessage(
                    message = state.error ?: "Console is unavailable",
                    actionLabel = "Retry",
                    onAction = viewModel::load
                )
                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "VM console"
                            liveRegion = LiveRegionMode.Polite
                        }
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(
                                        view: WebView?,
                                        url: String?,
                                        favicon: android.graphics.Bitmap?
                                    ) {
                                        pageLoading = true
                                        pageError = null
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        pageLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame != true) return
                                        pageLoading = false
                                        pageError = error?.description?.toString()
                                            ?.takeIf { it.isNotBlank() }
                                            ?: "The console failed to load"
                                    }

                                    override fun onReceivedSslError(
                                        view: WebView?,
                                        handler: SslErrorHandler?,
                                        error: SslError?
                                    ) {
                                        if (handler == null) return
                                        sslChallenge = SslChallenge(handler, error)
                                    }
                                }
                                loadUrl(state.uri!!)
                            }.also { webView = it }
                        },
                        onRelease = { view ->
                            view.stopLoading()
                            view.destroy()
                            webView = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (pageLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

    sslChallenge?.let { challenge ->
        AlertDialog(
            onDismissRequest = {
                challenge.handler.cancel()
                sslChallenge = null
            },
            title = { Text("Certificate problem") },
            text = {
                Text(
                    "The console presented a certificate that could not be verified. " +
                        "Proceeding once is insecure and may expose the session to interception."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        challenge.handler.proceed()
                        sslChallenge = null
                    }
                ) { Text("Proceed once") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        challenge.handler.cancel()
                        sslChallenge = null
                    }
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ConsoleMessage(message: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Monitor,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        Button(
            onClick = onAction,
            modifier = Modifier.padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.large
        ) { Text(actionLabel) }
    }
}
