<div class="space-hero" markdown="1">

<span class="eyebrow">Kotlin Multiplatform · LiteRT</span>

# Ship on-device AI,<br>where Kotlin runs

<p class="lead">KMPLiteRT is the Kotlin Multiplatform runtime for LiteRT. Build a single, typed inference pipeline and take it from Android and iOS to desktop and the browser.</p>

<div class="actions">
  <a class="md-button md-button--primary" href="getting-started/">Get started →</a>
  <a class="md-button" href="dataprocess/">Explore data processing</a>
  <a class="md-button" href="demo/">Try the live demo</a>
</div>

</div>

<div class="capability-strip" markdown="1">

**Shared Kotlin API** · **Typed tensor buffers** · **Image & audio tools** · **Browser demo**

</div>

## A route from model to product

<div class="grid cards" markdown="1">

-   **01 / Get started**

    ---

    Install Core and run your first temperature conversion model.

    [Run your first inference →](getting-started.md)

-   **02 / Build the pipeline**

    ---

    Prepare images or audio, then write exactly the tensor your model expects.

    [Explore data processing →](dataprocess.md)

-   **03 / Understand the runtime**

    ---

    Learn model lifecycle, typed buffers, delegates, and resource ownership.

    [Explore Core →](guide/lifecycle.md)

-   **04 / Choose a platform**

    ---

    Find integration details for Android, JVM, Native, JavaScript, and WasmJS.

    [Browse platform guides →](platforms.md)

</div>

## One model. A predictable lifecycle.

`LiteRTCompiler` owns a model session. `TFBuffer` moves precisely typed tensor data in and out. The optional `dataproces` module turns raw image and audio inputs into model-ready arrays without leaving shared Kotlin code.

```kotlin
compiler.init()
val inputs = compiler.getInputBuffers()
val outputs = compiler.getOutputBuffers()
inputs.single().writeFloat(floatArrayOf(100f))
compiler.run(inputs, outputs)
val fahrenheit = outputs.single().readFloat().single() // approximately 212f
```

The same flow powers the [live browser demo](demo.md). For model preparation and resource cleanup, follow the [quick start](getting-started.md).
