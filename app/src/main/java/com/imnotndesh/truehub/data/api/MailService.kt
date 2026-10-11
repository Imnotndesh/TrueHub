package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class MailService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Mail.MAIL_CONFIG, listOf(), Any::class.java)

    suspend fun localAdministratorEmailWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Mail.MAIL_LOCAL_ADMINISTRATOR_EMAIL, listOf(), Any::class.java)

    suspend fun sendWithResult(message: Any?, config: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Mail.MAIL_SEND, listOf(message, config), Any::class.java)

    suspend fun updateWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Mail.MAIL_UPDATE, listOf(data), Any::class.java)
}
