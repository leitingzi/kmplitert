package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.tool.expand.proceed
import io.github.kmplitert.tool.image.LiteRtImage

/**
 * An interceptor that validates the shape of the input data or transformed data.
 *
 * @param expectedShape The expected dimensions.
 * @param useTransformedData If true, validates [io.github.kmplitert.tool.interceptor.LiteRTInterceptor.Chain.transformedData].
 *                           Otherwise, validates [io.github.kmplitert.tool.interceptor.LiteRTInterceptor.Chain.input].
 * @param onValidated Optional callback invoked when the shape is successfully validated.
 * @param onInvalidated Optional callback invoked when the shape validation fails.
 * @param getShape A function to extract the shape from the target data.
 */
class LiteRTInputShapeInterceptor<I, O>(
    private val expectedShape: IntArray,
    private val useTransformedData: Boolean = false,
    private val onValidated: ((IntArray) -> Unit)? = null,
    private val onInvalidated: ((expected: IntArray, actual: IntArray) -> Unit)? = null,
    private val getShape: (Any?) -> IntArray
) : LiteRTInterceptor<I, O> {

    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        val target = if (useTransformedData) chain.transformedData else chain.input
        val actualShape = getShape(target)
        
        if (!actualShape.contentEquals(expectedShape)) {
            onInvalidated?.invoke(expectedShape, actualShape)
            val sourceName = if (useTransformedData) "transformed data" else "input"
            throw LiteRTInterceptionException(
                "Invalid $sourceName shape: expected ${expectedShape.contentToString()}, " +
                        "but got ${actualShape.contentToString()}"
            )
        }
        
        onValidated?.invoke(actualShape)
        
        return chain.proceed()
    }

    companion object {
        /**
         * Creates a shape interceptor for [LiteRtImage].
         * The shape is expected in [height, width, channels] format.
         */
        fun <I, O> image(
            expectedShape: IntArray,
            useTransformedData: Boolean = false,
            onValidated: ((IntArray) -> Unit)? = null,
            onInvalidated: ((expected: IntArray, actual: IntArray) -> Unit)? = null
        ): LiteRTInputShapeInterceptor<I, O> {
            return LiteRTInputShapeInterceptor(expectedShape, useTransformedData, onValidated, onInvalidated) { target ->
                if (target is LiteRtImage) {
                    intArrayOf(target.height, target.width, target.channels)
                } else {
                    intArrayOf()
                }
            }
        }
    }
}
