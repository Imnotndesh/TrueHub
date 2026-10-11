package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Reporting
import com.squareup.moshi.Types

class ReportingService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Reporting.Entry> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_CONFIG, listOf(), Reporting.Entry::class.java)

    suspend fun exportersWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS, listOf(), Any::class.java)

    suspend fun exportersCreateWithResult(reportingExporterCreate: Any?): ApiResult<Reporting.ExporterEntry> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS_CREATE, listOf(reportingExporterCreate), Reporting.ExporterEntry::class.java)

    suspend fun exportersDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS_DELETE, listOf(id), Unit::class.java)

    suspend fun exportersExporterSchemasWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS_EXPORTER_SCHEMAS, listOf(), Any::class.java)

    suspend fun exportersGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Reporting.ExporterEntry> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS_GET_INSTANCE, listOf(id, options), Reporting.ExporterEntry::class.java)

    suspend fun exportersQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Reporting.ExporterEntry>> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Reporting.ExporterEntry::class.java))

    suspend fun exportersUpdateWithResult(id: Any?, reportingExporterUpdate: Any?): ApiResult<Reporting.ExporterEntry> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_EXPORTERS_UPDATE, listOf(id, reportingExporterUpdate), Reporting.ExporterEntry::class.java)

    suspend fun graphWithResult(str: Any?, query: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_GRAPH, listOf(str, query), Any::class.java)

    suspend fun netdataGetDataWithResult(graphs: Any?, query: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_NETDATA_GET_DATA, listOf(graphs, query), Any::class.java)

    suspend fun netdataGraphWithResult(str: Any?, query: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_NETDATA_GRAPH, listOf(str, query), Any::class.java)

    suspend fun netdataGraphsWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_NETDATA_GRAPHS, listOf(filters, options), Any::class.java)

    suspend fun updateWithResult(reportingUpdate: Any?): ApiResult<Reporting.Entry> =
        manager.callWithResult(ApiMethods.Reporting.REPORTING_UPDATE, listOf(reportingUpdate), Reporting.Entry::class.java)
}
