package io.github.kmplitert.tool.interceptor

/**
 * A simple interceptor that logs the execution time and basic info of a LiteRT task.
 *
 * @param tag The tag to use for logging.
 * @param logger A function to handle the log messages. Defaults to println.
 * @param clock A function that returns the current time in milliseconds.
 */
class LiteRTLoggingInterceptor<I, O>(
    private val tag: String = "LiteRT",
    private val logger: (String) -> Unit = { println(it) },
    private val clock: () -> Long = { 0L }
) : LiteRTInterceptor<I, O> {

    override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
        logger("[$tag] Starting task...")
        val startTime = clock()
        
        return try {
            val result = chain.proceed(chain.input)
            val duration = clock() - startTime
            if (startTime != 0L) {
                logger("[$tag] Task completed in ${duration}ms")
            } else {
                logger("[$tag] Task completed.")
            }
            result
        } catch (e: Exception) {
            val duration = clock() - startTime
            if (startTime != 0L) {
                logger("[$tag] Task failed after ${duration}ms: ${e.message}")
            } else {
                logger("[$tag] Task failed: ${e.message}")
            }
            throw e
        }
    }
}
