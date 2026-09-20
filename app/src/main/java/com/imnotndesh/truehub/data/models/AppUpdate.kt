package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json

enum class UpdateChannel { STABLE, PRERELEASE }

enum class DeviceAbi(val assetKey: String) {
    ARM64("arm64"),
    ARMV7A("armv7a"),
    X86_64("x86_64"),
    UNIVERSAL("universal"),
    UNSUPPORTED("")
}

data class GitHubRelease(
    @Json(name = "tag_name") val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    val prerelease: Boolean = false,
    val draft: Boolean = false,
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

data class GitHubAsset(
    val name: String = "",
    @Json(name = "browser_download_url") val downloadUrl: String = "",
    val size: Long = 0,
    @Json(name = "content_type") val contentType: String? = null
)

data class UpdateManifest(
    val versionName: String = "",
    val versionCode: Int = 0,
    val tag: String? = null,
    val channel: String? = null,
    val publishedAt: String? = null,
    val minSupportedVersionCode: Int = 0,
    val notes: String? = null,
    val assets: Map<String, UpdateAsset> = emptyMap()
)

data class UpdateAsset(
    val url: String = "",
    val size: Long = 0,
    val sha256: String? = null
)

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val tag: String,
    val channel: UpdateChannel,
    val publishedAt: String? = null,
    val notes: String? = null,
    val releaseUrl: String? = null,
    val asset: UpdateAsset? = null,
    val abi: DeviceAbi = DeviceAbi.UNSUPPORTED
)
