package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTPhase

internal class RealInterceptorChain<I, O>(
    private val interceptors: List<Pair<LiteRTPhase, LiteRTInterceptor<I, O>>>,
    private val index: Int,
    override val input: I,
    override val inputBuffers: List<TFBuffer>? = null,
    override val outputBuffers: List<TFBuffer>? = null
) : LiteRTInterceptor.Chain<I, O> {

    override val phase: LiteRTPhase
        get() = if (index < interceptors.size) interceptors[index].first else LiteRTPhase.POSTPROCESS

    override suspend fun proceed(input: I): O {
        if (index >= interceptors.size) {
            throw AssertionError("Chain reached end without base execution")
        }

        val next = RealInterceptorChain(
            interceptors = interceptors,
            index = index + 1,
            input = input,
            inputBuffers = inputBuffers,
            outputBuffers = outputBuffers
        )
        
        return interceptors[index].second.intercept(next)
    }
}
