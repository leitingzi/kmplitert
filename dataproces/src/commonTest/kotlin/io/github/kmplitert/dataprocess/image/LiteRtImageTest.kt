package io.github.kmplitert.dataprocess.image

import io.github.kmplitert.core.TFBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LiteRtImageTest {
    private val rgb = byteArrayOf(
        255.toByte(), 0, 0, 0, 255.toByte(), 0,
        0, 0, 255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(),
    )

    @Test
    fun rawPixelsAreCopiedAndConversionsAreUnsigned() {
        val source = rgb.copyOf()
        val image = LiteRtImage.fromRawRgb(source, 2, 2)
        source[0] = 0
        image.data[1] = 100
        assertContentEquals(rgb, image.toInt8Array())
        assertEquals(255, image.toIntArray()[0])
        assertEquals(255L, image.toLongArray()[0])
        assertEquals(true, image.toBooleanArray()[0])
        assertEquals(1f, image.toFloatArray(0f, 255f)[0])
        assertFailsWith<IllegalArgumentException> { image.toFloatArray(std = 0f) }
    }

    @Test
    fun writesTensorBuffers() {
        var bytes = byteArrayOf()
        var floats = floatArrayOf()
        val buffer = object : TFBuffer {
            override fun writeInt8(data: ByteArray) { bytes = data }
            override fun writeFloat(data: FloatArray) { floats = data }
            override fun writeInt(data: IntArray) = error("Unexpected write")
            override fun writeBoolean(data: BooleanArray) = error("Unexpected write")
            override fun writeLong(data: LongArray) = error("Unexpected write")
            override suspend fun readInt(): IntArray = error("Unexpected read")
            override suspend fun readFloat(): FloatArray = error("Unexpected read")
            override suspend fun readInt8(): ByteArray = error("Unexpected read")
            override suspend fun readBoolean(): BooleanArray = error("Unexpected read")
            override suspend fun readLong(): LongArray = error("Unexpected read")
        }
        val image = LiteRtImage.fromRawRgb(rgb, 2, 2)
        image.writeInt8Buffer(buffer)
        image.writeFloatBuffer(buffer, 1f, 2f)
        assertContentEquals(rgb, bytes)
        assertContentEquals(image.toFloatArray(1f, 2f), floats)
    }

    @Test
    fun geometryAndColorOperations() {
        val image = LiteRtImage.fromRawRgb(rgb, 2, 2)
        assertContentEquals(rgb.copyOfRange(3, 6), image.crop(1, 0, 1, 1).data)
        assertContentEquals(rgb.copyOfRange(0, 3), image.centerCrop(1, 1).data)
        assertContentEquals(byteArrayOf(0, 0, -1, -1, 0, 0, -1, -1, -1, 0, -1, 0), image.rotate(90f).data)
        assertContentEquals(byteArrayOf(0, -1, 0, -1, 0, 0, -1, -1, -1, 0, 0, -1), image.flip(true, false).data)
        assertContentEquals(byteArrayOf(76, -106, 29, -1), image.toGrayscale().data)
        assertEquals(3, image.toGrayscale().toRgb().channels)
        assertEquals(3, image.resize(3, 4).width)
        assertEquals(4, image.resize(3, 4).height)
        assertFailsWith<IllegalArgumentException> { image.crop(-1, 0, 1, 1) }
        assertFailsWith<IllegalArgumentException> { image.resize(0, 1) }
        assertFailsWith<IllegalArgumentException> { image.rotate(Float.NaN) }
    }

    @Test
    fun rawDataValidation() {
        assertFailsWith<IllegalArgumentException> { LiteRtImage.fromRawRgb(byteArrayOf(0), 1, 1) }
        assertFailsWith<IllegalArgumentException> { LiteRtImage.fromRawRgb(byteArrayOf(), Int.MAX_VALUE, 2) }
    }

    @Test
    fun rotatesNonSquareImageAndInterpolatesResize() {
        val image = LiteRtImage.fromRawRgb(byteArrayOf(0, 0, 0, 100, 100, 100), 2, 1)
        val rotated = image.rotate(90f)
        assertEquals(1, rotated.width)
        assertEquals(2, rotated.height)
        assertContentEquals(image.data, rotated.data)
        assertContentEquals(byteArrayOf(0, 0, 0, 50, 50, 50, 100, 100, 100), image.resize(3, 1).data)
    }

    @Test
    fun decodesBmpAndRejectsTruncation() {
        val bmp = ByteArray(62)
        bmp[0] = 'B'.code.toByte()
        bmp[1] = 'M'.code.toByte()
        bmp[2] = 62
        bmp[10] = 54
        bmp[14] = 40
        bmp[18] = 2
        bmp[22] = 1
        bmp[26] = 1
        bmp[28] = 24
        bmp[54] = 3
        bmp[55] = 2
        bmp[56] = 1
        bmp[57] = 6
        bmp[58] = 5
        bmp[59] = 4
        val image = BitmapDecoder.decodeToLiteRtImage(bmp)
        assertEquals(2, image.width)
        assertEquals(1, image.height)
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5, 6), image.toRgb().data)
        assertFailsWith<IllegalArgumentException> { BitmapDecoder.decodeToLiteRtImage(bmp.copyOf(60)) }
        bmp[22] = -1
        bmp[23] = -1
        bmp[24] = -1
        bmp[25] = -1
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5, 6), BitmapDecoder.decodeToLiteRtImage(bmp).data)
        bmp[30] = 1
        assertFailsWith<IllegalArgumentException> { BitmapDecoder.decodeToLiteRtImage(bmp) }
    }

    @Test
    fun bmpRowOrientationAndHighByteAreRespected() {
        val bmp = ByteArray(62)
        bmp[0] = 'B'.code.toByte()
        bmp[1] = 'M'.code.toByte()
        bmp[10] = 54
        bmp[14] = 40
        bmp[18] = 1
        bmp[22] = 2
        bmp[26] = 1
        bmp[28] = 24
        bmp[54] = 1
        bmp[58] = 2
        assertContentEquals(byteArrayOf(0, 0, 2, 0, 0, 1), BitmapDecoder.decodeToLiteRtImage(bmp).data)
        bmp[22] = -2
        bmp[23] = -1
        bmp[24] = -1
        bmp[25] = -1
        assertContentEquals(byteArrayOf(0, 0, 1, 0, 0, 2), BitmapDecoder.decodeToLiteRtImage(bmp).data)
        bmp[13] = 1
        assertFailsWith<IllegalArgumentException> { BitmapDecoder.decodeToLiteRtImage(bmp) }
    }
}
