# Tensors and Buffers

A model tensor describes the data; `TFBuffer` reads and writes the actual data. They must match.

## Element types and operations

| Model element type | Kotlin write | Kotlin read |
|---|---|---|
| `FLOAT` | `writeFloat(FloatArray)` | `readFloat(): FloatArray` |
| `INT` | `writeInt(IntArray)` | `readInt(): IntArray` |
| `INT8` | `writeInt8(ByteArray)` | `readInt8(): ByteArray` |
| `BOOLEAN` | `writeBoolean(BooleanArray)` | `readBoolean(): BooleanArray` |
| `INT64` | `writeLong(LongArray)` | `readLong(): LongArray` |

`LiteRTElementType` describes additional types, but `TFBuffer` does not provide dedicated read/write methods for every enum value. Platform model-type support can differ; an enum value alone does not guarantee inference support.

For multiple inputs or outputs, use buffers for the appropriate signature in the order defined by the model. Written lengths and element types must match; quantized models also require the model's quantization parameters.

## Shape and layout

`LiteRTTensorType.elementType` describes the element type; `layout` may be `null`. `LiteRTLayout.dimensions` contains dimension sizes and `rank` their count. An empty `strides` list means there are no explicit strides; otherwise its length must match the dimensions.

`LiteRTLayout.calculateDefaultStrides(listOf(1, 2, 3))` returns row-major element strides `[6, 3, 1]`, not byte offsets multiplied by element size.

## Buffer requirements {#requirements}

Query `LiteRTBufferRequirements` by input or output name:

- `supportedTypes`: runtime-reported buffer types, such as host memory or GPU buffers.
- `bufferSize`: required size in bytes.
- `strides`: strides returned by the runtime or platform implementation.

!!! warning "Stride units"
    The shared type's documentation describes requirements strides in bytes, but some implementations fill in the element-based default strides above. Cross-platform byte-offset semantics need further verification; do not use them to calculate byte addresses without validating them.

## When to read

Write inputs, run the model, then read outputs. Web implementations create input tensors on `write*()` and populate outputs after `run()`; reading an uninitialized buffer fails.
