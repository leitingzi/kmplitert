package io.github.kmplitert.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelFilePathTest {
    @Test
    fun convertsFileUriToAbsolutePath() {
        assertEquals(
            "/tmp/CelsiusToFahrenheit.tflite",
            "file:///tmp/CelsiusToFahrenheit.tflite".toLiteRtModelFilePath()
        )
    }

    @Test
    fun preservesFileSystemPath() {
        assertEquals(
            "/tmp/CelsiusToFahrenheit.tflite",
            "/tmp/CelsiusToFahrenheit.tflite".toLiteRtModelFilePath()
        )
    }
}
