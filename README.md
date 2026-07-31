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
  - [1. High-Level Inference (Recommended)](#1-high-level-inference-recommended)
  - [2. Low-Level Inference](#2-low-level-inference)
- [🛠️ Core API Reference](#️-core-api-reference)
  - [LiteRTCompiler](#litertcompiler)
  - [TFBuffer](#tfbuffer)
  - [LiteRTExt (Data Models)](#literext-data-models)
- [🖼️ Image Processing (LiteRtImage)](#️-image-processing-litertimage)
- [🔊 Audio Processing (LiteRtAudio)](#-audio-processing-litertaudio)
- [⚡ Extensions & Utilities](#-extensions--utilities)
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
            
            // Optional: Image/Audio processing & File utilities
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
tasks.withType<KotlinNativeLink>().configureEach {
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
import io.github.kmplitert.tool.expand.*

class MyClassifier : LiteRTHandler<LiteRtImage, List<LiteRTExt.Category>>() {
    private val labels = listOf("Cat", "Dog", "Bird")

    override suspend fun init() {
        // Setup compiler with model path and accelerator
        setupCompiler("path/to/model.tflite", LiteRTAccelerator.CPU)
    }

    override suspend fun preprocess(input: LiteRtImage, inputBuffers: List<TFBuffer>) {
        // 1. Resize, Normalize and write directly to native buffer
        input.resize(224, 224)
             .toRgb()
             .writeFloatBuffer(inputBuffers[0], mean = 127.5f, std = 127.5f)
    }

    override suspend fun postprocess(outputBuffers: List<TFBuffer>): List<LiteRTExt.Category> {
        // 2. Read output and convert to high-level Category models using extension
        val scores = outputBuffers[0].readFloat()
        return scores.toCategories(labels, threshold = 0.5f)
    }
    
    suspend fun classify(image: LiteRtImage) = runTask(image)
}
```

### 2. Low-Level Inference

For manual control, use `LiteRTCompiler` and extensions for idiomatic Kotlin.

```kotlin
import io.github.kmplitert.core.*
import io.github.kmplitert.tool.expand.*

suspend fun simpleInference(modelPath: String) {
    // 1. Initialize Compiler with DSL-like 'use' block for auto-cleanup
    LiteRTCompiler(modelPath, LiteRTAccelerator.CPU).use { compiler ->
        compiler.init()

        // 2. Prepare I/O Buffers
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 3. Write input data using extensions
        floatArrayOf(1.0f, 2.0f, 3.0f).writeTo(inputs[0])

        // 4. Run inference
        compiler.run(inputs, outputs)

        // 5. Read result directly
        val result = outputs[0].readFloat()
        println("Inference result: ${result.joinToString()}")
    }
}
```

---

## 🛠️ Core API Reference

### `LiteRTHandler<I, O>`
Primary base class for model implementation.
- `runTask(input)`: Orchestrates `preprocess -> run -> postprocess`.
- `setupCompiler(path, accel)`: Thread-safe, automated compiler setup.
- `close()`: Safely releases resources.

### `LiteRTCompiler`
The engine managing the native model.
- `run(inputs, outputs)`: Executes raw inference.
- `getInputBuffers()` / `getOutputBuffers()`: Pre-allocates native buffers.

### `LiteRTExt` (Common Models)
Foundational data structures in `io.github.kmplitert.tool`:
- **General**: `Category`, `BoundingBox`, `Detection`.
- **Vision**: `Face.Result`, `Hand.Gesture`, `Pose.Result`.
- **Segmentation**: `Segmentation.Mask`.
- **NLP**: `Nlp.Tokenizer` interface.
- **OCR**: `Text.Result`, `Text.Block`, `Text.Line`.
- **Video**: `Video.Frame`.

---

## 🖼️ Image Processing (`LiteRtImage`)
Located in `io.github.kmplitert.tool.image`, provides a fluent API:
```kotlin
val processedImage = LiteRtImage.fromBytes(bytes)
    .resize(320, 320)
    .rotate(90f) // Degrees as Float
    .flip(horizontal = true, vertical = false)
    .toRgb()

// Efficiently write to buffer
processedImage.writeFloatBuffer(buffer, mean = 0f, std = 255f)
```

---

## 🔊 Audio Processing (`LiteRtAudio`)
New in `io.github.kmplitert.tool.audio`, simplifies audio tasks:
```kotlin
import io.github.kmplitert.tool.audio.*

// Decode WAV file
val audio = WavDecoder.decode(wavBytes)
val pcmData = audio.data // FloatArray

// Basic signal processing
val normalized = SignalProcessing.normalize(pcmData)
```

---

## ⚡ Extensions & Utilities
The `io.github.kmplitert.tool.expand` package provides powerful extensions:

- **Core Extensions**: `TFBuffer.writeTo(array)`, `TFBuffer.toFloatArray()`, `LiteRTCompiler.use { ... }`.
- **PostProcessing**: `FloatArray.argmax()`, `FloatArray.softmax()`, `FloatArray.toCategories(...)`.
- **NMS**: `performNms(boxes, scores, threshold)` for object detection.

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the **Apache License, Version 2.0**. See `LICENSE` for more information.

---

<p align="center">
  Built with ❤️ for the Kotlin Multiplatform community.
</p>
