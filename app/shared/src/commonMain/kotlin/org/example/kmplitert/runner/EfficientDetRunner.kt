package org.example.kmplitert.runner

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.image.LiteRtImage
import io.github.kmplitert.tool.Detection
import io.github.kmplitert.tool.Category
import io.github.kmplitert.tool.BoundingBox
import kotlin.math.max
import kotlin.math.min

class EfficientDetRunner(
    private val scoreThreshold: Float = 0.4f,
    private val iouThreshold: Float = 0.5f,
    private val labels: List<String>? = null
) : BaseLiteRTRunner<LiteRtImage, List<Detection>>(
    modelResourcePath = "files/efficientdet_lite0.tflite",
    accelerator = LiteRTAccelerator.CPU
) {

    override suspend fun preprocess(input: LiteRtImage, inputBuffers: List<TFBuffer>) {
        // EfficientDet-Lite0 expects 320x320 RGB uint8 input
        val resized = input.resize(320, 320).toRgb()
        val data = resized.toInt8Array()
        inputBuffers[0].writeInt8(data)
    }

    override suspend fun postprocess(outputBuffers: List<TFBuffer>): List<Detection> {
        val out0 = outputBuffers[0].readFloat()
        val out1 = outputBuffers[1].readFloat()

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
