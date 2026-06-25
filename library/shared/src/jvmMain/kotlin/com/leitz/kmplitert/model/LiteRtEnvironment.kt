package com.leitz.kmplitert.model

import com.leitz.kmplitert.LiteRtLibrary
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

class LiteRtEnvironment : PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyEnvironment(this)
    }

    companion object {
        fun create(): LiteRtEnvironment {
            val ref = PointerByReference()
            val status = LiteRtLibrary.INSTANCE.LiteRtCreateEnvironment(
                num_options = 0,
                options = null,
                environment = ref
            )

            check(status == 0) {
                "Failed to create environment: $status"
            }

            val env = LiteRtEnvironment()
            env.pointer = ref.value
            return env
        }
    }
}