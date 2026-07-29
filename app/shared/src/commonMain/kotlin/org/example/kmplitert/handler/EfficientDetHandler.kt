package org.example.kmplitert.handler

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.classification.Category
import io.github.kmplitert.tool.detection.BoundingBox
import io.github.kmplitert.tool.detection.Detection
import kotlin.math.max
import kotlin.math.min

/**
 * A custom handler for EfficientDet-Lite0.
 *
 * Model Specs:
 * - Input: uint8[1, 320, 320, 3]
 * - Output 0: float32[1, 19206, 4] (ymin, xmin, ymax, xmax)
 * - Output 1: float32[1, 19206, 90] (scores)
 */
class EfficientDetHandler(
    private val scoreThreshold: Float = 0.3f,
    private val iouThreshold: Float = 0.5f,
    private val labels: List<String>? = null
) : LiteRTHandler<LiteRtImage, List<Detection>> {

    override suspend fun preprocess(
        input: LiteRtImage,
        compiler: LiteRTCompiler,
        inputBuffers: List<TFBuffer>
    ) {
        // EfficientDet-Lite0 expects 320x320 RGB uint8 input
        val resized = input.resize(320, 320).toRgb()
        val data = resized.toInt8Array() // Maps 0..255 to signed Byte
        inputBuffers[0].writeInt8(data)
    }

    override suspend fun postprocess(
        outputBuffers: List<TFBuffer>,
        compiler: LiteRTCompiler
    ): List<Detection> {
        val out0 = outputBuffers[0].readFloat()
        val out1 = outputBuffers[1].readFloat()

        // EfficientDet-Lite0 has 19206 anchors.
        // One output is boxes (N*4), the other is scores (N*C).
        // Let's determine which is which based on size.
        val rawBoxes: FloatArray
        val rawScores: FloatArray
        val numClasses: Int

        if (out0.size == 19206 * 4) {
            rawBoxes = out0
            rawScores = out1
            numClasses = out1.size / 19206
        } else {
            rawBoxes = out1
            rawScores = out0
            numClasses = out0.size / 19206
        }

        val numAnchors = 19206
        val candidates = mutableListOf<Detection>()

        for (i in 0 until numAnchors) {
            // Find max score and class index for this anchor
            var maxScore = 0f
            var classIndex = -1
            
            for (c in 0 until numClasses) {
                val score = rawScores[i * numClasses + c]
                if (score > maxScore) {
                    maxScore = score
                    classIndex = c
                }
            }

            if (maxScore >= scoreThreshold) {
                val ymin = rawBoxes[i * 4]
                val xmin = rawBoxes[i * 4 + 1]
                val ymax = rawBoxes[i * 4 + 2]
                val xmax = rawBoxes[i * 4 + 3]

                val label = labels?.getOrNull(classIndex) ?: classIndex.toString()
                
                candidates.add(
                    Detection(
                        boundingBox = BoundingBox(xmin, ymin, xmax, ymax),
                        categories = listOf(Category(label, maxScore, classIndex))
                    )
                )
            }
        }

        return performNms(candidates, iouThreshold)
    }

    private fun performNms(detections: List<Detection>, iouThreshold: Float): List<Detection> {
        val sortedDetections = detections.sortedByDescending { it.categories.first().score }.toMutableList()
        val results = mutableListOf<Detection>()

        while (sortedDetections.isNotEmpty()) {
            val best = sortedDetections.removeAt(0)
            results.add(best)

            val iterator = sortedDetections.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (calculateIou(best.boundingBox, next.boundingBox) > iouThreshold) {
                    iterator.remove()
                }
            }
        }

        return results
    }

    private fun calculateIou(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = max(box1.left, box2.left)
        val y1 = max(box1.top, box2.top)
        val x2 = min(box1.right, box2.right)
        val y2 = min(box1.bottom, box2.bottom)

        val intersectionWidth = max(0f, x2 - x1)
        val intersectionHeight = max(0f, y2 - y1)
        val intersectionArea = intersectionWidth * intersectionHeight

        val unionArea = box1.area + box2.area - intersectionArea

        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }
}
