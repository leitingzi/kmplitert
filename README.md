# KMPLiteRT 🚀

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.leitingzi/kmplitert-core)](https://central.sonatype.com/artifact/io.github.leitingzi/kmplitert-core)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20JVM%20%7C%20Web%20%7C%20Native-orange.svg)](#-platform-support)

**KMPLiteRT** brings the power of [Google LiteRT](https://ai.google.dev/edge/litert) (formerly TensorFlow Lite) to the Kotlin Multiplatform ecosystem. It provides a unified, type-safe API to run machine learning inference across mobile, desktop, and web platforms.

> [!IMPORTANT]
> **Beta Release**
> This project has reached a stable milestone with unified support across all platforms. API stability is now a priority, and resource management is robustly handled.

---

## ✨ Features

- 🏗️ **Unified API**: Write your inference logic once in `commonMain` and run it everywhere.
- ⚡ **Coroutine Support**: First-class support for asynchronous initialization and inference.
- 📊 **Metadata Inspection**: Query tensor types, shapes (layout), and buffer requirements at runtime.
- 🚀 **Hardware Acceleration**: Support for CPU, GPU, and NPU (including WebGPU and WebNN).
- 🔒 **Type-Safe Tensors**: Direct and safe access to `Float`, `Int`, `Long`, `Boolean`, and `Byte` buffers via `TFBuffer`.
- 🔄 **Multi-Signature Support**: Seamlessly work with models containing multiple computation graphs.
- 🏎️ **Optimized Data Transfer**: Platform-specific optimizations (`memcpy`, `TypedArray.set`) for high-performance inference.
- 🖼️ **Image Preprocessing**: Built-in utilities for image resizing and format conversion.

---

## 🏛️ Architecture: `kmplitert-core`

`kmplitert-core` is the foundational module of the project. It abstracts the native LiteRT runtimes into a clean, idiomatic Kotlin API.

- **Platform Abstraction**: Uses Kotlin `expect/actual` to wrap JNI (Android), C-API via JNA (JVM), C-Interop (Native), and JS/Wasm JS wrappers.
- **Efficient Buffers**: The `TFBuffer` API manages memory efficiently, utilizing `memcpy` on Native and `TypedArray` optimizations on Web to minimize overhead.
- **Consistent Lifecycle**: Provides a predictable `init()` -> `run()` -> `close()` lifecycle with guaranteed resource cleanup.
- **API documentation**: [dokka documentation](https://leitingzi.github.io/kmplitert/)

---

## 💻 Platform Support & Acceleration Matrix

| Platform | Status | Implementation | Hardware Acceleration |
| :--- | :---: | :--- | :--- |
| **Android** | ✅ Beta | [LiteRT Android SDK](https://github.com/google-ai-edge/litert) | CPU / GPU / NPU |
| **JVM (Desktop)** | ✅ Beta | LiteRT C API via JNA | CPU / GPU (WebGPU) |
| **Web (JS/Wasm)** | ✅ Beta | [@litertjs/core](https://www.npmjs.com/package/@litertjs/core) | Browser / WebGL / WebGPU / WebNN |
| **Native (Win/Linux/Mac)** | ✅ Beta | LiteRT C API | CPU / GPU |
| **iOS** | ✅ Beta | LiteRT C API | CPU / GPU (Metal) |

---

## 📦 Installation

Add the dependency to your `commonMain` source set in `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.leitingzi:kmplitert-core:0.1.3")
        }
    }
}
```

---

## 💡 Usage Examples

### 1. Platform Initialization (Android Only)
Before using `LiteRTFileUtils` on Android (e.g., to load models from assets), you must initialize it with a `Context`.

```kotlin
// In your Android Activity or Application
LiteRTFileUtils.init(applicationContext)
```

### 2. Loading Model from ByteArray
Useful for loading models from KMP resources or remote downloads.

```kotlin
val modelBytes: ByteArray = Res.readBytes("files/model.tflite")// ... load model bytes
val modelPath = LiteRTFileUtils.createFileFromByteArray(modelBytes, "model.tflite")

// Now use modelPath with LiteRTCompiler
```

### 3. Basic Vector Inference with Metadata Inspection
Suitable for regression, classification, or any model processing simple numerical vectors.

```kotlin
import io.github.leitingzi.kmplitert.core.*

suspend fun runInference(modelPath: String) {
    // 1. Instantiate the compiler with a specific accelerator
    val compiler = LiteRTCompiler(
        filePath = modelPath, 
        accelerator = LiteRTAccelerator.CPU
    )
    
    try {
        // 2. Initialize (load model and prepare environment)
        compiler.init()

        // 3. Inspect Model Metadata (Optional but helpful)
        val inputType = compiler.getInputTensorType("input_0")
        // inputType.elementType can be FLOAT, INT, INT8, BOOLEAN, or INT64
        println("Input Type: ${inputType.elementType}, Shape: ${inputType.layout?.dimensions}")
        
        val reqs = compiler.getInputBufferRequirements("input_0")
        println("Required Buffer Size: ${reqs.bufferSize} bytes")

        // 4. Get managed buffers
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 5. Fill input data using typed write operations
        // Supports writeFloat, writeInt, writeInt8, writeBoolean, writeLong
        inputs[0].writeFloat(floatArrayOf(1.0f, 2.0f, 3.0f))

        // 6. Execute inference
        compiler.run(inputs, outputs)

        // 7. Extract results using typed read operations
        val result = outputs[0].readFloat()
        println("Inference result: ${result.contentToString()}")
        
    } finally {
        // 8. Always close to release native resources
        compiler.close()
    }
}
```

### 4. Image Classification
For computer vision models, use `LiteRtImage` for seamless preprocessing (resizing and format conversion).

```kotlin
import io.github.leitingzi.kmplitert.core.*

suspend fun classifyImage(modelPath: String, rawImageBytes: ByteArray) {
    // Specify the accelerator (CPU, GPU, or NPU)
    val compiler = LiteRTCompiler(filePath = modelPath, accelerator = LiteRTAccelerator.GPU)
    
    try {
        compiler.init()
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 1. Preprocessing: Load -> Resize -> Convert to model format
        // LiteRtImage supports: toFloatArray, toInt8Array, toIntArray, toBooleanArray, toLongArray
        val inputData = LiteRtImage.fromBytes(rawImageBytes)
            .resize(224, 224)
            .toFloatArray(mean = 127.5f, std = 127.5f)

        inputs[0].writeFloat(inputData)
        compiler.run(inputs, outputs)

        val result = outputs[0].readFloat()
        val maxIndex = result.indices.maxByOrNull { result[it] }
        println("Identified class index: $maxIndex")
        
    } finally {
        compiler.close()
    }
}
```

---

## 📐 Model Input / Output Metadata

KMPLiteRT provides explicit APIs to query your model's structure. This is essential for dynamic buffer allocation or validating that the model matches your expectations.

- **`getInputTensorType(name)`**: Returns `LiteRTTensorType` containing the `LiteRTElementType` (FLOAT, INT, etc.) and `LiteRTLayout` (dimensions and rank).
- **`getInputBufferRequirements(name)`**: Returns `LiteRTBufferRequirements`, describing the exact memory `bufferSize` and `strides` needed for a custom buffer.

While the application usually knows the model contract, these APIs enable building more generic tools and safer data pipelines.

---

## 🤝 Contributing

Contributions are welcome! If you encounter issues or have ideas for improvements, please:
- Open an **Issue** to report bugs or suggest features.
- Submit a **Pull Request** with your enhancements.

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
