package io.github.kmplitert.tool.segmentation

import kotlin.test.*

class SegmentationTest {

    @Test
    fun testSegmentationMask() {
        val data = floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f)
        val mask = SegmentationMask(2, 2, data)
        
        assertEquals(2, mask.width)
        assertEquals(2, mask.height)
        assertEquals(0.1f, mask.getValue(0, 0), 1e-5f)
        assertEquals(0.4f, mask.getValue(1, 1))
        assertEquals(0f, mask.getValue(2, 2)) // Out of bounds
    }

    @Test
    fun testImageSegmenterResult() {
        val mask = SegmentationMask(1, 1, floatArrayOf(0.9f))
        val result = ImageSegmenterResult(mask)
        assertEquals(mask, result.mask)
    }

    @Test
    fun testSegmentationMaskEquality() {
        val mask1 = SegmentationMask(1, 1, floatArrayOf(0.5f))
        val mask2 = SegmentationMask(1, 1, floatArrayOf(0.5f))
        val mask3 = SegmentationMask(1, 1, floatArrayOf(0.6f))
        
        assertEquals(mask1, mask2)
        assertNotEquals(mask1, mask3)
        assertEquals(mask1.hashCode(), mask2.hashCode())
    }
}
