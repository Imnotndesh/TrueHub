package com.imnotndesh.truehub.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper

@Composable
fun ServiceListSkeleton(itemCount: Int = 5) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val columnCount = AdaptiveLayoutHelper.getColumnCount(compact = 1, medium = 2, expanded = 3)
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()
    val horizontalSpacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    SkeletonHost {
        if (isCompact) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = contentPadding.dp, end = contentPadding.dp, top = 8.dp, bottom = 8.dp + floatingNavBottomClearance())
            ) {
                items(count = itemCount) {
                    ServiceCardSkeleton(cardPadding = 20, horizontalPadding = 4)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = contentPadding.dp, end = contentPadding.dp, top = 8.dp, bottom = 8.dp + floatingNavBottomClearance())
            ) {
                items(count = itemCount) {
                    ServiceCardSkeleton(cardPadding = 16, horizontalPadding = 0)
                }
            }
        }
    }
}

@Composable
private fun ServiceCardSkeleton(cardPadding: Int, horizontalPadding: Int) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(modifier = Modifier.size(52.dp), shape = MaterialTheme.shapes.medium)
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(20.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(14.dp))
                }
                SkeletonBox(
                    modifier = Modifier.width(64.dp).height(28.dp),
                    shape = RoundedCornerShape(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) {
                    SkeletonBox(
                        modifier = Modifier.width(104.dp).height(36.dp),
                        shape = RoundedCornerShape(100.dp)
                    )
                }
            }
        }
    }
}
