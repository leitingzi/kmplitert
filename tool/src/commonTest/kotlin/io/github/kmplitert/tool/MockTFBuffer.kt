package io.github.kmplitert.tool

import io.github.kmplitert.core.TFBuffer

class MockTFBuffer: TFBuffer {
    var bytes: ByteArray? = null
    var floats: FloatArray? = null
    override fun writeInt(data: IntArray) {}
    override fun writeFloat(data: FloatArray) { floats = data }
    override fun writeInt8(data: ByteArray) { bytes = data }
    override fun writeBoolean(data: BooleanArray) {}
    override fun writeLong(data: LongArray) {}
    override suspend fun readInt(): IntArray = intArrayOf()
    override suspend fun readFloat(): FloatArray = floatArrayOf()
    override suspend fun readInt8(): ByteArray = byteArrayOf()
    override suspend fun readBoolean(): BooleanArray = booleanArrayOf()
    override suspend fun readLong(): LongArray = longArrayOf()
}