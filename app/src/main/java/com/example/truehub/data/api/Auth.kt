package com.example.truehub.data.api

import com.example.truehub.data.Client
import com.example.truehub.helpers.models.AuthUserDetailsResponse
import com.example.truehub.helpers.models.System


class Auth(private val client : Client) {
    data class DefaultAuth(
        val username : String,
        val password : String,
        val otpToken : String? = null)
    suspend fun loginUser(details: DefaultAuth) : Boolean {
        val defaultParams = listOf(details.username,details.password)
        return client.call(
            method = ApiMethod.Auth.AUTH_LOGIN,
            params = defaultParams,
            resultClass = Boolean::class.java
        )
    }
    suspend fun loginWithApiKey(apiKey : String) : Boolean {
        val loginParams = listOf(apiKey)
        return client.call(
            method = ApiMethod.Auth.AUTH_API_LOGIN,
            params = loginParams,
            resultClass = Boolean::class.java
        )
    }

    suspend fun logoutUser() : Boolean {
        return client.call(
            method = ApiMethod.Auth.AUTH_LOGOUT,
            params = listOf(),
            resultClass = Boolean::class.java
        )
    }

    suspend fun getUserDetails() : AuthUserDetailsResponse {
        val raw = client.call(
            method = ApiMethod.Auth.AUTH_ME,
            params = listOf(),
            resultClass = Any::class.java
        ) as Map<*, *>
        // Find way to fix this
        @Suppress("UNCHECKED_CAST")
        return AuthUserDetailsResponse(
            username = raw["pw_name"] as String,
            fullName = raw["pw_gecos"] as String?,
            local = raw["local"] as Boolean,
            accountAttributes = (raw["account_attributes"] as? List<String>).orEmpty()
        )
    }

    suspend fun generateToken() : String {
        return client.call(
            method = ApiMethod.Auth.GEN_AUTH_TOKEN,
            params = listOf(),
            resultClass = String::class.java
        )
    }

    suspend fun loginWithToken(token : String) : Boolean {
        val loginParams = listOf(token)
        return client.call(
            method = ApiMethod.Auth.AUTH_TOKEN_LOGIN,
            params = loginParams,
            resultClass = Boolean::class.java
        )
    }

    // Move to other file
    suspend fun getSystemInfo(): System.SystemInfo {
        val raw = client.call(
            method = "system.info",
            params = listOf(),
            resultClass = Any::class.java
        ) as Map<*, *>

        // Find way to fix this
        @Suppress("UNCHECKED_CAST")
        return System.SystemInfo(
            version = raw["version"] as String,
            hostname = raw["hostname"] as String,
            physMem = (raw["physmem"] as Number).toDouble(),
            model = raw["model"] as String,
            cores = (raw["cores"] as Number).toDouble(),
            physicalCores = (raw["physical_cores"] as Number).toInt(),
            loadAvg = (raw["loadavg"] as List<Double>),
            uptime = raw["uptime"] as String,
            uptimeSeconds = (raw["uptime_seconds"] as Number).toDouble(),
            systemSerial = raw["system_serial"] as? String,
            systemProduct = raw["system_product"] as? String,
            systemProductVersion = raw["system_product_version"] as? String,
            license = raw["license"] as? String,
            timeZone = raw["timezone"] as String,
            systemManufacturer = raw["system_manufacturer"] as? String,
            eccMemory = raw["ecc_memory"] as? Boolean ?: false,

            // handle the "$date" objects
            buildTime = (raw["buildtime"] as? Map<*, *>)?.get("\$date") as? Long,
            bootTime = (raw["boottime"] as? Map<*, *>)?.get("\$date") as? Long,
            dateTime = (raw["datetime"] as? Map<*, *>)?.get("\$date") as? Long
        )
    }

    suspend fun keepConnection() : Boolean{
        val response = client.call(
            method = ApiMethod.Connection.CONNECTION_KEEP_ALIVE,
            params = listOf(),
            resultClass = String::class.java
        )
        return response == "pong"
    }
}
