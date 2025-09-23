package com.example.truehub.data.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Auth {
    enum class LoginMode {
        PASSWORD,
        API_KEY
    }
    @Serializable
    data class AuthResponse(
        @SerialName("pw_name")
        val pwName: String,

        @SerialName("pw_gecos")
        val pwGecos: String? = null,

        @SerialName("pw_dir")
        val pwDir: String? = null,

        @SerialName("pw_shell")
        val pwShell: String? = null,

        @SerialName("pw_uid")
        val pwUid: Long? = null,

        @SerialName("pw_gid")
        val pwGid: Long? = null,

        @SerialName("grouplist")
        val groupList: List<String>? = null,

        @SerialName("sid")
        val sid: String? = null,

        @SerialName("source")
        val source: String? = null,

        @SerialName("local")
        val local: Boolean,

        @SerialName("attributes")
        val attributes: Map<String, @Contextual Any>? = null,

        @SerialName("two_factor_config")
        val twoFactorConfig: Map<String, @Contextual Any>? = null,

        @SerialName("privilege")
        val privilege: Map<String, @Contextual Any>? = null,

        @SerialName("account_attributes")
        val accountAttributes: List<String> = emptyList()
    )
}