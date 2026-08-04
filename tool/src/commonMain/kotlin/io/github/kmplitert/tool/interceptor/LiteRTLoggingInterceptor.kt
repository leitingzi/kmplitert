package io.github.kmplitert.tool.interceptor

import io.github.kmplitert.tool.expand.proceed

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
        val phasePrefix = "[$tag][${chain.phase}]"
        logger("$phasePrefix Starting...")
        val startTime = clock()
        
        return try {
            val result = chain.proceed()
            val duration = clock() - startTime
            if (startTime != 0L) {
                logger("$phasePrefix Completed in ${duration}ms")
            } else {
                logger("$phasePrefix Completed.")
            }
            result
        } catch (e: Exception) {
            if (e !is LiteRTInterceptionException) {
                val duration = clock() - startTime
                if (startTime != 0L) {
                    logger("$phasePrefix Failed after ${duration}ms: ${e.message}")
                } else {
                    logger("$phasePrefix Failed: ${e.message}")
                }
            }
            throw e
        }
    }
}
