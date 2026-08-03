package io.github.kmplitert.tool

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.interceptor.LiteRTInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * A generic base class for model-specific preprocessing and postprocessing logic,
 * combined with high-level task orchestration and interceptor support.
 *
 * @param I The type of the input data to be preprocessed (e.g., LiteRtImage).
 * @param O The type of the final result returned after postprocessing.
 */
abstract class LiteRTHandler<I, O> {

    /**
     * Represents the current status of the [LiteRTHandler].
     */
    sealed interface Status {
        data object Idle : Status
        data object Initializing : Status
        data object Ready : Status
        data object Running : Status
        data object Closing : Status
        data class Error(val throwable: Throwable) : Status
    }

    private var _compiler: LiteRTCompiler? = null
    private val initLock = Mutex()
    private val taskLock = Mutex()

    private var dispatcher: CoroutineDispatcher = Dispatchers.Default
    private val _status = MutableStateFlow<Status>(Status.Idle)
    
    private val interceptors = mutableListOf<LiteRTInterceptor<I, O>>()

    /**
     * A [StateFlow] that tracks the current [Status] of the handler.
     */
    val status: StateFlow<Status> = _status.asStateFlow()

    protected val compiler: LiteRTCompiler
        get() = _compiler ?: error("Compiler not initialized. Call init() first.")

    fun setDispatcher(dispatcher: CoroutineDispatcher) {
        this.dispatcher = dispatcher
    }

    /**
     * Adds an interceptor to the task execution chain.
     */
    fun addInterceptor(interceptor: LiteRTInterceptor<I, O>) {
        interceptors.add(interceptor)
    }

    /**
     * Removes an interceptor from the task execution chain.
     */
    fun removeInterceptor(interceptor: LiteRTInterceptor<I, O>) {
        interceptors.remove(interceptor)
    }

    /**
     * Clears all interceptors from the task execution chain.
     */
    fun clearInterceptors() {
        interceptors.clear()
    }

    protected fun updateStatus(status: Status) {
        _status.value = status
    }

    protected fun setCompiler(compiler: LiteRTCompiler) {
        _compiler = compiler
    }

    protected suspend fun setupCompiler(filePath: String, accelerator: LiteRTAccelerator) = withContext(dispatcher) {
        if (_compiler != null) return@withContext
        initLock.withLock {
            if (_compiler != null) return@withLock
            updateStatus(Status.Initializing)
            try {
                val newCompiler = LiteRTCompiler(filePath = filePath, accelerator = accelerator)
                newCompiler.init()
                setCompiler(compiler = newCompiler)
                updateStatus(Status.Ready)
            } catch (e: Exception) {
                updateStatus(Status.Error(e))
                throw e
            }
        }
    }

    open suspend fun init() {}

    protected abstract suspend fun preprocess(input: I, inputBuffers: List<TFBuffer>)

    protected abstract suspend fun postprocess(outputBuffers: List<TFBuffer>): O

    /**
     * Executes the full LiteRT task through the interceptor chain.
     */
    suspend fun runTask(input: I): O = withContext(dispatcher) {
        val allInterceptors = interceptors.toMutableList()
        allInterceptors.add(BaseInferenceInterceptor())
        
        val chain = RealInterceptorChain(0, input, allInterceptors)
        chain.proceed(input)
    }

    private inner class BaseInferenceInterceptor : LiteRTInterceptor<I, O> {
        override suspend fun intercept(chain: LiteRTInterceptor.Chain<I, O>): O {
            return performDirectInference(chain.input)
        }
    }

    private suspend fun performDirectInference(input: I): O {
        return taskLock.withLock {
            if (_compiler == null) {
                initLock.withLock {
                    if (_compiler == null) {
                        updateStatus(Status.Initializing)
                        try {
                            init()
                            if (_compiler != null && _status.value == Status.Initializing) {
                                updateStatus(Status.Ready)
                            }
                        } catch (e: Exception) {
                            updateStatus(Status.Error(e))
                            throw e
                        }
                    }
                }
            }

            updateStatus(Status.Running)
            try {
                val inputBuffers = compiler.getInputBuffers()
                preprocess(input = input, inputBuffers = inputBuffers)

                val outputBuffers = compiler.getOutputBuffers()
                compiler.run(inputs = inputBuffers, outputs = outputBuffers)

                val result = postprocess(outputBuffers = outputBuffers)
                updateStatus(Status.Ready)
                result
            } catch (e: Exception) {
                updateStatus(Status.Error(e))
                throw e
            }
        }
    }

    private inner class RealInterceptorChain(
        private val index: Int,
        override val input: I,
        private val interceptors: List<LiteRTInterceptor<I, O>>
    ) : LiteRTInterceptor.Chain<I, O> {
        
        override suspend fun proceed(input: I): O {
            if (index >= interceptors.size) throw AssertionError("Chain reached end without base execution")
            
            val next = RealInterceptorChain(index + 1, input, interceptors)
            val interceptor = interceptors[index]
            
            return interceptor.intercept(next)
        }
    }

    open suspend fun close() = withContext(dispatcher) {
        initLock.withLock {
            updateStatus(Status.Closing)
            try {
                _compiler?.close()
                _compiler = null
                updateStatus(Status.Idle)
            } catch (e: Exception) {
                updateStatus(Status.Error(e))
                throw e
            }
        }
    }
}
