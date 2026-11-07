# KMPLiteRT

**KMPLiteRT** 是一款面向 Kotlin Multiplatform 的 LiteRT（原 TensorFlow Lite）推理库。  
该库提供跨平台统一的 Kotlin API，支持 Android、iOS、JVM、Windows、Linux、macOS、JS 以及 WasmJS平台。  
可供应用加载 LiteRT 模型、准备张量数据并执行本地推理计算。

## 能做什么

- 在共享 Kotlin 代码中定义推理流程，由各平台适配层对接底层 LiteRT 运行时。
- **Core 模块**：通过 `LiteRTCompiler` 执行模型推理，借助 `TFBuffer` 完成张量读写。
- 可按需选用 CPU、GPU、NPU 加速器；硬件加速能力受目标平台与模型本身限制。
- **Tool 模块（可选）**：封装数据预处理与后处理逻辑，例如图像转模型输入张量、模型输出结果解析转换。

## 快速开始

### 添加依赖

#### 直接添加

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.leitingzi:kmplitert-core:0.3.0")
            implementation("io.github.leitingzi:kmplitert-tool:0.3.0") // 可选
        }
    }
}
```

#### 使用版本目录

```toml
[versions]
kmplitert = "0.3.0"

[libraries]
kmplitert-core = { module = "io.github.leitingzi:kmplitert-core", version.ref = "kmplitert" }
kmplitert-tool = { module = "io.github.leitingzi:kmplitert-tool", version.ref = "kmplitert" }
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kmplitert.core)
            implementation(libs.kmplitert.tool) // 可选
        }
    }
}
```

`kmplitert-core` 和 `kmplitert-tool` 为相互独立的模块。  
如果仅需要模型加载与推理能力，单独引入 `kmplitert-core` 即可。  
`kmplitert-tool` 仅提供配套工具 API，用于简化 `kmplitert-core` 的输入、输出数据处理。

### 运行推理

```kotlin
suspend fun runInference(modelPath: String): FloatArray {
    val compiler = LiteRTCompiler(modelPath)
    try {
        compiler.init()
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        inputs[0].writeFloat(floatArrayOf(1f, 2f, 3f))
        compiler.run(inputs, outputs)
        return outputs[0].readFloat()
    } finally {
        compiler.close()
    }
}
```

使用 `LiteRTCompiler` 前，必须调用 `init()` 完成初始化；使用完毕后，务必调用 `close()` 释放关联资源。  
示例中的 `modelPath` 需要是目标平台可正常访问的模型文件路径。
