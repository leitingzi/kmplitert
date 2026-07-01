@file:JsModule("@litertjs/core")
@file:JsNonModule
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

external interface LiteRtTensorBuffer: JsAny {
    fun lock(mode: JsAny): Int;  // Returns the pointer to the locked buffer.
    fun unlock()
    fun bufferType(): JsAny
    fun tensorType(): JsAny
    fun isWebGpuMemory(): Boolean
    fun getWebGpuBuffer(): Int // Use wasm.WebGPU.getJsObject() to get GPUBuffer.
    fun size(): Int
    fun packedSize(): Int
    fun offset(): Int
}