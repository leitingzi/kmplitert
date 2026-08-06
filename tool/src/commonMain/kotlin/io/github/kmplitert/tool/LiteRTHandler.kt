package io.github.kmplitert.tool

import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer
import io.github.kmplitert.tool.interceptor.FeedInterceptor
import io.github.kmplitert.tool.interceptor.InferenceInterceptor
import io.github.kmplitert.tool.interceptor.LiteRTInterceptor
import io.github.kmplitert.tool.interceptor.LiteRTLoggingInterceptor
import io.github.kmplitert.tool.interceptor.LiteRTResultCacheInterceptor
import io.github.kmplitert.tool.interceptor.PhaseBoundaryInterceptor
import io.github.kmplitert.tool.interceptor.PostprocessInterceptor
import io.github.kmplitert.tool.interceptor.RealInterceptorChain
import io.github.kmplitert.tool.interceptor.TransformInterceptor
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

    private var dispatcher = Dispatchers.Default
    private val _status = MutableStateFlow<Status>(Status.Idle)
    
    private val interceptors = LiteRTPhase.entries.associateWith { 
        mutableListOf<LiteRTInterceptor<I, O>>() 
    }

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
     * Adds an interceptor to the task execution chain at the specified phase.
     */
    fun addInterceptor(
        interceptor: LiteRTInterceptor<I, O>,
        phase: LiteRTPhase = LiteRTPhase.TASK
    ) {
        interceptors[phase]?.add(interceptor)
    }

    /**
     * Removes an interceptor from the task execution chain.
     */
    fun removeInterceptor(interceptor: LiteRTInterceptor<I, O>) {
        interceptors.values.forEach { it.remove(interceptor) }
    }

    /**
     * Clears all interceptors from the task execution chain.
     */
    fun clearInterceptors() {
        interceptors.values.forEach { it.clear() }
    }

    protected fun updateStatus(status: Status) {
        _status.value = status
    }

    protected fun setCompiler(compiler: LiteRTCompiler) {
        _compiler = compiler
    }

    protected suspend fun setupCompiler(filePath: String, accelerator: LiteRTAccelerator) {
        withContext(context = dispatcher) {
            if (_compiler != null) {
                return@withContext
            }

            initLock.withLock {
                if (_compiler != null) {
                    return@withLock
                }

                updateStatus(Status.Initializing)

                try {
                    val newCompiler = LiteRTCompiler(filePath = filePath, accelerator = accelerator)
                    newCompiler.init()
                    setCompiler(compiler = newCompiler)
                    updateStatus(status = Status.Ready)
                } catch (e: Exception) {
                    updateStatus(status = Status.Error(throwable = e))
                    throw e
                }
            }
        }
    }

    open suspend fun init() {

    }

    /**
     * Transforms the input into an intermediate format (e.g., resizing an image).
     */
    protected abstract suspend fun transform(input: I): Any?

    /**
     * Feeds the transformed data into the model's input buffers.
     */
    protected abstract suspend fun feed(data: Any?, inputBuffers: List<TFBuffer>)

    /**
     * Postprocesses the output buffers to produce the final result.
     */
    protected abstract suspend fun postprocess(outputBuffers: List<TFBuffer>): O

    /**
     * Executes the full LiteRT task through the interceptor chain.
     */
    suspend fun runTask(input: I): O {
        return withContext(context = dispatcher) {
            updateStatus(status = Status.Running)
            try {
                taskLock.withLock {
                    ensureInitialized()
                    val inputBuffers = compiler.getInputBuffers()
                    val outputBuffers = compiler.getOutputBuffers()

                    val chainList = buildFlattenedChain()

                    val rootChain = RealInterceptorChain(
                        phase = LiteRTPhase.TASK,
                        interceptors = chainList,
                        index = 0,
                        input = input,
                        transformedData = null,
                        inputBuffers = inputBuffers,
                        outputBuffers = outputBuffers
                    )

                    val result = rootChain.proceed(input = input)
                    updateStatus(status = Status.Ready)
                    result
                }
            } catch (e: Exception) {
                if (_compiler != null) {
                    updateStatus(status = Status.Ready)
                } else {
                    updateStatus(status = Status.Error(throwable = e))
                }
                throw e
            }
        }
    }

    private fun buildFlattenedChain(): List<LiteRTInterceptor<I, O>> {
        return buildList {
            // 1. TASK phase
            addAll(interceptors[LiteRTPhase.TASK]!!)

            // 2. TRANSFORM phase
            add(PhaseBoundaryInterceptor(LiteRTPhase.TRANSFORM))
            addAll(interceptors[LiteRTPhase.TRANSFORM]!!)
            add(TransformInterceptor(::transform))

            // 3. FEED phase
            add(PhaseBoundaryInterceptor(LiteRTPhase.FEED))
            addAll(interceptors[LiteRTPhase.FEED]!!)
            add(FeedInterceptor(::feed))

            // 4. INFERENCE phase
            add(PhaseBoundaryInterceptor(LiteRTPhase.INFERENCE))
            addAll(interceptors[LiteRTPhase.INFERENCE]!!)
            add(InferenceInterceptor(compiler::run))

            // 5. POSTPROCESS phase
            add(PhaseBoundaryInterceptor(LiteRTPhase.POSTPROCESS))
            addAll(interceptors[LiteRTPhase.POSTPROCESS]!!)
            add(PostprocessInterceptor(::postprocess))
        }
    }

    private suspend fun ensureInitialized() {
        if (_compiler == null) {
            initLock.withLock {
                if (_compiler == null) {
                    updateStatus(status = Status.Initializing)
                    try {
                        init()
                        if (_compiler != null && _status.value == Status.Initializing) {
                            updateStatus(status = Status.Ready)
                            updateStatus(status = Status.Running)
                        }
                    } catch (e: Exception) {
                        updateStatus(status = Status.Error(throwable = e))
                        throw e
                    }
                }
            }
        }
    }

    open suspend fun close() = withContext(dispatcher) {
        initLock.withLock {
            updateStatus(status = Status.Closing)
            try {
                _compiler?.close()
                _compiler = null
                updateStatus(status = Status.Idle)
            } catch (e: Exception) {
                updateStatus(status = Status.Error(throwable = e))
                throw e
            }
        }
    }
}

