package io.github.kmplitert.tool.expand

import io.github.kmplitert.core.LiteRTElementType
import io.github.kmplitert.core.LiteRTLayout
import io.github.kmplitert.core.LiteRTTensorType
import io.github.kmplitert.core.TFBuffer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ExpandTest {

    // --- MockTFBuffer for CoreExpand tests ---
    class MockTFBuffer : TFBuffer {
        var intArray: IntArray = intArrayOf()
        var floatArray: FloatArray = floatArrayOf()
        var byteArray: ByteArray = byteArrayOf()
        var booleanArray: BooleanArray = booleanArrayOf()
        var longArray: LongArray = longArrayOf()

        override fun writeInt(data: IntArray) { intArray = data }
        override fun writeFloat(data: FloatArray) { floatArray = data }
        override fun writeInt8(data: ByteArray) { byteArray = data }
        override fun writeBoolean(data: BooleanArray) { booleanArray = data }
        override fun writeLong(data: LongArray) { longArray = data }

        override suspend fun readInt(): IntArray = intArray
        override suspend fun readFloat(): FloatArray = floatArray
        override suspend fun readInt8(): ByteArray = byteArray
        override suspend fun readBoolean(): BooleanArray = booleanArray
        override suspend fun readLong(): LongArray = longArray
    }

    @Test
    fun testTFBufferExtensions() = runTest {
        val buffer = MockTFBuffer()
        
        floatArrayOf(1f, 2f).writeTo(buffer)
        assertContentEquals(floatArrayOf(1f, 2f), buffer.toFloatArray())

        intArrayOf(1, 2).writeTo(buffer)
        assertContentEquals(intArrayOf(1, 2), buffer.toIntArray())

        byteArrayOf(1, 2).writeTo(buffer)
        assertContentEquals(byteArrayOf(1, 2), buffer.toByteArray())

        booleanArrayOf(true, false).writeTo(buffer)
        assertContentEquals(booleanArrayOf(true, false), buffer.toBooleanArray())

        longArrayOf(1L, 2L).writeTo(buffer)
        assertContentEquals(longArrayOf(1L, 2L), buffer.toLongArray())

        buffer.writeFloatValue(3.14f)
        assertEquals(3.14f, buffer.readFloatValue())
    }

    // --- MathUtils Tests ---

    @Test
    fun testMeanAndStd() {
        val data = floatArrayOf(1.0f, 2.0f, 3.0f)
        assertEquals(2.0f, data.mean(), 1e-5f)
        assertEquals(0.8164966f, data.std(), 1e-5f)
        
        assertEquals(0f, floatArrayOf().mean())
        assertEquals(0f, floatArrayOf(1f).std())
    }

    @Test
    fun testCosineSimilarity() {
        val v1 = floatArrayOf(1.0f, 0.0f)
        val v2 = floatArrayOf(1.0f, 0.0f)
        val v3 = floatArrayOf(0.0f, 1.0f)
        
        assertEquals(1.0f, v1.cosineSimilarity(v2), 1e-5f)
        assertEquals(0.0f, v1.cosineSimilarity(v3), 1e-5f)
        
        assertFailsWith<IllegalArgumentException> {
            floatArrayOf(1f).cosineSimilarity(floatArrayOf(1f, 2f))
        }
    }

    @Test
    fun testEuclideanDistance() {
        val v1 = floatArrayOf(0f, 0f)
        val v2 = floatArrayOf(3f, 4f)
        assertEquals(5f, v1.euclideanDistance(v2), 1e-5f)

        assertFailsWith<IllegalArgumentException> {
            floatArrayOf(1f).euclideanDistance(floatArrayOf(1f, 2f))
        }
    }

    // --- PostProcessing Tests ---

    @Test
    fun testSoftmax() {
        val logits = floatArrayOf(1.0f, 2.0f, 3.0f)
        val probabilities = logits.softmax()
        assertEquals(1.0f, probabilities.sum(), 1e-5f)
        assertTrue(probabilities[2] > probabilities[1])
        assertTrue(probabilities[1] > probabilities[0])
        assertTrue(floatArrayOf().softmax().isEmpty())
    }

    @Test
    fun testSigmoid() {
        val data = floatArrayOf(-100f, 0f, 100f)
        val result = data.sigmoid()
        assertEquals(0f, result[0], 1e-5f)
        assertEquals(0.5f, result[1], 1e-5f)
        assertEquals(1f, result[2], 1e-5f)
    }

    @Test
    fun testArgmax() {
        val data = floatArrayOf(0.1f, 0.8f, 0.1f)
        assertEquals(1, data.argmax())
        assertEquals(-1, floatArrayOf().argmax())
    }

    @Test
    fun testTopK() {
        val data = floatArrayOf(0.1f, 0.5f, 0.2f, 0.8f)
        val top2 = data.topK(2)
        assertEquals(2, top2.size)
        assertEquals(3, top2[0].first)
        assertEquals(1, top2[1].first)
    }

    @Test
    fun testIou() {
        val box1 = floatArrayOf(0f, 0f, 10f, 10f)
        val box2 = floatArrayOf(5f, 5f, 15f, 15f)
        assertEquals(0.142857f, calculateIou(box1, box2), 1e-5f)
        
        val box3 = floatArrayOf(20f, 20f, 30f, 30f)
        assertEquals(0f, calculateIou(box1, box3))
    }

    @Test
    fun testPerformNms() {
        val boxes = arrayOf(
            floatArrayOf(0f, 0f, 10f, 10f),
            floatArrayOf(1f, 1f, 11f, 11f), // High overlap with box 0
            floatArrayOf(20f, 20f, 30f, 30f)
        )
        val scores = floatArrayOf(0.9f, 0.8f, 0.7f)
        val selected = performNms(boxes, scores, 0.5f)
        assertEquals(2, selected.size)
        assertTrue(selected.contains(0))
        assertTrue(selected.contains(2))
        assertFalse(selected.contains(1))
    }

    // --- TensorProcessing Tests ---

    @Test
    fun testConversions() {
        // ByteArray <-> FloatArray
        val bytes = byteArrayOf(1, -1, 127)
        val floatsFromBytes = bytes.toFloatArray()
        assertContentEquals(floatArrayOf(1f, -1f, 127f), floatsFromBytes)
        assertContentEquals(bytes, floatsFromBytes.toByteArray())

        // IntArray -> FloatArray
        val ints = intArrayOf(10, 20)
        assertContentEquals(floatArrayOf(10f, 20f), ints.toFloatArray())
        assertContentEquals(ints, floatArrayOf(10.1f, 19.9f).toIntArray())

        // BooleanArray <-> ByteArray
        val booleans = booleanArrayOf(true, false, true)
        val bytesFromBooleans = booleans.toByteArray()
        assertContentEquals(byteArrayOf(1, 0, 1), bytesFromBooleans)
        assertContentEquals(booleans, bytesFromBooleans.toBooleanArray())
    }

    @Test
    fun testNormalize() {
        val data = floatArrayOf(10f, 20f, 30f)
        val normalized = data.normalize(20f, 10f)
        assertContentEquals(floatArrayOf(-1f, 0f, 1f), normalized)
        
        assertFailsWith<IllegalArgumentException> {
            data.normalize(20f, 0f)
        }
    }

    @Test
    fun testClamp() {
        val data = floatArrayOf(-10f, 0f, 10f, 20f)
        assertContentEquals(floatArrayOf(0f, 0f, 10f, 10f), data.clamp(0f, 10f))
    }

    @Test
    fun testQuantization() {
        val data = floatArrayOf(-1f, 0f, 1f)
        // scale = 0.01, zeroPoint = 0
        // -1 / 0.01 + 0 = -100
        // 0 / 0.01 + 0 = 0
        // 1 / 0.01 + 0 = 100
        val quantized = data.quantize(0.01f, 0)
        assertContentEquals(byteArrayOf(-100, 0, 100), quantized)
        
        val dequantized = quantized.dequantize(0.01f, 0)
        assertContentEquals(data, dequantized)
    }

    @Test
    fun testPad() {
        val data = floatArrayOf(1f, 2f)
        val padded = data.pad(1, 2, 0f)
        assertContentEquals(floatArrayOf(0f, 1f, 2f, 0f, 0f), padded)
    }

    @Test
    fun testReshape() {
        val data = floatArrayOf(1f, 2f, 3f, 4f)
        data.reshape(2, 2) // Should not throw
        
        assertFailsWith<IllegalArgumentException> {
            data.reshape(3, 1)
        }
    }

    // --- Model Info Helpers ---

    @Test
    fun testLayoutTotalElements() {
        val layout = LiteRTLayout(listOf(1, 224, 224, 3), emptyList())
        assertEquals(1 * 224 * 224 * 3, layout.totalElements)

        val emptyLayout = LiteRTLayout(emptyList(), emptyList())
        assertEquals(0, emptyLayout.totalElements)
    }

    @Test
    fun testTensorTypeDimensions() {
        val layout = LiteRTLayout(listOf(1, 10), emptyList())
        val type = LiteRTTensorType(LiteRTElementType.FLOAT, layout)
        assertEquals(listOf(1, 10), type.dimensions)

        val typeNoLayout = LiteRTTensorType(LiteRTElementType.FLOAT, null)
        assertTrue(typeNoLayout.dimensions.isEmpty())
    }
}
