# Web 初始化 API

`LiteRtInit` 位于 `webMain`，供 JS 和 WasmJS 使用。`LiteRTCompiler.init()` 会调用 `awaitInit()`，因此通常不需要手动初始化。

```kotlin
import io.github.kmplitert.core.LiteRtInit

// 首次初始化开始前配置；目录需包含与 @litertjs/core 匹配的 WASM 资源。
LiteRtInit.setLiteRtWasmPath("/litert/wasm/")
```

| 成员 | 用途 |
|---|---|
| `setLiteRtWasmPath(path)` | 设置资源目录；空路径报错，缺少尾部 `/` 时自动补充。必须在首次初始化开始前调用。 |
| `setCoroutineScope(scope)` | 指定异步初始化使用的协程作用域；必须在首次初始化开始前调用。 |
| `suspend awaitInit()` | 等待共享的初始化任务；失败后允许后续调用重试。 |
| `isInitializing` | 初始化任务是否正在运行。 |
| `isInitialized` | 初始化任务是否成功完成。 |
| `isLoaded` | 任务是否已完成；不要单独用它判断初始化是否成功。 |

默认资源地址为 `https://cdn.jsdelivr.net/npm/@litertjs/core@2.6.0/wasm/`，与当前构建配置的 npm 依赖版本一致。生产环境可以自托管匹配版本的文件；请检查资源路径、跨域策略和网络访问。此 API 只适用于 Web 源集。
