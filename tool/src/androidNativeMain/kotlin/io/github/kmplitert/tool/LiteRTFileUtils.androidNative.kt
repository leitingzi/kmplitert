package io.github.kmplitert.tool

internal actual fun getWritablePath(fileName: String): String {
    // For Android Native, we don't have easy access to app's cache dir without passing it from JVM.
    // For now, return the file name as a relative path.
    return fileName
}
