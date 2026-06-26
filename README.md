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