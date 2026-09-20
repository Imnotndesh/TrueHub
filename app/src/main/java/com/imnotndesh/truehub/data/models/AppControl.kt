package com.imnotndesh.truehub.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppControlEntry(
    val appName: String,
    val title: String,
    val iconUrl: String? = null,
    val cachedIconPath: String? = null,
    val state: String = "unknown"
)
