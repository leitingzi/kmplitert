package io.github.kmplitert.tool.expand

import io.github.kmplitert.tool.interceptor.LiteRTInterceptor

/**
 * Proceeds with the current input and transformed data in the chain.
 */
suspend fun <I, T, O> LiteRTInterceptor.Chain<I, T, O>.proceed(): O {
    return proceed(input = input, transformedData = transformedData)
}