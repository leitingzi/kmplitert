package io.github.kmplitert.tool

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A generic base class for model-specific preprocessing and postprocessing logic,
 * combined with high-level task orchestration.
 *
 * @param I The type of the input data to be preprocessed (e.g., LiteRtImage).
 * @param O The type of the final result returned after postprocessing.
 */
abstract class LiteRTHandler<I, O> {

    private var _compiler: LiteRTCompiler? = null
    private val initLock = Mutex()

    /**
     * The [LiteRTCompiler] instance used for inference.
     * Accessing this before [init] or after [close] will throw an error.
     */
    protected val compiler: LiteRTCompiler
        get() = _compiler ?: error("Compiler not initialized. Call init() first.")

    /**
     * Sets the internal [LiteRTCompiler] instance.
     * Should be called within the [init] method of a subclass.
     */
    protected fun setCompiler(compiler: LiteRTCompiler) {
        _compiler = compiler
    }

    /**
     * Helper method to initialize the [LiteRTCompiler] in a thread-safe manner.
     * This method creates the compiler instance, calls its [LiteRTCompiler.init] method,
     * and sets it as the internal compiler.
     *
     * @param filePath The absolute path to the .tflite model file.
     * @param accelerator The hardware accelerator to use.
     */
    protected suspend fun setupCompiler(filePath: String, accelerator: LiteRTAccelerator) {
        if (_compiler != null) return
        initLock.withLock {
            if (_compiler != null) return@withLock
            val newCompiler = LiteRTCompiler(filePath = filePath, accelerator = accelerator)
            newCompiler.init()
            _compiler = newCompiler
        }
    }

    /**
     * Initializes the handler and its underlying resources.
     * This is an optional step that can be called before the first [runTask].
     * Subclasses should initialize the compiler here using [setupCompiler] or [setCompiler].
     */
    open suspend fun init() {
        // Optional initialization
    }

    /**
     * Performs preprocessing on the input data and fills the input buffers.
     *
     * @param input The input data to process.
     * @param inputBuffers The list of input buffers to be filled.
     */
    protected abstract suspend fun preprocess(input: I, inputBuffers: List<TFBuffer>)

    /**
     * Performs postprocessing on the output buffers and returns the inference results.
     *
     * @param outputBuffers The list of output buffers containing inference results.
     * @return The processed result of type [O].
     */
    protected abstract suspend fun postprocess(outputBuffers: List<TFBuffer>): O

    /**
     * Executes the full LiteRT task: preprocess -> run -> postprocess.
     *
     * This method automatically calls [init] if the compiler is not yet initialized.
     *
     * @param input The input data to process.
     * @return The final processed result.
     */
    suspend fun runTask(input: I): O {
        if (_compiler == null) {
            initLock.withLock {
                if (_compiler == null) {
                    init()
                }
            }
        }
        
        val inputBuffers = compiler.getInputBuffers()
        preprocess(input, inputBuffers)

        val outputBuffers = compiler.getOutputBuffers()
        compiler.run(inputBuffers, outputBuffers)

        return postprocess(outputBuffers)
    }

    /**
     * Closes the underlying [LiteRTCompiler].
     */
    open suspend fun close() {
        initLock.withLock {
            _compiler?.close()
            _compiler = null
        }
    }
}
