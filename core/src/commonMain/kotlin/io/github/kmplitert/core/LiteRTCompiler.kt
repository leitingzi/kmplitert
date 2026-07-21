@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.kmplitert.core

/**
 * Compiles and executes a LiteRT model.
 *
 * Call [init] before using this instance and [close] when it is no longer needed.
 *
 * @property filePath Path to the LiteRT model file.
 * @property accelerator Preferred hardware accelerator.
 */
expect class LiteRTCompiler(filePath: String, accelerator: LiteRTAccelerator) {

    /**
     * Initializes the model and prepares it for inference.
     *
     * This function must be called before invoking any other API on this
     * compiler instance.
     */
    suspend fun init()

    /**
     * Returns the tensor type information for the specified input tensor.
     *
     * @param inputName Name of the input tensor.
     * @return The input tensor type.
     */
    suspend fun getInputTensorType(inputName: String): LiteRTTensorType

    /**
     * Returns the buffer requirements for the specified input tensor.
     *
     * The returned requirements describe the memory layout and size needed
     * to create a compatible input buffer.
     *
     * @param inputName Name of the input tensor.
     * @return Buffer requirements for the input tensor.
     */
    suspend fun getInputBufferRequirements(inputName: String): LiteRTBufferRequirements

    /**
     * Returns all input buffers allocated for the model.
     *
     * @param signatureIndex Index of the signature to get buffers for.
     * @return A list of input buffers.
     */
    suspend fun getInputBuffers(signatureIndex: Int = 0): List<TFBuffer>

    /**
     * Returns the tensor type information for the specified output tensor.
     *
     * @param outputName Name of the output tensor.
     * @return The output tensor type.
     */
    suspend fun getOutputTensorType(outputName: String): LiteRTTensorType

    /**
     * Returns the buffer requirements for the specified output tensor.
     *
     * The returned requirements describe the memory layout and size needed
     * to create a compatible input buffer.
     *
     * @param outputName Name of the output tensor.
     * @return Buffer requirements for the output tensor.
     */
    suspend fun getOutputBufferRequirements(outputName: String): LiteRTBufferRequirements

    /**
     * Returns all output buffers allocated for the model.
     *
     * @param signatureIndex Index of the signature to get buffers for.
     * @return A list of output buffers.
     */
    suspend fun getOutputBuffers(signatureIndex: Int = 0): List<TFBuffer>

    /**
     * Executes inference using the provided input and output buffers.
     *
     * The supplied buffers must satisfy the corresponding tensor buffer
     * requirements reported by the model.
     *
     * @param inputs Input buffers containing the inference data.
     * @param outputs Output buffers that will receive the inference results.
     * @param signatureIndex Index of the signature to run.
     */
    suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>, signatureIndex: Int = 0)

    /**
     * Releases all native resources associated with this compiler.
     *
     * Once this function has been called, the compiler instance can no longer
     * be used.
     */
    suspend fun close()
}

