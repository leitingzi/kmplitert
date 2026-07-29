package org.example.kmplitert.runner

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTFileUtils
import kmplitert.app.shared.generated.resources.Res


abstract class BaseLiteRTRunner<I, O>(
    private val modelResourcePath: String,
    private val accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU
) : InferenceRunner<I, O> {

    protected var compiler: LiteRTCompiler? = null
        private set

    protected suspend fun ensureInitialized() {
        if (compiler == null) {
            val modelData = Res.readBytes(modelResourcePath)
            val modelName = modelResourcePath.substringAfterLast("/")
            val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, modelName)

            val newCompiler = LiteRTCompiler(filePath = filePath, accelerator = accelerator)
            newCompiler.init()

            compiler = newCompiler
        }
    }

    override suspend fun close() {
        compiler?.close()
        compiler = null
    }
}
