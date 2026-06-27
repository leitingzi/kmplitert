package io.github.leitingzi.kmplitert.core

import com.google.ai.edge.litert.TensorBuffer

internal class AndroidTFBuffer(val buffer: TensorBuffer): TFBuffer {
    override fun writeInt(data: IntArray) {
        buffer.writeInt(data)
    }
    override fun writeFloat(data: FloatArray) {
        buffer.writeFloat(data)
    }
    override fun writeInt8(data: ByteArray) {
        buffer.writeInt8(data)
    }
    override fun writeBoolean(data: BooleanArray) {
        buffer.writeBoolean(data)
    }
    override fun writeLong(data: LongArray) {
        buffer.writeLong(data)
    }

    override suspend fun readInt(): IntArray {
        return buffer.readInt()
    }
    override suspend fun readFloat(): FloatArray {
        return buffer.readFloat()
    }
    override suspend fun readInt8(): ByteArray {
        return buffer.readInt8()
    }
    override suspend fun readBoolean(): BooleanArray {
        return buffer.readBoolean()
    }
    override suspend fun readLong(): LongArray {
        return buffer.readLong()
    }
}