package io.github.kmplitert.tool

import io.github.kmplitert.core.TFBuffer

class MockTFBuffer: TFBuffer {
    lateinit var bytes: ByteArray

    lateinit var floats: FloatArray

    lateinit var ints: IntArray

    lateinit var bools: BooleanArray

    lateinit var longs: LongArray

    override fun writeInt(data: IntArray) { ints = data }
    override fun writeFloat(data: FloatArray) { floats = data }
    override fun writeInt8(data: ByteArray) { bytes = data }
    override fun writeBoolean(data: BooleanArray) { bools = data }
    override fun writeLong(data: LongArray) { longs = data }
    override suspend fun readInt(): IntArray = ints
    override suspend fun readFloat(): FloatArray = floats
    override suspend fun readInt8(): ByteArray = bytes
    override suspend fun readBoolean(): BooleanArray = bools
    override suspend fun readLong(): LongArray = longs
}