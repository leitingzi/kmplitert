package com.leitz.kmplitert

import org.khronos.webgl.toFloat32Array

@OptIn(ExperimentalWasmJsInterop::class)
class JsTFBuffer: WebTFBuffer {
    override var tensor: JsAny? = null

    private val nativeTensor: Tensor<*>
        get() = tensor as Tensor<*>
    override fun writeInt(data: IntArray) {
        TODO("Not yet implemented")
    }

    override fun writeFloat(data: FloatArray) {
        val data = data.toFloat32Array()
        val shape = arrayOf(1, data.length)
        tensor = Tensor(data, shape)
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
        val data = nativeTensor.data.asDynamic()
        val len = data.length as Int
        val result = FloatArray(len)
        for (i in 0 until len) {
            result[i] = data[i] as Float
        }
        return result
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