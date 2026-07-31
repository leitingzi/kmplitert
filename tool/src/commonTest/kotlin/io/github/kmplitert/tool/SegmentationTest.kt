package io.github.kmplitert.tool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SegmentationTest {

    @Test
    fun testSegmentationMask() {
        val data = floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f)
        val mask = LiteRTExt.Segmentation.Mask(2, 2, data)

        assertEquals(2, mask.width)
        assertEquals(2, mask.height)
        assertEquals(0.1f, mask.getValue(0, 0), 1e-5f)
        assertEquals(0.4f, mask.getValue(1, 1), 1e-5f)
        assertEquals(0f, mask.getValue(2, 2)) // Out of bounds
    }

    @Test
    fun testSegmentationMaskEquality() {
        val mask1 = LiteRTExt.Segmentation.Mask(1, 1, floatArrayOf(0.5f))
        val mask2 = LiteRTExt.Segmentation.Mask(1, 1, floatArrayOf(0.5f))
        val mask3 = LiteRTExt.Segmentation.Mask(1, 1, floatArrayOf(0.6f))

        assertEquals(mask1, mask2)
        assertNotEquals(mask1, mask3)
        assertEquals(mask1.hashCode(), mask2.hashCode())
    }
}
