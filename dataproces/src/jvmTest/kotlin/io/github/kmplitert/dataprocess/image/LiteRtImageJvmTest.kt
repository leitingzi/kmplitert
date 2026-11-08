package io.github.kmplitert.dataprocess.image

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LiteRtImageJvmTest {
    @Test
    fun decodesPngWithAlpha() {
        val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(0, 0, 0x80402010.toInt())
        val output = ByteArrayOutputStream()
        check(ImageIO.write(image, "png", output))

        val decoded = LiteRtImage(output.toByteArray())
        assertEquals(4, decoded.channels)
        assertContentEquals(byteArrayOf(64, 32, 16, -128), decoded.data)
        assertContentEquals(byteArrayOf(64, 32, 16), decoded.toRgb().data)
    }

    @Test
    fun rejectsInvalidImage() {
        assertFailsWith<IllegalArgumentException> { LiteRtImage(byteArrayOf(1, 2, 3)) }
    }

    @Test
    fun convertsVideoFrameAndAppliesRotationThenFlip() {
        val source = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB)
        source.setRGB(0, 0, 0xff0000)
        source.setRGB(1, 0, 0x0000ff)
        val result = LiteRtImage.fromVideoFrame(source, ImageRotation.ROTATION_90, ImageFlip(vertical = true))
        assertEquals(1, result.width)
        assertEquals(2, result.height)
        assertContentEquals(byteArrayOf(0, 0, -1, -1, 0, 0), result.data)
        assertContentEquals(result.data, result.asBufferedImage().asLiteRtImage().data)
    }

    @Test
    fun convertsNv12AndNv21WithExplicitDimensions() {
        val nv21 = ByteBuffer.wrap(byteArrayOf(0, 0, 0, 0, -128, -128))
        val image = LiteRtImage.fromYuvByteBuffer(nv21, 2, 2)
        assertContentEquals(ByteArray(12), image.data)
        assertEquals(0, nv21.position())
        val nv12 = ByteBuffer.wrap(byteArrayOf(-1, -1, -1, -1, -128, -128))
        assertContentEquals(ByteArray(12) { -1 }, LiteRtImage.fromYuvByteBuffer(nv12, 2, 2, false).data)
        val colorNv21 = ByteBuffer.wrap(byteArrayOf(99, 76, 76, 76, 76, -1, 85)).apply { position(1) }
        val colorNv12 = ByteBuffer.wrap(byteArrayOf(76, 76, 76, 76, 85, -1))
        val red = LiteRtImage.fromYuvByteBuffer(colorNv21, 2, 2).data
        assertContentEquals(red, LiteRtImage.fromYuvByteBuffer(colorNv12, 2, 2, false).data)
        assertEquals(254, red[0].toInt() and 255)
        assertEquals(1, colorNv21.position())
        assertFailsWith<IllegalArgumentException> { LiteRtImage.fromYuvByteBuffer(ByteBuffer.allocate(5), 2, 2) }
        assertFailsWith<IllegalArgumentException> { LiteRtImage.fromVideoFrame(nv21) }
    }
}
