package io.github.kmplitert.core

import io.github.kmplitert.core.model.LiteRtGpuBackend
import io.github.kmplitert.core.model.LiteRtHwAcceleratorSet
import io.github.kmplitert.core.model.LiteRtLayout
import io.github.kmplitert.core.model.LiteRtRankedTensorType
import io.github.kmplitert.core.model.LiteRtTensorBufferLockMode
import io.github.kmplitert.core.model.LiteRtTensorBufferType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiteRtAbiTest {
    private class LayoutProbe : LiteRtLayout() {
        fun offsetOf(field: String): Int = fieldOffset(field)
    }

    private class RankedTensorTypeProbe : LiteRtRankedTensorType() {
        fun offsetOf(field: String): Int = fieldOffset(field)
    }

    @Test
    fun layoutMatchesNativeAbi() {
        val layout = LayoutProbe()
        val rankedType = RankedTensorTypeProbe()

        assertEquals(68, layout.size())
        assertEquals(4, layout.offsetOf("dimensions"))
        assertEquals(36, layout.offsetOf("strides"))
        assertEquals(72, rankedType.size())
        assertEquals(4, rankedType.offsetOf("layout"))

        layout.setRank(7)
        layout.setHasStrides(true)
        assertEquals(7, layout.getRank())
        assertTrue(layout.hasStrides())

        layout.setRank(2)
        assertEquals(2, layout.getRank())
        assertTrue(layout.hasStrides())
        layout.setHasStrides(false)
        assertFalse(layout.hasStrides())
        assertEquals(2, layout.getRank())
    }

    @Test
    fun nativeEnumsMatchHeaderValues() {
        assertEquals(
            setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23),
            LiteRTElementType.entries.map { it.value }.toSet()
        )
        assertEquals(listOf(0, 1, 2, 3), LiteRtGpuBackend.entries.map { it.value })
        assertEquals(listOf(0, 1, 2), LiteRtTensorBufferLockMode.entries.map { it.value })
        assertEquals(
            setOf(
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                20,
                21,
                22,
                23,
                24,
                25,
                26,
                30,
                31,
                32,
                33,
                34,
                40,
                41,
                42,
                43,
                44,
                45,
                46,
                100
            ),
            LiteRtTensorBufferType.entries.map { it.value }.toSet()
        )
        assertEquals(
            setOf(
                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                20,
                21,
                22,
                23,
                24,
                25,
                26,
                30,
                31,
                32,
                33,
                34,
                40,
                41,
                42,
                43,
                44,
                45,
                46,
                100,
                199
            ),
            LiteRTTensorBufferType.entries.map { it.type }.toSet()
        )
        assertEquals(8, LiteRtHwAcceleratorSet.WEBNN.value)
        assertEquals(30, LiteRTTensorBufferType.MetalBuffer.type)
        assertEquals(100, LiteRTTensorBufferType.UserCustomBuffer.type)
        assertEquals(100, LiteRTTensorBufferType.OpenVINOTensorBuffer.type)
    }

    @Test
    fun sizeTUsesNativePointerWidth() {
        assertEquals(1234L, SizeT(1234L).toLong())
        val reference = SizeTByReference()
        reference.value = 1234L
        assertEquals(1234L, reference.value)
    }
}
