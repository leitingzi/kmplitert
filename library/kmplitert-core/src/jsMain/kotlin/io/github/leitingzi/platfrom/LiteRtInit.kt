package io.github.leitingzi.platfrom

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object LiteRtInit {
    private val mutex = Mutex()
    private var loadTask: Deferred<Unit>? = null

    private var LOAD_URL = "https://cdn.jsdelivr.net/npm/@litertjs/core/wasm/"

    val isLoaded: Boolean
        get() = loadTask?.isCompleted == true

    /**
     * 设置 LiteRT Wasm 资源加载根路径
     *
     * 默认 CDN 地址：`https://cdn.jsdelivr.net/npm/@litertjs/core/wasm/`
     * 也可将 wasm 静态资源部署至业务服务端自行托管
     * 本地 npm 源码路径参考：node_modules/@litertjs/core/wasm/
     *
     * @param path Wasm 文件目录根路径（需以 / 结尾）
     */
    fun setLiteRtWasmFile(path: String) {
        LOAD_URL = path
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    internal suspend fun awaitInit() {
        mutex.withLock {
            if (loadTask == null) {
                loadTask = MainScope().async {
                    val promise = loadLiteRt(LOAD_URL)
                    promise.await()
                }
            }
        }
        loadTask?.await()
    }
}