package io.github.kmplitert.tool.pose

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for skeletal tracking (Pose Estimation).
 *
 * This class coordinates skeletal tracking by delegating model-specific logic
 * to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles pose tracking logic.
 */
class PoseTracker(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtImage, PoseResult>
) {

    /**
     * Tracks pose in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [PoseResult] containing detected landmarks.
     */
    suspend fun track(image: LiteRtImage): PoseResult {
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
