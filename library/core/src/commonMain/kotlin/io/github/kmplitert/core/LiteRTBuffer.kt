package io.github.kmplitert.core

interface LiteRTBuffer {
    fun writeBoolean(data: BooleanArray)
    fun writeByte(data: ByteArray)
    fun writeInt(data: IntArray)
    fun writeFloat(data: FloatArray)
    fun writeLong(data: LongArray)
    fun readBoolean(): BooleanArray
    fun readByte(): ByteArray
    fun readInt(): IntArray
    fun readFloat(): FloatArray
    fun readLong(): LongArray
}