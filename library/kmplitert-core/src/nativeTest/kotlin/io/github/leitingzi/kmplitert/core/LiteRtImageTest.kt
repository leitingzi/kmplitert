package io.github.leitingzi.kmplitert.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiteRtImageTest {

    @Test
    fun testResize() {
        // Create a 2x2 image (Red, Green, Blue, White)
        // R G
        // B W
        val data = byteArrayOf(
            255.toByte(), 0, 0,   0, 255.toByte(), 0,
            0, 0, 255.toByte(),   255.toByte(), 255.toByte(), 255.toByte()
        )
        val image = LiteRtImage(data, 2, 2)

        val resized = image.resize(1, 1)
        assertEquals(1, resized.width)
        assertEquals(1, resized.height)

        // Resizing 2x2 to 4x4 should not crash and produce some data
        val upscaled = image.resize(4, 4)
        assertEquals(4, upscaled.width)
        assertEquals(4, upscaled.height)
        assertEquals(4 * 4 * 3, upscaled.data.size)
    }

    @Test
    fun testToFloatArray() {
        val data = byteArrayOf(0, 127, 255.toByte())
        val image = LiteRtImage(data, 1, 1)

        val floatArray = image.toFloatArray(mean = 127f, std = 128f)
        assertEquals(3, floatArray.size)
        assertTrue(floatArray[0] < 0)
        assertTrue(floatArray[1] > -0.1f && floatArray[1] < 0.1f)
        assertTrue(floatArray[2] > 0.9f)
    }

    @Test
    fun testBmpDecoding() {
        // Simple 1x1 24-bit BMP (Red pixel)
        val bmpData = byteArrayOf(
            0x42, 0x4D,             // Signature "BM"
            0x3A, 0x00, 0x00, 0x00, // File size (58 bytes)
            0x00, 0x00, 0x00, 0x00, // Reserved
            0x36, 0x00, 0x00, 0x00, // Data offset (54 bytes)
            0x28, 0x00, 0x00, 0x00, // Header size (40 bytes)
            0x01, 0x00, 0x00, 0x00, // Width (1)
            0x01, 0x00, 0x00, 0x00, // Height (1)
            0x01, 0x00,             // Planes (1)
            0x18, 0x00,             // Bits per pixel (24)
            0x00, 0x00, 0x00, 0x00, // Compression (0)
            0x04, 0x00, 0x00, 0x00, // Image size (4 bytes with padding)
            0x13, 0x0B, 0x00, 0x00, // XpixelsPerM
            0x13, 0x0B, 0x00, 0x00, // YpixelsPerM
            0x00, 0x00, 0x00, 0x00, // Colors used
            0x00, 0x00, 0x00, 0x00, // Important colors
            0x00, 0x00, 0xFF.toByte(), 0x00 // Blue, Green, Red, Padding
        )

        val image = LiteRtImage.fromBytes(bmpData)
        assertEquals(1, image.width)
        assertEquals(1, image.height)
        assertEquals(255.toByte(), image.data[0]) // R
        assertEquals(0.toByte(), image.data[1])   // G
        assertEquals(0.toByte(), image.data[2])   // B
    }
}