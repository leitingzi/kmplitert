package io.github.kmplitert.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AndroidLiteRTCompilerTest {

    @Test
    fun compilesModelReadsMetadataAndRunsCpuInference() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        val modelFile = withContext(Dispatchers.IO) {
            val modelBytes = context.assets.open("CelsiusToFahrenheit.tflite").use { it.readBytes() }
            File.createTempFile("kmplitert-android-", ".tflite").apply {
                writeBytes(modelBytes)
            }
        }

        val compiler = LiteRTCompiler(
            filePath = modelFile.absolutePath,
            accelerator = LiteRTAccelerator.CPU
        )

        try {
            compiler.init()

            val inputType = compiler.getInputTensorType("input_c")
            assertEquals(LiteRTElementType.FLOAT, inputType.elementType)
            assertEquals(listOf(1, 1), inputType.layout?.dimensions)

            val inputRequirements = compiler.getInputBufferRequirements("input_c")
            assertTrue(inputRequirements.bufferSize > 0)
            assertTrue(inputRequirements.supportedTypes.isNotEmpty())

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
            withContext(Dispatchers.IO) {
                modelFile.delete()
            }
        }
    }
}
