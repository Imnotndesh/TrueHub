package com.imnotndesh.truehub.ui.services.apps.details.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.components.SkeletonBox
import com.imnotndesh.truehub.ui.components.SkeletonHost

@Composable
fun MarketplaceSkeleton() {
    SkeletonHost {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SkeletonBox(
                modifier = Modifier
                    .padding(start = 20.dp, top = 16.dp)
                    .width(120.dp)
                    .height(22.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(count = 3) {
                    SkeletonBox(
                        modifier = Modifier.width(280.dp).height(168.dp),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            CategoryRailSkeleton()
            Spacer(modifier = Modifier.height(24.dp))
            CategoryRailSkeleton()
        }
    }
}

@Composable
private fun CategoryRailSkeleton() {
    SkeletonBox(
        modifier = Modifier
            .padding(start = 20.dp)
            .width(140.dp)
            .height(22.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                SkeletonBox(
                    modifier = Modifier.width(76.dp).height(76.dp),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(modifier = Modifier.width(60.dp).height(14.dp))
            }
        }
    }
}
