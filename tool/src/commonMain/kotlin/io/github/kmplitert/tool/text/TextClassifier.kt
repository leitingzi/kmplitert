package io.github.kmplitert.tool.text

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.classification.Category

/**
 * A high-level API for performing text classification using LiteRT.
 *
 * This class coordinates text classification by delegating model-specific logic
 * to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles text classification logic.
 */
class TextClassifier(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<IntArray, List<Category>>
) {

    /**
     * Classifies the given tokens.
     *
     * @param tokens The pre-tokenized input text.
     * @return A list of [Category] results.
     */
    suspend fun classify(tokens: IntArray): List<Category> {
        val inputBuffers = compiler.getInputBuffers()
        
        // 1. Preprocess
        handler.preprocess(tokens, compiler, inputBuffers)

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
