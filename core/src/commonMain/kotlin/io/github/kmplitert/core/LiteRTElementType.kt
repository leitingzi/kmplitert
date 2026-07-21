package io.github.kmplitert.core

/**
 * Supported tensor element types.
 */
enum class LiteRTElementType {
    /**
     * 32-bit signed integer.
     */
    INT,

    /**
     * 32-bit floating-point number.
     */
    FLOAT,

    /**
     * 8-bit signed integer.
     */
    INT8,

    /**
     * Boolean value.
     */
    BOOLEAN,

    /**
     * 64-bit signed integer.
     */
    INT64;
}