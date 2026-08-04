package io.github.kmplitert.tool.interceptor

/**
 * Exception thrown when a task is intentionally intercepted and cancelled.
 */
class LiteRTInterceptionException(message: String) : RuntimeException(message)