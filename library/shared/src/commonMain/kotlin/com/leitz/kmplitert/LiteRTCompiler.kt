package com.leitz.kmplitert

/**
 * Cross-platform compiler wrapper for LiteRT model inference.
 *
 * Provides suspend APIs to initialize model, manage input/output buffers,
 * execute inference and release native resources.
 */
expect class LiteRTCompiler() {
    /**
     * Initialize the LiteRT model from specified file path.
     *
     * @param filePath Absolute path to the LiteRT model file.
     */
    suspend fun init(filePath: String)

    /**
     * Retrieve all input tensor buffers of the loaded model.
     *
     * @return List of input TFBuffer instances.
     */
    suspend fun getInputBuffers(): List<TFBuffer>

    /**
     * Retrieve all output tensor buffers of the loaded model.
     *
     * @return List of output TFBuffer instances.
     */
    suspend fun getOutputBuffers(): List<TFBuffer>

    /**
     * Run model inference with given input and output buffers.
     *
     * @param inputs Prepared input data buffers
     * @param outputs Buffers to receive inference result data
     */
    suspend fun run(inputs: List<TFBuffer>, outputs: List<TFBuffer>)

    /**
     * Release all allocated native memory and model resources.
     * Call this method when the compiler is no longer used.
     */
    suspend fun close()
}