package io.github.kmplitert.tool

import platform.Foundation.NSTemporaryDirectory

internal actual fun getWritablePath(fileName: String): String {
    val tempDir = NSTemporaryDirectory()
    return if (tempDir.endsWith("/")) {
        "$tempDir$fileName"
    } else {
        "$tempDir/$fileName"
    }
}
