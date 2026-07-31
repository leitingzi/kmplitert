package io.github.kmplitert.tool.image

internal actual fun fromPlatformImage(
    frame: Any,
    rotation: ImageRotation,
    flip: ImageFlip
): LiteRtImage {
    throw UnsupportedOperationException("fromVideoFrame is not supported on MacOS yet.")
}
