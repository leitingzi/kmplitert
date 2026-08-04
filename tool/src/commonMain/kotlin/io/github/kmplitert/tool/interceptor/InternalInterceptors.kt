package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.LiteRTPhase

/**
 * Internal interceptors to encapsulate core LiteRT task steps.
 */
internal class PhaseBoundaryInterceptor<I, O>(
    val newPhase: LiteRTPhase
) : LiteRTInterceptor<I, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        return chain.proceed()
    }
}

internal class TransformInterceptor<I, O>(
    private val transform: suspend (I) -> Any?
) : LiteRTInterceptor<I, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        val data = transform(chain.input)
        return chain.proceed(transformedData = data)
    }
}

internal class FeedInterceptor<I, O>(
    private val feed: suspend (Any?, List<TFBuffer>) -> Unit
) : LiteRTInterceptor<I, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        chain.inputBuffers?.let { feed(chain.transformedData, it) }
        return chain.proceed()
    }
}

internal class InferenceInterceptor<I, O>(
    private val inference: suspend (List<TFBuffer>, List<TFBuffer>) -> Unit
) : LiteRTInterceptor<I, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        if (chain.inputBuffers != null && chain.outputBuffers != null) {
            inference(chain.inputBuffers!!, chain.outputBuffers!!)
        }
        return chain.proceed()
    }
}

internal class PostprocessInterceptor<I, O>(
    private val postprocess: suspend (List<TFBuffer>) -> O
) : LiteRTInterceptor<I, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        return postprocess(chain.outputBuffers ?: emptyList())
    }
}
