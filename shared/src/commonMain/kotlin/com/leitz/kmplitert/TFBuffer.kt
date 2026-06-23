package com.leitz.kmplitert

interface TFBuffer {
    fun writeInt(data: IntArray)
    fun writeFloat(data: FloatArray)
    fun writeInt8(data: ByteArray)
    fun writeBoolean(data: BooleanArray)
    fun writeLong(data: LongArray)

    fun readInt(): IntArray
    fun readFloat(): FloatArray
    fun readInt8(): ByteArray
    fun readBoolean(): BooleanArray
    fun readLong(): LongArray
}