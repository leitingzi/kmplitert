package io.github.kmplitert.file

import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

internal actual fun resolveCommonResource(filePath: String): String {
    val bundleResourcePath = NSBundle.mainBundle.resourcePath
    val bundlePath = bundleResourcePath?.let { "$it/$filePath" }
    if (bundlePath != null && NSFileManager.defaultManager.fileExistsAtPath(bundlePath)) {
        return bundlePath
    }

    return resolveCommonResourceFromFileSystem(filePath)
}

internal actual fun resolveComposeUri(uri: String): String {
    val path = if (uri.startsWith("file:")) {
        NSURL.URLWithString(uri)?.path
    } else {
        uri
    }
    check(path != null && NSFileManager.defaultManager.fileExistsAtPath(path)) {
        "Compose resource file does not exist: $uri"
    }
    return path
}
