# 安装与快速开始

## 添加依赖

在项目的 `commonMain` source set 中添加需要的模块。版本号应与 [Maven Central](https://central.sonatype.com/artifact/io.github.leitingzi/kmplitert-core) 上已发布的版本一致。

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            val kmplitert = "0.3.0"
            implementation("io.github.leitingzi:kmplitert-core:$kmplitert")
            implementation("io.github.leitingzi:kmplitert-tool:$kmplitert") // 可选：提供数据预处理和后期处理
        }
    }
}
```

`kmplitert-core` 和 `kmplitert-tool` 为相互独立模块。  
如果仅需要模型加载与推理能力，单独引入 `kmplitert-core` 即可。  
`kmplitert-tool` 仅提供配套工具 API，用于简化 `kmplitert-core` 的输入、输出数据处理。

## 基础推理流程

下方示例演示 Core API 的基础使用流程。输入数据的类型、长度与排布顺序必须匹配模型输入张量规格；输出结果同样需要按照模型定义进行解析。

```kotlin
import io.github.kmplitert.core.LiteRTAccelerator
import io.github.kmplitert.core.LiteRTCompiler

// 示例输入仅用于展示 API；实际长度和数值需匹配模型输入。
suspend fun runInference(modelPath: String): FloatArray {
    val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
    try {
        // 初始化
        compiler.init()
        // 获取输入和输出张量列表
        val inputs = compiler.getInputBuffers()
        val outputs = compiler.getOutputBuffers()
        // 填充输入张量数据
        inputs.first().writeFloat(floatArrayOf(1f, 2f, 3f))
        // 运行推理
        compiler.run(inputs, outputs)
        // 读取输出张量数据
        return outputs.first().readFloat()
    } finally {
        // 释放资源
        compiler.close()
    }
}
```

使用 `LiteRTCompiler` 前，必须调用 `init()` 完成初始化；使用完毕后，务必调用 `close()` 释放关联资源。  
示例中的 `modelPath` 需要是目标平台可正常访问的模型文件路径。