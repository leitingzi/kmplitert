package io.github.leitingzi.kmplitert.tool

import kotlin.test.Test
import kotlin.test.assertEquals

class LiteRtImageTest {

    @Test
    fun testResize() {
        val data = ByteArray(2 * 2 * 3) { it.toByte() }
        val image = LiteRtImage.fromRawRgb(data, 2, 2)
        val resized = image.resize(4, 4)
        assertEquals(4, resized.width)
        assertEquals(4, resized.height)
    }

    @Test
    fun testCrop() {
        val data = ByteArray(4 * 4 * 3) { it.toByte() }
        val image = LiteRtImage.fromRawRgb(data, 4, 4)
        val cropped = image.crop(1, 1, 2, 2)
        assertEquals(2, cropped.width)
        assertEquals(2, cropped.height)
    }
}
