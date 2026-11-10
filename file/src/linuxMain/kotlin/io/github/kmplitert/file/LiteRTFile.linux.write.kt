@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kmplitert.file

import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual fun getWritablePath(fileName: String): String {
    val temporaryDirectory = getenv("TMPDIR")?.toKString()
        ?.takeIf { it.startsWith("/") }
        ?: "/tmp"
    return if (temporaryDirectory.endsWith("/")) {
        "$temporaryDirectory$fileName"
    } else {
        "$temporaryDirectory/$fileName"
    }
}
