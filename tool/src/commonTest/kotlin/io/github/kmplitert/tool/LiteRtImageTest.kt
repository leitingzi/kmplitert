package io.github.kmplitert.tool

import io.github.kmplitert.tool.image.ImageFlip
import io.github.kmplitert.tool.image.ImageRotation
import io.github.kmplitert.tool.image.LiteRtImage
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiteRtImageTest : PlatformTest() {

    private fun createDummyImage(width: Int, height: Int, channels: Int = 3): LiteRtImage {
        val data = ByteArray(width * height * channels) { it.toByte() }
        return LiteRtImage.fromRawRgb(data, width, height)
    }

    @Test
    fun testProperties() {
        val image = createDummyImage(width = 10, height = 20)
        assertEquals(expected = 10, actual = image.width)
        assertEquals(expected = 20, actual = image.height)
        assertTrue(actual = image.channels >= 3)
    }

    @Test
    fun testResize() {
        val image = createDummyImage(width = 2, height = 2)
        val resized = image.resize(width = 4, height = 8)
        assertEquals(expected = 4, actual = resized.width)
        assertEquals(expected = 8, actual = resized.height)
    }

    @Test
    fun testCrop() {
        val image = createDummyImage(width = 10, height = 10)
        val cropped = image.crop(x = 2, y = 2, width = 5, height = 5)
        assertEquals(expected = 5, actual = cropped.width)
        assertEquals(expected = 5, actual = cropped.height)
    }

    @Test
    fun testCenterCrop() {
        val image = createDummyImage(width = 10, height = 10)
        val cropped = image.centerCrop(width = 4, height = 4)
        assertEquals(expected = 4, actual = cropped.width)
        assertEquals(expected = 4, actual = cropped.height)
    }

    @Test
    fun testRotate() {
        val image = createDummyImage(width = 10, height = 10)
        val rotated = image.rotate(degrees = 90f)
        assertEquals(expected = 10, actual = rotated.width)
        assertEquals(expected = 10, actual = rotated.height)
    }

    @Test
    fun testFlip() {
        val image = createDummyImage(10, 10)
        val flipped = image.flip(horizontal = true, vertical = false)
        assertEquals(expected = 10, actual = flipped.width)
        assertEquals(expected = 10, actual = flipped.height)
    }

    @Test
    fun testColorConversions() {
        val image = createDummyImage(width = 4, height = 4)
        val grayscale = image.toGrayscale()
        assertEquals(expected = 1, actual = grayscale.channels)
        
        val rgb = grayscale.toRgb()
        assertEquals(expected = 3, actual = rgb.channels)
    }

    @Test
    fun testArrayExports() {
        val image = createDummyImage(width = 2, height = 2, channels = 3)
        val channels = image.channels
        
        val floatArray = image.toFloatArray()
        assertEquals(expected = 2 * 2 * channels, actual = floatArray.size)
        
        val int8Array = image.toInt8Array()
        assertEquals(expected = 2 * 2 * channels, actual = int8Array.size)
        
        val intArray = image.toIntArray()
        assertEquals(expected = 2 * 2 * channels, actual = intArray.size)
        
        val booleanArray = image.toBooleanArray()
        assertEquals(expected = 2 * 2 * channels, actual = booleanArray.size)
        
        val longArray = image.toLongArray()
        assertEquals(expected = 2 * 2 * channels, actual = longArray.size)
    }

    @Test
    fun testRotationEnum() {
        assertEquals(expected = 0, actual = ImageRotation.ROTATION_0.degrees)
        assertEquals(expected = 90, actual = ImageRotation.ROTATION_90.degrees)
        assertEquals(expected = 180, actual = ImageRotation.ROTATION_180.degrees)
        assertEquals(expected = 270, actual = ImageRotation.ROTATION_270.degrees)
    }

    @Test
    fun testFlipDataClass() {
        val flip = ImageFlip(horizontal = true, vertical = true)
        assertTrue(actual = flip.horizontal)
        assertTrue(actual = flip.vertical)
    }

    @Test
    fun testBufferWrites() {
        val image = createDummyImage(width = 2, height = 2, channels = 3)
        val mockBuffer = MockTFBuffer()
        
        image.writeInt8Buffer(mockBuffer)
        assertContentEquals(expected = image.toInt8Array(), actual = mockBuffer.bytes)
        
        image.writeFloatBuffer(buffer = mockBuffer, mean = 127.5f, std = 127.5f)
        assertContentEquals(
            expected = image.toFloatArray(mean = 127.5f, std = 127.5f),
            actual = mockBuffer.floats,
        )
    }
}
