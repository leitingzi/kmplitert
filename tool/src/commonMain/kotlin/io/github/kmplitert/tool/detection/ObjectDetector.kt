package io.github.kmplitert.tool.detection

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage
import io.github.kmplitert.tool.classification.Category

/**
 * A high-level API for performing object detection using LiteRT.
 *
 * This class coordinates the object detection process by delegating
 * model-specific preprocessing and postprocessing to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles model-specific logic.
 */
class ObjectDetector(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtImage, List<Detection>>
) {

    /**
     * Detects objects in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A list of [Detection] results.
     */
    suspend fun detect(image: LiteRtImage): List<Detection> {
        val inputBuffers = compiler.getInputBuffers()
        
        // 1. Preprocess
        handler.preprocess(image, compiler, inputBuffers)

        // 2. Inference
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // 3. Postprocess
        val detections = handler.postprocess(outputBuffers, compiler)

        // 4. Denormalize coordinates (if they are normalized)
        // Handlers typically return normalized coordinates [0, 1].
        // We convert them to pixel coordinates relative to the input image.
        return detections.map { detection ->
            val box = detection.boundingBox
            detection.copy(
                boundingBox = BoundingBox(
                    left = box.left * image.width,
                    top = box.top * image.height,
                    right = box.right * image.width,
                    bottom = box.bottom * image.height
                )
            )
        }
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }
}
