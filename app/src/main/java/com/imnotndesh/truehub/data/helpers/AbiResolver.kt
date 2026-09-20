package com.imnotndesh.truehub.data.helpers

import android.os.Build
import com.imnotndesh.truehub.data.models.DeviceAbi

object AbiResolver {

    fun current(): DeviceAbi = fromAbis(Build.SUPPORTED_ABIS)

    fun fromAbis(abis: Array<String>): DeviceAbi = when {
        abis.any { it.equals("arm64-v8a", ignoreCase = true) } -> DeviceAbi.ARM64
        abis.any { it.equals("armeabi-v7a", ignoreCase = true) } -> DeviceAbi.ARMV7A
        abis.any { it.equals("x86_64", ignoreCase = true) } -> DeviceAbi.X86_64
        else -> DeviceAbi.UNSUPPORTED
    }
}
