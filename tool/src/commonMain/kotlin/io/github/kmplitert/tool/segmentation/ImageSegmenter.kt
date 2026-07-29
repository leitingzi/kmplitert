package io.github.kmplitert.tool.segmentation

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for image segmentation.
 *
 * This class coordinates image segmentation by delegating model-specific logic
 * to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles image segmentation logic.
 */
class ImageSegmenter(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtImage, ImageSegmenterResult>
) {

    /**
     * Performs segmentation on the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return An [ImageSegmenterResult] containing the segmentation mask.
     */
    suspend fun segment(image: LiteRtImage): ImageSegmenterResult {
        val inputBuffers = compiler.getInputBuffers()
        
        // 1. Preprocess
        handler.preprocess(image, compiler, inputBuffers)

        // 2. Inference
        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        // 3. Postprocess
        return handler.postprocess(outputBuffers, compiler)
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }
}
