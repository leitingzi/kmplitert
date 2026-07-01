package io.github.leitingzi.model

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.leitingzi.LiteRtLibrary

class LiteRtEnvironment : PointerType() {
    fun destroy() {
        LiteRtLibrary.INSTANCE.LiteRtDestroyEnvironment(this)
    }

    companion object {
        fun create(options: List<LiteRtEnvOption> = emptyList()): LiteRtEnvironment {
            val ref = PointerByReference()

            val optionsArray = if (options.isNotEmpty()) {
                val first = LiteRtEnvOption()
                val array = first.toArray(options.size) as Array<LiteRtEnvOption>
                options.forEachIndexed { index, opt ->
                    opt.write()
                    array[index].pointer.write(
                        0,
                        opt.pointer.getByteArray(0, opt.size()),
                        0,
                        opt.size()
                    )
                    array[index].read()
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

            // Manually allocate UTF-8 memory for the string to avoid JNA encoding issues
            val bytes = pluginDir.toByteArray(Charsets.UTF_8)
            val memory = Memory((bytes.size + 1).toLong())
            memory.write(0, bytes, 0, bytes.size)
            memory.setByte(bytes.size.toLong(), 0.toByte())

            opt.value.value.str_value = null
            opt.value.value.ptr_value = memory
            opt.value.value.setType(Pointer::class.java)
            opt.value.write()

            val env = create(listOf(opt))
            // Keep memory alive during the native call
            memory.hashCode() 
            return env
        }
    }
}
