package com.imnotndesh.truehub.ui.homepage.details

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import com.imnotndesh.truehub.ui.components.ExpressiveIconButton
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

sealed class ShareType {
    data class Smb(val share: Shares.SmbShare) : ShareType()
    data class Nfs(val share: Shares.NfsShare) : ShareType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareInfoScreen(
    shareType: ShareType,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit
) {
    val viewModel: ShareInfoViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(shareType) { viewModel.load(shareType) }
    LaunchedEffect(state.deleted) { if (state.deleted) onNavigateBack() }

    val smb = state.smbShare ?: (shareType as? ShareType.Smb)?.share
    val nfs = state.nfsShare ?: (shareType as? ShareType.Nfs)?.share

    val title = when {
        smb != null -> smb.name
        nfs != null -> nfs.path.substringAfterLast('/').ifEmpty { "NFS Share" }
        else -> "Share"
    }
    val subtitle = if (smb != null) "SMB Share" else "NFS Share"

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = title,
            subtitle = subtitle,
            isLoading = state.isLoading,
            isRefreshing = state.isLoading,
            error = state.error,
            onRefresh = { viewModel.refresh() },
            onDismissError = { viewModel.dismissError() },
            manager = manager,
            onBackPressed = onNavigateBack,
            trailingActions = {
                ExpressiveIconButton(
                    onClick = { showDeleteDialog = true },
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete share",
                    enabled = !state.isDeleting,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when {
                    smb != null -> ShareHeroCard(
                        name = smb.name,
                        path = smb.path,
                        enabled = smb.enabled,
                        icon = if (smb.timemachine == true) Icons.Default.Backup
                        else if (smb.home == true) Icons.Default.Home
                        else Icons.Default.Folder,
                        badgeLabel = "SMB"
                    )
                    nfs != null -> ShareHeroCard(
                        name = nfs.path.substringAfterLast('/').ifEmpty { "NFS Share" },
                        path = nfs.path,
                        enabled = nfs.enabled,
                        icon = Icons.Default.Storage,
                        badgeLabel = "NFS"
                    )
                }
            }

            item { ShareStatusBanner(enabled = smb?.enabled ?: nfs?.enabled ?: false) }

            item {
                when {
                    smb != null -> SmbShareSections(share = smb)
                    nfs != null -> NfsShareSections(share = nfs)
                }
            }

            state.smbAcl?.let { acl ->
                item { SmbAclSection(acl = acl) }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete share?") },
            text = { Text("This removes the share configuration. Data on disk is not deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SmbAclSection(acl: Shares.SmbAcl) {
    ShareInfoSection(title = "Share ACL", icon = Icons.Default.Security) {
        if (acl.shareAcl.isEmpty()) {
            Text(
                text = "No ACL entries.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                acl.shareAcl.forEach { entry ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = entry.aeWhoStr ?: entry.aeWhoSid ?: "Unknown",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = listOfNotNull(entry.aeType, entry.aePerm).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
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
private fun ShareHeroCard(
    name: String,
    path: String,
    enabled: Boolean,
    icon: ImageVector,
    badgeLabel: String
) {
    val enabledColor by animateColorAsState(
        targetValue = if (enabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroEnabled"
    )

    WavyGradientBackground {
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = path,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShareBadge(
                            label = badgeLabel,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        ShareBadge(
                            label = if (enabled) "Active" else "Disabled",
                            containerColor = enabledColor.copy(alpha = 0.15f),
                            contentColor = enabledColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareBadge(label: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ShareStatusBanner(enabled: Boolean) {
    val bannerColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bannerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bannerContent"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Share Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Text(
                text = if (enabled) "Enabled & Active" else "Disabled",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SmbShareSections(share: Shares.SmbShare) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ShareInfoSection(title = "Basic Information", icon = Icons.Default.Info) {
            ExpressiveInfoGrid(
                items = buildList {
                    add("Share Name" to share.name)
                    add("Path" to share.path)
                    if (!share.path_suffix.isNullOrEmpty()) add("Path Suffix" to share.path_suffix)
                    if (!share.comment.isNullOrEmpty()) add("Description" to share.comment)
                    add("Purpose" to share.purpose!!.ifEmpty { "General" })
                }
            )
        }

        ShareInfoSection(title = "Features", icon = Icons.Default.ToggleOn) {
            ShareFeatureChips(share = share)
        }

        ShareInfoSection(title = "Access Control", icon = Icons.Default.Security) {
            ShareAccessCard(share = share)
        }

        ShareInfoSection(title = "Advanced Settings", icon = Icons.Default.Settings) {
            ShareAdvancedCard(share = share)
        }

        if (!share.hostsallow.isNullOrEmpty()) {
            ShareInfoSection(title = "Network Access", icon = Icons.Default.NetworkCheck) {
                ShareNetworkCard(share = share)
            }
        }

        if (share.timemachine == true) {
            ShareInfoSection(title = "Time Machine", icon = Icons.Default.Backup) {
                ShareTimeMachineCard(share = share)
            }
        }

        if (share.audit.enable) {
            ShareInfoSection(title = "Audit Settings", icon = Icons.AutoMirrored.Filled.Assignment) {
                ShareAuditCard(share = share)
            }
        }

        if (!share.auxsmbconf.isNullOrEmpty()) {
            ShareInfoSection(title = "Additional Configuration", icon = Icons.Default.Code) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
private fun NfsShareSections(share: Shares.NfsShare) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ShareInfoSection(title = "Basic Information", icon = Icons.Default.Info) {
            ExpressiveInfoGrid(
                items = buildList {
                    add("Path" to share.path)
                    add("ID" to share.id.toString())
                    if (share.comment.isNotEmpty()) add("Description" to share.comment)
                }
            )
        }

        ShareInfoSection(title = "Features", icon = Icons.Default.ToggleOn) {
            NfsFeatureChips(share = share)
        }

        if (share.networks.isNotEmpty() || share.hosts.isNotEmpty()) {
            ShareInfoSection(title = "Network Access", icon = Icons.Default.NetworkCheck) {
                NfsNetworkAccessCard(share = share)
            }
        }

        if (!share.maproot_user.isNullOrEmpty() || !share.mapall_user.isNullOrEmpty()) {
            ShareInfoSection(title = "User Mapping", icon = Icons.Default.Person) {
                NfsUserMappingCard(share = share)
            }
        }

        if (share.security.isNotEmpty()) {
            ShareInfoSection(title = "Security", icon = Icons.Default.Security) {
                NfsSecurityCard(share = share)
            }
        }

        if (share.aliases.isNotEmpty()) {
            ShareInfoSection(title = "Aliases", icon = Icons.AutoMirrored.Filled.Label) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
private fun ExpressiveInfoGrid(items: List<Pair<String, String>>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            items.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
private fun ShareInfoRow(label: String, value: String) {
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
private fun AccessDetailRow(label: String, value: String, isEnabled: Boolean) {
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
private fun ShareFeatureChips(share: Shares.SmbShare) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (share.browsable) FeatureChip("Browsable", Icons.Default.Visibility)
            if (share.guestok == true) FeatureChip("Guest Access", Icons.Default.PersonOutline)
            if (share.ro == true) FeatureChip("Read-Only", Icons.Default.Lock)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (share.home == true) FeatureChip("Home Share", Icons.Default.Home)
            if (share.recyclebin == true) FeatureChip("Recycle Bin", Icons.Default.Delete)
            if (share.shadowcopy == true) FeatureChip("Shadow Copy", Icons.Default.ContentCopy)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (share.acl == true) FeatureChip("ACL", Icons.Default.AdminPanelSettings)
            if (share.streams == true) FeatureChip("Streams", Icons.Default.Stream)
            if (share.durablehandle == true) FeatureChip("Durable Handle", Icons.Default.Link)
        }
    }
}

@Composable
private fun NfsFeatureChips(share: Shares.NfsShare) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (share.ro) FeatureChip("Read-Only", Icons.Default.Lock)
        if (share.locked) FeatureChip("Locked", Icons.Default.LockPerson)
        if (share.expose_snapshots) FeatureChip("Snapshots Exposed", Icons.Default.CameraAlt)
    }
}

@Composable
private fun ShareAccessCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessDetailRow("Read-Only", if (share.ro == true) "Yes" else "No", share.ro ?: false)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Guest Access", if (share.guestok == true) "Allowed" else "Not Allowed", share.guestok == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Browsable", if (share.browsable) "Yes" else "No", share.browsable)
            if (!share.vuid.isNullOrEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                AccessDetailRow("VUID", share.vuid, true)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Locked", if (share.locked) "Yes" else "No", share.locked)
        }
    }
}

@Composable
private fun ShareAdvancedCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccessDetailRow("ACL Support", if (share.acl == true) "Enabled" else "Disabled", share.acl == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Alternate Data Streams", if (share.streams == true) "Enabled" else "Disabled", share.streams == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Durable Handles", if (share.durablehandle == true) "Enabled" else "Disabled", share.durablehandle == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Apple Name Mangling", if (share.aapl_name_mangling == true) "Enabled" else "Disabled", share.aapl_name_mangling == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Access Based Enumeration", if (share.abe == true) "Enabled" else "Disabled", share.abe ?: false)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Shadow Copies", if (share.shadowcopy == true) "Enabled" else "Disabled", share.shadowcopy == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("FSRVP", if (share.fsrvp == true) "Enabled" else "Disabled", share.fsrvp == true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AccessDetailRow("Recycle Bin", if (share.recyclebin == true) "Enabled" else "Disabled", share.recyclebin == true)
        }
    }
}

@Composable
private fun ShareNetworkCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!share.hostsallow.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Allowed Hosts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    share.hostsallow.forEach { host ->
                        Text("• $host", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }
            if (!share.hostsdeny.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cancel, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Denied Hosts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    share.hostsdeny.forEach { host ->
                        Text("• $host", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareTimeMachineCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Backup, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Time Machine Enabled", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            if (share.timemachine_quota != null && share.timemachine_quota > 0) {
                Text("Quota: ${share.timemachine_quota} GB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
            } else {
                Text("No quota limit set", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun ShareAuditCard(share: Shares.SmbShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Auditing Enabled", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (share.audit.watch_list.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Watch List:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    share.audit.watch_list.forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
            if (share.audit.ignore_list.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Ignore List:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    share.audit.ignore_list.forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NfsNetworkAccessCard(share: Shares.NfsShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (share.networks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NetworkCheck, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Allowed Networks", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    share.networks.forEach { network ->
                        Text("• $network", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }
            if (share.hosts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Computer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Allowed Hosts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    share.hosts.forEach { host ->
                        Text("• $host", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NfsUserMappingCard(share: Shares.NfsShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!share.maproot_user.isNullOrEmpty()) ShareInfoRow("Map Root User", share.maproot_user)
            if (!share.maproot_group.isNullOrEmpty()) ShareInfoRow("Map Root Group", share.maproot_group)
            if (!share.mapall_user.isNullOrEmpty()) ShareInfoRow("Map All User", share.mapall_user)
            if (!share.mapall_group.isNullOrEmpty()) ShareInfoRow("Map All Group", share.mapall_group)
        }
    }
}

@Composable
private fun NfsSecurityCard(share: Shares.NfsShare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                Text("Security Flavors", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            share.security.forEach { flavor ->
                Surface(
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = flavor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}