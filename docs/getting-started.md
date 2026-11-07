# 安装与快速开始

## 添加依赖

在项目的 `commonMain` source set 中添加需要的模块。版本号应与 [Maven Central](https://central.sonatype.com/artifact/io.github.leitingzi/kmplitert-core) 上已发布的版本一致。

```kotlin
val kmplitertVersion = "0.3.0"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.leitingzi:kmplitert-core:$kmplitertVersion")

            // 可选：高层任务流程和图像、音频等工具
            implementation("io.github.leitingzi:kmplitert-tool:$kmplitertVersion")
        }
    }
}
```

只需要模型加载和推理时，可以只添加 `kmplitert-core`。`kmplitert-tool` 依赖 Core，并提供更高层的任务处理 API。

## 基础推理流程

下面的示例展示了使用 Core API 的基本步骤。输入数据的类型、长度和顺序必须符合模型输入张量的要求；输出也应按模型定义解读。

```kotlin
import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler

suspend fun runInference(modelPath: String): FloatArray {
    val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
    try {
        compiler.init()

        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()

        // 示例输入仅用于展示 API；实际长度和数值需匹配模型输入。
        inputs.first().writeFloat(floatArrayOf(1f, 2f, 3f))
        compiler.run(inputs, outputs)
        return outputs.first().readFloat()
    } finally {
        compiler.close()
    }
}
```

`LiteRTCompiler` 使用后必须调用 `close()` 释放关联资源。示例中的 `modelPath` 必须是目标平台可以访问的模型文件路径。

## Android 模型文件

如果要从 Android `assets` 加载模型，可在应用启动时初始化工具模块的文件工具：

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LiteRTFileUtils.init(this)
    }
}
```

此初始化仅适用于 Android。请确保模型文件已放入应用资源，并按照当前版本 `LiteRTFileUtils` 的 API 获取模型路径。

## 选择加速器

```kotlin
LiteRTAccelerator.CPU
LiteRTAccelerator.GPU
LiteRTAccelerator.NPU
```

请求某个加速器不代表每台设备都支持它。实际可用性还取决于平台后端、设备驱动、模型算子和运行时配置；建议在目标设备上验证，并准备 CPU 路径作为回退方案。
