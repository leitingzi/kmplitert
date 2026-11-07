package io.github.kmplitert.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import java.nio.file.Files
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class LiteRTTest {

    private suspend fun runModelInferenceTest(config: ModelTestConfig) {
        val modelFile = withContext(Dispatchers.IO) {
            Files.createTempFile("kmplitert-${config.name}-", ".tflite")
        }
        try {
            withContext(Dispatchers.IO) { Files.write(modelFile, config.modelBytes) }
            val compiler = LiteRTCompiler(
                filePath = modelFile.toAbsolutePath().toString(),
                accelerator = LiteRTAccelerator.CPU
            )
            try {
                compiler.init()

                for (expect in config.inputs) {
                    val type = compiler.getInputTensorType(expect.name)
                    assertEquals(expect.elementType, type.elementType, "Input ${expect.name} type mismatch")
                    expect.dimensions?.let { dimensions ->
                        assertEquals(dimensions, type.layout?.dimensions, "Input ${expect.name} dimensions mismatch")
                    }

                    val req = compiler.getInputBufferRequirements(expect.name)
                    assertTrue(req.bufferSize > 0, "Buffer size for ${expect.name} should be > 0")
                }

                for (expect in config.outputs) {
                    val type = compiler.getOutputTensorType(expect.name)
                    assertEquals(expect.elementType, type.elementType, "Output ${expect.name} type mismatch")
                    expect.dimensions?.let { dimensions ->
                        assertEquals(dimensions, type.layout?.dimensions, "Output ${expect.name} dimensions mismatch")
                    }
                }

                val inputBuffers = compiler.getInputBuffers()
                val outputBuffers = compiler.getOutputBuffers()

                config.inputs.forEachIndexed { index, expect ->
                    when (val data = expect.testData) {
                        is FloatArray -> inputBuffers[index].writeFloat(data)
                        is IntArray -> inputBuffers[index].writeInt(data)
                        is ByteArray -> inputBuffers[index].writeInt8(data)
                        is LongArray -> inputBuffers[index].writeLong(data)
                        is BooleanArray -> inputBuffers[index].writeBoolean(data)
                    }
                }

                compiler.run(inputBuffers, outputBuffers)

                config.outputs.forEachIndexed { index, expect ->
                    when (val expected = expect.expectedValue) {
                        is FloatArray -> {
                            val actual = outputBuffers[index].readFloat()
                            assertEquals(expected.size, actual.size, "Output ${expect.name} size mismatch")
                            for (i in expected.indices) {
                                assertEquals(
                                    expected[i],
                                    actual[i],
                                    expect.tolerance,
                                    "Output ${expect.name} value mismatch at index $i"
                                )
                            }
                        }

                        is IntArray -> {
                            assertContentEquals(
                                expected,
                                outputBuffers[index].readInt(),
                                "Output ${expect.name} mismatch"
                            )
                        }

                        is ByteArray -> {
                            assertContentEquals(
                                expected,
                                outputBuffers[index].readInt8(),
                                "Output ${expect.name} mismatch"
                            )
                        }
                    }
                }
            } finally {
                compiler.close()
            }
        } finally {
            withContext(Dispatchers.IO) { Files.deleteIfExists(modelFile) }
        }
    }

    @Test
    fun testCelsiusToFahrenheit() = runTest(timeout = 60.seconds) {
        val bytes = loadModelBytes("CelsiusToFahrenheit.tflite")
        val config = ModelTestConfig(
            name = "CelsiusToFahrenheit",
            modelBytes = bytes,
            inputs = listOf(
                TensorExpectation(
                    name = "input_c",
                    elementType = LiteRTElementType.FLOAT,
                    dimensions = listOf(1, 1),
                    testData = floatArrayOf(100f),
                )
            ),
            outputs = listOf(
                TensorExpectation(
                    name = "Identity",
                    elementType = LiteRTElementType.FLOAT,
                    dimensions = listOf(1, 1),
                    expectedValue = floatArrayOf(212f),
                    tolerance = 1.0f
                )
            )
        )
        runModelInferenceTest(config)
    }

    @Test
    fun testCelsiusToFahrenheitEx() = runTest(timeout = 60.seconds) {
        val bytes = loadModelBytes("CelsiusToFahrenheitEx.tflite")
        val config = ModelTestConfig(
            name = "CelsiusToFahrenheitEx",
            modelBytes = bytes,
            inputs = listOf(
                TensorExpectation(
                    name = "input_c",
                    elementType = LiteRTElementType.FLOAT,
                    dimensions = listOf(1, 3),
                    testData = floatArrayOf(100f, 0f, -40f)
                )
            ),
            outputs = listOf(
                TensorExpectation(
                    name = "Identity",
                    elementType = LiteRTElementType.FLOAT,
                    dimensions = listOf(1, 3),
                    expectedValue = floatArrayOf(212f, 32f, -40f),
                    tolerance = 1.0f
                )
            )
        )
        runModelInferenceTest(config)
    }

    @Test
    fun compilerCloseReleasesBuffersAndIsIdempotent() = runTest(timeout = 60.seconds) {
        val modelFile = withContext(Dispatchers.IO) {
            Files.createTempFile("kmplitert-close-", ".tflite")
        }
        val compiler = LiteRTCompiler(
            filePath = modelFile.toAbsolutePath().toString(),
            accelerator = LiteRTAccelerator.CPU
        )
        try {
            withContext(Dispatchers.IO) {
                Files.write(modelFile, loadModelBytes("CelsiusToFahrenheit.tflite"))
            }
            compiler.init()
            compiler.init()
            val inputBuffer = compiler.getInputBuffers().single()
            val outputBuffer = compiler.getOutputBuffers().single()

            compiler.close()
            compiler.close()

            assertFailsWith<IllegalStateException> {
                inputBuffer.writeFloat(floatArrayOf(100f))
            }
            assertFailsWith<IllegalStateException> {
                outputBuffer.writeFloat(floatArrayOf(100f))
            }
            val useAfterClose = try {
                compiler.getInputTensorType("input_c")
                null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: IllegalStateException) {
                failure
            }
            assertIs<IllegalStateException>(useAfterClose)
        } finally {
            compiler.close()
            withContext(Dispatchers.IO) { Files.deleteIfExists(modelFile) }
        }
    }

    private fun loadModelBytes(name: String): ByteArray {
        val stream = checkNotNull(javaClass.classLoader.getResourceAsStream(name)) {
            "Model test resource not found: $name"
        }
        return stream.use { it.readBytes() }
    }
}
