package io.github.kmplitert.tool.expand

import io.github.kmplitert.tool.LiteRTHandler
import io.github.kmplitert.tool.LiteRTPhase
import io.github.kmplitert.tool.image.LiteRtImage
import io.github.kmplitert.tool.interceptor.LiteRTInputShapeInterceptor
import io.github.kmplitert.tool.interceptor.LiteRTLoggingInterceptor
import io.github.kmplitert.tool.interceptor.LiteRTResultCacheInterceptor

/**
 * Shorthand for adding a logging interceptor.
 */
fun <I, O> LiteRTHandler<I, O>.addLogging(
    tag: String = "LiteRT",
    phase: LiteRTPhase = LiteRTPhase.TASK,
    logger: (String) -> Unit = { println(it) }
) {
    addInterceptor(LiteRTLoggingInterceptor(tag, logger), phase = phase)
}

/**
 * Shorthand for adding a result cache interceptor.
 */
fun <I, O> LiteRTHandler<I, O>.addCache(
    onCacheHit: ((I, O) -> Unit)? = null,
    calculateFingerprint: (I) -> Any = { it.hashCode() }
) {
    addInterceptor(LiteRTResultCacheInterceptor(onCacheHit, calculateFingerprint), phase = LiteRTPhase.TASK)
}


/**
 * Shorthand for adding an input shape validator interceptor.
 */
fun <I, O> LiteRTHandler<I, O>.addInputShapeValidator(
    expectedShape: IntArray,
    getShape: (Any?) -> IntArray,
    phase: LiteRTPhase = LiteRTPhase.FEED,
    onValidated: ((IntArray) -> Unit)? = null,
    onInvalidated: ((expected: IntArray, actual: IntArray) -> Unit)? = null
) {
    addInterceptor(
        LiteRTInputShapeInterceptor(
            expectedShape = expectedShape,
            useTransformedData = (phase != LiteRTPhase.TASK && phase != LiteRTPhase.TRANSFORM),
            onValidated = onValidated,
            onInvalidated = onInvalidated,
            getShape = getShape
        ),
        phase = phase
    )
}

/**
 * Shorthand for adding an image input shape validator interceptor.
 */
fun <O> LiteRTHandler<LiteRtImage, O>.addImageShapeValidator(
    expectedShape: IntArray,
    phase: LiteRTPhase = LiteRTPhase.FEED,
    onValidated: ((IntArray) -> Unit)? = null,
    onInvalidated: ((expected: IntArray, actual: IntArray) -> Unit)? = null
) {
    addInterceptor(
        LiteRTInputShapeInterceptor.image(
            expectedShape = expectedShape,
            useTransformedData = (phase != LiteRTPhase.TASK && phase != LiteRTPhase.TRANSFORM),
            onValidated = onValidated,
            onInvalidated = onInvalidated
        ),
        phase = phase
    )
}