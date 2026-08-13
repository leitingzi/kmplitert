package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.core.TFBuffer

/**
 * Internal interceptors to encapsulate core LiteRT task steps.
 */
internal class PreprocessInterceptor<I, O>(
    private val preprocess: suspend (I, List<TFBuffer>) -> Unit
) : LiteRTInterceptor<I, O> {
    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        val buffers = chain.inputBuffers
        if (buffers != null) {
            preprocess(chain.input, buffers)
        }
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
