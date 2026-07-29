package io.github.kmplitert.tool.gesture

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRtImage

/**
 * A high-level API for hand landmark detection and gesture recognition.
 *
 * This class handles image preprocessing and maps the model output tensors
 * to structured hand landmark data and recognized gestures by delegating
 * to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles hand gesture logic.
 */
class HandGestureRecognizer(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtImage, HandGestureResult>
) {

    /**
     * Recognizes hand landmarks and gestures in the given [LiteRtImage].
     *
     * @param image The input image to process.
     * @return A [HandGestureResult] containing detected landmarks and gestures.
     */
    suspend fun recognize(image: LiteRtImage): HandGestureResult {
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
