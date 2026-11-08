# 模型与推理生命周期

一轮推理可以分为五个阶段：**准备模型 → 初始化 → 创建缓冲区 → 写入并运行 → 释放资源**。

```text
modelPath → LiteRTCompiler → init()
                               ↓
                    input / output buffers
                               ↓
                      write → run → read
                               ↓
                         close resources
```

## 初始化

使用模型路径和可选的 `LiteRTAccelerator` 创建 `LiteRTCompiler`，默认加速器是 CPU。调用 `init()` 之后再查询张量或创建缓冲区。

编译器操作和读取方法是 `suspend` 函数，需要从协程调用。`suspend` 并不保证每个平台都切换到后台线程，也不保证可以并发调用同一个实例；应根据应用的线程和生命周期设计调度。

## 查询与运行

模型的输入输出顺序、名称、数据类型和维度由模型定义。可通过 `getInputTensorType(name)`、`getOutputTensorType(name)` 和对应的 buffer requirements 查询元数据。名称必须来自模型；公共 API 没有枚举张量名称的方法。

从同一个编译器和签名获取输入、输出缓冲区，按模型约定的顺序写入数据，再调用 `run(inputs, outputs)`。运行完成后读取输出。多签名模型要注意[平台差异](../platforms.md#differences)。

## 释放 {#cleanup}

以 `try/finally` 管理编译器和已经获取的缓冲区，参照[完整示例](../getting-started.md#inference)。示例按 Native 测试的顺序先关闭缓冲区、再关闭编译器；JVM 编译器关闭时也会释放其跟踪的缓冲区。Android 的当前缓冲区包装实现沿用接口的空 `close()`，Web 包装的 `close()` 也沿用该默认实现。

不要在关闭后继续使用缓冲区或编译器，也不要依赖重复初始化、重复关闭或多个协程并发访问在所有平台表现一致。

## 重复推理

在模型和资源仍然有效时，可以根据模型需求更新输入并再次调用 `run`。模型、数据类型或签名发生变化时，不要假设先前缓冲区仍然兼容；重新核对目标平台和模型要求。
