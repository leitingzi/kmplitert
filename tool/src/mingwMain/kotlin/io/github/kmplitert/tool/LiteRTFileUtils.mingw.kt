package io.github.kmplitert.tool

internal actual fun getWritablePath(fileName: String): String {
    // For Windows, we'll keep using the relative path for now.
    return fileName
}
