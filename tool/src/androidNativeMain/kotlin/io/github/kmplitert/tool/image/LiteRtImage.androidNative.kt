package io.github.kmplitert.tool.image

import io.github.kmplitert.tool.image.ImageFlip
import io.github.kmplitert.tool.image.ImageRotation
import io.github.kmplitert.tool.image.LiteRtImage

internal actual fun fromVideoFrameNative(
    frame: Any,
    rotation: ImageRotation,
    flip: ImageFlip
): LiteRtImage {
    throw UnsupportedOperationException("fromVideoFrameNative is not supported on Android Native yet.")
}
