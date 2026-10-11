package com.imnotndesh.truehub.ui
import com.imnotndesh.truehub.ui.login.TotpScreen
import com.imnotndesh.truehub.ui.adaptive.ProvideTrueHubWindow
import com.imnotndesh.truehub.ui.components.expressive.DetailHeroIcon
import com.imnotndesh.truehub.ui.components.expressive.DetailHeroCard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.components.ExpressiveIconButton
import com.imnotndesh.truehub.ui.components.HtmlDescription
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveBadge
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveEntryCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusDot
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveToggleRow
import com.imnotndesh.truehub.ui.components.expressive.InfoRow
import com.imnotndesh.truehub.ui.components.expressive.JobProgressView
import com.imnotndesh.truehub.ui.components.expressive.StatusTone
import com.imnotndesh.truehub.ui.services.containers.containerStatusIcon
import com.imnotndesh.truehub.ui.services.containers.containerStatusLabel
import com.imnotndesh.truehub.ui.services.containers.containerStatusTone
import com.imnotndesh.truehub.ui.services.containers.containerTypeContainerColor
import com.imnotndesh.truehub.ui.services.containers.containerTypeContentColor
import com.imnotndesh.truehub.ui.services.containers.containerTypeIcon
import com.imnotndesh.truehub.ui.services.containers.details.ContainerInfoPane
import com.imnotndesh.truehub.ui.services.vm.details.VmInfoPane
import com.imnotndesh.truehub.ui.services.vm.vmStatusIcon
import com.imnotndesh.truehub.ui.services.vm.vmStatusLabel
import com.imnotndesh.truehub.ui.services.vm.vmStatusTone
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme

private enum class MockPage(val label: String) {
    VmInfo("VM Info"),
    ContainerInfo("Container Info"),
    AppDetails("App Details"),
    Components("Components"),
    Dialogs("Dialogs"),
    Totp("TOTP")
}

class MockDataPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrueHubAppTheme {
                MockDataPreviewScreen()
            }
        }
    }
}

