package io.github.kmplitert.tool.classification

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for performing image classification using LiteRT.
 *
 * This class coordinates the process of image classification by delegating
 * model-specific preprocessing and postprocessing to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles model-specific logic.
 */
class ImageClassifier(
    val compiler: LiteRTCompiler,
    val handler: LiteRTHandler<LiteRtImage, List<Category>>
) {

    /**
     * Classifies the given [LiteRtImage].
     *
     * This method handles the orchestration of the classification process:
     * 1. Preprocesses the image using the provided handler.
     * 2. Runs inference using the underlying compiler.
     * 3. Postprocesses the results using the provided handler.
     *
     * @param image The input image to classify.
     * @return A list of [Category] results.
     */
    suspend fun classify(image: LiteRtImage): List<Category> {
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
