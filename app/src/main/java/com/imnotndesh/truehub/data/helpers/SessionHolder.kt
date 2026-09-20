package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.api.TrueNASApiManager

/** Holds the currently-authenticated manager so Hilt can provide it to feature ViewModels. */
object SessionHolder {
    @Volatile
    var current: TrueNASApiManager? = null
}
