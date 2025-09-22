package com.example.truehub.data.api

import com.example.truehub.data.Client
import com.example.truehub.helpers.models.Apps
import com.example.truehub.helpers.models.AuthUserDetailsResponse
import com.example.truehub.helpers.models.System
import com.squareup.moshi.Types


class Auth(private val client : Client) {
    data class DefaultAuth(
        val username: String,
        val password: String,
        val otpToken: String? = null
    )

    suspend fun loginUser(details: DefaultAuth): Boolean {
        val defaultParams = listOf(details.username, details.password)
        return client.call(
            method = ApiMethod.Auth.AUTH_LOGIN,
            params = defaultParams,
            resultType = Boolean::class.java
        )
    }

    suspend fun loginWithApiKey(apiKey: String): Boolean {
        val loginParams = listOf(apiKey)
        return client.call(
            method = ApiMethod.Auth.AUTH_API_LOGIN,
            params = loginParams,
            resultType = Boolean::class.java
        )
    }

    suspend fun logoutUser(): Boolean {
        return client.call(
            method = ApiMethod.Auth.AUTH_LOGOUT,
            params = listOf(),
            resultType = Boolean::class.java
        )
    }

    suspend fun getUserDetails(): AuthUserDetailsResponse {
        val raw = client.call(
            method = ApiMethod.Auth.AUTH_ME,
            params = listOf(),
            resultType = Any::class.java
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

    suspend fun generateToken(): String {
        return client.call(
            method = ApiMethod.Auth.GEN_AUTH_TOKEN,
            params = listOf(),
            resultType = String::class.java
        )
    }

    suspend fun loginWithToken(token: String): Boolean {
        val loginParams = listOf(token)
        return client.call(
            method = ApiMethod.Auth.AUTH_TOKEN_LOGIN,
            params = loginParams,
            resultType = Boolean::class.java
        )
    }

    // Move to other file
    suspend fun getSystemInfo(): System.SystemInfo {
        val raw = client.call(
            method = ApiMethod.System.SYSTEM_INFO,
            params = listOf(),
            resultType = Any::class.java
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

    suspend fun keepConnection(): Boolean {
        val response = client.call<String>(
            method = ApiMethod.Connection.CONNECTION_KEEP_ALIVE,
            params = listOf(),
            resultType = String::class.java
        )
        return response == "pong"
    }

//        suspend fun getInstalledApps(): List<Apps.AppQueryResponse> {
//        val raw = client.call(
//            method = QUERY_APPS,
//            params = listOf(emptyList<Any>(), emptyMap<String, Any>()),
//             resultType =  Any::class.java
//        ) as List<*>
//
//        return raw.map { item ->
//            item as Map<*, *>
//
//            Apps.AppQueryResponse(
//                name = item["name"] as String,
//                id = item["id"] as String,
//                state = item["state"] as String,
//                upgradeAvailable = item["upgrade_available"] as? Boolean ?: false,
//                latestVersion = item["latest_version"] as? String,
//                imageUpdatesAvailable = item["image_updates_available"] as? Boolean ?: false,
//                customApp = item["custom_app"] as? Boolean ?: false,
//                migrated = item["migrated"] as? Boolean ?: false,
//                humanVersion = item["human_version"] as? String,
//                version = item["version"] as? String,
//                metadata = (item["metadata"] as? Map<*, *>)?.let { m ->
//                    Apps.Metadata(
//                        appVersion = m["app_version"] as? String,
//                        categories = m["categories"] as? List<String>,
//                        description = m["description"] as? String,
//                        home = m["home"] as? String,
//                        icon = m["icon"] as? String,
//                        keywords = m["keywords"] as? List<String>,
//                        title = m["title"] as? String,
//                        train = m["train"] as? String,
//                        screenshots = m["screenshots"] as? List<String>,
//                        sources = m["sources"] as? List<String>,
//                        maintainers = (m["maintainers"] as? List<*>)?.map { maint ->
//                            maint as Map<*, *>
//                            Apps.Maintainer(
//                                name = maint["name"] as String,
//                                email = maint["email"] as? String,
//                                url = maint["url"] as? String
//                            )
//                        },
//                        capabilities = (m["capabilities"] as? List<*>)?.map { cap ->
//                            cap as Map<*, *>
//                            Apps.Capability(
//                                name = cap["name"] as String,
//                                description = cap["description"] as String
//                            )
//                        },
//                        dateAdded = m["date_added"] as? String,
//                        lastUpdate = m["last_update"] as? String,
//                        changelogUrl = m["changelog_url"] as? String,
//                        libVersion = m["lib_version"] as? String,
//                        libVersionHash = m["lib_version_hash"] as? String,
//                        runAsContext = (m["run_as_context"] as? List<*>)?.map { ctx ->
//                            ctx as Map<*, *>
//                            Apps.RunAsContext(
//                                description = ctx["description"] as? String,
//                                uid = (ctx["uid"] as? Number)?.toInt(),
//                                gid = (ctx["gid"] as? Number)?.toInt(),
//                                userName = ctx["user_name"] as? String,
//                                groupName = ctx["group_name"] as? String
//                            )
//                        },
//                        hostMounts = (m["host_mounts"] as? List<*>)?.map { hm ->
//                            hm as Map<*, *>
//                            Apps.HostMount(
//                                hostPath = hm["host_path"] as? String,
//                                description = hm["description"] as? String
//                            )
//                        }
//                    )
//                },
//                activeWorkloads = (item["active_workloads"] as? Map<*, *>)?.let { aw ->
//                    Apps.ActiveWorkloads(
//                        containers = (aw["containers"] as? Number)?.toInt() ?: 0,
//                        usedPorts = (aw["used_ports"] as? List<*>)?.map { up ->
//                            up as Map<*, *>
//                            Apps.UsedPort(
//                                containerPort = (up["container_port"] as Number).toInt(),
//                                protocol = up["protocol"] as String,
//                                hostPorts = (up["host_ports"] as List<*>).map { hp ->
//                                    hp as Map<*, *>
//                                    Apps.HostPort(
//                                        hostPort = (hp["host_port"] as Number).toInt(),
//                                        hostIp = hp["host_ip"] as String
//                                    )
//                                }
//                            )
//                        },
//                        containerDetails = (aw["container_details"] as? List<*>)?.map { cd ->
//                            cd as Map<*, *>
//                            Apps.ContainerDetail(
//                                id = cd["id"] as String,
//                                serviceName = cd["service_name"] as String,
//                                image = cd["image"] as String,
//                                portConfig = (cd["port_config"] as? List<*>)?.map { pc ->
//                                    pc as Map<*, *>
//                                    Apps.UsedPort(
//                                        containerPort = (pc["container_port"] as Number).toInt(),
//                                        protocol = pc["protocol"] as String,
//                                        hostPorts = (pc["host_ports"] as List<*>).map { hp ->
//                                            hp as Map<*, *>
//                                            Apps.HostPort(
//                                                hostPort = (hp["host_port"] as Number).toInt(),
//                                                hostIp = hp["host_ip"] as String
//                                            )
//                                        }
//                                    )
//                                },
//                                state = cd["state"] as String,
//                                volumeMounts = (cd["volume_mounts"] as? List<*>)?.map { vm ->
//                                    vm as Map<*, *>
//                                    Apps.Volume(
//                                        source = vm["source"] as String,
//                                        destination = vm["destination"] as String,
//                                        mode = vm["mode"] as? String,
//                                        type = vm["type"] as? String
//                                    )
//                                }
//                            )
//                        },
//                        volumes = (aw["volumes"] as? List<*>)?.map { vm ->
//                            vm as Map<*, *>
//                            Apps.Volume(
//                                source = vm["source"] as String,
//                                destination = vm["destination"] as String,
//                                mode = vm["mode"] as? String,
//                                type = vm["type"] as? String
//                            )
//                        },
//                        images = aw["images"] as? List<String>,
//                        networks = (aw["networks"] as? List<*>)?.map { nw ->
//                            nw as Map<*, *>
//                            Apps.Network(
//                                name = nw["Name"] as String,
//                                id = nw["Id"] as String,
//                                labels = nw["Labels"] as? Map<String, String>,
//                                created = nw["Created"] as? String,
//                                scope = nw["Scope"] as? String,
//                                driver = nw["Driver"] as? String,
//                                enableIPv6 = nw["EnableIPv6"] as? Boolean,
//                                ipam = (nw["IPAM"] as? Map<*, *>)?.let { ip ->
//                                    Apps.IPAM(
//                                        driver = ip["Driver"] as? String,
//                                        options = ip["Options"] as? Map<String, String>,
//                                        config = (ip["Config"] as? List<*>)?.map { cfg ->
//                                            cfg as Map<*, *>
//                                            Apps.IPAMConfig(
//                                                subnet = cfg["Subnet"] as? String,
//                                                gateway = cfg["Gateway"] as? String
//                                            )
//                                        }
//                                    )
//                                }
//                            )
//                        }
//                    )
//                },
//                notes = item["notes"] as? String,
//                portals = item["portals"] as? Map<String, String>
//            )
//        }
//    }
    suspend fun getInstalledApps(): List<Apps.AppQueryResponse> {
        val type = Types.newParameterizedType(List::class.java,Apps.AppQueryResponse::class.java)
        return client.call(
            method = ApiMethod.Apps.QUERY_APPS,
            params = listOf(),
            resultType = type
        )
    }
}
