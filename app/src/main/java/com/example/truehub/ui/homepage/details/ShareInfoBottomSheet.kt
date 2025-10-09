package com.example.truehub.ui.homepage.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truehub.data.models.Shares
import com.example.truehub.ui.background.WavyGradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareInfoBottomSheet(
    share: Shares.SmbShare,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            ShareInfoHeader(share = share, onDismiss = onDismiss)

            // Share details content
            ShareDetailsContent(share = share)
        }
    }
}

@Composable
private fun ShareInfoHeader(
    share: Shares.SmbShare,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        WavyGradientBackground {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Share info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share icon
                Icon(
                    imageVector = if (share.timemachine) Icons.Default.Backup
                    else if (share.home) Icons.Default.Home
                    else Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )

                // Share name
                Text(
                    text = share.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Status and path
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (share.enabled)
                            Color(0xFF2E7D32).copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (share.enabled) "Active" else "Disabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = share.path,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareDetailsContent(
    share: Shares.SmbShare
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Basic Information Section
        ShareInfoSection(
            title = "Basic Information",
            icon = Icons.Default.Info
        ) {
            ShareInfoRow("Share Name", share.name)
            ShareInfoRow("Path", share.path)
            if (share.path_suffix.isNotEmpty()) {
                ShareInfoRow("Path Suffix", share.path_suffix)
            }
            if (share.comment.isNotEmpty()) {
                ShareInfoRow("Description", share.comment)
            }
            ShareInfoRow("Purpose", share.purpose.ifEmpty { "General" })
        }

        // Status & Features Section
        ShareInfoSection(
            title = "Status & Features",
            icon = Icons.Default.ToggleOn
        ) {
            ShareStatusCard(
                title = "Share Status",
                status = if (share.enabled) "Enabled" else "Disabled",
                isPositive = share.enabled,
                icon = Icons.Default.PowerSettingsNew
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feature chips
            ShareFeatureChips(share = share)
        }

        // Access Control Section
        ShareInfoSection(
            title = "Access Control",
            icon = Icons.Default.Security
        ) {
            ShareAccessCard(share = share)
        }

        // Advanced Settings Section
        ShareInfoSection(
            title = "Advanced Settings",
            icon = Icons.Default.Settings
        ) {
            ShareAdvancedCard(share = share)
        }

        // Network Access Section
        if (share.hostsallow.isNotEmpty() || share.hostsdeny.isNotEmpty()) {
            ShareInfoSection(
                title = "Network Access",
                icon = Icons.Default.NetworkCheck
            ) {
                ShareNetworkCard(share = share)
            }
        }

        // Time Machine Section
        if (share.timemachine) {
            ShareInfoSection(
                title = "Time Machine",
                icon = Icons.Default.Backup
            ) {
                ShareTimeMachineCard(share = share)
            }
        }

        // Audit Section
        if (share.audit.enable) {
            ShareInfoSection(
                title = "Audit Settings",
                icon = Icons.Default.Assignment
            ) {
                ShareAuditCard(share = share)
            }
        }

        // Additional Configuration
        if (share.auxsmbconf.isNotEmpty()) {
            ShareInfoSection(
                title = "Additional Configuration",
                icon = Icons.Default.Code
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = share.auxsmbconf,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareInfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
private fun ShareInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ShareStatusCard(
    title: String,
    status: String,
    isPositive: Boolean,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isPositive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPositive) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareFeatureChips(share: Shares.SmbShare) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (share.browsable) {
                FeatureChip("Browsable", Icons.Default.Visibility)
            }
            if (share.guestok) {
                FeatureChip("Guest Access", Icons.Default.PersonOutline)
            }
            if (share.ro) {
                FeatureChip("Read-Only", Icons.Default.Lock)
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (share.home) {
                FeatureChip("Home Share", Icons.Default.Home)
            }
            if (share.recyclebin) {
                FeatureChip("Recycle Bin", Icons.Default.Delete)
            }
            if (share.shadowcopy) {
                FeatureChip("Shadow Copy", Icons.Default.ContentCopy)
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (share.acl) {
                FeatureChip("ACL", Icons.Default.AdminPanelSettings)
            }
            if (share.streams) {
                FeatureChip("Streams", Icons.Default.Stream)
            }
            if (share.durablehandle) {
                FeatureChip("Durable Handle", Icons.Default.Link)
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ShareAccessCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessDetailRow("Read-Only", if (share.ro) "Yes" else "No", share.ro)
            AccessDetailRow("Guest Access", if (share.guestok) "Allowed" else "Not Allowed", share.guestok)
            AccessDetailRow("Browsable", if (share.browsable) "Yes" else "No", share.browsable)
            if (share.vuid.isNotEmpty()) {
                AccessDetailRow("VUID", share.vuid, true)
            }
            AccessDetailRow("Locked", if (share.locked) "Yes" else "No", share.locked)
        }
    }
}

@Composable
private fun ShareAdvancedCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessDetailRow("ACL Support", if (share.acl) "Enabled" else "Disabled", share.acl)
            AccessDetailRow("Alternate Data Streams", if (share.streams) "Enabled" else "Disabled", share.streams)
            AccessDetailRow("Durable Handles", if (share.durablehandle) "Enabled" else "Disabled", share.durablehandle)
            AccessDetailRow("Apple Name Mangling", if (share.aapl_name_mangling) "Enabled" else "Disabled", share.aapl_name_mangling)
            AccessDetailRow("Access Based Enumeration", if (share.abe) "Enabled" else "Disabled", share.abe)
            AccessDetailRow("Shadow Copies", if (share.shadowcopy) "Enabled" else "Disabled", share.shadowcopy)
            AccessDetailRow("FSRVP", if (share.fsrvp) "Enabled" else "Disabled", share.fsrvp)
            AccessDetailRow("Recycle Bin", if (share.recyclebin) "Enabled" else "Disabled", share.recyclebin)
        }
    }
}

@Composable
private fun ShareNetworkCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (share.hostsallow.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Allowed Hosts",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    share.hostsallow.forEach { host ->
                        Text(
                            text = "• $host",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }

            if (share.hostsdeny.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Denied Hosts",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    share.hostsdeny.forEach { host ->
                        Text(
                            text = "• $host",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareTimeMachineCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Time Machine Enabled",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (share.timemachine_quota > 0) {
                Text(
                    text = "Quota: ${share.timemachine_quota} GB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "No quota limit set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ShareAuditCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Auditing Enabled",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (share.audit.watch_list.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Watch List:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    share.audit.watch_list.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            if (share.audit.ignore_list.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Ignore List:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    share.audit.ignore_list.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessDetailRow(
    label: String,
    value: String,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isEnabled) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF2E7D32)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}