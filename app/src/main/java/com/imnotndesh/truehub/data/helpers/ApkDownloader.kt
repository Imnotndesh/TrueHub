package com.imnotndesh.truehub.data.helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object ApkDownloader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.MINUTES)
        .build()

    suspend fun download(
        url: String,
        destination: File,
        expectedSize: Long = 0L,
        onProgress: suspend (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Download failed: ${response.code}")
                val body = response.body
                val total = body.contentLength().takeIf { it > 0 } ?: expectedSize
                destination.parentFile?.mkdirs()

                body.byteStream().use { input ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        var lastPercent = -1
                        var read = input.read(buffer)
                        while (read >= 0) {
                            if (read > 0) {
                                output.write(buffer, 0, read)
                                downloaded += read
                                if (total > 0) {
                                    val percent = ((downloaded * 100) / total).toInt()
                                    if (percent != lastPercent) {
                                        lastPercent = percent
                                        onProgress(percent.coerceIn(0, 100))
                                    }
                                }
                            }
                            read = input.read(buffer)
                        }
                    }
                }
            }
            destination
        }
    }
}
