package io.github.kmplitert.tool.image

internal actual fun fromVideoFrameNative(
    frame: Any,
    rotation: LiteRtRotation,
    flip: LiteRtFlip
): LiteRtImage {
    throw UnsupportedOperationException("fromVideoFrame is not supported on Linux yet.")
}
