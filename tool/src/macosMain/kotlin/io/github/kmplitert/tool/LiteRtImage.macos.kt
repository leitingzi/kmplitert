package io.github.kmplitert.tool

internal actual fun fromPlatformImage(
    frame: Any,
    rotation: LiteRtRotation,
    flip: LiteRtFlip
): LiteRtImage {
    throw UnsupportedOperationException("fromVideoFrame is not supported on MacOS yet.")
}
