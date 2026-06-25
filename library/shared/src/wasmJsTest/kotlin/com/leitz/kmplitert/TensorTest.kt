package com.leitz.kmplitert

import kotlinx.coroutines.test.runTest
import org.khronos.webgl.Float32Array
import org.khronos.webgl.set
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalWasmJsInterop::class)
class TensorTest {
    @Test
    fun create() = runTest {
        LiteRtInit.awaitInit()
        println(LiteRtInit.isLoaded)

        val wasmTFBuffer = WasmTFBuffer()
        wasmTFBuffer.writeFloat(floatArrayOf(1f, 2f, 3f))
        println(wasmTFBuffer.tensor)
    }
}