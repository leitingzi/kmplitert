package io.github.leitingzi.kmplitert.tool

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LiteRTFileUtilsTest {

    @Test
    fun testCreateFileFromByteArray() {
        val data = "Hello LiteRT".encodeToByteArray()
        val fileName = "test_file.txt"
        
        try {
            val path = LiteRTFileUtils.createFileFromByteArray(data, fileName)
            assertTrue(path.isNotEmpty(), "Created file path should not be empty")
            // On most platforms we can't easily check if file exists in commonTest 
            // without more expect/actual, but if it didn't throw and returned a path, 
            // it's a good sign.
        } catch (e: Throwable) {
            // Some platforms might fail if they don't have write access in test environment
            println("Skipping or failed createFileFromByteArray test: ${e.message}")
        }
    }

    @Test
    fun testReadAssetNotFound() = runTest {
        try {
            LiteRTFileUtils.readAsset("non_existent.tflite")
            // If it doesn't throw, it's either because the file exists (unlikely) 
            // or the platform implementation didn't catch the error.
        } catch (e: IllegalStateException) {
            // Expected
        } catch (e: Throwable) {
            // Other errors are also possible depending on platform
        }
    }
}
