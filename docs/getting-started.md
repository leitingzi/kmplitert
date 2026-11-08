# 安装与第一次推理

从一个真实的温度转换模型开始：输入 `100°C`，得到约 `212°F`。

## 1. 添加依赖

本文对应当前仓库中的 Core 源码。先克隆仓库，并在仓库中配置你的 Kotlin Multiplatform 应用模块；根项目的 `settings.gradle.kts` 已包含 `:core`。在应用模块的 `commonMain` 中添加项目依赖：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
    }
}
```

启用应用模块对应的平台目标，并按[平台指南](platforms.md)配置运行环境。模型预处理工具是独立的可选模块，不是运行 Core 的前提。

!!! warning "Maven Central 发布状态"
    截至 2026-11-08，仓库版本目录为 `0.3.0`，但 Maven Central 上尚未找到 `io.github.leitingzi:core:0.3.0` 或旧坐标 `io.github.leitingzi:kmplitert-core:0.3.0`。当前 Core 构建配置使用的发布坐标是 `io.github.leitingzi:core`。使用 Maven 依赖前，请核对[已发布版本](https://central.sonatype.com/artifact/io.github.leitingzi/core)及其 API；不要假设旧版与当前源码行为一致。

## 2. 准备模型

示例使用仓库中的 [CelsiusToFahrenheit.tflite](https://github.com/leitingzi/kmplitert/blob/master/core/src/commonTest/resources/CelsiusToFahrenheit.tflite)。下载该文件并按目标平台提供给应用：

- Android、JVM 和 Native：传入运行时可访问的文件路径。
- JS 和 WasmJS：将模型作为 Web 静态资源提供，并传入浏览器可访问的 URL。

模型输入名为 `input_c`，输出名为 `Identity`；两者均为 `FLOAT`，shape 为 `[1, 1]`。其他模型需要使用自己的名称、类型和数据长度。具体操作见[平台指南](platforms.md)。

## 3. 执行推理 {#inference}

在协程中调用以下函数；`modelPath` 是上一步准备的路径或 URL。

```kotlin
import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler
import io.github.kmplitert.core.TFBuffer

suspend fun celsiusToFahrenheit(modelPath: String): Float {
    val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
    val buffers = mutableListOf<TFBuffer>()
    try {
        compiler.init()
        val inputs = compiler.getInputBuffers().also { buffers.addAll(it) }
        val outputs = compiler.getOutputBuffers().also { buffers.addAll(it) }

        inputs.single().writeFloat(floatArrayOf(100f))
        compiler.run(inputs, outputs)
        return outputs.single().readFloat().single()
    } finally {
        try {
            buffers.forEach { it.close() }
        } finally {
            compiler.close()
        }
    }
}
```

运行结果应接近 `212f`；仓库测试使用 `1f` 的容差。此示例先关闭已获得的缓冲区，再关闭编译器，与 Native 测试的释放顺序一致；不同平台的所有权细节见[生命周期](guide/lifecycle.md)。

!!! note "模型不是通用输入格式"
    不要直接对任意模型写入 `100f`。先检查模型的输入类型、形状、顺序及量化要求，再准备匹配的数据。

## 接下来

了解[推理生命周期](guide/lifecycle.md)，或者前往[对应平台](platforms.md)配置模型和运行环境。遇到问题时查看[故障排查](troubleshooting.md)。
