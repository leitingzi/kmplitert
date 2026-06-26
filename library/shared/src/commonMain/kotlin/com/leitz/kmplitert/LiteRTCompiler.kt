@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.leitz.kmplitert

/**
 * Compiles and executes a LiteRT model.
 *
 * This class provides a platform-independent API for loading a LiteRT model,
 * querying its input and output buffers, executing inference, and releasing
 * associated resources.
 *
 * Instances must be initialized by calling [init] before any other operations.
 *
 * @property filePath Path to the LiteRT model file.
 */
expect class LiteRTCompiler(filePath: String) {

    /**
     * Initializes the compiler and prepares the model for inference.
     *
     * This method must be called before invoking [getInputBuffers],
     * [getOutputBuffers], or [run].
     */
    suspend fun init()

    /**
     * Returns the input buffers required by the compiled model.
     *
     * The returned buffers describe the expected input tensors and can be
     * populated with data before calling [run].
     *
     * @return A list of input buffers.
     */
    suspend fun getInputBuffers(): List<TFBuffer>

    /**
     * Returns the output buffers required by the compiled model.
     *
     * The returned buffers receive inference results after [run] completes.
     *
     * @return A list of output buffers.
     */
    suspend fun getOutputBuffers(): List<TFBuffer>

    /**
     * Executes inference using the provided input and output buffers.
     *
     * The input and output buffer lists must match the model's expected tensor
     * layout and ordering.
     *
     * @param inputs The input buffers containing tensor data.
     * @param outputs The output buffers that will receive inference results.
     */
    suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>)

    /**
     * Releases all resources associated with this compiler.
     *
     * After calling this method, the instance must not be used again.
     */
    suspend fun close()
}