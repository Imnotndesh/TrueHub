package com.imnotndesh.truehub.data.helpers

import android.content.Context
import com.imnotndesh.truehub.BuildConfig
import com.imnotndesh.truehub.data.api.GitHubReleaseService
import com.imnotndesh.truehub.data.models.AppVersion
import com.imnotndesh.truehub.data.models.DeviceAbi
import com.imnotndesh.truehub.data.models.GitHubRelease
import com.imnotndesh.truehub.data.models.UpdateAsset
import com.imnotndesh.truehub.data.models.UpdateChannel
import com.imnotndesh.truehub.data.models.UpdateInfo
import com.imnotndesh.truehub.data.models.UpdateManifest

object UpdateRepository {

    suspend fun check(
        context: Context,
        includePrerelease: Boolean = false
    ): Result<UpdateInfo?> = runCatching {
        val token = UpdatePrefs.getToken(context)
        val releases = GitHubReleaseService.fetchReleases(limit = 10, token = token).getOrThrow()
        val latest = releases
            .filter { !it.draft && (includePrerelease || !it.prerelease) }
            .maxByOrNull { AppVersion.parse(it.tagName) ?: AppVersion(0, 0, 0) }

        UpdatePrefs.setLastChecked(context, java.lang.System.currentTimeMillis())
        if (latest == null) return@runCatching null

        val manifest = GitHubReleaseService.fetchManifest(latest, token).getOrNull()
        val candidateCode = manifest?.versionCode
            ?: AppVersion.parse(latest.tagName)?.toVersionCode()
            ?: 0
        val currentCode = if (manifest != null) {
            BuildConfig.VERSION_CODE
        } else {
            AppVersion.parse(BuildConfig.VERSION_NAME)?.toVersionCode() ?: BuildConfig.VERSION_CODE
        }
        if (candidateCode <= currentCode) return@runCatching null

        val abi = AbiResolver.current()
        val info = UpdateInfo(
            versionName = manifest?.versionName?.takeIf { it.isNotBlank() }
                ?: latest.tagName.removePrefix("v").removePrefix("V"),
            versionCode = candidateCode,
            tag = manifest?.tag?.takeIf { it.isNotBlank() } ?: latest.tagName,
            channel = if (latest.prerelease) UpdateChannel.PRERELEASE else UpdateChannel.STABLE,
            publishedAt = manifest?.publishedAt ?: latest.publishedAt,
            notes = manifest?.notes?.takeIf { it.isNotBlank() } ?: latest.body,
            releaseUrl = latest.htmlUrl,
            asset = resolveAsset(latest, manifest, abi),
            abi = abi
        )
        UpdatePrefs.saveLatest(context, info)
        info
    }

    suspend fun clear(context: Context) {
        UpdatePrefs.saveLatest(context, null)
    }

    private fun resolveAsset(
        release: GitHubRelease,
        manifest: UpdateManifest?,
        abi: DeviceAbi
    ): UpdateAsset? {
        val key = abi.assetKey
        if (key.isNotEmpty()) {
            manifest?.assets?.get(key)?.let { return it }
            release.assets
                .firstOrNull { it.name.endsWith("-$key.apk", ignoreCase = true) }
                ?.let { return UpdateAsset(it.downloadUrl, it.size) }
        }
        manifest?.assets?.get(DeviceAbi.UNIVERSAL.assetKey)?.let { return it }
        release.assets
            .firstOrNull { it.name.contains("universal", ignoreCase = true) }
            ?.let { return UpdateAsset(it.downloadUrl, it.size) }
        return null
    }
}
