package com.imnotndesh.truehub.ui.homepage.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.ui.background.WavyGradientBackground

// Sealed class to handle both share types
sealed class ShareType {
    data class Smb(val share: Shares.SmbShare) : ShareType()
    data class Nfs(val share: Shares.NfsShare) : ShareType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareInfoBottomSheet(
    shareType: ShareType,
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
            when (shareType) {
                is ShareType.Smb -> SmbShareInfoHeader(share = shareType.share, onDismiss = onDismiss)
                is ShareType.Nfs -> NfsShareInfoHeader(share = shareType.share, onDismiss = onDismiss)
            }

            // Share details content
            when (shareType) {
                is ShareType.Smb -> SmbShareDetailsContent(share = shareType.share)
                is ShareType.Nfs -> NfsShareDetailsContent(share = shareType.share)
            }
        }
    }
}

// SMB Share Components
@Composable
private fun SmbShareInfoHeader(
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

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (share.timemachine?:false) Icons.Default.Backup
                    else if (share.home?:false) Icons.Default.Home
                    else Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = share.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Shared Components
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

// NFS Share Header
@Composable
private fun NfsShareInfoHeader(
    share: Shares.NfsShare,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        WavyGradientBackground {
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

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = share.path.substringAfterLast('/').ifEmpty { "NFS Share" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
private fun SmbShareDetailsContent(
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
            if (!share.path_suffix.isNullOrEmpty()) {
                ShareInfoRow("Path Suffix", share.path_suffix)
            }
            if (!share.comment.isNullOrEmpty()) {
                ShareInfoRow("Description", share.comment)
            }
            if (!share.purpose.isNullOrEmpty()){
                ShareInfoRow("Purpose", share.purpose.ifEmpty { "General" })
            }else{
                ShareInfoRow("Purpose", "General")
            }
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
        if (!share.hostsallow.isNullOrEmpty()) {
            ShareInfoSection(
                title = "Network Access",
                icon = Icons.Default.NetworkCheck
            ) {
                ShareNetworkCard(share = share)
            }
        }

        // Time Machine Section
        if (share.timemachine?:false) {
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
                icon = Icons.AutoMirrored.Filled.Assignment
            ) {
                ShareAuditCard(share = share)
            }
        }

        // Additional Configuration
        if (!share.auxsmbconf.isNullOrEmpty()) {
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
private fun NfsShareDetailsContent(
    share: Shares.NfsShare
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
            ShareInfoRow("Path", share.path)
            ShareInfoRow("ID", share.id.toString())
            if (share.comment.isNotEmpty()) {
                ShareInfoRow("Description", share.comment)
            }
        }

        // Status Section
        ShareInfoSection(
            title = "Status",
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
            NfsFeatureChips(share = share)
        }

        // Network Access Section
        if (share.networks.isNotEmpty() || share.hosts.isNotEmpty()) {
            ShareInfoSection(
                title = "Network Access",
                icon = Icons.Default.NetworkCheck
            ) {
                NfsNetworkAccessCard(share = share)
            }
        }

        // User Mapping Section
        if (!share.maproot_user.isNullOrEmpty() || !share.mapall_user.isNullOrEmpty()) {
            ShareInfoSection(
                title = "User Mapping",
                icon = Icons.Default.Person
            ) {
                NfsUserMappingCard(share = share)
            }
        }

        // Security Section
        if (share.security.isNotEmpty()) {
            ShareInfoSection(
                title = "Security",
                icon = Icons.Default.Security
            ) {
                NfsSecurityCard(share = share)
            }
        }

        // Aliases Section
        if (share.aliases.isNotEmpty()) {
            ShareInfoSection(
                title = "Aliases",
                icon = Icons.AutoMirrored.Filled.Label
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        share.aliases.forEach { alias ->
                            Text(
                                text = "• $alias",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
            if (share.guestok == true) {
                FeatureChip("Guest Access", Icons.Default.PersonOutline)
            }
            if (share.ro == true) {
                FeatureChip("Read-Only", Icons.Default.Lock)
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (share.home == true) {
                FeatureChip("Home Share", Icons.Default.Home)
            }
            if (share.recyclebin == true) {
                FeatureChip("Recycle Bin", Icons.Default.Delete)
            }
            if (share.shadowcopy == true) {
                FeatureChip("Shadow Copy", Icons.Default.ContentCopy)
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (share.acl == true) {
                FeatureChip("ACL", Icons.Default.AdminPanelSettings)
            }
            if (share.streams == true) {
                FeatureChip("Streams", Icons.Default.Stream)
            }
            if (share.durablehandle == true) {
                FeatureChip("Durable Handle", Icons.Default.Link)
            }
        }
    }
}

@Composable
private fun NfsFeatureChips(share: Shares.NfsShare) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (share.ro) {
            FeatureChip("Read-Only", Icons.Default.Lock)
        }
        if (share.locked) {
            FeatureChip("Locked", Icons.Default.LockPerson)
        }
        if (share.expose_snapshots) {
            FeatureChip("Snapshots Exposed", Icons.Default.CameraAlt)
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
            AccessDetailRow("Read-Only", if (share.ro == true) "Yes" else "No", share.ro ?: false)
            AccessDetailRow("Guest Access", if (share.guestok == true) "Allowed" else "Not Allowed",
                share.guestok == true
            )
            AccessDetailRow("Browsable", if (share.browsable) "Yes" else "No", share.browsable)
            if (!share.vuid.isNullOrEmpty()) {
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
            AccessDetailRow("ACL Support", if (share.acl == true) "Enabled" else "Disabled",
                share.acl == true
            )
            AccessDetailRow("Alternate Data Streams", if (share.streams == true) "Enabled" else "Disabled",
                share.streams == true
            )
            AccessDetailRow("Durable Handles", if (share.durablehandle == true) "Enabled" else "Disabled",
                share.durablehandle == true
            )
            AccessDetailRow("Apple Name Mangling", if (share.aapl_name_mangling == true) "Enabled" else "Disabled",
                share.aapl_name_mangling == true
            )
            AccessDetailRow("Access Based Enumeration", if (share.abe == true) "Enabled" else "Disabled", share.abe?: false)
            AccessDetailRow("Shadow Copies", if (share.shadowcopy == true) "Enabled" else "Disabled",
                share.shadowcopy == true
            )
            AccessDetailRow("FSRVP", if (share.fsrvp == true) "Enabled" else "Disabled",
                share.fsrvp == true
            )
            AccessDetailRow("Recycle Bin", if (share.recyclebin == true) "Enabled" else "Disabled",
                share.recyclebin == true
            )
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
            if (!share.hostsallow.isNullOrEmpty()) {
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

            if (!share.hostsdeny.isNullOrEmpty()) {
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
            if (share.timemachine_quota != null && share.timemachine_quota > 0) {
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
private fun NfsNetworkAccessCard(share: Shares.NfsShare) {
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
            if (share.networks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Allowed Networks",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    share.networks.forEach { network ->
                        Text(
                            text = "• $network",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }

            if (share.hosts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Allowed Hosts",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    share.hosts.forEach { host ->
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
private fun NfsUserMappingCard(share: Shares.NfsShare) {
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
            if (share.maproot_user!!.isNotEmpty()) {
                ShareInfoRow("Map Root User", share.maproot_user)
            }
            if (share.maproot_group!!.isNotEmpty()) {
                ShareInfoRow("Map Root Group", share.maproot_group)
            }
            if (share.mapall_user!!.isNotEmpty()) {
                ShareInfoRow("Map All User", share.mapall_user)
            }
            if (share.mapall_group!!.isNotEmpty()) {
                ShareInfoRow("Map All Group", share.mapall_group)
            }
        }
    }
}

@Composable
private fun NfsSecurityCard(share: Shares.NfsShare) {
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
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = share.path,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (share.enabled == true)
                            Color(0xFF2E7D32).copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (share.enabled == true) "Active" else "Disabled",
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