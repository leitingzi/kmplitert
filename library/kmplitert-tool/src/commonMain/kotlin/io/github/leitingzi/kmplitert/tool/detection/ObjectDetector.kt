package io.github.leitingzi.kmplitert.tool.detection

import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import io.github.leitingzi.kmplitert.core.LiteRTElementType
import io.github.leitingzi.kmplitert.tool.LiteRtImage
import io.github.leitingzi.kmplitert.tool.classification.Category
import kotlin.math.max
import kotlin.math.min

/**
 * A high-level API for performing object detection using LiteRT.
 *
 * This class simplifies image preprocessing, running inference, and post-processing
 * (including Non-Maximum Suppression) to return a list of [Detection] objects.
 *
 * This detector assumes a standard TFLite detection model output format:
 * - Output 0: Bounding boxes [1, max_detections, 4] (ymin, xmin, ymax, xmax)
 * - Output 1: Classes [1, max_detections]
 * - Output 2: Scores [1, max_detections]
 * - Output 3: Number of detections [1]
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for detection.
 */
class ObjectDetector(
    private val compiler: LiteRTCompiler,
    private val options: ObjectDetectorOptions = ObjectDetectorOptions()
) {

    /**
     * Detects objects in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A list of [Detection] results.
     */
    suspend fun detect(image: LiteRtImage): List<Detection> {
        // 1. Preprocess
        val inputType = compiler.getInputTensorType("0")
        val layout = inputType.layout ?: throw IllegalStateException("Input layout not found")
        val inputHeight = layout.dimensions[1]
        val inputWidth = layout.dimensions[2]

        val resizedImage = if (image.width != inputWidth || image.height != inputHeight) {
            image.resize(inputWidth, inputHeight)
        } else {
            image
        }

        val inputBuffers = compiler.getInputBuffers()
        val inputBuffer = inputBuffers[0]

        when (inputType.elementType) {
            LiteRTElementType.FLOAT -> {
                val floatData = resizedImage.toFloatArray(options.mean, options.std)
                inputBuffer.writeFloat(floatData)
            }
            LiteRTElementType.INT8 -> {
                val byteData = resizedImage.toInt8Array()
                inputBuffer.writeInt8(byteData)
            }
            else -> throw UnsupportedOperationException("Unsupported input element type: ${inputType.elementType}")
        }

        // 2. Inference
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // 3. Postprocess
        val locations = outputBuffers[0].readFloat() // [ymin, xmin, ymax, xmax] * max_detections
        val classes = outputBuffers[1].readFloat()
        val scores = outputBuffers[2].readFloat()
        val count = outputBuffers[3].readFloat().firstOrNull()?.toInt() ?: 0

        val detections = mutableListOf<Detection>()
        for (i in 0 until count) {
            val score = scores[i]
            if (score < options.scoreThreshold) continue

            val classIndex = classes[i].toInt()
            val label = options.labels?.getOrNull(classIndex) ?: classIndex.toString()

            val ymin = locations[i * 4]
            val xmin = locations[i * 4 + 1]
            val ymax = locations[i * 4 + 2]
            val xmax = locations[i * 4 + 3]

            // Convert normalized coordinates to pixel coordinates
            val boundingBox = BoundingBox(
                left = xmin * image.width,
                top = ymin * image.height,
                right = xmax * image.width,
                bottom = ymax * image.height
            )

            detections.add(
                Detection(
                    boundingBox = boundingBox,
                    categories = listOf(Category(label, score, classIndex))
                )
            )
        }

        // 4. NMS (Non-Maximum Suppression)
        val nmsResults = performNms(detections, options.iouThreshold)

        return if (options.maxResults > 0) {
            nmsResults.take(options.maxResults)
        } else {
            nmsResults
        }
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

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }

    companion object {
        /**
         * Creates an [ObjectDetector] from a model file.
         *
         * @param modelPath Path to the LiteRT model file.
         * @param accelerator Preferred hardware accelerator. Defaults to CPU.
         * @param options Configuration options for detection.
         * @return An initialized [ObjectDetector].
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: ObjectDetectorOptions = ObjectDetectorOptions()
        ): ObjectDetector {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return ObjectDetector(compiler, options)
        }
    }
}
