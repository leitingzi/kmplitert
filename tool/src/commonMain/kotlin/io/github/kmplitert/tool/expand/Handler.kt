package io.github.kmplitert.tool.expand

import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRTPhase
import io.github.kmplitert.tool.interceptor.LiteRTLoggingInterceptor
import io.github.kmplitert.tool.interceptor.LiteRTResultCacheInterceptor

/**
 * Shorthand for adding a logging interceptor.
 */
fun <I, T, O> LiteRTHandler<I, T, O>.addLogging(
    tag: String = "LiteRT",
    phase: LiteRTPhase = LiteRTPhase.TASK,
    logger: (String) -> Unit = { println(it) }
) {
    addInterceptor(LiteRTLoggingInterceptor(tag, logger), phase = phase)
}

/**
 * Shorthand for adding a result cache interceptor.
 */
fun <I, T, O> LiteRTHandler<I, T, O>.addCache(
    onCacheHit: ((I, O) -> Unit)? = null,
    calculateFingerprint: (I) -> Any = { it.hashCode() }
) {
    addInterceptor(LiteRTResultCacheInterceptor(onCacheHit, calculateFingerprint), phase = LiteRTPhase.TASK)
}
