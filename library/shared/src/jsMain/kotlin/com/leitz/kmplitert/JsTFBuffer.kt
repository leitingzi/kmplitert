package com.leitz.kmplitert

import com.leitz.kmplitert.model.Tensor
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.toFloat32Array
import org.khronos.webgl.toFloatArray

class JsTFBuffer(val shape: Int32Array) : TFBuffer {

    lateinit var tensor: Tensor

    override fun writeInt(data: IntArray) { TODO() }
    override fun writeFloat(data: FloatArray) {
        val data = data.toFloat32Array()
        tensor = Tensor(data, shape)
    }
    override fun writeInt8(data: ByteArray) { TODO() }
    override fun writeBoolean(data: BooleanArray) { TODO() }
    override fun writeLong(data: LongArray) { TODO() }
    override fun readInt(): IntArray = TODO()
    override fun readFloat(): FloatArray {
        val float32Array = tensor.toTypedArray() as Float32Array
        return float32Array.toFloatArray()
    }
    override fun readInt8(): ByteArray = TODO()
    override fun readBoolean(): BooleanArray = TODO()
    override fun readLong(): LongArray = TODO()
}