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

    // On Windows (mingwX64), the return type of `ftell` may be implicitly handled or mapped to `Int` by Kotlin/Native.
    // However, on macOS (macosArm64), `ftell` strictly returns a `Long` (corresponding to the C `long` type), whereas the `ByteArray(size)` constructor accepts only an `Int`.
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

