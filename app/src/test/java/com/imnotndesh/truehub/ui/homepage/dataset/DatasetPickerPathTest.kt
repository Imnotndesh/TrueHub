package com.imnotndesh.truehub.ui.homepage.dataset

import org.junit.Assert.assertEquals
import org.junit.Test

class DatasetPickerPathTest {
    @Test
    fun usesMountpointWhenAvailable() {
        assertEquals(
            "/mnt/Main/Jellyfin",
            fullDatasetMountPath("Main/Jellyfin", "/mnt/Main/Jellyfin")
        )
    }

    @Test
    fun fallsBackToMntDatasetPath() {
        assertEquals("/mnt/Main/Jellyfin", fullDatasetMountPath("Main/Jellyfin", null))
        assertEquals("/mnt/Main/Jellyfin", fullDatasetMountPath("Main/Jellyfin", "  "))
    }
}
