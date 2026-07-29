package io.github.kmplitert.tool.audio

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.classification.Category

/**
 * A high-level API for performing audio classification using LiteRT.
 *
 * This class coordinates the process of audio classification by delegating
 * model-specific preprocessing and postprocessing to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles audio-specific logic.
 */
class AudioClassifier(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<LiteRtAudio, List<Category>>
) {

    /**
     * Classifies the given [LiteRtAudio].
     *
     * @param audio The input audio to classify.
     * @return A list of [Category] results.
     */
    suspend fun classify(audio: LiteRtAudio): List<Category> {
        val inputBuffers = compiler.getInputBuffers()
        
        // 1. Preprocess
        handler.preprocess(audio, compiler, inputBuffers)

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
