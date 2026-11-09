# KMP LiteRT

A Kotlin Multiplatform library for running Google [LiteRT](https://ai.google.dev/edge/litert) models across Android, iOS, JVM, JS, WasmJS, Windows, Linux, and macOS with a unified, idiomatic Kotlin API.

### Core Module Platform Support (LiteRT)  

KMPLiteRT Platform Runtime Dependencies    

| Platform | LiteRT Official Dependency | dependencies |
|---|---| --- |
| Android | LiteRT CompiledModel [Kotlin API](https://developers.google.com/edge/litert/next/android_kotlin) | `"com.google.ai.edge.litert:litert:2.3.0"` |
| JVM | LiteRT CompiledModel [C++ Binary](https://developers.google.com/edge/litert/next/cpp_sdk)  | JNA (Java Native Access)|
| JS、 WasmJS| LiteRT for Web with [LiteRT.js](https://developers.google.com/edge/litert/web)    | `@litertjs/core 2.6.0`|
| Windows、Linux、iOS、macOS | LiteRT CompiledModel [C++ API](https://developers.google.com/edge/litert/next/cpp)| Kotlin/Native C interop|  
  
KMPLiteRT Hardware Acceleration Support  
  
| Platform | CPU | GPU | NPU |
|---|---|---|---|
| Android | ✅ XNNPACK / CPU backend | ✅ OpenCL / OpenGL | ⚠️ Vendor-specific NPU delegates |
| iOS  | ✅ XNNPACK | ✅ Metal | ⚠️ Apple Neural Engine (ANE) |
| JVM  | ✅ XNNPACK via JNI / FFM | ⚠️ OpenCL / Vulkan, environment-dependent | ⚠️ Vendor-specific native delegates |
| JS | ✅ WebAssembly | ✅ WebGPU | ⚠️ WebNN, browser-dependent |
| WasmJS | ✅ WebAssembly | ✅ WebGPU | ⚠️ WebNN, browser-dependent |
| Windows | ✅ XNNPACK | ⚠️ WebGPU / native GPU backends | ⚠️ Hardware-specific |
| Linux  | ✅ XNNPACK | ⚠️ WebGPU / OpenCL / Vulkan | ⚠️ Hardware-specific |
| macOS  | ✅ XNNPACK | ✅ Metal / WebGPU | ⚠️ Apple Neural Engine (ANE) |  
  
## Modules

| Module | Responsibility |
|---|---|
| `core` | Core LiteRT runtime integration, model management, tensor handling, and inference execution. |
| `dataproces` | 	Input preprocessing and output postprocessing utilities for model inference, including image and audio processing. |

## Quick start

To use **KMPLiteRT** in your Kotlin Multiplatform application, add the required modules to your `commonMain` source set.

### Add Dependencies

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            val kmplitert = "0.3.0"
            implementation("io.github.leitingzi:kmplitert-core:$kmplitert")
        }
    }
}
```

Using the Version Directory

```toml
[versions]
kmplitert = "0.3.0"

[libraries]
kmplitert-core = { module = "io.github.leitingzi:kmplitert-core", version.ref = "kmplitert" }
kmplitert-dataproces = { module = "io.github.leitingzi:kmplitert-core", version.ref = "kmplitert" }
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kmplitert.core)
        }
    }
}
```

### Instructions Use

The following example demonstrates how to execute a `CelsiusToFahrenheit.tflite` model using KMPLiteRT.

```kotlin
suspend fun runInference(celsius: Float): Float {
    val compiler = LiteRTCompiler("CelsiusToFahrenheit.tflite", LiteRTAccelerator.CPU)
    compiler.init()

    val input = compiler.getInputBuffers()
    val output = compiler.getOutputBuffers()

    input[0].writeFloat(floatArrayOf(celsius))
    compiler.run(input, output)

    val result = output[0].readFloat()[0]
    compiler.close()
    return result
}
```

The typical inference workflow consists of the following steps:   
1. load the model `LiteRTCompiler`.
2. initialize the runtime `init()`.
3. retrieve the input and output tensors `getInputBuffers(), getOutputBuffers()`.
4. populate the input tensors with data `writeFloat()`.
5. execute inference `run()`.
6. read the results from the output tensors `readFloat()`
7. release the model resources `close()`.

## API Introduction

In most cases, the `LiteRTCompiler`, `LiteRTAccelerator`, and `TFBuffer` APIs are sufficient for typical development needs. Since model input and output tensor shapes and types are generally fixed, development primarily focuses on data preprocessing and postprocessing. For more advanced use cases, the `core` module also provides access to model metadata and additional low-level capabilities.

## License

KMPLiteRT is distributed under the [Apache License 2.0](LICENSE).
