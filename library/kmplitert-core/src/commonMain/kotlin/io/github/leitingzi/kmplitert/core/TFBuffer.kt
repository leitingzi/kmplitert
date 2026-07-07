package io.github.leitingzi.kmplitert.core

/**
 * Represents a LiteRT tensor buffer.
 *
 * Provides typed read and write operations for tensor data.
 */
interface TFBuffer {

    /** The shape of the tensor. */
    val shape: IntArray

    /** The total number of elements in the tensor. */
    val size: Int

    /** Writes integer values. */
    fun writeInt(data: IntArray)

    /** Writes floating-point values. */
    fun writeFloat(data: FloatArray)

    /** Writes signed 8-bit integer values. */
    fun writeInt8(data: ByteArray)

    /** Writes boolean values. */
    fun writeBoolean(data: BooleanArray)

    /** Writes long integer values. */
    fun writeLong(data: LongArray)

    /** Reads integer values. */
    suspend fun readInt(): IntArray

    /** Reads floating-point values. */
    suspend fun readFloat(): FloatArray

    /** Reads signed 8-bit integer values. */
    suspend fun readInt8(): ByteArray

    /** Reads boolean values. */
    suspend fun readBoolean(): BooleanArray

    /** Reads long integer values. */
    suspend fun readLong(): LongArray
}

