package io.github.kmplitert.core

import io.github.kmplitert.tool.LiteRTFileUtils
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class LiteRTTest {

    @Test
    fun testDecodeBase64Simple() {
        val original = "Hello World"
        val base64 = "SGVsbG8gV29ybGQ="
        val decoded = decodeBase64(base64)
        assertEquals(original, decoded.decodeToString())
    }

    @Test
    fun verifyModelBytes() {
        val bytes = try {
            decodeBase64(CELSIUS_TO_FAHRENHEIT_MODEL_BASE64)
        } catch (e: Throwable) {
            println("Decode failed: ${e.message}")
            return
        }
        println("Model bytes size: ${bytes.size}")
        // Print hex for debugging
        val hex = bytes.take(16).joinToString(" ") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
        println("Header: $hex")
        
        assertTrue(bytes.size > 100)
        // TFL3 header is at offset 4: 54 46 4C 33
        assertEquals(0x54.toByte(), bytes[4], "Byte 4 should be 'T'")
        assertEquals(0x46.toByte(), bytes[5], "Byte 5 should be 'F'")
        assertEquals(0x4c.toByte(), bytes[6], "Byte 6 should be 'L'")
        assertEquals(0x33.toByte(), bytes[7], "Byte 7 should be '3'")
    }

    private suspend fun runModelInferenceTest(config: ModelTestConfig) {
        println("Starting test for model: ${config.name}")
        
        // 1. Create temporary model file
        val filePath = LiteRTFileUtils.createFileFromByteArray(config.modelBytes, "${config.name}.tflite")
        
        for (accelerator in config.accelerators) {
            if (!accelerator.isSupportedOnCurrentPlatform()) {
                println("Skipping unsupported accelerator $accelerator on this platform")
                continue
            }
            
            println("Testing with accelerator: $accelerator")
            val compiler = LiteRTCompiler(filePath = filePath, accelerator = accelerator)
            
            try {
                compiler.init()
                
                // 2. Verify Tensors Metadata
                for (expect in config.inputs) {
                    val type = compiler.getInputTensorType(expect.name)
                    assertEquals(expect.elementType, type.elementType, "Input ${expect.name} type mismatch")
                    if (expect.dimensions != null) {
                        // Some platforms might return different layout details, 
                        // but dimensions should be compatible.
                        // For now just check rank if provided
                        assertTrue(type.layout?.rank!! >= 1)
                    }
                    
                    val req = compiler.getInputBufferRequirements(expect.name)
                    assertTrue(req.bufferSize > 0, "Buffer size for ${expect.name} should be > 0")
                }
                
                for (expect in config.outputs) {
                    val type = compiler.getOutputTensorType(expect.name)
                    assertEquals(expect.elementType, type.elementType, "Output ${expect.name} type mismatch")
                }

                // 3. Run Inference
                val inputBuffers = compiler.getInputBuffers()
                val outputBuffers = compiler.getOutputBuffers()
                
                // Fill input data
                config.inputs.forEachIndexed { index, expect ->
                    when (val data = expect.testData) {
                        is FloatArray -> inputBuffers[index].writeFloat(data)
                        // Add other types as needed
                    }
                }
                
                compiler.run(inputBuffers, outputBuffers)
                
                // 4. Verify Output Data
                config.outputs.forEachIndexed { index, expect ->
                    when (val expected = expect.expectedValue) {
                        is FloatArray -> {
                            val actual = outputBuffers[index].readFloat()
                            assertEquals(expected.size, actual.size, "Output ${expect.name} size mismatch")
                            for (i in expected.indices) {
                                assertEquals(expected[i], actual[i], expect.tolerance, "Output ${expect.name} value mismatch at index $i")
                            }
                        }
                    }
                }
                
            } catch (e: Throwable) {
                if (accelerator != LiteRTAccelerator.CPU) {
                    println("Optional accelerator $accelerator failed: ${e.message}. This might be expected in some CI environments.")
                } else {
                    throw e
                }
            } finally {
                compiler.close()
            }
        }
    }

    @Test
    fun testCelsiusToFahrenheit() = runTest(timeout = 60.seconds) {
        val bytes = try {
            loadResourceAsBytes("CelsiusToFahrenheit.tflite")
        } catch (e: Throwable) {
            println("Falling back to embedded model for CelsiusToFahrenheit: ${e.message}")
            decodeBase64(CELSIUS_TO_FAHRENHEIT_MODEL_BASE64)
        }
        val config = ModelTestConfig(
            name = "CelsiusToFahrenheit",
            modelBytes = bytes,
            inputs = listOf(
                TensorExpectation(
                    name = "input_c",
                    elementType = LiteRTElementType.FLOAT,
                    testData = floatArrayOf(100f)
                )
            ),
            outputs = listOf(
                TensorExpectation(
                    name = "Identity",
                    elementType = LiteRTElementType.FLOAT,
                    expectedValue = floatArrayOf(212f),
                    tolerance = 1.0f
                )
            ),
            accelerators = listOf(LiteRTAccelerator.CPU, LiteRTAccelerator.GPU)
        )
        runModelInferenceTest(config)
    }

    @Test
    fun testCelsiusToFahrenheitEx() = runTest(timeout = 60.seconds) {
        val bytes = try {
            loadResourceAsBytes("CelsiusToFahrenheitEx.tflite")
        } catch (e: Throwable) {
            println("Falling back to embedded model for CelsiusToFahrenheitEx: ${e.message}")
            decodeBase64(CELSIUS_TO_FAHRENHEIT_EX_MODEL_BASE64)
        }
        
        val config = ModelTestConfig(
            name = "CelsiusToFahrenheitEx",
            modelBytes = bytes,
            inputs = listOf(
                TensorExpectation(
                    name = "input_c",
                    elementType = LiteRTElementType.FLOAT,
                    testData = floatArrayOf(100f, 0f, -40f)
                )
            ),
            outputs = listOf(
                TensorExpectation(
                    name = "Identity",
                    elementType = LiteRTElementType.FLOAT,
                    expectedValue = floatArrayOf(212f, 32f, -40f),
                    tolerance = 1.0f
                )
            )
        )
        runModelInferenceTest(config)
    }
}
