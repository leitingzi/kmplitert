package io.github.leitingzi.kmplitert.core

actual fun <T> arrayToString(array: T): String {
    return when (array) {
        is IntArray -> array.contentToString()
        is FloatArray -> array.contentToString()
        is DoubleArray -> array.contentToString()
        is BooleanArray -> array.contentToString()
        is ByteArray -> array.contentToString()
        is ShortArray -> array.contentToString()
        is CharArray -> array.contentToString()
        is LongArray -> array.contentToString()
        else -> ""
    }
}


