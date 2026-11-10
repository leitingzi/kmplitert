package io.github.kmplitert.file

import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class LiteRTFileTest {
    @Test
    fun resolvesCommonResourceToAnExistingFile() {
        val modelPath = LiteRTFile.fromCommonRes("CelsiusToFahrenheit.tflite")

        assertTrue(File(modelPath).isAbsolute)
        assertTrue(File(modelPath).isFile)
        assertTrue(File(modelPath).length() > 0)
    }

    @Test
    fun resolvesComposeFileUriToAnAbsolutePath() {
        val uri = checkNotNull(javaClass.classLoader.getResource("CelsiusToFahrenheit.tflite")).toURI().toString()

        val modelPath = LiteRTFile.fromComposeUri(uri)

        assertTrue(File(modelPath).isAbsolute)
        assertTrue(File(modelPath).isFile)
    }

    @Test
    fun createsFileFromByteArray() {
        val data = byteArrayOf(0, 1, 2, -1)
        val fileName = "litert-${UUID.randomUUID()}.tflite"
        val modelPath = LiteRTFile.createFileFromByteArray(data, fileName)
        val modelFile = File(modelPath)

        try {
            assertTrue(modelFile.isAbsolute)
            assertTrue(modelFile.isFile)
            assertContentEquals(data, modelFile.readBytes())
        } finally {
            modelFile.delete()
        }
    }
}
