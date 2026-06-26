package com.leitz.kmplitert

/**
 * Abstraction for LiteRT tensor data buffer.
 *
 * Provides typed read/write methods to transfer numeric and boolean data
 * between application layer and underlying model tensor memory.
 */
interface TFBuffer {
    /**
     * Write integer array data into this tensor buffer.
     *
     * @param data Source int array to be copied into buffer
     */
    fun writeInt(data: IntArray)

    /**
     * Write float array data into this tensor buffer.
     *
     * @param data Source float array to be copied into buffer
     */
    fun writeFloat(data: FloatArray)

    /**
     * Write int8 byte array data into this tensor buffer.
     *
     * @param data Source signed 8-bit byte array to be copied into buffer
     */
    fun writeInt8(data: ByteArray)

    /**
     * Write boolean array data into this tensor buffer.
     *
     * @param data Source boolean array to be copied into buffer
     */
    fun writeBoolean(data: BooleanArray)

    /**
     * Write long array data into this tensor buffer.
     *
     * @param data Source long array to be copied into buffer
     */
    fun writeLong(data: LongArray)

    /**
     * Read all stored integer values from the tensor buffer.
     *
     * @return A new int array containing buffer integer data
     */
    suspend fun readInt(): IntArray

    /**
     * Read all stored float values from the tensor buffer.
     *
     * @return A new float array containing buffer float data
     */
    suspend fun readFloat(): FloatArray

    /**
     * Read all stored int8 byte values from the tensor buffer.
     *
     * @return A new byte array containing signed 8-bit tensor data
     */
    suspend fun readInt8(): ByteArray

    /**
     * Read all stored boolean values from the tensor buffer.
     *
     * @return A new boolean array containing buffer boolean data
     */
    suspend fun readBoolean(): BooleanArray

    /**
     * Read all stored long values from the tensor buffer.
     *
     * @return A new long array containing buffer long data
     */
    suspend fun readLong(): LongArray
}