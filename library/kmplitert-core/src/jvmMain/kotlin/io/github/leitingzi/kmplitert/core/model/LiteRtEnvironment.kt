package io.github.leitingzi.kmplitert.core.model

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.kmplitert.core.LiteRtLibrary

class LiteRtEnvironment : PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyEnvironment(this)
    }

    companion object {
        fun create(options: List<LiteRtEnvOption> = emptyList()): LiteRtEnvironment {
            val ref = PointerByReference()

            val optionsArray = if (options.isNotEmpty()) {
                val array = LiteRtEnvOption().toArray(options.size) as Array<LiteRtEnvOption>
                options.forEachIndexed { index, opt ->
                    array[index].tag = opt.tag
                    array[index].value.type = opt.value.type
                    array[index].value.value = opt.value.value
                }
                array[0]
            } else {
                null
            }

            val status = LiteRtLibrary.INSTANCE.LiteRtCreateEnvironment(
                num_options = options.size,
                options = optionsArray,
                environment = ref
            )

            check(status == 0) {
                "Failed to create environment: $status"
            }

            val env = LiteRtEnvironment()
            env.pointer = ref.value
            return env
        }

        fun createWithPluginDir(pluginDir: String): LiteRtEnvironment {
            val opt = LiteRtEnvOption()
            opt.tag = 0 // kLiteRtEnvOptionTagCompilerPluginLibraryDir
            opt.value.type = 8 // kLiteRtAnyTypeString
            opt.value.value.str_value = pluginDir
            opt.value.value.setType(String::class.java)
            opt.value.value.write()

            return create(listOf(opt))
        }
    }
}
