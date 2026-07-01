@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi

/**
 * Compiles and executes a LiteRT model.
 *
 * Call [init] before using this instance and [close] when it is no longer needed.
 *
 * @property filePath Path to the LiteRT model file.
 * @property accelerator Preferred hardware accelerators.
 */
expect class LiteRTCompiler(filePath: String, accelerator: LiteRTAccelerator) {

    /**
     * Initializes the model for inference.
     */
    suspend fun init()

    /**
     * Returns the model input buffers.
     */
    suspend fun getInputBuffers(): List<TFBuffer>

    /**
     * Returns the model output buffers.
     */
    suspend fun getOutputBuffers(): List<TFBuffer>

    /**
     * Runs inference.
     *
     * @param inputs Input buffers.
     * @param outputs Output buffers.
     */
    suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>)

    /**
     * Releases all associated resources.
     */
    suspend fun close()
}