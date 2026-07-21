package io.github.kmplitert.tool.text

import kotlin.test.Test
import kotlin.test.assertEquals

class BertQuestionAnswererTest {

    @Test
    fun testQaAnswerStructure() {
        val answer = QaAnswer("The answer", 0.95f)
        assertEquals("The answer", answer.text)
        assertEquals(0.95f, answer.score)
    }

    @Test
    fun testOptions() {
        val options = BertQuestionAnswererOptions(topK = 5)
        assertEquals(5, options.topK)
    }
}
