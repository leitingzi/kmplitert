package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTPhase

/**
 * Interface for intercepting LiteRT task execution.
 *
 * @param I The type of the input data.
 * @param O The type of the final result.
 */
fun interface LiteRTInterceptor<I, O> {
    suspend fun intercept(chain: Chain<I, O>): O

    interface Chain<I, O> {
        /** The original input data. */
        val input: I

        /** The current phase of the chain. */
        val phase: LiteRTPhase

        /** The input buffers. Available in PREPROCESS, INFERENCE, and POSTPROCESS. */
        val inputBuffers: List<TFBuffer>?

        /** The output buffers. Available after inference. */
        val outputBuffers: List<TFBuffer>?

        /**
         * Continues the execution of the chain.
         */
        suspend fun proceed(input: I = this.input): O
    }
}
