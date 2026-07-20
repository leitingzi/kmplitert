package io.github.leitingzi.kmplitert.tool.text

import io.github.leitingzi.kmplitert.core.LiteRTAccelerator
import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import kotlin.math.exp

/**
 * A high-level API for performing Question Answering using BERT-based LiteRT models.
 *
 * @param compiler The [LiteRTCompiler] instance to use for inference.
 * @param options Configuration options for question answering.
 */
class BertQuestionAnswerer(
    private val compiler: LiteRTCompiler,
    private val options: BertQuestionAnswererOptions = BertQuestionAnswererOptions()
) {

    /**
     * Answers the given question based on the provided context.
     *
     * This method assumes the model takes three IntArray inputs:
     * 1. input_ids
     * 2. input_mask
     * 3. segment_ids
     *
     * And produces two FloatArray outputs:
     * 1. start_logits
     * 2. end_logits
     *
     * @param inputIds Tokenized IDs for context and question.
     * @param inputMask Mask for input IDs.
     * @param segmentIds Segment IDs to distinguish context and question.
     * @param tokens The original tokens (used to reconstruct the answer text).
     * @return A list of [QaAnswer] results.
     */
    suspend fun answer(
        inputIds: IntArray,
        inputMask: IntArray,
        segmentIds: IntArray,
        tokens: List<String>
    ): List<QaAnswer> {
        val inputBuffers = compiler.getInputBuffers()
        
        // Populate inputs. Order depends on the model, but usually matches index.
        inputBuffers[0].writeInt(inputIds)
        inputBuffers[1].writeInt(inputMask)
        inputBuffers[2].writeInt(segmentIds)

        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        val startLogits = outputBuffers[0].readFloat()
        val endLogits = outputBuffers[1].readFloat()

        return decodeAnswers(startLogits, endLogits, tokens)
    }

    private fun decodeAnswers(
        startLogits: FloatArray,
        endLogits: FloatArray,
        tokens: List<String>
    ): List<QaAnswer> {
        val answers = mutableListOf<Pair<IntRange, Float>>()
        
        // Simple decoding: find best start and end
        // In a real implementation, we would consider top-K starts and ends.
        val maxLen = minOf(startLogits.size, endLogits.size, tokens.size)
        
        for (i in 0 until maxLen) {
            for (j in i until maxLen) {
                val score = startLogits[i] + endLogits[j]
                answers.add((i..j) to score)
            }
        }

        return answers.sortedByDescending { it.second }
            .take(options.topK)
            .map { (range, score) ->
                val text = tokens.subList(range.first, range.last + 1).joinToString(" ")
                    .replace(" ##", "") // Simple BERT subword reconstruction
                QaAnswer(text, exp(score)) // Using exp for a pseudo-probability if needed, or just score
            }
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    suspend fun close() {
        compiler.close()
    }

    companion object {
        /**
         * Creates a [BertQuestionAnswerer] from a model file.
         *
         * @param modelPath Path to the LiteRT model file.
         * @param accelerator Preferred hardware accelerator. Defaults to CPU.
         * @param options Configuration options for question answering.
         * @return An initialized [BertQuestionAnswerer].
         */
        suspend fun create(
            modelPath: String,
            accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU,
            options: BertQuestionAnswererOptions = BertQuestionAnswererOptions()
        ): BertQuestionAnswerer {
            val compiler = LiteRTCompiler(modelPath, accelerator)
            compiler.init()
            return BertQuestionAnswerer(compiler, options)
        }
    }
}
