@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.kmplitert.dataprocess.image

import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VideoFrameWebTest {
    @Test
    fun canvasFrameRoundTripAndTransform() {
        val source = LiteRtImage.fromRawRgb(byteArrayOf(-1, 0, 0, 0, 0, -1), 2, 1)
        val canvas = source.asCanvas()
        val frame = LiteRtImage.fromVideoFrame(canvas, ImageRotation.ROTATION_90, ImageFlip(vertical = true))
        assertEquals(1, frame.width)
        assertEquals(2, frame.height)
        assertContentEquals(byteArrayOf(0, 0, -1, -1, 0, 0), frame.toRgb().data)
        assertContentEquals(source.data, canvas.asLiteRtImage().toRgb().data)
        val empty = document.createElement("canvas") as HTMLCanvasElement
        empty.width = 0
        empty.height = 0
        assertFailsWith<IllegalArgumentException> { empty.asLiteRtImage() }
    }
}
