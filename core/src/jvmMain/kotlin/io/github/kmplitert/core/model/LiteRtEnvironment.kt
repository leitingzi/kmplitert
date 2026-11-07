package io.github.kmplitert.core.model

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import io.github.kmplitert.core.LiteRtLibrary
import io.github.kmplitert.core.checkLiteRtStatus
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtEnvironment : PointerType() {
    private var retainedOptions: List<LiteRtEnvOption> = emptyList()
    private val destroyed = AtomicBoolean(false)

    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            try {
                LiteRtLibrary.INSTANCE.LiteRtDestroyEnvironment(this)
            } finally {
                retainedOptions = emptyList()
                pointer = null
            }
        }
    }

    companion object {
        fun create(options: List<LiteRtEnvOption> = emptyList()): LiteRtEnvironment {
            val ref = PointerByReference()

            val optionsArray = if (options.isNotEmpty()) {
                val first = LiteRtEnvOption()

                @Suppress("UNCHECKED_CAST")
                val array = first.toArray(options.size) as Array<LiteRtEnvOption>
                options.forEachIndexed { index, opt ->
                    opt.write()
                    opt.copyTo(array[index])
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

            checkLiteRtStatus(status, "LiteRtCreateEnvironment")

            val env = LiteRtEnvironment()
            env.pointer = requireNotNull(ref.value) {
                "LiteRtCreateEnvironment returned a null handle"
            }
            env.retainedOptions = options
            return env
        }

        fun createWithPluginDir(pluginDir: String): LiteRtEnvironment {
            val opt = LiteRtEnvOption()
            opt.tag = LiteRtEnvOptionTag.COMPILER_PLUGIN_LIBRARY_DIR
            opt.value.type = LiteRtAnyType.STRING

            // Manually allocate UTF-8 memory for the string to avoid JNA encoding issues
            val bytes = pluginDir.toByteArray(Charsets.UTF_8)
            val memory = Memory((bytes.size + 1).toLong())
            memory.write(0, bytes, 0, bytes.size)
            memory.setByte(bytes.size.toLong(), 0.toByte())

            opt.value.value.ptr_value = memory
            opt.value.value.setType(Pointer::class.java)
            opt.value.write()
            opt.retainedMemory = memory

            return create(listOf(opt))
        }
    }
}
