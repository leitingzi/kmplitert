package com.leitz.kmplitert

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TensorTest {
    @Test
    fun create() = runTest {
        LiteRtInit.awaitInit()
        println(LiteRtInit.isLoaded)

        val jsTFBuffer = JsTFBuffer()
        jsTFBuffer.writeFloat(floatArrayOf(1f, 2f, 3f))
        println(jsTFBuffer.tensor)
    }
}