package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.tool.LiteRtImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiteRtImageTest {

    @Test
    fun testConversions() {
        // Red, Green, Blue, Black (0,0,0)
        // For a 2x2 image, we need 2x2x3 = 12 bytes
        val data = byteArrayOf(
            255.toByte(), 0, 0, // Top-left: Red
            0, 255.toByte(), 0, // Top-right: Green
            0, 0, 255.toByte(), // Bottom-left: Blue
            0, 0, 0             // Bottom-right: Black
        )
        
        val image = LiteRtImage.fromRawRgb(data, 2, 2)
        
        val int8 = image.toInt8Array()
        assertEquals(12, int8.size)
        // 255.toByte() is -1
        assertEquals(255.toByte(), int8[0], "Red channel should be 255")
        assertEquals(0.toByte(), int8[1], "Green channel should be 0")
        // int8[3] is Green pixel R channel (index 1*3 = 3)
        assertEquals(0.toByte(), int8[3], "Green pixel R channel should be 0")
        assertEquals(255.toByte(), int8[4], "Green pixel G channel should be 255")

        val ints = image.toIntArray()
        assertEquals(12, ints.size)
        assertEquals(255, ints[0])
        assertEquals(0, ints[1])
        assertEquals(255, ints[4])

        val longs = image.toLongArray()
        assertEquals(12, longs.size)
        assertEquals(255L, longs[0])
        assertEquals(0L, longs[1])
        assertEquals(255L, longs[4])

        val booleans = image.toBooleanArray()
        assertEquals(12, booleans.size)
        assertTrue(booleans[0])
        assertTrue(!booleans[1])
        assertTrue(booleans[4])
        assertTrue(!booleans[11]) // Black pixel
    }
}
