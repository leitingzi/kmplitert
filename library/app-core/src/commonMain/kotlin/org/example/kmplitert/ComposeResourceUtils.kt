@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.example.kmplitert

/**
 * Utility functions for accessing files stored in Compose Multiplatform resources.
 *
 * This object provides platform-independent APIs for resolving files located
 * under the `composeResources/files` directory.
 */
expect object ComposeResourceUtils {
    /**
     * Resolves a file from the `composeResources/files` directory and returns
     * its absolute local filesystem path.
     *
     * If the resource is packaged inside the application, it is extracted to a
     * temporary location when necessary before returning the path.
     *
     * @param resourcePath The path relative to the `composeResources/files`
     * directory.
     * @return The absolute filesystem path of the resolved file.
     */
    suspend fun getFilePath(resourcePath: String): String
}
