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

| Platform | CI runner | CI coverage | Acceleration coverage | Current result |
|---|---|---|---|---|
| Linux x64 | `ubuntu-latest` | JVM tests, `linuxX64Test`, and Linux ARM64 cross-compilation | CPU; no hosted GPU/NPU assumed | Previous JVM/Linux x64 tests passed; the added ARM64 cross-compilation task is **pending verification** |
| Linux ARM64 | `ubuntu-24.04-arm` | JVM tests against the ARM64 LiteRT library; native ARM64 compilation runs on Linux x64 | CPU; no hosted GPU/NPU assumed | **Pending rerun** after replacing the unavailable `linuxArm64Test` task |
| macOS | `macos-latest` | JVM CPU tests and `macosArm64Test` | Metal GPU test runs in the macOS native test step | **Pending rerun** after isolating JVM CPU tests from native GPU tests |
| iOS Simulator | `macos-latest` | `iosSimulatorArm64Test` | CPU; Metal GPU inference is disabled in the simulator | **Pending rerun**; simulator GPU previously crashed in native code |
| Windows | `windows-latest` | JVM tests and `mingwX64Test` | CPU; hosted Windows GPU is not assumed | **Pending rerun** with GPU tests disabled |
| Android Emulator | `ubuntu-latest` + x86_64 emulator (API 35) | `connectedAndroidDeviceTest` | CPU; GPU/NPU instrumentation tests are disabled | **Pending rerun** after adding KVM setup for the ADB-offline startup issue |
| JavaScript | `ubuntu-latest` + Firefox | `jsBrowserTest` | WebGPU/WebNN only when available in the browser | **Not yet confirmed** in the reported runs |
| WasmJS | `ubuntu-latest` + Firefox | `wasmJsBrowserTest` | WebGPU/WebNN only when available in the browser | **Not yet confirmed** in the reported runs |

Accelerator inference tests use the same test model and output assertions as CPU tests. Android GPU/NPU tests can be enabled with `-Pandroid.testInstrumentationRunnerArguments.testGpu=true` and/or `-Pandroid.testInstrumentationRunnerArguments.testNpu=true`; JVM and Native tests use `KMPLITERT_TEST_GPU=true` and/or `KMPLITERT_TEST_NPU=true`. Enabled tests fail if initialization or inference fails. The statuses above reflect the latest results shared during CI troubleshooting; changes marked pending or unconfirmed still need a new GitHub Actions run.

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
