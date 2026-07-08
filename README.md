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
- 🖼️ **Image Preprocessing**: Built-in utilities for image resizing and format conversion.

---

## 💻 Platform Support & Acceleration Matrix

| Platform | Status | Implementation | Hardware Acceleration |
| :--- | :---: | :--- | :--- |
| **Android** | ⚠️ Alpha | [LiteRT Android SDK](https://github.com/google-ai-edge/litert) | CPU / GPU / NNAPI |
| **JVM (Desktop)** | ⚠️ Alpha | LiteRT C API via JNA | CPU |
| **Web (JS/Wasm)** | ⚠️ Alpha | [@litertjs/core](https://www.npmjs.com/package/@litertjs/core) | Browser / WebGL / WebGPU |
| **Native (Windows)** | 🚧 Untested | LiteRT C API | CPU / WebGPU Accelerator |
| **Native (Linux)** | 🚧 Untested | LiteRT C API | CPU / WebGPU Accelerator |
| **Native (macOS)** | 🚧 Untested | LiteRT C API | CPU / Metal Accelerator |
| **iOS** | ❌ Unsupported | Placeholder only | Metal Accelerator (Planned) |

---

## 🛠️ Supported Data Types

KMPLiteRT provides type-safe buffers for the following Kotlin types:
- `FloatArray` (Float32)
- `IntArray` (Int32)
- `LongArray` (Int64)
- `ByteArray` (Int8 / UInt8)
- `BooleanArray` (Bool)

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

## 💡 Usage Examples

### 1. Basic Vector Inference
Suitable for regression, classification, or any model processing simple numerical vectors.

```kotlin
import io.github.leitingzi.kmplitert.core.*

suspend fun runBasicInference(modelPath: String) {
    // 1. Initialize the compiler
    val compiler = LiteRTCompiler(
        filePath = modelPath, 
        accelerator = LiteRTAccelerator.CPU
    )
    
    try {
        compiler.init()

        // 2. Get typed input and output buffers
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 3. Write input data (e.g., Float array)
        inputs[0].writeFloat(floatArrayOf(1.0f, 2.0f, 3.0f))

        // 4. Execute inference
        compiler.run(inputs, outputs)

        // 5. Read the result
        val result = outputs[0].readFloat()
        println("Inference result: ${result.contentToString()}")
        
    } finally {
        // 6. Release resources
        compiler.close()
    }
}
```

### 2. Image Classification
For computer vision models, use `LiteRtImage` for seamless preprocessing (resizing and format conversion).

```kotlin
import io.github.leitingzi.kmplitert.core.*

suspend fun classifyImage(modelPath: String, rawImageBytes: ByteArray) {
    val compiler = LiteRTCompiler(filePath = modelPath)
    
    try {
        compiler.init()
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 1. Image preprocessing: Load -> Resize -> Convert to model format (Int8/Float)
        val inputData = LiteRtImage.fromBytes(rawImageBytes)
            .resize(224, 224)
            .toInt8Array() // Use toFloatArray() if required by the model

        // 2. Load data into input buffer
        inputs[0].writeInt8(inputData)

        // 3. Execute inference
        compiler.run(inputs, outputs)

        // 4. Read and process output (e.g., finding the max probability)
        val result = outputs[0].readInt8()
        val maxIndex = result.indices.maxByOrNull { result[it] }
        println("Identified class index: $maxIndex")
        
    } finally {
        compiler.close()
    }
}
```

---

## ⚠️ Current Limitations & Known Issues

- **General Status**: This library is in its early stages. Most platforms have not undergone full validation.
- **Web (JS/WasmJS)**:
    - **Adaptive Models**: No support for models with dynamic/adaptive shapes yet.
    - **Environment**: Requires a browser environment with WebGL/WebGPU support for the LiteRT JS runtime.
- **Native Platforms**:
    - Includes support for **Windows (mingwX64)**, **Linux (linuxX64)**, and **macOS (macosArm64)**.
    - **Status**: These platforms are currently **NOT TESTED**.
- **iOS**: Implementation is currently a placeholder.
- **JVM**: Primarily tested on Windows; Linux and macOS versions are less stable.

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
