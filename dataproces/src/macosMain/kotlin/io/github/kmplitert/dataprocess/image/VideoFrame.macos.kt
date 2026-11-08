package io.github.kmplitert.dataprocess.image

internal actual fun fromAppleImage(frame: Any): LiteRtImage =
    throw UnsupportedOperationException("Platform image objects are not supported on macOS")
