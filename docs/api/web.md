# Web Initialization API

`LiteRtInit` is in `webMain` for JS and WasmJS. `LiteRTCompiler.init()` calls `awaitInit()`, so manual initialization is normally unnecessary.

```kotlin
import io.github.kmplitert.core.LiteRtInit

// Configure before initialization starts; the directory must contain matching WASM assets.
LiteRtInit.setLiteRtWasmPath("/litert/wasm/")
```

| Member | Purpose |
|---|---|
| `setLiteRtWasmPath(path)` | Set the asset directory; rejects blank paths and adds a trailing `/` if needed. Call before initialization starts. |
| `setCoroutineScope(scope)` | Set the coroutine scope for asynchronous initialization before initialization starts. |
| `suspend awaitInit()` | Await the shared initialization task; a failed task can be retried later. |
| `isInitializing` | Whether the task is running. |
| `isInitialized` | Whether the task completed successfully. |
| `isLoaded` | Whether the task completed; do not use this alone to determine success. |

The default asset URL is `https://cdn.jsdelivr.net/npm/@litertjs/core@2.6.0/wasm/`, matching the current npm dependency. Production applications may self-host matching files; check the asset path, cross-origin policy, and network access. This API is available only in web source sets.
