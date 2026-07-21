package io.github.kmplitert.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.js.ExperimentalWasmJsInterop

/**
 * Initializes the LiteRT Wasm runtime.
 *
 * This object guarantees that the LiteRT runtime is loaded only once,
 * even when multiple coroutines call [awaitInit] concurrently.
 *
 * By default, LiteRT Wasm resources are loaded from the official jsDelivr CDN:
 *
 * `https://cdn.jsdelivr.net/npm/@litertjs/core/wasm/`
 *
 * Applications may override the resource location using [setLiteRtWasmPath]
 * before the first initialization.
 *
 * Initialization is thread-safe and idempotent.
 * If initialization fails, the internal state is reset so that a subsequent
 * call to [awaitInit] can retry loading.
 */
object LiteRtInit {

    private val mutex = Mutex()
    private var initTask: Deferred<Unit>? = null

    private var scope = MainScope()

    private var wasmRootPath = "https://cdn.jsdelivr.net/npm/@litertjs/core/wasm/"

    /**
     * Returns whether the LiteRT runtime has been successfully initialized.
     */
    val isInitialized: Boolean
        get() = initTask?.isCompleted == true && initTask?.isCancelled == false

    /**
     * Returns whether initialization is currently in progress.
     */
    val isInitializing: Boolean
        get() = initTask?.isActive == true

    val isLoaded: Boolean get() = initTask?.isCompleted == true

    /**
     * Sets the root directory used to load LiteRT Wasm resources.
     *
     * This method must be called before the first invocation of [awaitInit].
     *
     * The supplied path must point to the directory containing the LiteRT Wasm
     * runtime files and should end with a trailing `/`.
     *
     * Example:
     *
     * ```
     * LiteRtInit.setLiteRtWasmPath(
     *     "https://cdn.jsdelivr.net/npm/@litertjs/core/wasm/"
     * )
     * ```
     *
     * @param path Root directory containing LiteRT Wasm resources.
     *
     * @throws IllegalStateException If initialization has already started.
     * @throws IllegalArgumentException If the path is blank.
     */
    fun setLiteRtWasmPath(path: String) {
        check(initTask == null) {
            "LiteRT initialization has already started."
        }

        require(path.isNotBlank()) {
            "The Wasm resource path must not be blank."
        }

        wasmRootPath = if (path.endsWith('/')) path else "$path/"
    }

    /**
     * Sets the coroutine scope used for asynchronous initialization.
     *
     * This method must be called before the first invocation of [awaitInit].
     *
     * @param scope Coroutine scope used to launch the initialization task.
     *
     * @throws IllegalStateException If initialization has already started.
     */
    fun setCoroutineScope(scope: CoroutineScope) {
        check(initTask == null) {
            "LiteRT initialization has already started."
        }

        this.scope = scope
    }

    /**
     * Suspends until the LiteRT runtime has been initialized.
     *
     * If multiple coroutines invoke this function simultaneously,
     * only one initialization task will be executed and all callers
     * will await the same result.
     *
     * If initialization fails, the internal state is reset so that
     * subsequent invocations can retry initialization.
     *
     * @throws Throwable If the LiteRT runtime fails to load.
     */
    @OptIn(ExperimentalWasmJsInterop::class)
    suspend fun awaitInit() {
        val task = mutex.withLock {
            initTask ?: scope.async {
                try {
                    loadLiteRt(path = wasmRootPath).await()
                    Unit
                } catch (t: Throwable) {
                    mutex.withLock {
                        initTask = null
                    }
                    throw t
                }
            }.also {
                initTask = it
            }
        }

        task.await()
    }
}