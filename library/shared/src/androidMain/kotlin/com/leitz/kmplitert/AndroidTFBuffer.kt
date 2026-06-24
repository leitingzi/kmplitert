package com.leitz.kmplitert

import com.google.ai.edge.litert.TensorBuffer

class AndroidTFBuffer(val buffer: TensorBuffer): TFBuffer {
    override fun writeInt(data: IntArray) = buffer.writeInt(data)
    override fun writeFloat(data: FloatArray) = buffer.writeFloat(data)
    override fun writeInt8(data: ByteArray) = buffer.writeInt8(data)
    override fun writeBoolean(data: BooleanArray) = buffer.writeBoolean(data)
    override fun writeLong(data: LongArray) = buffer.writeLong(data)

    override fun readInt(): IntArray = buffer.readInt()
    override fun readFloat(): FloatArray = buffer.readFloat()
    override fun readInt8(): ByteArray = buffer.readInt8()
    override fun readBoolean(): BooleanArray = buffer.readBoolean()
    override fun readLong(): LongArray = buffer.readLong()
}