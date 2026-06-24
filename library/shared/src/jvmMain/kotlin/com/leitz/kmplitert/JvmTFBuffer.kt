package com.leitz.kmplitert

import com.leitz.kmplitert.model.LiteRtTensorBuffer

class JvmTFBuffer(
    val buffer: LiteRtTensorBuffer
): TFBuffer {
    override fun writeInt(data: IntArray) {
        TODO("Not yet implemented")
    }

    override fun writeFloat(data: FloatArray) {
        val inputAddr = buffer.lock()
        inputAddr.write(0, data, 0, data.size)
        buffer.unlock()
    }

    override fun writeInt8(data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun writeBoolean(data: BooleanArray) {
        TODO("Not yet implemented")
    }

    override fun writeLong(data: LongArray) {
        TODO("Not yet implemented")
    }

    override fun readInt(): IntArray {
        TODO("Not yet implemented")
    }

    override fun readFloat(): FloatArray {
        val outputAddr = buffer.lock()
        val results = FloatArray(1)
        outputAddr.read(0, results, 0, 1)
        buffer.unlock()
        return results
    }

    override fun readInt8(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun readBoolean(): BooleanArray {
        TODO("Not yet implemented")
    }

    override fun readLong(): LongArray {
        TODO("Not yet implemented")
    }
}