package com.imnotndesh.truehub.ui.adaptive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowClassesTest {
    @Test fun widthBreakpoints() {
        assertEquals(WidthClass.Compact, widthClassFor(0f))
        assertEquals(WidthClass.Compact, widthClassFor(599f))
        assertEquals(WidthClass.Medium, widthClassFor(600f))
        assertEquals(WidthClass.Medium, widthClassFor(839f))
        assertEquals(WidthClass.Expanded, widthClassFor(840f))
        assertEquals(WidthClass.Expanded, widthClassFor(1199f))
        assertEquals(WidthClass.Large, widthClassFor(1200f))
        assertEquals(WidthClass.Large, widthClassFor(1599f))
        assertEquals(WidthClass.ExtraLarge, widthClassFor(1600f))
        assertEquals(WidthClass.ExtraLarge, widthClassFor(3000f))
    }

    @Test fun heightBreakpoints() {
        assertEquals(HeightClass.Compact, heightClassFor(479f))
        assertEquals(HeightClass.Medium, heightClassFor(480f))
        assertEquals(HeightClass.Medium, heightClassFor(899f))
        assertEquals(HeightClass.Expanded, heightClassFor(900f))
    }

    @Test fun widthClassesAreOrdered() {
        assertTrue(WidthClass.Large > WidthClass.Expanded)
        assertTrue(WidthClass.Compact < WidthClass.Medium)
    }

    @Test fun helpers() {
        val w = fakeTrueHubWindow(WidthClass.Expanded, HeightClass.Compact)
        assertTrue(w.allowsMultiPane)
        assertTrue(w.isCompactHeight)
        assertFalse(w.isCompactWidth)
        assertFalse(fakeTrueHubWindow(WidthClass.Medium).allowsMultiPane)
    }

    @Test fun spacingByWidth() {
        assertEquals(16f, adaptiveSpacingFor(WidthClass.Compact).screenMargin.value, 0f)
        assertEquals(32f, adaptiveSpacingFor(WidthClass.ExtraLarge).screenMargin.value, 0f)
    }
}
