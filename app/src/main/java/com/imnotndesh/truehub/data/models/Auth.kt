package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json

object Auth {
    enum class LoginMode {
        PASSWORD,
        API_KEY
    }

    @Suppress("PropertyName")
    data class AuthResponse(
        //@field:Json("pw_name")
        val pw_name: String? = "missing",

        @field:Json("pw_gecos")
        val pwGecos: String? = null,

        @field:Json("pw_dir")
        val pwDir: String? = null,

        @field:Json("pw_shell")
        val pwShell: String? = null,

        @field:Json("pw_uid")
        val pwUid: Long? = null,

        @field:Json("pw_gid")
        val pwGid: Long? = null,

        @field:Json("grouplist")
        val groupList: List<String>? = null,

        @field:Json("sid")
        val sid: String? = null,

        @field:Json("source")
        val source: String? = null,

        @field:Json("local")
        val local: Boolean,

        // CHANGE HERE: Remove @Contextual
        @field:Json("attributes")
        val attributes: Map<String, Any>? = null,

        // CHANGE HERE: Remove @Contextual
        @field:Json("two_factor_config")
        val twoFactorConfig: Map<String, Any>? = null,

        // CHANGE HERE: Remove @Contextual
        @field:Json("privilege")
        val privilege: Map<String, Any>? = null,

        @field:Json("account_attributes")
        val accountAttributes: List<String> = emptyList()
    )
    data class TokenRequest(
        val ttl: Int = 6000,
        val attrs: Map<String, Any> = emptyMap(),
        val matchOrigin: Boolean = true,
        val singleUse: Boolean = false
    )
}