@Composable
private fun MockDataPreviewScreen() {
    var page by rememberSaveable { mutableStateOf(MockPage.VmInfo) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MockPage.entries) { entry ->
                FilterChip(
                    selected = entry == page,
                    onClick = { page = entry },
                    label = { Text(entry.label) }
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (page) {
                MockPage.VmInfo -> VmInfoPane(
                    vm = mockVm,
                    onClose = {},
                    showInlineHeader = false,
                    contentTopPadding = 16.dp,
                    topContent = {
                        MockHeroCard(
                            icon = Icons.Default.Computer,
                            iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                            iconContent = MaterialTheme.colorScheme.onTertiaryContainer,
                            title = mockVm.name,
                            subtitle = mockVm.description.ifEmpty { "ID: ${mockVm.id}" },
                            statusLabel = vmStatusLabel(mockVm.status.state),
                            statusTone = vmStatusTone(mockVm.status.state),
                            statusIcon = vmStatusIcon(mockVm.status.state),
                            primaryIcon = Icons.Default.Stop,
                            primaryLabel = "Stop",
                            secondaryIcon = Icons.Default.Edit,
                            secondaryLabel = "Edit configuration"
                        )
                        Spacer(Modifier.height(8.dp))
                        ExpressiveEntryCard(
                            icon = Icons.Default.Build,
                            title = "Devices",
                            subtitle = "Disks, NICs, display and passthrough",
                            onClick = {}
                        )
                        Spacer(Modifier.height(8.dp))
                        ExpressiveEntryCard(
                            icon = Icons.Default.Edit,
                            title = "Edit configuration",
                            subtitle = "CPU, memory, boot and behaviour",
                            onClick = {}
                        )
                    }
                )
                MockPage.ContainerInfo -> ContainerInfoPane(
                    container = mockContainer,
                    onClose = {},
                    showInlineHeader = false,
                    contentTopPadding = 16.dp,
                    topContent = {
                        MockHeroCard(
                            icon = containerTypeIcon(mockContainer.type),
                            iconContainer = containerTypeContainerColor(mockContainer.type),
                            iconContent = containerTypeContentColor(mockContainer.type),
                            title = mockContainer.name,
                            subtitle = "ID: ${mockContainer.id.take(12)}",
                            statusLabel = containerStatusLabel(mockContainer.status),
                            statusTone = containerStatusTone(mockContainer.status),
                            statusIcon = containerStatusIcon(mockContainer.status),
                            primaryIcon = Icons.Default.Stop,
                            primaryLabel = "Stop",
                            secondaryIcon = Icons.Default.Edit,
                            secondaryLabel = "Edit configuration"
                        )
                        Spacer(Modifier.height(8.dp))
                        ExpressiveEntryCard(
                            icon = Icons.Default.Build,
                            title = "Devices",
                            subtitle = "Attached hardware, NICs and passthrough",
                            onClick = {}
                        )
                        Spacer(Modifier.height(8.dp))
                        ExpressiveEntryCard(
                            icon = Icons.Default.Edit,
                            title = "Edit configuration",
                            subtitle = "CPU, memory, VNC and boot settings",
                            onClick = {}
                        )
                    }
                )
                MockPage.AppDetails -> AppDetailsPortraitMock(app = mockApp)
                MockPage.Components -> ComponentsPage()
                MockPage.Dialogs -> DialogsPage()
                MockPage.Totp -> ProvideTrueHubWindow {
                    TotpScreen(
                        username = "admin",
                        otpToken = "123",
                        onOtpChange = {},
                        onSubmit = {},
                        onBackToLogin = {},
                        onChangeServer = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun MockHeroCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainer: androidx.compose.ui.graphics.Color,
    iconContent: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    statusLabel: String,
    statusTone: StatusTone,
    statusIcon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryLabel: String,
    secondaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    secondaryLabel: String
) {
    DetailHeroCard(
        title = title,
        subtitle = subtitle,
        leading = {
            DetailHeroIcon(
                icon = icon,
                containerColor = iconContainer,
                contentColor = iconContent
            )
        },
        badge = { ExpressiveStatusChip(label = statusLabel, tone = statusTone, icon = statusIcon) },
        trailing = {
            ExpressiveIconButton(
                onClick = {},
                icon = primaryIcon,
                contentDescription = primaryLabel,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            Spacer(Modifier.size(8.dp))
            ExpressiveIconButton(
                onClick = {},
                icon = secondaryIcon,
                contentDescription = secondaryLabel,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    )
}

@Composable
private fun AppDetailsPortraitMock(app: Apps.AppQueryResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.metadata?.title ?: app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Version: ${app.humanVersion ?: app.version ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    ExpressiveBadge(
                        label = app.state.replaceFirstChar { it.uppercase() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.size(8.dp))
                ExpressiveIconButton(
                    onClick = {},
                    icon = Icons.Default.Stop,
                    contentDescription = "Stop ${app.metadata?.title ?: app.name}",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Spacer(Modifier.size(8.dp))
                ExpressiveIconButton(
                    onClick = {},
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit ${app.metadata?.title ?: app.name} Configuration",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpressiveEntryCard(
                icon = Icons.Default.Build,
                title = "Advanced Information",
                subtitle = "Workloads, ports, storage, notes and screenshots",
                onClick = {}
            )
            ExpressiveEntryCard(
                icon = Icons.Default.MonitorHeart,
                title = "App Resource Usage",
                subtitle = "Live CPU, memory, network and disk I/O",
                onClick = {}
            )
        }

        ExpressiveSection(title = "Basic Information", icon = Icons.Default.Info) {
            ExpressiveInfoCard {
                InfoRow(label = "App Name", value = app.metadata?.title ?: app.name)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                InfoRow(label = "ID", value = app.id)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                InfoRow(label = "Status", value = app.state.replaceFirstChar { it.uppercase() })
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                InfoRow(label = "Catalog", value = app.metadata?.train ?: "Unknown")
                if (app.upgrade_available) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(label = "Latest Version", value = app.latestVersion ?: "Update Available")
                }
            }
        }

        ExpressiveSection(title = "Description", icon = Icons.Default.Description) {
            ExpressiveInfoCard {
                HtmlDescription(
                    html = app.metadata?.description ?: "No description",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ExpressiveSection(title = "Categories & Tags", icon = Icons.Default.Label) {
            ExpressiveInfoCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    app.metadata?.categories.orEmpty().forEach { category ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = category.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        ExpressiveSection(title = "Danger Zone", icon = Icons.Default.Delete) {
            ExpressiveInfoCard {
                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete Application") }
            }
        }
    }
}

@Composable
private fun ComponentsPage() {
    var secretVisible by remember { mutableStateOf(false) }
    var port by remember { mutableStateOf("70000") }
    var toggle by remember { mutableStateOf(true) }
    val portError = port.trim().toIntOrNull()?.let { it !in 5900..65535 } ?: true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ExpressiveSection(title = "Choice field (T1)", icon = Icons.Default.Add) {
            ExpressiveInfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExpressiveChoiceField(
                        label = "Storage pool",
                        value = "tank",
                        placeholder = "Select a pool",
                        onClick = {}
                    )
                    ExpressiveChoiceField(
                        label = "Empty state",
                        value = "",
                        placeholder = "Select a pool",
                        onClick = {}
                    )
                }
            }
        }

        ExpressiveSection(title = "Status (T3)", icon = Icons.Default.CheckCircle) {
            ExpressiveInfoCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveStatusDot(tone = StatusTone.POSITIVE, label = "Running")
                    ExpressiveStatusDot(tone = StatusTone.NEGATIVE, label = "Error")
                    ExpressiveStatusDot(tone = StatusTone.NEUTRAL, label = "Stopped")
                    ExpressiveStatusChip(label = "Running", tone = StatusTone.POSITIVE, icon = Icons.Default.CheckCircle)
                    ExpressiveStatusChip(label = "Failed", tone = StatusTone.NEGATIVE, icon = Icons.Default.Error)
                }
            }
        }

        ExpressiveSection(title = "Toggle row (T2)", icon = Icons.Default.Pause) {
            ExpressiveInfoCard {
                ExpressiveToggleRow(
                    title = "Take snapshot before upgrade",
                    description = "Creates a ZFS snapshot that can be used for rollback",
                    checked = toggle,
                    onCheckedChange = { toggle = it }
                )
            }
        }

        ExpressiveSection(title = "Secret + validation (s5-01/s5-02)", icon = Icons.Default.PowerOff) {
            ExpressiveInfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExpressiveTextField(
                        value = "hunter2",
                        onValueChange = {},
                        label = "VNC password",
                        visualTransformation = if (secretVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { secretVisible = !secretVisible }) {
                                Icon(
                                    imageVector = if (secretVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (secretVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    ExpressiveTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = "VNC port",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = if (portError) {
                            { Text("Port must be between 5900 and 65535") }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        ExpressiveSection(title = "HTML description (s2-11)", icon = Icons.Default.CheckCircle) {
            ExpressiveInfoCard {
                HtmlDescription(
                    html = "<p>Plex organizes <b>all</b> of your personal media. " +
                        "See the <a href=\"https://plex.tv\">docs</a>.</p>",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ExpressiveSection(title = "Progress (T6)", icon = Icons.Default.Pause) {
            ExpressiveInfoCard {
                JobProgressView(
                    title = "Upgrading plex",
                    description = "Applying configuration changes",
                    progress = 42,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DialogsPage() {
    var dialog by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Confirmation dialogs added by the audit", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { dialog = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Uninstall app (s1-23)") }
        Button(onClick = { dialog = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Power off VM (s6-03/s6-17)") }
        Button(onClick = { dialog = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Rollback version (s3-16)") }
        Button(onClick = { dialog = 4 }, modifier = Modifier.fillMaxWidth()) { Text("Discard form (s4-29/s7-02)") }
        Button(onClick = { dialog = 5 }, modifier = Modifier.fillMaxWidth()) { Text("Proceed once on TLS error (P0-1)") }
    }

    when (dialog) {
        1 -> ConfirmDialog(
            title = "Uninstall Plex Media Server?",
            message = "This removes the application and its configuration. This cannot be undone.",
            confirmLabel = "Uninstall",
            onDismiss = { dialog = 0 }
        )
        2 -> ConfirmDialog(
            title = "Power off ubuntu-server?",
            message = "This immediately cuts power to the VM, like pulling the plug. Unsaved data may be lost.",
            confirmLabel = "Power Off",
            onDismiss = { dialog = 0 }
        )
        3 -> ConfirmDialog(
            title = "Roll back to 1.40.0?",
            message = "The app will be restored to this version. Changes made since then will be discarded.",
            confirmLabel = "Rollback",
            onDismiss = { dialog = 0 }
        )
        4 -> ConfirmDialog(
            title = "Discard changes?",
            message = "Your unsaved VM configuration will be lost.",
            confirmLabel = "Discard",
            onDismiss = { dialog = 0 }
        )
        5 -> AlertDialog(
            onDismissRequest = { dialog = 0 },
            title = { Text("Certificate problem") },
            text = {
                Text(
                    "The console presented a certificate that could not be verified. " +
                        "Proceeding once is insecure and may expose the session to interception."
                )
            },
            confirmButton = { TextButton(onClick = { dialog = 0 }) { Text("Proceed once") } },
            dismissButton = { TextButton(onClick = { dialog = 0 }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private val mockVm = Vm.VmQueryResponse(
    name = "ubuntu-server",
    description = "Ubuntu 24.04 LTS development VM",
    vcpus = 4,
    cores = 2,
    threads = 2,
    memory = 8192,
    min_memory = 4096,
    bootloader = "UEFI",
    autostart = true,
    time = "America/New_York",
    shutdown_timeout = 90,
    arch_type = "x86_64",
    machine_type = "q35",
    display_available = true,
    id = 3,
    status = Vm.VmStatus(state = "RUNNING"),
    enable_secure_boot = true
)

private val mockContainer = Virt.ContainerResponse(
    id = "a1b2c3d4e5f6",
    name = "nextcloud",
    type = Virt.Type.CONTAINER,
    status = Virt.Status.RUNNING,
    cpu = "2",
    memory = 2L * 1024 * 1024 * 1024,
    autostart = true,
    environment = mapOf("PUID" to "1000", "PGID" to "1000", "TZ" to "UTC"),
    aliases = listOf(Virt.Aliases(type = Virt.AliasType.INET, address = "192.168.1.50", netmask = 24)),
    image = Virt.Image(architecture = "amd64", os = "linux", release = "24.04", variant = "default", type = "container"),
    vnc_enabled = true,
    vnc_port = 5900,
    vnc_password = "secret",
    secure_boot = true,
    root_disk_size = 10,
    root_disk_io_bus = Virt.RootDiskIOBus.VIRTIO_BLK,
    storage_pool = "tank"
)

private val mockApp = Apps.AppQueryResponse(
    name = "plex",
    id = "plex",
    state = "running",
    upgrade_available = true,
    latestVersion = "1.40.2",
    humanVersion = "1.40.1",
    version = "1.40.1",
    metadata = Apps.Metadata(
        title = "Plex Media Server",
        train = "community",
        description = "Plex organizes all of your personal media so you can easily access it.",
        categories = listOf("media"),
        keywords = listOf("media", "streaming"),
        dateAdded = "2024-01-01",
        lastUpdate = "2024-06-01"
    )
)
