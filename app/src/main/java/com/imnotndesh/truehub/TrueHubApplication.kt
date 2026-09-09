package com.imnotndesh.truehub

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import com.imnotndesh.truehub.data.workers.AlertsWorker
import com.imnotndesh.truehub.ui.utils.AppCache

class TrueHubApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        TrueHubLogger.initialize(this)
        AppCache.init(this)
        AlertsWorker.schedule(this)
    }
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(200 * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}