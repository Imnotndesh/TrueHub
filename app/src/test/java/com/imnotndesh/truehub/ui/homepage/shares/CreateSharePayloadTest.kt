package com.imnotndesh.truehub.ui.homepage.shares

import org.junit.Assert.assertEquals
import org.junit.Test

class CreateSharePayloadTest {
    @Test
    fun normalizesRelativeDatasetPaths() {
        assertEquals("/tank/data", normalizeSharePath("  tank/data  "))
        assertEquals("/tank/data", normalizeSharePath("/tank/data"))
    }

    @Test
    fun smbPayloadContainsRequiredFields() {
        val payload = smbSharePayload(
            path = "tank/data",
            name = " Media ",
            comment = " Shared media ",
            enabled = true,
            readOnly = true,
            guestAccess = false
        )

        assertEquals("/tank/data", payload["path"])
        assertEquals("Media", payload["name"])
        assertEquals("Shared media", payload["comment"])
        assertEquals(true, payload["enabled"])
        assertEquals(true, payload["ro"])
        assertEquals(false, payload["guestok"])
    }

    @Test
    fun nfsPayloadDoesNotSendSmbOnlyFields() {
        val payload = nfsSharePayload(
            path = "/tank/data",
            comment = "",
            enabled = true,
            readOnly = false
        )

        assertEquals("/tank/data", payload["path"])
        assertEquals("", payload["comment"])
        assertEquals(false, payload["ro"])
        assertEquals(false, payload.containsKey("name"))
        assertEquals(false, payload.containsKey("guestok"))
    }
}
