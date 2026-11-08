# Core API

公共 API 位于 `io.github.kmplitert.core`。以下是共享代码常用的接口；平台底层绑定不应被视为稳定的跨平台抽象。

## LiteRTCompiler

```kotlin
LiteRTCompiler(
    filePath: String,
    accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU
)
```

| 方法 | 用途 |
|---|---|
| `suspend init()` | 加载并准备模型。其他操作前调用。 |
| `suspend getInputTensorType(inputName)` | 按名称查询输入的元素类型和布局。 |
| `suspend getOutputTensorType(outputName)` | 按名称查询输出的元素类型和布局。 |
| `suspend getInputBufferRequirements(inputName)` | 查询输入缓冲区需求。 |
| `suspend getOutputBufferRequirements(outputName)` | 查询输出缓冲区需求。 |
| `suspend getInputBuffers(signatureIndex = 0)` | 创建指定签名的输入缓冲区列表。 |
| `suspend getOutputBuffers(signatureIndex = 0)` | 创建指定签名的输出缓冲区列表。 |
| `suspend run(inputs, outputs, signatureIndex = 0)` | 使用指定的输入、输出缓冲区执行推理。 |
| `suspend close()` | 释放编译器资源。关闭后不再使用实例。 |

`filePath` 在本地目标上是模型文件路径，在 Web 上通常是可访问的模型 URL。名称、顺序及签名由模型决定；[平台能力](platforms.md)列出了签名行为差异。

## TFBuffer

`TFBuffer` 扩展 `AutoCloseable`。写入是普通函数，读取是挂起函数：

| 写入 | 读取 | Kotlin 数组 |
|---|---|---|
| `writeInt` | `readInt` | `IntArray` |
| `writeFloat` | `readFloat` | `FloatArray` |
| `writeInt8` | `readInt8` | `ByteArray` |
| `writeBoolean` | `readBoolean` | `BooleanArray` |
| `writeLong` | `readLong` | `LongArray` |

调用方必须确保类型和长度符合模型。资源所有权见[生命周期](guide/lifecycle.md#cleanup)。

## 类型与枚举

| 类型 | 说明 |
|---|---|
| `LiteRTAccelerator` | `CPU`、`GPU`、`NPU`；实际能力依平台而定。 |
| `LiteRTTensorType` | `elementType` 和可为空的 `layout`。 |
| `LiteRTElementType` | LiteRT 元素类型及其原生值 `value`；`fromNative(value)` 在未知值时抛出异常。枚举不代表所有类型都有 `TFBuffer` 读写方法。 |
| `LiteRTLayout` | `dimensions`、`strides`、`rank`、`hasStrides`；`calculateDefaultStrides` 计算行主序元素步长。 |
| `LiteRTBufferRequirements` | `supportedTypes`、字节数 `bufferSize` 和 `strides`；步长单位注意事项见[张量指南](guide/tensors.md#requirements)。 |
| `LiteRTTensorBufferType` | 主机内存及各类平台缓冲区类型，`type` 是原生值；`from(type)` 用于转换。 |

更多示例见[快速开始](getting-started.md)，可选预处理 API 见[图像与音频处理流程](dataprocess.md)，Web 专用初始化入口见 [Web 初始化 API](api/web.md)。
