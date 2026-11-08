<div class="space-hero" markdown="1">

<span class="eyebrow">Kotlin Multiplatform · LiteRT</span>

# 一份推理逻辑，<br>跨越不同平台

<p class="lead">用 Kotlin 连接 LiteRT。在共享代码中加载模型、读写张量并执行推理，让应用从移动端延伸到桌面与 Web。</p>

<div class="actions">
  <a class="md-button md-button--primary" href="getting-started/">开始使用 →</a>
  <a class="md-button" href="core-api/">探索 Core API</a>
</div>

</div>

## 从这里出发

<div class="grid cards" markdown>

-   **01 / 快速运行**

    ---

    从安装依赖到运行第一个温度转换模型。

    [开始第一次推理 →](getting-started.md)

-   **02 / 理解 Core**

    ---

    掌握模型生命周期、张量、缓冲区和加速器。

    [了解推理流程 →](guide/lifecycle.md)

-   **03 / 选择平台**

    ---

    找到 Android、JVM、Native 与 Web 的接入细节。

    [查看平台指南 →](platforms.md)

-   **04 / 查阅 API**

    ---

    查询公共接口、数据类型和平台行为差异。

    [查看 API 参考 →](core-api.md)

</div>

## 小而清晰的核心

`LiteRTCompiler` 管理模型推理，`TFBuffer` 负责张量读写。Core 只关注运行时；数据预处理和后处理可以按需使用独立的 Tool 模块。

```kotlin
compiler.init()
val inputs = compiler.getInputBuffers()
val outputs = compiler.getOutputBuffers()
inputs.single().writeFloat(floatArrayOf(100f))
compiler.run(inputs, outputs)
val fahrenheit = outputs.single().readFloat().single() // 约 212f
```

上面是流程概览；完整示例还包含模型准备和资源释放，请从[快速开始](getting-started.md)继续。
