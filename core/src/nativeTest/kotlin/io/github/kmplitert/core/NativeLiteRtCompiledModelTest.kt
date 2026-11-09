package io.github.kmplitert.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.test.runTest
import platform.posix.getenv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
class NativeLiteRtCompiledModelTest {
    @Test
    fun compilesModelReadsRequirementsAndRunsCpuInference() = runTest(timeout = 60.seconds) {
        runInference(LiteRTAccelerator.CPU)
    }

    @Test
    fun runsGpuInferenceWhenEnabled() = runTest(timeout = 60.seconds) {
        if (!isAcceleratorTestEnabled(LiteRTAccelerator.GPU)) {
            println("Skipping GPU inference: set KMPLITERT_TEST_GPU=true to enable")
            return@runTest
        }
        runInference(LiteRTAccelerator.GPU)
    }

    @Test
    fun runsNpuInferenceWhenEnabled() = runTest(timeout = 60.seconds) {
        if (!isAcceleratorTestEnabled(LiteRTAccelerator.NPU)) {
            println("Skipping NPU inference: set KMPLITERT_TEST_NPU=true to enable")
            return@runTest
        }
        runInference(LiteRTAccelerator.NPU)
    }

    private suspend fun runInference(accelerator: LiteRTAccelerator) {
        val modelPath = getenv("KMPLITERT_TEST_MODEL_PATH")?.toKString()
            ?: "core/CelsiusToFahrenheit.tflite"
        val compiler = LiteRTCompiler(
            filePath = modelPath,
            accelerator = accelerator
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
            assertTrue(
                inputRequirements.bufferSize >= Float.SIZE_BYTES,
                "Input buffer size is too small: ${inputRequirements.bufferSize}"
            )
            assertTrue(
                inputRequirements.supportedTypes.isNotEmpty(),
                "Input buffer types are empty"
            )

            val outputRequirements = compiler.getOutputBufferRequirements("Identity")
            assertTrue(
                outputRequirements.bufferSize >= Float.SIZE_BYTES,
                "Output buffer size is too small: ${outputRequirements.bufferSize}"
            )

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

    private fun isAcceleratorTestEnabled(accelerator: LiteRTAccelerator): Boolean {
        val name = "KMPLITERT_TEST_${accelerator.name}"
        return getenv(name)?.toKString().equals("true", ignoreCase = true)
    }
}
