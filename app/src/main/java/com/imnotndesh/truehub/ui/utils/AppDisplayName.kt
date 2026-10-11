package com.imnotndesh.truehub.ui.utils

import com.imnotndesh.truehub.data.models.Apps

/**
 * Human-friendly name for an app instance. Falls back to the instance id when the catalog
 * title is blank or a generic placeholder ("iX App", "Custom App") that carries no meaning.
 */
fun Apps.AppQueryResponse.displayName(): String =
    metadata?.title?.takeUnless {
        it.isBlank() ||
            it.equals("iX App", ignoreCase = true) ||
            it.equals("Custom App", ignoreCase = true)
    } ?: name
