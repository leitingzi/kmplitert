package io.github.kmplitert.tool.face

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for face analysis, including landmarks, orientation, and expressions.
 *
 * This class coordinates the process of face analysis by delegating
 * model-specific preprocessing and postprocessing to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles face analysis logic.
 */
class FaceAnalyzer(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtImage, FaceAnalysisResult>
) {

    /**
     * Analyzes the face in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [FaceAnalysisResult] containing the analysis data.
     */
    suspend fun analyze(image: LiteRtImage): FaceAnalysisResult {
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
