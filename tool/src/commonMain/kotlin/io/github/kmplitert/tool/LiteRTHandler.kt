package io.github.kmplitert.tool

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer

/**
 * A generic interface for model-specific preprocessing and postprocessing logic.
 *
 * @param I The type of the input data to be preprocessed (e.g., LiteRtImage).
 * @param O The type of the final result returned after postprocessing.
 */
interface LiteRTHandler<I, O> {

    /**
     * Performs preprocessing on the input data and fills the input buffers.
     *
     * @param input The input data to process.
     * @param compiler The [LiteRTCompiler] instance.
     * @param inputBuffers The list of input buffers to be filled.
     */
    suspend fun preprocess(input: I, compiler: LiteRTCompiler, inputBuffers: List<TFBuffer>)

    /**
     * Performs postprocessing on the output buffers and returns the inference results.
     *
     * @param outputBuffers The list of output buffers containing inference results.
     * @param compiler The [LiteRTCompiler] instance.
     * @return The processed result of type [O].
     */
    suspend fun postprocess(outputBuffers: List<TFBuffer>, compiler: LiteRTCompiler): O
}
