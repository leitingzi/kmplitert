package io.github.kmplitert.tool.classification

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.LiteRTFileUtils
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ImageClassifierTest {

    @Test
    fun testCategorySorting() {
        val categories = listOf(
            Category("A", 0.1f, 0),
            Category("B", 0.8f, 1),
            Category("C", 0.5f, 2)
        )
        val sorted = categories.sortedByDescending { it.score }
        assertEquals("B", sorted[0].label)
        assertEquals("C", sorted[1].label)
        assertEquals("A", sorted[2].label)
    }

    @Test
    fun testClassificationWithMobileNet() = runTest {
        val modelBytes = try {
            loadResourceAsBytes("mobilenet_v1.tflite")
        } catch (e: Exception) {
            println("Skipping test: MobileNet model not found: ${e.message}")
            return@runTest
        }

        val modelPath = LiteRTFileUtils.createFileFromByteArray(modelBytes, "mobilenet_v1.tflite")
        val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
        
        try {
            compiler.init()

            // Define a handler for MobileNet
            val handler = object : io.github.kmplitert.tool.LiteRTHandler<LiteRtImage, List<Category>> {
                override suspend fun preprocess(
                    input: LiteRtImage,
                    compiler: LiteRTCompiler,
                    inputBuffers: List<io.github.kmplitert.core.TFBuffer>
                ) {
                    val data = input.resize(224, 224).toFloatArray(127.5f, 127.5f)
                    inputBuffers[0].writeFloat(data)
                }

                override suspend fun postprocess(
                    outputBuffers: List<io.github.kmplitert.core.TFBuffer>,
                    compiler: LiteRTCompiler
                ): List<Category> {
                    val scores = outputBuffers[0].readFloat()
                    return scores.mapIndexed { index, score ->
                        Category(index.toString(), score, index)
                    }.sortedByDescending { it.score }.take(3)
                }
            }
            
            val classifier = ImageClassifier(
                compiler = compiler,
                handler = handler
            )

            // Create a dummy black image (224x224)
            val dummyData = ByteArray(224 * 224 * 3) { 0 }
            val image = LiteRtImage.fromRawRgb(dummyData, 224, 224)

            val results = classifier.classify(image)
            
            assertTrue(results.isNotEmpty(), "Results should not be empty")
            assertTrue(results.size <= 3, "Results size should be <= topK")
            
            println("Top result: ${results[0].label} (${results[0].score})")
            
        } finally {
            compiler.close()
        }
    }
}
