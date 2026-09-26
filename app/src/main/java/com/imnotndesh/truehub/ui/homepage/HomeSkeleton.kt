package com.imnotndesh.truehub.ui.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.components.SkeletonBox
import com.imnotndesh.truehub.ui.components.SkeletonHost
import com.imnotndesh.truehub.ui.components.floatingNavBottomClearance

@Composable
fun HomeSkeleton() {
    SkeletonHost {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = floatingNavBottomClearance())
                .semantics { contentDescription = "Loading dashboard" }
        ) {
            SystemOverviewSkeleton()
            repeat(2) { StorageSkeleton() }
            SharesSkeleton()
        }
    }
}

@Composable
private fun SkeletonCard(
    modifier: Modifier = Modifier,
    padding: Int = 24,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(padding.dp)) { content() }
    }
}

@Composable
private fun SystemOverviewSkeleton() {
    SkeletonCard(modifier = Modifier.padding(bottom = 16.dp), padding = 20) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp))
            Spacer(modifier = Modifier.width(20.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(26.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(16.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.45f).height(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonBox(
                    modifier = Modifier.width(112.dp).height(36.dp),
                    shape = RoundedCornerShape(100.dp)
                )
            }
        }
    }
}

@Composable
private fun StorageSkeleton() {
    SkeletonCard(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(modifier = Modifier.width(96.dp).height(14.dp))
                SkeletonBox(modifier = Modifier.width(148.dp).height(26.dp))
            }
            SkeletonBox(
                modifier = Modifier.width(84.dp).height(28.dp),
                shape = RoundedCornerShape(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonBox(modifier = Modifier.width(110.dp).height(14.dp))
            SkeletonBox(modifier = Modifier.width(110.dp).height(14.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(12.dp), shape = RoundedCornerShape(6.dp))
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp))
    }
}

@Composable
private fun SharesSkeleton() {
    SkeletonCard {
        SkeletonBox(modifier = Modifier.width(128.dp).height(22.dp))
        Spacer(modifier = Modifier.height(20.dp))
        repeat(3) { index ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp))
                }
            }
            if (index < 2) Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
