package io.github.kmplitert.tool.ocr

import io.github.kmplitert.tool.detection.BoundingBox
import kotlin.test.*

class TextRecognitionTest {

    @Test
    fun testTextRecognitionResultStructure() {
        val line1 = TextLine("Hello", BoundingBox(0f, 0f, 50f, 20f))
        val line2 = TextLine("World", BoundingBox(0f, 25f, 50f, 45f))
        
        val block = TextBlock(
            text = "Hello World",
            boundingBox = BoundingBox(0f, 0f, 50f, 45f),
            lines = listOf(line1, line2)
        )
        
        val result = TextRecognitionResult(
            text = "Hello World",
            blocks = listOf(block)
        )
        
        assertEquals("Hello World", result.text)
        assertEquals(1, result.blocks.size)
        assertEquals(2, result.blocks[0].lines.size)
        assertEquals("Hello", result.blocks[0].lines[0].text)
        assertEquals(0f, result.blocks[0].boundingBox.left)
    }

    @Test
    fun testTextRecognizerOptions() {
        val options = TextRecognizerOptions(minScoreThreshold = 0.8f)
        assertEquals(0.8f, options.minScoreThreshold)
        assertEquals(127.5f, options.mean)
    }
}
