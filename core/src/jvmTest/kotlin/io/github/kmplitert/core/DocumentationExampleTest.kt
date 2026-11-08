package io.github.kmplitert.core

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class DocumentationExampleTest {
    @Test
    fun temperatureQuickStart() = runTest(timeout = 60.seconds) {
        val modelFile = Files.createTempFile("kmplitert-docs-", ".tflite")
        try {
            val resource = checkNotNull(javaClass.classLoader.getResourceAsStream("CelsiusToFahrenheit.tflite"))
            resource.use { Files.copy(it, modelFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING) }
            assertEquals(212f, celsiusToFahrenheit(modelFile.toString()), 1f)
        } finally {
            Files.deleteIfExists(modelFile)
        }
    }

    private suspend fun celsiusToFahrenheit(modelPath: String): Float {
        val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
        val buffers = mutableListOf<TFBuffer>()
        try {
            compiler.init()
            val inputs = compiler.getInputBuffers().also { buffers.addAll(it) }
            val outputs = compiler.getOutputBuffers().also { buffers.addAll(it) }

            inputs.single().writeFloat(floatArrayOf(100f))
            compiler.run(inputs, outputs)
            return outputs.single().readFloat().single()
        } finally {
            try {
                buffers.forEach { it.close() }
            } finally {
                compiler.close()
            }
        }
    }
}
