# Installation and First Inference

Start with a real temperature conversion model: give it `100°C` and get approximately `212°F`.

## 1. Add the dependency

This guide describes the Core source in the current repository. Clone the repository and configure your Kotlin Multiplatform application module within it; the root `settings.gradle.kts` already includes `:core`. Add the project dependency to the application's `commonMain`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
    }
}
```

Enable the relevant platform targets in your application and follow the [platform guides](platforms.md) to configure the runtime. The preprocessing tools are a separate optional module, not a prerequisite for Core.

!!! warning "Maven Central publication status"
    As of 2026-11-08, the repository version is `0.3.0`, but neither `io.github.leitingzi:core:0.3.0` nor the older `io.github.leitingzi:kmplitert-core:0.3.0` was found on Maven Central. The current Core build publishes under `io.github.leitingzi:core`. Before using a Maven dependency, check the [published versions](https://central.sonatype.com/artifact/io.github.leitingzi/core) and their APIs; do not assume older versions behave like the current source.

## 2. Prepare the model

The example uses [CelsiusToFahrenheit.tflite](https://github.com/leitingzi/kmplitert/blob/master/core/src/commonTest/resources/CelsiusToFahrenheit.tflite) from this repository. Download it and make it available to the application:

- Android, JVM, and Native: provide a file path accessible to the runtime.
- JS and WasmJS: serve it as a static web resource and provide a URL accessible to the browser.

The input is named `input_c` and the output `Identity`; both are `FLOAT` tensors with shape `[1, 1]`. Other models require their own names, types, and data lengths. See the [platform guides](platforms.md) for details.

## 3. Run inference {#inference}

Call the function from a coroutine; `modelPath` is the path or URL prepared above.

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

The result should be close to `212f`; the repository test uses a tolerance of `1f`. The example closes acquired buffers before the compiler, matching the Native test. See [lifecycle](guide/lifecycle.md) for platform-specific ownership.

!!! note "Model inputs are not interchangeable"
    Do not write `100f` to an arbitrary model. Check its input type, shape, ordering, and quantization requirements before preparing data.

## Next steps

Learn about the [inference lifecycle](guide/lifecycle.md), configure your [platform](platforms.md), or consult [troubleshooting](troubleshooting.md).
