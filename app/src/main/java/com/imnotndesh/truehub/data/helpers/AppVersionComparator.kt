package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.models.AppVersion

object AppVersionComparator {

    fun compare(left: String?, right: String?): Int {
        val a = AppVersion.parse(left)
        val b = AppVersion.parse(right)
        return when {
            a == null && b == null -> 0
            a == null -> -1
            b == null -> 1
            else -> a.compareTo(b)
        }
    }

    fun isNewer(candidate: String?, current: String?): Boolean = compare(candidate, current) > 0
}
