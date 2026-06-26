# KMPLiteRT

> Run LiteRT (TensorFlow Lite) models with a unified Kotlin Multiplatform API.

KMPLiteRT is a Kotlin Multiplatform library for running LiteRT models on Android, iOS, JVM, JavaScript, and WasmJS using a single, consistent API.

## Features

- 🚀 Kotlin Multiplatform
- 📱 Android / iOS / JVM / JavaScript / WasmJS
- 📦 Type-safe tensor APIs
- ⚡ Coroutine-friendly
- 🔒 Unified `expect/actual` implementation

## Platform Support

| Platform | Support |
|----------|:-------:|
| Android | ✅ |
| iOS | ✅ |
| JVM | ✅ |
| JavaScript | ✅ |
| WasmJS | ✅ |

## Installation

```kotlin
commonMain.dependencies {
    // The library is currently under development.
    implementation("")
}
```

## Usage

```kotlin
suspend fun inference() {
    val compiler = LiteRTCompiler("model.tflite")

    compiler.init()

    val inputs = compiler.getInputBuffers()
    val outputs = compiler.getOutputBuffers()

    inputs[0].writeFloat(
        floatArrayOf(
            // input data
        )
    )

    compiler.run(inputs, outputs)

    val result = outputs[0].readFloat()

    println(result.joinToString())

    compiler.close()
}
```

Inference follows a simple workflow:

1. Create a `LiteRTCompiler`
2. Call `init()`
3. Obtain input and output buffers
4. Write input tensor data
5. Execute `run()`
6. Read output tensor data
7. Call `close()` to release resources

## License

Apache License 2.0
```
Copyright [2026] [leitingzi]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```