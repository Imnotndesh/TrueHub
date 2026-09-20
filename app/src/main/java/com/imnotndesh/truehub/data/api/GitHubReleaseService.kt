package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.models.GitHubRelease
import com.imnotndesh.truehub.data.models.UpdateManifest
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object GitHubReleaseService {

    private const val BASE_URL = "https://api.github.com/repos/Imnotndesh/TrueHub"
    private const val MANIFEST_ASSET = "update-manifest.json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val releaseAdapter = moshi.adapter(GitHubRelease::class.java)
    private val releaseListAdapter = moshi.adapter<List<GitHubRelease>>(
        Types.newParameterizedType(List::class.java, GitHubRelease::class.java)
    )
    private val manifestAdapter = moshi.adapter(UpdateManifest::class.java)

    suspend fun fetchLatest(token: String? = null): Result<GitHubRelease> =
        fetch("$BASE_URL/releases/latest", token).mapCatching { body ->
            releaseAdapter.fromJson(body) ?: error("Empty release response")
        }

    suspend fun fetchReleases(limit: Int = 10, token: String? = null): Result<List<GitHubRelease>> =
        fetch("$BASE_URL/releases?per_page=$limit", token).mapCatching { body ->
            releaseListAdapter.fromJson(body).orEmpty()
        }

    suspend fun fetchManifest(release: GitHubRelease, token: String? = null): Result<UpdateManifest?> {
        val asset = release.assets.firstOrNull { it.name.equals(MANIFEST_ASSET, ignoreCase = true) }
            ?: return Result.success(null)
        return fetch(asset.downloadUrl, token).mapCatching { body ->
            manifestAdapter.fromJson(body)
        }
    }

    private suspend fun fetch(url: String, token: String?): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .apply {
                    token?.takeIf { it.isNotBlank() }?.let { header("Authorization", "Bearer $it") }
                }
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("GitHub request failed: ${response.code}")
                response.body?.string() ?: error("Empty GitHub response")
            }
        }
    }
}
