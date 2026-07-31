package io.github.kmplitert.tool

import kotlin.test.Test
import kotlin.test.assertTrue

class LiteRTFileUtilsTest {

    @Test
    fun testCreateFileFromByteArray() {
        val data = "Hello LiteRT".encodeToByteArray()
        val fileName = "test_file.txt"
        
        val path = LiteRTFileUtils.createFileFromByteArray(data, fileName)
        assertTrue(path.isNotEmpty(), "Created file path should not be empty")
    }
}
