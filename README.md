# KMPLiteRT 🚀

<p align="center">
  <b>High-performance, type-safe Kotlin Multiplatform (KMP) library for Google LiteRT (TensorFlow Lite).</b>
  <br>
  <i>Bringing on-device Machine Learning to every screen, from Mobile to Web and Desktop.</i>
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
  <a href="http://kotlinlang.org"><img src="https://img.shields.io/badge/kotlin-2.1.0-purple.svg?logo=kotlin" alt="Kotlin"></a>
  <a href="https://central.sonatype.com/artifact/io.github.leitingzi/kmplitert-core"><img src="https://img.shields.io/maven-central/v/io.github.leitingzi/kmplitert-core" alt="Maven Central"></a>
  <a href="https://github.com/leitingzi/kmplitert/actions"><img src="https://img.shields.io/github/actions/workflow/status/leitingzi/kmplitert/core-ci.yml?branch=master" alt="CI Status"></a>
</p>

---

## 📖 Table of Contents

- [🌟 Introduction](#-introduction)
- [🏗️ Architecture](#️-architecture)
- [📊 Platform Support & Acceleration](#-platform-support--acceleration)
- [📦 Installation](#-installation)
- [🍎 iOS & Native Setup (Critical)](#-ios--native-setup-critical)
- [🚀 Quick Start (Real-world Examples)](#-quick-start-real-world-examples)
  - [1. Numeric Regression (Celsius to Fahrenheit)](#1-numeric-regression-celsius-to-fahrenheit)
  - [2. Vision Classification (MobileNet)](#2-vision-classification-mobilenet)
- [🛠️ Core API Reference](#️-core-api-reference)
  - [LiteRTCompiler](#litertcompiler)
  - [TFBuffer](#tfbuffer)
  - [LiteRTFileUtils](#litertfileutils)
- [🖼️ Image Processing (LiteRtImage)](#️-image-processing-litertimage)
- [⚡ Performance & Hardware Acceleration](#-performance--hardware-acceleration)
- [🛑 Troubleshooting & FAQ](#-troubleshooting--faq)
- [🗺️ Roadmap](#️-roadmap)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

---

## 🌟 Introduction

**KMPLiteRT** is a powerful bridge between the **Google AI Edge LiteRT** (formerly TensorFlow Lite) ecosystem and the **Kotlin Multiplatform** world. While TensorFlow Lite is the industry standard for on-device AI, integrating it into a cross-platform project often involves complex native interop, memory management issues, and inconsistent behavior across platforms.

KMPLiteRT solves these problems by providing:
- **Unified DSL**: Write your inference logic once in `commonMain` and run it anywhere.
- **Zero-Copy Performance**: Direct native memory access via `TFBuffer` ensures minimal overhead.
- **Hardware First**: Seamless integration with **Metal (Apple)**, **NNAPI (Android)**, and **WebGPU (Web)**.
- **Developer Friendly**: Type-safe APIs, coroutine support, and high-level image processing tools.

---

## 🏗️ Architecture

KMPLiteRT abstracts platform-specific runtimes into a consistent Kotlin interface. It doesn't just wrap the APIs; it harmonizes the data types and execution models.

```mermaid
graph TD
    subgraph "Your KMP Application"
        UI["Compose Multiplatform UI"]
        Logic["commonMain (Shared Logic)"]
    end

    subgraph "KMPLiteRT Common API"
        API["LiteRTCompiler & TFBuffer"]
    end

    subgraph "Platform Implementations"
        Android["Android (Official SDK / JNI)"]
        iOS["Apple Targets (C-API / Metal)"]
        Desktop["Desktop (JNA / C-Interop)"]
        Web["Web (JS/Wasm / @litertjs)"]
    end

    Logic --> API
    API --> Android & iOS & Desktop & Web
    
    subgraph "Accelerators"
        NPU["NNAPI / CoreML"]
        GPU["GPU (Metal/Vulkan/WebGPU)"]
        CPU["XNNPACK (Optimized CPU)"]
    end

    Android -.-> NPU & GPU
    iOS -.-> GPU
    Web -.-> NPU & GPU
```

---

## 📊 Platform Support & Acceleration

| Platform | Target Support | Runtime Engine | CPU (XNNPACK) | GPU Accel. | NPU Accel. |
| :--- | :--- | :--- | :---: | :---: | :---: |
| **Android** | API 21+ | Official LiteRT SDK | ✅ | ✅ (GLES) | ✅ (NNAPI) |
| **iOS** | 15.0+ | Native C-API | ✅ | ✅ (Metal) | ✅ (CoreML) |
| **macOS** | 12.0+ | Native C-API | ✅ | ✅ (Metal) | ✅ (CoreML) |
| **Windows** | x64 | Native C-API | ✅ | ✅ (Vulkan) | 🚧 |
| **Linux** | x64 / ARM64 | Native C-API | ✅ | ✅ (OpenGL) | 🚧 |
| **Web** | JS / Wasm | @litertjs/core | ✅ | ✅ (WebGPU) | ✅ (WebNN) |

---

## 📦 Installation

### 1. Add Repository & Version

Add KMPLiteRT to your `commonMain` source set in `build.gradle.kts`.

```kotlin
val kmplitertVersion = "0.1.4" // Replace with latest

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The core ML runtime engine
            implementation("io.github.leitingzi:kmplitert-core:$kmplitertVersion")
            
            // Optional: Image processing & File utilities
            implementation("io.github.leitingzi:kmplitert-tool:$kmplitertVersion")
        }
    }
}
```

### 🤖 Android Setup

On Android, initialization is required to handle assets correctly:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Required for LiteRTFileUtils to load models from assets
        LiteRTFileUtils.init(this)
    }
}
```

---

## 🍎 iOS & Native Setup (Critical)

Dynamic linking is **mandatory** for iOS and Apple targets to support hardware delegates like Metal and to ensure binary compatibility.

### 1. Framework Configuration

In your `shared` module's `build.gradle.kts`, configure your framework to be **dynamic** and set the linker search paths.

```kotlin
kotlin {
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())
    
    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "SharedLib"
            isStatic = false // MUST BE FALSE
            
            // Link against the LiteRT dynamic library
            linkerOpts("-lLiteRt", "-lc++")
            
            // Configure rpath for the system to find the dylib inside the framework
            linkerOpts(
                "-Wl,-rpath,@executable_path/Frameworks",
                "-Wl,-rpath,@loader_path/Frameworks",
                "-Wl,-rpath,@loader_path/../../Frameworks"
            )
        }
    }
}
```

### 2. Automatic Binary Bundling

The `libLiteRt.dylib` must be physically present in your `.framework/Frameworks` directory. You can automate this via Gradle:

```kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>().configureEach {
    val target = binary.target.konanTarget
    if (target.isApple) {
        doLast {
            val destination = destinationDirectory.get().asFile
            val frameworkDir = File(destination, "${binary.baseName}.framework")
            val frameworksFolder = File(frameworkDir, "Frameworks").apply { mkdirs() }
            
            // Path to where you store the prebuilt LiteRT binaries
            val libPath = File(project.rootDir, "native-libs/ios/${target.name}")
            
            copy {
                from(libPath)
                include("*.dylib")
                into(frameworksFolder)
            }
        }
    }
}
```

---

## 🚀 Quick Start (Real-world Examples)

### 1. High-Level Inference (Recommended)

The best way to implement a model is by inheriting from `LiteRTHandler`. It handles the orchestration and lifecycle for you.

```kotlin
import io.github.kmplitert.core.*
import io.github.kmplitert.tool.*
import io.github.kmplitert.tool.image.LiteRtImage

class MyClassifier : LiteRTHandler<LiteRtImage, List<Category>>() {
    override suspend fun init() {
        // 1. Prepare model path (e.g. from resources)
        val path = "path/to/model.tflite"
        // 2. Setup compiler (automatically calls compiler.init())
        setupCompiler(path, LiteRTAccelerator.CPU)
    }

    override suspend fun preprocess(input: LiteRtImage, inputBuffers: List<TFBuffer>) {
        val data = input.resize(224, 224).toRgb().toInt8Array()
        inputBuffers[0].writeInt8(data)
    }

    override suspend fun postprocess(outputBuffers: List<TFBuffer>): List<Category> {
        val scores = outputBuffers[0].readInt8()
        // ... transform scores to List<Category>
        return listOf(Category("Example", 0.9f, 0))
    }
    
    // Custom friendly entry point
    suspend fun classify(image: LiteRtImage) = runTask(image)
}
```

### 2. Low-Level Inference

For manual control, you can use `LiteRTCompiler` and `TFBuffer` directly.

```kotlin
import io.github.kmplitert.core.*
import io.github.kmplitert.tool.LiteRTFileUtils

suspend fun simpleInference(modelBytes: ByteArray) {
    // 1. Prepare model file
    val filePath = LiteRTFileUtils.createFileFromByteArray(modelBytes, "temp_model.tflite")
    
    // 2. Initialize Compiler
    val compiler = LiteRTCompiler(filePath, LiteRTAccelerator.CPU)
    compiler.init()

    // 3. Prepare I/O
    val inputs = compiler.getInputBuffers()
    val outputs = compiler.getOutputBuffers()

    // 4. Input & Run
    inputs[0].writeFloat(floatArrayOf(100f))
    compiler.run(inputs, outputs)

    // 5. Output result
    val result = outputs[0].readFloat()
    println("Result: ${result[0]}")

    // 6. Cleanup
    compiler.close()
}
```

---

## 🛠️ Core API Reference

### `LiteRTHandler<I, O>`
The primary base class for model implementation.
- `init()`: Initialize resources and call `setupCompiler`.
- `runTask(input)`: Orchestrates `preprocess -> run -> postprocess`.
- `setupCompiler(path, accel)`: Thread-safe, automated compiler setup.
- `close()`: Safely releases the underlying compiler.

### `LiteRTCompiler`
The low-level engine managing the native model.
- `run(inputs, outputs)`: Executes raw inference.
- `getInputBuffers()` / `getOutputBuffers()`: Pre-allocates native buffers.

### 🖼️ Image Processing (`LiteRtImage`)
Located in `io.github.kmplitert.tool.image`, provides a fluent API for vision models:
```kotlin
import io.github.kmplitert.tool.image.*

val data = LiteRtImage.fromBytes(bytes)
    .resize(320, 320)
    .rotate(ImageRotation.ROTATION_90)
    .flip(ImageFlip(horizontal = true))
    .toRgb()
    .toInt8Array()
```

### 📊 Foundational Data Types
Standardized structures for common ML tasks:
- `Category`: Label, score, and index.
- `BoundingBox`: Normalized or pixel coordinates.
- `Detection`: A `BoundingBox` coupled with a list of `Category`.
- `SegmentationMask`: Raw mask data with accessors.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the **Apache License, Version 2.0**. See `LICENSE` for more information.

```text
Copyright 2026 yebintang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<p align="center">
  Built with ❤️ for the Kotlin Multiplatform community.
</p>
