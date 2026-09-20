package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Disk
import com.squareup.moshi.Types

class DiskService(private val manager: TrueNASApiManager) {

    suspend fun detailsWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Disk.DISK_DETAILS, listOf(data), Any::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Disk.Entry> =
        manager.callWithResult(ApiMethods.Disk.DISK_GET_INSTANCE, listOf(id, options), Disk.Entry::class.java)

    suspend fun getUsedWithResult(joinPartitions: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Disk.DISK_GET_USED, listOf(joinPartitions), Any::class.java)

    suspend fun temperatureAggWithResult(names: Any?, days: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Disk.DISK_TEMPERATURE_AGG, listOf(names, days), Any::class.java)

    suspend fun temperatureAlertsWithResult(names: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Disk.DISK_TEMPERATURE_ALERTS, listOf(names), Any::class.java)

    suspend fun temperaturesWithResult(name: Any?, includeThresholds: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Disk.DISK_TEMPERATURES, listOf(name, includeThresholds), Any::class.java)

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Disk.Entry> =
        manager.callWithResult(ApiMethods.Disk.DISK_UPDATE, listOf(id, data), Disk.Entry::class.java)

    suspend fun wipeWithResult(dev: Any?, mode: Any?, synccache: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Disk.DISK_WIPE, listOf(dev, mode, synccache), Any::class.java)
}
