package io.github.leitingzi.model

import com.sun.jna.Structure

@Structure.FieldOrder("tag", "value")
open class LiteRtEnvOption : Structure() {
    @JvmField
    var tag: Int = 0

    @JvmField
    var value: LiteRtAny = LiteRtAny()

    class ByReference : LiteRtEnvOption(), Structure.ByReference
}