package com.imnotndesh.truehub.ui.utils

import java.net.URI

/**
 * Swaps the host of a TrueNAS app portal URL for the host of the server the user is connected
 * to, but ONLY when the portal host is a wildcard bind (0.0.0.0 / ::) that is not routable from
 * a phone. The portal's scheme, port and path are preserved (the container's actual mapped port
 * and any path stay intact). Returns [this] unchanged on any failure or when
 * [serverBaseHttpUrl] is blank/unparseable, or when the portal host is already routable.
 */
fun String.withRoutableServerHost(serverBaseHttpUrl: String?): String {
    if (serverBaseHttpUrl.isNullOrBlank()) return this
    return try {
        val portal = URI(this)
        val host = portal.host
        if (host != null && host != "0.0.0.0" && host != "::") return this // already routable
        val server = URI(serverBaseHttpUrl).host ?: return this
        val routableHost = if (server.contains(':')) "[$server]" else server
        val scheme = portal.scheme ?: "http"
        val port = if (portal.port != -1) ":${portal.port}" else ""
        val path = portal.rawPath ?: ""
        val query = portal.rawQuery?.let { "?$it" } ?: ""
        "$scheme://$routableHost$port$path$query"
    } catch (_: Exception) {
        this
    }
}
