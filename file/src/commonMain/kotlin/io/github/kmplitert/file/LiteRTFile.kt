package io.github.kmplitert.file

internal expect fun resolveCommonResource(filePath: String): String
internal expect fun resolveComposeUri(uri: String): String
internal expect fun writeModelFile(data: ByteArray, fileName: String): String

/**
 * Resolves bundled LiteRT model resources to locations that can be loaded by LiteRT.
 *
 * Use [fromCommonRes] for files in `commonMain/resources` or [fromComposeUri] for
 * Compose Multiplatform resource URIs.
 */
object LiteRTFile {
    /**
     * Resolves a resource from the `commonMain/resources` directory.
     *
     * @param filePath the resource path relative to `commonMain/resources`.
     * @return the resource's filesystem path, or a relative URL on browser targets.
     * @throws IllegalArgumentException if [filePath] is blank or contains a parent-directory segment.
     */
    fun fromCommonRes(filePath: String): String {
        require(filePath.isNotBlank()) { "Resource path must not be blank." }

        val normalizedPath = filePath.replace('\\', '/').trimStart('/')
        require(normalizedPath.isNotBlank()) { "Resource path must not be blank." }
        require(normalizedPath.split('/').none { it == "." || it == ".." }) {
            "Resource path must be relative to commonMain/resources."
        }

        return resolveCommonResource(normalizedPath)
    }

    /**
     * Resolves a Compose Multiplatform resource URI, such as one returned by `Res.getUri`.
     *
     * On Android, initialize the file module with `LiteRTFile.init(context)` before resolving
     * asset-backed URIs; those assets are copied to the application's cache directory. Browser
     * targets return the URI for use as a resource URL because browsers do not expose local file
     * paths.
     *
     * @param uri the Compose resource URI to resolve.
     * @return an absolute filesystem path on filesystem targets, or a resource URL on browser targets.
     * @throws IllegalArgumentException if [uri] is blank or uses an unsupported URI scheme.
     * @throws IllegalStateException if Android has not been initialized or the resource cannot be found.
     */
    fun fromComposeUri(uri: String): String {
        require(uri.isNotBlank()) { "Compose resource URI must not be blank." }
        return resolveComposeUri(uri)
    }

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
     * @return The absolute path of the created model file, or an object URL on browser targets.
     *
     * @throws IllegalArgumentException if [fileName] is blank or is not a valid file name.
     * @throws IllegalStateException if the file cannot be created or written.
     */
    fun createFileFromByteArray(data: ByteArray, fileName: String): String {
        require(fileName.isNotBlank()) { "File name must not be blank." }
        require(fileName != "." && fileName != ".." && '/' !in fileName && '\\' !in fileName && ':' !in fileName) {
            "File name must not contain a path separator."
        }
        require('\u0000' !in fileName) { "File name must not contain a null character." }
        return writeModelFile(data, fileName)
    }
}

internal fun decodeUriPath(path: String): String = buildString {
    var index = 0
    while (index < path.length) {
        if (path[index] != '%' || index + 2 >= path.length) {
            append(path[index++])
            continue
        }

        val bytes = mutableListOf<Byte>()
        while (index + 2 < path.length && path[index] == '%') {
            val high = path[index + 1].digitToIntOrNull(16)
            val low = path[index + 2].digitToIntOrNull(16)
            if (high == null || low == null) break
            bytes += ((high shl 4) or low).toByte()
            index += 3
        }
        if (bytes.isEmpty()) {
            append(path[index++])
        } else {
            append(bytes.toByteArray().decodeToString())
        }
    }
}