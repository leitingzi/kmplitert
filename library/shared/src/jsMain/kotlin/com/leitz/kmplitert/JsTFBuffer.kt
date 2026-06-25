package com.leitz.kmplitert

import org.khronos.webgl.Int32Array

class JsTFBuffer(val shape: Int32Array) : TFBuffer {
    var data: FloatArray = FloatArray(0)
    override fun writeInt(data: IntArray) { TODO() }
    override fun writeFloat(data: FloatArray) { this.data = data }
    override fun writeInt8(data: ByteArray) { TODO() }
    override fun writeBoolean(data: BooleanArray) { TODO() }
    override fun writeLong(data: LongArray) { TODO() }
    override fun readInt(): IntArray = TODO()
    override fun readFloat(): FloatArray = data
    override fun readInt8(): ByteArray = TODO()
    override fun readBoolean(): BooleanArray = TODO()
    override fun readLong(): LongArray = TODO()
}