package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.core.TFBuffer

/**
 * Internal interceptors to encapsulate core LiteRT task steps.
 */
internal class TransformInterceptor<I, T, O>(
    private val transform: suspend (I) -> T
) : LiteRTInterceptor<I, T, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, T, O>): O {
        val data = transform(chain.input)
        return chain.proceed(transformedData = data)
    }
}

internal class FeedInterceptor<I, T, O>(
    private val feed: suspend (T, List<TFBuffer>) -> Unit
) : LiteRTInterceptor<I, T, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, T, O>): O {
        val buffers = chain.inputBuffers
        val data = chain.transformedData
        if (buffers != null && data != null) {
            feed(data, buffers)
        }
        return chain.proceed()
    }
}

internal class InferenceInterceptor<I, T, O>(
    private val inference: suspend (List<TFBuffer>, List<TFBuffer>) -> Unit
) : LiteRTInterceptor<I, T, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, T, O>): O {
        if (chain.inputBuffers != null && chain.outputBuffers != null) {
            inference(chain.inputBuffers!!, chain.outputBuffers!!)
        }
        return chain.proceed()
    }
}

internal class PostprocessInterceptor<I, T, O>(
    private val postprocess: suspend (List<TFBuffer>) -> O
) : LiteRTInterceptor<I, T, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, T, O>): O {
        return postprocess(chain.outputBuffers ?: emptyList())
    }
}
