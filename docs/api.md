# Core API

核心 API 位于 `io.github.kmplitert.core`。它由共享接口和平台实现组成。

## `LiteRTCompiler`

`LiteRTCompiler` 管理模型实例和推理流程：

1. 使用模型路径和加速器创建实例。
2. 调用 `init()` 初始化模型。
3. 通过 `getInputBuffers()` / `getOutputBuffers()` 获取输入和输出张量缓冲区。
4. 按模型要求写入输入数据并调用 `run()`。
5. 读取输出后调用 `close()` 释放资源。

```kotlin
val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
try {
    compiler.init()
    val inputs = compiler.getInputBuffers()
    val outputs = compiler.getOutputBuffers()

    inputs[0].writeFloat(inputValues)
    compiler.run(inputs, outputs)
    val result = outputs[0].readFloat()
} finally {
    compiler.close()
}
```

`getInputTensorType()`、`getOutputTensorType()` 和对应的 `get*BufferRequirements()` 可用于查询张量类型及缓冲区要求。多签名模型可使用 `signatureIndex` 参数选择签名。

## `TFBuffer`

`TFBuffer` 提供类型化数据读写：

| 数据类型 | 写入 | 读取 |
| --- | --- | --- |
| 32 位整数 | `writeInt(IntArray)` | `readInt()` |
| 浮点数 | `writeFloat(FloatArray)` | `readFloat()` |
| 8 位整数 | `writeInt8(ByteArray)` | `readInt8()` |
| 布尔值 | `writeBoolean(BooleanArray)` | `readBoolean()` |
| 64 位整数 | `writeLong(LongArray)` | `readLong()` |

读取函数是挂起函数。选择读写类型时，应与模型张量的数据类型相符；调用者还应遵守模型报告的缓冲区要求。

## `LiteRTHandler`（`kmplitert-tool`）

需要复用预处理、推理和后处理流程时，可继承 `LiteRTHandler<I, T, O>` 并实现以下挂起函数：

```kotlin
abstract suspend fun init()
abstract suspend fun transform(input: I): T
abstract suspend fun feed(data: T, inputBuffers: List<TFBuffer>)
abstract suspend fun postprocess(outputBuffers: List<TFBuffer>): O
```

`init()` 中通常通过 `setupCompiler(...)` 配置模型。调用 `runTask(input)` 执行任务；`status` 提供 `Idle`、`Initializing`、`Ready`、`Running`、`Closing` 和 `Error` 状态。调用 `close()` 释放 compiler 资源。

Handler 支持在 `TASK`、`TRANSFORM`、`FEED`、`INFERENCE` 和 `POSTPROCESS` 阶段添加 interceptor。请查看仓库 README 的完整示例和 `tool` 模块源码，确认与你的版本相匹配的 API 签名。
