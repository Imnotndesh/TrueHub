package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class FtpService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ftp.FTP_CONFIG, listOf(), Any::class.java)

    suspend fun updateWithResult(ftpUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ftp.FTP_UPDATE, listOf(ftpUpdate), Any::class.java)
}
