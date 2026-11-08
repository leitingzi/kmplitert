# Core API

The public API is in `io.github.kmplitert.core`. These are the interfaces commonly used from shared code; platform-level bindings should not be treated as stable cross-platform abstractions.

## LiteRTCompiler

```kotlin
LiteRTCompiler(
    filePath: String,
    accelerator: LiteRTAccelerator = LiteRTAccelerator.CPU
)
```

| Method | Purpose |
|---|---|
| `suspend init()` | Load and prepare the model. Call before other operations. |
| `suspend getInputTensorType(inputName)` | Query an input's element type and layout by name. |
| `suspend getOutputTensorType(outputName)` | Query an output's element type and layout by name. |
| `suspend getInputBufferRequirements(inputName)` | Query input buffer requirements. |
| `suspend getOutputBufferRequirements(outputName)` | Query output buffer requirements. |
| `suspend getInputBuffers(signatureIndex = 0)` | Create the signature's input buffer list. |
| `suspend getOutputBuffers(signatureIndex = 0)` | Create the signature's output buffer list. |
| `suspend run(inputs, outputs, signatureIndex = 0)` | Run inference with the supplied buffers. |
| `suspend close()` | Release compiler resources. Do not reuse the instance afterward. |

`filePath` is a model file path on local targets and typically an accessible model URL on the web. The model defines names, ordering, and signatures; see [platform capabilities](platforms.md) for signature differences.

## TFBuffer

`TFBuffer` extends `AutoCloseable`. Writes are regular functions; reads are suspending:

| Write | Read | Kotlin array |
|---|---|---|
| `writeInt` | `readInt` | `IntArray` |
| `writeFloat` | `readFloat` | `FloatArray` |
| `writeInt8` | `readInt8` | `ByteArray` |
| `writeBoolean` | `readBoolean` | `BooleanArray` |
| `writeLong` | `readLong` | `LongArray` |

The caller must ensure the type and length match the model. See [lifecycle](guide/lifecycle.md#cleanup) for resource ownership.

## Types and enums

| Type | Description |
|---|---|
| `LiteRTAccelerator` | `CPU`, `GPU`, `NPU`; actual capability depends on the platform. |
| `LiteRTTensorType` | `elementType` and nullable `layout`. |
| `LiteRTElementType` | LiteRT element types and native `value`; `fromNative(value)` throws for unknown values. Not all enum values have `TFBuffer` read/write methods. |
| `LiteRTLayout` | `dimensions`, `strides`, `rank`, `hasStrides`; `calculateDefaultStrides` computes row-major element strides. |
| `LiteRTBufferRequirements` | `supportedTypes`, `bufferSize` in bytes, and `strides`; see [stride considerations](guide/tensors.md#requirements). |
| `LiteRTTensorBufferType` | Host and platform buffer types; `type` is the native value and `from(type)` converts it. |

See the [quick start](getting-started.md) for examples and the [Web initialization API](api/web.md) for web-specific setup.
