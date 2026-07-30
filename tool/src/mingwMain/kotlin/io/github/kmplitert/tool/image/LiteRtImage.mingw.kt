package io.github.kmplitert.tool.image

internal actual fun fromVideoFrameNative(
    frame: Any,
    rotation: ImageRotation,
    flip: ImageFlip
): LiteRtImage {
    throw UnsupportedOperationException("fromVideoFrame is not supported on Windows yet.")
}
