package io.github.leitingzi.kmplitert.tool.expand

import kotlin.test.*

class ExpandTest {

    @Test
    fun testSoftmax() {
        val logits = floatArrayOf(1.0f, 2.0f, 3.0f)
        val probabilities = logits.softmax()
        assertEquals(1.0f, probabilities.sum(), 1e-5f)
        assertTrue(probabilities[2] > probabilities[1])
        assertTrue(probabilities[1] > probabilities[0])
    }

    @Test
    fun testArgmax() {
        val data = floatArrayOf(0.1f, 0.8f, 0.1f)
        assertEquals(1, data.argmax())
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
    fun testCosineSimilarity() {
        val v1 = floatArrayOf(1.0f, 0.0f)
        val v2 = floatArrayOf(1.0f, 0.0f)
        val v3 = floatArrayOf(0.0f, 1.0f)
        
        assertEquals(1.0f, v1.cosineSimilarity(v2), 1e-5f)
        assertEquals(0.0f, v1.cosineSimilarity(v3), 1e-5f)
    }

    @Test
    fun testMeanAndStd() {
        val data = floatArrayOf(1.0f, 2.0f, 3.0f)
        assertEquals(2.0f, data.mean(), 1e-5f)
        // variance = ((1-2)^2 + (2-2)^2 + (3-2)^2) / 3 = (1 + 0 + 1) / 3 = 0.666...
        // std = sqrt(0.666...) = 0.816...
        assertEquals(0.8164966f, data.std(), 1e-5f)
    }

    @Test
    fun testIou() {
        val box1 = floatArrayOf(0f, 0f, 10f, 10f)
        val box2 = floatArrayOf(5f, 5f, 15f, 15f)
        // intersection: [5, 5, 10, 10] -> area = 25
        // area1 = 100, area2 = 100
        // union = 100 + 100 - 25 = 175
        // iou = 25 / 175 = 1/7 = 0.1428...
        assertEquals(0.142857f, calculateIou(box1, box2), 1e-5f)
    }
}
