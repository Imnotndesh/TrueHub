package com.imnotndesh.truehub.ui.settings.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.helpers.ApkInstaller
import com.imnotndesh.truehub.data.models.DeviceAbi
import com.imnotndesh.truehub.data.models.UpdateInfo
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import dev.jeziellago.compose.markdowntext.MarkdownText

private const val RELEASES_URL = "https://github.com/Imnotndesh/TrueHub/releases"

@Composable
fun AppUpdateScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AppUpdateViewModel = viewModel(
        factory = AppUpdateViewModel.Factory(context.applicationContext as Application)
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        UnifiedScreenHeader(
            title = "App Updates",
            subtitle = "Keep TrueHub up to date",
            isLoading = uiState.isChecking,
            isRefreshing = false,
            error = uiState.error,
            onDismissError = { viewModel.handleEvent(AppUpdateEvent.DismissError) },
            onBackPressed = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InstalledVersionCard(uiState)

            if (uiState.isPlayStoreBuild) {
                PlayStoreManagedCard(onOpenStore = { openPlayStore(context) })
            } else {
                when {
                    uiState.isChecking -> Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    uiState.hasUpdate -> UpdateAvailableCard(
                        info = uiState.latest!!,
                        phase = uiState.phase,
                        progress = uiState.downloadProgress,
                        onPrimaryAction = {
                            val info = uiState.latest
                            when {
                                info == null -> Unit
                                info.asset == null -> openReleasePage(context, info)
                                !ApkInstaller.canInstall(context) ->
                                    context.startActivity(ApkInstaller.unknownSourcesIntent(context))
                                uiState.phase == UpdatePhase.READY ->
                                    viewModel.handleEvent(AppUpdateEvent.Install)
                                else ->
                                    viewModel.handleEvent(AppUpdateEvent.StartDownload)
                            }
                        },
                        onSkip = { viewModel.handleEvent(AppUpdateEvent.SkipVersion(uiState.latest!!.versionName)) }
                    )
                    else -> UpToDateCard(uiState)
                }

                UpdatePreferencesCard(
                    autoCheckEnabled = uiState.autoCheckEnabled,
                    wifiOnly = uiState.wifiOnly,
                    onCheckNow = { viewModel.handleEvent(AppUpdateEvent.Check) },
                    onAutoCheckChanged = { viewModel.handleEvent(AppUpdateEvent.SetAutoCheck(it)) },
                    onWifiOnlyChanged = { viewModel.handleEvent(AppUpdateEvent.SetWifiOnly(it)) }
                )
            }
        }
    }
}

@Composable
private fun InstalledVersionCard(state: AppUpdateUiState) {
    SettingsCard {
        Text(
            text = "Installed version",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = state.currentVersionName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Build ${state.currentVersionCode}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpToDateCard(state: AppUpdateUiState) {
    SettingsCard {
        Text(
            text = "You're up to date",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "TrueHub ${state.currentVersionName} is the latest version available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpdateAvailableCard(
    info: UpdateInfo,
    phase: UpdatePhase,
    progress: Int,
    onPrimaryAction: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SystemUpdateAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Update available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = info.versionName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = buildString {
                    append(abiLabel(info.abi))
                    if (info.asset != null) append(" · ${formatSize(info.asset.size)}")
                    if (info.asset == null) append(" · no compatible build")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            if (!info.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                MarkdownText(
                    markdown = info.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (phase == UpdatePhase.DOWNLOADING) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onPrimaryAction,
                    enabled = phase != UpdatePhase.DOWNLOADING
                ) {
                    Text(primaryLabel(phase, progress))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onSkip) {
                    Text("Skip this version")
                }
            }
        }
    }
}

@Composable
private fun UpdatePreferencesCard(
    autoCheckEnabled: Boolean,
    wifiOnly: Boolean,
    onCheckNow: () -> Unit,
    onAutoCheckChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit
) {
    SettingsCard {
        Text(
            text = "Update settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        ToggleRow(
            title = "Check automatically",
            subtitle = "Look for new releases once a day",
            checked = autoCheckEnabled,
            onCheckedChange = onAutoCheckChanged
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        ToggleRow(
            title = "Wi-Fi only",
            subtitle = "Download updates only on unmetered networks",
            checked = wifiOnly,
            onCheckedChange = onWifiOnlyChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCheckNow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check for updates")
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun PlayStoreManagedCard(onOpenStore: () -> Unit) {
    SettingsCard {
        Text(
            text = "Updates managed by Google Play",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "This build updates automatically through the Play Store. New versions install in the background.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenStore, modifier = Modifier.fillMaxWidth()) {
            Text("Open Google Play")
        }
    }
}

private fun openReleasePage(context: Context, info: UpdateInfo) {
    val url = info.releaseUrl ?: RELEASES_URL
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}

private fun openPlayStore(context: Context) {
    val appId = context.packageName
    val marketIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$appId".toUri())
    runCatching { context.startActivity(marketIntent) }.onFailure {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appId".toUri())
        )
    }
}

private fun primaryLabel(phase: UpdatePhase, progress: Int): String = when (phase) {
    UpdatePhase.IDLE -> "Download"
    UpdatePhase.DOWNLOADING -> "Downloading $progress%"
    UpdatePhase.READY -> "Install"
    UpdatePhase.ERROR -> "Retry download"
}

private fun abiLabel(abi: DeviceAbi): String = when (abi) {
    DeviceAbi.ARM64 -> "arm64"
    DeviceAbi.ARMV7A -> "armv7a"
    DeviceAbi.X86_64 -> "x86_64"
    DeviceAbi.UNIVERSAL -> "universal"
    DeviceAbi.UNSUPPORTED -> "unsupported"
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "unknown size"
    return "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
