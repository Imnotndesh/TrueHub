package com.imnotndesh.truehub.data.helpers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.io.File
import java.security.MessageDigest

object ApkVerifier {

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read >= 0) {
                if (read > 0) digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().toHex()
    }

    fun checksumMatches(file: File, expected: String?): Boolean {
        if (expected.isNullOrBlank()) return true
        return sha256(file).equals(expected.trim(), ignoreCase = true)
    }

    fun signatureMatches(context: Context, apkFile: File): Boolean {
        val packageManager = context.packageManager
        val flags = PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
        val installed = certificates(packageManager.getPackageInfo(context.packageName, flags))
        val candidate = certificates(packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags))
        return installed.isNotEmpty() && installed == candidate
    }

    private fun certificates(info: PackageInfo?): Set<String> {
        val signers = info?.signingInfo?.apkContentsSigners ?: return emptySet()
        return signers.map { fingerprint(it.toByteArray()) }.toSet()
    }

    private fun fingerprint(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    private fun ByteArray.toHex(): String = joinToString("") { byte -> "%02x".format(byte) }
}
