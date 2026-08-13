package io.github.kmplitert.tool.expand

import io.github.kmplitert.tool.interceptor.LiteRTInterceptor

/**
 * Proceeds with the current input in the chain.
 */
suspend fun <I, O> LiteRTInterceptor.Chain<I, O>.proceed(): O {
    return proceed(input = input)
}
