# KMPLiteRT 🚀

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20JVM%20%7C%20JS%20%7C%20WasmJS-orange.svg)](#-platform-support)

**KMPLiteRT** is a high-performance Kotlin Multiplatform library that brings **LiteRT** (formerly TensorFlow Lite) to the KMP ecosystem with a unified, type-safe API.

Write your inference logic once in `commonMain` and run it seamlessly across Android, JVM, and Web (JS/Wasm) platforms.

---

## ✨ Features

- 🌍 **True Multiplatform**: One API for Android, Desktop (JVM), and Web (JS & WasmJS).
- ⚡ **Coroutine Powered**: Native support for Kotlin Coroutines with `suspend` functions for non-blocking inference.
- 🔒 **Type-Safe Tensors**: Specialized `TFBuffer` API for `Float`, `Int`, `Long`, `Boolean`, and `Byte` arrays.
- 🚀 **Hardware Acceleration**: Support for CPU, GPU, and NPU/NNAPI (platform dependent).
- 🛠️ **Developer Friendly**: Simplified workflow from model loading to result extraction.

---

## 📱 Platform Support

| Platform | Status | Hardware Acceleration | Backend              |
| :--- | :---: | :--- |:---------------------|
| **Android** | ✅ | CPU / GPU / NNAPI | LiteRT Android SDK   |
| **JVM (Desktop)** | ✅ | CPU | LiteRT C API via JNA |
| **JavaScript** | ✅ | Browser / WebGL | @litertjs/core       |
| **WasmJS** | ✅ | Browser / WebGL | @litertjs/core       |
| **iOS** | 🚧 | *Planned* | LiteRT SPM           |

---

## 📦 Installation

Add the dependency to your `commonMain` source set:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("")
        }
    }
}
```

---

## 💡 Quick Start

Running a LiteRT model is straightforward with KMPLiteRT:

```kotlin
import io.github.leitingzi.kmplitert.core.*

suspend fun runInference() {
    // 1. Initialize the compiler with the model file and preferred accelerator
    val compiler = LiteRTCompiler("model.tflite", LiteRTAccelerator.CPU)
    
    try {
        compiler.init()

        // 2. Get typed buffers
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 3. Prepare input data
        inputs[0].writeFloat(floatArrayOf(1.0f, 2.0f, 3.0f))

        // 4. Run inference
        compiler.run(inputs, outputs)

        // 5. Read results
        val result = outputs[0].readFloat()
        println("Inference result: ${result.joinToString()}")
        
    } finally {
        // 6. Always release resources
        compiler.close()
    }
}
```

### Advanced Workflow

1. **Accelerator Selection**: Choose between `CPU`, `GPU`, or `NPU` via `LiteRTAccelerator`.
2. **Buffer Operations**: `TFBuffer` provides efficient `read`/`write` methods for various data types.
3. **Lifecycle Management**: Always use `compiler.close()` to prevent memory leaks, especially on native/JVM backends.

---

## ⚠️ Current Limitations

- **JVM**: Currently supports CPU inference only.
- **JS / WasmJS**: 
    - Dynamic input shapes are not supported; models must have fixed dimensions.
    - Requires a browser environment for the LiteRT JS runtime.
- **iOS**: Implementation is currently under development.

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
