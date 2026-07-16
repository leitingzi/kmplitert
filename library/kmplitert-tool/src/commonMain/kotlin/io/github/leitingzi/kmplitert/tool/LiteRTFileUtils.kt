@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.tool

/**
 * Utility functions for working with LiteRT model files.
 *
 * This object provides platform-specific implementations for creating
 * temporary or persistent model files from in-memory byte arrays.
 */
expect object LiteRTFileUtils {

    /**
     * Creates a local model file from the given LiteRT model bytes and returns
     * its absolute filesystem path.
     *
     * This function is intended for platforms or APIs that require a model to
     * be loaded from a file instead of directly from an in-memory byte array.
     *
     * @param data The binary content of the LiteRT model.
     * @param fileName The name of the file to create, including its extension
     * (for example, `model.tflite`).
     *
     * @return The absolute path of the created model file.
     *
     * @throws IllegalStateException If the file cannot be created or written.
     */
    fun createFileFromByteArray(data: ByteArray, fileName: String): String

    /**
     * Reads the binary content of a model from the platform's assets or resources.
     *
     * @param path The relative path to the asset (e.g., "models/my_model.tflite").
     * @return The binary content of the asset.
     * @throws IllegalStateException If the asset cannot be found or read.
     */
    suspend fun readAsset(path: String): ByteArray
}
