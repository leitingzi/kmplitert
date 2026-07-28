package io.github.kmplitert.tool

internal actual fun fromVideoFrameNative(
    frame: Any,
    rotation: LiteRtRotation,
    flip: LiteRtFlip
): LiteRtImage {
    throw UnsupportedOperationException("fromVideoFrame is not supported on Windows yet.")
}
