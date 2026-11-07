package io.github.kmplitert.core

/**
 * Supported tensor element types.
 */
enum class LiteRTElementType(val value: Int) {
    /**
     * 32-bit signed integer.
     */
    INT(2),

    /**
     * 32-bit floating-point number.
     */
    FLOAT(1),

    /** 8-bit signed integer. */
    INT8(9),

    /** Boolean value. */
    BOOLEAN(6),

    /** 64-bit signed integer. */
    INT64(4),

    NONE(0),
    INT2(20),
    INT4(18),
    INT16(7),
    UINT4(21),
    UINT8(3),
    UINT16(17),
    UINT32(16),
    UINT64(13),
    FLOAT8E4M3FN(22),
    FLOAT8E5M2(23),
    FLOAT16(10),
    BFLOAT16(19),

    /** 64-bit floating-point number. */
    FLOAT64(11),

    /** Complex number with 32-bit floating-point components. */
    COMPLEX64(8),

    /** Complex number with 64-bit floating-point components. */
    COMPLEX128(12),

    TF_RESOURCE(14),
    TF_STRING(5),
    TF_VARIANT(15);

    companion object {
        fun fromNative(value: Int): LiteRTElementType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown LiteRT element type: $value")
    }
}