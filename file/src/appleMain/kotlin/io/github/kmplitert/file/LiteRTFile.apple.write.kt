package io.github.kmplitert.file

import platform.Foundation.NSTemporaryDirectory

internal actual fun getWritablePath(fileName: String): String {
    val temporaryDirectory = NSTemporaryDirectory()
    return if (temporaryDirectory.endsWith("/")) {
        "$temporaryDirectory$fileName"
    } else {
        "$temporaryDirectory/$fileName"
    }
}
