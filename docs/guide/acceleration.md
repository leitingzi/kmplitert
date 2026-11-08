# Hardware Acceleration

Request `LiteRTAccelerator.CPU`, `GPU`, or `NPU` when creating a compiler. CPU is the default.

```kotlin
val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
```

These are requests, not guarantees that the device, model operators, runtime, plugins, or browser support the hardware. Establish a CPU correctness baseline first, then test performance and fallback on the target device.

| Implementation | Current behavior and considerations |
|---|---|
| Android | Passes the selection to Android LiteRT; support depends on runtime and device. |
| JVM | Maps to LiteRT hardware options; the native loader logs a CPU fallback warning when the GPU acceleration plugin is unavailable. |
| Native | Maps to LiteRT C API hardware options; plugins, dynamic libraries, and device capabilities must match. |
| JS / WasmJS | CPU uses WASM; GPU requests select WASM when WebGPU is unavailable; NPU requests use WebNN options. Verify WebNN on the target browser. |

Successful initialization does not necessarily mean the entire model runs on the requested hardware. For performance-sensitive applications, measure initialization, data transfer, and repeated inference separately, and inspect runtime logs.
