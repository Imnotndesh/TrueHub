package com.imnotndesh.truehub.ui.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.NavbarDestination
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.theme.AppTheme
import com.imnotndesh.truehub.ui.theme.ForestDarkColors
import com.imnotndesh.truehub.ui.theme.ForestLightColors
import com.imnotndesh.truehub.ui.theme.LavenderDarkColors
import com.imnotndesh.truehub.ui.theme.LavenderLightColors
import com.imnotndesh.truehub.ui.theme.MonochromeDarkColors
import com.imnotndesh.truehub.ui.theme.MonochromeLightColors
import com.imnotndesh.truehub.ui.theme.OceanDarkColors
import com.imnotndesh.truehub.ui.theme.OceanLightColors
import com.imnotndesh.truehub.ui.theme.SunsetDarkColors
import com.imnotndesh.truehub.ui.theme.SunsetLightColors
import com.imnotndesh.truehub.ui.theme.TrueHubDarkColors
import com.imnotndesh.truehub.ui.theme.TrueHubLightColors

@Composable
fun ThemeScreen(
    currentTheme: AppTheme,
    isBlackModeEnabled: Boolean,
    onThemeSelected: (AppTheme) -> Unit,
    onBlackModeToggled: (Boolean) -> Unit,
    onNavigateBack: () -> Unit = {},
    manager: TrueNASApiManager?,
    userKey: String
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    var blackMode by remember { mutableStateOf(isBlackModeEnabled) }
    val personalization by PersonalizationManager.state.collectAsState()
    val enabledDestinations = personalization.navbarDestinations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        UnifiedScreenHeader(
            title = "Theme",
            subtitle = "Choose your color scheme",
            onDismissError = {},
            onBackPressed = onNavigateBack,
            isLoading = false,
            isRefreshing = false,
            error = null,
            manager = manager!!,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Color Scheme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    ThemePreviewCard(
                        theme = theme,
                        isSelected = theme == selectedTheme,
                        onClick = {
                            selectedTheme = theme
                            onThemeSelected(theme)
                        }
                    )
                }
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Black",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Uses less power on AMOLED screens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = blackMode,
                        onCheckedChange = { isChecked ->
                            blackMode = isChecked
                            onBlackModeToggled(isChecked)
                        }
                    )
                }
            }

            // ── Navbar customization ──────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Navbar Customization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose which items appear on the bottom navigation bar and their order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Compact mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Compact mode",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Hide labels, leave slightly larger icons",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = personalization.compactNav,
                            onCheckedChange = { compact ->
                                PersonalizationManager.saveCompactNav(context, userKey, compact)
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Destinations
                    Text(
                        "Destinations",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Home is always present and locked first.
                    DestinationRow(
                        title = NavbarDestination.HOME.title,
                        enabled = true,
                        canToggle = false,
                        canMoveUp = false,
                        canMoveDown = false,
                        onToggle = {},
                        onMoveUp = {},
                        onMoveDown = {}
                    )

                    val optional = PersonalizationManager.availableDestinations.filter { !it.isRequired }
                    optional.forEachIndexed { index, destination ->
                        val selected = enabledDestinations.contains(destination)
                        val selectedIndex = enabledDestinations.indexOfFirst { it == destination }
                        DestinationRow(
                            title = destination.title,
                            enabled = selected,
                            canToggle = true,
                            canMoveUp = selected && selectedIndex > 1,
                            canMoveDown = selected && selectedIndex < enabledDestinations.lastIndex,
                            onToggle = {
                                val updated = if (selected) {
                                    enabledDestinations - destination
                                } else {
                                    enabledDestinations + destination
                                }
                                PersonalizationManager.saveNavbar(context, userKey, updated)
                            },
                            onMoveUp = {
                                val updated = moveDestination(enabledDestinations, destination, -1)
                                PersonalizationManager.saveNavbar(context, userKey, updated)
                            },
                            onMoveDown = {
                                val updated = moveDestination(enabledDestinations, destination, 1)
                                PersonalizationManager.saveNavbar(context, userKey, updated)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun moveDestination(
    list: List<NavbarDestination>,
    destination: NavbarDestination,
    delta: Int
): List<NavbarDestination> {
    val mutable = list.toMutableList()
    val index = mutable.indexOfFirst { it == destination }
    if (index < 0) return list
    val target = index + delta
    if (target < 1 || target >= mutable.size) return list // never move over required Home (index 0)
    val item = mutable.removeAt(index)
    mutable.add(target, item)
    return mutable
}

@Composable
private fun DestinationRow(
    title: String,
    enabled: Boolean,
    canToggle: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        if (canMoveUp || canMoveDown) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move up",
                    tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move down",
                    tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            enabled = canToggle,
            modifier = Modifier.padding(start = if (canMoveUp || canMoveDown) 0.dp else 8.dp)
        )
    }
}

@Composable
private fun ThemePreviewCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()


    val samplePrimary = when(theme) {
        AppTheme.DYNAMIC -> MaterialTheme.colorScheme.primary
        AppTheme.TRUEHUB -> if (isSystemDark) TrueHubDarkColors.primary else TrueHubLightColors.primary
        AppTheme.OCEAN -> if (isSystemDark) OceanDarkColors.primary else OceanLightColors.primary
        AppTheme.FOREST -> if (isSystemDark) ForestDarkColors.primary else ForestLightColors.primary
        AppTheme.SUNSET -> if (isSystemDark) SunsetDarkColors.primary else SunsetLightColors.primary
        AppTheme.LAVENDER -> if (isSystemDark) LavenderDarkColors.primary else LavenderLightColors.primary
        AppTheme.MONOCHROME -> if (isSystemDark) MonochromeDarkColors.primary else MonochromeLightColors.primary
    }

    val sampleSecondary = when(theme) {
        AppTheme.DYNAMIC -> MaterialTheme.colorScheme.secondary
        AppTheme.TRUEHUB -> if (isSystemDark) TrueHubDarkColors.secondary else TrueHubLightColors.secondary
        AppTheme.OCEAN -> if (isSystemDark) OceanDarkColors.secondary else OceanLightColors.secondary
        AppTheme.FOREST -> if (isSystemDark) ForestDarkColors.secondary else ForestLightColors.secondary
        AppTheme.SUNSET -> if (isSystemDark) SunsetDarkColors.secondary else SunsetLightColors.secondary
        AppTheme.LAVENDER -> if (isSystemDark) LavenderDarkColors.secondary else LavenderLightColors.secondary
        AppTheme.MONOCHROME -> if (isSystemDark) MonochromeDarkColors.secondary else MonochromeLightColors.secondary
    }

    val sampleSurface = when(theme) {
        AppTheme.DYNAMIC -> MaterialTheme.colorScheme.surfaceVariant
        AppTheme.TRUEHUB -> if (isSystemDark) Color(0xFF1E1E1E) else Color(0xFFEAEAEA)
        AppTheme.OCEAN -> if (isSystemDark) Color(0xFF1A2230) else Color(0xFFE5ECF4)
        AppTheme.FOREST -> if (isSystemDark) Color(0xFF1B221A) else Color(0xFFE6EFE5)
        AppTheme.SUNSET -> if (isSystemDark) Color(0xFF261812) else Color(0xFFFBECE6)
        AppTheme.LAVENDER -> if (isSystemDark) Color(0xFF211A26) else Color(0xFFF5EEFA)
        AppTheme.MONOCHROME -> if (isSystemDark) Color(0xFF222222) else Color(0xFFEEEEEE)
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.width(130.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, samplePrimary) else null,
            colors = CardDefaults.cardColors(containerColor = sampleSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Abc",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = samplePrimary
                )


                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(samplePrimary)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(samplePrimary.copy(alpha = 0.6f))
                )

                Spacer(modifier = Modifier.weight(1f))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) samplePrimary else sampleSecondary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}