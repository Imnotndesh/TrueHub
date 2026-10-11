package com.imnotndesh.truehub.ui.utils

/** Turns raw TrueNAS/JSON-RPC error strings into something a non-developer can act on. */
fun friendlyApiError(raw: String?): String? {
    if (raw.isNullOrBlank()) return raw
    val lower = raw.lowercase()
    return when {
        "not enough permissions" in lower || "eperm" in lower || "permission" in lower ->
            "You don't have permission to do that. Ask a TrueNAS administrator for access."
        "eexist" in lower || "already exists" in lower -> "That already exists."
        "enoent" in lower || "no such" in lower -> "That item no longer exists."
        else -> raw.substringAfter("]", raw).trim().ifEmpty { raw }
    }
}
