package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class SshService(private val manager: TrueNASApiManager) {

    suspend fun bindifaceChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Ssh.SSH_BINDIFACE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ssh.SSH_CONFIG, listOf(), Any::class.java)

    suspend fun updateWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ssh.SSH_UPDATE, listOf(data), Any::class.java)
}
