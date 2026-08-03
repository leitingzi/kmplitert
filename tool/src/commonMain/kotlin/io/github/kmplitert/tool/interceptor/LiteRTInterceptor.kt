package io.github.kmplitert.tool.interceptor

/**
 * Interface for intercepting LiteRT task execution.
 * Interceptors can be used for logging, caching, data transformation, etc.
 */
interface LiteRTInterceptor<I, O> {
    suspend fun intercept(chain: Chain<I, O>): O

    interface Chain<I, O> {
        val input: I
        suspend fun proceed(input: I): O
    }
}