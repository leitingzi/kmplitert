<div class="space-hero" markdown="1">

<span class="eyebrow">Kotlin Multiplatform · LiteRT</span>

# 让端侧 AI，<br>随 Kotlin 一起运行

<p class="lead">KMPLiteRT 是面向 LiteRT 的 Kotlin Multiplatform 运行时。用一套类型安全的推理流程，把模型带到 Android、iOS、桌面与浏览器。</p>

<div class="actions">
  <a class="md-button md-button--primary" href="getting-started/">开始使用 →</a>
  <a class="md-button" href="dataprocess/">探索数据处理</a>
  <a class="md-button" href="demo/">试用在线示例</a>
</div>

</div>

<div class="capability-strip" markdown="1">

**共享 Kotlin API** · **类型安全张量缓冲区** · **图像与音频工具** · **浏览器在线示例**

</div>

## 从模型到产品的路径

<div class="grid cards" markdown="1">

-   **01 / 快速运行**

    ---

    从安装依赖到运行第一个温度转换模型。

    [开始第一次推理 →](getting-started.md)

-   **02 / 构建数据流程**

    ---

    准备图像或音频，并写入模型所需的准确张量。

    [探索数据处理 →](dataprocess.md)

-   **03 / 理解运行时**

    ---

    了解模型生命周期、类型化缓冲区、硬件委托与资源所有权。

    [探索 Core →](guide/lifecycle.md)

-   **04 / 选择平台**

    ---

    查看 Android、JVM、Native、JavaScript 与 WasmJS 的集成细节。

    [查看平台指南 →](platforms.md)

</div>

## 一个模型，一条可预期的生命周期

`LiteRTCompiler` 管理模型会话，`TFBuffer` 负责精确类型的张量读写。可选的 `dataproces` 模块可在共享 Kotlin 代码中将原始图像和音频转换为模型所需的数组。

```kotlin
compiler.init()
val inputs = compiler.getInputBuffers()
val outputs = compiler.getOutputBuffers()
inputs.single().writeFloat(floatArrayOf(100f))
compiler.run(inputs, outputs)
val fahrenheit = outputs.single().readFloat().single() // 约 212f
```

这条流程同样驱动[浏览器在线示例](demo.md)。完整示例还包含模型准备和资源释放，请从[快速开始](getting-started.md)继续。
