# KMPLiteRT 🚀

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.leitingzi/kmplitert-core)](https://central.sonatype.com/artifact/io.github.leitingzi/kmplitert-core)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20JVM%20%7C%20Web-orange.svg)](#-platform-support)

**KMPLiteRT** brings the power of [Google LiteRT](https://ai.google.dev/edge/litert) (formerly TensorFlow Lite) to the Kotlin Multiplatform ecosystem. It provides a unified, type-safe API to run machine learning inference across mobile, desktop, and web platforms.

> [!WARNING]
> This project is currently in early development. APIs are subject to change.
> Not recommended for use.

---

## ✨ Features

- 🏗️ **Unified API**: Write your inference logic once in `commonMain`.
- ⚡ **Coroutine Support**: Asynchronous initialization and inference for smooth UI performance.
- 🔒 **Type-Safe Tensors**: Direct access to `Float`, `Int`, `Long`, `Boolean`, and `Byte` buffers.
- 🚀 **Hardware Acceleration**: Support for CPU, GPU, and NPU (platform dependent).

---

## 💻 Platform Support

| Platform | Status | Tested On | Hardware Acceleration | Backend |
| :--- | :---: | :--- | :--- | :--- |
| **Android** | ⚠️ Alpha | Device/Emulator | CPU / GPU / NNAPI | [LiteRT Android SDK](https://github.com/google-ai-edge/litert) |
| **JVM (Desktop)** | ⚠️ Alpha | **Windows Only** | CPU | LiteRT C API via JNA |
| **Web (JS/Wasm)** | ⚠️ Alpha | Chrome | Browser / WebGL | [@litertjs/core](https://www.npmjs.com/package/@litertjs/core) |
| **iOS** | ❌ Not Supported | - | *Planned* | - |

---

## 📦 Installation

Add the dependency to your `commonMain` source set in `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.leitingzi:kmplitert-core:0.1.1")
        }
    }
}
```

---

## 💡 Usage Example

Here is a quick look at how to run a model in your common code:

```kotlin
import io.github.leitingzi.kmplitert.core.*
import kotlinx.coroutines.test.runTest

suspend fun runInference(modelPath: String) {
    // 1. Initialize the compiler with the model and accelerator
    val compiler = LiteRTCompiler(
        filePath = modelPath, 
        accelerator = LiteRTAccelerator.CPU
    )
    
    try {
        compiler.init()

        // 2. Prepare typed input and output buffers
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 3. Write data to input buffer
        inputs[0].writeFloat(floatArrayOf(100f))

        // 4. Run inference
        compiler.run(inputs, outputs)

        // 5. Read the results
        val result = outputs[0].readFloat()
        println("Result: ${result.contentToString()}")
        
    } finally {
        // 6. Release resources
        compiler.close()
    }
}
```

---

## ⚠️ Current Limitations

- **JVM (Desktop)**: Currently only tested and verified on **Windows**. Support for Linux and macOS is present in the source but not yet fully validated.
- **Web (JS/WasmJS)**: 
    - The implementation is currently unstable.
    - Requires a browser environment with WebGL support for the LiteRT JS runtime.
    - Unable to run models with adaptive shapes.
- **iOS**: Implementation is currently a placeholder and not functional.

---

## 🤝 Community

Contributions of all kinds are welcome! Whether you'd like to:

- 🐛 Report bugs
- 💡 Suggest new features
- 📖 Improve documentation
- 🚀 Submit pull requests
- ⭐ Star the project

Please feel free to get involved!

---

## 📄 License

```text
Copyright 2026 leitingzi (yebintang)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---
<p align="center">Made with ❤️ for the Kotlin Multiplatform community.</p>
