package io.github.leitingzi.kmplitert.core

actual class LiteRtImage {
    actual fun resize(width: Int, height: Int): LiteRtImage {
        // Placeholder implementation for Web
        return this
    }

    actual fun toFloatArray(mean: Float, std: Float): FloatArray {
        // Placeholder implementation for Web
        return floatArrayOf()
    }

    actual companion object {
        actual fun fromBytes(bytes: ByteArray): LiteRtImage {
            // Placeholder implementation for Web
            return LiteRtImage()
        }
    }
}
