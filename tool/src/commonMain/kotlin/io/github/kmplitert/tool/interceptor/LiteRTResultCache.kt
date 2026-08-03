package io.github.kmplitert.tool.interceptor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * An interceptor that caches the last inference result to avoid redundant processing
 * when the input hasn't changed significantly.
 *
 * @param onCacheHit Optional callback invoked when a cache hit occurs.
 * @param calculateFingerprint A function to calculate a unique key or fingerprint for the input.
 */
class LiteRTResultCache<I, O>(
    private val onCacheHit: ((I, O) -> Unit)? = null,
    private val calculateFingerprint: (I) -> Any
) : LiteRTInterceptor<I, O> {

    private var lastFingerprint: Any? = null
    private var lastResult: O? = null
    private val mutex = Mutex()

    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        val currentFingerprint = calculateFingerprint(chain.input)

        mutex.withLock {
            if (currentFingerprint == lastFingerprint && lastResult != null) {
                @Suppress("UNCHECKED_CAST")
                val result = lastResult as O
                onCacheHit?.invoke(chain.input, result)
                return result
            }
        }

        val result = chain.proceed(chain.input)

        mutex.withLock {
            lastFingerprint = currentFingerprint
            lastResult = result
        }

        return result
    }
}
