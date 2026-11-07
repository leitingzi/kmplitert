package io.github.kmplitert.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class WasmJsLiteRTCompilerTest {

    @Test
    fun loadsModelReadsTensorMetadataAndRunsCpuInference() = runTest(timeout = 60.seconds) {
        val compiler = LiteRTCompiler(
            filePath = "/base/core/build/processedResources/wasmJs/test/CelsiusToFahrenheit.tflite",
            accelerator = LiteRTAccelerator.CPU
        )

        try {
            compiler.init()

            val inputType = compiler.getInputTensorType("input_c")
            assertEquals(LiteRTElementType.FLOAT, inputType.elementType)
            assertEquals(listOf(1, 1), inputType.layout?.dimensions)

            val inputRequirements = compiler.getInputBufferRequirements("input_c")
            assertEquals(4, inputRequirements.bufferSize)
            assertTrue(LiteRTTensorBufferType.HostMemory in inputRequirements.supportedTypes)

            val outputType = compiler.getOutputTensorType("Identity")
            assertEquals(LiteRTElementType.FLOAT, outputType.elementType)
            assertEquals(listOf(1, 1), outputType.layout?.dimensions)

            val inputBuffers = compiler.getInputBuffers()
            val outputBuffers = compiler.getOutputBuffers()
            inputBuffers.single().writeFloat(floatArrayOf(100f))
            compiler.run(inputBuffers, outputBuffers)

            assertEquals(212f, outputBuffers.single().readFloat().single(), 1f)
        } finally {
            compiler.close()
        }
    }
}
