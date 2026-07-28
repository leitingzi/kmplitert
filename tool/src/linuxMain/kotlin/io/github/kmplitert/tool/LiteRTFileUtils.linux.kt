package io.github.kmplitert.tool

internal actual fun getWritablePath(fileName: String): String {
    // For Linux, we'll keep using the relative path for now, 
    // or we could use /tmp/ if needed.
    return fileName
}
