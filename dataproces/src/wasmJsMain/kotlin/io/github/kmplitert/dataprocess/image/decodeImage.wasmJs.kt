package io.github.kmplitert.dataprocess.image

actual fun decodeImage(bytes: ByteArray): LiteRtImage {
    return BitmapDecoder.decodeToLiteRtImage(bytes)
}
