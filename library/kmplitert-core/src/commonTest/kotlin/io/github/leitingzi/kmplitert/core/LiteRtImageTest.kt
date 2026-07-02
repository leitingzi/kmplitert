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
        // We can't directly construct LiteRtImage in common code if it doesn't have a public constructor in expect.
        // Let's check LiteRtImage expect declaration.
    }
}
