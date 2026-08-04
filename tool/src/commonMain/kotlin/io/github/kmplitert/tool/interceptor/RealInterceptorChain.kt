package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTPhase

internal class RealInterceptorChain<I, O>(
    override val phase: LiteRTPhase,
    private val interceptors: List<LiteRTInterceptor<I, O>>,
    private val index: Int,
    override val input: I,
    override val transformedData: Any?,
    override val inputBuffers: List<TFBuffer>? = null,
    override val outputBuffers: List<TFBuffer>? = null
) : LiteRTInterceptor.Chain<I, O> {

    override suspend fun proceed(input: I, transformedData: Any?): O {
        if (index >= interceptors.size) {
            throw AssertionError("Chain reached end without base execution")
        }

        val interceptor = interceptors[index]
        
        // Auto-update phase if we hit a boundary
        val nextPhase = if (interceptor is PhaseBoundaryInterceptor<*, *>) {
            interceptor.newPhase
        } else {
            phase
        }

        val next = RealInterceptorChain(
            phase = nextPhase,
            interceptors = interceptors,
            index = index + 1,
            input = input,
            transformedData = transformedData,
            inputBuffers = inputBuffers,
            outputBuffers = outputBuffers
        )
        
        return interceptor.intercept(next)
    }
}
