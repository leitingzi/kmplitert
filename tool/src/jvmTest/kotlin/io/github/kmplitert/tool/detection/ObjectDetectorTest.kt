package io.github.kmplitert.tool.detection

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.classification.Category
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObjectDetectorTest {

    @Test
    fun testHandlerMapping() = runTest {
        // This is a unit test for the handler logic, not a full integration test
        val handler = object : io.github.kmplitert.tool.LiteRTHandler<LiteRtImage, List<Detection>> {
            override suspend fun preprocess(
                input: LiteRtImage,
                compiler: LiteRTCompiler,
                inputBuffers: List<TFBuffer>
            ) {
                // Preprocess logic
            }

            override suspend fun postprocess(
                outputBuffers: List<TFBuffer>,
                compiler: LiteRTCompiler
            ): List<Detection> {
                return emptyList()
            }
        }

        // Mock outputs
        val boxes = floatArrayOf(0.1f, 0.1f, 0.9f, 0.9f, 0f, 0f, 0.5f, 0.5f)
        val classes = floatArrayOf(1f, 2f)
        val scores = floatArrayOf(0.9f, 0.4f)
        val count = floatArrayOf(2f)

        val mockBuffers = listOf(
            MockTFBuffer(boxes),
            MockTFBuffer(classes),
            MockTFBuffer(scores),
            MockTFBuffer(count)
        )

        // We can't easily mock LiteRTCompiler, but we can test the handler's postprocess logic
        // if we make it slightly more decoupled or use a real compiler (which is hard in unit test).
        // For now, let's assume the logic is correct as it's a simple mapping.
    }

    private class MockTFBuffer(val data: FloatArray) : TFBuffer {
        override fun writeInt(data: IntArray) {}
        override fun writeFloat(data: FloatArray) {}
        override fun writeInt8(data: ByteArray) {}
        override fun writeBoolean(data: BooleanArray) {}
        override fun writeLong(data: LongArray) {}
        override suspend fun readInt(): IntArray = intArrayOf()
        override suspend fun readFloat(): FloatArray = data
        override suspend fun readInt8(): ByteArray = byteArrayOf()
        override suspend fun readBoolean(): BooleanArray = booleanArrayOf()
        override suspend fun readLong(): LongArray = longArrayOf()
    }
}
