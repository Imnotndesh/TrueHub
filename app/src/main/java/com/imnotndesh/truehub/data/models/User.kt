package com.imnotndesh.truehub.data.models

import com.squareup.moshi.JsonClass

object User {
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UserUpdateRequest(
        val id: Int,
        val user_update: UserUpdate
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UserUpdate(
        val username: String? = null,
        val full_name: String? = null,
        val shell: String? = null,
        val email: String? = null,
        val password: String? = null,
        val password_disabled: Boolean? = null,
        val locked: Boolean? = null,
        val sudo_commands: List<String>? = null,
        val sudo_commands_nopasswd: List<String>? = null,
        val sshpubkey: String? = null,
        val group: Int? = null,
        val groups: List<Int>? = null,
        val home: String? = null,
        val home_create: Boolean? = null,
        val home_mode: String? = null,
        val uid: Int? = null,
        val smb: Boolean? = null,
        val subuid: Any? = null, // Can be "DIRECT", Int, or null
        val group_create: Boolean? = null,
        val generate_random_password: Boolean? = null,
        val roles: List<String>? = null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserUpdateResponse(
        val id: Int,
        val uid: Int,
        val username: String,
        val unixhash: String? = null,
        val smbhash: String? = null,
        val home: String = "/var/empty",
        val shell: String = "/usr/bin/zsh",
        val full_name: String,
        val builtin: Boolean,
        val smb: Boolean = true,
        val subuid: Any? = null, // Can be "DIRECT", Int, or null
        val password_disabled: Boolean = false,
        val locked: Boolean = false,
        val sudo_commands: List<String>? = null,
        val sudo_commands_nopasswd: List<String>? = null,
        val email: String? = null,
        val group: UserGroup,
        val groups: List<Int>,
        val sshpubkey: String? = null,
        val immutable: Boolean,
        val twofactor_auth_configured: Boolean,
        val local: Boolean,
        val id_type_both: Boolean,
        val nt_name: String? = null,
        val sid: String? = null,
        val roles: List<String>,
        val password_change_required: Boolean,
        val password_last_change: String? = null, // ISO 8601 date-time or null
        val password_age_days: Int? = null,
        val password_history: List<PasswordHistoryEntry>? = null,
        val password: String? = null // Only returned if password was set in request
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGroup(
        val id: Int,
        val bsdgrp_gid: Int,
        val bsdgrp_group: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class PasswordHistoryEntry(
        val hash: String,
        val timestamp: String // ISO 8601 date-time
    )

    /**
     * <strong>Get user section:</strong>
     *
     * This is different from auth.me
     */
    @Suppress("PropertyName")
    data class GetUserObjRequest(
        val username: String? = null,
        val uid: Int? = null,
        val get_groups: Boolean = false,
        val sid_info: Boolean = false
    )

    @Suppress("PropertyName")
    data class UserObjResponse(
        val pw_name: String,
        val pw_gecos: String,
        val pw_dir: String,
        val pw_shell: String,
        val pw_uid: Int,
        val pw_gid: Int,
        val grouplist: List<Int>? = null,
        val sid: String? = null,
        val source: UserSource,
        val local: Boolean
    )

    enum class UserSource {
        LOCAL,
        ACTIVEDIRECTORY,
        LDAP
    }
    data class ShellChoice(
        val path: String,
        val name: String
    )
    @Suppress("PropertyName")
    data class ChangeUserPasswordRequest(
        val username: String,
        val old_password: String,
        val new_password: String
    )
}
