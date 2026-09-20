package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class SmbService(private val manager: TrueNASApiManager) {

    suspend fun bindipChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Smb.SMB_BINDIP_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Smb.SMB_CONFIG, listOf(), Any::class.java)

    suspend fun unixcharsetChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Smb.SMB_UNIXCHARSET_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(smbUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Smb.SMB_UPDATE, listOf(smbUpdate), Any::class.java)
}
