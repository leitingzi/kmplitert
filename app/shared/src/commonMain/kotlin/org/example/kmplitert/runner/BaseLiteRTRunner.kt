package org.example.kmplitert.runner

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.tool.LiteRTFileUtils
import io.github.kmplitert.tool.LiteRTHandler
import kmplitert.app.shared.generated.resources.Res


abstract class BaseLiteRTRunner<I, O>(
    private val modelResourcePath: String,
    private val accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU
) : LiteRTHandler<I, O>(), InferenceRunner<I, O> {

    private var _compiler: LiteRTCompiler? = null

    override val compiler: LiteRTCompiler
        get() = _compiler ?: throw IllegalStateException("Compiler not initialized. Call init() first.")

    override suspend fun init() {
        ensureInitialized()
    }

    protected suspend fun ensureInitialized() {
        if (_compiler == null) {
            val modelData = Res.readBytes(modelResourcePath)
            val modelName = modelResourcePath.substringAfterLast("/")
            val filePath = LiteRTFileUtils.createFileFromByteArray(modelData, modelName)

            val newCompiler = LiteRTCompiler(filePath = filePath, accelerator = accelerator)
            newCompiler.init()

            _compiler = newCompiler
        }
    }

    override suspend fun run(input: I): Result<O> {
        return try {
            init()
            Result.success(runTask(input))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun close() {
        _compiler?.close()
        _compiler = null
    }
}
