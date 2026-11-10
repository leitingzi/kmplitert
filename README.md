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
  
### KMPLiteRT Hardware Acceleration Support

| Platform | CPU | GPU | NPU |
|---|:---:|:---:|:---:|
| Android | ✓ | ✓* | ✓* |
| Linux x64 (JVM / Native) | ✓ | ✓* | ✓* |
| Linux ARM64 (JVM / Native) | ✓ | ✓* | ✗ |
| Windows x64 (JVM / Native) | ✓ | ✓* | ✗ |
| macOS ARM64 (JVM / Native) | ✓ | ✓* | ✗ |
| iOS ARM64 device | ✓ | ✓* | ✗ |
| iOS Simulator ARM64 | ✓ | ✗ | ✗ |
| JavaScript | ✓ | ✓* | ✓* |
| WasmJS | ✓ | ✓* | ✓* |

`✓*` means the backend is integrated, but actual inference requires a compatible device, driver, runtime, or browser API. `✗` means the platform/backend combination is not supported or is disabled in the current CI configuration. GPU inference is disabled on iOS Simulator in CI because the native Metal path has crashed there.

CI validates CPU inference on the hosted runners. GPU/NPU inference is enabled only on platforms where the runner/runtime can provide the required accelerator. Browser GPU/NPU tests are conditional on WebGPU/WebNN availability; Android GPU/NPU instrumentation tests require explicitly enabling their runner arguments.

## Modules

| Module | Responsibility |
|---|---|
| `core` | Core LiteRT runtime integration, model management, tensor handling, and inference execution. |
| `file` | Resolves bundled model resource URIs to paths that LiteRT can load on each platform. |
| `dataproces` | Input preprocessing and output postprocessing utilities for model inference, including image and audio processing. |

## Quick start

To use **KMPLiteRT** in your Kotlin Multiplatform application, add the required modules to your `commonMain` source set.

### Add Dependencies

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            val kmplitert = "0.3.0"
            implementation("io.github.leitingzi:kmplitert-core:$kmplitert")
            implementation("io.github.leitingzi:kmplitert-file:$kmplitert")
            implementation("io.github.leitingzi:kmplitert-dataproces:$kmplitert")
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
kmplitert-file = { module = "io.github.leitingzi:kmplitert-file", version.ref = "kmplitert" }
kmplitert-dataproces = { module = "io.github.leitingzi:kmplitert-core", version.ref = "kmplitert" }
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kmplitert.core)
            implementation(libs.kmplitert.file)
            implementation(libs.kmplitert.dataproces)
        }
    }
}
```

### Instructions Use

The following example demonstrates how to execute a `CelsiusToFahrenheit.tflite` model using KMPLiteRT.

Place the model in the file module's `commonMain/resources` directory and resolve
it with `LiteRTFile.fromCommonRes` before passing it to `LiteRTCompiler`. File
system platforms return a filesystem path; browser targets return a relative
resource URL.

For Compose Multiplatform resources, pass `Res.getUri(...)` to
`LiteRTFile.fromComposeUri(...)`. Android applications must initialize the file
module once in `Application.onCreate` with `LiteRTFile.init(applicationContext)`;
import the Android-only extension from `io.github.kmplitert.file.init`. The
Android implementation copies asset-backed resources into the app cache.

```kotlin
@Composable
fun ModelScreen() {
    LaunchedEffect(Unit) {
        val modelPath = LiteRTFile.fromCommonRes("CelsiusToFahrenheit.tflite")
        val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
        compiler.init()
        // Run inference, then close the compiler when it is no longer needed.
    }
}
```

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

### Core 

In most cases, the `LiteRTCompiler`, `LiteRTAccelerator`, and `TFBuffer` APIs are sufficient for typical development needs. Since model input and output tensor shapes and types are generally fixed, development primarily focuses on data preprocessing and postprocessing. For more advanced use cases, the `core` module also provides access to model metadata and additional low-level capabilities.

- `LiteRTCompiler` loads a model, exposes its input/output buffers, and runs inference. Call `init()` before use and `close()` when finished.
- `LiteRTAccelerator` selects `CPU`, `GPU`, or `NPU` where supported.
- `TFBuffer` reads and writes typed tensor data, such as `writeFloat()` and `readFloat()`. Use `getInputTensorType()` and `getInputBufferRequirements()` when you need tensor metadata or buffer layout details.

### File

- `LiteRTFile.fromCommonRes("model.tflite")` resolves a model bundled in `commonMain/resources`.
- `LiteRTFile.fromComposeUri(Res.getUri(...))` resolves a Compose Multiplatform resource URI. On Android, call `LiteRTFile.init(context)` first; this is an Android-only extension.
- `LiteRTFile.createFileFromByteArray(bytes, "model.tflite")` writes model bytes to a file and returns its path. Browser targets return a Blob URL instead.

### DataProcess

- `LiteRtImage.fromBytes(bytes)` decodes encoded image data (supported formats vary by platform); `fromRawRgb(data, width, height)` creates an image from interleaved RGB pixels. Common operations include `resize()`, `crop()`, `rotate()`, `flip()`, `toGrayscale()`, and `toRgb()`.
- Convert image data for inference with `toFloatArray()` or write it directly to a tensor using `writeFloatBuffer()` or `writeInt8Buffer()`.
- `LiteRtAudio.fromWav(bytes)` decodes WAV data. Use `toMono()` and `resample(targetSampleRate)` to match the model's expected audio input.
- `SignalProcessing` provides common audio features such as `fft()`, window functions, and `melSpectrogram()`.

## License

KMPLiteRT is distributed under the [Apache License 2.0](LICENSE).
