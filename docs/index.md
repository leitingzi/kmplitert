<div class="space-hero" markdown="1">

<span class="eyebrow">Kotlin Multiplatform · LiteRT</span>

# One inference flow,<br>across platforms

<p class="lead">Bring LiteRT to Kotlin. Load models, read and write tensors, and run inference in shared code across mobile, desktop, and the web.</p>

<div class="actions">
  <a class="md-button md-button--primary" href="getting-started/">Get started →</a>
  <a class="md-button" href="core-api/">Explore the Core API</a>
  <a class="md-button" href="demo/">Try the live demo</a>
</div>

</div>

## Find your orbit

<div class="grid cards" markdown="1">

-   **01 / Get started**

    ---

    Install Core and run your first temperature conversion model.

    [Run your first inference →](getting-started.md)

-   **02 / Understand Core**

    ---

    Learn about the model lifecycle, tensors, buffers, and accelerators.

    [Explore the inference lifecycle →](guide/lifecycle.md)

-   **03 / Choose a platform**

    ---

    Find integration details for Android, JVM, Native, and the web.

    [Browse platform guides →](platforms.md)

-   **04 / Browse the API**

    ---

    Look up shared interfaces, data types, and platform differences.

    [Read the API reference →](core-api.md)

</div>

## A small, focused core

`LiteRTCompiler` manages model inference, while `TFBuffer` reads and writes tensors. Core focuses on the runtime; applications can build preprocessing and postprocessing on top of its typed tensor APIs.

```kotlin
compiler.init()
val inputs = compiler.getInputBuffers()
val outputs = compiler.getOutputBuffers()
inputs.single().writeFloat(floatArrayOf(100f))
compiler.run(inputs, outputs)
val fahrenheit = outputs.single().readFloat().single() // approximately 212f
```

This is a flow overview. For model preparation and resource cleanup, follow the [quick start](getting-started.md).
