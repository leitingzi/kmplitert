@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kmplitert.file

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.getcwd
import platform.posix.getenv

internal actual fun getWritablePath(fileName: String): String {
    val temporaryDirectory = sequenceOf("TEMP", "TMP")
        .mapNotNull { name -> getenv(name)?.toKString() }
        .firstOrNull { it.matches(Regex("^[A-Za-z]:[/\\\\].*")) }
    val directory = temporaryDirectory ?: memScoped {
        val buffer = allocArray<ByteVar>(4096)
        checkNotNull(getcwd(buffer, 4096)?.toKString()) {
            "Unable to determine the working directory for model file creation."
        }
    }
    val separator = if (directory.contains('\\')) "\\" else "/"
    return "${directory.trimEnd('/', '\\')}$separator$fileName"
}
