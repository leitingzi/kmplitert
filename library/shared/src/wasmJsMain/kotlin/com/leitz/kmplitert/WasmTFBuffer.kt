@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

class WasmTFBuffer: WebTFBuffer {
    override var tensor: JsAny?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun writeInt(data: IntArray) {
        TODO("Not yet implemented")
    }

    override fun writeFloat(data: FloatArray) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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