package io.github.kmplitert.dataprocess.image

internal actual fun decodeVideoFrame(frame: Any): LiteRtImage =
    throw UnsupportedOperationException("Platform video frames are not supported on Windows Native")
