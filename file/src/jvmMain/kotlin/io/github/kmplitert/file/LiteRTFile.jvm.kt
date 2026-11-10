package io.github.kmplitert.file

import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

internal actual fun resolveCommonResource(filePath: String): String {
    val resource = checkNotNull(LiteRTFile::class.java.classLoader.getResource(filePath)) {
        "Common resource '$filePath' was not found."
    }

    if (resource.protocol == "file") {
        return Paths.get(resource.toURI()).toAbsolutePath().toString()
    }

    val extension = filePath.substringAfterLast('.', "").takeIf(String::isNotEmpty)
        ?.let { ".$it" } ?: ".tmp"
    val temporaryFile = Files.createTempFile("kmplitert-", extension)
    var copied = false
    try {
        resource.openStream().use { input ->
            Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING)
        }
        copied = true
        return temporaryFile.toAbsolutePath().toString()
    } finally {
        if (!copied) {
            Files.deleteIfExists(temporaryFile)
        }
    }
}

internal actual fun resolveComposeUri(uri: String): String {
    if (uri.startsWith("jar:")) {
        val extension = uri.substringBefore('?').substringBefore('#')
            .substringAfterLast('.', "")
            .takeIf(String::isNotEmpty)
            ?.let { ".$it" } ?: ".tmp"
        val temporaryFile = Files.createTempFile("kmplitert-model-", extension)
        var copied = false
        try {
            URI(uri).toURL().openStream().use { input ->
                Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING)
            }
            copied = true
            return temporaryFile.toAbsolutePath().toString()
        } finally {
            if (!copied) {
                Files.deleteIfExists(temporaryFile)
            }
        }
    }

    val modelFile = if (uri.startsWith("file:")) {
        Paths.get(URI(uri)).toAbsolutePath()
    } else {
        require(!uri.contains("://")) { "Unsupported Compose resource URI: $uri" }
        Paths.get(uri).toAbsolutePath()
    }
    check(Files.isRegularFile(modelFile)) { "Compose resource file does not exist: $modelFile" }
    return modelFile.toString()
}

internal actual fun writeModelFile(data: ByteArray, fileName: String): String {
    val temporaryDirectory = Paths.get(System.getProperty("java.io.tmpdir") ?: ".").toAbsolutePath()
    val modelFile = temporaryDirectory.resolve(fileName)
    try {
        Files.write(modelFile, data)
    } catch (error: IOException) {
        throw IllegalStateException("Could not write model file: $modelFile", error)
    }
    return modelFile.toString()
}
