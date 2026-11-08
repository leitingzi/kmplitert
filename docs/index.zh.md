# KMPLiteRT 文档

KMPLiteRT 让 Kotlin Multiplatform 应用能够运行 LiteRT 模型。使用 **Core** 加载模型并执行推理；只有需要图像或音频预处理时，才添加 **Data Processing**。

<div class="grid cards" markdown>

-   **快速开始**

    ---

    添加 Core、准备模型并完成第一次推理。

    [安装并运行第一次推理 →](getting-started.md)

-   **Core 推理**

    ---

    了解模型生命周期、张量缓冲区与硬件加速。

    [了解 Core →](guide/lifecycle.md)

-   **数据处理**

    ---

    解码、变换和归一化图像或音频输入。

    [了解数据处理 →](dataprocess.md)

-   **平台指南**

    ---

    配置 Android、JVM、Apple、Native、JavaScript 或 WasmJS。

    [选择平台 →](platforms.md)

</div>

## 两个模块，各司其职

| 模块 | 职责 | 适用场景 |
|---|---|---|
| **Core** | 加载 LiteRT 模型、管理张量缓冲区并执行推理。 | 应用已经准备好符合模型要求的张量。 |
| **Data Processing**（`dataproces`） | 解码和变换图像或音频，生成模型所需的数据。 | 推理前还需要数据预处理。 |

Core 可单独使用，不依赖 Data Processing。Data Processing 是可选模块；它仅为直接写入 `TFBuffer` 而依赖 Core。

## 继续阅读

- [试用浏览器在线示例](demo.md)
- [查看 Core API](core-api.md)
- [排查常见问题](troubleshooting.md)
