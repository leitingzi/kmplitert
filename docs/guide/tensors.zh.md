# 张量与缓冲区

模型的张量描述了数据是什么；`TFBuffer` 是读写实际数据的接口。二者必须匹配。

## 元素类型与读写

| 模型元素类型 | Kotlin 写入 | Kotlin 读取 |
|---|---|---|
| `FLOAT` | `writeFloat(FloatArray)` | `readFloat(): FloatArray` |
| `INT` | `writeInt(IntArray)` | `readInt(): IntArray` |
| `INT8` | `writeInt8(ByteArray)` | `readInt8(): ByteArray` |
| `BOOLEAN` | `writeBoolean(BooleanArray)` | `readBoolean(): BooleanArray` |
| `INT64` | `writeLong(LongArray)` | `readLong(): LongArray` |

`LiteRTElementType` 还包含其他可描述的类型，但 `TFBuffer` 没有为所有枚举值提供专门的读写方法。平台对模型数据类型的支持也可能不同；不要仅凭枚举成员判断推理可用。

对于多个输入输出，按模型定义的顺序使用从对应签名获取的缓冲区。写入长度和元素类型必须匹配模型；对于量化模型，还要依据模型的量化参数处理数据。

## 形状与布局

`LiteRTTensorType.elementType` 描述元素类型，`layout` 可能为 `null`。`LiteRTLayout.dimensions` 是各维长度；`rank` 是维度数。`strides` 为空时表示没有显式步长；非空时长度必须与维度数一致。

`LiteRTLayout.calculateDefaultStrides(listOf(1, 2, 3))` 返回按行主序计算的元素步长 `[6, 3, 1]`。它不是自动乘以元素字节数的字节偏移。

## 缓冲区需求 {#requirements}

通过输入或输出名称查询 `LiteRTBufferRequirements`：

- `supportedTypes`：运行时报告的缓冲区类型，例如主机内存或 GPU 缓冲区类型。
- `bufferSize`：所需大小，单位为字节。
- `strides`：运行时或平台实现返回的步长。

!!! warning "步长单位"
    公共类型的注释将 requirements 步长描述为字节，而部分实现会回填上述按元素计算的默认步长。跨平台字节偏移的语义尚需进一步核实；不要未经验证直接把它用于手动计算字节地址。

## 读取时机

先写入输入，再运行模型，最后读取输出。Web 实现的输入张量在 `write*()` 时创建，输出在 `run()` 后填充；提前读取尚未初始化的缓冲区会报错。
