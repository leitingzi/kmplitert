package io.github.kmplitert.tool

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer

/**
 * A generic base class for model-specific preprocessing and postprocessing logic,
 * combined with high-level task orchestration.
 *
 * @param I The type of the input data to be preprocessed (e.g., LiteRtImage).
 * @param O The type of the final result returned after postprocessing.
 */
abstract class LiteRTHandler<I, O> {

    /**
     * The [LiteRTCompiler] instance used for inference.
     */
    protected abstract val compiler: LiteRTCompiler

    /**
     * Initializes the handler and its underlying resources.
     * This is an optional step that can be called before the first [runTask].
     */
    open suspend fun init() {
        // Optional initialization
    }

    /**
     * Performs preprocessing on the input data and fills the input buffers.
     *
     * @param input The input data to process.
     * @param inputBuffers The list of input buffers to be filled.
     */
    protected abstract suspend fun preprocess(input: I, inputBuffers: List<TFBuffer>)

    /**
     * Performs postprocessing on the output buffers and returns the inference results.
     *
     * @param outputBuffers The list of output buffers containing inference results.
     * @return The processed result of type [O].
     */
    protected abstract suspend fun postprocess(outputBuffers: List<TFBuffer>): O

    /**
     * Executes the full LiteRT task: preprocess -> run -> postprocess.
     *
     * @param input The input data to process.
     * @return The final processed result.
     */
    suspend fun runTask(input: I): O {
        val inputBuffers = compiler.getInputBuffers()
        preprocess(input, inputBuffers)

        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        return postprocess(outputBuffers)
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    open suspend fun close() {
        compiler.close()
    }
}
