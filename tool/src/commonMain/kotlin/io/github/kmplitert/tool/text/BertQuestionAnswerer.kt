package io.github.kmplitert.tool.text

import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTHandler

/**
 * A high-level API for performing Question Answering using BERT-based LiteRT models.
 *
 * This class coordinates question answering by delegating model-specific logic
 * to a [LiteRTHandler].
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param handler The [LiteRTHandler] that handles question answering logic.
 */
class BertQuestionAnswerer(
    private val compiler: LiteRTCompiler,
    private val handler: LiteRTHandler<BertQaInput, List<QaAnswer>>
) {

    /**
     * Answers the given question based on the provided context.
     *
     * @param input The [BertQaInput] containing tokens and masks.
     * @return A list of [QaAnswer] results.
     */
    suspend fun answer(input: BertQaInput): List<QaAnswer> {
        val inputBuffers = compiler.getInputBuffers()
        
        // 1. Preprocess
        handler.preprocess(input, compiler, inputBuffers)

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
