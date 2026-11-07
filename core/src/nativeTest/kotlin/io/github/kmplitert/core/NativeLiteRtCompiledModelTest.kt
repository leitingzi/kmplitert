package io.github.kmplitert.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class NativeLiteRtCompiledModelTest {
    @Test
    fun compilesModelReadsRequirementsAndRunsInference() = runTest(timeout = 60.seconds) {
        val compiler = LiteRTCompiler(
            filePath = "core/src/nativeTest/resources/CelsiusToFahrenheit.tflite",
            accelerator = LiteRTAccelerator.CPU
        )
        var buffers: List<TFBuffer> = emptyList()

        try {
            compiler.init()

            val inputType = compiler.getInputTensorType("input_c")
            assertEquals(LiteRTElementType.FLOAT, inputType.elementType)
            assertEquals(listOf(1, 1), inputType.layout?.dimensions)

            val outputType = compiler.getOutputTensorType("Identity")
            assertEquals(LiteRTElementType.FLOAT, outputType.elementType)
            assertEquals(listOf(1, 1), outputType.layout?.dimensions)

            val inputRequirements = compiler.getInputBufferRequirements("input_c")
            assertTrue(inputRequirements.bufferSize >= Float.SIZE_BYTES)
            assertTrue(LiteRTTensorBufferType.HostMemory in inputRequirements.supportedTypes)

            val outputRequirements = compiler.getOutputBufferRequirements("Identity")
            assertTrue(outputRequirements.bufferSize >= Float.SIZE_BYTES)

            val inputBuffers = compiler.getInputBuffers()
            buffers = inputBuffers
            val outputBuffers = compiler.getOutputBuffers()
            buffers = buffers + outputBuffers

            val inputBuffer = inputBuffers.single()
            inputBuffer.writeFloat(floatArrayOf(100f))
            compiler.run(inputBuffers, outputBuffers)

            assertEquals(212f, outputBuffers.single().readFloat().single(), 1f)

            inputBuffer.close()
            inputBuffer.close()
            assertFailsWith<IllegalStateException> {
                inputBuffer.writeFloat(floatArrayOf(100f))
            }
        } finally {
            try {
                buffers.forEach(TFBuffer::close)
            } finally {
                compiler.close()
            }
        }
    }
}
