package io.github.kmplitert.file

internal actual fun resolveCommonResource(filePath: String): String =
    resolveCommonResourceFromFileSystem(filePath)

internal actual fun resolveComposeUri(uri: String): String =
    resolveComposeUriFromFileSystem(uri)
