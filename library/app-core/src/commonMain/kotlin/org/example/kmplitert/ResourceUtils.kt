@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

expect object ResourceUtils {
    /**
     * Gets the local path of a file in the composeResources/files/ directory.
     * If the file is located within the package (e.g., Android Assets or a JAR), it is copied to a temporary directory and the path is returned.
     *
     * @param path The path relative to the files/ directory, e.g., "model.tflite"
     * @return The local absolute path of the file
     */
    suspend fun getResourcePath(path: String): String
}
