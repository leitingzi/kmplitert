package io.github.leitingzi.kmplitert.core

import io.github.leitingzi.kmplitert.core.model.LiteRtCompiledModel
import io.github.leitingzi.kmplitert.core.model.LiteRtHwAcceleratorSet
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Tests for Hardware Accelerators (GPU/NPU) on the JVM platform.
 * Note: These tests require specific hardware and drivers to be available in the environment.
 */
class JvmLiteRtAcceleratorTest {

    private val modelFilePath = "src/jvmTest/resources/CelsiusToFahrenheit.tflite"

    @Test
    fun testGpuAcceleratorInference() = runTest {
        println("Starting GPU Accelerator Test...")
        try {
            val compiledModel = LiteRtCompiledModel.create(modelFilePath, LiteRtHwAcceleratorSet.GPU)
            assertNotNull(compiledModel, "Failed to create compiled model with GPU")
            
            val inputBuffers = compiledModel.getInputBuffers()
            val outputBuffers = compiledModel.getOutputBuffers()

            // 100 Celsius
            inputBuffers[0].writeFloat(floatArrayOf(100f))

            compiledModel.run(0, inputBuffers, outputBuffers)

            val result = outputBuffers[0].readFloat()
            println("GPU Result for 100C: ${result[0]} F")
            
            // Validate result
            assertEquals(212f, result[0], 5.0f)

            compiledModel.destroy()
            println("GPU Accelerator Test Passed.")
        } catch (e: Exception) {
            val message = e.message ?: ""
            if (message.contains("504")) {
                println("GPU Test Skipped or Failed: Hardware/Driver not supported (Status 504)")
            } else {
                throw e
            }
        }
    }

    @Test
    fun testNpuAcceleratorInference() = runTest {
        println("Starting NPU Accelerator Test...")
        try {
            val compiledModel = LiteRtCompiledModel.create(modelFilePath, LiteRtHwAcceleratorSet.NPU)
            assertNotNull(compiledModel, "Failed to create compiled model with NPU")
            
            val inputBuffers = compiledModel.getInputBuffers()
            val outputBuffers = compiledModel.getOutputBuffers()

            // 100 Celsius
            inputBuffers[0].writeFloat(floatArrayOf(100f))

            compiledModel.run(0, inputBuffers, outputBuffers)

            val result = outputBuffers[0].readFloat()
            println("NPU Result for 100C: ${result[0]} F")
            
            // Validate result
            assertEquals(212f, result[0], 5.0f)

            compiledModel.destroy()
            println("NPU Accelerator Test Passed.")
        } catch (e: Exception) {
            val message = e.message ?: ""
            if (message.contains("504")) {
                println("NPU Test Skipped or Failed: Hardware/Driver not supported (Status 504)")
            } else {
                throw e
            }
        }
    }
}


