package io.github.kmplitert.dataprocess.image

import android.graphics.Bitmap
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class VideoFrameAndroidTest {
    @Test
    fun bitmapFramesAndRoundTrip() {
        val bitmap = Bitmap.createBitmap(intArrayOf(0xffff0000.toInt(), 0xff0000ff.toInt()), 2, 1, Bitmap.Config.ARGB_8888)
        val frame = LiteRtImage.fromVideoFrame(bitmap, ImageRotation.ROTATION_90, ImageFlip(vertical = true))
        assertEquals(1, frame.width)
        assertEquals(2, frame.height)
        assertContentEquals(byteArrayOf(0, 0, -1, -1, 0, 0), frame.toRgb().data)
        assertContentEquals(frame.data, frame.asBitmap().asLiteRtImage().data)
        assertFailsWith<IllegalArgumentException> { LiteRtImage.fromVideoFrame("unsupported") }
    }
}
