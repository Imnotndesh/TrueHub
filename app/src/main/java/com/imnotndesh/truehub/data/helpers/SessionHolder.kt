package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.api.TrueNASApiManager

object SessionHolder {
    @Volatile
    var current: TrueNASApiManager? = null
}
