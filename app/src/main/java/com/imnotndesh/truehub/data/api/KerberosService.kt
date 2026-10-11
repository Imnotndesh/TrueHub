package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Kerberos
import com.squareup.moshi.Types

class KerberosService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Kerberos.Entry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_CONFIG, listOf(), Kerberos.Entry::class.java)

    suspend fun keytabWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_KEYTAB, listOf(), Any::class.java)

    suspend fun keytabCreateWithResult(data: Any?): ApiResult<Kerberos.KeytabEntry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_KEYTAB_CREATE, listOf(data), Kerberos.KeytabEntry::class.java)

    suspend fun keytabDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_KEYTAB_DELETE, listOf(id), Unit::class.java)

    suspend fun keytabGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Kerberos.KeytabEntry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_KEYTAB_GET_INSTANCE, listOf(id, options), Kerberos.KeytabEntry::class.java)

    suspend fun keytabQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Kerberos.KeytabEntry>> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_KEYTAB_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Kerberos.KeytabEntry::class.java))

    suspend fun keytabUpdateWithResult(id: Any?, data: Any?): ApiResult<Kerberos.KeytabEntry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_KEYTAB_UPDATE, listOf(id, data), Kerberos.KeytabEntry::class.java)

    suspend fun realmWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_REALM, listOf(), Any::class.java)

    suspend fun realmCreateWithResult(data: Any?): ApiResult<Kerberos.RealmEntry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_REALM_CREATE, listOf(data), Kerberos.RealmEntry::class.java)

    suspend fun realmDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_REALM_DELETE, listOf(id), Unit::class.java)

    suspend fun realmGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Kerberos.RealmEntry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_REALM_GET_INSTANCE, listOf(id, options), Kerberos.RealmEntry::class.java)

    suspend fun realmQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Kerberos.RealmEntry>> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_REALM_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Kerberos.RealmEntry::class.java))

    suspend fun realmUpdateWithResult(id: Any?, data: Any?): ApiResult<Kerberos.RealmEntry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_REALM_UPDATE, listOf(id, data), Kerberos.RealmEntry::class.java)

    suspend fun updateWithResult(kerberosUpdate: Any?): ApiResult<Kerberos.Entry> =
        manager.callWithResult(ApiMethods.Kerberos.KERBEROS_UPDATE, listOf(kerberosUpdate), Kerberos.Entry::class.java)
}
