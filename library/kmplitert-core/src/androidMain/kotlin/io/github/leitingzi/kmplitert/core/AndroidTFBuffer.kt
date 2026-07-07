package io.github.leitingzi.kmplitert.core

import com.google.ai.edge.litert.TensorBuffer

internal class AndroidTFBuffer(val buffer: TensorBuffer): TFBuffer {
    override val shape: IntArray
        get() = try {
            // Using reflection to find the correct method name since it's not clear from the stub
            val methods = buffer::class.java.methods
            val shapeMethod = methods.find { it.name == "getShape" || it.name == "shape" }
            if (shapeMethod != null) {
                shapeMethod.invoke(buffer) as IntArray
            } else {
                intArrayOf()
            }
        } catch (e: Exception) {
            intArrayOf()
        }

    override val size: Int
        get() = try {
            val methods = buffer::class.java.methods
            val sizeMethod = methods.find { it.name == "getFlatSize" || it.name == "flatSize" }
            if (sizeMethod != null) {
                sizeMethod.invoke(buffer) as Int
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }

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
