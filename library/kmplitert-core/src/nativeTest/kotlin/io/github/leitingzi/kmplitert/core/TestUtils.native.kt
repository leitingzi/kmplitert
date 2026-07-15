package io.github.leitingzi.kmplitert.core

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadResourceAsBytes(name: String): ByteArray {
    val paths = listOf(
        "src/commonTest/resources/$name",
        "../library/kmplitert-core/src/commonTest/resources/$name",
        name
    )
    
    var file: CPointer<FILE>? = null
    for (path in paths) {
        file = fopen(path, "rb")
        if (file != null) break
    }
    
    if (file == null) throw IllegalArgumentException("Resource not found: $name")
    
    fseek(file, 0, SEEK_END)
    val size = ftell(file)
    fseek(file, 0, SEEK_SET)
    
    val buffer = ByteArray(size.toInt())
    buffer.usePinned { pinned ->
        fread(pinned.addressOf(0), 1.toULong(), size.toULong(), file)
    }
    fclose(file)
    return buffer
}

actual fun LiteRTAccelerator.isSupportedOnCurrentPlatform(): Boolean {
    return true
}

