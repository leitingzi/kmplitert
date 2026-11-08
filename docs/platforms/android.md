# Android

`androidMain` uses the Google LiteRT Android runtime. The current Core build configuration targets Android API 24 and above; its Android dependency supplies the runtime.

## Provide a model file

The repository's Android device test copies the model from assets to an accessible file and passes its absolute path to `LiteRTCompiler`. For example, put the model in the application's `src/main/assets/` and prepare a file:

```kotlin
import android.content.Context
import java.io.File

fun copyModel(context: Context): File {
    val modelFile = File(context.cacheDir, "CelsiusToFahrenheit.tflite")
    context.assets.open("CelsiusToFahrenheit.tflite").use { input ->
        modelFile.outputStream().use { output -> input.copyTo(output) }
    }
    return modelFile
}
```

File copying involves disk I/O; run it on an appropriate background dispatcher. Pass `modelFile.absolutePath` to the [inference example](../getting-started.md).

`LiteRTFileUtils` belongs to the optional Tool module and does not need initialization when using Core alone.

## Hardware and verification

CPU, GPU, and NPU selection is handled by Android LiteRT; actual capability depends on the device, model, and runtime. The repository has an on-device CPU test, but Core CI does not list it as an execution step; verify on your target device or emulator.

If initialization fails, check that the model exists, is readable, and is complete, then investigate device and accelerator compatibility.
