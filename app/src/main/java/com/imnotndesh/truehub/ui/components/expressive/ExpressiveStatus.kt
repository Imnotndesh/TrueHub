package com.imnotndesh.truehub.ui.components.expressive

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class StatusTone {
    POSITIVE,
    NEGATIVE,
    WARNING,
    INFO,
    NEUTRAL,
    OUTLINE
}

@Composable
fun StatusTone.color(): Color = when (this) {
    StatusTone.POSITIVE -> if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
    StatusTone.NEGATIVE -> MaterialTheme.colorScheme.error
    StatusTone.WARNING -> if (isSystemInDarkTheme()) Color(0xFFFFB74D) else Color(0xFF9A5B00)
    StatusTone.INFO -> if (isSystemInDarkTheme()) Color(0xFF64B5F6) else Color(0xFF1976D2)
    StatusTone.NEUTRAL -> if (isSystemInDarkTheme()) Color(0xFFBDBDBD) else Color(0xFF757575)
    StatusTone.OUTLINE -> MaterialTheme.colorScheme.outline
}

@Composable
fun ExpressiveStatusChip(
    label: String,
    tone: StatusTone,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = tone.color(),
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec<Color>(),
        label = "statusColor"
    )
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ExpressiveStatusDot(
    tone: StatusTone,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp
) {
    val color by animateColorAsState(
        targetValue = tone.color(),
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec<Color>(),
        label = "statusDotColor"
    )
    Surface(
        color = color,
        shape = CircleShape,
        modifier = modifier.size(size)
    ) {}
}
