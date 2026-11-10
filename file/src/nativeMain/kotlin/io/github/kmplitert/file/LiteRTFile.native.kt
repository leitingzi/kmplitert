@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kmplitert.file

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.F_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.getcwd

internal fun resolveCommonResourceFromFileSystem(filePath: String): String {
    val currentDirectory = memScoped {
        val buffer = allocArray<ByteVar>(4096)
        getcwd(buffer, 4096)?.toKString()
    }
    val separator = if (currentDirectory?.contains('\\') == true) "\\" else "/"
    val workingDirectory = currentDirectory?.trimEnd('/', '\\')
    val candidates = buildList {
        if (workingDirectory != null) {
            add("$workingDirectory$separator$filePath")
            add("$workingDirectory${separator}src${separator}commonMain${separator}resources$separator$filePath")
            add("$workingDirectory${separator}file${separator}src${separator}commonMain${separator}resources$separator$filePath")
            for (target in listOf(
                "iosArm64",
                "iosSimulatorArm64",
                "linuxX64",
                "linuxArm64",
                "macosArm64",
                "mingwX64"
            )) {
                add("$workingDirectory${separator}file${separator}build${separator}processedResources$separator$target${separator}main$separator$filePath")
            }
        }
    }

    return checkNotNull(candidates.firstOrNull { candidate ->
        access(candidate, F_OK) == 0
    }) {
        "Common resource '$filePath' was not found. Bundle it with the application or run from the project directory."
    }
}

internal fun resolveComposeUriFromFileSystem(uri: String): String {
    require(!uri.contains("://") || uri.startsWith("file://")) {
        "Compose resource URI must identify a local file: $uri"
    }
    val currentDirectory = memScoped {
        val buffer = allocArray<ByteVar>(4096)
        getcwd(buffer, 4096)?.toKString()
    }
    val isWindows = currentDirectory?.matches(Regex("^[A-Za-z]:[/\\\\].*")) == true
    var path = when {
        uri.startsWith("file://") -> uri.removePrefix("file://")
        uri.startsWith("file:") -> uri.removePrefix("file:")
        else -> uri
    }.let(::decodeUriPath)
    if (isWindows && path.matches(Regex("^/[A-Za-z]:/.*"))) {
        path = path.drop(1)
    }

    val isAbsolutePath = path.startsWith("/") || path.matches(Regex("^[A-Za-z]:[/\\\\].*"))
    val absolutePath = if (isAbsolutePath) path else {
        val workingDirectory = checkNotNull(currentDirectory) {
            "Unable to determine the working directory for Compose resource URI: $uri"
        }
        val separator = if (isWindows) "\\" else "/"
        "${workingDirectory.trimEnd('/', '\\')}$separator$path"
    }
    check(access(absolutePath, F_OK) == 0) { "Compose resource file does not exist: $absolutePath" }
    return absolutePath
}

internal expect fun getWritablePath(fileName: String): String

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal actual fun writeModelFile(data: ByteArray, fileName: String): String {
    val filePath = getWritablePath(fileName)
    val file = fopen(filePath, "wb")
        ?: throw IllegalStateException("Could not open model file for writing: $filePath")
    try {
        if (data.isNotEmpty()) {
            val written = data.usePinned { pinned ->
                fwrite(pinned.addressOf(0), 1uL, data.size.toULong(), file)
            }
            check(written == data.size.toULong()) {
                "Could not write all model bytes to: $filePath"
            }
        }
    } finally {
        fclose(file)
    }
    return filePath
}
