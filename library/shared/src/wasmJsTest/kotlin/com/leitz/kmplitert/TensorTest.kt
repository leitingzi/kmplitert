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

        val data = Float32Array(3)
        data[0] = 1f
        data[1] = 2f
        data[2] = 3f

        val shape = arrayOf(1, 3).map {
            it.toJsNumber()
        }.toJsArray()

        val tensor = Tensor(data, shape)

        println(tensor)
    }
}