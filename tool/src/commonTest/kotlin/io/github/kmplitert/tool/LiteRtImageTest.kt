package io.github.kmplitert.tool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiteRtImageTest : PlatformTest() {

    private fun createDummyImage(width: Int, height: Int, channels: Int = 3): LiteRtImage {
        val data = ByteArray(width * height * channels) { it.toByte() }
        return LiteRtImage.fromRawRgb(data, width, height)
    }

    @Test
    fun testProperties() {
        val image = createDummyImage(10, 20)
        assertEquals(10, image.width)
        assertEquals(20, image.height)
        assertTrue(image.channels >= 3)
    }

    @Test
    fun testResize() {
        val image = createDummyImage(2, 2)
        val resized = image.resize(4, 8)
        assertEquals(4, resized.width)
        assertEquals(8, resized.height)
    }

    @Test
    fun testCrop() {
        val image = createDummyImage(10, 10)
        val cropped = image.crop(2, 2, 5, 5)
        assertEquals(5, cropped.width)
        assertEquals(5, cropped.height)
    }

    @Test
    fun testCenterCrop() {
        val image = createDummyImage(10, 10)
        val cropped = image.centerCrop(4, 4)
        assertEquals(4, cropped.width)
        assertEquals(4, cropped.height)
    }

    @Test
    fun testRotate() {
        val image = createDummyImage(10, 10)
        val rotated = image.rotate(90f)
        assertEquals(10, rotated.width)
        assertEquals(10, rotated.height)
    }

    @Test
    fun testFlip() {
        val image = createDummyImage(10, 10)
        val flipped = image.flip(horizontal = true, vertical = false)
        assertEquals(10, flipped.width)
        assertEquals(10, flipped.height)
    }

    @Test
    fun testColorConversions() {
        val image = createDummyImage(4, 4)
        val grayscale = image.toGrayscale()
        assertEquals(1, grayscale.channels)
        
        val rgb = grayscale.toRgb()
        assertEquals(3, rgb.channels)
    }

    @Test
    fun testArrayExports() {
        val image = createDummyImage(2, 2, 3)
        val channels = image.channels
        
        val floatArray = image.toFloatArray()
        assertEquals(2 * 2 * channels, floatArray.size)
        
        val int8Array = image.toInt8Array()
        assertEquals(2 * 2 * channels, int8Array.size)
        
        val intArray = image.toIntArray()
        assertEquals(2 * 2 * channels, intArray.size)
        
        val booleanArray = image.toBooleanArray()
        assertEquals(2 * 2 * channels, booleanArray.size)
        
        val longArray = image.toLongArray()
        assertEquals(2 * 2 * channels, longArray.size)
    }
}
