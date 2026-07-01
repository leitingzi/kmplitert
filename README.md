# KMPLiteRT 🚀

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.leitingzi/kmplitert-core)](https://central.sonatype.com/artifact/io.github.leitingzi/kmplitert-core)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20JVM%20%7C%20Web%20%7C%20Native-orange.svg)](#-platform-support)

**KMPLiteRT** brings the power of [Google LiteRT](https://ai.google.dev/edge/litert) (formerly TensorFlow Lite) to the Kotlin Multiplatform ecosystem. It provides a unified, type-safe API to run machine learning inference across mobile, desktop, and web platforms.

> [!CAUTION]
> **UNDER ACTIVE DEVELOPMENT**
> This project is currently in early development (Alpha). APIs are unstable and subject to change.
> Many platforms are not yet tested or validated. **NOT RECOMMENDED FOR PRODUCTION USE.**

---

## ✨ Features

- 🏗️ **Unified API**: Write your inference logic once in `commonMain` and run it everywhere.
- ⚡ **Coroutine Support**: First-class support for asynchronous initialization and inference.
- 🔒 **Type-Safe Tensors**: Direct and safe access to `Float`, `Int`, `Long`, `Boolean`, and `Byte` buffers.
- 🚀 **Hardware Acceleration**: Support for CPU, GPU, and NPU where available on the platform.

---

## 💻 Platform Support

| Platform | Status | Implementation | Hardware Acceleration |
| :--- | :---: | :--- | :--- |
| **Android** | ⚠️ Alpha | [LiteRT Android SDK](https://github.com/google-ai-edge/litert) | CPU / GPU / NNAPI |
| **JVM (Desktop)** | ⚠️ Alpha | LiteRT C API via JNA | CPU |
| **Web (JS/Wasm)** | ⚠️ Alpha | [@litertjs/core](https://www.npmjs.com/package/@litertjs/core) | Browser / WebGL |
| **Native (Windows/Linux)** | 🚧 Untested | LiteRT C API | CPU |
| **Native (macOS)** | 🚧 Untested | LiteRT C API | CPU |
| **iOS** | ❌ Unsupported | Placeholder only | - |

---

## ⚠️ Current Limitations & Known Issues

- **General Status**: This library is in its early stages. Most platforms have not undergone full validation.
- **Web (JS/WasmJS)**:
    - **Adaptive Models**: No support for models with adaptive shapes yet.
    - **Environment**: Requires a browser environment with WebGL support for the LiteRT JS runtime.
- **Native Platforms**:
    - Includes support for **Windows (mingwX64)**, **Linux (linuxX64)**, and **macOS (macosArm64)**.
    - **Status**: These platforms are currently **NOT TESTED** and may not work as expected.
- **iOS**: Implementation is currently a placeholder and does not function. Full support is planned for future releases.
- **JVM**: Primarily tested on Windows; Linux and macOS versions are less stable.

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

Running a model in your common code is straightforward:

```kotlin
import io.github.leitingzi.kmplitert.core.*

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

## 🤝 Contributing

Contributions are welcome! If you encounter issues or have ideas for improvements, please:
- Open an **Issue** to report bugs or suggest features.
- Submit a **Pull Request** with your enhancements.
- Share your feedback to help us stabilize the library.

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
