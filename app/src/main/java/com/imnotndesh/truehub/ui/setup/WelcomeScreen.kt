package com.imnotndesh.truehub.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow

/**
 * First-run welcome screen. Shown when the app has no configured server and no saved accounts.
 * Leads into the server setup flow: Welcome -> Server Setup -> Login -> Home.
 */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val window = LocalTrueHubWindow.current
    val large = window.isAtLeastMedium
    OnboardingScaffold(
        brand = {
            OnboardingBrand(
                title = "TrueHub",
                tagline = "Your TrueNAS, beautifully managed from your pocket.",
                onContainer = large,
                shapeSize = if (large) 160.dp else 128.dp,
                titleStyle = if (large) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WelcomeFeature(
                icon = Icons.Default.Dns,
                title = "Connect your server",
                subtitle = "Point TrueHub at any TrueNAS instance"
            )
            WelcomeFeature(
                icon = Icons.Default.Lock,
                title = "Secure sign-in",
                subtitle = "Password, API key, or two-factor authentication"
            )
            WelcomeFeature(
                icon = Icons.Default.Bolt,
                title = "Monitor anywhere",
                subtitle = "Pools, apps, VMs, and alerts in real time"
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Get started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WelcomeFeature(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.size(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
