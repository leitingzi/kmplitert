package io.github.kmplitert.dataprocess.image

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class YuvFrameAndroidTest {
    @Test
    fun respectsPlanePositionsAndStrides() {
        val y = ByteBuffer.wrap(byteArrayOf(99, 76, 76, 0, 76, 76)).apply { position(1) }
        val u = ByteBuffer.wrap(byteArrayOf(0, 85, 0)).apply { position(1) }
        val v = ByteBuffer.wrap(byteArrayOf(0, -1, 0)).apply { position(1) }
        val planes = listOf(YuvPlane(y, 3, 1), YuvPlane(u, 2, 2), YuvPlane(v, 2, 2))
        val image = fromYuv420Planes(2, 2, planes)
        assertContentEquals(ByteArray(12) { if (it % 3 == 0) 254.toByte() else 0 }, image.data)
        assertEquals(1, y.position())
        assertFailsWith<IllegalArgumentException> { fromYuv420Planes(3, 2, planes) }
    }
}
