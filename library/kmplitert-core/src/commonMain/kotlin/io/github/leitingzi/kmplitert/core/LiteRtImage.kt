@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.leitingzi.kmplitert.core

expect class LiteRtImage {
    fun resize(width: Int, height: Int): LiteRtImage
    fun toFloatArray(mean: Float = 0f, std: Float = 1f): FloatArray

    companion object {
        fun fromBytes(bytes: ByteArray): LiteRtImage
    }
}
