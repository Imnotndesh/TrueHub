package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.components.SkeletonBox
import com.imnotndesh.truehub.ui.components.SkeletonHost

@Composable
fun AppResourceUsageSkeleton(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val wide = maxWidth >= 600.dp
        SkeletonHost {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) {
                        SkeletonBox(
                            modifier = Modifier.width(68.dp).height(36.dp),
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }

                OverviewSkeleton()

                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ChartSkeleton(Modifier.weight(1f))
                        ChartSkeleton(Modifier.weight(1f))
                    }
                } else {
                    ChartSkeleton(Modifier.fillMaxWidth())
                    ChartSkeleton(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun OverviewSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCellSkeleton(Modifier.weight(1f))
                MetricCellSkeleton(Modifier.weight(1f))
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCellSkeleton(Modifier.weight(1f))
                MetricCellSkeleton(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricCellSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(modifier = Modifier.size(26.dp), shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            SkeletonBox(modifier = Modifier.width(56.dp).height(14.dp))
        }
        SkeletonBox(modifier = Modifier.width(96.dp).height(22.dp))
    }
}

@Composable
private fun ChartSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.width(48.dp).height(18.dp))
                SkeletonBox(modifier = Modifier.width(72.dp).height(18.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
